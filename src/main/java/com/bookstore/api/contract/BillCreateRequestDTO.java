package com.bookstore.api.contract;

import java.util.List;

public record BillCreateRequestDTO(
        int customerId,
        int paymentMethodId,
        List<BillLineItemRequestDTO> details
) {
}
