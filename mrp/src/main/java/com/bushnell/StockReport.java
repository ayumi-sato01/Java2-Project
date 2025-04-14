package com.bushnell;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.IOException;
import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class StockReport extends JPanel {

    private JTable stockTable;
    private DefaultTableModel tableModel;
    private JButton exportButton;

    public StockReport() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Stock Report", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"SKU", "Description", "Price", "Stock"};
        tableModel = new DefaultTableModel(columns, 0);
        stockTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(stockTable);
        add(scrollPane, BorderLayout.CENTER);

        exportButton = new JButton("Export as PDF");
        exportButton.addActionListener(e -> exportStockReportToPDF());
        add(exportButton, BorderLayout.SOUTH);

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);

        try (Connection conn = DriverManager.getConnection(Database.DBName);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT sku, description, price, stock FROM part")) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("sku"),
                        rs.getString("description"),
                        rs.getString("price"),
                        rs.getString("stock")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading stock data.");
        }
    }

    private void exportStockReportToPDF() {
        PDDocument document = new PDDocument();
        try {
            float margin = 50;
            float yStart = PDRectangle.LETTER.getHeight() - margin;
            float tableWidth = PDRectangle.LETTER.getWidth() - 2 * margin;
            float[] colWidths = {160, 200, 80, 80}; // Adjusted: SKU more space, Description less
            String[] headers = {"SKU", "Description", "Price", "Stock"};
            PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
            PDType1Font bodyFont = PDType1Font.HELVETICA;
            int fontSize = 10;
            float cellHeight = 15;

            String timestamp = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date());
            List<List<String>> rows = new ArrayList<>();
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Vector<?> row = (Vector<?>) tableModel.getDataVector().get(i);
                List<String> rowList = new ArrayList<>();
                for (Object obj : row) {
                    rowList.add(obj.toString());
                }
                rows.add(rowList);
            }

            int rowIndex = 0;
            int pageNum = 0;

            while (rowIndex < rows.size()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                document.addPage(page);
                pageNum++;

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.setFont(bodyFont, fontSize);

                float yPos = yStart;

                // Header
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPos);
                contentStream.setFont(headerFont, 12);
                contentStream.showText("Visual Robotics Stock Report");
                contentStream.endText();

                yPos -= 20;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPos);
                contentStream.setFont(bodyFont, 10);
                contentStream.showText("Generated: " + timestamp);
                contentStream.endText();

                yPos -= 25;

                // Table headers
                float xPos = margin;
                contentStream.setFont(headerFont, fontSize);
                for (int i = 0; i < headers.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPos, yPos);
                    contentStream.showText(headers[i]);
                    contentStream.endText();
                    xPos += colWidths[i];
                }

                yPos -= cellHeight;

                // Rows
                contentStream.setFont(bodyFont, fontSize);
                while (rowIndex < rows.size() && yPos > margin + cellHeight) {
                    xPos = margin;
                    List<String> row = rows.get(rowIndex);
                    for (int i = 0; i < row.size(); i++) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(xPos, yPos);
                        contentStream.showText(row.get(i));
                        contentStream.endText();
                        xPos += colWidths[i];
                    }
                    yPos -= cellHeight;
                    rowIndex++;
                }

                // Footer
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
                contentStream.newLineAtOffset(PDRectangle.LETTER.getWidth() - margin - 100, margin - 10);
                contentStream.showText("Page " + pageNum);
                contentStream.endText();

                contentStream.close();
            }

            document.save("StockReport.pdf");
            JOptionPane.showMessageDialog(this, "Stock Report exported successfully to: StockReport.pdf");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error exporting stock report to PDF.");
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
