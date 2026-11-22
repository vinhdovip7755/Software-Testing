package com.bookstore.dao;

import com.bookstore.dto.AccountDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public AccountDTO selectByUsername(String username) {
        AccountDTO acc = null;

        String sql = "SELECT a.* FROM account a WHERE a.username = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    acc = new AccountDTO(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getInt("employee_id"),
                            rs.getInt("status")
                    );
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể truy vấn tài khoản.", e);
        }
        return acc;
    }

    public AccountDTO selectByEmployeeId(int employeeId) {
        AccountDTO acc = null;

        String sql = "SELECT a.* FROM account a WHERE a.employee_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    acc = new AccountDTO(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getInt("employee_id"),
                            rs.getInt("status")
                    );
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể truy vấn tài khoản nhân viên.", e);
        }
        return acc;
    }

    public boolean isEmployeeAccountActiveWithRole(int employeeId, int roleId) {
        String sql = "SELECT 1 FROM employee e " +
                "JOIN account a ON a.employee_id = e.employee_id " +
                "WHERE e.employee_id = ? AND e.status = 1 AND e.role_id = ? AND a.status = 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể xác thực trạng thái tài khoản.", e);
        }
    }

    public boolean updateUsername(String oldUsername, String newUsername) {
        String sql = "UPDATE account SET username = ? WHERE username = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.setString(2, oldUsername);
            return ps.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean updateAccount(AccountDTO acc, boolean isChangePassword) {
        String sql = isChangePassword
                ? "UPDATE account SET password = ?, status = ? WHERE username = ?"
                : "UPDATE account SET status = ? WHERE username = ?";

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            int updatedAccounts;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                if (isChangePassword) {
                    ps.setString(1, acc.getPassword());
                    ps.setInt(2, acc.getStatus());
                    ps.setString(3, acc.getUsername());
                } else {
                    ps.setInt(1, acc.getStatus());
                    ps.setString(2, acc.getUsername());
                }
                updatedAccounts = ps.executeUpdate();
            }

            if (updatedAccounts == 0) {
                c.rollback();
                return false;
            }

            if (acc.getEmployeeId() > 0) {
                String syncEmployeeSql = "UPDATE employee SET status = ? WHERE employee_id = ?";
                try (PreparedStatement psEmp = c.prepareStatement(syncEmployeeSql)) {
                    psEmp.setInt(1, acc.getStatus());
                    psEmp.setInt(2, acc.getEmployeeId());
                    psEmp.executeUpdate();
                }
            }

            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                try {
                    c.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (c != null) {
                try {
                    c.setAutoCommit(true);
                } catch (Exception ignored) {
                }
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public boolean restorePasswordHash(String username, String passwordHash) {
        String sql = "UPDATE account SET password = ? WHERE username = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAdminCredentialsAtomic(
            int employeeId,
            String oldUsername,
            String newUsername,
            String hashedNewPassword
    ) {
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            String accSql = hashedNewPassword != null
                    ? "UPDATE account SET username = ?, password = ? WHERE employee_id = ? AND username = ?"
                    : "UPDATE account SET username = ? WHERE employee_id = ? AND username = ?";
            int updatedAcc;
            try (PreparedStatement psAcc = c.prepareStatement(accSql)) {
                if (hashedNewPassword != null) {
                    psAcc.setString(1, newUsername);
                    psAcc.setString(2, hashedNewPassword);
                    psAcc.setInt(3, employeeId);
                    psAcc.setString(4, oldUsername);
                } else {
                    psAcc.setString(1, newUsername);
                    psAcc.setInt(2, employeeId);
                    psAcc.setString(3, oldUsername);
                }
                updatedAcc = psAcc.executeUpdate();
            }

            if (updatedAcc == 0) {
                c.rollback();
                return false;
            }

            String empSql = "UPDATE employee SET email = ? WHERE employee_id = ? AND role_id = 1";
            int updatedEmp;
            try (PreparedStatement psEmp = c.prepareStatement(empSql)) {
                psEmp.setString(1, newUsername);
                psEmp.setInt(2, employeeId);
                updatedEmp = psEmp.executeUpdate();
            }

            if (updatedEmp == 0) {
                c.rollback();
                return false;
            }

            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                try {
                    c.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (c != null) {
                try {
                    c.setAutoCommit(true);
                } catch (Exception ignored) {
                }
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public List<AccountDTO> selectAllAccounts() {
        List<AccountDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM account";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new AccountDTO(rs.getString("username"), rs.getString("password"),
                        rs.getInt("employee_id"), rs.getInt("status")));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể tải danh sách tài khoản.", e);
        }
        return list;
    }

    public boolean insert(AccountDTO acc) {
        String sql = "INSERT INTO account (username, password, employee_id, status) VALUES (?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, acc.getUsername());
            ps.setString(2, acc.getPassword());
            ps.setInt(3, acc.getEmployeeId());
            ps.setInt(4, acc.getStatus());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
