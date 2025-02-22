package net.coosanta.totalityloader.gui;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Gui extends JFrame {
    public Gui(Dimension size) throws IOException {
        setTitle("Minecraft - Select Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPanel = new Container(new ServerSelect());

        getContentPane().add(contentPanel);

        setSize(size);
        setMinimumSize(size);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
