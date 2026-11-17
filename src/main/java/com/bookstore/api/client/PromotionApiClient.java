package com.bookstore.api.client;

import com.bookstore.api.contract.BookPromotionDTO;
import com.bookstore.api.contract.PromotionSaveRequestDTO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.PromotionDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class PromotionApiClient {
    public List<PromotionDTO> getAllPromotions() {
        return ApiClient.get("/api/promotions", new TypeReference<>() {
        });
    }

    public List<PromotionDTO> searchPromotions(String keyword, String type) {
        String path = "/api/promotions/search?keyword=" + encode(keyword) + "&type=" + encode(type);
        return ApiClient.get(path, new TypeReference<>() {
        });
    }

    public List<BookDTO> suggestBooks(String keyword, int categoryId) {
        String path = "/api/promotions/books?keyword=" + encode(keyword) + "&categoryId=" + categoryId;
        return ApiClient.get(path, new TypeReference<>() {
        });
    }

    public BookPromotionDTO getPromotionForBook(int bookId) {
        return ApiClient.get("/api/promotions/book/" + bookId, new TypeReference<>() {
        });
    }

    public List<Integer> getSelectedBookIds(int promotionId) {
        return ApiClient.get("/api/promotions/" + promotionId + "/books", new TypeReference<>() {
        });
    }

    public void createPromotion(PromotionSaveRequestDTO request) {
        ApiClient.postNoContent("/api/promotions", request);
    }

    public void updatePromotion(int promotionId, PromotionSaveRequestDTO request) {
        ApiClient.putNoContent("/api/promotions/" + promotionId, request);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
