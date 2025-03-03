package net.coosanta.totalityloader.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Container extends JPanel {

    public Container(JPanel innerPanel) {
        JPanel container = this;
        setLayout(new GridBagLayout());
        add(innerPanel);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizePreview(innerPanel, container);
                SwingUtilities.invokeLater(() -> {
                    container.revalidate();
                    container.repaint();
                });
            }
        });
    }

    private static void resizePreview(JPanel innerPanel, JPanel container) {
        int w = container.getWidth();
        int h = container.getHeight();
        int size = Math.min(w, h);
        innerPanel.setPreferredSize(new Dimension(size, size));
        container.revalidate();
    }
}
