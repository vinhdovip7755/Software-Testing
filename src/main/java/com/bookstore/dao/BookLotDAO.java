package com.bookstore.dao;

import com.bookstore.dto.BookLotDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookLotDAO {

    public List<BookLotDTO> selectAll() {
        List<BookLotDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM book_lot";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<BookLotDTO> getByBookId(int bookId) {
        List<BookLotDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM book_lot WHERE book_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int add(BookLotDTO lot) {
        String sql = "INSERT INTO book_lot(book_id, import_ticket_id, import_date, cover_price, discount_percent, import_price, selling_price, quantity_initial, quantity_remain) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, lot.getBookId());
            ps.setInt(2, lot.getImportTicketId());
            ps.setTimestamp(3, lot.getImportDate());
            ps.setDouble(4, lot.getCoverPrice());
            ps.setDouble(5, lot.getDiscountPercent());
            ps.setDouble(6, lot.getImportPrice());
            ps.setDouble(7, lot.getSellingPrice());
            ps.setInt(8, lot.getQuantityInitial());
            ps.setInt(9, lot.getQuantityRemain());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean update(BookLotDTO lot) {
        String sql = "UPDATE book_lot SET book_id = ?, import_ticket_id = ?, import_date = ?, cover_price = ?, discount_percent = ?, import_price = ?, selling_price = ?, quantity_initial = ?, quantity_remain = ? " +
                "WHERE lot_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, lot.getBookId());
            ps.setInt(2, lot.getImportTicketId());
            ps.setTimestamp(3, lot.getImportDate());
            ps.setDouble(4, lot.getCoverPrice());
            ps.setDouble(5, lot.getDiscountPercent());
            ps.setDouble(6, lot.getImportPrice());
            ps.setDouble(7, lot.getSellingPrice());
            ps.setInt(8, lot.getQuantityInitial());
            ps.setInt(9, lot.getQuantityRemain());
            ps.setInt(10, lot.getLotId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int lotId) {
        String sql = "DELETE FROM book_lot WHERE lot_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, lotId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private BookLotDTO mapResultSet(ResultSet rs) throws SQLException {
        return new BookLotDTO(
                rs.getInt("lot_id"),
                rs.getInt("book_id"),
                rs.getInt("import_ticket_id"),
                rs.getTimestamp("import_date"),
                rs.getDouble("cover_price"),
                rs.getDouble("discount_percent"),
                rs.getDouble("import_price"),
                rs.getDouble("selling_price"),
                rs.getInt("quantity_initial"),
                rs.getInt("quantity_remain")
        );
    }
}
