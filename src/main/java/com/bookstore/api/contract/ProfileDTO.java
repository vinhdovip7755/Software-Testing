package com.bookstore.api.contract;

import com.bookstore.dto.EmployeeDTO;

public record ProfileDTO(
        EmployeeDTO employee,
        String username
) {
}
