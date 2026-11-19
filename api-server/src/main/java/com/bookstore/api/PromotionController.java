package com.bookstore.api;

import com.bookstore.api.contract.BookPromotionDTO;
import com.bookstore.api.contract.PromotionSaveRequestDTO;
import com.bookstore.bus.BookBUS;
import com.bookstore.bus.PromotionBUS;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.PromotionDTO;
import com.bookstore.util.AppConstant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private final AuthSessionStore sessionStore;

    public PromotionController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<PromotionDTO> getPromotions(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_PROMOTION");
        PromotionBUS promotionBUS = new PromotionBUS();
        promotionBUS.updateAutoStatus();
        return promotionBUS.selectAllPromotions();
    }

    @GetMapping("/search")
    public List<PromotionDTO> searchPromotions(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "type", defaultValue = "Tên") String type
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_PROMOTION");
        PromotionBUS promotionBUS = new PromotionBUS();
        promotionBUS.updateAutoStatus();
        return promotionBUS.search(keyword, type);
    }

    @GetMapping("/books")
    public List<BookDTO> suggestBooks(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "categoryId", defaultValue = "0") int categoryId
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_PROMOTION");
        if (categoryId < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã thể loại không hợp lệ.");
        }
        return new PromotionBUS().suggestBooksForManagement(keyword, categoryId);
    }

    @GetMapping("/book/{bookId}")
    public BookPromotionDTO getPromotionForBook(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("bookId") int bookId
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_SELLING");
        if (bookId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã sách không hợp lệ.");
        }
        PromotionBUS promotionBUS = new PromotionBUS();
        promotionBUS.updateAutoStatus();
        return new BookPromotionDTO(
                bookId,
                promotionBUS.getPromotionNameByBookId(bookId),
                promotionBUS.getPromotionPercentByBookId(bookId)
        );
    }

    @GetMapping("/{id}/books")
    public List<Integer> getSelectedBookIds(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_PROMOTION");
        PromotionBUS promotionBUS = new PromotionBUS();
        if (id <= 0 || promotionBUS.selectAllPromotions().stream()
                .noneMatch(promotion -> promotion.getPromotionId() == id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi.");
        }
        return promotionBUS.getSelectedBookIds(id);
    }

    @PostMapping
    public ResponseEntity<Void> createPromotion(
            @RequestHeader("Authorization") String authorization,
            @RequestBody PromotionSaveRequestDTO request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_PROMOTION");
        validateRequest(request);
        if (!new PromotionBUS().savePromotion(
                false,
                null,
                request.promotionName().trim(),
                request.percent(),
                request.startDate(),
                request.endDate(),
                request.status(),
                request.bookIds()
        )) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể lưu chương trình khuyến mãi.");
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePromotion(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id,
            @RequestBody PromotionSaveRequestDTO request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_PROMOTION");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã chương trình khuyến mãi không hợp lệ.");
        }
        PromotionBUS promotionBUS = new PromotionBUS();
        if (promotionBUS.selectAllPromotions().stream()
                .noneMatch(promotion -> promotion.getPromotionId() == id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chương trình khuyến mãi.");
        }
        validateRequest(request);
        PromotionDTO promotion = new PromotionDTO(
                id,
                request.promotionName().trim(),
                request.percent(),
                request.startDate(),
                request.endDate(),
                request.status()
        );
        if (!promotionBUS.savePromotion(
                true,
                promotion,
                request.promotionName().trim(),
                request.percent(),
                request.startDate(),
                request.endDate(),
                request.status(),
                request.bookIds()
        )) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Không thể cập nhật chương trình khuyến mãi.");
        }
        return ResponseEntity.noContent().build();
    }

    private void validateRequest(PromotionSaveRequestDTO request) {
        if (request == null || request.promotionName() == null || request.promotionName().isBlank()
                || !Double.isFinite(request.percent()) || request.percent() <= 0 || request.percent() > 100
                || request.startDate() == null || request.endDate() == null
                || request.status() < 0 || request.status() > 1
                || request.bookIds() == null || request.bookIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin chương trình khuyến mãi không hợp lệ.");
        }
        if (request.startDate().after(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ngày bắt đầu không được lớn hơn ngày kết thúc.");
        }
        if (AppConstant.isDateOutOfBounds(request.startDate())
                || AppConstant.isDateOutOfBounds(request.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Khoảng ngày khuyến mãi phải nằm trong khoảng từ 01/01/2026 đến 31/12/2099.");
        }

        Set<Integer> selectedBookIds = new HashSet<>();
        for (Integer bookId : request.bookIds()) {
            if (bookId == null || bookId <= 0 || !selectedBookIds.add(bookId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách sách khuyến mãi không hợp lệ.");
            }
        }
        Set<Integer> existingBookIds = new BookBUS().selectAllBooks().stream()
                .map(BookDTO::getBookId)
                .collect(java.util.stream.Collectors.toSet());
        if (!existingBookIds.containsAll(selectedBookIds)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách có sách không tồn tại.");
        }
    }
}
