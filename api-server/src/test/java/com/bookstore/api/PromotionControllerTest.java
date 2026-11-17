package com.bookstore.api;

import com.bookstore.api.contract.PromotionSaveRequestDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromotionControllerTest {
    @Test
    void promotionListingRequiresPromotionViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        PromotionController controller = new PromotionController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getPromotions("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void promotionCreationRequiresPromotionActionPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_PROMOTION", new PermissionDTO("MANAGE_PROMOTION", true, false))
        );
        PromotionController controller = new PromotionController(sessionStore);
        PromotionSaveRequestDTO request = new PromotionSaveRequestDTO(
                "",
                10,
                Timestamp.valueOf("2026-11-17 10:00:00"),
                Timestamp.valueOf("2026-12-17 10:00:00"),
                1,
                List.of(1)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createPromotion("Bearer " + token, request)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void rejectsInvalidPromotionBeforeLoadingBookData() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_PROMOTION", new PermissionDTO("MANAGE_PROMOTION", false, true))
        );
        PromotionController controller = new PromotionController(sessionStore);
        PromotionSaveRequestDTO request = new PromotionSaveRequestDTO(
                " ",
                10,
                Timestamp.valueOf("2026-11-17 10:00:00"),
                Timestamp.valueOf("2026-12-17 10:00:00"),
                1,
                List.of(1)
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createPromotion("Bearer " + token, request)
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
