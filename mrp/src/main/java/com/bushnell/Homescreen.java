package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

public class Homescreen extends JPanel {

    private CardLayout cardLayout;     // Used to switch between panels
    private JPanel cardPanel;          // Main content area that swaps panels

    private static final Color BUTTON_GREEN = new Color(4, 172, 116); // Button color

    // Constructor: builds the main screen layout
    public Homescreen() {
        setPreferredSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());

        // --- Left Sidebar ---
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.BLACK);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(200, 720));

        // Logo
        JLabel logoLabel = createLogoLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createVerticalStrut(20));

        // App title
        JLabel titleLabel = new JLabel("MRP System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(20));

        // Buttons to switch panels
        String[] buttonNames = {"Update Stock", "Stock Report", "Bundle", "Demand Analysis"};

        // --- Panels to switch between ---
        StockReport stockReportPanel = new StockReport();
        UpdateStock updateStockPanel = new UpdateStock(stockReportPanel);
        Bundle bundlePanel = new Bundle();
        JPanel demandAnalysisPanel = createCardPanel("Demand Analysis");

        // CardLayout panel to hold all screens
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Add all content panels to the CardLayout
        cardPanel.add(updateStockPanel, "Update Stock");
        cardPanel.add(stockReportPanel, "Stock Report");
        cardPanel.add(bundlePanel, "Bundle");
        cardPanel.add(demandAnalysisPanel, "Demand Analysis");

        // Create and add buttons for navigation
        for (String name : buttonNames) {
            JButton button = createStyledButton(name);
            button.setMaximumSize(new Dimension(180, 40));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftPanel.add(Box.createVerticalStrut(10));
            leftPanel.add(button);

            // Button logic: show the corresponding panel
            button.addActionListener((ActionEvent e) -> {
                cardLayout.show(cardPanel, name);

                // Extra logic for refreshing specific panels
                if (name.equals("Stock Report")) {
                    stockReportPanel.refreshTable();  // Reload stock table
                } else if (name.equals("Bundle")) {
                    bundlePanel.refreshData();       // Reload bundle dropdown & stock
                }
            });
        }

        // --- Add panels to the screen ---
        add(leftPanel, BorderLayout.WEST);     // Sidebar
        add(cardPanel, BorderLayout.CENTER);   // Main content
    }

    // Loads and resizes the logo image
    private JLabel createLogoLabel() {
        URL imageUrl = getClass().getResource("/com/bushnell/VisualRoboticsLogo.png");
        if (imageUrl == null) {
            throw new RuntimeException("Logo image not found");
        }
        ImageIcon originalIcon = new ImageIcon(imageUrl);
        Image scaledImage = originalIcon.getImage().getScaledInstance(180, 51, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(scaledImage));
    }

    // Creates a styled green navigation button
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(BUTTON_GREEN);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setOpaque(true);
        button.setBorderPainted(false);
        return button;
    }

    // Creates a placeholder panel for future features like Demand Analysis
    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }

    // Entry point: creates and displays the application window
    public static void main(String[] args) {
        JFrame frame = new JFrame("MRP System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Homescreen());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
