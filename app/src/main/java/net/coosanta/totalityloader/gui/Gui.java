package net.coosanta.totalityloader.gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Gui extends JFrame {
    public Gui(Dimension size) throws IOException {
        setTitle("Minecraft - Select Server");
        setSize(size);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        setContentPane(new LoadingMappings(this));

        setVisible(true);
    }
}
