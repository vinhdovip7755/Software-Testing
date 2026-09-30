package com.bookstore.dao;
import com.bookstore.dto.ProductStatsDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductStatsDAO {

    public List<ProductStatsDTO> getThongKeTheoNgay(Date tuNgay, Date denNgay){
        List<ProductStatsDTO> list = new ArrayList<>();

        String sql = """
                SELECT DATE(b.created_date) AS thoi_gian,
                bk.book_id,
                bk.book_name,
                SUM(bd.quantity) AS so_luong
                FROM bill b
                JOIN bill_detail bd ON b.bill_id = bd.bill_id
                JOIN book bk ON bd.book_id = bk.book_id
                WHERE DATE(b.created_date) BETWEEN ? AND ?
                GROUP BY DATE(b.created_date), bk.book_id
                """;

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);

            ResultSet rs = ps.executeQuery();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            while(rs.next()){
                String timeStr = rs.getString("thoi_gian");
                try {
                    timeStr = sdf.format(java.sql.Date.valueOf(timeStr));
                } catch(Exception ignored) {}
                list.add(new ProductStatsDTO(
                        timeStr,
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getInt("so_luong")
                ));
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public List<ProductStatsDTO> getThongKeTheoThang(int year){
        List<ProductStatsDTO> list = new ArrayList<>();

        String sql = """
                SELECT MONTH(b.created_date) AS thoi_gian,
            bk.book_id,
            bk.book_name,
            SUM(bd.quantity) AS so_luong
            FROM bill b
            JOIN bill_detail bd ON b.bill_id = bd.bill_id
            JOIN book bk ON bd.book_id = bk.book_id
            WHERE YEAR(b.created_date) = ?
            GROUP BY MONTH(b.created_date), bk.book_id
            ORDER BY MONTH(b.created_date)
            """;

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, year);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(new ProductStatsDTO(
                        "Tháng " + rs.getInt("thoi_gian"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getInt("so_luong")
                ));
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public List<ProductStatsDTO> getThongKeTheoQuy(int year){
        List<ProductStatsDTO> list = new ArrayList<>();

        String sql = """
            SELECT QUARTER(b.created_date) AS thoi_gian,
            bk.book_id,
            bk.book_name,
            SUM(bd.quantity) AS so_luong
            FROM bill b
            JOIN bill_detail bd ON b.bill_id = bd.bill_id
            JOIN book bk ON bd.book_id = bk.book_id
            WHERE YEAR(b.created_date) = ?
            GROUP BY QUARTER(b.created_date), bk.book_id
            ORDER BY QUARTER(b.created_date)
            """;

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, year);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(new ProductStatsDTO(
                        "Quý " + rs.getInt("thoi_gian"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getInt("so_luong")
                ));
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public List<ProductStatsDTO> getThongKeTheoNam(){
        List<ProductStatsDTO> list = new ArrayList<>();

        String sql = """
            SELECT YEAR(b.created_date) AS thoi_gian,
            bk.book_id,
            bk.book_name,
            SUM(bd.quantity) AS so_luong
            FROM bill b
            JOIN bill_detail bd ON b.bill_id = bd.bill_id
            JOIN book bk ON bd.book_id = bk.book_id
            GROUP BY YEAR(b.created_date), bk.book_id
            ORDER BY YEAR(b.created_date)
            """;

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(new ProductStatsDTO(
                        "Năm " + rs.getInt("thoi_gian"),
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getInt("so_luong")
                ));
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return list;
    }

    public List<ProductStatsDTO> getTop3BanChayTheoQuy(int year, int quarter){
        List<ProductStatsDTO> list = new ArrayList<>();

        String sql = """
                SELECT bk.book_id,
                bk.book_name,
                SUM(bd.quantity) AS so_luong
                FROM bill b
                JOIN bill_detail bd ON b.bill_id = bd.bill_id
                JOIN book bk ON bd.book_id = bk.book_id
                WHERE YEAR(b.created_date) = ?
                AND QUARTER(b.created_date) = ?
                GROUP BY bk.book_id
                ORDER BY so_luong DESC
                LIMIT 3
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){

            ps.setInt(1, year);
            ps.setInt(2,quarter);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(new ProductStatsDTO(
                        "Q" + quarter,
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getInt("so_luong")
                ));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }

    public List<ProductStatsDTO> getTop3BanItTheoQuy(int year, int quarter){
        List<ProductStatsDTO> list = new ArrayList<>();

        String sql = """
                SELECT bk.book_id,
                bk.book_name,
                SUM(bd.quantity) AS so_luong
                FROM bill b
                JOIN bill_detail bd ON b.bill_id = bd.bill_id
                JOIN book bk ON bd.book_id = bk.book_id
                WHERE YEAR(b.created_date) = ?
                AND QUARTER(b.created_date) = ?
                GROUP BY bk.book_id, bk.book_name
                ORDER BY so_luong ASC
                LIMIT 3
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, year);
            ps.setInt(2,quarter);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                list.add(new ProductStatsDTO(
                        "Q" + quarter,
                        rs.getInt("book_id"),
                        rs.getString("book_name"),
                        rs.getInt("so_luong")
                ));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return list;
    }
}
