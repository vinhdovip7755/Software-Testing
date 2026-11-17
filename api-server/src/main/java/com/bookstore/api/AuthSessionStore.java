package com.bookstore.api;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthSessionStore {
    private static final Duration SESSION_LIFETIME = Duration.ofHours(8);

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public Session find(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null) {
            return null;
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token, session);
            return null;
        }
        return session;
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public Session requireAction(String authorization, String actionCode) {
        return requirePermission(authorization, false, actionCode);
    }

    public Session requireAnyAction(String authorization, String... actionCodes) {
        return requirePermission(authorization, false, actionCodes);
    }

    public void requireAnyView(String authorization, String... actionCodes) {
        requirePermission(authorization, true, actionCodes);
    }

    public Session requireAuthenticated(String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length())
                : null;
        Session session = find(token);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Phiên đăng nhập không hợp lệ hoặc đã hết hạn.");
        }
        return session;
    }

    private Session requirePermission(String authorization, boolean view, String... actionCodes) {
        Session session = requireAuthenticated(authorization);
        boolean allowed = false;
        for (String actionCode : actionCodes) {
            PermissionDTO permission = session.permissions().get(actionCode);
            if (permission != null && (view ? permission.isView() : permission.isAction())) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
        }
        return session;
    }

    public String createToken(EmployeeDTO employee, Map<String, PermissionDTO> permissions) {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        sessions.put(token, new Session(employee, Map.copyOf(permissions), Instant.now().plus(SESSION_LIFETIME)));
        return token;
    }

    public record Session(EmployeeDTO employee, Map<String, PermissionDTO> permissions, Instant expiresAt) {
    }

}
