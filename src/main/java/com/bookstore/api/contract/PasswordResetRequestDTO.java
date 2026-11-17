package com.bookstore.api.contract;

public record PasswordResetRequestDTO(
        String email,
        String otp,
        String newPassword
) {
}
