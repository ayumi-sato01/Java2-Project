package com.bushnell;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StockReport extends JPanel {

    private JTable stockTable;
    private DefaultTableModel tableModel;

    public StockReport() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Title Label
        JLabel titleLabel = new JLabel("Stock Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"SKU", "Description", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0);
        stockTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        add(scrollPane, BorderLayout.CENTER);

        // Load data when the panel is created
        refreshTable();
    }

    // Method to refresh stock data from the database
    public void refreshTable() {
        tableModel.setRowCount(0); // Clear old data

        String url = "jdbc:sqlite:/Users/ayumisato/Java2-Project/VR-Factory.db";

        try (Connection conn = DriverManager.getConnection(url);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT sku, description, price, stock FROM part")) {

            while (rs.next()) {
                String sku = rs.getString("sku");
                String description = rs.getString("description");
                String price = rs.getString("price");
                String stock = rs.getString("stock");
                tableModel.addRow(new Object[]{sku, description, price, stock});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading stock data.");
        }
    }
}
