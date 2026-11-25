package com.bookstore.api;

import com.bookstore.util.EmailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetServiceTest {
    @Test
    void otpIsSentAndCanBeUsedOnlyOnce() {
        AtomicInteger resetCount = new AtomicInteger();
        PasswordResetService service = new PasswordResetService(
                email -> true,
                (email, password) -> {
                    resetCount.incrementAndGet();
                    return "OK";
                },
                (email, otp) -> "reader@example.com".equals(email) && "123456".equals(otp),
                () -> "123456",
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        );

        service.requestOtp("reader@example.com");
        service.verifyOtp("reader@example.com", "123456");
        assertEquals(0, resetCount.get());
        assertEquals("OK", service.confirmReset("reader@example.com", "123456", "new-pass"));

        ResponseStatusException reusedOtp = assertThrows(
                ResponseStatusException.class,
                () -> service.confirmReset("reader@example.com", "123456", "another-pass")
        );
        assertEquals(422, reusedOtp.getStatusCode().value());
        assertThrows(ResponseStatusException.class, () -> service.verifyOtp("reader@example.com", "123456"));
        assertEquals(1, resetCount.get());
    }

    @Test
    void unknownEmailDoesNotTriggerEmailDeliveryOrRevealAccountExistence() {
        AtomicInteger emailAttempts = new AtomicInteger();
        PasswordResetService service = new PasswordResetService(
                email -> false,
                (email, password) -> {
                    emailAttempts.incrementAndGet();
                    return "OK";
                },
                (email, otp) -> {
                    throw new AssertionError("Should not send to an unknown account");
                },
                () -> "123456",
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        );

        service.requestOtp("unknown@example.com");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.confirmReset("unknown@example.com", "123456", "new-pass")
        );
        assertEquals(422, exception.getStatusCode().value());
        assertEquals(0, emailAttempts.get());
    }

    @Test
    void otpIsInvalidatedAfterFiveIncorrectAttempts() {
        AtomicInteger resetCount = new AtomicInteger();
        PasswordResetService service = new PasswordResetService(
                email -> true,
                (email, password) -> {
                    resetCount.incrementAndGet();
                    return "OK";
                },
                (email, otp) -> true,
                () -> "123456",
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        );
        service.requestOtp("reader@example.com");

        for (int attempt = 0; attempt < 4; attempt++) {
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.verifyOtp("reader@example.com", "000000")
            );
        }
        assertThrows(
                ResponseStatusException.class,
                () -> service.confirmReset("reader@example.com", "000000", "new-pass")
        );
        assertThrows(
                ResponseStatusException.class,
                () -> service.confirmReset("reader@example.com", "123456", "new-pass")
        );
        assertEquals(0, resetCount.get());
    }

    @Test
    void writeTestOtpCapturesEmailAndCodeToLocalFile(@TempDir Path tempDir) throws Exception {
        Path otpFile = tempDir.resolve("otp.txt");
        assertTrue(PasswordResetService.writeTestOtp(otpFile, "reader@example.com", "654321"));
        assertEquals("reader@example.com=654321", Files.readString(otpFile, StandardCharsets.UTF_8).trim());
    }

    @Test
    void emailCaseVariantsShareRateLimitAndConfirmCallbackRevokesSessions() {
        AtomicReference<String> revokedEmail = new AtomicReference<>();
        PasswordResetService service = new PasswordResetService(
                email -> true,
                (email, password) -> "OK",
                (email, otp) -> true,
                () -> "654321",
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
                revokedEmail::set
        );

        service.requestOtp("  Reader@Example.COM ");

        ResponseStatusException rateLimited = assertThrows(
                ResponseStatusException.class,
                () -> service.requestOtp("reader@example.com")
        );
        assertEquals(429, rateLimited.getStatusCode().value());

        ResponseStatusException blankPassword = assertThrows(
                ResponseStatusException.class,
                () -> service.confirmReset("READER@example.com", "654321", "    ")
        );
        assertEquals(400, blankPassword.getStatusCode().value());
        assertEquals("Thông tin đặt lại mật khẩu không hợp lệ.", blankPassword.getReason());

        assertEquals("OK", service.confirmReset("READER@example.com", "654321", "NewPass123"));
        assertEquals("reader@example.com", revokedEmail.get());
    }

    @Test
    void smtpIoDoesNotHoldGlobalLockAndRollsBackSlotOnFailure() throws Exception {
        CountDownLatch slowEntered = new CountDownLatch(1);
        CountDownLatch allowSlowFinish = new CountDownLatch(1);
        AtomicBoolean failSlow = new AtomicBoolean(true);

        PasswordResetService service = new PasswordResetService(
                email -> true,
                (email, password) -> "OK",
                (email, otp) -> {
                    if ("slow@example.com".equals(email)) {
                        slowEntered.countDown();
                        try {
                            allowSlowFinish.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return !failSlow.get();
                    }
                    return true;
                },
                () -> "123456",
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
        );

        AtomicReference<Throwable> slowError = new AtomicReference<>();
        Thread slowThread = new Thread(() -> {
            try {
                service.requestOtp("slow@example.com");
            } catch (Throwable t) {
                slowError.set(t);
            }
        });
        slowThread.start();
        assertTrue(slowEntered.await(5, TimeUnit.SECONDS));

        // Different email must succeed immediately while slow@example.com is sending
        service.requestOtp("fast@example.com");
        assertEquals("OK", service.confirmReset("fast@example.com", "123456", "valid123"));

        // Release slow send with failure -> slot must roll back so retry succeeds
        allowSlowFinish.countDown();
        slowThread.join(5000);
        assertTrue(slowError.get() instanceof ResponseStatusException);
        assertEquals(503, ((ResponseStatusException) slowError.get()).getStatusCode().value());

        failSlow.set(false);
        service.requestOtp("slow@example.com");
        assertEquals("OK", service.confirmReset("slow@example.com", "123456", "valid123"));
    }

    @Test
    void temporaryPasswordMeetsComplexityRequirements() {
        String tempPass = EmailUtil.generateTemporaryPassword();
        assertTrue(tempPass.length() >= 12);
        assertTrue(tempPass.matches("^[A-Za-z0-9]+$"));
        assertTrue(tempPass.chars().anyMatch(Character::isUpperCase));
        assertTrue(tempPass.chars().anyMatch(Character::isLowerCase));
        assertTrue(tempPass.chars().anyMatch(Character::isDigit));
    }
}
