package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatisticsControllerTest {
    @Test
    void statisticsEndpointsRequireViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        StatisticsController controller = new StatisticsController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getTotalFinancialStats("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void rejectsReversedDateRangeBeforeQueryingStatistics() {
        AuthSessionStore sessionStore = authorizedSessionStore();
        StatisticsController controller = new StatisticsController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getFinancialStatsByDay(
                        authorizedToken(sessionStore),
                        LocalDate.of(2026, 11, 18),
                        LocalDate.of(2026, 11, 17)
                )
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void rejectsQuarterOutsideSupportedRange() {
        AuthSessionStore sessionStore = authorizedSessionStore();
        StatisticsController controller = new StatisticsController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getTopSellingProducts(authorizedToken(sessionStore), 2026, 5)
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    private AuthSessionStore authorizedSessionStore() {
        return new AuthSessionStore();
    }

    private String authorizedToken(AuthSessionStore sessionStore) {
        return "Bearer " + sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("STATISTICS", new PermissionDTO("STATISTICS", true, false))
        );
    }
}
