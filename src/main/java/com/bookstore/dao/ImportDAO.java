package com.bookstore.dao;

import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDAO {
    public int add(ImportTicketDTO importDTO) {
        int generatedId = -1;
        try {
            Connection c = DatabaseConnection.getConnection();
            String sql = "INSERT INTO import_ticket (employee_id, supplier_id, total_import_price, status) VALUES (?, ?, ?, 1)";
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, importDTO.getEmployeeID());
            ps.setInt(2, importDTO.getSupplierID());
            ps.setDouble(3, importDTO.getTotalPrice());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getInt(1);
                }
            }
            DatabaseConnection.closeConnection(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public List<ImportTicketDTO> getAll() {
        List<ImportTicketDTO> list = new ArrayList<>();
        try {
            Connection c = DatabaseConnection.getConnection();

            String sql = "SELECT i.import_ticket_id, s.supplier_name, i.created_date, " +
                    "e.employee_name AS creator_name, i.total_import_price, i.status, " +
                    "a.employee_name AS approver_name " +
                    "FROM import_ticket i " +
                    "JOIN supplier s ON i.supplier_id = s.supplier_id " +
                    "JOIN employee e ON i.employee_id = e.employee_id " +
                    "LEFT JOIN employee a ON i.approver_id = a.employee_id " +
                    "ORDER BY i.import_ticket_id DESC";

            PreparedStatement ps = c.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ImportTicketDTO dto = new ImportTicketDTO();
                dto.setImportID(rs.getInt("import_ticket_id"));
                dto.setSupplierName(rs.getString("supplier_name"));
                dto.setCreatedDate(rs.getTimestamp("created_date"));

                dto.setEmployeeName(rs.getString("creator_name"));

                dto.setApproverName(rs.getString("approver_name"));

                dto.setTotalPrice(rs.getDouble("total_import_price"));
                dto.setStatus(rs.getInt("status"));
                list.add(dto);
            }
            DatabaseConnection.closeConnection(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int importId, int newStatus, int approverId) {
        boolean result = false;
        try {
            Connection c = DatabaseConnection.getConnection();
            String sql = "UPDATE import_ticket SET status = ?, approver_id = ?, approved_date = NOW() WHERE import_ticket_id = ? AND status = 1";

            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, newStatus);
            ps.setInt(2, approverId);
            ps.setInt(3, importId);

            if (ps.executeUpdate() > 0) result = true;
            DatabaseConnection.closeConnection(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean delete(int importId) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM import_ticket WHERE import_ticket_id = ?")) {
            ps.setInt(1, importId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}