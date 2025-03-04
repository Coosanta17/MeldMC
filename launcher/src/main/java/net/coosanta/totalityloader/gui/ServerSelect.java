package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;
import net.coosanta.totalityloader.minecraft.ServerInfo;
import net.coosanta.totalityloader.network.Pinger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.coosanta.totalityloader.gui.Gui.refreshGui;

public class ServerSelect extends JPanel implements ScalablePanel {
    private final int designWidth = 600;
    private final int designHeight = 400;
    private double currentScale = 1.0;

    private final Main instance;
    private final List<ServerInfo> serverList;
    private final Path gameDir;

    private CompoundTag serversDat;
    private ExecutorService pingTask = Executors.newFixedThreadPool(Math.min(4, Runtime.getRuntime().availableProcessors()));
    private Logger log = LoggerFactory.getLogger(ServerSelect.class);

    public ServerSelect() throws IOException {
        this.instance = Main.getInstance();
        this.gameDir = this.instance.getGameDir();

        this.serverList = getServersFromFile();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        serverList.forEach((s) -> add(new ServerOption(s)));
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

    private class ServerOption extends JPanel {
        private final ServerInfo server;
        private final JPanel header = new JPanel(new GridBagLayout());
        private final MiniMessage miniMessage = MiniMessage.miniMessage();

        private Font originalNameFont;
        private Font originalPingFont;
        private Font originalPlayerCountFont;
        private Font originalMotdFont;

        private Image originalIconImage;
        private int originalIconSize = 64;

        private JLabel ping;
        private JLabel playerCount;
        private JLabel name;
        private JLabel motd = new JLabel();
        private ImageIcon icon;

        ServerOption(ServerInfo serverIn) {
            this.server = serverIn;

            setLayout(new BorderLayout(5, 0));
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

            int preferredHeight = 64;

            name = new JLabel(server.getName());
            ping = new JLabel("Pinging...");
            playerCount = new JLabel("Unknown");

            buildHeader();
            add(header, BorderLayout.NORTH);

            @Nullable byte[] favicon = server.getFavicon();
            if (favicon != null) {
                originalIconImage = new ImageIcon(favicon).getImage();
            } else {
                try {
                    BufferedImage unknownServerImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/unknown_server.png")));
                    originalIconImage = unknownServerImage;
                } catch (IOException | NullPointerException e) {
                    throw new RuntimeException(e);
                }
            }
            icon = new ImageIcon(originalIconImage);

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setSize(64, 64);
            add(iconLabel, BorderLayout.WEST);

            setDescription();
            add(motd, BorderLayout.CENTER);

            pingTask.submit(() -> {
                Pinger.ping(server)
                        .thenAccept(unused -> SwingUtilities.invokeLater(this::updateComponents))
                        .exceptionally(throwable -> {
                            log.error("Error during ping", throwable);
                            return null;
                        });
            });

            originalNameFont = name.getFont();
            originalPingFont = ping.getFont();
            originalPlayerCountFont = playerCount.getFont();
            originalMotdFont = motd.getFont();
        }

        private void buildHeader() {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 5, 2, 5);

            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            name.setHorizontalAlignment(SwingConstants.CENTER);
            header.add(name, c);

            c.gridx = 1;
            c.weightx = 0.0;
            c.fill = GridBagConstraints.NONE;
            header.add(playerCount, c);

            c.gridx = 2;
            header.add(ping, c);
        }

        private void setDescription() {
            // Because there are two different Component classes in this code I did the full thing instead of importing.
            net.kyori.adventure.text.Component descriptionComp = server.getDescription();
            String description = "";
            if (descriptionComp == null) {
                if (server.getStatus() == ServerInfo.Status.PINGING || server.getStatus() == ServerInfo.Status.INITIAL) {
                    description = "Pinging..."; // Grey
                } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
                    description = "Can't connect to server"; // Red
                }
            } else {
                description = miniMessage.serialize(server.getDescription());
            }
            motd.setText(description);
        }

        private void updateComponents() {
            ping.setText(server.getPing() + " ms");
            playerCount.setText(server.getPlayers() == null ? "unknown" : server.getPlayers().getOnlinePlayers() + "/" + server.getPlayers().getMaxPlayers());

            if (server.getFavicon() != null) {
                originalIconImage = new ImageIcon(server.getFavicon()).getImage();
                Image scaledImage = originalIconImage.getScaledInstance(
                        (int) (originalIconSize * currentScale),
                        (int) (originalIconSize * currentScale),
                        Image.SCALE_SMOOTH);
                icon.setImage(scaledImage);
            }

            setDescription();
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

        public void applyScale(double scale) {
            name.setFont(originalNameFont.deriveFont((float) (originalNameFont.getSize() * scale)));
            ping.setFont(originalPingFont.deriveFont((float) (originalPingFont.getSize() * scale)));
            playerCount.setFont(originalPlayerCountFont.deriveFont((float) (originalPlayerCountFont.getSize() * scale)));
            motd.setFont(originalMotdFont.deriveFont((float) (originalMotdFont.getSize() * scale)));

            // Scale icon
            int scaledSize = (int) (originalIconSize * scale);
            if (originalIconImage != null) {
                Image scaledImage = originalIconImage.getScaledInstance(
                        scaledSize, scaledSize, Image.SCALE_SMOOTH);

                // Update the icon with the scaled image
                icon.setImage(scaledImage);

                // Update component that contains the icon
                for (java.awt.Component comp : getComponents()) {
                    if (comp instanceof JLabel && ((JLabel) comp).getIcon() == icon) {
                        ((JLabel) comp).setIcon(icon);
                        comp.setPreferredSize(new Dimension(scaledSize, scaledSize));
                    }
                }
            }

            refreshGui(this);
        }

        @Override
        public Dimension getMaximumSize() {
            Dimension preferredSize = getPreferredSize();
            return new Dimension(
                    Integer.MAX_VALUE,  // Can stretch horizontally if needed
                    preferredSize.height  // But maintain the preferred height
            );
        }
    }
}
