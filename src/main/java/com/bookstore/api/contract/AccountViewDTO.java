package com.bookstore.api.contract;

public record AccountViewDTO(
        int employeeId,
        String username,
        int status,
        String employeeName,
        int roleId,
        String roleName
) {
}
