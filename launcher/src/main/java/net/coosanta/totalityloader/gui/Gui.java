package net.coosanta.totalityloader.gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Gui extends JFrame {
    public Gui(Dimension size) throws IOException {
        setTitle("Minecraft - Select Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent contentPanel = new ScrollableContainer(new ServerSelect());

        setContentPane(contentPanel);

        setSize(size);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void refreshGui(JComponent component) {
        SwingUtilities.invokeLater(() -> {
            component.revalidate();
            component.repaint();
        });
    }
}
