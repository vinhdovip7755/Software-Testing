package com.bookstore.dao;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    public EmployeeDTO selectById(int id) {
        EmployeeDTO employee = null;

        try {
            Connection c = DatabaseConnection.getConnection();
            String sql = "SELECT e.*, r.role_name " +
                    "FROM employee e " +
                    "JOIN role r ON r.role_id = e.role_id " +
                    "WHERE employee_id = ? ";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                employee = new EmployeeDTO(
                        rs.getInt("employee_id"),
                        rs.getString("employee_name"),
                        rs.getString("employee_phone"),
                        rs.getDate("birthday"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("salary_factor"),
                        rs.getDate("day_in"),
                        rs.getInt("status"),
                        rs.getInt("role_id"),
                        rs.getString("r.role_name")
                );
            }
            DatabaseConnection.closeConnection(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employee;
    }

    public List<EmployeeDTO> selectAllEmployees() {
        List<EmployeeDTO> list = new ArrayList<>();
        String sql = "SELECT e.*, r.role_name FROM employee e JOIN role r ON e.role_id = r.role_id WHERE e.employee_name NOT LIKE '%Admin%'";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO(
                        rs.getInt("employee_id"),
                        rs.getString("employee_name"),
                        rs.getString("employee_phone"),
                        rs.getDate("birthday"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("salary_factor"),
                        rs.getDate("day_in"),
                        rs.getInt("status"),
                        rs.getInt("role_id"),
                        rs.getString("role_name")
                );
                list.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateEmployee(EmployeeDTO emp) {
        Connection c = null;
        PreparedStatement psEmp = null;
        PreparedStatement psAcc = null;

        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            String sqlEmp = "UPDATE employee SET employee_name = ?, employee_phone = ?, birthday = ?, " +
                    "base_salary = ?, role_id = ?, status = ? WHERE employee_id = ?";
            psEmp = c.prepareStatement(sqlEmp);
            psEmp.setString(1, emp.getEmployeeName());
            psEmp.setString(2, emp.getEmployeePhone());
            psEmp.setDate(3, emp.getBirthday() != null ? new java.sql.Date(emp.getBirthday().getTime()) : null);
            psEmp.setDouble(4, emp.getBaseSalary());
            psEmp.setInt(5, emp.getRoleId());
            psEmp.setInt(6, emp.getStatus());
            psEmp.setInt(7, emp.getEmployeeId());

            psEmp.executeUpdate();

            String sqlAcc = "UPDATE account SET status = ? WHERE employee_id = ?";
            psAcc = c.prepareStatement(sqlAcc);
            psAcc.setInt(1, emp.getStatus());
            psAcc.setInt(2, emp.getEmployeeId());

            psAcc.executeUpdate();

            c.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                try {
                    c.rollback();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try { if (psAcc != null) psAcc.close(); } catch (Exception e) {}
            try { if (psEmp != null) psEmp.close(); } catch (Exception e) {}
            try { if (c != null) { c.setAutoCommit(true); c.close(); } } catch (Exception e) {}
        }
    }

    public boolean isPhoneExist(String phone, int ignoreId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE employee_phone = ? AND employee_id != ?";
        try (java.sql.Connection c = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setInt(2, ignoreId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return false;
    }

    public boolean isNameExist(String name, int ignoreId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE employee_name = ? AND employee_id != ?";
        try (java.sql.Connection c = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, ignoreId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return false;
    }

    public int countBillsByEmployee(int employeeId) {
        String sql = "SELECT COUNT(*) FROM bill WHERE employee_id = ?";
        try (java.sql.Connection c = com.bookstore.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int insertEmployee(EmployeeDTO e) {
        String sql = "INSERT INTO employee (employee_name, employee_phone, birthday, base_salary, day_in, role_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = com.bookstore.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getEmployeeName());
            ps.setString(2, e.getEmployeePhone());
            ps.setDate(3, e.getBirthday() != null ? new java.sql.Date(e.getBirthday().getTime()) : null);
            ps.setDouble(4, e.getBaseSalary());
            ps.setDate(5, e.getDayIn() != null ? new java.sql.Date(e.getDayIn().getTime()) : null);
            ps.setInt(6, e.getRoleId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public List<EmployeeDTO> getEmployeesWithoutAccount() {
        List<EmployeeDTO> list = new ArrayList<>();
        String sql = "SELECT e.*, r.role_name FROM employee e " +
                "JOIN role r ON e.role_id = r.role_id " +
                "WHERE e.status = 1 AND e.employee_id NOT IN (SELECT employee_id FROM account)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO();
                emp.setEmployeeId(rs.getInt("employee_id"));
                emp.setEmployeeName(rs.getString("employee_name"));
                emp.setRoleId(rs.getInt("role_id"));
                emp.setRoleName(rs.getString("role_name"));
                list.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}

