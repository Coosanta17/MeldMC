package net.coosanta.totalityloader.gui.lookandfeel;

import javax.swing.*;
import java.awt.*;

public class TransparentPanel extends JPanel {
    public TransparentPanel() {
        super();
        setOpaque(false);
    }

    public TransparentPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }
}
