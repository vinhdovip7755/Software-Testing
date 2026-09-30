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

    public boolean approveImportTransaction(int importId, int approverId, java.util.List<com.bookstore.dto.ImportDetailDTO> details) {
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);
            
            String sqlUpdateStatus = "UPDATE import_ticket SET status = 2, approver_id = ?, approved_date = NOW(), total_import_quantity = (SELECT COALESCE(SUM(import_quantity), 0) FROM import_ticket_detail WHERE import_ticket_id = ?) WHERE import_ticket_id = ? AND status = 1";
            try (PreparedStatement ps = c.prepareStatement(sqlUpdateStatus)) {
                ps.setInt(1, approverId);
                ps.setInt(2, importId);
                ps.setInt(3, importId);
                if (ps.executeUpdate() == 0) {
                    c.rollback();
                    return false;
                }
            }

            String sqlUpdateBook = "UPDATE book SET quantity = quantity + ? WHERE book_id = ?";
            String sqlInsertLot = "INSERT INTO book_lot (book_id, import_ticket_id, import_date, cover_price, import_price, selling_price, discount_percent, quantity_initial, quantity_remain) VALUES (?, ?, NOW(), ?, ?, ?, ?, ?, ?)";
            String sqlInsertLog = "INSERT INTO inventory_log (action, change_quantity, remain_quantity, reference_id, book_id) VALUES ('Nhập hàng', ?, ?, ?, ?)";
            String sqlCheckStock = "SELECT quantity FROM book WHERE book_id = ?";

            for (com.bookstore.dto.ImportDetailDTO d : details) {
                try (PreparedStatement psUpdateBook = c.prepareStatement(sqlUpdateBook)) {
                    psUpdateBook.setInt(1, d.getQuantity());
                    psUpdateBook.setInt(2, d.getBookID());
                    psUpdateBook.executeUpdate();
                }

                int newStock = 0;
                try (PreparedStatement psCheck = c.prepareStatement(sqlCheckStock)) {
                    psCheck.setInt(1, d.getBookID());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            newStock = rs.getInt("quantity");
                        }
                    }
                }

                try (PreparedStatement psLot = c.prepareStatement(sqlInsertLot)) {
                    psLot.setInt(1, d.getBookID());
                    psLot.setInt(2, importId);
                    psLot.setDouble(3, d.getCoverPrice());
                    psLot.setDouble(4, d.getPrice());
                    psLot.setDouble(5, d.getCoverPrice());
                    psLot.setDouble(6, d.getDiscountPercent());
                    psLot.setInt(7, d.getQuantity());
                    psLot.setInt(8, d.getQuantity());
                    psLot.executeUpdate();
                }

                try (PreparedStatement psLog = c.prepareStatement(sqlInsertLog)) {
                    psLog.setInt(1, d.getQuantity());
                    psLog.setInt(2, newStock);
                    psLog.setInt(3, importId);
                    psLog.setInt(4, d.getBookID());
                    psLog.executeUpdate();
                }
            }
            
            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                try { c.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); c.close(); } catch (SQLException ex) {}
            }
        }
    }

    public boolean importBooksTransaction(com.bookstore.dto.ImportTicketDTO importDTO, com.bookstore.dto.ImportDetailDTO[] details) {
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            int totalQty = 0;
            if (details != null) {
                for (com.bookstore.dto.ImportDetailDTO d : details) {
                    if (d != null) {
                        totalQty += d.getQuantity();
                    }
                }
            }

            String sqlTicket = "INSERT INTO import_ticket (employee_id, supplier_id, total_import_quantity, total_import_price, status) VALUES (?, ?, ?, ?, 1)";
            int newImportID = -1;
            try (PreparedStatement ps = c.prepareStatement(sqlTicket, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, importDTO.getEmployeeID());
                ps.setInt(2, importDTO.getSupplierID());
                ps.setInt(3, totalQty);
                ps.setDouble(4, importDTO.getTotalPrice());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) newImportID = rs.getInt(1);
                    }
                }
            }

            if (newImportID == -1) {
                c.rollback();
                return false;
            }

            String sqlDetail = "INSERT INTO import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price, cover_price, discount_percent) VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE import_quantity = import_quantity + VALUES(import_quantity)";
            try (PreparedStatement psD = c.prepareStatement(sqlDetail)) {
                for (com.bookstore.dto.ImportDetailDTO d : details) {
                    if (d != null) {
                        psD.setInt(1, newImportID);
                        psD.setInt(2, d.getBookID());
                        psD.setInt(3, d.getQuantity());
                        psD.setDouble(4, d.getPrice());
                        psD.setDouble(5, d.getCoverPrice());
                        psD.setDouble(6, d.getDiscountPercent());
                        psD.addBatch();
                    }
                }
                psD.executeBatch();
            }

            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                try { c.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); c.close(); } catch (SQLException ex) {}
            }
        }
    }
}