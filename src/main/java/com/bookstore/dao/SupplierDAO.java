package com.bookstore.dao;

import com.bookstore.dto.SupplierDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    public List<SupplierDTO> selectAllSuppliers() {
        List<SupplierDTO> list = new ArrayList<>();
        String sql = "SELECT * FROM supplier";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new SupplierDTO(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("supplier_address"),
                        rs.getString("supplier_phone")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int add(SupplierDTO supplier){
        String sql = "INSERT INTO supplier(supplier_name, supplier_address, supplier_phone) " +
                "VALUES (?, ?, ?)";
        try(Connection c = DatabaseConnection.getConnection();
            PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){

            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getSupplierAddress());
            ps.setString(3, supplier.getSupplierPhone());

            int rowsAffected = ps.executeUpdate();

            if(rowsAffected > 0){
                try(ResultSet generatedKeys = ps.getGeneratedKeys()){
                    if(generatedKeys.next()){
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(SupplierDTO supplier){
        String sql = "UPDATE supplier SET supplier_name = ?, supplier_address = ?, supplier_phone = ? " +
                "WHERE supplier_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)){
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getSupplierAddress());
            ps.setString(3, supplier.getSupplierPhone());
            ps.setInt(4, supplier.getSupplierId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public SupplierDTO getBySupplierId(int supplierId){
        String sql = "SELECT * FROM supplier WHERE supplier_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);){
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    return new SupplierDTO(
                            rs.getInt("supplier_id"),
                            rs.getString("supplier_name"),
                            rs.getString("supplier_address"),
                            rs.getString("supplier_phone")
                    );
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}