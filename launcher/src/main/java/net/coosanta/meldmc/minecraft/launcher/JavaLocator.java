package net.coosanta.meldmc.minecraft.launcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class JavaLocator {
    public static Optional<Path> javaPathFromPid() {
        return ProcessHandle.of(ProcessHandle.current().pid())
                .flatMap(h -> h.info().command())
                .map(Paths::get)
                .map(JavaLocator::toRealPathIfPossible)
                .filter(Files::isRegularFile);
    }

    private static Path toRealPathIfPossible(Path p) {
        try {
            return p.toRealPath();
        } catch (Exception ignore) {
            return p.toAbsolutePath();
        }
    }

    private JavaLocator() {
    }
}