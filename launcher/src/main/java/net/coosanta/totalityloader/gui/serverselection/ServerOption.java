package net.coosanta.totalityloader.gui.serverselection;

import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import net.coosanta.totalityloader.gui.lookandfeel.TransparentPanel;
import net.coosanta.totalityloader.minecraft.ServerInfo;
import net.coosanta.totalityloader.network.Pinger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class ServerOption extends TransparentPanel implements ScalablePanel {
    private Logger log = LoggerFactory.getLogger(ServerOption.class);
    private final double SCALE_MODIFIER = 2.0; // That's right! I'm too lazy to figure out why the font size isn't changing!
    private double currentScale = 1.0;
    private final int originalTopBottomPadding = 25;

    private final ServerInfo server;
    private final JPanel header = new TransparentPanel(new GridBagLayout());
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // I know they're the same, but just in case they need changing individually.
    private final Font originalNameFont;
    private final Font originalPingFont;
    private final Font originalPlayerCountFont;
    private final Font originalMotdFont;

    private Image originalIconImage;
    private final int originalIconSize = 64;

    private JLabel ping;
    private JLabel playerCount;
    private JLabel name;
    private JLabel motd = new JLabel();
    private ImageIcon icon;

    ServerOption(ServerInfo serverIn, ExecutorService pingTask) {
        this.server = serverIn;

        setLayout(new BorderLayout(5, 5));

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
                originalIconImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/icons/unknown_server.png")));
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

        originalNameFont = name.getFont();
        originalPingFont = ping.getFont();
        originalPlayerCountFont = playerCount.getFont();
        originalMotdFont = motd.getFont();

        pingTask.submit(() -> {
            Pinger.ping(server)
                    .thenAccept(unused -> SwingUtilities.invokeLater(this::updateComponents))
                    .exceptionally(throwable -> {
                        log.error("Error during ping", throwable);
                        return null;
                    });
        });

        applyScale(currentScale);
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
        playerCount.setText(server.getPlayers() == null ? "unknown" : server.getPlayers().getOnlinePlayers() + "/" + server.getPlayers().getMaxPlayers());

        if (server.getStatus() == ServerInfo.Status.SUCCESSFUL) {
            ping.setText(server.getPing() + " ms");
        } else if (server.getStatus() == ServerInfo.Status.UNREACHABLE) {
            ping.setText("Can't connect");
        } else {
            ping.setText("Pinging...");
        }

        if (server.getFavicon() != null) {
            originalIconImage = new ImageIcon(server.getFavicon()).getImage();
            Image scaledImage = originalIconImage.getScaledInstance(
                    (int) (originalIconSize * currentScale * SCALE_MODIFIER),
                    (int) (originalIconSize * currentScale * SCALE_MODIFIER),
                    Image.SCALE_SMOOTH);
            icon.setImage(scaledImage);
        }

        Container parent = getParent();
        while (parent != null && !(parent instanceof ServerOptions)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            ((ServerOptions) parent).updateLayout();
        } else {
            invalidate();
            revalidate();
            repaint();
        }

        setDescription();
        revalidate();
    }

    @Override
    public void applyScale(double scale) {
        this.currentScale = scale;

        name.setFont(originalNameFont.deriveFont((float) (originalNameFont.getSize() * scale * SCALE_MODIFIER)));
        ping.setFont(originalPingFont.deriveFont((float) (originalPingFont.getSize() * scale * SCALE_MODIFIER)));
        playerCount.setFont(originalPlayerCountFont.deriveFont((float) (originalPlayerCountFont.getSize() * scale * SCALE_MODIFIER)));
        motd.setFont(originalMotdFont.deriveFont((float) (originalMotdFont.getSize() * scale * SCALE_MODIFIER)));

        int scaledTopBottom = (int) (originalTopBottomPadding * scale * SCALE_MODIFIER);
        setBorder(BorderFactory.createEmptyBorder(scaledTopBottom, 0, scaledTopBottom, 0));

        // Scale icon
        int scaledSize = (int) (originalIconSize * scale * SCALE_MODIFIER);
        if (originalIconImage != null && scaledSize != 0) {
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
        Dimension size = getPreferredSize();
        if (getParent() != null && getParent().getWidth() > 0) {
            return new Dimension(getParent().getWidth() - 20, size.height);
        }
        return size;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();

        if (getParent() != null && getParent().getWidth() > 0) {
            size.width = Math.min(size.width, getParent().getWidth());
        }

        return size;
    }
}