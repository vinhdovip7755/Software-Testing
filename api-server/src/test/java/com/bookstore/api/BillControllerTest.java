package com.bookstore.api;

import com.bookstore.api.contract.BillCreateRequestDTO;
import com.bookstore.api.contract.BillCreateResponseDTO;
import com.bookstore.api.contract.BillLineItemRequestDTO;
import com.bookstore.bus.BillBUS;
import com.bookstore.bus.BookBUS;
import com.bookstore.bus.PromotionBUS;
import com.bookstore.bus.SystemParameterBUS;
import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void rejectsDuplicateBookIdsInBillDetails() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", true, true))
        );
        BillController controller = new BillController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createBill(
                        "Bearer " + token,
                        new BillCreateRequestDTO(
                                0,
                                1,
                                List.of(new BillLineItemRequestDTO(5, 1), new BillLineItemRequestDTO(5, 2))
                        )
                )
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void handlesDuplicateActiveBookRowsAndForcesZeroEarnedPointsForWalkInGuest() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        EmployeeDTO seller = new EmployeeDTO();
        seller.setEmployeeId(3);
        seller.setStatus(1);
        String token = sessionStore.createToken(
                seller,
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", true, true))
        );
        BillController controller = new BillController(sessionStore);

        BookDTO first = new BookDTO();
        first.setBookId(1);
        first.setBookName("Sách A");
        first.setSellingPrice(100_000);
        first.setStatus(1);

        BookDTO duplicate = new BookDTO();
        duplicate.setBookId(1);
        duplicate.setBookName("Sách A");
        duplicate.setSellingPrice(120_000);
        duplicate.setStatus(1);

        try (var books = mockConstruction(BookBUS.class,
                (mock, context) -> when(mock.selectActiveBooks()).thenReturn(List.of(first, duplicate)));
             var promotions = mockConstruction(PromotionBUS.class,
                     (mock, context) -> when(mock.getPromotionPercentByBookId(1)).thenReturn(0.0));
             var params = mockConstruction(SystemParameterBUS.class, (mock, context) -> {
                 when(mock.getVAT()).thenReturn(0.08);
                 when(mock.getEarnedPointsPer10K()).thenReturn(100);
             });
             var bills = mockConstruction(BillBUS.class,
                     (mock, context) -> when(mock.createBill(any(BillDTO.class), anyList())).thenReturn(77))) {

            ResponseEntity<BillCreateResponseDTO> response = controller.createBill(
                    "Bearer " + token,
                    new BillCreateRequestDTO(0, 1, List.of(new BillLineItemRequestDTO(1, 2)))
            );

            assertEquals(201, response.getStatusCode().value());
            assertNotNull(response.getBody());
            assertEquals(77, response.getBody().billId());
            assertEquals(0, response.getBody().earnedPoints());
            assertEquals("Khách lẻ", response.getBody().customerName());
            verify(bills.constructed().get(0)).createBill(any(BillDTO.class), anyList());
        }
    }

    @Test
    void billBusRejectsNegativeQuantityOrPricesBeforeDatabaseAccess() {
        BillBUS billBUS = new BillBUS();
        BillDTO validBill = new BillDTO(0, null, 50_000, 0.08, 3, 0, 1, 0);

        assertEquals(-1, billBUS.createBill(validBill, List.of(new BillDetailDTO(0, 1, "Book", -5, 10_000))));
        assertEquals(-1, billBUS.createBill(validBill, List.of(new BillDetailDTO(0, 1, "Book", 0, 10_000))));
        assertEquals(-1, billBUS.createBill(validBill, List.of(new BillDetailDTO(0, 1, "Book", 1, -100))));
        assertEquals(-1, billBUS.createBill(validBill, List.of(new BillDetailDTO(0, 1, "Book", 1, Double.NaN))));

        BillDTO negativeTotal = new BillDTO(0, null, -1, 0.08, 3, 0, 1, 0);
        assertEquals(-1, billBUS.createBill(negativeTotal, List.of(new BillDetailDTO(0, 1, "Book", 1, 10_000))));

        BillDTO negativePoints = new BillDTO(0, null, 50_000, 0.08, 3, 1, 1, -10);
        assertEquals(-1, billBUS.createBill(negativePoints, List.of(new BillDetailDTO(0, 1, "Book", 1, 10_000))));
    }
}
