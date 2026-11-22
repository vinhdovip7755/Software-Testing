package com.bookstore.api;

import com.bookstore.api.contract.PriceBulkUpdateRequestDTO;
import com.bookstore.api.contract.PriceUpdateRequestDTO;
import com.bookstore.bus.PriceBUS;
import com.bookstore.dao.PriceDAO;
import com.bookstore.dto.PriceDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/prices")
public class PriceController {
    private static final Set<String> SEARCH_TYPES = Set.of("Tên", "Tác giả", "Thể loại", "Giá");

    private final AuthSessionStore sessionStore;

    public PriceController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<PriceDTO> getPrices(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_PRICE");
        return new PriceBUS().getPriceDTOs();
    }

    @GetMapping("/search")
    public List<PriceDTO> searchPrices(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "type", defaultValue = "Tên") String type
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_PRICE");
        if (!SEARCH_TYPES.contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Loại tìm kiếm giá không hợp lệ.");
        }
        PriceBUS priceBUS = new PriceBUS();
        return priceBUS.timKiemSach(priceBUS.getPriceDTOs(), keyword, type);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<Void> updateBookPrice(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("bookId") int bookId,
            @RequestBody PriceUpdateRequestDTO request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_PRICE");
        if (bookId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã sách không hợp lệ.");
        }
        validateProfitRate(request == null ? Double.NaN : request.profitRate());

        PriceBUS priceBUS = new PriceBUS();
        PriceDTO currentPrice = priceBUS.getPriceDTOs().stream()
                .filter(price -> price.getBookId() == bookId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy giá hiện tại của sách."));
        if (!Double.isFinite(currentPrice.getBasePrice()) || currentPrice.getBasePrice() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sách chưa có giá vốn nhập hàng hợp lệ nên không thể cập nhật giá bán.");
        }
        double newSellingPrice = PriceDAO.calculateSellingPrice(currentPrice.getBasePrice(), request.profitRate());
        if (!Double.isFinite(newSellingPrice) || newSellingPrice <= 0 || newSellingPrice > PriceDAO.MAX_PRICE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá bán mới vượt quá giới hạn hợp lệ.");
        }
        if (!priceBUS.createNewPrice(
                bookId,
                currentPrice.getBasePrice(),
                request.profitRate(),
                newSellingPrice
        )) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể cập nhật giá sách.");
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bulk")
    public ResponseEntity<Void> updateBulkPrices(
            @RequestHeader("Authorization") String authorization,
            @RequestBody PriceBulkUpdateRequestDTO request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_PRICE");
        validateBulkRequest(request);

        Set<Integer> requestedBookIds = new HashSet<>();
        for (Integer bookId : request.bookIds()) {
            if (bookId == null || bookId <= 0 || !requestedBookIds.add(bookId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách mã sách không hợp lệ.");
            }
        }

        PriceBUS priceBUS = new PriceBUS();
        List<PriceDTO> allPrices = priceBUS.getPriceDTOs();
        List<PriceDTO> selectedPrices = allPrices.stream()
                .filter(price -> requestedBookIds.contains(price.getBookId()))
                .toList();
        if (selectedPrices.size() != requestedBookIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Danh sách chứa sách không có giá bán đang hoạt động.");
        }
        List<PriceDTO> eligiblePrices = selectedPrices.stream()
                .filter(price -> Double.isFinite(price.getBasePrice()) && price.getBasePrice() > 0)
                .toList();
        if (eligiblePrices.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Không có sách nào trong danh sách có giá vốn nhập hàng hợp lệ để cập nhật giá.");
        }
        for (PriceDTO price : eligiblePrices) {
            double predictedPrice = PriceDAO.calculateSellingPrice(price.getBasePrice(), request.profitRate());
            if (!Double.isFinite(predictedPrice) || predictedPrice <= 0 || predictedPrice > PriceDAO.MAX_PRICE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Giá bán mới vượt quá giới hạn hợp lệ.");
            }
        }
        if (!priceBUS.updateBulkPrice(eligiblePrices, request.profitRate())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể cập nhật giá hàng loạt.");
        }
        return ResponseEntity.noContent().build();
    }

    private void validateBulkRequest(PriceBulkUpdateRequestDTO request) {
        if (request == null || request.bookIds() == null || request.bookIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cần chọn ít nhất một sách để cập nhật giá.");
        }
        validateProfitRate(request.profitRate());
    }

    private void validateProfitRate(double profitRate) {
        if (!Double.isFinite(profitRate) || profitRate < 0 || profitRate > PriceDAO.MAX_PROFIT_RATE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tỷ suất lợi nhuận phải nằm trong khoảng hợp lệ từ 0% đến 999%.");
        }
    }
}
