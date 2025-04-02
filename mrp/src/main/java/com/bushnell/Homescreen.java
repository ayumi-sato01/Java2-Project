package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;

public class Homescreen extends JPanel {
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

        // Buttons
        String[] buttonNames = {"Update Stock", "Stock Report", "Bundle", "Demand Analysis"};
        int buttonY = 110;
        for (String name : buttonNames) {
            JButton button = createStyledButton(name);
            button.setBounds(10, buttonY, 180, 40);
            add(button);
            buttonY += 50;
        }

        // Dropdown menu
        String[] skuData = fetchSKUsFromDatabase();
        JComboBox<String> comboBox = new JComboBox<>(skuData);
        comboBox.setBounds(500, 150, 180, 30); // Place it right below the last button
        add(comboBox);

        // CardLayout Panel
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBounds(220, 10, 1050, 700);
        cardPanel.setBackground(Color.WHITE);
        add(cardPanel);

        // Add cards
        for (String name : buttonNames) {
            JPanel panel = new JPanel();
            panel.setBackground(Color.WHITE);
            JLabel label = new JLabel(name);
            label.setFont(new Font("Arial", Font.BOLD, 24));
            panel.add(label);
            cardPanel.add(panel, name);
        }

        // Button handlers
        for (Component comp : getComponents()) {
            if (comp instanceof JButton) {
                ((JButton) comp).addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cardLayout.show(cardPanel, ((JButton) e.getSource()).getText());
                    }
                });
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

    // Method to fetch SKU data from the database
    private String[] fetchSKUsFromDatabase() {
        ArrayList<String> skuList = new ArrayList<>();
        String url = "jdbc:sqlite:your_database.db"; // Change to your actual database file

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT sku FROM stock")) { // Adjust table/column names

            while (rs.next()) {
                skuList.add(rs.getString("sku")); // Get SKU values from the database
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Print the exception if there's an issue with the DB connection
        }

        return skuList.toArray(new String[0]); // Convert ArrayList to String array for JComboBox
    }

    public static void main(String[] args) {
        // Load SQLite driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Create JFrame and add Homescreen panel
        JFrame frame = new JFrame("MRP System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Homescreen());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
