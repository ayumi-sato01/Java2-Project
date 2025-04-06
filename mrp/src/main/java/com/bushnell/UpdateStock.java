package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class UpdateStock extends JPanel {

    public UpdateStock() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        // Label for the title - Centered at the top
        JLabel label = new JLabel("Update Stock", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(CENTER_ALIGNMENT);  // Center the label
        this.add(label);  // Add title at the top

        // SKU ComboBox and Description field panel
        JPanel skuPanel = new JPanel();
        skuPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel skuLabel = new JLabel("SKU: ");
        skuPanel.add(skuLabel);

        // Fetch SKUs from database and populate ComboBox
        String[] skuData = fetchSKUsFromDatabase();
        JComboBox<String> comboBox = new JComboBox<>(skuData);
        skuPanel.add(comboBox);
        skuPanel.setAlignmentX(CENTER_ALIGNMENT);  // Center the panel

        this.add(skuPanel);  // Add SKU panel below title

        // Description TextField (Read-only)
        JTextField descriptionField = new JTextField(20);
        descriptionField.setEditable(false);
        descriptionField.setText("Description will appear here");
        descriptionField.setHorizontalAlignment(JTextField.CENTER);
        descriptionField.setAlignmentX(CENTER_ALIGNMENT);  // Center the description field
        this.add(descriptionField);  // Add description field below SKU selection

        // Price and Stock Fields (Editable)
        JTextField priceField = new JTextField(10);
        JTextField stockField = new JTextField(10);

        // Panel for Price and Stock labels and fields
        JPanel priceStockPanel = new JPanel();
        priceStockPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        priceStockPanel.add(new JLabel("Price:"));
        priceStockPanel.add(priceField);
        priceStockPanel.add(new JLabel("Stock:"));
        priceStockPanel.add(stockField);
        priceStockPanel.setAlignmentX(CENTER_ALIGNMENT);  // Center price and stock panel
        this.add(priceStockPanel);  // Add price and stock fields to the bottom

        // ComboBox ActionListener to Fetch Description when SKU is selected
        comboBox.addActionListener(e -> {
            String selectedSku = (String) comboBox.getSelectedItem();
            String description = fetchDescriptionForSku(selectedSku);
            descriptionField.setText(description);
        });

        // Submit Button to Update Stock and Price
        JButton submitButton = new JButton("Update Stock");
        submitButton.setAlignmentX(CENTER_ALIGNMENT);  // Center the button
        submitButton.addActionListener(e -> {
            String selectedSku = (String) comboBox.getSelectedItem();
            String newPrice = priceField.getText();
            String newStock = stockField.getText();
            updateStockInDatabase(selectedSku, newPrice, newStock);
        });

        // Panel for Submit Button
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.setAlignmentX(CENTER_ALIGNMENT);  // Center the button panel
        this.add(buttonPanel);  // Add submit button below fields
    }

    // Fetch all SKUs from the database
    private String[] fetchSKUsFromDatabase() {
        ArrayList<String> skuList = new ArrayList<>();
        String url = "jdbc:sqlite:/Users/ayumisato/Java2-Project/VR-Factory.db";

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

    // Fetch the description for the selected SKU
    private String fetchDescriptionForSku(String sku) {
        String description = "";
        String url = "jdbc:sqlite:/Users/ayumisato/Java2-Project/VR-Factory.db";

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

    // Update the stock and price of the selected SKU in the database
    private void updateStockInDatabase(String sku, String price, String stock) {
        String url = "jdbc:sqlite:/Users/ayumisato/Java2-Project/VR-Factory.db";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement statement = conn.prepareStatement("UPDATE part SET price = ?, stock = ? WHERE sku = ?")) {
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
