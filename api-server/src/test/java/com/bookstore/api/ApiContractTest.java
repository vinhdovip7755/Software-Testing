package com.bookstore.api;

import com.bookstore.api.contract.ApiSessionDTO;
import com.bookstore.api.contract.AccountViewDTO;
import com.bookstore.api.contract.BillCreateRequestDTO;
import com.bookstore.api.contract.BillCreateResponseDTO;
import com.bookstore.api.contract.BillLineItemRequestDTO;
import com.bookstore.api.contract.BillReceiptLineDTO;
import com.bookstore.api.contract.BookPromotionDTO;
import com.bookstore.api.contract.PromotionSaveRequestDTO;
import com.bookstore.api.contract.CustomerPurchaseSummaryDTO;
import com.bookstore.api.contract.ImportCreateRequestDTO;
import com.bookstore.api.contract.PriceBulkUpdateRequestDTO;
import com.bookstore.api.contract.PriceUpdateRequestDTO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.FinancialQuarterSummaryDTO;
import com.bookstore.dto.FinancialStatsDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.dto.PromotionDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiContractTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void roundTripsSessionPermissionsAndBookAuthorIds() throws Exception {
        ApiSessionDTO session = new ApiSessionDTO(
                "token",
                new EmployeeDTO(),
                Map.of("MANAGE_PRODUCT", new PermissionDTO("MANAGE_PRODUCT", true, true))
        );
        ApiSessionDTO decodedSession = objectMapper.readValue(
                objectMapper.writeValueAsString(session),
                ApiSessionDTO.class
        );

        assertEquals("token", decodedSession.getAccessToken());
        assertTrue(decodedSession.getPermissions().get("MANAGE_PRODUCT").isAction());
        assertTrue(decodedSession.getPermissions().get("MANAGE_PRODUCT").isView());

        BookDTO book = new BookDTO();
        book.setAuthorIdsList(List.of(3, 5));
        BookDTO decodedBook = objectMapper.readValue(objectMapper.writeValueAsString(book), BookDTO.class);
        assertEquals(List.of(3, 5), decodedBook.getAuthorIdsList());
    }

    @Test
    void roundTripsImportCreateRequest() throws Exception {
        ImportTicketDTO ticket = new ImportTicketDTO();
        ticket.setSupplierID(7);
        ticket.setCreatedDate(Timestamp.valueOf("2026-11-17 19:00:00"));
        ImportDetailDTO detail = new ImportDetailDTO(0, 3, 4, 12000, 20000, 40);
        ImportCreateRequestDTO request = new ImportCreateRequestDTO(ticket, List.of(detail));

        ImportCreateRequestDTO decoded = objectMapper.readValue(
                objectMapper.writeValueAsString(request),
                ImportCreateRequestDTO.class
        );

        assertEquals(7, decoded.getTicket().getSupplierID());
        assertEquals(ticket.getCreatedDate(), decoded.getTicket().getCreatedDate());
        assertEquals(3, decoded.getDetails().get(0).getBookID());
        assertEquals(4, decoded.getDetails().get(0).getQuantity());

        CustomerPurchaseSummaryDTO summary = new CustomerPurchaseSummaryDTO(
                12,
                98765.5,
                Timestamp.valueOf("2026-11-17 19:00:00")
        );
        CustomerPurchaseSummaryDTO decodedSummary = objectMapper.readValue(
                objectMapper.writeValueAsString(summary),
                CustomerPurchaseSummaryDTO.class
        );
        assertEquals(12, decodedSummary.getTotalBooks());
        assertEquals(98765.5, decodedSummary.getTotalSpent());
        assertEquals(summary.getLastPurchase(), decodedSummary.getLastPurchase());
    }

    @Test
    void roundTripsBillCreateContracts() throws Exception {
        BillCreateRequestDTO request = new BillCreateRequestDTO(
                7,
                2,
                List.of(new BillLineItemRequestDTO(11, 3))
        );
        BillCreateRequestDTO decodedRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(request),
                BillCreateRequestDTO.class
        );
        assertEquals(7, decodedRequest.customerId());
        assertEquals(2, decodedRequest.paymentMethodId());
        assertEquals(11, decodedRequest.details().get(0).bookId());
        assertEquals(3, decodedRequest.details().get(0).quantity());

        BillCreateResponseDTO response = new BillCreateResponseDTO(
                101,
                125000,
                0.08,
                12,
                "Nguyễn An",
                List.of(new BillReceiptLineDTO(11, "Sách", 3, 50000, 10, 135000))
        );
        BillCreateResponseDTO decodedResponse = objectMapper.readValue(
                objectMapper.writeValueAsString(response),
                BillCreateResponseDTO.class
        );
        assertEquals(101, decodedResponse.billId());
        assertEquals(125000, decodedResponse.totalBillPrice());
        assertEquals(0.08, decodedResponse.tax());
        assertEquals(12, decodedResponse.earnedPoints());
        assertEquals("Nguyễn An", decodedResponse.customerName());
        assertEquals("Sách", decodedResponse.details().get(0).bookName());
        assertEquals(135000, decodedResponse.details().get(0).totalPrice());

        BookPromotionDTO promotion = new BookPromotionDTO(11, "Khuyến mãi sách", 10);
        BookPromotionDTO decodedPromotion = objectMapper.readValue(
                objectMapper.writeValueAsString(promotion),
                BookPromotionDTO.class
        );
        assertEquals(promotion, decodedPromotion);
    }

    @Test
    void roundTripsPromotionSaveRequest() throws Exception {
        Timestamp start = Timestamp.valueOf("2026-11-17 10:00:00");
        Timestamp end = Timestamp.valueOf("2026-12-17 10:00:00");
        PromotionSaveRequestDTO request = new PromotionSaveRequestDTO(
                "Ưu đãi tháng 11",
                15,
                start,
                end,
                1,
                List.of(3, 5)
        );

        PromotionSaveRequestDTO decoded = objectMapper.readValue(
                objectMapper.writeValueAsString(request),
                PromotionSaveRequestDTO.class
        );

        assertEquals(request, decoded);
    }

    @Test
    void roundTripsFinancialStatisticsResponses() throws Exception {
        FinancialStatsDTO stats = new FinancialStatsDTO("17/11/2026", 100, 40, 25);
        FinancialStatsDTO decodedStats = objectMapper.readValue(
                objectMapper.writeValueAsString(stats),
                FinancialStatsDTO.class
        );
        assertEquals(stats.getThoiGian(), decodedStats.getThoiGian());
        assertEquals(stats.getDoanhThu(), decodedStats.getDoanhThu());
        assertEquals(stats.getChiPhi(), decodedStats.getChiPhi());
        assertEquals(stats.getVonNhapHang(), decodedStats.getVonNhapHang());
        assertEquals(stats.getLoiNhuan(), decodedStats.getLoiNhuan());

        FinancialQuarterSummaryDTO summary = new FinancialQuarterSummaryDTO(2026, 2, 500, 1, 200);
        FinancialQuarterSummaryDTO decodedSummary = objectMapper.readValue(
                objectMapper.writeValueAsString(summary),
                FinancialQuarterSummaryDTO.class
        );
        assertTrue(decodedSummary.isHasData());
        assertEquals(summary.getYear(), decodedSummary.getYear());
        assertEquals(summary.getHighestRevenueQuarter(), decodedSummary.getHighestRevenueQuarter());
        assertEquals(summary.getHighestQuarterRevenue(), decodedSummary.getHighestQuarterRevenue());
        assertEquals(summary.getLowestRevenueQuarter(), decodedSummary.getLowestRevenueQuarter());
        assertEquals(summary.getLowestQuarterRevenue(), decodedSummary.getLowestQuarterRevenue());
    }

    @Test
    void roundTripsPriceUpdateRequests() throws Exception {
        PriceUpdateRequestDTO singleRequest = new PriceUpdateRequestDTO(0.25);
        PriceUpdateRequestDTO decodedSingleRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(singleRequest),
                PriceUpdateRequestDTO.class
        );
        assertEquals(singleRequest, decodedSingleRequest);

        PriceBulkUpdateRequestDTO bulkRequest = new PriceBulkUpdateRequestDTO(0.3, List.of(3, 5));
        PriceBulkUpdateRequestDTO decodedBulkRequest = objectMapper.readValue(
                objectMapper.writeValueAsString(bulkRequest),
                PriceBulkUpdateRequestDTO.class
        );
        assertEquals(bulkRequest, decodedBulkRequest);
    }

    @Test
    void accountResponseContractDoesNotExposePassword() throws Exception {
        AccountViewDTO account = new AccountViewDTO(7, "staff@example.com", 1, "Nguyễn An", 3, "Nhân viên");
        var json = objectMapper.readTree(objectMapper.writeValueAsString(account));

        assertFalse(json.has("password"));
        assertEquals("staff@example.com", json.get("username").asText());
    }

    @Test
    void roundTripsPromotionDtoAndExcludesFormattedDateRange() throws Exception {
        ObjectMapper springBootMapper = new ObjectMapper()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        ObjectMapper clientMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Timestamp start = Timestamp.valueOf("2026-11-17 10:00:00");
        Timestamp end = Timestamp.valueOf("2026-12-17 10:00:00");
        PromotionDTO promotion = new PromotionDTO(5, "Black Friday", 20.0, start, end, 1);

        String json = springBootMapper.writeValueAsString(promotion);
        var jsonTree = springBootMapper.readTree(json);
        assertFalse(jsonTree.has("formattedDateRange"));
        assertTrue(jsonTree.get("startDate").isTextual());
        assertTrue(jsonTree.get("endDate").isTextual());

        PromotionDTO decoded = clientMapper.readValue(json, PromotionDTO.class);
        assertEquals(5, decoded.getPromotionId());
        assertEquals("Black Friday", decoded.getPromotionName());
        assertEquals(20.0, decoded.getPercent());
        assertEquals(start, decoded.getStartDate());
        assertEquals(end, decoded.getEndDate());
        assertEquals(1, decoded.getStatus());
        assertEquals("17/11/2026 - 17/12/2026", decoded.getFormattedDateRange());

        PromotionDTO nullStart = new PromotionDTO(6, "No Start", 10.0, null, end, 1);
        PromotionDTO nullEnd = new PromotionDTO(7, "No End", 10.0, start, null, 1);
        PromotionDTO bothNull = new PromotionDTO(8, "No Dates", 10.0, null, null, 0);

        assertEquals("", nullStart.getFormattedDateRange());
        assertEquals("", nullEnd.getFormattedDateRange());
        assertEquals("", bothNull.getFormattedDateRange());

        String bothNullJson = springBootMapper.writeValueAsString(bothNull);
        assertFalse(springBootMapper.readTree(bothNullJson).has("formattedDateRange"));
        PromotionDTO decodedNullDates = clientMapper.readValue(bothNullJson, PromotionDTO.class);
        assertNull(decodedNullDates.getStartDate());
        assertNull(decodedNullDates.getEndDate());
        assertEquals("", decodedNullDates.getFormattedDateRange());
    }

    @Test
    void financialStatsBusNormalizesSameDayTimestampsBeforeComparison() {
        com.bookstore.bus.FinancialStatsBUS bus = new com.bookstore.bus.FinancialStatsBUS();
        java.util.Date fromAfternoon = Timestamp.valueOf("2026-09-25 18:30:00");
        java.util.Date toMorningSameDay = Timestamp.valueOf("2026-09-25 08:00:00");

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> bus.validateThongKeTheoNgay(fromAfternoon, toMorningSameDay)
        );
    }

    @Test
    void importBusRejectsInvalidDetailsAndDuplicateBooksBeforeDatabaseAccess() {
        com.bookstore.bus.ImportBUS bus = new com.bookstore.bus.ImportBUS();
        ImportTicketDTO ticket = new ImportTicketDTO(2, 1, 100_000);

        assertFalse(bus.importBooks(ticket, new ImportDetailDTO[0]));
        assertFalse(bus.importBooks(ticket, new ImportDetailDTO[]{
                new ImportDetailDTO(0, 1, 10, 50_000, 90_000, 40),
                new ImportDetailDTO(0, 1, 5, 45_000, 90_000, 50)
        }));
        assertFalse(bus.importBooks(ticket, new ImportDetailDTO[]{
                new ImportDetailDTO(0, 1, Integer.MAX_VALUE, 1, 10, 0),
                new ImportDetailDTO(0, 2, 1, 1, 10, 0)
        }));
        assertFalse(bus.importBooks(ticket, new ImportDetailDTO[]{
                new ImportDetailDTO(0, 1, -1, 50_000, 90_000, 40)
        }));
    }

    @Test
    void databaseConnectionThrowsIllegalStateExceptionInsteadOfReturningNull() {
        String previousUrl = System.getProperty("bookstore.db.url");
        System.setProperty("bookstore.db.url", "jdbc:invalid://127.0.0.1:1/nonexistent");
        try {
            IllegalStateException ex = org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalStateException.class,
                    com.bookstore.util.DatabaseConnection::getConnection
            );
            assertEquals("Không thể kết nối cơ sở dữ liệu.", ex.getMessage());
        } finally {
            if (previousUrl == null) {
                System.clearProperty("bookstore.db.url");
            } else {
                System.setProperty("bookstore.db.url", previousUrl);
            }
        }
    }
}
