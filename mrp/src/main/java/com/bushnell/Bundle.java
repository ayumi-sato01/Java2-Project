package com.bushnell;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Bundle extends JPanel {

    private JComboBox<String> parentDropdown;  // Dropdown for selecting parent SKU
    private JTable childTable;                 // Table showing child part info
    private DefaultTableModel tableModel;      // Model for the table
    private JButton bundleButton;              // Button to trigger bundling
    private JLabel bundleCountLabel;           // Shows how many bundles can be created
    private JLabel descriptionLabel;           // Shows description of selected parent SKU
    private static final Color BUTTON_GREEN = new Color(4, 172, 116);

    public Bundle() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel dropdownPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dropdownPanel.add(new JLabel("Select Bundle (Parent SKU):"));

        parentDropdown = new JComboBox<>();
        loadParentSKUs(); // Load parent SKUs into dropdown
        dropdownPanel.add(parentDropdown);
        topPanel.add(dropdownPanel);

        descriptionLabel = new JLabel(" ");
        descriptionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        JPanel descriptionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        descriptionPanel.add(descriptionLabel);
        topPanel.add(descriptionPanel);

        add(topPanel, BorderLayout.NORTH);

        // Setup table with 3 columns
        tableModel = new DefaultTableModel(new Object[]{"Part SKU", "Required Quantity", "Stock Available"}, 0);
        childTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                int requiredQty = (int) getValueAt(row, 1);
                int stockQty = (int) getValueAt(row, 2);
                c.setForeground((column == 2 && stockQty < requiredQty) ? Color.RED : Color.BLACK);
                return c;
            }
        };
        JScrollPane scrollPane = new JScrollPane(childTable);
        add(scrollPane, BorderLayout.CENTER);
        childTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        childTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        childTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        // Bottom panel for button and count
        bundleButton = new JButton("Bundle");
        bundleButton.setFont(new Font("Arial", Font.BOLD, 14));
        bundleButton.setOpaque(true);
        bundleButton.setBorderPainted(false);
        bundleButton.setFocusPainted(false);
        bundleButton.addActionListener(e -> bundleParts());

        bundleCountLabel = new JLabel("Can bundle: 0");
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(bundleButton);
        bottomPanel.add(bundleCountLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        parentDropdown.addActionListener(e -> {
            String selectedSKU = (String) parentDropdown.getSelectedItem();
            loadChildParts(selectedSKU);
            descriptionLabel.setText("Description: " + fetchDescriptionForSKU(selectedSKU));
        });
    }

    // Loads unique parent SKUs into the dropdown
    private void loadParentSKUs() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:VR-Factory.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT parent_SKU FROM bom")) {
            while (rs.next()) {
                parentDropdown.addItem(rs.getString("parent_SKU"));
            }
        } catch (SQLException e) {
            showError("Error loading parent SKUs: " + e.getMessage());
        }
    }

    // Fetches description for selected parent SKU
    private String fetchDescriptionForSKU(String sku) {
        String description = "";
        String query = "SELECT description FROM part WHERE SKU = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:VR-Factory.db");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, sku);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) description = rs.getString("description");
        } catch (SQLException e) {
            showError("Error fetching description: " + e.getMessage());
        }
        return description;
    }

    // Loads child parts for selected parent SKU into the table
    private void loadChildParts(String parentSKU) {
        tableModel.setRowCount(0);
        boolean canBundle = true;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:VR-Factory.db")) {
            String query = "SELECT SKU, quantity FROM bom WHERE parent_SKU = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, parentSKU);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String partSKU = rs.getString("SKU");
                int requiredQty = rs.getInt("quantity");
                int stockQty = getStockForSKU(conn, partSKU);
                tableModel.addRow(new Object[]{partSKU, requiredQty, stockQty});
                if (stockQty < requiredQty) canBundle = false;
            }
        } catch (SQLException e) {
            showError("Error loading bundle details: " + e.getMessage());
        }
        bundleButton.setEnabled(canBundle);
        bundleButton.setBackground(canBundle ? BUTTON_GREEN : UIManager.getColor("Button.background"));
        bundleButton.setForeground(canBundle ? Color.WHITE : Color.BLACK);
        bundleCountLabel.setText("Can bundle: " + calculateMaxBundles());
    }

    // Calculates how many bundles can be created based on available stock
    private int calculateMaxBundles() {
        int maxBundles = Integer.MAX_VALUE;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int requiredQty = (int) tableModel.getValueAt(i, 1);
            int stockQty = (int) tableModel.getValueAt(i, 2);
            if (requiredQty > 0) {
                int possibleBundles = stockQty / requiredQty;
                if (possibleBundles < maxBundles) maxBundles = possibleBundles;
            }
        }
        return maxBundles == Integer.MAX_VALUE ? 0 : maxBundles;
    }

    // Retrieves stock quantity for a part
    private int getStockForSKU(Connection conn, String sku) throws SQLException {
        String query = "SELECT stock FROM part WHERE SKU = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, sku);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt("stock") : 0;
        }
    }

    // Updates the stock for a given SKU by delta
    private void updateStockForSKU(Connection conn, String sku, int delta) throws SQLException {
        String query = "UPDATE part SET stock = stock + ? WHERE SKU = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, delta);
            pstmt.setString(2, sku);
            pstmt.executeUpdate();
        }
    }

    // Creates a bundle by decreasing part stock and increasing parent stock
    private void bundleParts() {
        String parentSKU = (String) parentDropdown.getSelectedItem();
        if (parentSKU == null) return;
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:VR-Factory.db")) {
            conn.setAutoCommit(false);
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String partSKU = (String) tableModel.getValueAt(i, 0);
                int requiredQty = (int) tableModel.getValueAt(i, 1);
                int stockQty = getStockForSKU(conn, partSKU);
                if (stockQty < requiredQty) {
                    showError("Not enough stock to bundle.");
                    conn.rollback();
                    return;
                }
                updateStockForSKU(conn, partSKU, -requiredQty);
            }
            updateStockForSKU(conn, parentSKU, 1);
            conn.commit();
            JOptionPane.showMessageDialog(this, "Bundle created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadChildParts(parentSKU);
        } catch (SQLException e) {
            showError("Error during bundling: " + e.getMessage());
        }
    }

    // Displays a popup error message
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    // Refreshes dropdown and re-selects first SKU
    public void refreshData() {
        parentDropdown.removeAllItems();
        loadParentSKUs();
        if (parentDropdown.getItemCount() > 0) {
            parentDropdown.setSelectedIndex(0);
            loadChildParts((String) parentDropdown.getSelectedItem());
        } else {
            tableModel.setRowCount(0);
            bundleButton.setEnabled(false);
            bundleCountLabel.setText("Can bundle: 0");
        }
    }
}
