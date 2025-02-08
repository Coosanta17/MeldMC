package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;
import net.coosanta.totalityloader.server.ServerEntry;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ServerSelect extends JPanel {
    private final Main instance;
    private Tag<?> serversDat;
    private List<ServerEntry> serverList;
    private Path gameDir;

    public ServerSelect() throws IOException {
        this.instance = Main.getInstance();
        this.gameDir = this.instance.getGameDir();

        this.serverList = getServersFromFile();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        serverList.forEach((s) -> add(new ServerOption(s)));
    }

    @SuppressWarnings("unchecked")
    private List<ServerEntry> getServersFromFile() throws IOException {
        Path serverDatFile = gameDir.resolve("servers.dat");
        this.serversDat = NBTUtil.read(serverDatFile.toFile()).getTag();

        if (!(serversDat instanceof ListTag<?> && ((ListTag<?>) serversDat).getTypeClass().equals(ServerEntry.class))) {
            throw new IllegalArgumentException("Invalid NBT tag when getting servers");
        }

        return (List<ServerEntry>) serversDat;
    }

    public class ServerOption extends JPanel {
        ServerOption(ServerEntry server) {
            setLayout(new BorderLayout());

            JTextArea name = new JTextArea(server.getName());
//            JPanel playerCountAndPing = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//            playerCountAndPing.add(server.getServerStat().)
            add(name, BorderLayout.NORTH);

            ImageIcon icon = new ImageIcon(server.getIcon());
            JLabel iconLabel = new JLabel(icon);
            add(iconLabel, BorderLayout.WEST);

            JTextArea ip = new JTextArea(server.getIp());
            add(ip, BorderLayout.CENTER);
        }
    }
}
