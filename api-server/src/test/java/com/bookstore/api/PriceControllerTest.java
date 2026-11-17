package com.bookstore.api;

import com.bookstore.api.contract.PriceBulkUpdateRequestDTO;
import com.bookstore.api.contract.PriceUpdateRequestDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceControllerTest {
    @Test
    void priceListingRequiresPriceViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        PriceController controller = new PriceController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getPrices("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void priceUpdatesRequirePriceActionPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_PRICE", new PermissionDTO("MANAGE_PRICE", true, false))
        );
        PriceController controller = new PriceController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateBookPrice("Bearer " + token, 1, new PriceUpdateRequestDTO(0.2))
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void rejectsInvalidBulkPriceBeforeLoadingPrices() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_PRICE", new PermissionDTO("MANAGE_PRICE", false, true))
        );
        PriceController controller = new PriceController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateBulkPrices(
                        "Bearer " + token,
                        new PriceBulkUpdateRequestDTO(-0.1, List.of(1))
                )
        );

        assertEquals(400, exception.getStatusCode().value());
    }
}
