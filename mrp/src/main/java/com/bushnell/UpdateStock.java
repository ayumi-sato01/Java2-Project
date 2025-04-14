package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class UpdateStock extends JPanel {

    private JComboBox<String> comboBox;  // Made global to access throughout the class
    private JTextField descriptionField;
    private JTextField priceField;
    private JTextField stockField;

    public UpdateStock() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        // Title label
        JLabel label = new JLabel("Update Stock", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(CENTER_ALIGNMENT);
        this.add(label);

        // SKU ComboBox and label
        JPanel skuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel skuLabel = new JLabel("SKU: ");
        skuPanel.add(skuLabel);

        String[] skuData = fetchSKUsFromDatabase();
        comboBox = new JComboBox<>(skuData);
        skuPanel.add(comboBox);
        skuPanel.setAlignmentX(CENTER_ALIGNMENT);
        this.add(skuPanel);

        // Description Field
        descriptionField = new JTextField(20);
        descriptionField.setEditable(false);
        descriptionField.setText("Description will appear here");
        descriptionField.setHorizontalAlignment(JTextField.CENTER);
        descriptionField.setAlignmentX(CENTER_ALIGNMENT);
        this.add(descriptionField);

        // Price and Stock Fields
        priceField = new JTextField(10);
        stockField = new JTextField(10);

        JPanel priceStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        priceStockPanel.add(new JLabel("Price:"));
        priceStockPanel.add(priceField);
        priceStockPanel.add(new JLabel("Stock:"));
        priceStockPanel.add(stockField);
        priceStockPanel.setAlignmentX(CENTER_ALIGNMENT);
        this.add(priceStockPanel);

        // ComboBox listener for live updates
        comboBox.addActionListener(e -> {
            String selectedSku = (String) comboBox.getSelectedItem();
            String description = fetchDescriptionForSku(selectedSku);
            descriptionField.setText(description);

            String[] priceAndStock = fetchPriceAndStockForSku(selectedSku);
            priceField.setText(priceAndStock[0]);
            stockField.setText(priceAndStock[1]);
        });

        // Submit button
        JButton submitButton = new JButton("Update Stock");
        submitButton.setAlignmentX(CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> {
            String selectedSku = (String) comboBox.getSelectedItem();
            String newPrice = priceField.getText();
            String newStock = stockField.getText();
            updateStockInDatabase(selectedSku, newPrice, newStock);
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);
        this.add(buttonPanel);

        // Optional: Trigger initial selection update
        if (skuData.length > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    // Fetch all SKUs from the database
    private String[] fetchSKUsFromDatabase() {
        ArrayList<String> skuList = new ArrayList<>();
        String url = Database.DBName;

        try (Connection conn = DriverManager.getConnection(url);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT sku FROM part")) {
            while (rs.next()) {
                skuList.add(rs.getString("sku"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skuList.toArray(new String[0]);
    }

    // Fetch description for a given SKU
    private String fetchDescriptionForSku(String sku) {
        String description = "";
        String url = Database.DBName;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement statement = conn.prepareStatement("SELECT description FROM part WHERE sku = ?")) {
            statement.setString(1, sku);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    description = rs.getString("description");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return description;
    }

    // Fetch price and stock for a given SKU
    private String[] fetchPriceAndStockForSku(String sku) {
        String[] data = new String[2]; // [0] = price, [1] = stock
        String url = Database.DBName;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement statement = conn.prepareStatement("SELECT price, stock FROM part WHERE sku = ?")) {
            statement.setString(1, sku);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    data[0] = rs.getString("price");
                    data[1] = rs.getString("stock");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Update price and stock in the database
    private void updateStockInDatabase(String sku, String price, String stock) {
        try (Connection conn = DriverManager.getConnection(Database.DBName);
             PreparedStatement statement = conn.prepareStatement(
                     "UPDATE part SET price = ?, stock = ? WHERE sku = ?")) {
            statement.setString(1, price);
            statement.setString(2, stock);
            statement.setString(3, sku);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Stock updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "No stock updated. Please check SKU.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating stock.");
        }
    }
}
