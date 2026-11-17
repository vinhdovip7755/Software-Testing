package com.bookstore.api.contract;

public record EmployeeActivityDTO(
        int billCount,
        int createdImportCount,
        int approvedImportCount
) {
}
