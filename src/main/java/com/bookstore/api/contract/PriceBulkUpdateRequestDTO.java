package com.bookstore.api.contract;

import java.util.List;

public record PriceBulkUpdateRequestDTO(double profitRate, List<Integer> bookIds) {
}
