package com.bookstore.dao;

import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BillDAO {
    public int createBillTransaction(BillDTO bill, List<BillDetailDTO> details) {
        String sqlBill = "INSERT INTO bill(total_bill_price, tax, employee_id, customer_id, payment_method_id, earned_points) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO bill_detail (bill_id, book_id, quantity, unit_price, lot_id) VALUES (?, ?, ?, ?, ?)";
        String sqlCheckStock = "SELECT quantity FROM book WHERE book_id = ? FOR UPDATE";
        String sqlUpdateStock = "UPDATE book SET quantity = quantity - ? WHERE book_id = ?";
        String sqlLog = "INSERT INTO inventory_log (action, change_quantity, remain_quantity, reference_id, book_id) VALUES (?, ?, ?, ?, ?)";
        String sqlFindLots = "SELECT lot_id, quantity_remain FROM book_lot WHERE book_id = ? AND quantity_remain > 0 ORDER BY import_date ASC FOR UPDATE";
        String sqlUpdateLot = "UPDATE book_lot SET quantity_remain = quantity_remain - ? WHERE lot_id = ?";

        Connection c = null;
        PreparedStatement psBill = null;
        PreparedStatement psDetail = null;
        PreparedStatement psCheck = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psLog = null;
        PreparedStatement psFindLots = null;
        PreparedStatement psUpdateLot = null;
        ResultSet rs = null;
        ResultSet rsStock = null;
        ResultSet rsLots = null;

        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            psBill = c.prepareStatement(sqlBill, Statement.RETURN_GENERATED_KEYS);
            psBill.setDouble(1, bill.getTotalBillPrice());
            psBill.setDouble(2, bill.getTax());
            psBill.setInt(3, bill.getEmployeeId());
            if (bill.getCustomerId() > 0) {
                psBill.setInt(4, bill.getCustomerId());
            } else {
                psBill.setNull(4, Types.INTEGER);
            }
            psBill.setInt(5, bill.getPaymentMethodId());
            psBill.setInt(6, bill.getEarnedPoints());
            psBill.executeUpdate();

            int generatedBillId = -1;
            rs = psBill.getGeneratedKeys();
            if (rs.next()) {
                generatedBillId = rs.getInt(1);
            }

            if (generatedBillId > 0) {
                psDetail = c.prepareStatement(sqlDetail);
                psCheck = c.prepareStatement(sqlCheckStock);
                psUpdate = c.prepareStatement(sqlUpdateStock);
                psLog = c.prepareStatement(sqlLog);
                psFindLots = c.prepareStatement(sqlFindLots);
                psUpdateLot = c.prepareStatement(sqlUpdateLot);

                for (BillDetailDTO detail : details) {
                    psCheck.setInt(1, detail.getBookId());
                    rsStock = psCheck.executeQuery();
                    int currentStock = 0;
                    if (rsStock.next()) {
                        currentStock = rsStock.getInt("quantity");
                    }
                    rsStock.close();

                    if (currentStock < detail.getQuantity()) {
                        throw new SQLException("Sách ID " + detail.getBookId() + " không đủ tồn kho! (Còn: " + currentStock + ", Khách mua: " + detail.getQuantity() + ")");
                    }

                    int requiredQty = detail.getQuantity();

                    psFindLots.setInt(1, detail.getBookId());
                    rsLots = psFindLots.executeQuery();

                    while (rsLots.next() && requiredQty > 0) {
                        int lotId = rsLots.getInt("lot_id");
                        int remainInLot = rsLots.getInt("quantity_remain");

                        int deductQty = Math.min(requiredQty, remainInLot);

                        psUpdateLot.setInt(1, deductQty);
                        psUpdateLot.setInt(2, lotId);
                        psUpdateLot.executeUpdate();

                        psDetail.setInt(1, generatedBillId);
                        psDetail.setInt(2, detail.getBookId());
                        psDetail.setInt(3, deductQty);
                        psDetail.setDouble(4, detail.getUnitPrice());
                        psDetail.setInt(5, lotId);
                        psDetail.executeUpdate();

                        requiredQty -= deductQty;
                    }
                    rsLots.close();

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

                if (bill.getCustomerId() > 0 && bill.getEarnedPoints() > 0) {
                    String sqlUpdatePoint = "UPDATE customer SET point = point + ? WHERE customer_id = ?";
                    try (PreparedStatement psUpdatePoint = c.prepareStatement(sqlUpdatePoint)) {
                        psUpdatePoint.setInt(1, bill.getEarnedPoints());
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
                        ResultSet rsRank = psCheckRank.executeQuery();
                        if (rsRank.next()) {
                            currentPoint = rsRank.getInt("point");
                            currentRankId = rsRank.getInt("rank_id");
                        }
                        rsRank.close();
                    }

                    String sqlFindNewRank = "SELECT rank_id FROM membership_rank WHERE min_point <= ? ORDER BY min_point DESC LIMIT 1";
                    int newRankId = currentRankId;

                    try (PreparedStatement psFindRank = c.prepareStatement(sqlFindNewRank)) {
                        psFindRank.setInt(1, currentPoint);
                        ResultSet rsNewRank = psFindRank.executeQuery();
                        if (rsNewRank.next()) {
                            newRankId = rsNewRank.getInt("rank_id");
                        }
                        rsNewRank.close();
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
            }

            c.commit();
            return generatedBillId;

        } catch (SQLException e) {
            e.printStackTrace();
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return 0;
        } finally {
            try { if (rsLots != null) rsLots.close(); } catch (Exception e) {}
            try { if (rsStock != null) rsStock.close(); } catch (Exception e) {}
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (psLog != null) psLog.close(); } catch (Exception e) {}
            try { if (psUpdate != null) psUpdate.close(); } catch (Exception e) {}
            try { if (psCheck != null) psCheck.close(); } catch (Exception e) {}
            try { if (psDetail != null) psDetail.close(); } catch (Exception e) {}
            try { if (psFindLots != null) psFindLots.close(); } catch (Exception e) {}
            try { if (psUpdateLot != null) psUpdateLot.close(); } catch (Exception e) {}
            try { if (psBill != null) psBill.close(); } catch (Exception e) {}
            try { if (c != null) { c.setAutoCommit(true); c.close(); } } catch (Exception e) {}
        }
    }

    public List<BillDTO> getAllBills() {
        List<BillDTO> list = new ArrayList<>();
        String sql = "SELECT *, employee_name, customer_name, payment_method_name " +
                "FROM bill b " +
                "JOIN employee e ON e.employee_id = b.employee_id " +
                "LEFT JOIN customer c ON c.customer_id = b.customer_id " +
                "JOIN payment_method pm ON pm.payment_method_id = b.payment_method_id";

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
        List<BillDetailDTO> list = new ArrayList<>();
        String sql = "SELECT bd.bill_id, bd.book_id, b.book_name, bd.quantity, bd.unit_price " +
                "FROM bill_detail bd " +
                "JOIN book b ON bd.book_id = b.book_id " +
                "WHERE bd.bill_id = ? " +
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
}