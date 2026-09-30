package com.bookstore.dao;

import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ImportDetailDAO {

    public boolean add(ImportDetailDTO detail) {
        try {
            Connection c = DatabaseConnection.getConnection();
            String sql = "INSERT INTO import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price, cover_price, discount_percent) VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE import_quantity = import_quantity + VALUES(import_quantity)";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, detail.getImportID());
            ps.setInt(2, detail.getBookID());
            ps.setInt(3, detail.getQuantity());
            ps.setDouble(4, detail.getPrice());
            ps.setDouble(5, detail.getCoverPrice());
            ps.setDouble(6, detail.getDiscountPercent());

            boolean added = ps.executeUpdate() > 0;
            DatabaseConnection.closeConnection(c);
            return added;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ImportDetailDTO> getDetailsByImportId(int importId) {
        List<ImportDetailDTO> list = new ArrayList<>();
        try {
            Connection c = DatabaseConnection.getConnection();

            String sql = "SELECT d.import_ticket_id, d.book_id, b.book_name, d.import_quantity, d.import_price, d.cover_price, d.discount_percent " +
                    "FROM import_ticket_detail d " +
                    "JOIN book b ON d.book_id = b.book_id " +
                    "WHERE d.import_ticket_id = ?";

            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, importId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ImportDetailDTO detail = new ImportDetailDTO();
                detail.setImportID(rs.getInt("import_ticket_id"));
                detail.setBookID(rs.getInt("book_id"));
                detail.setBookName(rs.getString("book_name"));
                detail.setQuantity(rs.getInt("import_quantity"));
                detail.setPrice(rs.getDouble("import_price"));
                detail.setCoverPrice(rs.getDouble("cover_price"));
                detail.setDiscountPercent(rs.getDouble("discount_percent"));
                list.add(detail);
            }
            DatabaseConnection.closeConnection(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}