package com.bookstore.bus;

import com.bookstore.dao.AccountDAO;
import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.EmployeeDTO;

public class AccountBUS {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public EmployeeDTO login(String username, String password) throws Exception {
        AccountDTO acc = accountDAO.selectByUsername(username);

        if (acc == null) {
            throw new Exception("Tài khoản không tồn tại!");
        }

        if (acc.getStatus() == 0) {
            throw new Exception("Tài khoản này đã bị khóa. Vui lòng liên hệ Quản Lý!");
        }

        if (!acc.getPassword().equals(password)) {
            throw new Exception("Mật khẩu không chính xác!");
        }

        return employeeDAO.selectById(acc.getEmployeeId());
    }

    public AccountDTO selectByUsername(String username) {
        return accountDAO.selectByUsername(username);
    }

    public String updateAccount(AccountDTO acc, boolean isChangePassword) {
        if (acc.getUsername() == null || acc.getUsername().trim().isEmpty()) {
            return "Tên đăng nhập không hợp lệ!";
        }

        boolean success = accountDAO.updateAccount(acc, isChangePassword);

        if (success) {
            return "Cập nhật tài khoản thành công!";
        } else {
            return "Cập nhật thất bại. Vui lòng kiểm tra lại hệ thống!";
        }
    }

    public String addAccount(AccountDTO acc) {
        if (acc.getUsername().trim().isEmpty()) return "Username không được để trống!";

        if (accountDAO.selectByUsername(acc.getUsername()) != null) {
            return "Username này đã tồn tại!";
        }

        return accountDAO.insert(acc) ? "Thêm tài khoản thành công!" : "Thêm thất bại!";
    }
        public boolean isEmailExists(String email) { return accountDAO.selectByUsername(email) != null; } public String verifyEmployeeInfoByPhone(String name, String phone, java.util.Date dob) {
        com.bookstore.dao.EmployeeDAO empDAO = new com.bookstore.dao.EmployeeDAO();
        com.bookstore.dto.EmployeeDTO emp = null;
        for (com.bookstore.dto.EmployeeDTO e : empDAO.selectAllEmployees()) {
            if (phone.equals(e.getEmployeePhone())) {
                emp = e;
                break;
            }
        }
        if (emp == null) return "Không tìm thấy nhân viên với số điện thoại này!";
        if (!name.equalsIgnoreCase(emp.getEmployeeName())) return "Họ tên không khớp!";
        if (emp.getBirthday() == null) return "Hệ thống chưa có ngày sinh của nhân viên này!";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        if (!sdf.format(dob).equals(sdf.format(emp.getBirthday()))) return "Ngày sinh không chính xác!";
        return "OK";
    }

    public String resetPasswordByEmail(String email, String newPass) { AccountDTO acc = accountDAO.selectByUsername(email); if(acc == null) return "Không tìm thấy tài khoản"; acc.setPassword(newPass); return accountDAO.updateAccount(acc, true) ? "OK" : "Lỗi cập nhật mật khẩu"; } public String resetPasswordByPhone(String phone, String newPass) {
        com.bookstore.dao.EmployeeDAO empDAO = new com.bookstore.dao.EmployeeDAO();
        com.bookstore.dto.EmployeeDTO emp = null;
        for (com.bookstore.dto.EmployeeDTO e : empDAO.selectAllEmployees()) {
            if (phone.equals(e.getEmployeePhone())) {
                emp = e;
                break;
            }
        }
        if (emp == null) return "Không tìm thấy nhân viên!";
        com.bookstore.dto.AccountDTO acc = null;
        for(com.bookstore.dto.AccountDTO a : accountDAO.selectAllAccounts()) {
            if(a.getEmployeeId() == emp.getEmployeeId()) {
                acc = a;
                break;
            }
        }
        if (acc == null) return "Nhân viên chưa có tài khoản!";
        acc.setPassword(newPass);
        boolean success = accountDAO.updateAccount(acc, true);
        return success ? "OK" : "Lỗi Cập nhật mật khẩu!";
    }

    public String updateUsername(String oldUsername, String newUsername) {
        if (oldUsername.equals(newUsername)) return "OK";
        
        com.bookstore.dto.AccountDTO existing = accountDAO.selectByUsername(newUsername);
        if (existing != null) return "Email này đã được sử dụng bởi người khác!";
        
        com.bookstore.dto.AccountDTO acc = accountDAO.selectByUsername(oldUsername);
        if (acc == null) return "Không tìm thấy tài khoản!";
        
        boolean success = accountDAO.updateUsername(oldUsername, newUsername);
        return success ? "OK" : "Lỗi Cập nhật tên đăng nhập!";
    }
}
