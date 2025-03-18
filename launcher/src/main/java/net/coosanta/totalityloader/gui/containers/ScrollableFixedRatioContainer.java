package net.coosanta.totalityloader.gui.containers;

    import net.coosanta.totalityloader.gui.lookandfeel.TransparentPanel;

    import javax.swing.*;
    import java.awt.*;

    import static net.coosanta.totalityloader.gui.GuiFrame.refreshGui;

    public class ScrollableFixedRatioContainer extends FixedRatioContainer {
        private JScrollPane scrollPane;
        private JPanel contentPanel;

        public ScrollableFixedRatioContainer(JPanel content) {
            super(new TransparentPanel(new BorderLayout()));
            this.contentPanel = content;

            scrollPane = new JScrollPane(content);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(null); // Remove border for cleaner look

            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);

            innerPanel.add(scrollPane, BorderLayout.CENTER);
        }

        @Override
        protected void scale(JPanel innerPanel, JPanel container) {
            super.scale(innerPanel, container);

            updateScrollPaneSize();
        }

        private void updateScrollPaneSize() {
            int size = Math.min(getWidth(), getHeight());
            scrollPane.setPreferredSize(new Dimension(size, size));

            if (contentPanel instanceof ScalablePanel) {
                ((ScalablePanel) contentPanel).applyScale(getScaleFactor());
            }

            refreshGui(this);
        }
    }