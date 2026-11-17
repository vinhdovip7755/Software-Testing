package com.bookstore.api.client;

import com.fasterxml.jackson.core.type.TypeReference;

public final class SystemParameterApiClient {
    public double getVatRate() {
        return ApiClient.get("/api/system-parameters/vat", new TypeReference<>() {
        });
    }
}
