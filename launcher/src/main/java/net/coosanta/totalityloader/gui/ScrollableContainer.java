package net.coosanta.totalityloader.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

public class ScrollableContainer extends JScrollPane {
    private FixedRatioContainer innerContainer;

    public ScrollableContainer(JPanel content) {
        innerContainer = new FixedRatioContainer(content);

        setViewportView(innerContainer);

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateContainerSize();
            }
        });
    }

    private void updateContainerSize() {
        // Get the viewport size
        Dimension viewportSize = getViewport().getSize();

        // Set the inner container's preferred size to match the viewport width
        innerContainer.setPreferredSize(new Dimension(viewportSize.width,
                innerContainer.getPreferredSize().height));

        refreshGui(innerContainer);
    }
}
