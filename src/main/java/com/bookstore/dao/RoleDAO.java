package com.bookstore.dao;

import com.bookstore.dto.PermissionDTO;
import com.bookstore.dto.RoleDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {

    public List<RoleDTO> selectAllRoles() {
        List<RoleDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM role WHERE role_name != 'Admin' ORDER BY role_id ASC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RoleDTO role = new RoleDTO(
                        rs.getInt("role_id"),
                        rs.getString("role_name")
                );
                list.add(role);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải danh sách chức vụ.", e);
        }
        return list;
    }

    public int insertRole(RoleDTO role) {
        String sql = "INSERT INTO role (role_name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, role.getRoleName());
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateRole(RoleDTO role) {
        String sql = "UPDATE role SET role_name = ? WHERE role_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, role.getRoleName());
            ps.setInt(2, role.getRoleId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int insertRoleWithPermissions(RoleDTO role, List<PermissionDTO> permissions, List<Integer> actionIds) {
        if (role == null || permissions == null || actionIds == null || permissions.size() != actionIds.size()) {
            return 0;
        }
        String insertRoleSql = "INSERT INTO role (role_name) VALUES (?)";
        String insertPermSql = "INSERT INTO permission (role_id, action_id, is_view, is_action) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int roleId = 0;
            try (PreparedStatement psRole = conn.prepareStatement(insertRoleSql, Statement.RETURN_GENERATED_KEYS)) {
                psRole.setString(1, role.getRoleName());
                if (psRole.executeUpdate() > 0) {
                    try (ResultSet rs = psRole.getGeneratedKeys()) {
                        if (rs.next()) {
                            roleId = rs.getInt(1);
                        }
                    }
                }
            }

            if (roleId <= 0) {
                conn.rollback();
                return 0;
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertPermSql)) {
                for (int i = 0; i < permissions.size(); i++) {
                    PermissionDTO perm = permissions.get(i);
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
            return roleId;
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return 0;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ignored) {}
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    public boolean updateRoleWithPermissions(RoleDTO role, List<PermissionDTO> permissions, List<Integer> actionIds) {
        if (role == null || role.getRoleId() <= 1
                || permissions == null || actionIds == null || permissions.size() != actionIds.size()) {
            return false;
        }
        String updateRoleSql = "UPDATE role SET role_name = ? WHERE role_id = ?";
        String deletePermSql = "DELETE FROM permission WHERE role_id = ?";
        String insertPermSql = "INSERT INTO permission (role_id, action_id, is_view, is_action) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psRole = conn.prepareStatement(updateRoleSql)) {
                psRole.setString(1, role.getRoleName());
                psRole.setInt(2, role.getRoleId());
                if (psRole.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement psDelete = conn.prepareStatement(deletePermSql)) {
                psDelete.setInt(1, role.getRoleId());
                psDelete.executeUpdate();
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertPermSql)) {
                for (int i = 0; i < permissions.size(); i++) {
                    PermissionDTO perm = permissions.get(i);
                    if (perm.isView() || perm.isAction()) {
                        psInsert.setInt(1, role.getRoleId());
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
                try { conn.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ignored) {}
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }
}