package com.bookstore.api;

import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.PermissionBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSessionStoreTest {
    @Test
    void createsUniqueTokensAndRemovesSessions() {
        AuthSessionStore store = new AuthSessionStore();
        EmployeeDTO employee = new EmployeeDTO();

        String firstToken = store.createToken(employee, Map.of());
        String secondToken = store.createToken(employee, Map.of());

        assertNotEquals(firstToken, secondToken);
        assertNotNull(store.find(firstToken));

        store.remove(firstToken);

        assertNull(store.find(firstToken));
    }

    @Test
    void requiresTheRequestedActionAndReturnsItsSession() {
        AuthSessionStore store = new AuthSessionStore();
        EmployeeDTO employee = new EmployeeDTO();
        String token = store.createToken(
                employee,
                Map.of("MANAGE_IMPORT", new PermissionDTO("MANAGE_IMPORT", true, true))
        );
        AuthSessionStore.Session session = store.find(token);

        assertSame(session, store.requireAction("Bearer " + token, "MANAGE_IMPORT"));
        assertSame(session, store.requireAnyAction("Bearer " + token, "MANAGE_CUSTOMER", "MANAGE_IMPORT"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> store.requireAction("Bearer " + token, "MANAGE_IMPORT_TICKET")
        );
        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void authenticationFilterWhitelistsPublicEndpointsIncludingPasswordReset() {
        ApiAuthenticationFilter filter = new ApiAuthenticationFilter(new AuthSessionStore(), new ObjectMapper());

        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/auth/login")));
        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/auth/password-reset/request")));
        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("POST", "/api/auth/password-reset/confirm")));
        assertTrue(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/health")));
        assertFalse(filter.shouldNotFilter(new MockHttpServletRequest("GET", "/api/books")));
    }

    @Test
    void revokesSessionsByEmployeeIdAndRoleId() {
        AuthSessionStore store = new AuthSessionStore();
        EmployeeDTO emp2Role2 = employee(2, 2);
        EmployeeDTO emp3Role3 = employee(3, 3);
        EmployeeDTO emp4Role3 = employee(4, 3);

        String emp2TokenA = store.createToken(emp2Role2, Map.of());
        String emp2TokenB = store.createToken(emp2Role2, Map.of());
        String emp3Token = store.createToken(emp3Role3, Map.of());
        String emp4Token = store.createToken(emp4Role3, Map.of());

        store.revokeByEmployeeIdExcept(2, emp2TokenB);
        assertNull(store.find(emp2TokenA));
        assertNotNull(store.find(emp2TokenB));

        store.revokeByEmployeeId(2);
        assertNull(store.find(emp2TokenB));
        assertNotNull(store.find(emp3Token));
        assertNotNull(store.find(emp4Token));

        store.revokeByRoleId(3);
        assertNull(store.find(emp3Token));
        assertNull(store.find(emp4Token));
    }

    @Test
    void activeCheckerRevokesSessionImmediatelyWhenEmployeeBecomesInactive() {
        AtomicBoolean active = new AtomicBoolean(true);
        AuthSessionStore store = new AuthSessionStore(
                session -> active.get(),
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
                100,
                10
        );
        String token = store.createToken(employee(5, 3), Map.of());
        assertNotNull(store.find(token));

        active.set(false);
        assertNull(store.find(token));
        assertEquals(0, store.activeSessionCount());
    }

    @Test
    void enforcesPerEmployeeAndGlobalCapsAndEvictsExpiredSessions() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));
        Clock mutableClock = new Clock() {
            @Override
            public ZoneOffset getZone() { return ZoneOffset.UTC; }
            @Override
            public Clock withZone(java.time.ZoneId zone) { return this; }
            @Override
            public Instant instant() { return now.get(); }
        };

        AuthSessionStore store = new AuthSessionStore(null, mutableClock, 4, 2);
        String e1t1 = store.createToken(employee(1, 2), Map.of());
        String e1t2 = store.createToken(employee(1, 2), Map.of());
        String e1t3 = store.createToken(employee(1, 2), Map.of());

        // Per-employee cap = 2 evicts oldest (e1t1)
        assertNull(store.find(e1t1));
        assertNotNull(store.find(e1t2));
        assertNotNull(store.find(e1t3));

        // Global cap = 4 evicts oldest globally (e1t2)
        String e2t1 = store.createToken(employee(2, 3), Map.of());
        String e3t1 = store.createToken(employee(3, 3), Map.of());
        String e4t1 = store.createToken(employee(4, 3), Map.of());
        assertEquals(4, store.activeSessionCount());
        assertNull(store.find(e1t2));
        assertNotNull(store.find(e4t1));

        // Advance past 8h lifetime -> lazy sweep evicts expired sessions
        now.set(now.get().plus(Duration.ofHours(9)));
        assertNull(store.find(e4t1));
        assertEquals(0, store.activeSessionCount());
    }

    @Test
    void filterSetsSecurityHeadersAndReturns503OnDatabaseOutage() throws Exception {
        AuthSessionStore store = new AuthSessionStore(
                session -> { throw new IllegalStateException("DB down"); },
                Clock.systemUTC(),
                100,
                10
        );
        String token = store.createToken(employee(2, 2), Map.of());
        ApiAuthenticationFilter filter = new ApiAuthenticationFilter(store, new ObjectMapper());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(503, response.getStatus());
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void authControllerThrottlesRepeatedFailuresAndSeparatesDbErrorsFrom401() throws Exception {
        AccountBUS accountBUS = mock(AccountBUS.class);
        PermissionBUS permissionBUS = mock(PermissionBUS.class);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        AuthController controller = new AuthController(new AuthSessionStore(), accountBUS, permissionBUS, fixedClock);

        when(accountBUS.login("dbdown@example.com", "secret"))
                .thenThrow(new IllegalStateException("MySQL connection refused"));
        ResponseEntity<?> dbOutage = controller.login(new AuthController.LoginRequest("dbdown@example.com", "secret"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, dbOutage.getStatusCode());
        assertFalse(dbOutage.getBody().toString().contains("MySQL"));

        when(accountBUS.login("user@example.com", "wrong"))
                .thenThrow(new AccountBUS.AuthenticationException("Mật khẩu không chính xác!"));
        for (int i = 0; i < AuthController.MAX_FAILED_ATTEMPTS; i++) {
            ResponseEntity<?> fail = controller.login(new AuthController.LoginRequest("User@Example.COM", "wrong"));
            assertEquals(HttpStatus.UNAUTHORIZED, fail.getStatusCode());
        }
        ResponseEntity<?> throttled = controller.login(new AuthController.LoginRequest("user@example.com", "wrong"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, throttled.getStatusCode());
    }

    @Test
    void exceptionHandlerSanitizesSpringAndUnexpectedErrorsAndAuthorRejectsEmptyJsonWithout500() throws Exception {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<Map<String, String>> mismatch = handler.handleTypeMismatch(
                new MethodArgumentTypeMismatchException("abc", Integer.class, "id", null, new NumberFormatException()));
        assertEquals(400, mismatch.getStatusCode().value());
        assertFalse(mismatch.getBody().get("message").contains("Integer"));

        ResponseEntity<Map<String, String>> missing = handler.handleMissingParam(
                new MissingServletRequestParameterException("from", "LocalDate"));
        assertEquals(400, missing.getStatusCode().value());
        assertFalse(missing.getBody().get("message").contains("LocalDate"));

        ResponseEntity<Map<String, String>> methodNotAllowed = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("DELETE"));
        assertEquals(405, methodNotAllowed.getStatusCode().value());

        ResponseEntity<Map<String, String>> unexpected = handler.handleUnexpected(
                new NullPointerException("java.lang.String.trim() is null"));
        assertEquals(500, unexpected.getStatusCode().value());
        assertFalse(unexpected.getBody().get("message").contains("java.lang"));

        AuthSessionStore store = new AuthSessionStore();
        String token = store.createToken(employee(2, 2),
                Map.of("MANAGE_AUTHOR", new PermissionDTO("MANAGE_AUTHOR", true, true)));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AuthorController(store))
                .setControllerAdvice(handler).build();
        mvc.perform(post("/api/authors")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    private static EmployeeDTO employee(int employeeId, int roleId) {
        EmployeeDTO emp = new EmployeeDTO();
        emp.setEmployeeId(employeeId);
        emp.setRoleId(roleId);
        emp.setStatus(1);
        return emp;
    }
}
