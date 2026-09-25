package com.bookstore.dao;

import com.bookstore.dto.PromotionDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PromotionDAO {
    public String getPromotionNameByBookId(int bookId) {
        String promoName = null;
        String sql = "SELECT p.promotion_name FROM promotion p " +
                "JOIN promotion_detail pd ON p.promotion_id = pd.promotion_id " +
                "WHERE pd.book_id = ? " +
                "AND p.status = 1 " +
                "AND NOW() BETWEEN p.start_date AND p.end_date " +
                "ORDER BY p.percent DESC LIMIT 1";

        try (java.sql.Connection c = com.bookstore.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    promoName = rs.getString("promotion_name");
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return promoName;
    }
    
    public double getPromotionPercentByBookId(int bookId) {
        double percent = 0;
        String sql = "SELECT p.percent FROM promotion p " +
                "JOIN promotion_detail pd ON p.promotion_id = pd.promotion_id " +
                "WHERE pd.book_id = ? " +
                "AND p.status = 1 " +
                "AND NOW() BETWEEN p.start_date AND p.end_date " +
                "ORDER BY p.percent DESC LIMIT 1";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, bookId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    percent = rs.getDouble("percent");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return percent;
    }

    public List<PromotionDTO> selectAllPromotions() {
        List<PromotionDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM promotion";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PromotionDTO(
                        rs.getInt("promotion_id"),
                        rs.getString("promotion_name"),
                        rs.getDouble("percent"),
                        rs.getTimestamp("start_date"),
                        rs.getTimestamp("end_date"),
                        rs.getInt("status")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateStatus(PromotionDTO p) {
        String sql = "UPDATE promotion SET status = ? WHERE promotion_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, p.getStatus());
            ps.setInt(2, p.getPromotionId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int add(PromotionDTO p) {
        String sql = "INSERT INTO promotion (promotion_name, percent, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPromotionName());
            ps.setDouble(2, p.getPercent());
            ps.setTimestamp(3, p.getStartDate());
            ps.setTimestamp(4, p.getEndDate());
            ps.setInt(5, p.getStatus());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean savePromotionDetails(int promoId, List<Integer> bookIds) {
        String sql = "INSERT INTO promotion_detail (promotion_id, book_id) VALUES (?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int bookId : bookIds) {
                ps.setInt(1, promoId);
                ps.setInt(2, bookId);
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> getSelectedBookIds(int promoId) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT book_id FROM promotion_detail WHERE promotion_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(rs.getInt("book_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean update(PromotionDTO p) {
        String sql = "UPDATE promotion SET promotion_name=?, percent=?, start_date=?, end_date=?, status=? WHERE promotion_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getPromotionName());
            ps.setDouble(2, p.getPercent());
            ps.setTimestamp(3, p.getStartDate());
            ps.setTimestamp(4, p.getEndDate());
            ps.setInt(5, p.getStatus());
            ps.setInt(6, p.getPromotionId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deletePromotionDetails(int promoId) {
        String sql = "DELETE FROM promotion_detail WHERE promotion_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, promoId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean savePromotionTransaction(boolean isEdit, PromotionDTO dto, List<Integer> bookIds) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int promoId = dto.getPromotionId();
            if (!isEdit) {
                String sqlInsert = "INSERT INTO promotion (promotion_name, percent, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, dto.getPromotionName());
                    ps.setDouble(2, dto.getPercent());
                    ps.setTimestamp(3, dto.getStartDate());
                    ps.setTimestamp(4, dto.getEndDate());
                    ps.setInt(5, dto.getStatus());
                    if (ps.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            promoId = rs.getInt(1);
                        }
                    }
                }
            } else {
                String sqlUpdate = "UPDATE promotion SET promotion_name=?, percent=?, start_date=?, end_date=?, status=? WHERE promotion_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setString(1, dto.getPromotionName());
                    ps.setDouble(2, dto.getPercent());
                    ps.setTimestamp(3, dto.getStartDate());
                    ps.setTimestamp(4, dto.getEndDate());
                    ps.setInt(5, dto.getStatus());
                    ps.setInt(6, promoId);
                    if (ps.executeUpdate() <= 0) {
                        conn.rollback();
                        return false;
                    }
                }

                String sqlDel = "DELETE FROM promotion_detail WHERE promotion_id = ?";
                try (PreparedStatement psDel = conn.prepareStatement(sqlDel)) {
                    psDel.setInt(1, promoId);
                    psDel.executeUpdate();
                }
            }

            if (bookIds != null && !bookIds.isEmpty()) {
                String sqlDetail = "INSERT INTO promotion_detail (promotion_id, book_id) VALUES (?, ?)";
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    for (int bookId : bookIds) {
                        psDetail.setInt(1, promoId);
                        psDetail.setInt(2, bookId);
                        psDetail.addBatch();
                    }
                    psDetail.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
            }
        }
    }
}