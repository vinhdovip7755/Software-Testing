package com.bookstore.dao;

import com.bookstore.dto.PriceDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PriceDAO {

    public List<PriceDTO> getAllActivePrices() {
        List<PriceDTO> list = new ArrayList<>();
        String sql = "SELECT b.book_id, b.book_name, c.category_name, " +
                "GROUP_CONCAT(DISTINCT a.author_name SEPARATOR ', ') as author_names, " +
                "COALESCE(" +
                "  (SELECT SUM(bl.quantity_remain * bl.import_price) / SUM(bl.quantity_remain) " +
                "   FROM book_lot bl WHERE bl.book_id = b.book_id AND bl.quantity_remain > 0), " +
                "  (SELECT SUM(bl.quantity_initial * bl.import_price) / SUM(bl.quantity_initial) " +
                "   FROM book_lot bl WHERE bl.book_id = b.book_id), " +
                "  0" +
                ") as base_price, " +
                "IFNULL(bp.selling_price, b.selling_price) as selling_price " +
                "FROM book b " +
                "JOIN category c ON b.category_id = c.category_id " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.author_id = a.author_id " +
                "LEFT JOIN price bp ON b.book_id = bp.book_id AND bp.is_active = 1 " +
                "WHERE b.status = 1 " +
                "GROUP BY b.book_id, bp.selling_price, b.selling_price";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                double basePrice = rs.getDouble("base_price");
                double sellingPrice = rs.getDouble("selling_price");
                double profitRate = 0;
                if (basePrice > 0) {
                    profitRate = (sellingPrice - basePrice) / basePrice;
                }
                PriceDTO dto = new PriceDTO(
                        rs.getInt("book_id"),
                        basePrice,
                        profitRate,
                        sellingPrice,
                        rs.getString("book_name"),
                        rs.getString("author_names"),
                        rs.getString("category_name")
                );
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createNewPrice(int bookId, double basePrice, double profitRate, double sellingPrice) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String disableOldSql = "UPDATE price SET is_active = 0, end_date = NOW() WHERE book_id = ? AND is_active = 1";
            try (PreparedStatement psDisable = conn.prepareStatement(disableOldSql)) {
                psDisable.setInt(1, bookId);
                psDisable.executeUpdate();
            }

            String insertNewSql = "INSERT INTO price (book_id, base_price, profit_rate, selling_price, effective_date, is_active) " +
                    "VALUES (?, ?, ?, ?, NOW(), 1)";
            try (PreparedStatement psInsert = conn.prepareStatement(insertNewSql)) {
                psInsert.setInt(1, bookId);
                psInsert.setDouble(2, basePrice);
                psInsert.setDouble(3, profitRate);
                psInsert.setDouble(4, sellingPrice);
                psInsert.executeUpdate();
            }

            String updateBookSql = "UPDATE book SET selling_price = ? WHERE book_id = ?";
            try (PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql)) {
                psUpdateBook.setDouble(1, sellingPrice);
                psUpdateBook.setInt(2, bookId);
                psUpdateBook.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public PriceDTO getActivePriceByBookId(int bookId) {
        String sql = "SELECT * FROM price WHERE book_id = ? AND is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PriceDTO price = new PriceDTO();
                    price.setPriceId(rs.getInt("price_id"));
                    price.setBookId(rs.getInt("book_id"));
                    price.setBasePrice(rs.getDouble("base_price"));
                    price.setProfitRate(rs.getDouble("profit_rate"));
                    price.setSellingPrice(rs.getDouble("selling_price"));
                    price.setEffectiveDate(rs.getTimestamp("effective_date"));
                    price.setIsActive(rs.getInt("is_active"));
                    return price;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deactivatePrice(int bookId) {
        String sql = "UPDATE price SET is_active = 0, end_date = NOW() WHERE book_id = ? AND is_active = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addPrice(PriceDTO price) {
        String sql = "INSERT INTO price (book_id, base_price, profit_rate, selling_price, effective_date, is_active) " +
                "VALUES (?, ?, ?, ?, NOW(), 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, price.getBookId());
            ps.setDouble(2, price.getBasePrice());
            ps.setDouble(3, price.getProfitRate());
            ps.setDouble(4, price.getSellingPrice());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBulkPrice(List<PriceDTO> listToUpdate, double newProfitRate) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String disableOldSql = "UPDATE price SET is_active = 0, end_date = NOW() WHERE book_id = ? AND is_active = 1";
            String insertNewSql = "INSERT INTO price (book_id, base_price, profit_rate, selling_price, effective_date, is_active) " +
                    "VALUES (?, ?, ?, ?, NOW(), 1)";
            String updateBookSql = "UPDATE book SET selling_price = ? WHERE book_id = ?";

            try (PreparedStatement psDisable = conn.prepareStatement(disableOldSql);
                 PreparedStatement psInsert = conn.prepareStatement(insertNewSql);
                 PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql)) {

                for (PriceDTO p : listToUpdate) {
                    psDisable.setInt(1, p.getBookId());
                    psDisable.addBatch();

                    double predictedPrice = p.getBasePrice() * (1 + newProfitRate);
                    psInsert.setInt(1, p.getBookId());
                    psInsert.setDouble(2, p.getBasePrice());
                    psInsert.setDouble(3, newProfitRate);
                    psInsert.setDouble(4, predictedPrice);
                    psInsert.addBatch();

                    psUpdateBook.setDouble(1, predictedPrice);
                    psUpdateBook.setInt(2, p.getBookId());
                    psUpdateBook.addBatch();
                }

                psDisable.executeBatch();
                psInsert.executeBatch();
                psUpdateBook.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    DatabaseConnection.closeConnection(conn);
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}