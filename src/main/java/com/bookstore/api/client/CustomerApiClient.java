package com.bookstore.api.client;

import com.bookstore.api.contract.CustomerPurchaseSummaryDTO;
import com.bookstore.dto.CustomerDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class CustomerApiClient {
    public List<CustomerDTO> selectAllCustomers() {
        return ApiClient.get("/api/customers", new TypeReference<>() {
        });
    }

    public CustomerDTO selectByPhone(String phone) {
        try {
            return ApiClient.get("/api/customers/phone/" + phone, new TypeReference<>() {
            });
        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    public CustomerPurchaseSummaryDTO getCustomerStatistics(int customerId) {
        return ApiClient.get(
                "/api/customers/" + customerId + "/statistics",
                new TypeReference<>() {
                }
        );
    }

    public String updateCustomerInfo(CustomerDTO customer) {
        return ApiClient.put("/api/customers/" + customer.getCustomerId(), customer, new TypeReference<>() {
        });
    }

    public String insertCustomer(CustomerDTO customer) {
        return ApiClient.post("/api/customers", customer, new TypeReference<>() {
        });
    }
}
