package com.bookstore.dao;

import com.bookstore.dto.ActionDTO;
import com.bookstore.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActionDAO {
    public List<ActionDTO> selectAllActions() {
        List<ActionDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM action ORDER BY action_id ASC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ActionDTO(
                        rs.getInt("action_id"),
                        rs.getString("action_code"),
                        rs.getString("action_name")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}