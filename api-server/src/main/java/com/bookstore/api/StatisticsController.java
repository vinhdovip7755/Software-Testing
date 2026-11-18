package com.bookstore.api;

import com.bookstore.bus.CustomerStatsBUS;
import com.bookstore.bus.FinancialStatsBUS;
import com.bookstore.bus.ProductStatsBUS;
import com.bookstore.dto.CustomerHistoryDTO;
import com.bookstore.dto.CustomerStatsDTO;
import com.bookstore.dto.FinancialQuarterSummaryDTO;
import com.bookstore.dto.FinancialStatsDTO;
import com.bookstore.dto.ProductStatsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
    private static final LocalDate MIN_DATE = LocalDate.of(2026, 1, 1);
    private static final LocalDate MAX_DATE = LocalDate.of(2099, 12, 31);

    private final AuthSessionStore sessionStore;

    public StatisticsController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping("/financial/daily")
    public List<FinancialStatsDTO> getFinancialStatsByDay(
            @RequestHeader("Authorization") String authorization,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        requireStatisticsView(authorization);
        validateDateRange(from, to);
        return new FinancialStatsBUS().getThongKeTheoNgay(
                java.sql.Date.valueOf(from),
                java.sql.Date.valueOf(to)
        );
    }

    @GetMapping("/financial/total")
    public FinancialStatsDTO getTotalFinancialStats(@RequestHeader("Authorization") String authorization) {
        requireStatisticsView(authorization);
        return new FinancialStatsBUS().getThongKeTong();
    }

    @GetMapping("/financial/quarter-summary")
    public FinancialQuarterSummaryDTO getFinancialQuarterSummary(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int year
    ) {
        requireStatisticsView(authorization);
        validateYear(year);
        FinancialStatsBUS.QuyDoanhThuSummary summary = new FinancialStatsBUS().getQuyDoanhThuSummary(year);
        if (summary == null) {
            return new FinancialQuarterSummaryDTO();
        }
        return new FinancialQuarterSummaryDTO(
                summary.getNam(),
                summary.getQuyCaoNhat(),
                summary.getDoanhThuQuyCaoNhat(),
                summary.getQuyThapNhat(),
                summary.getDoanhThuQuyThapNhat()
        );
    }

    @GetMapping("/products/daily")
    public List<ProductStatsDTO> getProductStatsByDay(
            @RequestHeader("Authorization") String authorization,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        requireStatisticsView(authorization);
        validateDateRange(from, to);
        return new ProductStatsBUS().getThongKeTheoNgay(
                java.sql.Date.valueOf(from),
                java.sql.Date.valueOf(to)
        );
    }

    @GetMapping("/products/monthly")
    public List<ProductStatsDTO> getProductStatsByMonth(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int year
    ) {
        requireStatisticsView(authorization);
        validateYear(year);
        return new ProductStatsBUS().getThongKeTheoThang(year);
    }

    @GetMapping("/products/quarterly")
    public List<ProductStatsDTO> getProductStatsByQuarter(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int year
    ) {
        requireStatisticsView(authorization);
        validateYear(year);
        return new ProductStatsBUS().getThongKeTheoQuy(year);
    }

    @GetMapping("/products/yearly")
    public List<ProductStatsDTO> getProductStatsByYear(@RequestHeader("Authorization") String authorization) {
        requireStatisticsView(authorization);
        return new ProductStatsBUS().getThongKeTheoNam();
    }

    @GetMapping("/products/top-selling")
    public List<ProductStatsDTO> getTopSellingProducts(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int year,
            @RequestParam int quarter
    ) {
        requireStatisticsView(authorization);
        validateYear(year);
        validateQuarter(quarter);
        return new ProductStatsBUS().getTop3BanChayTheoQuy(year, quarter);
    }

    @GetMapping("/products/top-slow")
    public List<ProductStatsDTO> getTopSlowProducts(
            @RequestHeader("Authorization") String authorization,
            @RequestParam int year,
            @RequestParam int quarter
    ) {
        requireStatisticsView(authorization);
        validateYear(year);
        validateQuarter(quarter);
        return new ProductStatsBUS().getTop3BanItTheoQuy(year, quarter);
    }

    @GetMapping("/customers/top-quantity")
    public List<CustomerStatsDTO> getTopCustomersByQuantity(
            @RequestHeader("Authorization") String authorization,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        requireStatisticsView(authorization);
        validateDateRange(from, to);
        return new CustomerStatsBUS().getTopCustomersByQuantity(startOfDay(from), endOfDay(to));
    }

    @GetMapping("/customers/top-spending")
    public List<CustomerStatsDTO> getTopCustomersBySpending(
            @RequestHeader("Authorization") String authorization,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        requireStatisticsView(authorization);
        validateDateRange(from, to);
        return new CustomerStatsBUS().getTopCustomersBySpending(startOfDay(from), endOfDay(to));
    }

    @GetMapping("/customers/{customerId}/history")
    public List<CustomerHistoryDTO> getCustomerHistory(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int customerId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        requireStatisticsView(authorization);
        if (customerId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã khách hàng không hợp lệ.");
        }
        validateDateRange(from, to);
        return new CustomerStatsBUS().getCustomerHistory(customerId, startOfDay(from), endOfDay(to));
    }

    private void requireStatisticsView(String authorization) {
        sessionStore.requireAnyView(authorization, "STATISTICS");
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
        if (from.isBefore(MIN_DATE) || to.isAfter(MAX_DATE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Khoảng ngày thống kê phải nằm trong khoảng từ 01/01/2026 đến 31/12/2099.");
        }
    }

    private void validateYear(int year) {
        if (year < MIN_DATE.getYear() || year > MAX_DATE.getYear()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Năm thống kê không hợp lệ.");
        }
    }

    private void validateQuarter(int quarter) {
        if (quarter < 1 || quarter > 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quý thống kê không hợp lệ.");
        }
    }

    private Timestamp startOfDay(LocalDate date) {
        return Timestamp.valueOf(date.atStartOfDay());
    }

    private Timestamp endOfDay(LocalDate date) {
        return Timestamp.valueOf(date.atTime(23, 59, 59, 999_000_000));
    }
}
