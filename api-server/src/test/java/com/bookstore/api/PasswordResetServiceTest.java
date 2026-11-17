package com.bookstore.api;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertEquals("OK", service.confirmReset("reader@example.com", "123456", "new-pass"));

        ResponseStatusException reusedOtp = assertThrows(
                ResponseStatusException.class,
                () -> service.confirmReset("reader@example.com", "123456", "another-pass")
        );
        assertEquals(422, reusedOtp.getStatusCode().value());
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

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.confirmReset("reader@example.com", "000000", "new-pass")
            );
        }
        assertThrows(
                ResponseStatusException.class,
                () -> service.confirmReset("reader@example.com", "123456", "new-pass")
        );
        assertEquals(0, resetCount.get());
    }
}
