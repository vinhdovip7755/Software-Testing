package com.bookstore.api;

import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeControllerTest {
    @Test
    void employeeEndpointsRequireEmployeeViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        EmployeeController controller = new EmployeeController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getEmployees("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void updateRejectsEmployeeIdMismatchBeforeAccessingDatabase() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_EMPLOYEE", new PermissionDTO("MANAGE_EMPLOYEE", true, true))
        );
        EmployeeController controller = new EmployeeController(sessionStore);
        EmployeeDTO employee = new EmployeeDTO();
        employee.setEmployeeId(2);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateEmployee("Bearer " + token, 3, employee)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void activityRejectsInvalidEmployeeId() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_EMPLOYEE", new PermissionDTO("MANAGE_EMPLOYEE", true, false))
        );
        EmployeeController controller = new EmployeeController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getActivity("Bearer " + token, 0)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void forbidsAdminEscalationAdminModificationAndSelfRoleOrStatusChange() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        EmployeeBUS employeeBUS = mock(EmployeeBUS.class);
        EmployeeController controller = new EmployeeController(sessionStore, employeeBUS);

        EmployeeDTO manager = validEmployee(2, 2);
        String managerToken = sessionStore.createToken(
                manager,
                Map.of("MANAGE_EMPLOYEE", new PermissionDTO("MANAGE_EMPLOYEE", true, true))
        );

        // POST with roleId == 1 -> 403
        EmployeeDTO newAdmin = validEmployee(0, 1);
        ResponseStatusException postAdmin = assertThrows(
                ResponseStatusException.class,
                () -> controller.addEmployee("Bearer " + managerToken, newAdmin)
        );
        assertEquals(403, postAdmin.getStatusCode().value());

        // PUT self promoting to roleId == 1 -> 403
        EmployeeDTO promoteSelf = validEmployee(2, 1);
        ResponseStatusException putAdmin = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateEmployee("Bearer " + managerToken, 2, promoteSelf)
        );
        assertEquals(403, putAdmin.getStatusCode().value());

        // PUT modifying existing Admin (roleId == 1) -> 403
        when(employeeBUS.getEmployeeById(1)).thenReturn(validEmployee(1, 1));
        EmployeeDTO demoteAdmin = validEmployee(1, 3);
        demoteAdmin.setStatus(0);
        ResponseStatusException editAdmin = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateEmployee("Bearer " + managerToken, 1, demoteAdmin)
        );
        assertEquals(403, editAdmin.getStatusCode().value());

        // PUT changing own role or status -> 403
        when(employeeBUS.getEmployeeById(2)).thenReturn(validEmployee(2, 2));
        EmployeeDTO changeOwnRole = validEmployee(2, 3);
        ResponseStatusException selfRole = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateEmployee("Bearer " + managerToken, 2, changeOwnRole)
        );
        assertEquals(403, selfRole.getStatusCode().value());

        EmployeeDTO changeOwnStatus = validEmployee(2, 2);
        changeOwnStatus.setStatus(0);
        ResponseStatusException selfStatus = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateEmployee("Bearer " + managerToken, 2, changeOwnStatus)
        );
        assertEquals(403, selfStatus.getStatusCode().value());
    }

    @Test
    void validatesFinitePositiveSalaryAndRevokesSessionOnDeactivation() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        EmployeeBUS employeeBUS = mock(EmployeeBUS.class);
        EmployeeController controller = new EmployeeController(sessionStore, employeeBUS);

        String managerToken = sessionStore.createToken(
                validEmployee(2, 2),
                Map.of("MANAGE_EMPLOYEE", new PermissionDTO("MANAGE_EMPLOYEE", true, true))
        );
        String staffToken = sessionStore.createToken(validEmployee(5, 3), Map.of());

        EmployeeDTO badSalary = validEmployee(5, 3);
        badSalary.setSalaryFactor(0.0);
        ResponseStatusException zeroFactor = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateEmployee("Bearer " + managerToken, 5, badSalary)
        );
        assertEquals(400, zeroFactor.getStatusCode().value());

        badSalary.setSalaryFactor(Double.NaN);
        ResponseStatusException nanFactor = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateEmployee("Bearer " + managerToken, 5, badSalary)
        );
        assertEquals(400, nanFactor.getStatusCode().value());

        when(employeeBUS.getEmployeeById(5)).thenReturn(validEmployee(5, 3));
        when(employeeBUS.updateEmployee(any(EmployeeDTO.class))).thenReturn("Cập nhật thành công!");

        EmployeeDTO deactivateStaff = validEmployee(5, 3);
        deactivateStaff.setStatus(0);
        ResponseEntity<String> response = controller.updateEmployee("Bearer " + managerToken, 5, deactivateStaff);
        assertEquals(200, response.getStatusCode().value());
        assertNull(sessionStore.find(staffToken));
    }

    private static EmployeeDTO validEmployee(int employeeId, int roleId) {
        EmployeeDTO emp = new EmployeeDTO();
        emp.setEmployeeId(employeeId);
        emp.setEmployeeName("Nguyen Van A");
        emp.setEmployeePhone("0912345678");
        emp.setEmail("emp" + employeeId + "@example.com");
        emp.setBaseSalary(10_000_000);
        emp.setSalaryFactor(1.2);
        emp.setDayIn(Date.valueOf("2025-01-01"));
        emp.setStatus(1);
        emp.setRoleId(roleId);
        return emp;
    }
}
