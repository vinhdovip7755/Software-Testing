package com.bookstore.api.client;

import com.bookstore.api.contract.PriceBulkUpdateRequestDTO;
import com.bookstore.api.contract.PriceUpdateRequestDTO;
import com.bookstore.dto.PriceDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class PriceApiClient {
    public List<PriceDTO> getPrices() {
        return ApiClient.get("/api/prices", new TypeReference<>() {
        });
    }

    public List<PriceDTO> searchPrices(String keyword, String type) {
        String path = "/api/prices/search?keyword=" + encode(keyword) + "&type=" + encode(type);
        return ApiClient.get(path, new TypeReference<>() {
        });
    }

    public void updateBookPrice(int bookId, double profitRate) {
        ApiClient.putNoContent("/api/prices/" + bookId, new PriceUpdateRequestDTO(profitRate));
    }

    public void updateBulkPrices(List<PriceDTO> prices, double profitRate) {
        List<Integer> bookIds = prices.stream().map(PriceDTO::getBookId).toList();
        ApiClient.putNoContent("/api/prices/bulk", new PriceBulkUpdateRequestDTO(profitRate, bookIds));
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
