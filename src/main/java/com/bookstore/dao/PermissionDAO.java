package com.bookstore.dao;

import com.bookstore.dto.PermissionDTO;
import com.bookstore.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionDAO {
    public Map<String, PermissionDTO> getPermissionsByRoleId(int roleId) {
        Map<String, PermissionDTO> map = new HashMap<>();
        String sql = "SELECT a.action_code, p.is_view, p.is_action " +
                "FROM permission p " +
                "JOIN action a ON p.action_id = a.action_id " +
                "WHERE p.role_id = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("action_code");
                    boolean isView = rs.getBoolean("is_view");
                    boolean isAction = rs.getBoolean("is_action");
                    map.put(code, new PermissionDTO(code, isView, isAction));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean saveRolePermissions(int roleId, List<PermissionDTO> newPerms, List<Integer> actionIds) {
        String deleteSql = "DELETE FROM permission WHERE role_id = ?";
        String insertSql = "INSERT INTO permission (role_id, action_id, is_view, is_action) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                psDelete.setInt(1, roleId);
                psDelete.executeUpdate();
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                for (int i = 0; i < newPerms.size(); i++) {
                    com.bookstore.dto.PermissionDTO perm = newPerms.get(i);
                    if (perm.isView() || perm.isAction()) {
                        psInsert.setInt(1, roleId);
                        psInsert.setInt(2, actionIds.get(i));
                        psInsert.setBoolean(3, perm.isView());
                        psInsert.setBoolean(4, perm.isAction());
                        psInsert.addBatch();
                    }
                }
                psInsert.executeBatch();
            }

            conn.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }
    }
}