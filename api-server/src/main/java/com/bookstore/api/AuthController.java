package com.bookstore.api;

import com.bookstore.api.contract.ApiSessionDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.PermissionBUS;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    static final int MAX_FAILED_ATTEMPTS = 10;
    static final Duration THROTTLE_WINDOW = Duration.ofSeconds(60);
    private static final int MAX_TRACKED_KEYS = 2000;

    private final AccountBUS accountBUS;
    private final PermissionBUS permissionBUS;
    private final AuthSessionStore sessionStore;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> failedAttempts = new ConcurrentHashMap<>();

    @Autowired
    public AuthController(AuthSessionStore sessionStore) {
        this(sessionStore, new AccountBUS(), new PermissionBUS(), Clock.systemUTC());
    }

    AuthController(AuthSessionStore sessionStore, AccountBUS accountBUS, PermissionBUS permissionBUS, Clock clock) {
        this.sessionStore = sessionStore;
        this.accountBUS = accountBUS;
        this.permissionBUS = permissionBUS;
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String trimmedUsername = request.username().trim();
        String throttleKey = trimmedUsername.toLowerCase(Locale.ROOT);
        Instant now = clock.instant();

        if (isThrottled(throttleKey, now)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Quá nhiều lần đăng nhập thất bại. Vui lòng thử lại sau 1 phút."));
        }

        try {
            EmployeeDTO employee = accountBUS.login(trimmedUsername, request.password());
            if (employee == null) {
                recordFailure(throttleKey, now);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Tài khoản không gắn với nhân viên hợp lệ."));
            }
            Map<String, PermissionDTO> permissions = permissionBUS.getPermissionsByRoleId(employee.getRoleId());
            String token = sessionStore.createToken(employee, permissions);
            failedAttempts.remove(throttleKey);
            return ResponseEntity.ok(new ApiSessionDTO(token, employee, permissions));
        } catch (AccountBUS.AuthenticationException e) {
            recordFailure(throttleKey, now);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage() == null ? "Đăng nhập thất bại." : e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Hệ thống tạm thời không thể kết nối cơ sở dữ liệu. Vui lòng thử lại sau."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            sessionStore.remove(authorization.substring("Bearer ".length()));
        }
        return ResponseEntity.noContent().build();
    }

    private boolean isThrottled(String throttleKey, Instant now) {
        Deque<Instant> timestamps = failedAttempts.get(throttleKey);
        if (timestamps == null) {
            return false;
        }
        Instant cutoff = now.minus(THROTTLE_WINDOW);
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(cutoff)) {
                timestamps.removeFirst();
            }
            if (timestamps.isEmpty()) {
                failedAttempts.remove(throttleKey, timestamps);
                return false;
            }
            return timestamps.size() >= MAX_FAILED_ATTEMPTS;
        }
    }

    private void recordFailure(String throttleKey, Instant now) {
        if (failedAttempts.size() > MAX_TRACKED_KEYS) {
            Instant cutoff = now.minus(THROTTLE_WINDOW);
            failedAttempts.entrySet().removeIf(entry -> {
                Deque<Instant> q = entry.getValue();
                synchronized (q) {
                    while (!q.isEmpty() && !q.peekFirst().isAfter(cutoff)) {
                        q.removeFirst();
                    }
                    return q.isEmpty();
                }
            });
        }
        Deque<Instant> timestamps = failedAttempts.computeIfAbsent(throttleKey, key -> new ArrayDeque<>());
        Instant cutoff = now.minus(THROTTLE_WINDOW);
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && !timestamps.peekFirst().isAfter(cutoff)) {
                timestamps.removeFirst();
            }
            timestamps.addLast(now);
        }
    }

    public record LoginRequest(
            @NotBlank(message = "Email không được để trống.") String username,
            @NotBlank(message = "Mật khẩu không được để trống.") String password
    ) {
    }
}
