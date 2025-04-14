package com.bushnell;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MRP System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            Homescreen homeScreenPanel = new Homescreen();
            frame.setContentPane(homeScreenPanel);

            frame.setVisible(true);
        });
    }
}
