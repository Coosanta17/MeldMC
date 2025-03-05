package net.coosanta.totalityloader.gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SelectServerScreen extends JPanel implements ScalablePanel {
    private final int designWidth = 600;
    private final int designHeight = 400;

    private JLabel title;
    private Font originalTitleFont;

    public SelectServerScreen() throws IOException {
        setLayout(new BorderLayout());

        title = new JLabel("Select Server");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        originalTitleFont = new Font(title.getFont().getName(), title.getFont().getStyle(), 24);
        title.setFont(originalTitleFont);
        add(title, BorderLayout.NORTH);

        final JComponent serverOptions = new ScrollableContainer(new ServerOptions());
        add(serverOptions, BorderLayout.CENTER);

//        final JPanel footer = new JPanel();
//        add(footer, BorderLayout.SOUTH);
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
        title.setFont(originalTitleFont.deriveFont((float)(originalTitleFont.getSize() * scaleFactor)));
    }
}
