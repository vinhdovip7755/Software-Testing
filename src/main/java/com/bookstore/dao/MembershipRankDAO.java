package com.bookstore.dao;

import com.bookstore.dto.MembershipRankDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MembershipRankDAO {

    public MembershipRankDTO getRankByPoint(int point) {
        String sql = "SELECT rank_id, rank_name, min_point, discount_percent " +
                "FROM membership_rank WHERE min_point <= ? " +
                "ORDER BY min_point DESC LIMIT 1";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, point);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new MembershipRankDTO(
                            rs.getInt("rank_id"),
                            rs.getString("rank_name"),
                            rs.getInt("min_point"),
                            rs.getDouble("discount_percent")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.List<MembershipRankDTO> getAllRanks() {
        java.util.List<MembershipRankDTO> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM membership_rank ORDER BY min_point ASC";

        try (java.sql.Connection c = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new MembershipRankDTO(
                        rs.getInt("rank_id"),
                        rs.getString("rank_name"),
                        rs.getInt("min_point"),
                        rs.getDouble("discount_percent")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addRank(MembershipRankDTO rank) {
        String sql = "INSERT INTO membership_rank (rank_name, min_point, discount_percent) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, rank.getRankName());
            ps.setInt(2, rank.getMinPoint());
            ps.setDouble(3, rank.getDiscountPercent());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRank(MembershipRankDTO rank) {
        String sql = "UPDATE membership_rank SET rank_name = ?, min_point = ?, discount_percent = ? WHERE rank_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, rank.getRankName());
            ps.setInt(2, rank.getMinPoint());
            ps.setDouble(3, rank.getDiscountPercent());
            ps.setInt(4, rank.getRankId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkRankNameExists(String rankName, int excludeId) {
        String sql = "SELECT 1 FROM membership_rank WHERE rank_name = ? AND rank_id != ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, rankName);
            ps.setInt(2, excludeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean isRankInUse(int rankId) {
        String sql = "SELECT 1 FROM customer WHERE rank_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, rankId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public boolean deleteRank(int rankId) {
        String sql = "DELETE FROM membership_rank WHERE rank_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, rankId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}