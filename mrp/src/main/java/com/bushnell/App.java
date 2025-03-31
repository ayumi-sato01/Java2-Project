package com.bushnell;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Create and display the Homescreen
        JFrame frame = new JFrame("MRP System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Homescreen());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
