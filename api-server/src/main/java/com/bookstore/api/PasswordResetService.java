package com.bookstore.api;

import com.bookstore.bus.AccountBUS;
import com.bookstore.dto.AccountDTO;
import com.bookstore.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Component
public class PasswordResetService {
    private static final Duration OTP_LIFETIME = Duration.ofMinutes(2);
    private static final int MAX_ATTEMPTS = 5;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final Predicate<String> accountExists;
    private final BiFunction<String, String, String> passwordResetter;
    private final BiPredicate<String, String> otpSender;
    private final Supplier<String> otpGenerator;
    private final Clock clock;
    private final Consumer<String> onPasswordReset;
    private final ConcurrentHashMap<String, ResetCode> codes = new ConcurrentHashMap<>();

    public PasswordResetService() {
        this(null);
    }

    @Autowired
    public PasswordResetService(AuthSessionStore sessionStore) {
        AccountBUS accountBUS = new AccountBUS();
        this.accountExists = accountBUS::isEmailExists;
        this.passwordResetter = accountBUS::resetPasswordByEmail;
        String otpTestOut = System.getProperty("bookstore.otp.test-out");
        if (otpTestOut != null && !otpTestOut.isBlank()) {
            Path otpPath = Path.of(otpTestOut);
            this.otpSender = (email, otp) -> writeTestOtp(otpPath, email, otp);
        } else {
            this.otpSender = EmailUtil::sendOTP;
        }
        this.otpGenerator = EmailUtil::generateOTP;
        this.clock = Clock.systemUTC();
        this.onPasswordReset = email -> {
            if (sessionStore != null) {
                AccountDTO account = accountBUS.selectByUsername(email);
                if (account != null) {
                    sessionStore.revokeByEmployeeId(account.getEmployeeId());
                }
            }
        };
    }

    static boolean writeTestOtp(Path otpPath, String email, String otp) {
        try {
            Path parent = otpPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(otpPath, email + "=" + otp + System.lineSeparator(), StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    PasswordResetService(
            Predicate<String> accountExists,
            BiFunction<String, String, String> passwordResetter,
            BiPredicate<String, String> otpSender,
            Supplier<String> otpGenerator,
            Clock clock
    ) {
        this(accountExists, passwordResetter, otpSender, otpGenerator, clock, email -> {});
    }

    PasswordResetService(
            Predicate<String> accountExists,
            BiFunction<String, String, String> passwordResetter,
            BiPredicate<String, String> otpSender,
            Supplier<String> otpGenerator,
            Clock clock,
            Consumer<String> onPasswordReset
    ) {
        this.accountExists = accountExists;
        this.passwordResetter = passwordResetter;
        this.otpSender = otpSender;
        this.otpGenerator = otpGenerator;
        this.clock = clock;
        this.onPasswordReset = onPasswordReset == null ? email -> {} : onPasswordReset;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public void requestOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không đúng định dạng.");
        }
        if (!accountExists.test(normalizedEmail)) {
            return;
        }

        String otp = otpGenerator.get();
        ResetCode reserved;
        synchronized (codes) {
            Instant now = clock.instant();
            codes.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
            if (codes.containsKey(normalizedEmail)) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Vui lòng đợi mã OTP hết hạn trước khi yêu cầu mã mới."
                );
            }
            reserved = new ResetCode(otp, now.plus(OTP_LIFETIME), true);
            codes.put(normalizedEmail, reserved);
        }

        boolean sent;
        try {
            sent = otpSender.test(normalizedEmail, otp);
        } catch (IllegalStateException e) {
            codes.remove(normalizedEmail, reserved);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
        } catch (RuntimeException e) {
            codes.remove(normalizedEmail, reserved);
            throw e;
        }

        if (!sent) {
            codes.remove(normalizedEmail, reserved);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi email.");
        }

        synchronized (reserved) {
            reserved.markReady(clock.instant().plus(OTP_LIFETIME));
        }
    }

    public String confirmReset(String email, String otp, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || !EMAIL_PATTERN.matcher(normalizedEmail).matches()
                || otp == null || !otp.matches("^\\d{6}$")
                || newPassword == null || newPassword.isBlank() || newPassword.trim().length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin đặt lại mật khẩu không hợp lệ.");
        }

        ResetCode resetCode = codes.get(normalizedEmail);
        if (resetCode == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        synchronized (resetCode) {
            if (codes.get(normalizedEmail) != resetCode || resetCode.isPending()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không hợp lệ hoặc đã hết hạn.");
            }
            if (!resetCode.expiresAt().isAfter(clock.instant())) {
                codes.remove(normalizedEmail, resetCode);
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không hợp lệ hoặc đã hết hạn.");
            }
            if (!MessageDigest.isEqual(
                    resetCode.otp().getBytes(StandardCharsets.UTF_8),
                    otp.getBytes(StandardCharsets.UTF_8))) {
                resetCode.incrementAttempts();
                if (resetCode.attempts() >= MAX_ATTEMPTS) {
                    codes.remove(normalizedEmail, resetCode);
                }
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không chính xác.");
            }

            String result = passwordResetter.apply(normalizedEmail, newPassword);
            if (!"OK".equals(result)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, result);
            }
            codes.remove(normalizedEmail, resetCode);
            onPasswordReset.accept(normalizedEmail);
            return result;
        }
    }

    private static final class ResetCode {
        private final String otp;
        private volatile Instant expiresAt;
        private volatile boolean pending;
        private int attempts;

        private ResetCode(String otp, Instant expiresAt, boolean pending) {
            this.otp = otp;
            this.expiresAt = expiresAt;
            this.pending = pending;
        }

        private String otp() {
            return otp;
        }

        private Instant expiresAt() {
            return expiresAt;
        }

        private boolean isPending() {
            return pending;
        }

        private void markReady(Instant readyExpiresAt) {
            this.expiresAt = readyExpiresAt;
            this.pending = false;
        }

        private int attempts() {
            return attempts;
        }

        private void incrementAttempts() {
            attempts++;
        }
    }
}
