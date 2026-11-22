package com.bookstore.dao;

import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportDAO {
    public static final double MAX_AMOUNT = 999_999_999_999_999.0;

    private static final String CHECKER_PERMISSION_EXISTS_SQL =
            "AND EXISTS (" +
            "  SELECT 1 FROM employee checker " +
            "  WHERE checker.employee_id = ? " +
            "    AND checker.status = 1 " +
            "    AND EXISTS (" +
            "      SELECT 1 FROM permission p " +
            "      JOIN action a ON p.action_id = a.action_id " +
            "      WHERE p.role_id = checker.role_id " +
            "        AND a.action_code = 'MANAGE_IMPORT_TICKET' " +
            "        AND p.is_action = 1" +
            "    )" +
            ")";

    public int add(ImportTicketDTO importDTO) {
        int generatedId = -1;
        String sql = "INSERT INTO import_ticket (employee_id, supplier_id, total_import_price, status) VALUES (?, ?, ?, 1)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, importDTO.getEmployeeID());
            ps.setInt(2, importDTO.getSupplierID());
            ps.setDouble(3, importDTO.getTotalPrice());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return generatedId;
    }

    public List<ImportTicketDTO> getAll() {
        List<ImportTicketDTO> list = new ArrayList<>();
        String sql = "SELECT i.import_ticket_id, i.supplier_id, s.supplier_name, i.created_date, " +
                "i.employee_id AS creator_employee_id, e.employee_name AS creator_name, i.total_import_price, i.status, " +
                "a.employee_name AS approver_name " +
                "FROM import_ticket i " +
                "JOIN supplier s ON i.supplier_id = s.supplier_id " +
                "JOIN employee e ON i.employee_id = e.employee_id " +
                "LEFT JOIN employee a ON i.approver_id = a.employee_id " +
                "ORDER BY i.import_ticket_id DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ImportTicketDTO getById(int importId) {
        if (importId <= 0) {
            return null;
        }
        String sql = "SELECT i.import_ticket_id, i.supplier_id, s.supplier_name, i.created_date, " +
                "i.employee_id AS creator_employee_id, e.employee_name AS creator_name, i.total_import_price, i.status, " +
                "a.employee_name AS approver_name " +
                "FROM import_ticket i " +
                "JOIN supplier s ON i.supplier_id = s.supplier_id " +
                "JOIN employee e ON i.employee_id = e.employee_id " +
                "LEFT JOIN employee a ON i.approver_id = a.employee_id " +
                "WHERE i.import_ticket_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, importId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTicket(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCreatorEmployeeId(int importId) {
        String sql = "SELECT employee_id FROM import_ticket WHERE import_ticket_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, importId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("employee_id") : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean updateStatus(int importId, int newStatus, int approverId) {
        if (importId <= 0 || approverId <= 0 || (newStatus != 0 && newStatus != 2)) {
            return false;
        }
        String sql = "UPDATE import_ticket SET status = ?, approver_id = ?, approved_date = NOW() " +
                "WHERE import_ticket_id = ? AND status = 1 AND employee_id <> ? " +
                CHECKER_PERMISSION_EXISTS_SQL;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, newStatus);
            ps.setInt(2, approverId);
            ps.setInt(3, importId);
            ps.setInt(4, approverId);
            ps.setInt(5, approverId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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

    public boolean approveImportTransaction(int importId, int approverId, List<ImportDetailDTO> ignoredDetails) {
        return approveImportTransaction(importId, approverId);
    }

    public boolean approveImportTransaction(int importId, int approverId) {
        if (importId <= 0 || approverId <= 0) {
            return false;
        }

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            List<ImportDetailDTO> lockedDetails = new ArrayList<>();
            long totalImportQuantity = 0L;
            String sqlLoadDetails = "SELECT book_id, import_quantity, import_price, cover_price, discount_percent " +
                    "FROM import_ticket_detail WHERE import_ticket_id = ? ORDER BY book_id ASC FOR UPDATE";
            try (PreparedStatement psDetails = c.prepareStatement(sqlLoadDetails)) {
                psDetails.setInt(1, importId);
                try (ResultSet rs = psDetails.executeQuery()) {
                    while (rs.next()) {
                        int qty = rs.getInt("import_quantity");
                        if (qty <= 0) {
                            c.rollback();
                            return false;
                        }
                        totalImportQuantity += qty;
                        if (totalImportQuantity > Integer.MAX_VALUE) {
                            c.rollback();
                            return false;
                        }
                        ImportDetailDTO detail = new ImportDetailDTO();
                        detail.setImportID(importId);
                        detail.setBookID(rs.getInt("book_id"));
                        detail.setQuantity(qty);
                        detail.setPrice(rs.getDouble("import_price"));
                        detail.setCoverPrice(rs.getDouble("cover_price"));
                        detail.setDiscountPercent(rs.getDouble("discount_percent"));
                        lockedDetails.add(detail);
                    }
                }
            }

            if (lockedDetails.isEmpty() || totalImportQuantity <= 0) {
                c.rollback();
                return false;
            }

            String sqlUpdateStatus = "UPDATE import_ticket SET status = 2, approver_id = ?, approved_date = NOW(), total_import_quantity = ? " +
                    "WHERE import_ticket_id = ? AND status = 1 AND employee_id <> ? " +
                    CHECKER_PERMISSION_EXISTS_SQL;
            try (PreparedStatement ps = c.prepareStatement(sqlUpdateStatus)) {
                ps.setInt(1, approverId);
                ps.setInt(2, (int) totalImportQuantity);
                ps.setInt(3, importId);
                ps.setInt(4, approverId);
                ps.setInt(5, approverId);
                if (ps.executeUpdate() == 0) {
                    c.rollback();
                    return false;
                }
            }

            String sqlLockBook = "SELECT quantity FROM book WHERE book_id = ? FOR UPDATE";
            String sqlUpdateBook = "UPDATE book SET quantity = ? WHERE book_id = ?";
            String sqlActiveSellingPrice = "SELECT selling_price FROM price WHERE book_id = ? AND is_active = 1 " +
                    "ORDER BY effective_date DESC, price_id DESC LIMIT 1";
            String sqlInsertLot = "INSERT INTO book_lot (book_id, import_ticket_id, import_date, cover_price, import_price, selling_price, discount_percent, quantity_initial, quantity_remain) " +
                    "VALUES (?, ?, NOW(), ?, ?, ?, ?, ?, ?)";
            String sqlInsertLog = "INSERT INTO inventory_log (action, change_quantity, remain_quantity, reference_id, book_id) " +
                    "VALUES ('Nhập hàng', ?, ?, ?, ?)";

            try (PreparedStatement psLockBook = c.prepareStatement(sqlLockBook);
                 PreparedStatement psUpdateBook = c.prepareStatement(sqlUpdateBook);
                 PreparedStatement psActivePrice = c.prepareStatement(sqlActiveSellingPrice);
                 PreparedStatement psLot = c.prepareStatement(sqlInsertLot);
                 PreparedStatement psLog = c.prepareStatement(sqlInsertLog)) {

                for (ImportDetailDTO d : lockedDetails) {
                    int currentStock;
                    psLockBook.setInt(1, d.getBookID());
                    try (ResultSet rs = psLockBook.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return false;
                        }
                        currentStock = rs.getInt("quantity");
                    }

                    long updatedStockLong = (long) currentStock + d.getQuantity();
                    if (updatedStockLong > Integer.MAX_VALUE) {
                        c.rollback();
                        return false;
                    }
                    int newStock = (int) updatedStockLong;

                    psUpdateBook.setInt(1, newStock);
                    psUpdateBook.setInt(2, d.getBookID());
                    psUpdateBook.executeUpdate();

                    double lotSellingPrice = d.getCoverPrice();
                    psActivePrice.setInt(1, d.getBookID());
                    try (ResultSet rsPrice = psActivePrice.executeQuery()) {
                        if (rsPrice.next()) {
                            double activePrice = rsPrice.getDouble("selling_price");
                            if (Double.isFinite(activePrice) && activePrice > 0) {
                                lotSellingPrice = activePrice;
                            }
                        }
                    }

                    psLot.setInt(1, d.getBookID());
                    psLot.setInt(2, importId);
                    psLot.setDouble(3, d.getCoverPrice());
                    psLot.setDouble(4, d.getPrice());
                    psLot.setDouble(5, lotSellingPrice);
                    psLot.setDouble(6, d.getDiscountPercent());
                    psLot.setInt(7, d.getQuantity());
                    psLot.setInt(8, d.getQuantity());
                    psLot.executeUpdate();

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
            rollbackQuietly(c);
            return false;
        } finally {
            closeTransactionalConnection(c);
        }
    }

    public boolean importBooksTransaction(ImportTicketDTO importDTO, ImportDetailDTO[] details) {
        if (importDTO == null || importDTO.getEmployeeID() <= 0 || importDTO.getSupplierID() <= 0
                || details == null || details.length == 0
                || !Double.isFinite(importDTO.getTotalPrice())
                || importDTO.getTotalPrice() < 0 || importDTO.getTotalPrice() > MAX_AMOUNT) {
            return false;
        }

        long totalQtyLong = 0L;
        Set<Integer> seenBookIds = new HashSet<>();
        for (ImportDetailDTO d : details) {
            if (d == null || d.getBookID() <= 0 || d.getQuantity() <= 0
                    || !seenBookIds.add(d.getBookID())
                    || !Double.isFinite(d.getPrice()) || d.getPrice() < 0 || d.getPrice() > MAX_AMOUNT
                    || !Double.isFinite(d.getCoverPrice()) || d.getCoverPrice() <= 0 || d.getCoverPrice() > MAX_AMOUNT
                    || !Double.isFinite(d.getDiscountPercent()) || d.getDiscountPercent() < 0 || d.getDiscountPercent() > 100) {
                return false;
            }
            totalQtyLong += d.getQuantity();
            if (totalQtyLong > Integer.MAX_VALUE) {
                return false;
            }
        }

        int totalQty = (int) totalQtyLong;
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            String sqlTicket = "INSERT INTO import_ticket (employee_id, supplier_id, total_import_quantity, total_import_price, status) VALUES (?, ?, ?, ?, 1)";
            int newImportID = -1;
            try (PreparedStatement ps = c.prepareStatement(sqlTicket, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, importDTO.getEmployeeID());
                ps.setInt(2, importDTO.getSupplierID());
                ps.setInt(3, totalQty);
                ps.setDouble(4, importDTO.getTotalPrice());
                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            newImportID = rs.getInt(1);
                        }
                    }
                }
            }

            if (newImportID == -1) {
                c.rollback();
                return false;
            }

            String sqlDetail = "INSERT INTO import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price, cover_price, discount_percent) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psD = c.prepareStatement(sqlDetail)) {
                for (ImportDetailDTO d : details) {
                    psD.setInt(1, newImportID);
                    psD.setInt(2, d.getBookID());
                    psD.setInt(3, d.getQuantity());
                    psD.setDouble(4, d.getPrice());
                    psD.setDouble(5, d.getCoverPrice());
                    psD.setDouble(6, d.getDiscountPercent());
                    psD.addBatch();
                }
                psD.executeBatch();
            }

            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            rollbackQuietly(c);
            return false;
        } finally {
            closeTransactionalConnection(c);
        }
    }

    private ImportTicketDTO mapTicket(ResultSet rs) throws SQLException {
        ImportTicketDTO dto = new ImportTicketDTO();
        dto.setImportID(rs.getInt("import_ticket_id"));
        dto.setSupplierID(rs.getInt("supplier_id"));
        dto.setSupplierName(rs.getString("supplier_name"));
        dto.setCreatedDate(rs.getTimestamp("created_date"));
        dto.setEmployeeID(rs.getInt("creator_employee_id"));
        dto.setEmployeeName(rs.getString("creator_name"));
        dto.setApproverName(rs.getString("approver_name"));
        dto.setTotalPrice(rs.getDouble("total_import_price"));
        dto.setStatus(rs.getInt("status"));
        return dto;
    }

    private void rollbackQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeTransactionalConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            } finally {
                DatabaseConnection.closeConnection(conn);
            }
        }
    }
}
