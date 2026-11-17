package com.bookstore.api.client;

import com.bookstore.dto.CategoryDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class CategoryApiClient {
    public List<CategoryDTO> selectAllCategories() {
        return ApiClient.get("/api/categories", new TypeReference<>() {
        });
    }

    public CategoryDTO getById(int id) {
        return ApiClient.get("/api/categories/" + id, new TypeReference<>() {
        });
    }

    public String addCategory(CategoryDTO category) {
        return ApiClient.post("/api/categories", category, new TypeReference<>() {
        });
    }

    public String updateCategory(CategoryDTO category) {
        return ApiClient.put("/api/categories/" + category.getCategoryId(), category, new TypeReference<>() {
        });
    }
}
