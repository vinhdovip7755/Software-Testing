package com.bookstore.dao;

import com.bookstore.dto.FinancialStatsDTO;
import com.bookstore.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FinancialStatsDAO {

    public FinancialStatsDTO getThongKeTong() {
        String sql = "SELECT SUM(x.doanh_thu) AS doanh_thu, SUM(x.chi_phi) AS chi_phi, SUM(x.von_nhap_hang) AS von_nhap_hang " +
                "FROM (" +
                "  SELECT SUM(total_bill_price / (1 + tax)) AS doanh_thu, 0 AS chi_phi, 0 AS von_nhap_hang FROM bill " +
                "  UNION ALL " +
                "  SELECT 0 AS doanh_thu, SUM(bd.quantity * bl.import_price) AS chi_phi, 0 AS von_nhap_hang " +
                "  FROM bill b " +
                "  JOIN bill_detail bd ON bd.bill_id = b.bill_id " +
                "  JOIN book_lot bl ON bl.lot_id = bd.lot_id " +
                "  UNION ALL " +
                "  SELECT 0 AS doanh_thu, 0 AS chi_phi, SUM(d.import_quantity * d.import_price) AS von_nhap_hang " +
                "  FROM import_ticket i " +
                "  JOIN import_ticket_detail d ON d.import_ticket_id = i.import_ticket_id " +
                "  WHERE i.status = 2 AND i.approved_date IS NOT NULL " +
                ") x";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                double doanhThu = rs.getDouble("doanh_thu");
                double chiPhi = rs.getDouble("chi_phi");
                double von = rs.getDouble("von_nhap_hang");
                return new FinancialStatsDTO("Tổng", doanhThu, chiPhi, von);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new FinancialStatsDTO("Tổng", 0, 0, 0);
    }

    public List<FinancialStatsDTO> getThongKeTheoNgay(Date tuNgay, Date denNgay) {
        List<FinancialStatsDTO> list = new ArrayList<>();
        String sql = "SELECT x.thoi_gian, SUM(x.doanh_thu) AS doanh_thu, SUM(x.chi_phi) AS chi_phi, SUM(x.von_nhap_hang) AS von_nhap_hang " +
                "FROM (" +
                "  SELECT DATE(created_date) AS thoi_gian, " +
                "         SUM(total_bill_price / (1 + tax)) AS doanh_thu, " +
                "         0 AS chi_phi, 0 AS von_nhap_hang " +
                "  FROM bill " +
                "  WHERE DATE(created_date) BETWEEN ? AND ? " +
                "  GROUP BY DATE(created_date) " +
                "  UNION ALL " +
                "  SELECT DATE(b.created_date) AS thoi_gian, " +
                "         0 AS doanh_thu, " +
                "         SUM(bd.quantity * bl.import_price) AS chi_phi, " +
                "         0 AS von_nhap_hang " +
                "  FROM bill b " +
                "  JOIN bill_detail bd ON bd.bill_id = b.bill_id " +
                "  JOIN book_lot bl ON bl.lot_id = bd.lot_id " +
                "  WHERE DATE(b.created_date) BETWEEN ? AND ? " +
                "  GROUP BY DATE(b.created_date) " +
                "  UNION ALL " +
                "  SELECT DATE(i.approved_date) AS thoi_gian, 0 AS doanh_thu, 0 AS chi_phi, " +
                "         SUM(d.import_quantity * d.import_price) AS von_nhap_hang " +
                "  FROM import_ticket i " +
                "  JOIN import_ticket_detail d ON d.import_ticket_id = i.import_ticket_id " +
                "  WHERE i.status = 2 AND i.approved_date IS NOT NULL AND DATE(i.approved_date) BETWEEN ? AND ? " +
                "  GROUP BY DATE(i.approved_date) " +
                ") x " +
                "GROUP BY x.thoi_gian " +
                "ORDER BY x.thoi_gian";

        SimpleDateFormat viewFormat = new SimpleDateFormat("dd/MM/yyyy");

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            java.sql.Date from = new java.sql.Date(tuNgay.getTime());
            java.sql.Date to = new java.sql.Date(denNgay.getTime());
            ps.setDate(1, from);
            ps.setDate(2, to);
            ps.setDate(3, from);
            ps.setDate(4, to);
            ps.setDate(5, from);
            ps.setDate(6, to);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String time = rs.getString("thoi_gian");
                    try {
                        time = viewFormat.format(java.sql.Date.valueOf(time));
                    } catch(Exception e) {}
                    double doanhThu = rs.getDouble("doanh_thu");
                    double chiPhi = rs.getDouble("chi_phi");
                    double von = rs.getDouble("von_nhap_hang");
                    list.add(new FinancialStatsDTO(time, doanhThu, chiPhi, von));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public List<FinancialStatsDTO> getThongKeTheoThang(int nam) {
        List<FinancialStatsDTO> list = new ArrayList<>();
        String sql = "SELECT x.thang, SUM(x.doanh_thu) AS doanh_thu, SUM(x.chi_phi) AS chi_phi, SUM(x.von_nhap_hang) AS von_nhap_hang " +
                "FROM (" +
                "  SELECT MONTH(created_date) AS thang, " +
                "         SUM(total_bill_price / (1 + tax)) AS doanh_thu, " +
                "         0 AS chi_phi, 0 AS von_nhap_hang " +
                "  FROM bill " +
                "  WHERE YEAR(created_date) = ? " +
                "  GROUP BY MONTH(created_date) " +
                "  UNION ALL " +
                "  SELECT MONTH(b.created_date) AS thang, " +
                "         0 AS doanh_thu, " +
                "         SUM(bd.quantity * bl.import_price) AS chi_phi, " +
                "         0 AS von_nhap_hang " +
                "  FROM bill b " +
                "  JOIN bill_detail bd ON bd.bill_id = b.bill_id " +
                "  JOIN book_lot bl ON bl.lot_id = bd.lot_id " +
                "  WHERE YEAR(b.created_date) = ? " +
                "  GROUP BY MONTH(b.created_date) " +
                "  UNION ALL " +
                "  SELECT MONTH(i.approved_date) AS thang, 0 AS doanh_thu, 0 AS chi_phi, " +
                "         SUM(d.import_quantity * d.import_price) AS von_nhap_hang " +
                "  FROM import_ticket i " +
                "  JOIN import_ticket_detail d ON d.import_ticket_id = i.import_ticket_id " +
                "  WHERE i.status = 2 AND i.approved_date IS NOT NULL AND YEAR(i.approved_date) = ? " +
                "  GROUP BY MONTH(i.approved_date) " +
                ") x " +
                "GROUP BY x.thang " +
                "ORDER BY x.thang";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ps.setInt(2, nam);
            ps.setInt(3, nam);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int thang = rs.getInt("thang");
                    double doanhThu = rs.getDouble("doanh_thu");
                    double chiPhi = rs.getDouble("chi_phi");
                    double von = rs.getDouble("von_nhap_hang");
                    list.add(new FinancialStatsDTO("Tháng " + thang, doanhThu, chiPhi, von));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
