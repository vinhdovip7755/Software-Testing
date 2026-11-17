package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryControllerTest {
    @Test
    void inventoryEndpointsRequireInventoryViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        InventoryController controller = new InventoryController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getInventoryLogs("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void rejectsInvalidBookIdBeforeLoadingLots() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_INVENTORY", new PermissionDTO("MANAGE_INVENTORY", true, false))
        );
        InventoryController controller = new InventoryController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getBookLots("Bearer " + token, 0)
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
