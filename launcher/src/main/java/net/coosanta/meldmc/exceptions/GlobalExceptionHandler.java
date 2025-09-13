package net.coosanta.meldmc.exceptions;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.coosanta.meldmc.gui.views.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class GlobalExceptionHandler implements UncaughtExceptionHandler {
    public static final GlobalExceptionHandler INSTANCE = new GlobalExceptionHandler();
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private GlobalExceptionHandler() {
    }

    public static void installGlobal() {
        Thread.setDefaultUncaughtExceptionHandler(INSTANCE);
    }

    public static void installOnFxThread() {
        Thread.currentThread().setUncaughtExceptionHandler(INSTANCE);
    }

    public static ThreadFactory threadFactory(String namePrefix) {
        AtomicInteger n = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r, namePrefix + "-" + n.getAndIncrement());
            t.setUncaughtExceptionHandler(INSTANCE);
            return t;
        };
    }

    public static ExecutorService fixedThreadPool(int nThreads, String namePrefix) {
        return Executors.newFixedThreadPool(nThreads, threadFactory(namePrefix));
    }

    public static ExecutorService singleThreadExecutor(String namePrefix) {
        return Executors.newSingleThreadExecutor(threadFactory(namePrefix));
    }

    public static CompletableFuture<Void> runAsync(Runnable task, Executor executor) {
        return CompletableFuture
                .runAsync(task, executor)
                .exceptionally(ex -> {
                    Platform.runLater(() -> handle(ex));
                    return null;
                });
    }


    public static void handle(Throwable t) {
        INSTANCE.uncaughtException(Thread.currentThread(), t);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception in thread '{}': {}", t.getName(), e.getMessage(), e);

        if (Platform.isFxApplicationThread()) {
            showExceptionUI(e);
        } else {
            try {
                Platform.runLater(() -> showExceptionUI(e));
            } catch (IllegalStateException notStarted) {
                throw new RuntimeException("The exception handler failed to show GUI and could not handle it's own exception :(", e);
            }
        }
    }

    private void showExceptionUI(Throwable e) {
        var win = MainWindow.getInstance();
        if (win != null && win.getController() != null) {
            win.getController().showExceptionScreen(e);
            return;
        }
        // Fallback if controller is not ready
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Unexpected error");
        alert.setHeaderText(e.getClass().getSimpleName());
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}
