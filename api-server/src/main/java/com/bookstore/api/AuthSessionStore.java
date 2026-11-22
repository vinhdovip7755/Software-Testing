package com.bookstore.api;

import com.bookstore.dao.AccountDAO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Component
public class AuthSessionStore {
    private static final Duration SESSION_LIFETIME = Duration.ofHours(8);
    private static final Duration EVICTION_INTERVAL = Duration.ofMinutes(1);
    static final int DEFAULT_MAX_SESSIONS = 5000;
    static final int DEFAULT_MAX_SESSIONS_PER_EMPLOYEE = 10;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, StoredSession> sessions = new ConcurrentHashMap<>();
    private final Object sessionLock = new Object();
    private final AtomicLong sequence = new AtomicLong();
    private final AtomicReference<Instant> lastSweepAt;
    private final Predicate<Session> activeChecker;
    private final Clock clock;
    private final int maxGlobalSessions;
    private final int maxSessionsPerEmployee;

    @Autowired
    public AuthSessionStore(Environment environment) {
        this(AuthSessionStore::isSessionActiveInDb, Clock.systemUTC(),
                DEFAULT_MAX_SESSIONS, DEFAULT_MAX_SESSIONS_PER_EMPLOYEE);
    }

    public AuthSessionStore() {
        this(null, Clock.systemUTC(), DEFAULT_MAX_SESSIONS, DEFAULT_MAX_SESSIONS_PER_EMPLOYEE);
    }

    AuthSessionStore(
            Predicate<Session> activeChecker,
            Clock clock,
            int maxGlobalSessions,
            int maxSessionsPerEmployee
    ) {
        this.activeChecker = activeChecker;
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.maxGlobalSessions = Math.max(1, maxGlobalSessions);
        this.maxSessionsPerEmployee = Math.max(1, maxSessionsPerEmployee);
        this.lastSweepAt = new AtomicReference<>(this.clock.instant());
    }

    private static boolean isSessionActiveInDb(Session session) {
        if (session == null || session.employee() == null || session.employee().getEmployeeId() <= 0) {
            return false;
        }
        return new AccountDAO().isEmployeeAccountActiveWithRole(
                session.employee().getEmployeeId(),
                session.employee().getRoleId()
        );
    }

    public static String extractToken(String authorization) {
        return authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length())
                : null;
    }

    public Session find(String token) {
        Instant now = clock.instant();
        maybeSweepExpired(now);

        if (token == null || token.isBlank()) {
            return null;
        }
        StoredSession stored = sessions.get(token);
        if (stored == null) {
            return null;
        }
        Session session = stored.session();
        if (!session.expiresAt().isAfter(now)) {
            sessions.remove(token, stored);
            return null;
        }
        if (activeChecker != null && !activeChecker.test(session)) {
            sessions.remove(token, stored);
            return null;
        }
        return session;
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public void revokeByEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            return;
        }
        sessions.entrySet().removeIf(entry -> {
            EmployeeDTO employee = entry.getValue().session().employee();
            return employee != null && employee.getEmployeeId() == employeeId;
        });
    }

    public void revokeByEmployeeIdExcept(int employeeId, String keepToken) {
        if (employeeId <= 0) {
            return;
        }
        sessions.entrySet().removeIf(entry -> {
            if (keepToken != null && keepToken.equals(entry.getKey())) {
                return false;
            }
            EmployeeDTO employee = entry.getValue().session().employee();
            return employee != null && employee.getEmployeeId() == employeeId;
        });
    }

    public void revokeByRoleId(int roleId) {
        if (roleId <= 0) {
            return;
        }
        sessions.entrySet().removeIf(entry -> {
            EmployeeDTO employee = entry.getValue().session().employee();
            return employee != null && employee.getRoleId() == roleId;
        });
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
        String token = extractToken(authorization);
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
        if (employee == null) {
            throw new IllegalArgumentException("employee must not be null");
        }
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        Map<String, PermissionDTO> safePermissions = permissions != null ? Map.copyOf(permissions) : Map.of();

        synchronized (sessionLock) {
            Instant now = clock.instant();
            evictExpiredLocked(now);

            int employeeId = employee.getEmployeeId();
            if (employeeId > 0) {
                evictOldestForEmployeeLocked(employeeId);
            }
            evictOldestGlobalLocked();

            Session session = new Session(employee, safePermissions, now.plus(SESSION_LIFETIME));
            sessions.put(token, new StoredSession(token, session, sequence.incrementAndGet()));
            lastSweepAt.set(now);
        }
        return token;
    }

    int activeSessionCount() {
        return sessions.size();
    }

    private void maybeSweepExpired(Instant now) {
        Instant previous = lastSweepAt.get();
        if (previous.plus(EVICTION_INTERVAL).isBefore(now) && lastSweepAt.compareAndSet(previous, now)) {
            synchronized (sessionLock) {
                evictExpiredLocked(now);
            }
        }
    }

    private void evictExpiredLocked(Instant now) {
        sessions.entrySet().removeIf(entry -> !entry.getValue().session().expiresAt().isAfter(now));
    }

    private void evictOldestForEmployeeLocked(int employeeId) {
        List<StoredSession> employeeSessions = sessions.values().stream()
                .filter(stored -> stored.session().employee() != null
                        && stored.session().employee().getEmployeeId() == employeeId)
                .sorted(Comparator.comparingLong(StoredSession::order))
                .toList();
        int toRemove = employeeSessions.size() - maxSessionsPerEmployee + 1;
        for (int i = 0; i < toRemove && i < employeeSessions.size(); i++) {
            StoredSession oldest = employeeSessions.get(i);
            sessions.remove(oldest.token(), oldest);
        }
    }

    private void evictOldestGlobalLocked() {
        int excess = sessions.size() - maxGlobalSessions + 1;
        if (excess <= 0) {
            return;
        }
        List<StoredSession> oldestSessions = sessions.values().stream()
                .sorted(Comparator.comparingLong(StoredSession::order))
                .limit(excess)
                .toList();
        for (StoredSession oldest : oldestSessions) {
            sessions.remove(oldest.token(), oldest);
        }
    }

    private record StoredSession(String token, Session session, long order) {
    }

    public record Session(EmployeeDTO employee, Map<String, PermissionDTO> permissions, Instant expiresAt) {
    }
}
