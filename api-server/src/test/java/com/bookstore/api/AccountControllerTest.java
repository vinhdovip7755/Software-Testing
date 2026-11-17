package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountControllerTest {
    @Test
    void accountListRequiresAccountViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        AccountController controller = new AccountController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getAccounts("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void updateRejectsInvalidEmployeeIdBeforeDatabaseAccess() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_ACCOUNT", new com.bookstore.dto.PermissionDTO("MANAGE_ACCOUNT", true, true))
        );
        AccountController controller = new AccountController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateAccount(
                        "Bearer " + token,
                        0,
                        new AccountController.UpdateAccountRequest(null, 1)
                )
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
