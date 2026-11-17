package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SellingSupportControllerTest {
    @Test
    void promotionLookupRequiresSellingViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        PromotionController controller = new PromotionController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getPromotionForBook("Bearer " + token, 1)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void rejectsInvalidPromotionLookupForAuthorizedSeller() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", true, false))
        );
        PromotionController controller = new PromotionController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getPromotionForBook("Bearer " + token, 0)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void vatLookupRequiresSellingViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        SystemParameterController controller = new SystemParameterController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getVatRate("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }
}
