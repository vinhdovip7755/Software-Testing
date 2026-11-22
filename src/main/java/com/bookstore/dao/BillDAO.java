package com.bookstore.dao;

import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BillDAO {
    public static final double MAX_AMOUNT = 999_999_999_999_999.0;
    private static volatile boolean billDetailBookNameEnsured = false;

    private static void ensureBillDetailBookNameColumn() {
        if (billDetailBookNameEnsured) {
            return;
        }
        synchronized (BillDAO.class) {
            if (billDetailBookNameEnsured) {
                return;
            }
            try (Connection conn = DatabaseConnection.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                boolean exists = false;
                try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, "bill_detail", "book_name")) {
                    exists = rs.next();
                }

                try (Statement stmt = conn.createStatement()) {
                    if (!exists) {
                        try {
                            stmt.executeUpdate("ALTER TABLE bill_detail ADD COLUMN book_name VARCHAR(255) NULL AFTER book_id");
                        } catch (SQLException ex) {
                            if (ex.getErrorCode() != 1060) {
                                throw ex;
                            }
                        }
                    }
                    stmt.executeUpdate("UPDATE bill_detail bd JOIN book b ON bd.book_id = b.book_id "
                            + "SET bd.book_name = b.book_name WHERE bd.book_name IS NULL");
                }
                billDetailBookNameEnsured = true;
            } catch (SQLException e) {
                throw new IllegalStateException("Không thể bảo đảm tên sách lịch sử trong chi tiết hóa đơn.", e);
            }
        }
    }

    public int createBillTransaction(BillDTO bill, List<BillDetailDTO> details) {
        if (!isValidBillPayload(bill, details)) {
            return -1;
        }
        ensureBillDetailBookNameColumn();

        Map<Integer, BillDetailDTO> consolidated = new TreeMap<>();
        for (BillDetailDTO d : details) {
            if (consolidated.containsKey(d.getBookId())) {
                BillDetailDTO exist = consolidated.get(d.getBookId());
                long newQtyLong = (long) exist.getQuantity() + d.getQuantity();
                if (newQtyLong > Integer.MAX_VALUE) {
                    return -1;
                }
                int newQty = (int) newQtyLong;
                double totalCost = exist.getQuantity() * exist.getUnitPrice() + d.getQuantity() * d.getUnitPrice();
                if (!Double.isFinite(totalCost)) {
                    return -1;
                }
                exist.setQuantity(newQty);
                exist.setUnitPrice(totalCost / newQty);
                if ((exist.getBookName() == null || exist.getBookName().isEmpty()) && d.getBookName() != null) {
                    exist.setBookName(d.getBookName());
                }
            } else {
                consolidated.put(d.getBookId(), new BillDetailDTO(d.getBillId(), d.getBookId(), d.getBookName(), d.getQuantity(), d.getUnitPrice()));
            }
        }

        int effectiveEarnedPoints = bill.getCustomerId() > 0 ? bill.getEarnedPoints() : 0;

        String sqlBill = "INSERT INTO bill(total_bill_price, tax, employee_id, customer_id, payment_method_id, earned_points) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO bill_detail (bill_id, book_id, book_name, quantity, unit_price, lot_id) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCheckStock = "SELECT quantity, book_name FROM book WHERE book_id = ? AND status = 1 FOR UPDATE";
        String sqlUpdateStock = "UPDATE book SET quantity = quantity - ? WHERE book_id = ?";
        String sqlLog = "INSERT INTO inventory_log (action, change_quantity, remain_quantity, reference_id, book_id) VALUES (?, ?, ?, ?, ?)";
        String sqlFindLots = "SELECT lot_id, quantity_remain FROM book_lot WHERE book_id = ? AND quantity_remain > 0 ORDER BY import_date ASC, lot_id ASC FOR UPDATE";
        String sqlUpdateLot = "UPDATE book_lot SET quantity_remain = quantity_remain - ? WHERE lot_id = ?";

        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            int generatedBillId = -1;
            try (PreparedStatement psBill = c.prepareStatement(sqlBill, Statement.RETURN_GENERATED_KEYS)) {
                psBill.setDouble(1, bill.getTotalBillPrice());
                psBill.setDouble(2, bill.getTax());
                psBill.setInt(3, bill.getEmployeeId());
                if (bill.getCustomerId() > 0) {
                    psBill.setInt(4, bill.getCustomerId());
                } else {
                    psBill.setNull(4, Types.INTEGER);
                }
                psBill.setInt(5, bill.getPaymentMethodId());
                psBill.setInt(6, effectiveEarnedPoints);
                psBill.executeUpdate();

                try (ResultSet rs = psBill.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedBillId = rs.getInt(1);
                    }
                }
            }

            if (generatedBillId <= 0) {
                c.rollback();
                return 0;
            }

            try (PreparedStatement psDetail = c.prepareStatement(sqlDetail);
                 PreparedStatement psCheck = c.prepareStatement(sqlCheckStock);
                 PreparedStatement psUpdate = c.prepareStatement(sqlUpdateStock);
                 PreparedStatement psLog = c.prepareStatement(sqlLog);
                 PreparedStatement psFindLots = c.prepareStatement(sqlFindLots);
                 PreparedStatement psUpdateLot = c.prepareStatement(sqlUpdateLot)) {

                for (BillDetailDTO detail : consolidated.values()) {
                    int currentStock;
                    String bookNameFromDb;
                    psCheck.setInt(1, detail.getBookId());
                    try (ResultSet rsStock = psCheck.executeQuery()) {
                        if (!rsStock.next()) {
                            throw new SQLException("Sách ID " + detail.getBookId() + " không tồn tại hoặc đã ngừng bán!");
                        }
                        currentStock = rsStock.getInt("quantity");
                        bookNameFromDb = rsStock.getString("book_name");
                    }

                    String finalBookName = (detail.getBookName() != null && !detail.getBookName().trim().isEmpty())
                            ? detail.getBookName()
                            : (bookNameFromDb != null ? bookNameFromDb : "");

                    if (currentStock < detail.getQuantity()) {
                        throw new SQLException("Sách ID " + detail.getBookId() + " không đủ tồn kho! (Còn: " + currentStock + ", Khách mua: " + detail.getQuantity() + ")");
                    }

                    int requiredQty = detail.getQuantity();
                    psFindLots.setInt(1, detail.getBookId());
                    try (ResultSet rsLots = psFindLots.executeQuery()) {
                        while (rsLots.next() && requiredQty > 0) {
                            int lotId = rsLots.getInt("lot_id");
                            int remainInLot = rsLots.getInt("quantity_remain");
                            int deductQty = Math.min(requiredQty, remainInLot);

                            psUpdateLot.setInt(1, deductQty);
                            psUpdateLot.setInt(2, lotId);
                            psUpdateLot.executeUpdate();

                            psDetail.setInt(1, generatedBillId);
                            psDetail.setInt(2, detail.getBookId());
                            psDetail.setString(3, finalBookName);
                            psDetail.setInt(4, deductQty);
                            psDetail.setDouble(5, detail.getUnitPrice());
                            psDetail.setInt(6, lotId);
                            psDetail.executeUpdate();

                            requiredQty -= deductQty;
                        }
                    }

                    if (requiredQty > 0) {
                        throw new SQLException("Sách ID " + detail.getBookId() + " không đủ tồn kho trong lô!");
                    }

                    int remainQuantity = currentStock - detail.getQuantity();

                    psUpdate.setInt(1, detail.getQuantity());
                    psUpdate.setInt(2, detail.getBookId());
                    psUpdate.executeUpdate();

                    psLog.setString(1, "Bán hàng");
                    psLog.setInt(2, -detail.getQuantity());
                    psLog.setInt(3, remainQuantity);
                    psLog.setInt(4, generatedBillId);
                    psLog.setInt(5, detail.getBookId());
                    psLog.executeUpdate();
                }
            }

            if (bill.getCustomerId() > 0 && effectiveEarnedPoints > 0) {
                String sqlUpdatePoint = "UPDATE customer SET point = point + ? WHERE customer_id = ?";
                try (PreparedStatement psUpdatePoint = c.prepareStatement(sqlUpdatePoint)) {
                    psUpdatePoint.setInt(1, effectiveEarnedPoints);
                    psUpdatePoint.setInt(2, bill.getCustomerId());
                    psUpdatePoint.executeUpdate();
                }

                String sqlCheckRank = "SELECT c.point, c.rank_id, r.min_point " +
                        "FROM customer c JOIN membership_rank r ON c.rank_id = r.rank_id " +
                        "WHERE c.customer_id = ?";
                int currentPoint = 0;
                int currentRankId = 0;

                try (PreparedStatement psCheckRank = c.prepareStatement(sqlCheckRank)) {
                    psCheckRank.setInt(1, bill.getCustomerId());
                    try (ResultSet rsRank = psCheckRank.executeQuery()) {
                        if (rsRank.next()) {
                            currentPoint = rsRank.getInt("point");
                            currentRankId = rsRank.getInt("rank_id");
                        }
                    }
                }

                String sqlFindNewRank = "SELECT rank_id FROM membership_rank WHERE min_point <= ? ORDER BY min_point DESC LIMIT 1";
                int newRankId = currentRankId;

                try (PreparedStatement psFindRank = c.prepareStatement(sqlFindNewRank)) {
                    psFindRank.setInt(1, currentPoint);
                    try (ResultSet rsNewRank = psFindRank.executeQuery()) {
                        if (rsNewRank.next()) {
                            newRankId = rsNewRank.getInt("rank_id");
                        }
                    }
                }

                if (newRankId != currentRankId) {
                    String sqlUpdateRank = "UPDATE customer SET rank_id = ? WHERE customer_id = ?";
                    try (PreparedStatement psUpdateRank = c.prepareStatement(sqlUpdateRank)) {
                        psUpdateRank.setInt(1, newRankId);
                        psUpdateRank.setInt(2, bill.getCustomerId());
                        psUpdateRank.executeUpdate();
                    }
                }
            }

            c.commit();
            return generatedBillId;

        } catch (SQLException e) {
            e.printStackTrace();
            rollbackQuietly(c);
            return 0;
        } finally {
            closeTransactionalConnection(c);
        }
    }

    public List<BillDTO> getAllBills() {
        List<BillDTO> list = new ArrayList<>();
        String sql = "SELECT b.*, employee_name, customer_name, payment_method_name " +
                "FROM bill b " +
                "JOIN employee e ON e.employee_id = b.employee_id " +
                "LEFT JOIN customer c ON c.customer_id = b.customer_id " +
                "JOIN payment_method pm ON pm.payment_method_id = b.payment_method_id " +
                "ORDER BY b.created_date DESC, b.bill_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                BillDTO bill = new BillDTO(
                        rs.getInt("bill_id"),
                        rs.getTimestamp("created_date"),
                        rs.getDouble("total_bill_price"),
                        rs.getDouble("tax"),
                        rs.getInt("employee_id"),
                        rs.getString("employee_name"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getInt("payment_method_id"),
                        rs.getString("payment_method_name"),
                        rs.getInt("earned_points")
                );
                list.add(bill);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public BillDTO getBillById(int billId) {
        String sql = "SELECT b.*, employee_name, customer_name, payment_method_name " +
                "FROM bill b " +
                "JOIN employee e ON e.employee_id = b.employee_id " +
                "LEFT JOIN customer c ON c.customer_id = b.customer_id " +
                "JOIN payment_method pm ON pm.payment_method_id = b.payment_method_id " +
                "WHERE b.bill_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new BillDTO(
                            rs.getInt("bill_id"),
                            rs.getTimestamp("created_date"),
                            rs.getDouble("total_bill_price"),
                            rs.getDouble("tax"),
                            rs.getInt("employee_id"),
                            rs.getString("employee_name"),
                            rs.getInt("customer_id"),
                            rs.getString("customer_name"),
                            rs.getInt("payment_method_id"),
                            rs.getString("payment_method_name"),
                            rs.getInt("earned_points")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<BillDetailDTO> getBillDetailsByBillId(int billId) {
        ensureBillDetailBookNameColumn();
        List<BillDetailDTO> list = new ArrayList<>();
        String sql = "SELECT bd.bill_id, bd.book_id, bd.book_name, SUM(bd.quantity) as quantity, bd.unit_price " +
                "FROM bill_detail bd " +
                "WHERE bd.bill_id = ? " +
                "GROUP BY bd.bill_id, bd.book_id, bd.book_name, bd.unit_price " +
                "ORDER BY bd.book_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BillDetailDTO(
                            rs.getInt("bill_id"),
                            rs.getInt("book_id"),
                            rs.getString("book_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private boolean isValidBillPayload(BillDTO bill, List<BillDetailDTO> details) {
        if (bill == null || details == null || details.isEmpty()) {
            return false;
        }
        if (bill.getEmployeeId() <= 0 || bill.getPaymentMethodId() <= 0 || bill.getCustomerId() < 0) {
            return false;
        }
        if (!Double.isFinite(bill.getTotalBillPrice()) || bill.getTotalBillPrice() < 0 || bill.getTotalBillPrice() > MAX_AMOUNT) {
            return false;
        }
        if (!Double.isFinite(bill.getTax()) || bill.getTax() < 0 || bill.getTax() > 1) {
            return false;
        }
        if (bill.getEarnedPoints() < 0) {
            return false;
        }
        for (BillDetailDTO detail : details) {
            if (detail == null || detail.getBookId() <= 0 || detail.getQuantity() <= 0) {
                return false;
            }
            if (!Double.isFinite(detail.getUnitPrice()) || detail.getUnitPrice() < 0 || detail.getUnitPrice() > MAX_AMOUNT) {
                return false;
            }
        }
        return true;
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
