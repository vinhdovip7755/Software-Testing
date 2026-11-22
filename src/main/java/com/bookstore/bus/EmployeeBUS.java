package com.bookstore.bus;

import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.PasswordUtil;

import java.text.SimpleDateFormat;
import java.util.List;

public class EmployeeBUS {
    private static final String NAME_REGEX = "^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$";
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final double MAX_BASE_SALARY = 999_999_999_999_999.0;
    private static final double MAX_SALARY_FACTOR = 999.99;

    private final EmployeeDAO employeeDAO;
    private final AccountBUS accountBUS;

    public EmployeeBUS() {
        this(new EmployeeDAO(), new AccountBUS());
    }

    EmployeeBUS(EmployeeDAO employeeDAO, AccountBUS accountBUS) {
        this.employeeDAO = employeeDAO;
        this.accountBUS = accountBUS;
    }

    public List<EmployeeDTO> getAllEmployees() {
        return employeeDAO.selectAllEmployees();
    }

    public EmployeeDTO getEmployeeById(int employeeId) {
        if (employeeId <= 0) {
            return null;
        }
        return employeeDAO.selectById(employeeId);
    }

    public String updateEmployee(EmployeeDTO e) {
        if (e == null) return "Thông tin nhân viên không hợp lệ!";
        if (e.getEmployeeName() == null || e.getEmployeeName().trim().isEmpty()) return "Tên nhân viên không được để trống!";
        String trimmedName = e.getEmployeeName().trim();
        if (trimmedName.length() > 100 || !trimmedName.matches(NAME_REGEX)) return "Tên nhân viên không hợp lệ!";
        if (e.getEmployeePhone() == null || e.getEmployeePhone().trim().isEmpty()) return "Số điện thoại không được để trống!";
        String trimmedPhone = e.getEmployeePhone().trim();
        if (!trimmedPhone.matches("^0\\d{9}$")) return "Số điện thoại phải có 10 số và bắt đầu bằng 0!";

        if (!Double.isFinite(e.getBaseSalary()) || e.getBaseSalary() <= 0 || e.getBaseSalary() > MAX_BASE_SALARY) {
            return "Lương cơ bản phải là số dương hợp lệ!";
        }

        if (!Double.isFinite(e.getSalaryFactor()) || e.getSalaryFactor() <= 0 || e.getSalaryFactor() > MAX_SALARY_FACTOR) {
            return "Hệ số lương phải là số dương hợp lệ!";
        }

        if (e.getStatus() != 0 && e.getStatus() != 1) {
            return "Trạng thái nhân viên không hợp lệ!";
        }

        if (e.getRoleId() <= 0) {
            return "Chức vụ không hợp lệ!";
        }

        if (e.getEmail() != null && !e.getEmail().trim().isEmpty()) {
            String trimmedEmail = e.getEmail().trim();
            if (trimmedEmail.length() > AccountBUS.MAX_USERNAME_LENGTH || !trimmedEmail.matches(EMAIL_REGEX)) {
                return "Email không đúng định dạng!";
            }
            e.setEmail(trimmedEmail);
        }

        if (employeeDAO.isPhoneExist(trimmedPhone, e.getEmployeeId())) {
            return "Số điện thoại này đã tồn tại trong hệ thống!";
        }

        EmployeeDTO existing = employeeDAO.selectById(e.getEmployeeId());
        if (existing == null) {
            return "Không tìm thấy nhân viên!";
        }

        if (e.getRoleId() == 1 && existing.getRoleId() != 1) {
            return "Không được phép gán chức vụ Admin cho nhân viên!";
        }

        if (existing.getRoleId() == 1 && (e.getStatus() == 0 || e.getRoleId() != 1)) {
            int activeAdmins = employeeDAO.countActiveAdminsExcluding(e.getEmployeeId());
            if (activeAdmins == 0) {
                return "Không thể thay đổi chức vụ hoặc cho nghỉ việc Admin duy nhất của hệ thống!";
            }
        }

        if (existing.getRoleId() == 2 && (e.getStatus() == 0 || e.getRoleId() != 2)) {
            int activeManagers = employeeDAO.countActiveManagersExcluding(e.getEmployeeId());
            if (activeManagers == 0) {
                return "Không thể thay đổi chức vụ hoặc cho nghỉ việc, vì hệ thống phải có ít nhất 1 Quản lý đang làm việc!";
            }
        }

        e.setEmployeeName(trimmedName);
        e.setEmployeePhone(trimmedPhone);
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
        if (e == null) return "Thông tin nhân viên không hợp lệ!";
        if (e.getEmployeeName() == null || e.getEmployeeName().trim().isEmpty()) return "Tên nhân viên không được để trống!";
        String trimmedName = e.getEmployeeName().trim();
        if (trimmedName.length() > 100 || !trimmedName.matches(NAME_REGEX)) return "Tên nhân viên không hợp lệ!";
        if (e.getEmployeePhone() == null || e.getEmployeePhone().trim().isEmpty()) return "Số điện thoại không được để trống!";
        String trimmedPhone = e.getEmployeePhone().trim();
        if (!trimmedPhone.matches("^0\\d{9}$")) return "Số điện thoại phải có 10 số và bắt đầu bằng 0!";
        if (e.getEmail() == null || e.getEmail().trim().isEmpty()) return "Email không được để trống!";
        String trimmedEmail = e.getEmail().trim();
        if (!trimmedEmail.matches(EMAIL_REGEX)) return "Email không đúng định dạng!";
        if (trimmedEmail.length() > AccountBUS.MAX_USERNAME_LENGTH) return "Email không được vượt quá 50 ký tự!";
        if (e.getDayIn() == null) return "Vui lòng chọn ngày vào làm!";

        if (e.getRoleId() <= 0) {
            return "Chức vụ không hợp lệ!";
        }
        if (e.getRoleId() == 1) {
            return "Không được phép tạo nhân viên với chức vụ Admin!";
        }

        if (!Double.isFinite(e.getBaseSalary()) || e.getBaseSalary() <= 0 || e.getBaseSalary() > MAX_BASE_SALARY) {
            return "Lương cơ bản phải là số dương hợp lệ!";
        }

        if (!Double.isFinite(e.getSalaryFactor()) || e.getSalaryFactor() < 0 || e.getSalaryFactor() > MAX_SALARY_FACTOR) {
            return "Hệ số lương phải là số dương hợp lệ!";
        }
        if (e.getSalaryFactor() == 0) {
            e.setSalaryFactor(1.0);
        }

        if (employeeDAO.isPhoneExist(trimmedPhone, 0)) {
            return "Số điện thoại này đã tồn tại trong hệ thống!";
        }

        if (accountBUS.isEmailExists(trimmedEmail) || employeeDAO.isEmailExist(trimmedEmail, 0)) {
            return "Email này đã được đăng ký cho một tài khoản khác!";
        }

        e.setEmployeeName(trimmedName);
        e.setEmployeePhone(trimmedPhone);
        e.setEmail(trimmedEmail);

        String pwd = "123456";
        if (e.getBirthday() != null) {
            pwd = new SimpleDateFormat("ddMMyyyy").format(e.getBirthday());
        }
        String hashedPassword = PasswordUtil.hash(pwd);

        int newId = employeeDAO.insertEmployeeWithAccount(e, hashedPassword);
        if (newId > 0) {
            return "Thêm nhân viên thành công!";
        }
        return "Thêm thất bại!";
    }

    public List<EmployeeDTO> getEmployeesWithoutAccount() {
        return employeeDAO.getEmployeesWithoutAccount();
    }
}
