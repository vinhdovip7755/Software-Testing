package com.bookstore.api.client;

import com.bookstore.dto.SupplierDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class SupplierApiClient {
    public List<SupplierDTO> selectAll() {
        return ApiClient.get("/api/suppliers", new TypeReference<>() {
        });
    }

    public SupplierDTO getById(int id) {
        return ApiClient.get("/api/suppliers/" + id, new TypeReference<>() {
        });
    }

    public String addSupplier(SupplierDTO supplier) {
        return ApiClient.post("/api/suppliers", supplier, new TypeReference<>() {
        });
    }

    public String updateSupplier(SupplierDTO supplier) {
        return ApiClient.put("/api/suppliers/" + supplier.getSupplierId(), supplier, new TypeReference<>() {
        });
    }
}
