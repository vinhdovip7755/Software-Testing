package com.bookstore.api.client;

import com.bookstore.api.contract.PasswordResetRequestDTO;

public final class PasswordResetApiClient {
    public void requestOtp(String email) {
        ApiClient.postNoContent("/api/auth/password-reset/request", new PasswordResetRequestDTO(email, null, null));
    }

    public void resetPassword(String email, String otp, String newPassword) {
        ApiClient.postNoContent(
                "/api/auth/password-reset/confirm",
                new PasswordResetRequestDTO(email, otp, newPassword)
        );
    }
}
