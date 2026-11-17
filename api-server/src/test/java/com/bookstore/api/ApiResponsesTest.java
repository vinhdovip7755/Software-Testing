package com.bookstore.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiResponsesTest {
    @Test
    void mapsBusinessSuccessAndValidationFailureToHttpStatuses() {
        assertEquals(200, ApiResponses.fromBusinessMessage("Thêm sách thành công!").getStatusCode().value());
        assertEquals(422, ApiResponses.fromBusinessMessage("Tên sách không được để trống!").getStatusCode().value());
    }
}
