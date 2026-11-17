package com.bookstore.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

public final class ApiResponses {
    private ApiResponses() {
    }

    public static ResponseEntity<String> fromBusinessMessage(String message) {
        String normalized = message == null ? "" : message.toLowerCase(Locale.ROOT);
        HttpStatus status = "ok".equalsIgnoreCase(message) || normalized.contains("thành công")
                ? HttpStatus.OK
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(message);
    }
}
