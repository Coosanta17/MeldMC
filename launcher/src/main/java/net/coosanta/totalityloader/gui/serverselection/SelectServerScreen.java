package net.coosanta.totalityloader.gui.serverselection;

import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import net.coosanta.totalityloader.gui.containers.ScrollableFixedRatioContainer;
import net.coosanta.totalityloader.gui.lookandfeel.TransparentPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SelectServerScreen extends TransparentPanel implements ScalablePanel {

    private JLabel title;
    private Font originalTitleFont;

    public SelectServerScreen() throws IOException {
        setLayout(new BorderLayout());

        title = new JLabel("Select Server");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        originalTitleFont = new Font(title.getFont().getName(), title.getFont().getStyle(), 24);
        title.setFont(originalTitleFont);
        add(title, BorderLayout.NORTH);

        final JComponent serverOptions = new ScrollableFixedRatioContainer(new ServerOptions());
        add(serverOptions, BorderLayout.CENTER);

//        final JPanel footer = new JPanel();
//        add(footer, BorderLayout.SOUTH);
    }

    @Override
    public void applyScale(double scaleFactor) {
        title.setFont(originalTitleFont.deriveFont((float)(originalTitleFont.getSize() * scaleFactor)));
    }
}
