package net.coosanta.totalityloader.gui;

import net.coosanta.totalityloader.Main;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class GuiFrame extends JFrame {
    public GuiFrame(Dimension size) throws IOException {
        setTitle("Minecraft - Select Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        applyMojanglesFont();

        JPanel contentPanel = new Container(new SelectServerScreen());
        setContentPane(contentPanel);

        setSize(size);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void applyMojanglesFont() {
        try (InputStream is = Main.class.getResourceAsStream("/mojangles.ttf")) {
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
                if (value instanceof Font)
                    UIManager.put(key, mojangles);
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
}
