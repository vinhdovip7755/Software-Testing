package com.bookstore.api.contract;

public record AccountCreateRequestDTO(
        String username,
        String password,
        int employeeId,
        int status
) {
}
