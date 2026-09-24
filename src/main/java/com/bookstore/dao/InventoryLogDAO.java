package com.bookstore.dao;
import com.bookstore.dto.InventoryLogDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryLogDAO {
    public List<InventoryLogDTO> getAll() {
        List<InventoryLogDTO> list = new ArrayList<>();
        try {
            Connection c = com.bookstore.util.DatabaseConnection.getConnection();
            String sql = "SELECT l.*, b.book_name FROM inventory_log l JOIN book b ON l.book_id = b.book_id ORDER BY l.created_date DESC";
            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                InventoryLogDTO dto = new InventoryLogDTO();
                dto.setLogId(rs.getInt("log_id"));
                dto.setAction(rs.getString("action"));
                dto.setChangeQuantity(rs.getInt("change_quantity"));
                dto.setRemainQuantity(rs.getInt("remain_quantity"));
                dto.setReferenceId(rs.getInt("reference_id"));
                dto.setCreatedDate(rs.getTimestamp("created_date"));
                dto.setBookId(rs.getInt("book_id"));
                dto.setBookName(rs.getString("book_name"));
                list.add(dto);
            }
            com.bookstore.util.DatabaseConnection.closeConnection(c);
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(InventoryLogDTO dto) {
        boolean result = false;
        try {
            Connection c = com.bookstore.util.DatabaseConnection.getConnection();
            String sql = "INSERT INTO inventory_log (action, change_quantity, remain_quantity, reference_id, book_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = c.prepareStatement(sql);

            ps.setString(1, dto.getAction());
            ps.setInt(2, dto.getChangeQuantity());
            ps.setInt(3, dto.getRemainQuantity());
            ps.setInt(4, dto.getReferenceId());
            ps.setInt(5, dto.getBookId());

            result = ps.executeUpdate() > 0;
            com.bookstore.util.DatabaseConnection.closeConnection(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}