package com.bookstore.api;

import com.bookstore.api.contract.BillCreateRequestDTO;
import com.bookstore.api.contract.BillLineItemRequestDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillControllerTest {
    @Test
    void rejectsBillCreationWithoutSellingPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        BillController controller = new BillController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createBill(
                        "Bearer " + token,
                        new BillCreateRequestDTO(0, 1, List.of(new BillLineItemRequestDTO(1, 1)))
                )
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void rejectsInvalidBillInputBeforeAccessingBusinessData() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", false, true))
        );
        BillController controller = new BillController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createBill(
                        "Bearer " + token,
                        new BillCreateRequestDTO(-1, 1, List.of(new BillLineItemRequestDTO(1, 1)))
                )
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void billHistoryRequiresBillViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", true, true))
        );
        BillController controller = new BillController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getBills("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }
}
