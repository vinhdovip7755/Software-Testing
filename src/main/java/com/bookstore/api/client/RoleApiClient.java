package com.bookstore.api.client;

import com.bookstore.api.contract.RoleActionPermissionDTO;
import com.bookstore.api.contract.RoleSaveRequestDTO;
import com.bookstore.dto.RoleDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class RoleApiClient {
    public List<RoleDTO> getRoles() {
        return ApiClient.get("/api/roles", new TypeReference<>() {
        });
    }

    public List<RoleActionPermissionDTO> getPermissions(int roleId) {
        return ApiClient.get("/api/roles/" + roleId + "/permissions", new TypeReference<>() {
        });
    }

    public String createRole(RoleSaveRequestDTO request) {
        return ApiClient.post("/api/roles", request, new TypeReference<>() {
        });
    }

    public String updateRole(int roleId, RoleSaveRequestDTO request) {
        return ApiClient.put("/api/roles/" + roleId, request, new TypeReference<>() {
        });
    }
}
