package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

public class Homescreen extends JPanel {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private static final Color BUTTON_GREEN = new Color(4, 172, 116);

    public Homescreen() {
        setPreferredSize(new Dimension(1280, 720));
        setLayout(new BorderLayout());

        // Left Panel (Logo + Buttons)
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

        // Title
        JLabel titleLabel = new JLabel("MRP System");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(20));

        // Buttons
        String[] buttonNames = {"Update Stock", "Stock Report", "Bundle", "Demand Analysis"};
        for (String name : buttonNames) {
            JButton button = createStyledButton(name);
            button.setMaximumSize(new Dimension(180, 40));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            leftPanel.add(Box.createVerticalStrut(10));
            leftPanel.add(button);

            button.addActionListener((ActionEvent e) -> {
                cardLayout.show(cardPanel, name);
            });
        }

        add(leftPanel, BorderLayout.WEST);

        // Main Content Panel (Right Side)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel updateStockPanel = new UpdateStock();
        JPanel stockReportPanel = new StockReport();
        JPanel bundlePanel = createCardPanel("Bundle");
        JPanel demandAnalysisPanel = createCardPanel("Demand Analysis");

        cardPanel.add(updateStockPanel, "Update Stock");
        cardPanel.add(stockReportPanel, "Stock Report");
        cardPanel.add(bundlePanel, "Bundle");
        cardPanel.add(demandAnalysisPanel, "Demand Analysis");

        add(cardPanel, BorderLayout.CENTER);
    }

    private JLabel createLogoLabel() {
        URL imageUrl = getClass().getResource("/com/bushnell/VisualRoboticsLogo.png");
        if (imageUrl == null) {
            throw new RuntimeException("Logo image not found");
        }
        ImageIcon originalIcon = new ImageIcon(imageUrl);
        Image scaledImage = originalIcon.getImage().getScaledInstance(180, 51, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(scaledImage));
    }

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

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MRP System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new Homescreen());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
