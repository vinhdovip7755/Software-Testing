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

        String sql = "SELECT e.*, r.role_name FROM employee e JOIN role r ON r.role_id = e.role_id WHERE employee_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                employee = new EmployeeDTO(
                        rs.getInt("employee_id"),
                        rs.getString("employee_name"),
                        rs.getString("employee_phone"),
                        rs.getString("email"),
                        rs.getDate("birthday"),
                        rs.getDouble("base_salary"),
                        rs.getDouble("salary_factor"),
                        rs.getDate("day_in"),
                        rs.getInt("status"),
                        rs.getInt("role_id"),
                        rs.getString("r.role_name")
                );
            }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể truy vấn nhân viên.", e);
        }
        return employee;
    }

    public List<EmployeeDTO> selectAllEmployees() {
        List<EmployeeDTO> list = new ArrayList<>();
        String sql = "SELECT e.*, r.role_name FROM employee e JOIN role r ON e.role_id = r.role_id WHERE e.role_id != 1 ORDER BY e.employee_id ASC";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO(
                        rs.getInt("employee_id"),
                        rs.getString("employee_name"),
                        rs.getString("employee_phone"),
                        rs.getString("email"),
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
            throw new IllegalStateException("Không thể tải danh sách nhân viên.", e);
        }
        return list;
    }

    public boolean updateEmployee(EmployeeDTO emp) {
        if (emp == null || !Double.isFinite(emp.getBaseSalary()) || emp.getBaseSalary() <= 0
                || !Double.isFinite(emp.getSalaryFactor()) || emp.getSalaryFactor() <= 0) {
            return false;
        }

        Connection c = null;
        PreparedStatement psEmp = null;
        PreparedStatement psAcc = null;

        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            String sqlEmp = "UPDATE employee SET employee_name = ?, employee_phone = ?, email = ?, birthday = ?, " +
                    "base_salary = ?, salary_factor = ?, role_id = ?, status = ? WHERE employee_id = ?";
            psEmp = c.prepareStatement(sqlEmp);
            psEmp.setString(1, emp.getEmployeeName());
            psEmp.setString(2, emp.getEmployeePhone());
            psEmp.setString(3, emp.getEmail());
            psEmp.setDate(4, emp.getBirthday() != null ? new java.sql.Date(emp.getBirthday().getTime()) : null);
            psEmp.setDouble(5, emp.getBaseSalary());
            psEmp.setDouble(6, emp.getSalaryFactor());
            psEmp.setInt(7, emp.getRoleId());
            psEmp.setInt(8, emp.getStatus());
            psEmp.setInt(9, emp.getEmployeeId());

            int updated = psEmp.executeUpdate();
            if (updated == 0) {
                c.rollback();
                return false;
            }

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
            if (psAcc != null) {
                try { psAcc.close(); } catch (Exception ignored) {}
            }
            if (psEmp != null) {
                try { psEmp.close(); } catch (Exception ignored) {}
            }
            if (c != null) {
                try { c.setAutoCommit(true); } catch (Exception ignored) {}
                try { c.close(); } catch (Exception ignored) {}
            }
        }
    }

    public boolean updateSelfProfile(
            int employeeId,
            String employeeName,
            String employeePhone,
            String email,
            java.sql.Date birthday
    ) {
        String sql = "UPDATE employee SET employee_name = ?, employee_phone = ?, email = ?, birthday = ? " +
                "WHERE employee_id = ? AND status = 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, employeeName);
            ps.setString(2, employeePhone);
            ps.setString(3, email);
            ps.setDate(4, birthday != null ? new java.sql.Date(birthday.getTime()) : null);
            ps.setInt(5, employeeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSelfProfileAtomic(
            int employeeId,
            String employeeName,
            String employeePhone,
            String email,
            java.sql.Date birthday,
            String oldUsername,
            String newUsername,
            String hashedNewPassword
    ) {
        Connection c = null;
        try {
            c = DatabaseConnection.getConnection();
            c.setAutoCommit(false);

            String sqlEmp = "UPDATE employee SET employee_name = ?, employee_phone = ?, email = ?, birthday = ? " +
                    "WHERE employee_id = ? AND status = 1";
            int updatedEmp;
            try (PreparedStatement psEmp = c.prepareStatement(sqlEmp)) {
                psEmp.setString(1, employeeName);
                psEmp.setString(2, employeePhone);
                psEmp.setString(3, email);
                psEmp.setDate(4, birthday != null ? new java.sql.Date(birthday.getTime()) : null);
                psEmp.setInt(5, employeeId);
                updatedEmp = psEmp.executeUpdate();
            }
            if (updatedEmp == 0) {
                c.rollback();
                return false;
            }

            boolean usernameChanged = oldUsername != null && newUsername != null && !oldUsername.equals(newUsername);
            boolean passwordChanged = hashedNewPassword != null && !hashedNewPassword.isBlank();
            if (usernameChanged || passwordChanged) {
                String sqlAcc;
                if (usernameChanged && passwordChanged) {
                    sqlAcc = "UPDATE account SET username = ?, password = ? WHERE employee_id = ? AND username = ? AND status = 1";
                } else if (usernameChanged) {
                    sqlAcc = "UPDATE account SET username = ? WHERE employee_id = ? AND username = ? AND status = 1";
                } else {
                    sqlAcc = "UPDATE account SET password = ? WHERE employee_id = ? AND username = ? AND status = 1";
                }

                int updatedAcc;
                try (PreparedStatement psAcc = c.prepareStatement(sqlAcc)) {
                    if (usernameChanged && passwordChanged) {
                        psAcc.setString(1, newUsername);
                        psAcc.setString(2, hashedNewPassword);
                        psAcc.setInt(3, employeeId);
                        psAcc.setString(4, oldUsername);
                    } else if (usernameChanged) {
                        psAcc.setString(1, newUsername);
                        psAcc.setInt(2, employeeId);
                        psAcc.setString(3, oldUsername);
                    } else {
                        psAcc.setString(1, hashedNewPassword);
                        psAcc.setInt(2, employeeId);
                        psAcc.setString(3, oldUsername);
                    }
                    updatedAcc = psAcc.executeUpdate();
                }
                if (updatedAcc == 0) {
                    c.rollback();
                    return false;
                }
            }

            c.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (c != null) {
                try { c.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (c != null) {
                try { c.setAutoCommit(true); } catch (Exception ignored) {}
                try { c.close(); } catch (Exception ignored) {}
            }
        }
    }

    public boolean isEmailExist(String email, int ignoreId) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String sql = "SELECT COUNT(*) FROM employee WHERE LOWER(email) = LOWER(?) AND employee_id != ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setInt(2, ignoreId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể kiểm tra email nhân viên.", ex);
        }
        return false;
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
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể kiểm tra số điện thoại nhân viên.", ex);
        }
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
            throw new IllegalStateException("Không thể đếm hóa đơn của nhân viên.", e);
        }
        throw new IllegalStateException("Không thể đếm hóa đơn của nhân viên.");
    }

    public int countCreatedImportByEmployee(int employeeId) {
        String sql = "SELECT COUNT(*) FROM import_ticket WHERE employee_id = ?";
        try (java.sql.Connection c = com.bookstore.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể đếm phiếu nhập do nhân viên lập.", e);
        }
        throw new IllegalStateException("Không thể đếm phiếu nhập do nhân viên lập.");
    }

    public int countApprovedImportByEmployee(int employeeId) {
        String sql = "SELECT COUNT(*) FROM import_ticket WHERE approver_id = ? AND status = 2";
        try (java.sql.Connection c = com.bookstore.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể đếm phiếu nhập do nhân viên duyệt.", e);
        }
        throw new IllegalStateException("Không thể đếm phiếu nhập do nhân viên duyệt.");
    }

    public int insertEmployee(EmployeeDTO e) {
        if (e == null || !Double.isFinite(e.getBaseSalary()) || e.getBaseSalary() <= 0
                || !Double.isFinite(e.getSalaryFactor()) || e.getSalaryFactor() <= 0) {
            return 0;
        }
        String sql = "INSERT INTO employee (employee_name, employee_phone, email, birthday, base_salary, salary_factor, day_in, role_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = com.bookstore.util.DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getEmployeeName());
            ps.setString(2, e.getEmployeePhone());
            ps.setString(3, e.getEmail());
            ps.setDate(4, e.getBirthday() != null ? new java.sql.Date(e.getBirthday().getTime()) : null);
            ps.setDouble(5, e.getBaseSalary());
            ps.setDouble(6, e.getSalaryFactor());
            ps.setDate(7, e.getDayIn() != null ? new java.sql.Date(e.getDayIn().getTime()) : null);
            ps.setInt(8, e.getRoleId());

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

    public int insertEmployeeWithAccount(EmployeeDTO e, String hashedPassword) {
        if (e == null || hashedPassword == null || hashedPassword.isBlank()
                || !Double.isFinite(e.getBaseSalary()) || e.getBaseSalary() <= 0
                || !Double.isFinite(e.getSalaryFactor()) || e.getSalaryFactor() <= 0) {
            return 0;
        }
        String insertEmpSql = "INSERT INTO employee (employee_name, employee_phone, email, birthday, base_salary, salary_factor, day_in, status, role_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?)";
        String insertAccSql = "INSERT INTO account (username, password, employee_id, status) VALUES (?, ?, ?, 1)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int newId = 0;
            try (PreparedStatement psEmp = conn.prepareStatement(insertEmpSql, Statement.RETURN_GENERATED_KEYS)) {
                psEmp.setString(1, e.getEmployeeName());
                psEmp.setString(2, e.getEmployeePhone());
                psEmp.setString(3, e.getEmail());
                psEmp.setDate(4, e.getBirthday() != null ? new java.sql.Date(e.getBirthday().getTime()) : null);
                psEmp.setDouble(5, e.getBaseSalary());
                psEmp.setDouble(6, e.getSalaryFactor());
                psEmp.setDate(7, e.getDayIn() != null ? new java.sql.Date(e.getDayIn().getTime()) : null);
                psEmp.setInt(8, e.getRoleId());

                int affected = psEmp.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = psEmp.getGeneratedKeys()) {
                        if (rs.next()) {
                            newId = rs.getInt(1);
                        }
                    }
                }
            }

            if (newId <= 0) {
                conn.rollback();
                return 0;
            }

            try (PreparedStatement psAcc = conn.prepareStatement(insertAccSql)) {
                psAcc.setString(1, e.getEmail());
                psAcc.setString(2, hashedPassword);
                psAcc.setInt(3, newId);
                if (psAcc.executeUpdate() == 0) {
                    conn.rollback();
                    return 0;
                }
            }

            conn.commit();
            return newId;
        } catch (Exception ex) {
            ex.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (Exception rollbackEx) { rollbackEx.printStackTrace(); }
            }
            return 0;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (Exception ignored) {}
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    public int countActiveAdminsExcluding(int employeeId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE role_id = 1 AND status = 1 AND employee_id != ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public int countActiveManagersExcluding(int employeeId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE role_id = 2 AND status = 1 AND employee_id != ?";
        try (java.sql.Connection c = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
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
                "WHERE e.status = 1 AND e.role_id != 1 AND e.employee_id NOT IN (SELECT employee_id FROM account)";

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                EmployeeDTO emp = new EmployeeDTO(
                        rs.getInt("employee_id"),
                        rs.getString("employee_name"),
                        rs.getString("employee_phone"),
                        rs.getString("email"),
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
            throw new IllegalStateException("Không thể tải danh sách nhân viên chưa có tài khoản.", e);
        }
        return list;
    }
}

