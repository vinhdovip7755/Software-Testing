package com.bookstore.dao;

import com.bookstore.dto.AccountDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public AccountDTO selectByUsername(String username) {
        AccountDTO acc = null;

        try {
            Connection c = DatabaseConnection.getConnection();
            String sql = "SELECT a.* FROM account a " +
                    "JOIN employee e ON a.employee_id = e.employee_id " +
                    "WHERE a.status = 1 AND a.username = ? AND e.status = 1 ";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                acc = new AccountDTO(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("employee_id"),
                        rs.getInt("status")
                );
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return acc;
    }

    public boolean updateAccount(AccountDTO acc, boolean isChangePassword) {
        String sql;

        if (isChangePassword) {
            sql = "UPDATE account SET password = ?, status = ? WHERE username = ?";
        } else {
            sql = "UPDATE account SET status = ? WHERE username = ?";
        }

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (isChangePassword) {
                ps.setString(1, acc.getPassword());
                ps.setInt(2, acc.getStatus());
                ps.setString(3, acc.getUsername());
            } else {
                ps.setInt(1, acc.getStatus());
                ps.setString(2, acc.getUsername());
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
            e.printStackTrace();
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
