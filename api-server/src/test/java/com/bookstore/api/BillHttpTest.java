package com.bookstore.api;

import com.bookstore.bus.BookBUS;
import com.bookstore.bus.PromotionBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BillHttpTest {
    private MockMvc mvc;
    private String authorization;
    private AuthSessionStore sessions;

    @BeforeEach
    void setUp() {
        sessions = new AuthSessionStore();
        authorization = "Bearer " + sessions.createToken(new EmployeeDTO(),
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", true, true)));
        mvc = MockMvcBuilders.standaloneSetup(new BillController(sessions))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }

    private static final String VALID_BILL = """
            {"customerId":0,"paymentMethodId":1,"details":[{"bookId":1,"quantity":1}]}
            """;

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mvc.perform(post("/api/bills").contentType(MediaType.APPLICATION_JSON).content(VALID_BILL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Bearer ", "Bearer forged-token", "Basic forged-token"})
    void rejectsInvalidAuthorization(String header) throws Exception {
        mvc.perform(post("/api/bills").header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BILL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsLoggedOutSession() throws Exception {
        sessions.remove(authorization.substring("Bearer ".length()));
        mvc.perform(post("/api/bills").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BILL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void warehouseEmployeeCannotForgeSellingPermissionInBody() throws Exception {
        String token = sessions.createToken(new EmployeeDTO(),
                Map.of("MANAGE_IMPORT", new PermissionDTO("MANAGE_IMPORT", true, true)));
        mvc.perform(post("/api/bills").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BILL.replace("\"customerId\":0", """
                                "customerId":0,"roleId":1,"employeeId":1,
                                "permissions":{"MANAGE_SELLING":{"action":true,"view":true}}
                                """)))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewOnlySellingPermissionCannotCreateBill() throws Exception {
        String token = sessions.createToken(new EmployeeDTO(),
                Map.of("MANAGE_SELLING", new PermissionDTO("MANAGE_SELLING", true, false)));
        mvc.perform(post("/api/bills").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BILL))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"customerId\":-1,\"paymentMethodId\":1,\"details\":[{\"bookId\":1,\"quantity\":1}]}",
            "{\"paymentMethodId\":1,\"details\":[null]}",
            "{\"paymentMethodId\":1,\"details\":[{\"bookId\":1,\"quantity\":-1}]}",
            "{\"paymentMethodId\":1,\"details\":[{\"bookId\":1,\"quantity\":2147483648}]}",
            "{\"paymentMethodId\":1,\"details\":[{\"bookId\":1,\"quantity\":1},{\"bookId\":1,\"quantity\":-1}]}"
    })
    void rejectsAbusiveCartInput(String body) throws Exception {
        mvc.perform(post("/api/bills").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1, 999999, Integer.MAX_VALUE})
    void rejectsUnsupportedPointSpendingBeforeBusinessAccess(int points) throws Exception {
        mvc.perform(post("/api/bills").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":1,"paymentMethodId":1,
                                 "details":[{"bookId":1,"quantity":1}],"pointsUsed":%d}
                                """.formatted(points)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Số điểm thưởng vượt giới hạn."));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "0.9", "-1.5", "1.0", "\"1.5\"", "true", "[1]", "2147483648"})
    void rejectsNonIntegerQuantitiesBeforeAccessingBusinessData(String quantity) throws Exception {
        try (var books = mockConstruction(BookBUS.class)) {
            mvc.perform(post("/api/bills").header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"customerId":0,"paymentMethodId":1,
                                     "details":[{"bookId":1,"quantity":%s}]}
                                    """.formatted(quantity)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty());
            org.junit.jupiter.api.Assertions.assertTrue(books.constructed().isEmpty());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ",\"pointsUsed\":0"})
    void inactiveBookReturnsBusinessMessageWithLegacyAndExplicitZeroPoints(String points) throws Exception {
        try (var books = mockConstruction(BookBUS.class,
                (mock, context) -> when(mock.selectActiveBooks()).thenReturn(List.of()));
             var promotions = mockConstruction(PromotionBUS.class)) {
            mvc.perform(post("/api/bills").header("Authorization", authorization)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"customerId":0,"paymentMethodId":1,
                                     "details":[{"bookId":8,"quantity":1}]%s}
                                    """.formatted(points)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Sách không tồn tại hoặc đã ngừng bán."));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"details\":[{\"bookId\":,\"quantity\":1}]}",
            "{\"pointsUsed\":2147483648}",
            "{\"pointsUsed\":\"invalid\"}"
    })
    void malformedJsonAndInvalidTypesReturnReadableMessage(String body) throws Exception {
        mvc.perform(post("/api/bills").header("Authorization", authorization)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dữ liệu JSON không hợp lệ hoặc sai kiểu dữ liệu."));
    }
}
