package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;
import net.coosanta.totalityloader.minecraft.MultiplayerServerListPingerWrapper;
import net.coosanta.totalityloader.minecraft.ServerInfoWrapper;
import net.coosanta.totalityloader.minecraft.SharedConstantsWrapper;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import net.querz.nbt.tag.Tag;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class ServerSelect extends JPanel {
    private final Main instance;
    private final List<ServerInfoWrapper> serverList;
    private final Path gameDir;
    private MultiplayerServerListPingerWrapper pinger = new MultiplayerServerListPingerWrapper();
    private CompoundTag serversDat;

    public ServerSelect() throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.instance = Main.getInstance();
        this.gameDir = this.instance.getGameDir();

        this.serverList = getServersFromFile();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        serverList.forEach((s) -> {
            try {
                add(new ServerOption(s));
            } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private ArrayList<ServerInfoWrapper> getServersFromFile() throws IOException {
        File serversDatFile = gameDir.resolve("servers.dat").toFile();

        if (!serversDatFile.isFile()) {
            serversDat = new CompoundTag();
            serversDat.put("servers", new ListTag<>(CompoundTag.class));
            NBTUtil.write(serversDat, serversDatFile, false);
        } else {
            Tag<?> serversDatRaw = NBTUtil.read(serversDatFile).getTag();
            if (serversDatRaw.getID() != 10)
                throw new IllegalArgumentException("Invalid tags in servers.dat - Expected Compound");
            serversDat = (CompoundTag) serversDatRaw;
        }

        Tag<?> serverDatListTagRaw = serversDat.get("servers");
        if (serverDatListTagRaw.getID() != 9)
            throw new IllegalArgumentException("Invalid tags in servers.dat - Expected List");

        ListTag<?> serverDatListTagUnchecked = (ListTag<?>) serverDatListTagRaw;
        if (serverDatListTagUnchecked.getTypeClass() != CompoundTag.class)
            throw new IllegalArgumentException("Invalid tags in servers.dat List - Expected Compound");

        @SuppressWarnings("unchecked")
        ListTag<CompoundTag> serversDatList = (ListTag<CompoundTag>) serverDatListTagUnchecked;

        try {
            new SharedConstantsWrapper().callStaticMethod("createGameVersion");
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException |
                 NoSuchFieldException | InstantiationException | IOException e) {
            throw new RuntimeException("Error creating game version. ", e);
        }

        ArrayList<ServerInfoWrapper> serverListUnfinished = new ArrayList<>();
        serversDatList.forEach((server) -> {
            try {
                serverListUnfinished.add(new ServerInfoWrapper(server.getString("name"), server.getString("ip"), "OTHER"));
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException | IOException | NullPointerException e) {
                throw new RuntimeException("Error getting server information. ", e.getCause());
            }
        });
        return serverListUnfinished;
    }

    private void saveServersToNbt() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        ListTag<CompoundTag> servers = new ListTag<>(CompoundTag.class);
        for (ServerInfoWrapper server : serverList) {
            CompoundTag serverTag = new CompoundTag();
            @Nullable byte[] favicon = (byte[]) server.callMethod("getFavicon");
            if (favicon != null) {
                serverTag.put("icon", new StringTag(Base64.getEncoder().encodeToString(favicon)));
            }
            serverTag.put("ip", new StringTag((String) server.getFieldValue("address")));
            serverTag.put("name", new StringTag((String) server.getFieldValue("name")));
            servers.add(serverTag);
        }
        try {
            NBTUtil.write(serversDat, gameDir.resolve("servers.dat").toFile(), false);
        } catch (IOException e) {
            throw new RuntimeException("Error saving servers to servers.dat. ", e);
        }
    }

    public class ServerOption extends JPanel {
        ServerOption(ServerInfoWrapper server) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            setLayout(new BorderLayout());
            JLabel ping = new JLabel("Pinging...");
            JLabel playerCount = new JLabel("Unknown");

            JLabel name = new JLabel(String.valueOf(server.getFieldValue("name")));
            add(name, BorderLayout.NORTH);

            Object favicon = server.callMethod("getFavicon");
            ImageIcon icon;
            if (favicon instanceof byte[]) {
                icon = new ImageIcon((byte[]) favicon);
            } else {
                try {
                    BufferedImage unknownServerImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/unknown_server.png")));
                    icon = new ImageIcon(unknownServerImage);
                } catch (IOException | NullPointerException e) {
                    throw new RuntimeException(e);
                }
            }
            JLabel iconLabel = new JLabel(icon);
            add(iconLabel, BorderLayout.WEST);

            JLabel ip = new JLabel(String.valueOf(server.getFieldValue("address")));
            add(ip, BorderLayout.CENTER);

            pinger.callMethod(
                    "add",
                    server.getInstance(), (Runnable) () -> SwingUtilities.invokeLater(() -> {
                        try {
                            saveServersToNbt();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        this.revalidate();
                        this.repaint();
                    }),
                    (Runnable) () -> SwingUtilities.invokeLater(() -> {
                        this.revalidate();
                        this.repaint();
                    }));
        }
    }
}
