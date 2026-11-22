package com.bookstore.api;

import com.bookstore.api.contract.PriceBulkUpdateRequestDTO;
import com.bookstore.api.contract.PriceUpdateRequestDTO;
import com.bookstore.bus.PriceBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.dto.PriceDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void rejectsOutOfBoundsAndNonFiniteProfitRates() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_PRICE", new PermissionDTO("MANAGE_PRICE", true, true))
        );
        PriceController controller = new PriceController(sessionStore);

        for (double badRate : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -0.01, 10.0, 1000.0}) {
            ResponseStatusException ex = assertThrows(
                    ResponseStatusException.class,
                    () -> controller.updateBookPrice("Bearer " + token, 1, new PriceUpdateRequestDTO(badRate))
            );
            assertEquals(400, ex.getStatusCode().value());
        }

        PriceBUS priceBUS = new PriceBUS();
        assertFalse(priceBUS.createNewPrice(1, 0, 0.4, 0));
        assertFalse(priceBUS.createNewPrice(1, 50_000, Double.NaN, 70_000));
        assertFalse(priceBUS.createNewPrice(1, 50_000, 100.0, 5_000_000));
        assertFalse(priceBUS.updateBulkPrice(List.of(new PriceDTO(1, 50_000, 0.4, 70_000, "A", "B", "C")), Double.POSITIVE_INFINITY));
    }

    @Test
    void rejectsSingleUpdateAndSkipsUnimportedBooksInBulkUpdateWhenBasePriceIsZero() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_PRICE", new PermissionDTO("MANAGE_PRICE", true, true))
        );
        PriceController controller = new PriceController(sessionStore);

        PriceDTO unimported = new PriceDTO(10, 0, 0, 100_000, "Unimported", "Author", "Cat");
        PriceDTO imported = new PriceDTO(11, 60_000, 0.5, 90_000, "Imported", "Author", "Cat");

        try (var busMock = mockConstruction(PriceBUS.class, (mock, context) -> {
            when(mock.getPriceDTOs()).thenReturn(List.of(unimported, imported));
            when(mock.updateBulkPrice(anyList(), anyDouble())).thenReturn(true);
        })) {
            ResponseStatusException singleEx = assertThrows(
                    ResponseStatusException.class,
                    () -> controller.updateBookPrice("Bearer " + token, 10, new PriceUpdateRequestDTO(0.4))
            );
            assertEquals(400, singleEx.getStatusCode().value());

            ResponseStatusException allZeroBulkEx = assertThrows(
                    ResponseStatusException.class,
                    () -> controller.updateBulkPrices("Bearer " + token, new PriceBulkUpdateRequestDTO(0.4, List.of(10)))
            );
            assertEquals(400, allZeroBulkEx.getStatusCode().value());

            assertEquals(204, controller.updateBulkPrices(
                    "Bearer " + token,
                    new PriceBulkUpdateRequestDTO(0.4, List.of(10, 11))
            ).getStatusCode().value());

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<PriceDTO>> listCaptor = ArgumentCaptor.forClass(List.class);
            verify(busMock.constructed().get(2)).updateBulkPrice(listCaptor.capture(), org.mockito.ArgumentMatchers.eq(0.4));
            assertEquals(1, listCaptor.getValue().size());
            assertEquals(11, listCaptor.getValue().get(0).getBookId());
        }
    }
}
