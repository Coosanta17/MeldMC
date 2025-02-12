package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;
import net.coosanta.totalityloader.minecraft.ServerInfoWrapper;
import net.coosanta.totalityloader.minecraft.SharedConstantsWrapper;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerSelect extends JPanel {
    private final Main instance;
    private List<ServerInfoWrapper> serverList;
    private Path gameDir;

    public ServerSelect() throws IOException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
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

    private ArrayList<ServerInfoWrapper> getServersFromFile() throws IOException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CompoundTag serversDat;

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
            new SharedConstantsWrapper().callMethod("createGameVersion");
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

    public static class ServerOption extends JPanel {
        ServerOption(ServerInfoWrapper server) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            setLayout(new BorderLayout());

            JTextArea name = new JTextArea(String.valueOf(server.getFieldValue("name")));
            add(name, BorderLayout.NORTH);

            ImageIcon icon = new ImageIcon(String.valueOf(server.callMethod("getFavicon")));
            JLabel iconLabel = new JLabel(icon);
            add(iconLabel, BorderLayout.WEST);

            JTextArea ip = new JTextArea(String.valueOf(server.getFieldValue("address")));
            add(ip, BorderLayout.CENTER);
        }
    }
}
