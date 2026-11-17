package com.bookstore.api.contract;

public record BookPromotionDTO(int bookId, String promotionName, double percent) {
}
