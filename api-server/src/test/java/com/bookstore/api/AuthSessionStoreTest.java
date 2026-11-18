package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
