package com.bookstore.bus;

import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.EmployeeDTO;

import java.util.List;

public class EmployeeBUS {
    private EmployeeDAO employeeDAO = new EmployeeDAO();

    public List<EmployeeDTO> getAllEmployees() {
        return employeeDAO.selectAllEmployees();
    }

    public String updateEmployee(EmployeeDTO e) {
        if (e.getEmployeeName().trim().isEmpty()) return "Tên nhân viên không được để trống!";
        if (!e.getEmployeeName().trim().matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$")) return "Tên nhân viên không hợp lệ!";
        if (e.getEmployeePhone().trim().isEmpty()) return "Số điện thoại không được để trống!";
        if (!e.getEmployeePhone().matches("^0\\d{9}$")) return "Số điện thoại phải có 10 số và bắt đầu bằng 0!";

        if (employeeDAO.isPhoneExist(e.getEmployeePhone(), e.getEmployeeId())) {
            return "Số điện thoại này đã tồn tại trong hệ thống!";
        }

        if (e.getBaseSalary() < 0) {
            return "Lương cơ bản phải lớn hơn hoặc bằng 0!";
        }

        if (e.getSalaryFactor() < 0) {
            return "Hệ số lương phải lớn hơn hoặc bằng 0!";
        }

        if (e.getStatus() == 0 || e.getRoleId() > 2) {
            int activeManagers = employeeDAO.countActiveManagersExcluding(e.getEmployeeId());
            if (activeManagers == 0) {
                return "Không thể thay đổi chức vụ hoặc cho nghỉ việc, vì hệ thống phải có ít nhất 1 Quản lý/Admin đang làm việc!";
            }
        }

        return employeeDAO.updateEmployee(e) ? "Cập nhật thành công!" : "Cập nhật thất bại!";
    }

    public int getBillCountByEmployee(int employeeId) {
        return employeeDAO.countBillsByEmployee(employeeId);
    }

    public int getCreatedImportCountByEmployee(int employeeId) {
        return employeeDAO.countCreatedImportByEmployee(employeeId);
    }

    public int getApprovedImportCountByEmployee(int employeeId) {
        return employeeDAO.countApprovedImportByEmployee(employeeId);
    }

    public String addEmployee(EmployeeDTO e) {
        if (e.getEmployeeName().trim().isEmpty()) return "Tên nhân viên không được để trống!";
        if (!e.getEmployeeName().trim().matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$")) return "Tên nhân viên không hợp lệ!";
        if (e.getEmployeePhone().trim().isEmpty()) return "Số điện thoại không được để trống!";
        if (!e.getEmployeePhone().matches("^0\\d{9}$")) return "Số điện thoại phải có 10 số và bắt đầu bằng 0!";
        if (e.getEmail() == null || e.getEmail().trim().isEmpty()) return "Email không được để trống!";
        if (!e.getEmail().trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) return "Email không đúng định dạng!";
        if (e.getDayIn() == null) return "Vui lòng chọn ngày vào làm!";

        if (employeeDAO.isPhoneExist(e.getEmployeePhone(), 0)) {
            return "Số điện thoại này đã tồn tại trong hệ thống!";
        }

        if (e.getBaseSalary() < 0) {
            return "Lương cơ bản phải lớn hơn hoặc bằng 0!";
        }

        com.bookstore.bus.AccountBUS accountBUS = new com.bookstore.bus.AccountBUS();
        if (accountBUS.isEmailExists(e.getEmail())) {
            return "Email này đã được đăng ký cho một tài khoản khác!";
        }

        int newId = employeeDAO.insertEmployee(e);
        if (newId > 0) {
            try {
                com.bookstore.dto.AccountDTO acc = new com.bookstore.dto.AccountDTO();
                acc.setEmployeeId(newId);
                acc.setUsername(e.getEmail());
                String pwd = "123456";
                if (e.getBirthday() != null) {
                    pwd = new java.text.SimpleDateFormat("ddMMyyyy").format(e.getBirthday());
                }
                acc.setPassword(pwd);
                acc.setStatus(1);
                accountBUS.addAccount(acc);
            } catch(Exception ex) { ex.printStackTrace(); }
            return "Thêm nhân viên thành công!";
        }
        return "Thêm thất bại!";
    }

    public List<EmployeeDTO> getEmployeesWithoutAccount() {
        return employeeDAO.getEmployeesWithoutAccount();
    }
}
