package com.bookstore.dao;

import com.bookstore.dto.PriceDTO;
import com.bookstore.util.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PriceDAO {
    public static final double MAX_PROFIT_RATE = 9.99;
    public static final double MAX_PRICE = 999_999_999_999_999.0;

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
                "COALESCE(" +
                "  (SELECT p.selling_price FROM price p WHERE p.book_id = b.book_id AND p.is_active = 1 " +
                "   ORDER BY p.effective_date DESC, p.price_id DESC LIMIT 1), " +
                "  b.selling_price" +
                ") as selling_price " +
                "FROM book b " +
                "JOIN category c ON b.category_id = c.category_id " +
                "LEFT JOIN book_author ba ON b.book_id = ba.book_id " +
                "LEFT JOIN author a ON ba.author_id = a.author_id " +
                "WHERE b.status = 1 " +
                "GROUP BY b.book_id";

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
        if (bookId <= 0
                || !Double.isFinite(basePrice) || basePrice <= 0 || basePrice > MAX_PRICE
                || !Double.isFinite(profitRate) || profitRate < 0 || profitRate > MAX_PROFIT_RATE
                || !Double.isFinite(sellingPrice) || sellingPrice <= 0 || sellingPrice > MAX_PRICE) {
            return false;
        }

        double roundedBasePrice = roundMoney(basePrice);
        double storedProfitPercent = toStoredPercent(profitRate);
        double roundedSellingPrice = roundMoney(sellingPrice);
        if (roundedBasePrice <= 0 || roundedSellingPrice <= 0 || roundedSellingPrice > MAX_PRICE) {
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String lockBookSql = "SELECT book_id FROM book WHERE book_id = ? FOR UPDATE";
            try (PreparedStatement psLock = conn.prepareStatement(lockBookSql)) {
                psLock.setInt(1, bookId);
                try (ResultSet rs = psLock.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            String disableOldSql = "UPDATE price SET is_active = 0, end_date = NOW() WHERE book_id = ? AND is_active = 1";
            try (PreparedStatement psDisable = conn.prepareStatement(disableOldSql)) {
                psDisable.setInt(1, bookId);
                psDisable.executeUpdate();
            }

            String insertNewSql = "INSERT INTO price (book_id, base_price, profit_rate, selling_price, effective_date, is_active) " +
                    "VALUES (?, ?, ?, ?, NOW(), 1)";
            try (PreparedStatement psInsert = conn.prepareStatement(insertNewSql)) {
                psInsert.setInt(1, bookId);
                psInsert.setDouble(2, roundedBasePrice);
                psInsert.setDouble(3, storedProfitPercent);
                psInsert.setDouble(4, roundedSellingPrice);
                psInsert.executeUpdate();
            }

            String updateBookSql = "UPDATE book SET selling_price = ? WHERE book_id = ?";
            try (PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql)) {
                psUpdateBook.setDouble(1, roundedSellingPrice);
                psUpdateBook.setInt(2, bookId);
                psUpdateBook.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return false;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    public PriceDTO getActivePriceByBookId(int bookId) {
        String sql = "SELECT * FROM price WHERE book_id = ? AND is_active = 1 " +
                "ORDER BY effective_date DESC, price_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PriceDTO price = new PriceDTO();
                    price.setPriceId(rs.getInt("price_id"));
                    price.setBookId(rs.getInt("book_id"));
                    price.setBasePrice(rs.getDouble("base_price"));
                    price.setProfitRate(rs.getDouble("profit_rate") / 100.0);
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
        if (price == null || price.getBookId() <= 0
                || !Double.isFinite(price.getBasePrice()) || price.getBasePrice() <= 0 || price.getBasePrice() > MAX_PRICE
                || !Double.isFinite(price.getProfitRate()) || price.getProfitRate() < 0 || price.getProfitRate() > MAX_PROFIT_RATE
                || !Double.isFinite(price.getSellingPrice()) || price.getSellingPrice() <= 0 || price.getSellingPrice() > MAX_PRICE) {
            return false;
        }

        String sql = "INSERT INTO price (book_id, base_price, profit_rate, selling_price, effective_date, is_active) " +
                "VALUES (?, ?, ?, ?, NOW(), 1)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, price.getBookId());
            ps.setDouble(2, roundMoney(price.getBasePrice()));
            ps.setDouble(3, toStoredPercent(price.getProfitRate()));
            ps.setDouble(4, roundMoney(price.getSellingPrice()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBulkPrice(List<PriceDTO> listToUpdate, double newProfitRate) {
        if (listToUpdate == null || listToUpdate.isEmpty()
                || !Double.isFinite(newProfitRate) || newProfitRate < 0 || newProfitRate > MAX_PROFIT_RATE) {
            return false;
        }

        Map<Integer, PriceDTO> eligibleByBookId = new TreeMap<>();
        for (PriceDTO p : listToUpdate) {
            if (p == null || p.getBookId() <= 0) {
                continue;
            }
            if (!Double.isFinite(p.getBasePrice()) || p.getBasePrice() <= 0 || p.getBasePrice() > MAX_PRICE) {
                continue;
            }
            double predictedPrice = calculateSellingPrice(p.getBasePrice(), newProfitRate);
            if (!Double.isFinite(predictedPrice) || predictedPrice <= 0 || predictedPrice > MAX_PRICE) {
                return false;
            }
            eligibleByBookId.putIfAbsent(p.getBookId(), p);
        }

        if (eligibleByBookId.isEmpty()) {
            return false;
        }

        double storedProfitPercent = toStoredPercent(newProfitRate);
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String lockBookSql = "SELECT book_id FROM book WHERE book_id = ? FOR UPDATE";
            String disableOldSql = "UPDATE price SET is_active = 0, end_date = NOW() WHERE book_id = ? AND is_active = 1";
            String insertNewSql = "INSERT INTO price (book_id, base_price, profit_rate, selling_price, effective_date, is_active) " +
                    "VALUES (?, ?, ?, ?, NOW(), 1)";
            String updateBookSql = "UPDATE book SET selling_price = ? WHERE book_id = ?";

            try (PreparedStatement psLock = conn.prepareStatement(lockBookSql);
                 PreparedStatement psDisable = conn.prepareStatement(disableOldSql);
                 PreparedStatement psInsert = conn.prepareStatement(insertNewSql);
                 PreparedStatement psUpdateBook = conn.prepareStatement(updateBookSql)) {

                int updatedCount = 0;
                for (PriceDTO p : eligibleByBookId.values()) {
                    psLock.setInt(1, p.getBookId());
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) {
                            continue;
                        }
                    }

                    double roundedBasePrice = roundMoney(p.getBasePrice());
                    double predictedPrice = calculateSellingPrice(p.getBasePrice(), newProfitRate);

                    psDisable.setInt(1, p.getBookId());
                    psDisable.addBatch();

                    psInsert.setInt(1, p.getBookId());
                    psInsert.setDouble(2, roundedBasePrice);
                    psInsert.setDouble(3, storedProfitPercent);
                    psInsert.setDouble(4, predictedPrice);
                    psInsert.addBatch();

                    psUpdateBook.setDouble(1, predictedPrice);
                    psUpdateBook.setInt(2, p.getBookId());
                    psUpdateBook.addBatch();
                    updatedCount++;
                }

                if (updatedCount == 0) {
                    conn.rollback();
                    return false;
                }

                psDisable.executeBatch();
                psInsert.executeBatch();
                psUpdateBook.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            rollbackQuietly(conn);
            e.printStackTrace();
            return false;
        } finally {
            closeTransactionalConnection(conn);
        }
    }

    public static double calculateSellingPrice(double basePrice, double profitRate) {
        if (!Double.isFinite(basePrice) || !Double.isFinite(profitRate)) {
            return Double.NaN;
        }
        return BigDecimal.valueOf(basePrice)
                .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(profitRate)))
                .setScale(0, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static double roundMoney(double amount) {
        return BigDecimal.valueOf(amount).setScale(0, RoundingMode.HALF_UP).doubleValue();
    }

    private static double toStoredPercent(double profitRateRatio) {
        return BigDecimal.valueOf(profitRateRatio)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeTransactionalConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            } finally {
                DatabaseConnection.closeConnection(conn);
            }
        }
    }
}
