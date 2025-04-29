package com.bushnell;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DemandAnalysis extends JPanel {

    private JComboBox<String> componentDropdown;
    private JLabel descriptionLabel;
    private JLabel quantityLabel;
    private JTextField requestField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel totalCostLabel;

    public DemandAnalysis() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(1000, 690));

        JLabel titleLabel = new JLabel("Demand Analysis");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(20));
        add(titleLabel);
        add(Box.createVerticalStrut(20));

                JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Box infoBox = Box.createVerticalBox();

        // First row: dropdown
        JPanel dropdownRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        componentDropdown = new JComboBox<>();
        dropdownRow.add(new JLabel("Select Part:"));
        dropdownRow.add(componentDropdown);
        dropdownRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Second row: description and stock stacked
        descriptionLabel = new JLabel("Description: ");
        quantityLabel = new JLabel("In Stock: ");
        Box descBox = Box.createVerticalBox();
        descBox.add(descriptionLabel);
        descBox.add(quantityLabel);
        descBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add both to info box
        infoBox.add(dropdownRow);
        infoBox.add(descBox);

        // Add the box to input panel
        inputPanel.add(infoBox);
        add(inputPanel);


        JPanel requestPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        requestField = new JTextField(5);
        JButton submitButton = new JButton("Submit Request");
        requestPanel.add(new JLabel("Request Quantity:"));
        requestPanel.add(requestField);
        requestPanel.add(submitButton);
        submitButton.addActionListener(e -> calculateDemand());
        submitButton.setEnabled(false); 
        descriptionLabel.setText("Description: ");
        quantityLabel.setText("In Stock: ");
        componentDropdown.setSelectedIndex(-1); 

        add(requestPanel);

        tableModel = new DefaultTableModel(new Object[]{"SKU", "Description", "Need", "Stock", "Price", "Subtotal"}, 0);
        resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);
        add(scrollPane);

        totalCostLabel = new JLabel("Total Cost: $0.00");
        add(totalCostLabel);

        loadDropdownData();

        componentDropdown.addActionListener(e -> {
            if (componentDropdown.getSelectedIndex() != -1) {
                updateDescription();
                submitButton.setEnabled(true);
            } else {
                descriptionLabel.setText("Description: ");
                quantityLabel.setText("In Stock: ");
                submitButton.setEnabled(false);
            }
        });

        if (componentDropdown.getItemCount() > 0) {
            componentDropdown.setSelectedIndex(-1); 
        }

    }

    private void loadDropdownData() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:VR-Factory.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT parent_SKU FROM bom")) {
            while (rs.next()) {
                componentDropdown.addItem(rs.getString("parent_SKU"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading SKUs: " + e.getMessage());
        }
    }

    private void updateDescription() {
        String selectedSKU = (String) componentDropdown.getSelectedItem();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:VR-Factory.db")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT description, stock FROM part WHERE SKU = ?");
            stmt.setString(1, selectedSKU);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                descriptionLabel.setText("Description: " + rs.getString("description"));
                quantityLabel.setText("In Stock: " + rs.getInt("stock"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error retrieving part info: " + ex.getMessage());
        }
    }

    public void refreshData() {
        componentDropdown.removeAllItems();
        loadDropdownData();
        componentDropdown.setSelectedIndex(-1); 
        descriptionLabel.setText("Description: ");
        quantityLabel.setText("In Stock: ");
        requestField.setText("");
        tableModel.setRowCount(0);
        totalCostLabel.setText("Total Cost: $0.00");
    }
    

    private void calculateDemand() {
        String parentSKU = (String) componentDropdown.getSelectedItem();
        int requestedQty;
    
        try {
            requestedQty = Integer.parseInt(requestField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
            return;
        }
    
        tableModel.setRowCount(0);
        double totalCost = 0.0;
        boolean hasShortage = false;
    
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:VR-Factory.db")) {
            // FIRST: Check parent stock
            int parentStock = 0;
            PreparedStatement parentStmt = conn.prepareStatement("SELECT stock FROM part WHERE SKU = ?");
            parentStmt.setString(1, parentSKU);
            ResultSet parentRs = parentStmt.executeQuery();
            if (parentRs.next()) {
                parentStock = parentRs.getInt("stock");
            }
    
            if (parentStock >= requestedQty) {
                JOptionPane.showMessageDialog(this, "Enough parent stock available. You can fulfill the request with existing parent SKUs!");
                totalCostLabel.setText("Total Cost: $0.00");
                return; 
            }
    
            int remainingQty = requestedQty - parentStock;
    
            
            PreparedStatement bomStmt = conn.prepareStatement("SELECT SKU, quantity FROM bom WHERE parent_SKU = ?");
            bomStmt.setString(1, parentSKU);
            ResultSet bomRs = bomStmt.executeQuery();
    
            while (bomRs.next()) {
                String partSKU = bomRs.getString("SKU");
                int requiredPerUnit = bomRs.getInt("quantity");
                int totalNeeded = remainingQty * requiredPerUnit;
    
                PreparedStatement partStmt = conn.prepareStatement("SELECT description, stock, price FROM part WHERE SKU = ?");
                partStmt.setString(1, partSKU);
                ResultSet partRs = partStmt.executeQuery();
    
                if (partRs.next()) {
                    String description = partRs.getString("description");
                    int stock = partRs.getInt("stock");
                    double price = partRs.getDouble("price");
    
                    if (stock < totalNeeded) {
                        hasShortage = true;
                        int shortage = totalNeeded - stock;
                        double subtotal = shortage * price;
                        totalCost += subtotal;
                        tableModel.addRow(new Object[]{partSKU, description, shortage, stock, String.format("$%.2f", price), String.format("$%.2f", subtotal)});
                    }
                }
            }
    
            if (!hasShortage) {
                // Calculate how many full bundles could be made from available stock
                bomRs = bomStmt.executeQuery();
                int maxBundles = Integer.MAX_VALUE;
                while (bomRs.next()) {
                    String partSKU = bomRs.getString("SKU");
                    int requiredPerUnit = bomRs.getInt("quantity");
                    PreparedStatement partStmt = conn.prepareStatement("SELECT stock FROM part WHERE SKU = ?");
                    partStmt.setString(1, partSKU);
                    ResultSet partRs = partStmt.executeQuery();
                    if (partRs.next()) {
                        int stock = partRs.getInt("stock");
                        int possible = stock / requiredPerUnit;
                        maxBundles = Math.min(maxBundles, possible);
                    }
                }
                JOptionPane.showMessageDialog(this, "Enough parts available. You can make " + maxBundles + " additional unit(s)!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error during analysis: " + ex.getMessage());
        }
    
        totalCostLabel.setText("Total Cost: $" + String.format("%.2f", totalCost));
    }    
}
