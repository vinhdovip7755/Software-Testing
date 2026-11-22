package com.bookstore.bus;

import com.bookstore.dao.AccountDAO;
import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.PasswordUtil;

public class AccountBUS {
    public static final int MAX_USERNAME_LENGTH = 50;

    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
    }

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public EmployeeDTO login(String username, String password) throws Exception {
        String normalizedUsername = username == null ? "" : username.trim();
        AccountDTO acc = accountDAO.selectByUsername(normalizedUsername);

        if (acc == null) {
            throw new AuthenticationException("Tài khoản không tồn tại!");
        }

        EmployeeDTO emp = employeeDAO.selectById(acc.getEmployeeId());
        if (acc.getStatus() == 0 || (emp != null && emp.getStatus() == 0)) {
            throw new AuthenticationException("Tài khoản này đã bị khóa. Vui lòng liên hệ Quản Lý!");
        }

        if (!PasswordUtil.matches(password, acc.getPassword())) {
            throw new AuthenticationException("Mật khẩu không chính xác!");
        }

        // Tự động nâng cấp mật khẩu plaintext cũ lên BCrypt sau khi xác thực thành công
        if (!PasswordUtil.isBcryptHash(acc.getPassword())) {
            acc.setPassword(PasswordUtil.hash(password));
            accountDAO.updateAccount(acc, true);
        }

        return emp;
    }

    public AccountDTO selectByUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return accountDAO.selectByUsername(username.trim());
    }

    public AccountDTO selectByEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            return null;
        }
        return accountDAO.selectByEmployeeId(employeeId);
    }

    public String updateAccount(AccountDTO acc, boolean isChangePassword) {
        if (acc == null || acc.getUsername() == null || acc.getUsername().trim().isEmpty()) {
            return "Tên đăng nhập không hợp lệ!";
        }
        String trimmedUsername = acc.getUsername().trim();
        if (trimmedUsername.length() > MAX_USERNAME_LENGTH) {
            return "Email không được vượt quá 50 ký tự!";
        }
        if (acc.getStatus() != 0 && acc.getStatus() != 1) {
            return "Trạng thái tài khoản không hợp lệ!";
        }
        acc.setUsername(trimmedUsername);

        AccountDTO existingAcc = accountDAO.selectByUsername(trimmedUsername);
        if (existingAcc == null && acc.getEmployeeId() > 0) {
            existingAcc = accountDAO.selectByEmployeeId(acc.getEmployeeId());
        }

        int targetEmployeeId = acc.getEmployeeId() > 0
                ? acc.getEmployeeId()
                : (existingAcc != null ? existingAcc.getEmployeeId() : 0);
        if (targetEmployeeId > 0) {
            acc.setEmployeeId(targetEmployeeId);
        }

        if (acc.getStatus() == 0 && targetEmployeeId > 0) {
            EmployeeDTO targetEmp = employeeDAO.selectById(targetEmployeeId);
            if (targetEmp != null && targetEmp.getRoleId() == 1
                    && employeeDAO.countActiveAdminsExcluding(targetEmployeeId) == 0) {
                return "Không thể khóa tài khoản Admin duy nhất của hệ thống!";
            }
        }

        if (isChangePassword) {
            if (acc.getPassword() == null || acc.getPassword().isBlank()) {
                return "Mật khẩu mới không được để trống!";
            }
            if (existingAcc != null && existingAcc.getPassword() != null
                    && PasswordUtil.isSamePassword(acc.getPassword(), existingAcc.getPassword())) {
                return "Mật khẩu mới không được trùng với mật khẩu cũ!";
            }
            acc.setPassword(PasswordUtil.hash(acc.getPassword()));
        }

        boolean success = accountDAO.updateAccount(acc, isChangePassword);

        if (success) {
            return "Cập nhật tài khoản thành công!";
        } else {
            return "Cập nhật thất bại. Vui lòng kiểm tra lại hệ thống!";
        }
    }

    public String addAccount(AccountDTO acc) {
        if (acc == null || acc.getUsername() == null || acc.getUsername().trim().isEmpty()) {
            return "Username không được để trống!";
        }
        String trimmedUsername = acc.getUsername().trim();
        if (trimmedUsername.length() > MAX_USERNAME_LENGTH) {
            return "Email không được vượt quá 50 ký tự!";
        }
        if (acc.getPassword() == null || acc.getPassword().isBlank()) {
            return "Mật khẩu không được để trống!";
        }
        if (acc.getStatus() != 0 && acc.getStatus() != 1) {
            return "Trạng thái tài khoản không hợp lệ!";
        }

        if (accountDAO.selectByUsername(trimmedUsername) != null) {
            return "Username này đã tồn tại!";
        }

        acc.setUsername(trimmedUsername);
        acc.setPassword(PasswordUtil.hash(acc.getPassword()));
        return accountDAO.insert(acc) ? "Thêm tài khoản thành công!" : "Thêm thất bại!";
    }

    public boolean isEmailExists(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return accountDAO.selectByUsername(email.trim()) != null;
    }

    public String verifyEmployeeInfoByPhone(String name, String phone, java.util.Date dob) {
        com.bookstore.dao.EmployeeDAO empDAO = new com.bookstore.dao.EmployeeDAO();
        com.bookstore.dto.EmployeeDTO emp = null;
        for (com.bookstore.dto.EmployeeDTO e : empDAO.selectAllEmployees()) {
            if (phone != null && phone.equals(e.getEmployeePhone())) {
                emp = e;
                break;
            }
        }
        if (emp == null) return "Không tìm thấy nhân viên với số điện thoại này!";
        if (name == null || !name.equalsIgnoreCase(emp.getEmployeeName())) return "Họ tên không khớp!";
        if (emp.getBirthday() == null) return "Hệ thống chưa có ngày sinh của nhân viên này!";
        if (dob == null) return "Ngày sinh không chính xác!";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        if (!sdf.format(dob).equals(sdf.format(emp.getBirthday()))) return "Ngày sinh không chính xác!";
        return "OK";
    }

    public String resetPasswordByEmail(String email, String newPass) {
        if (email == null || email.isBlank()) return "Email không hợp lệ";
        if (newPass == null || newPass.isBlank() || newPass.length() < 4) return "Mật khẩu mới không hợp lệ";
        AccountDTO acc = accountDAO.selectByUsername(email.trim());
        if (acc == null) return "Không tìm thấy tài khoản";
        acc.setPassword(PasswordUtil.hash(newPass));
        return accountDAO.updateAccount(acc, true) ? "OK" : "Lỗi cập nhật mật khẩu";
    }

    public String resetPasswordByPhone(String phone, String newPass) {
        if (newPass == null || newPass.isBlank() || newPass.length() < 4) return "Mật khẩu mới không hợp lệ!";
        com.bookstore.dao.EmployeeDAO empDAO = new com.bookstore.dao.EmployeeDAO();
        com.bookstore.dto.EmployeeDTO emp = null;
        for (com.bookstore.dto.EmployeeDTO e : empDAO.selectAllEmployees()) {
            if (phone != null && phone.equals(e.getEmployeePhone())) {
                emp = e;
                break;
            }
        }
        if (emp == null) return "Không tìm thấy nhân viên!";
        AccountDTO acc = accountDAO.selectByEmployeeId(emp.getEmployeeId());
        if (acc == null) return "Nhân viên chưa có tài khoản!";
        acc.setPassword(PasswordUtil.hash(newPass));
        boolean success = accountDAO.updateAccount(acc, true);
        return success ? "OK" : "Lỗi Cập nhật mật khẩu!";
    }

    public String updateUsername(String oldUsername, String newUsername) {
        if (oldUsername == null || oldUsername.isBlank() || newUsername == null || newUsername.isBlank()) {
            return "Email không hợp lệ!";
        }
        String trimmedOld = oldUsername.trim();
        String trimmedNew = newUsername.trim();
        if (trimmedNew.length() > MAX_USERNAME_LENGTH) {
            return "Email không được vượt quá 50 ký tự!";
        }
        if (trimmedOld.equals(trimmedNew)) return "OK";

        AccountDTO existing = accountDAO.selectByUsername(trimmedNew);
        if (existing != null && !existing.getUsername().equalsIgnoreCase(trimmedOld)) {
            return "Email này đã được sử dụng bởi người khác!";
        }

        AccountDTO acc = accountDAO.selectByUsername(trimmedOld);
        if (acc == null) return "Không tìm thấy tài khoản!";

        boolean success = accountDAO.updateUsername(trimmedOld, trimmedNew);
        return success ? "OK" : "Lỗi Cập nhật tên đăng nhập!";
    }
}
