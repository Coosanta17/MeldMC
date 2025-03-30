package net.coosanta.totalityloader.gui.serverselection;

import net.coosanta.totalityloader.gui.containers.ScalablePanel;
import net.coosanta.totalityloader.gui.lookandfeel.MinecraftButton;
import net.coosanta.totalityloader.gui.lookandfeel.TransparentPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class OptionsPanel extends TransparentPanel implements ScalablePanel {
    private Logger log = LoggerFactory.getLogger(OptionsPanel.class);
    private double scaleFactor = 1.0;

    private final MinecraftButton join;
    private final MinecraftButton info;
    private final MinecraftButton add;
    private final MinecraftButton edit;
    private final MinecraftButton delete;
    private final MinecraftButton refresh;
    private final MinecraftButton settings;

    public OptionsPanel() {
        setLayout(new GridBagLayout());

        Dimension topLayerRatio = new Dimension(20, 3);
        Dimension bottomLayerRatio = new Dimension(5, 1);

        join = new MinecraftButton("Join", false, topLayerRatio);
        info = new MinecraftButton("Info", true, topLayerRatio);
        add = new MinecraftButton("Add", true, topLayerRatio);

        edit = new MinecraftButton("Edit", false, bottomLayerRatio);
        delete = new MinecraftButton("Delete", false, bottomLayerRatio);
        refresh = new MinecraftButton("Refresh", true, bottomLayerRatio);
        settings = new MinecraftButton("Settings", true, bottomLayerRatio);

        buildLayout();
    }

    private void buildLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Top row
        gbc.gridy = 0;
        gbc.weightx = 4.0;

        gbc.gridx = 0;
        add(join, gbc);

        gbc.gridx = 1;
        add(info, gbc);

        gbc.gridx = 2;
        add(add, gbc);

        // Bottom row
        gbc.gridy = 1;
        gbc.weightx = 3.0;

        gbc.gridx = 0;
        add(edit, gbc);

        gbc.gridx = 1;
        add(delete, gbc);

        gbc.gridx = 2;
        add(refresh, gbc);

        gbc.gridx = 3;
        add(settings, gbc);
    }

    @Override
    public void applyScale(double scaleFactor) {
        this.scaleFactor = scaleFactor;

        join.applyScale(scaleFactor);
        info.applyScale(scaleFactor);
        add.applyScale(scaleFactor);
        edit.applyScale(scaleFactor);
        delete.applyScale(scaleFactor);
        refresh.applyScale(scaleFactor);
        settings.applyScale(scaleFactor);

        // FIXME: scale cannot become larger than 1.0, due to preferredSize being used in calculations.
        setPreferredSize(new Dimension(
                super.getPreferredSize().width,
                (int) Math.round(getDesignHeight())));

        System.out.println("Rescaled to factor: " + scaleFactor);
    }

    @Override
    public double getDesignWidth() {
        return 800;
    }

    @Override
    public double getDesignHeight() {
        return 80;
    }
}
