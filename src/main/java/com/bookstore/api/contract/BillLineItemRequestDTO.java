package com.bookstore.api.contract;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record BillLineItemRequestDTO(
        int bookId,
        @JsonDeserialize(using = BillQuantityDeserializer.class) int quantity
) {
}
