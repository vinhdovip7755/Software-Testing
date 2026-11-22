package com.bookstore.util;

import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

public final class PasswordUtil {
    private static final int BCRYPT_COST = 10;
    private static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private PasswordUtil() {
    }

    public static boolean isBcryptHash(String value) {
        return value != null && BCRYPT_PATTERN.matcher(value).matches();
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Mật khẩu không được null.");
        }
        if (isBcryptHash(rawPassword)) {
            return rawPassword;
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            try {
                return BCrypt.checkpw(rawPassword, storedPassword);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
        // Tương thích ngược với dữ liệu mật khẩu plaintext cũ trong MySQL trước khi nâng cấp BCrypt
        return MessageDigest.isEqual(
                storedPassword.getBytes(StandardCharsets.UTF_8),
                rawPassword.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static boolean isSamePassword(String candidatePassword, String storedPassword) {
        if (candidatePassword == null || storedPassword == null) {
            return false;
        }
        if (matches(candidatePassword, storedPassword)) {
            return true;
        }
        return isBcryptHash(storedPassword) && storedPassword.equals(candidatePassword);
    }
}
