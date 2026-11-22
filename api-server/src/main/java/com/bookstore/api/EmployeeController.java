package com.bookstore.api;

import com.bookstore.api.contract.EmployeeActivityDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dto.EmployeeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private static final double MAX_BASE_SALARY = 999_999_999_999_999.0;
    private static final double MAX_SALARY_FACTOR = 999.99;

    private final EmployeeBUS employeeBUS;
    private final AuthSessionStore sessionStore;

    @Autowired
    public EmployeeController(AuthSessionStore sessionStore) {
        this(sessionStore, new EmployeeBUS());
    }

    EmployeeController(AuthSessionStore sessionStore, EmployeeBUS employeeBUS) {
        this.sessionStore = sessionStore;
        this.employeeBUS = employeeBUS;
    }

    @GetMapping
    public List<EmployeeDTO> getEmployees(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_EMPLOYEE");
        return employeeBUS.getAllEmployees();
    }

    @GetMapping("/without-account")
    public List<EmployeeDTO> getEmployeesWithoutAccount(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_ACCOUNT", "MANAGE_EMPLOYEE");
        return employeeBUS.getEmployeesWithoutAccount();
    }

    @GetMapping("/{employeeId}/activity")
    public EmployeeActivityDTO getActivity(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("employeeId") int employeeId
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_EMPLOYEE");
        if (employeeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã nhân viên không hợp lệ.");
        }
        return new EmployeeActivityDTO(
                employeeBUS.getBillCountByEmployee(employeeId),
                employeeBUS.getCreatedImportCountByEmployee(employeeId),
                employeeBUS.getApprovedImportCountByEmployee(employeeId)
        );
    }

    @PostMapping
    public ResponseEntity<String> addEmployee(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) EmployeeDTO employee
    ) {
        sessionStore.requireAction(authorization, "MANAGE_EMPLOYEE");
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin nhân viên không hợp lệ.");
        }
        if (employee.getRoleId() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không được phép tạo nhân viên với chức vụ Admin.");
        }
        validateEmployee(employee, false);
        return ApiResponses.fromBusinessMessage(employeeBUS.addEmployee(employee));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<String> updateEmployee(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("employeeId") int employeeId,
            @RequestBody(required = false) EmployeeDTO employee
    ) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_EMPLOYEE");
        if (employeeId <= 0 || employee == null || employee.getEmployeeId() != employeeId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã nhân viên không hợp lệ.");
        }
        if (employee.getRoleId() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không được phép gán chức vụ Admin cho nhân viên.");
        }
        validateEmployee(employee, true);

        EmployeeDTO existing = employeeBUS.getEmployeeById(employeeId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên.");
        }
        if (existing.getRoleId() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể chỉnh sửa nhân viên Admin tại đây.");
        }
        if (session.employee() != null && session.employee().getEmployeeId() == employeeId
                && (employee.getRoleId() != existing.getRoleId() || employee.getStatus() != existing.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể tự thay đổi chức vụ hoặc trạng thái làm việc của bản thân.");
        }

        String result = employeeBUS.updateEmployee(employee);
        if (result != null && result.toLowerCase(Locale.ROOT).contains("thành công")
                && (employee.getStatus() == 0 || employee.getRoleId() != existing.getRoleId())) {
            sessionStore.revokeByEmployeeId(employeeId);
        }
        return ApiResponses.fromBusinessMessage(result);
    }

    private void validateEmployee(EmployeeDTO employee, boolean update) {
        if (employee == null || employee.getEmployeeName() == null || employee.getEmployeePhone() == null
                || !Double.isFinite(employee.getBaseSalary())
                || employee.getBaseSalary() <= 0 || employee.getBaseSalary() > MAX_BASE_SALARY
                || !Double.isFinite(employee.getSalaryFactor())
                || employee.getSalaryFactor() > MAX_SALARY_FACTOR
                || (update ? employee.getSalaryFactor() <= 0 : employee.getSalaryFactor() < 0)
                || employee.getRoleId() <= 0 || (employee.getStatus() != 0 && employee.getStatus() != 1)
                || (!update && (employee.getEmail() == null || employee.getEmail().isBlank() || employee.getDayIn() == null))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin nhân viên không hợp lệ.");
        }
        if (employee.getEmail() != null && !employee.getEmail().isBlank()
                && employee.getEmail().trim().length() > AccountBUS.MAX_USERNAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được vượt quá 50 ký tự.");
        }
    }
}
