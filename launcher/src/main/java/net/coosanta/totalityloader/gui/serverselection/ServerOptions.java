package net.coosanta.totalityloader.gui.serverselection;

import net.coosanta.totalityloader.Main;
import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import net.coosanta.totalityloader.minecraft.ServerInfo;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class ServerOptions extends JPanel implements ScalablePanel {
    private final int designWidth = 600;
    private final int designHeight = 400;
    private double currentScale = 1.0;

    private final Main instance;
    private final List<ServerInfo> serverList;
    private final Path gameDir;

    private CompoundTag serversDat;
    private ExecutorService pingTask = Executors.newFixedThreadPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
    private Logger log = LoggerFactory.getLogger(ServerOptions.class);

    public ServerOptions() throws IOException {
        this.instance = Main.getInstance();
        this.gameDir = this.instance.getGameDir();

        this.serverList = getServersFromFile();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        serverList.forEach((s) -> add(new ServerOption(s, pingTask)));
    }

    private ArrayList<ServerInfo> getServersFromFile() throws IOException {
        File serversDatFile = gameDir.resolve("servers.dat").toFile();

        if (!serversDatFile.isFile()) {
            serversDat = new CompoundTag();
            serversDat.put("servers", new ListTag<>(CompoundTag.class));
            NBTUtil.write(serversDat, serversDatFile, false);
        } else {
            Tag<?> serversDatRaw = NBTUtil.read(serversDatFile).getTag();
            if (serversDatRaw.getID() != 10) {
                throw new IllegalArgumentException("Invalid tags in servers.dat - Expected Compound");
            }
            serversDat = (CompoundTag) serversDatRaw;
        }

        Tag<?> serverDatListTagRaw = serversDat.get("servers");
        if (serverDatListTagRaw.getID() != 9) {
            throw new IllegalArgumentException("Invalid tags in servers.dat - Expected List");
        }

        ListTag<?> serverDatListTagUnchecked = (ListTag<?>) serverDatListTagRaw;
        if (serverDatListTagUnchecked.getTypeClass() != CompoundTag.class)
            throw new IllegalArgumentException("Invalid tags in servers.dat List - Expected Compound");

        @SuppressWarnings("unchecked")
        ListTag<CompoundTag> serversDatList = (ListTag<CompoundTag>) serverDatListTagUnchecked;

        ArrayList<ServerInfo> serverListUnfinished = new ArrayList<>();
        serversDatList.forEach((server) -> serverListUnfinished.add(new ServerInfo(server)));

        return serverListUnfinished;
    }

    public void updateLayout() {
        revalidate();
        repaint();

        for (Component component : getComponents()) {
            if (component instanceof ServerOption) {
                component.invalidate();
            }
        }
    }

    @Override
    public double getDesignWidth() {
        return designWidth;
    }

    @Override
    public double getDesignHeight() {
        return designHeight;
    }

    @Override
    public void applyScale(double scaleFactor) {
        this.currentScale = scaleFactor;

        // Apply to all components in the server list
        for (java.awt.Component comp : getComponents()) {
            if (comp instanceof ServerOption) {
                ((ServerOption) comp).applyScale(scaleFactor);
            }
        }
        refreshGui(this);
    }

    public void pingCleanup() {
        pingTask.shutdown();
        try {
            if (!pingTask.awaitTermination(5, TimeUnit.SECONDS)) {
                pingTask.shutdownNow();
            }
        } catch (InterruptedException e) {
            pingTask.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
