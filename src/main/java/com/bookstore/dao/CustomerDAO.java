package com.bookstore.dao;

import com.bookstore.dto.CustomerDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    public CustomerDTO selectByPhone(String phone) {
        CustomerDTO customer = null;
        String sql = "SELECT c.customer_id, c.customer_name, c.customer_phone, c.point, c.rank_id, m.rank_name " +
                "FROM customer c " +
                "JOIN membership_rank m ON c.rank_id = m.rank_id " +
                "WHERE customer_phone = ?";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    customer = new CustomerDTO(
                            rs.getInt("customer_id"),
                            rs.getString("customer_name"),
                            rs.getString("customer_phone"),
                            rs.getInt("point"),
                            rs.getInt("rank_id"),
                            rs.getString("rank_name")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return customer;
    }

    public List<CustomerDTO> selectAllCustomers() {
        List<CustomerDTO> list = new ArrayList<>();
        String sql = "SELECT c.customer_id, c.customer_name, c.customer_phone, c.point, c.rank_id, m.rank_name " +
                "FROM customer c " +
                "JOIN membership_rank m ON c.rank_id = m.rank_id " +
                "ORDER BY c.customer_id DESC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CustomerDTO customer = new CustomerDTO(
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("customer_phone"),
                        rs.getInt("point"),
                        rs.getInt("rank_id"),
                        rs.getString("rank_name")
                );
                list.add(customer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateCustomerInfo(CustomerDTO c) {
        String sql = "UPDATE customer SET customer_name = ?, customer_phone = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerName());
            ps.setString(2, c.getCustomerPhone());
            ps.setInt(3, c.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPhoneExist(String phone, int ignoreId) {
        String sql = "SELECT COUNT(*) FROM customer WHERE customer_phone = ? AND customer_id != ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setInt(2, ignoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isNameExist(String name, int ignoreId) {
        String sql = "SELECT COUNT(*) FROM customer WHERE customer_name = ? AND customer_id != ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, ignoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object[] getCustomerStatistics(int customerId) {
        Object[] stats = new Object[]{0, 0.0, null};
        String sql = "SELECT " +
                "(SELECT SUM(bd.quantity) FROM bill_detail bd JOIN bill b ON bd.bill_id = b.bill_id WHERE b.customer_id = ?) AS total_books, " +
                "(SELECT SUM(total_bill_price) FROM bill WHERE customer_id = ?) AS total_spent, " +
                "(SELECT MAX(created_date) FROM bill WHERE customer_id = ?) AS last_purchase_date";

        try (java.sql.Connection c = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            ps.setInt(3, customerId);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stats[0] = rs.getInt("total_books");
                    stats[1] = rs.getDouble("total_spent");
                    stats[2] = rs.getTimestamp("last_purchase_date");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    public boolean insertCustomer(CustomerDTO c) {
        String sql = "INSERT INTO customer (customer_name, customer_phone) VALUES (?, ?)";
        try (java.sql.Connection conn = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getCustomerName());
            ps.setString(2, c.getCustomerPhone());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
