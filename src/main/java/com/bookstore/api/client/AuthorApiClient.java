package com.bookstore.api.client;

import com.bookstore.dto.AuthorDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class AuthorApiClient {
    public List<AuthorDTO> selectAllAuthors() {
        return ApiClient.get("/api/authors", new TypeReference<>() {
        });
    }

    public String addAuthor(AuthorDTO author) {
        return ApiClient.post("/api/authors", author, new TypeReference<>() {
        });
    }

    public String updateAuthor(AuthorDTO author) {
        return ApiClient.put("/api/authors/" + author.getAuthorId(), author, new TypeReference<>() {
        });
    }
}
