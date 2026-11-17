package com.bookstore.api.client;

import com.bookstore.dto.BookDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class BookApiClient {
    public List<BookDTO> selectAllBooks() {
        return ApiClient.get("/api/books", new TypeReference<>() {
        });
    }

    public List<BookDTO> selectActiveBooks() {
        return ApiClient.get("/api/books/active", new TypeReference<>() {
        });
    }

    public BookDTO selectById(int bookId) {
        return ApiClient.get("/api/books/" + bookId, new TypeReference<>() {
        });
    }

    public String addBook(BookDTO book) {
        return ApiClient.post("/api/books", book, new TypeReference<>() {
        });
    }

    public String addBookForImport(BookDTO book) {
        return ApiClient.post("/api/imports/quick-add-book", book, new TypeReference<>() {
        });
    }

    public String updateBook(BookDTO book) {
        return ApiClient.put("/api/books/" + book.getBookId(), book, new TypeReference<>() {
        });
    }
}
