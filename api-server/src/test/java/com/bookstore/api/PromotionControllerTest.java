package com.bookstore.api;

import com.bookstore.api.contract.PromotionSaveRequestDTO;
import com.bookstore.bus.PromotionBUS;
import com.bookstore.dao.PromotionDAO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.dto.PromotionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void rejectsOverlongPromotionNameAndInvalidSearchType() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_PROMOTION", new PermissionDTO("MANAGE_PROMOTION", true, true))
        );
        PromotionController controller = new PromotionController(sessionStore);

        PromotionSaveRequestDTO longNameRequest = new PromotionSaveRequestDTO(
                "A".repeat(256),
                10,
                Timestamp.valueOf("2026-11-17 10:00:00"),
                Timestamp.valueOf("2026-12-17 10:00:00"),
                1,
                List.of(1)
        );
        ResponseStatusException nameEx = assertThrows(
                ResponseStatusException.class,
                () -> controller.createPromotion("Bearer " + token, longNameRequest)
        );
        assertEquals(400, nameEx.getStatusCode().value());

        ResponseStatusException typeEx = assertThrows(
                ResponseStatusException.class,
                () -> controller.searchPromotions("Bearer " + token, "sale", "INVALID_TYPE")
        );
        assertEquals(400, typeEx.getStatusCode().value());
    }

    @Test
    void getPromotionsDoesNotMutateDatabaseAndUpdateAutoStatusNeverReenablesDisabledPromotion() {
        Instant now = Instant.now();
        PromotionDTO manuallyDisabled = new PromotionDTO(
                1,
                "Disabled Active Window",
                15.0,
                Timestamp.from(now.minus(1, ChronoUnit.DAYS)),
                Timestamp.from(now.plus(5, ChronoUnit.DAYS)),
                0
        );
        PromotionDTO expiredActive = new PromotionDTO(
                2,
                "Expired Promo",
                20.0,
                Timestamp.from(now.minus(10, ChronoUnit.DAYS)),
                Timestamp.from(now.minus(1, ChronoUnit.DAYS)),
                1
        );

        try (var daoMock = mockConstruction(PromotionDAO.class,
                (mock, context) -> when(mock.selectAllPromotions()).thenReturn(List.of(manuallyDisabled, expiredActive)))) {
            AuthSessionStore sessionStore = new AuthSessionStore();
            String token = sessionStore.createToken(
                    new EmployeeDTO(),
                    Map.of("MANAGE_PROMOTION", new PermissionDTO("MANAGE_PROMOTION", true, false))
            );
            PromotionController controller = new PromotionController(sessionStore);

            List<PromotionDTO> fetched = controller.getPromotions("Bearer " + token);
            assertEquals(2, fetched.size());
            assertEquals(0, fetched.get(0).getStatus());
            assertEquals(0, fetched.get(1).getStatus());
            verify(daoMock.constructed().get(0), never()).updateStatus(any());

            expiredActive.setStatus(1);
            PromotionBUS promotionBUS = new PromotionBUS();
            promotionBUS.updateAutoStatus();
            assertEquals(0, manuallyDisabled.getStatus());
            assertEquals(0, expiredActive.getStatus());
            verify(daoMock.constructed().get(1), times(1)).updateStatus(expiredActive);
        }
    }

    @Test
    void savePromotionValidatesFieldsBeforeDatabaseAccess() {
        PromotionBUS promotionBUS = new PromotionBUS();
        Timestamp start = Timestamp.valueOf("2026-11-17 10:00:00");
        Timestamp end = Timestamp.valueOf("2026-12-17 10:00:00");

        assertFalse(promotionBUS.savePromotion(false, null, " ", 10, start, end, 1, List.of(1)));
        assertFalse(promotionBUS.savePromotion(false, null, "X".repeat(256), 10, start, end, 1, List.of(1)));
        assertFalse(promotionBUS.savePromotion(false, null, "Sale", 0, start, end, 1, List.of(1)));
        assertFalse(promotionBUS.savePromotion(false, null, "Sale", 101, start, end, 1, List.of(1)));
        assertFalse(promotionBUS.savePromotion(false, null, "Sale", Double.NaN, start, end, 1, List.of(1)));
        assertFalse(promotionBUS.savePromotion(false, null, "Sale", 10, end, start, 1, List.of(1)));
        assertFalse(promotionBUS.savePromotion(false, null, "Sale", 10, start, end, 1, List.of()));
        assertFalse(promotionBUS.savePromotion(false, null, "Sale", 10, start, end, 1, Arrays.asList(1, null)));
    }
}
