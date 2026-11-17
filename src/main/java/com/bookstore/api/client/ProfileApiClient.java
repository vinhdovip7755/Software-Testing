package com.bookstore.api.client;

import com.bookstore.api.contract.ProfileDTO;
import com.bookstore.api.contract.ProfileUpdateRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;

public final class ProfileApiClient {
    public ProfileDTO getProfile() {
        return ApiClient.get("/api/profile", new TypeReference<>() {
        });
    }

    public ProfileDTO updateProfile(ProfileUpdateRequestDTO request) {
        return ApiClient.put("/api/profile", request, new TypeReference<>() {
        });
    }
}
