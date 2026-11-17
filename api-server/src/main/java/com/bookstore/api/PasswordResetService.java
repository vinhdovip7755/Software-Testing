package com.bookstore.api;

import com.bookstore.bus.AccountBUS;
import com.bookstore.util.EmailUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
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
    private final ConcurrentHashMap<String, ResetCode> codes = new ConcurrentHashMap<>();

    public PasswordResetService() {
        AccountBUS accountBUS = new AccountBUS();
        this.accountExists = accountBUS::isEmailExists;
        this.passwordResetter = accountBUS::resetPasswordByEmail;
        this.otpSender = EmailUtil::sendOTP;
        this.otpGenerator = EmailUtil::generateOTP;
        this.clock = Clock.systemUTC();
    }

    PasswordResetService(
            Predicate<String> accountExists,
            BiFunction<String, String, String> passwordResetter,
            BiPredicate<String, String> otpSender,
            Supplier<String> otpGenerator,
            Clock clock
    ) {
        this.accountExists = accountExists;
        this.passwordResetter = passwordResetter;
        this.otpSender = otpSender;
        this.otpGenerator = otpGenerator;
        this.clock = clock;
    }

    public void requestOtp(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không đúng định dạng.");
        }
        if (!accountExists.test(email)) {
            return;
        }

        synchronized (codes) {
            Instant now = clock.instant();
            codes.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
            if (codes.containsKey(email)) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Vui lòng đợi mã OTP hết hạn trước khi yêu cầu mã mới."
                );
            }

            String otp = otpGenerator.get();
            try {
                if (!otpSender.test(email, otp)) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi email.");
                }
            } catch (IllegalStateException e) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
            }
            codes.put(email, new ResetCode(otp, now.plus(OTP_LIFETIME)));
        }
    }

    public String confirmReset(String email, String otp, String newPassword) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()
                || otp == null || !otp.matches("^\\d{6}$")
                || newPassword == null || newPassword.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin đặt lại mật khẩu không hợp lệ.");
        }

        ResetCode resetCode = codes.get(email);
        if (resetCode == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không hợp lệ hoặc đã hết hạn.");
        }
        synchronized (resetCode) {
            if (codes.get(email) != resetCode) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không hợp lệ hoặc đã hết hạn.");
            }
            if (!resetCode.expiresAt().isAfter(clock.instant())) {
                codes.remove(email, resetCode);
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không hợp lệ hoặc đã hết hạn.");
            }
            if (!MessageDigest.isEqual(resetCode.otp().getBytes(), otp.getBytes())) {
                resetCode.incrementAttempts();
                if (resetCode.attempts() >= MAX_ATTEMPTS) {
                    codes.remove(email, resetCode);
                }
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mã OTP không chính xác.");
            }

            String result = passwordResetter.apply(email, newPassword);
            if (!"OK".equals(result)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, result);
            }
            codes.remove(email, resetCode);
            return result;
        }
    }

    private static final class ResetCode {
        private final String otp;
        private final Instant expiresAt;
        private int attempts;

        private ResetCode(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }

        private String otp() {
            return otp;
        }

        private Instant expiresAt() {
            return expiresAt;
        }

        private int attempts() {
            return attempts;
        }

        private void incrementAttempts() {
            attempts++;
        }
    }
}
