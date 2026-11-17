package com.bookstore.api.client;

import com.bookstore.dto.CustomerHistoryDTO;
import com.bookstore.dto.CustomerStatsDTO;
import com.bookstore.dto.FinancialQuarterSummaryDTO;
import com.bookstore.dto.FinancialStatsDTO;
import com.bookstore.dto.ProductStatsDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDate;
import java.util.List;

public final class StatisticsApiClient {
    public List<FinancialStatsDTO> getFinancialStatsByDay(LocalDate from, LocalDate to) {
        return ApiClient.get("/api/statistics/financial/daily?from=" + from + "&to=" + to,
                new TypeReference<>() {
                });
    }

    public FinancialStatsDTO getTotalFinancialStats() {
        return ApiClient.get("/api/statistics/financial/total", new TypeReference<>() {
        });
    }

    public FinancialQuarterSummaryDTO getFinancialQuarterSummary(int year) {
        return ApiClient.get("/api/statistics/financial/quarter-summary?year=" + year,
                new TypeReference<>() {
                });
    }

    public List<ProductStatsDTO> getProductStatsByDay(LocalDate from, LocalDate to) {
        return ApiClient.get("/api/statistics/products/daily?from=" + from + "&to=" + to,
                new TypeReference<>() {
                });
    }

    public List<ProductStatsDTO> getProductStatsByMonth(int year) {
        return ApiClient.get("/api/statistics/products/monthly?year=" + year, new TypeReference<>() {
        });
    }

    public List<ProductStatsDTO> getProductStatsByQuarter(int year) {
        return ApiClient.get("/api/statistics/products/quarterly?year=" + year, new TypeReference<>() {
        });
    }

    public List<ProductStatsDTO> getProductStatsByYear() {
        return ApiClient.get("/api/statistics/products/yearly", new TypeReference<>() {
        });
    }

    public List<ProductStatsDTO> getTopSellingProducts(int year, int quarter) {
        return ApiClient.get("/api/statistics/products/top-selling?year=" + year + "&quarter=" + quarter,
                new TypeReference<>() {
                });
    }

    public List<ProductStatsDTO> getTopSlowProducts(int year, int quarter) {
        return ApiClient.get("/api/statistics/products/top-slow?year=" + year + "&quarter=" + quarter,
                new TypeReference<>() {
                });
    }

    public List<CustomerStatsDTO> getTopCustomersByQuantity(LocalDate from, LocalDate to) {
        return ApiClient.get("/api/statistics/customers/top-quantity?from=" + from + "&to=" + to,
                new TypeReference<>() {
                });
    }

    public List<CustomerStatsDTO> getTopCustomersBySpending(LocalDate from, LocalDate to) {
        return ApiClient.get("/api/statistics/customers/top-spending?from=" + from + "&to=" + to,
                new TypeReference<>() {
                });
    }

    public List<CustomerHistoryDTO> getCustomerHistory(int customerId, LocalDate from, LocalDate to) {
        return ApiClient.get("/api/statistics/customers/" + customerId + "/history?from=" + from + "&to=" + to,
                new TypeReference<>() {
                });
    }
}
