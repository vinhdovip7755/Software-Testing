package com.bookstore.api.client;

import com.bookstore.dto.BookDTO;
import com.bookstore.dto.BookLotDTO;
import com.bookstore.dto.InventoryLogDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class InventoryApiClient {
    public List<BookDTO> getBooks() {
        return ApiClient.get("/api/inventory/books", new TypeReference<>() {
        });
    }

    public List<BookLotDTO> getBookLots(int bookId) {
        return ApiClient.get("/api/inventory/books/" + bookId + "/lots", new TypeReference<>() {
        });
    }

    public List<InventoryLogDTO> getInventoryLogs() {
        return ApiClient.get("/api/inventory/logs", new TypeReference<>() {
        });
    }
}
