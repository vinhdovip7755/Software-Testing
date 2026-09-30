package com.bookstore.dao;

import com.bookstore.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SystemParameterDAO {
    public String getValueByKey(String key) {
        String sql = "SELECT param_value FROM system_parameter WHERE param_key = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("param_value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}