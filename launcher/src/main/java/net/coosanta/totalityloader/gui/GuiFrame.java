package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;
import net.coosanta.totalityloader.gui.containers.ScalableContainer;
import net.coosanta.totalityloader.gui.lookandfeel.MinecraftPanel;
import net.coosanta.totalityloader.gui.serverselection.SelectServerScreen;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class GuiFrame extends JFrame {
    public static final Dimension DEFAULT_SIZE = new Dimension(800, 600);
    private static GuiFrame instance;

    private final JPanel contentPanel;

    public GuiFrame(Dimension size) throws IOException {
        instance = this;

        setTitle("Minecraft - Select Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        applyMojanglesFont();

        this.contentPanel = new SelectServerScreen();
        JPanel basePanel = new ScalableContainer(new MinecraftPanel(contentPanel));

        setContentPane(basePanel);

        setSize(size);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void applyMojanglesFont() {
        try (InputStream is = Main.class.getResourceAsStream("/fonts/mojangles.ttf")) {
            if (is == null) {
                throw new FileNotFoundException("Font file not found in resources.");
            }

            Font mojangles = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12f);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(mojangles);

            Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) {
                    UIManager.put(key, mojangles);
                }
                if (key.toString().endsWith(".foreground")) {
                    UIManager.put(key, Color.WHITE);
                }
            }
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void refreshGui(JComponent component) {
        SwingUtilities.invokeLater(() -> {
            component.revalidate();
            component.repaint();
        });
    }

    public static GuiFrame getInstance() {
        return instance;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }
}
