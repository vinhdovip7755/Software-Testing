package com.bookstore.api.contract;

public record AdminAccountUpdateRequestDTO(
        String oldUsername,
        String newUsername,
        String newPassword
) {
}
