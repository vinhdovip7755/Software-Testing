package com.bookstore.api.client;

import com.bookstore.api.contract.BillCreateRequestDTO;
import com.bookstore.api.contract.BillCreateResponseDTO;
import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public final class BillApiClient {
    private static final TypeReference<List<BillDTO>> BILL_LIST_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<BillDTO> BILL_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<BillDetailDTO>> BILL_DETAILS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<BillCreateResponseDTO> BILL_CREATE_RESPONSE_TYPE = new TypeReference<>() {
    };

    public List<BillDTO> getAllBills() {
        return ApiClient.get("/api/bills", BILL_LIST_TYPE);
    }

    public BillDTO getBillById(int billId) {
        return ApiClient.get("/api/bills/" + billId, BILL_TYPE);
    }

    public List<BillDetailDTO> getBillDetails(int billId) {
        return ApiClient.get("/api/bills/" + billId + "/details", BILL_DETAILS_TYPE);
    }

    public BillCreateResponseDTO createBill(BillCreateRequestDTO request) {
        return ApiClient.post("/api/bills", request, BILL_CREATE_RESPONSE_TYPE);
    }
}
