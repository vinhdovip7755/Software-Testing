package com.bookstore.api.contract;

import java.util.List;

public record BillCreateRequestDTO(
        int customerId,
        int paymentMethodId,
        List<BillLineItemRequestDTO> details,
        int pointsUsed
) {
    public BillCreateRequestDTO(int customerId, int paymentMethodId, List<BillLineItemRequestDTO> details) {
        this(customerId, paymentMethodId, details, 0);
    }
}
