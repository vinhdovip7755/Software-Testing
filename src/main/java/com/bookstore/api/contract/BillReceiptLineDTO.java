package com.bookstore.api.contract;

public record BillReceiptLineDTO(
        int bookId,
        String bookName,
        int quantity,
        double unitPrice,
        double promotionPercent,
        double totalPrice
) {
}
