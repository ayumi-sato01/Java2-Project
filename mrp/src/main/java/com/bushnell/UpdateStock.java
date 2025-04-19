package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class UpdateStock extends JPanel {

    private JComboBox<String> comboBox;       // Dropdown for SKU selection
    private JTextField descriptionField;      // Displays item description
    private JTextField priceField;            // Editable price input
    private JTextField stockField;            // Editable stock input
    private StockReport stockReport;          // Reference to update stock table

    // Constructor: builds the "Update Stock" interface
    public UpdateStock(StockReport stockReport) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        this.stockReport = stockReport;

        // Title
        JLabel label = new JLabel("Update Stock", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(CENTER_ALIGNMENT);
        this.add(label);

        // SKU Dropdown with label
        JPanel skuPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        skuPanel.add(new JLabel("SKU: "));
        String[] skuData = fetchSKUsFromDatabase();
        comboBox = new JComboBox<>(skuData);
        skuPanel.add(comboBox);
        skuPanel.setAlignmentX(CENTER_ALIGNMENT);
        this.add(skuPanel);

        // Description field
        descriptionField = new JTextField(20);
        descriptionField.setEditable(false);
        descriptionField.setText("Description will appear here");
        descriptionField.setHorizontalAlignment(JTextField.CENTER);
        descriptionField.setAlignmentX(CENTER_ALIGNMENT);
        this.add(descriptionField);

        // Price and Stock input fields
        priceField = new JTextField(10);
        stockField = new JTextField(10);
        JPanel priceStockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        priceStockPanel.add(new JLabel("Price:"));
        priceStockPanel.add(priceField);
        priceStockPanel.add(new JLabel("Stock:"));
        priceStockPanel.add(stockField);
        priceStockPanel.setAlignmentX(CENTER_ALIGNMENT);
        this.add(priceStockPanel);

        // Dropdown listener: auto-fill description, price, and stock
        comboBox.addActionListener(e -> {
            String selectedSku = (String) comboBox.getSelectedItem();
            descriptionField.setText(fetchDescriptionForSku(selectedSku));
            String[] priceAndStock = fetchPriceAndStockForSku(selectedSku);
            priceField.setText(priceAndStock[0]);
            stockField.setText(priceAndStock[1]);
        });

        // Submit button to update database
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

        // Trigger the dropdown listener on load
        if (skuData.length > 0) {
            comboBox.setSelectedIndex(0);
        }
    }

    // Fetch all SKUs from the database to populate the dropdown
    private String[] fetchSKUsFromDatabase() {
        ArrayList<String> skuList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(Database.DBName);
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

    // Fetch the description for the selected SKU
    private String fetchDescriptionForSku(String sku) {
        String description = "";
        try (Connection conn = DriverManager.getConnection(Database.DBName);
             PreparedStatement stmt = conn.prepareStatement("SELECT description FROM part WHERE sku = ?")) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    description = rs.getString("description");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return description;
    }

    // Fetch price and stock for the selected SKU
    private String[] fetchPriceAndStockForSku(String sku) {
        String[] data = new String[2]; // [0] = price, [1] = stock
        try (Connection conn = DriverManager.getConnection(Database.DBName);
             PreparedStatement stmt = conn.prepareStatement("SELECT price, stock FROM part WHERE sku = ?")) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
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

    // Update the price and stock of the selected SKU in the database
    private void updateStockInDatabase(String sku, String price, String stock) {
        try (Connection conn = DriverManager.getConnection(Database.DBName);
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE part SET price = ?, stock = ? WHERE sku = ?")) {

            stmt.setString(1, price);
            stmt.setString(2, stock);
            stmt.setString(3, sku);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Stock updated successfully!");
                stockReport.refreshTable(); // Refresh StockReport panel
            } else {
                JOptionPane.showMessageDialog(this, "No stock updated. Please check SKU.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating stock.");
        }
    }
}
