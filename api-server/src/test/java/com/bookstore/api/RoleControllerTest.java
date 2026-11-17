package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleControllerTest {
    @Test
    void roleEndpointsRequireRoleViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        RoleController controller = new RoleController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getRoles("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void defaultRoleCannotBeEdited() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_ROLE", new PermissionDTO("MANAGE_ROLE", true, true))
        );
        RoleController controller = new RoleController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateRole("Bearer " + token, 1, new RoleController.RoleSaveRequest("Admin", null))
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
