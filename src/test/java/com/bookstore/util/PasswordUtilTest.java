package com.bookstore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashProducesValidBcryptAndMatchesRawPassword() {
        String hash = PasswordUtil.hash("07072006");
        assertTrue(PasswordUtil.isBcryptHash(hash));
        assertNotEquals("07072006", hash);
        assertTrue(PasswordUtil.matches("07072006", hash));
        assertFalse(PasswordUtil.matches("wrongpass", hash));
    }

    @Test
    void hashDoesNotDoubleHashExistingBcrypt() {
        String hash = PasswordUtil.hash("admin");
        assertEquals(hash, PasswordUtil.hash(hash));
    }

    @Test
    void matchesSupportsLegacyPlaintextForBackwardCompatibility() {
        assertFalse(PasswordUtil.isBcryptHash("admin"));
        assertTrue(PasswordUtil.matches("admin", "admin"));
        assertFalse(PasswordUtil.matches("other", "admin"));
    }

    @Test
    void loginRejectsRawBcryptHashStringAsPassword() {
        String hash = PasswordUtil.hash("admin");
        assertFalse(PasswordUtil.matches(hash, hash));
    }

    @Test
    void isSamePasswordDetectsDuplicateForBothRawAndHash() {
        String hash = PasswordUtil.hash("07072006");
        assertTrue(PasswordUtil.isSamePassword("07072006", hash));
        assertTrue(PasswordUtil.isSamePassword(hash, hash));
        assertFalse(PasswordUtil.isSamePassword("newpass2026", hash));
    }
}
