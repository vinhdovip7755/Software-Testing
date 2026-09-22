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
        if (e.getEmployeePhone().trim().isEmpty()) return "Số điện thoại không được để trống!";
        if (!e.getEmployeePhone().matches("^0\\d{9}$")) return "Số điện thoại phải có 10 số và bắt đầu bằng 0!";

        if (employeeDAO.isPhoneExist(e.getEmployeePhone(), e.getEmployeeId())) {
            return "Số điện thoại này đã tồn tại trong hệ thống!";
        }
        if (employeeDAO.isNameExist(e.getEmployeeName(), e.getEmployeeId())) {
            return "Tên nhân viên này đã tồn tại trong hệ thống!";
        }

        if (e.getBaseSalary() < 0) {
            return "Lương cơ bản phải lớn hơn hoặc bằng 0!";
        }

        if (e.getSalaryFactor() < 0) {
            return "Hệ số lương phải lớn hơn hoặc bằng 0!";
        }

        return employeeDAO.updateEmployee(e) ? "Cập nhật thành công!" : "Cập nhật thất bại!";
    }

    public int getBillCountByEmployee(int employeeId) {
        return employeeDAO.countBillsByEmployee(employeeId);
    }

    public String addEmployee(EmployeeDTO e) {
        if (e.getEmployeeName().trim().isEmpty()) return "Tên nhân viên không được để trống!";
        if (e.getEmployeePhone().trim().isEmpty()) return "Số điện thoại không được để trống!";
        if (!e.getEmployeePhone().matches("^0\\d{9}$")) return "Số điện thoại phải có 10 số và bắt đầu bằng 0!";
        if (e.getDayIn() == null) return "Vui lòng chọn ngày vào làm!";

        if (employeeDAO.isPhoneExist(e.getEmployeePhone(), 0)) {
            return "Số điện thoại này đã tồn tại trong hệ thống!";
        }

        if (employeeDAO.isNameExist(e.getEmployeeName(), 0)) {
            return "Tên nhân viên này đã tồn tại trong hệ thống!";
        }

        if (e.getBaseSalary() < 0) {
            return "Lương cơ bản phải lớn hơn hoặc bằng 0!";
        }

        return employeeDAO.insertEmployee(e) > 0 ? "Thêm nhân viên thành công!" : "Thêm thất bại!";
    }

    public List<EmployeeDTO> getEmployeesWithoutAccount() {
        return employeeDAO.getEmployeesWithoutAccount();
    }
}
