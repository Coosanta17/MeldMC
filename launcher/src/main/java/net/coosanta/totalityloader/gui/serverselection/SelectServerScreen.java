package net.coosanta.totalityloader.gui.serverselection;

import net.coosanta.totalityloader.gui.containers.FixedRatioContainer;
import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import net.coosanta.totalityloader.gui.containers.ScrollableFixedRatioContainer;
import net.coosanta.totalityloader.gui.lookandfeel.TransparentPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class SelectServerScreen extends TransparentPanel implements ScalablePanel {
    private final JLabel title;
    private final Font originalTitleFont;
    private final JComponent mainContent;
    private final JComponent footer;

    public SelectServerScreen() throws IOException {
        setLayout(new BorderLayout(0, 5));

        this.title = new JLabel("Select Server");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        originalTitleFont = new Font(title.getFont().getName(), title.getFont().getStyle(), 24);
        title.setFont(originalTitleFont);
        add(title, BorderLayout.NORTH);

        this.mainContent = new ScrollableFixedRatioContainer(new ServerOptions(), 4, 3);
        add(mainContent, BorderLayout.CENTER);

        this.footer = new FixedRatioContainer(new OptionsPanel(), 10, 1);
        add(footer, BorderLayout.SOUTH);
    }

    public double getMainContentScaleFactor() {
        return ((ScrollableFixedRatioContainer) mainContent).getScaleFactor();
    }

    @Override
    public void applyScale(double scaleFactor) {
        title.setFont(originalTitleFont.deriveFont((float) (originalTitleFont.getSize() * scaleFactor)));
    }
}
