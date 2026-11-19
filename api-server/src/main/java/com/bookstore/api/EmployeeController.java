package com.bookstore.api;

import com.bookstore.api.contract.EmployeeActivityDTO;
import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dto.EmployeeDTO;
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

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final AuthSessionStore sessionStore;

    public EmployeeController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
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
            @RequestBody EmployeeDTO employee
    ) {
        sessionStore.requireAction(authorization, "MANAGE_EMPLOYEE");
        validateEmployee(employee, false);
        return ApiResponses.fromBusinessMessage(employeeBUS.addEmployee(employee));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<String> updateEmployee(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("employeeId") int employeeId,
            @RequestBody EmployeeDTO employee
    ) {
        sessionStore.requireAction(authorization, "MANAGE_EMPLOYEE");
        if (employeeId <= 0 || employee == null || employee.getEmployeeId() != employeeId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã nhân viên không hợp lệ.");
        }
        validateEmployee(employee, true);
        return ApiResponses.fromBusinessMessage(employeeBUS.updateEmployee(employee));
    }

    private void validateEmployee(EmployeeDTO employee, boolean update) {
        if (employee == null || employee.getEmployeeName() == null || employee.getEmployeePhone() == null
                || employee.getBaseSalary() < 0 || employee.getSalaryFactor() < 0
                || employee.getRoleId() <= 0 || (employee.getStatus() != 0 && employee.getStatus() != 1)
                || (!update && (employee.getEmail() == null || employee.getDayIn() == null))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin nhân viên không hợp lệ.");
        }
    }
}
