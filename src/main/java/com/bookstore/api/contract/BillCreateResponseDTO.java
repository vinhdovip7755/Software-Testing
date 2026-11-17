package com.bookstore.api.contract;

import java.util.List;

public record BillCreateResponseDTO(
        int billId,
        double totalBillPrice,
        double tax,
        int earnedPoints,
        String customerName,
        List<BillReceiptLineDTO> details
) {
}
