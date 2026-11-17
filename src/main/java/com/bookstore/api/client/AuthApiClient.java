package com.bookstore.api.client;

import com.bookstore.api.contract.ApiSessionDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class AuthApiClient {
    public ApiSessionDTO login(String username, String password) {
        ApiSessionDTO session = ApiClient.post(
                "/api/auth/login",
                Map.of("username", username, "password", password),
                new TypeReference<>() {
                }
        );
        ApiClient.setAccessToken(session.getAccessToken());
        return session;
    }

    public void logout() {
        try {
            ApiClient.postNoContent("/api/auth/logout");
        } finally {
            ApiClient.clearAccessToken();
        }
    }
}
