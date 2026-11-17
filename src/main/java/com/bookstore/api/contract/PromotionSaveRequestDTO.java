package com.bookstore.api.contract;

import java.sql.Timestamp;
import java.util.List;

public record PromotionSaveRequestDTO(
        String promotionName,
        double percent,
        Timestamp startDate,
        Timestamp endDate,
        int status,
        List<Integer> bookIds
) {
}
