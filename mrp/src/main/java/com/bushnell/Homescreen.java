package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

public class Homescreen extends JPanel {  // Removed <UpdateStock> from here
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public Homescreen() {
        setPreferredSize(new Dimension(1280, 720));
        setBackground(Color.BLACK);
        setLayout(null); // Using absolute positioning

        // Load and add logo
        JLabel logoLabel = createLogoLabel();
        add(logoLabel);

        // Title label
        JLabel titleLabel = new JLabel("MRP System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(10, 70, 200, 30);
        add(titleLabel);

        // CardLayout Panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBounds(220, 10, 1050, 700);
        cardPanel.setBackground(Color.WHITE);
        add(cardPanel);

        UpdateStock updateStockPanel = new UpdateStock();
        StockReport stockReportPanel = new StockReport();

        cardPanel.add(updateStockPanel, "Update Stock");
        cardPanel.add(stockReportPanel, "Stock Report");

        String[] buttonNames = {"Update Stock", "Stock Report", "Bundle", "Demand Analysis"};
        int buttonY = 110;
        for (String name : buttonNames) {
            JButton button = createStyledButton(name);
            button.setBounds(10, buttonY, 180, 40);
            add(button);
            buttonY += 50;

            // Add ActionListener for buttons to switch to the correct card when clicked
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (name.equals("Stock Report")) {
                        stockReportPanel.refreshTable(); 
                    }
                    cardLayout.show(cardPanel, name);
                }
            });
            
        }

// Optional: placeholder for other buttons
        for (String name : buttonNames) {
            if (!name.equals("Update Stock") && !name.equals("Stock Report")) {
                JPanel panel = new JPanel();
                panel.setBackground(Color.WHITE);
                JLabel label = new JLabel(name);
                label.setFont(new Font("Arial", Font.BOLD, 24));
                panel.add(label);
                cardPanel.add(panel, name);
                }
            }

    }

    // Method to create the logo label
    private JLabel createLogoLabel() {
        URL imageUrl = getClass().getResource("/com/bushnell/VisualRoboticsLogo.png");
        if (imageUrl == null) {
            throw new RuntimeException("Image not found");
        }
        ImageIcon originalIcon = new ImageIcon(imageUrl);
        Image scaledImage = originalIcon.getImage().getScaledInstance(180, 51, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel logoLabel = new JLabel(scaledIcon);
        logoLabel.setBounds(10, 10, 180, 51);
        return logoLabel;
    }

    // Method to create a styled button
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(4, 172, 116));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true);
        button.setBorderPainted(false);
        return button;
    }

    public static void main(String[] args) {
        // Create JFrame and add Homescreen panel
        JFrame frame = new JFrame("MRP System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Homescreen());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
