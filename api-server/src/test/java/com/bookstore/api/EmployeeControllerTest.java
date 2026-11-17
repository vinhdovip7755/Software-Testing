package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
