package com.bookstore.api.contract;

public record AccountUpdateRequestDTO(
        String newPassword,
        int status
) {
}
