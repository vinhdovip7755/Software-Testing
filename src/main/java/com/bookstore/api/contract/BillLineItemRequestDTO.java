package com.bookstore.api.contract;

public record BillLineItemRequestDTO(int bookId, int quantity) {
}
