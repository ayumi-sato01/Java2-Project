package com.bushnell;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class UpdateStock extends JPanel {
    public UpdateStock() {
        setLayout(new BorderLayout()); // Makes layout simple and label centered
        setBackground(Color.WHITE);

        JLabel label = new JLabel("Update Stock", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.BLACK);
        this.add(label, BorderLayout.CENTER);
    
        System.out.println("UpdateStock panel is being created...");
        System.out.println(new File("/Users/ayumisato/Java2-Project/VR-Factory.db").exists());

        String[] skuData = fetchSKUsFromDatabase();
    
        System.out.println("SKU data fetched: " + skuData.length + " items.");
    
        // Create combo box
        JComboBox<String> comboBox = new JComboBox<>(skuData);
        comboBox.setBounds(50, 50, 180, 30);
        add(comboBox);
    }


    private String[] fetchSKUsFromDatabase() {
        ArrayList<String> skuList = new ArrayList<>();
        String url = "jdbc:sqlite:/Users/ayumisato/Java2-Project/VR-Factory.db"; 
        
        System.out.println("Connecting to database...");
        
        try {
            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            try (
                // Declare resources that implement AutoCloseable
                Connection conn = DriverManager.getConnection(url);
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("SELECT sku FROM part")
            ) {
                while (rs.next()) {
                    skuList.add(rs.getString("sku"));
                }
                System.out.println("SKUs: " + skuList);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        
            System.out.println("Database query complete. Fetched " + skuList.size() + " SKUs.");
            return skuList.toArray(new String[0]);
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Please add the sqlite-jdbc jar to the classpath.");
            e.printStackTrace();
        }
        
        return new String[0];  // Return an empty array if the exception occurs
    }
    
}