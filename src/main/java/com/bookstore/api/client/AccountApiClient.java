package com.bookstore.api.client;

import com.bookstore.api.contract.AccountCreateRequestDTO;
import com.bookstore.api.contract.AccountUpdateRequestDTO;
import com.bookstore.api.contract.AccountViewDTO;
import com.bookstore.api.contract.AdminAccountUpdateRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class AccountApiClient {
    public List<AccountViewDTO> getAccounts() {
        return ApiClient.get("/api/accounts", new TypeReference<>() {
        });
    }

    public String createAccount(AccountCreateRequestDTO request) {
        return ApiClient.post("/api/accounts", request, new TypeReference<>() {
        });
    }

    public String updateAccount(int employeeId, AccountUpdateRequestDTO request) {
        return ApiClient.put("/api/accounts/" + employeeId, request, new TypeReference<>() {
        });
    }

    public String updateAdminAccount(AdminAccountUpdateRequestDTO request) {
        return ApiClient.put("/api/accounts/admin", request, new TypeReference<>() {
        });
    }

    public void sendNewPassword(int employeeId) {
        ApiClient.postNoContent("/api/accounts/" + employeeId + "/reset-password");
    }
}
