package com.bookstore.api.contract;

import java.sql.Date;

public record ProfileUpdateRequestDTO(
        String employeeName,
        String employeePhone,
        Date birthday,
        String username,
        String newPassword
) {
}
