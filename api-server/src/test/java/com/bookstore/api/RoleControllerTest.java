package com.bookstore.api;

import com.bookstore.api.contract.RoleActionPermissionDTO;
import com.bookstore.bus.ActionBUS;
import com.bookstore.bus.PermissionBUS;
import com.bookstore.bus.RoleBUS;
import com.bookstore.dao.RoleDAO;
import com.bookstore.dto.ActionDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.dto.RoleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void createRoleEnforcesRbacBeforeValidation() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(employee(3, 3), Map.of());
        RoleController controller = new RoleController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createRole("Bearer " + token, new RoleController.RoleSaveRequest("", List.of()))
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void blocksSelfRoleEditAndRevokesRoleSessionsOnAtomicUpdate() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        RoleBUS roleBUS = mock(RoleBUS.class);
        ActionBUS actionBUS = mock(ActionBUS.class);
        PermissionBUS permissionBUS = mock(PermissionBUS.class);
        RoleDAO roleDAO = mock(RoleDAO.class);
        RoleController controller = new RoleController(sessionStore, roleBUS, actionBUS, permissionBUS, roleDAO);

        String customAdminToken = sessionStore.createToken(
                employee(2, 2),
                Map.of("MANAGE_ROLE", new PermissionDTO("MANAGE_ROLE", true, true))
        );
        String adminToken = sessionStore.createToken(
                employee(1, 1),
                Map.of("MANAGE_ROLE", new PermissionDTO("MANAGE_ROLE", true, true))
        );
        String salesToken = sessionStore.createToken(employee(3, 3), Map.of());

        RoleActionPermissionDTO perm = new RoleActionPermissionDTO(1, "MANAGE_SELLING", "Bán hàng", true, false);
        RoleController.RoleSaveRequest req = new RoleController.RoleSaveRequest("Bán hàng mới", List.of(perm));

        // Caller editing their own role (roleId == 2) -> 403
        ResponseStatusException selfRole = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateRole("Bearer " + customAdminToken, 2, req)
        );
        assertEquals(403, selfRole.getStatusCode().value());

        // Admin updating role 3 commits atomically and revokes all role 3 sessions
        when(actionBUS.selectAllActions()).thenReturn(List.of(new ActionDTO(1, "MANAGE_SELLING", "Bán hàng")));
        when(roleDAO.updateRoleWithPermissions(any(RoleDTO.class), anyList(), anyList())).thenReturn(true);

        ResponseEntity<String> response = controller.updateRole("Bearer " + adminToken, 3, req);
        assertEquals(200, response.getStatusCode().value());
        assertNull(sessionStore.find(salesToken));
        assertNotNull(sessionStore.find(adminToken));
    }

    private static EmployeeDTO employee(int employeeId, int roleId) {
        EmployeeDTO emp = new EmployeeDTO();
        emp.setEmployeeId(employeeId);
        emp.setRoleId(roleId);
        emp.setStatus(1);
        return emp;
    }
}
