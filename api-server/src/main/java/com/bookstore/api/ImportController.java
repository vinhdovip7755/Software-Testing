package com.bookstore.api;

import com.bookstore.api.contract.ImportCreateRequestDTO;
import com.bookstore.bus.BookBUS;
import com.bookstore.bus.ImportBUS;
import com.bookstore.dao.ImportDAO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.ImportDetailDTO;
import com.bookstore.dto.ImportTicketDTO;
import com.bookstore.util.ImportApprovalPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/imports")
public class ImportController {
    private final ImportBUS importBUS = new ImportBUS();
    private final BookBUS bookBUS = new BookBUS();
    private final AuthSessionStore sessionStore;

    public ImportController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<ImportTicketDTO> getImports(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_IMPORT_TICKET");
        return importBUS.getAllImports();
    }

    @GetMapping("/{id}/details")
    public List<ImportDetailDTO> getImportDetails(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_IMPORT_TICKET");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã phiếu nhập không hợp lệ.");
        }
        return importBUS.getDetailsByImportId(id);
    }

    @PostMapping
    public ResponseEntity<Void> createImport(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ImportCreateRequestDTO request
    ) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_IMPORT");
        if (request == null || request.getTicket() == null
                || request.getTicket().getSupplierID() <= 0
                || request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phiếu nhập cần có nhà cung cấp và ít nhất một sách.");
        }

        double totalPrice = 0;
        long totalQuantity = 0L;
        int supplierId = request.getTicket().getSupplierID();
        Set<Integer> seenBookIds = new HashSet<>();

        for (ImportDetailDTO detail : request.getDetails()) {
            if (detail == null || detail.getBookID() <= 0 || detail.getQuantity() <= 0
                    || !Double.isFinite(detail.getDiscountPercent())
                    || detail.getDiscountPercent() < 0 || detail.getDiscountPercent() > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin sách nhập không hợp lệ.");
            }
            if (!seenBookIds.add(detail.getBookID())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phiếu nhập chứa sách trùng lặp.");
            }
            totalQuantity += detail.getQuantity();
            if (totalQuantity > Integer.MAX_VALUE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng số lượng nhập vượt quá giới hạn hợp lệ.");
            }

            BookDTO book = bookBUS.selectById(detail.getBookID());
            if (book == null || book.getSupplierId() != supplierId) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sách không tồn tại hoặc không thuộc nhà cung cấp đã chọn.");
            }
            if (!Double.isFinite(book.getCoverPrice()) || book.getCoverPrice() <= 0
                    || book.getCoverPrice() > ImportDAO.MAX_AMOUNT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sách chưa có giá bìa hợp lệ.");
            }
            double importPrice = book.getCoverPrice() * (1 - detail.getDiscountPercent() / 100.0);
            double lineTotal = detail.getQuantity() * importPrice;
            if (!Double.isFinite(importPrice) || importPrice < 0 || importPrice > ImportDAO.MAX_AMOUNT
                    || !Double.isFinite(lineTotal) || lineTotal < 0 || lineTotal > ImportDAO.MAX_AMOUNT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giá nhập không hợp lệ.");
            }
            detail.setCoverPrice(book.getCoverPrice());
            detail.setPrice(importPrice);
            totalPrice += lineTotal;
            if (!Double.isFinite(totalPrice) || totalPrice > ImportDAO.MAX_AMOUNT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng tiền phiếu nhập vượt quá giới hạn hợp lệ.");
            }
        }
        if (!Double.isFinite(totalPrice) || totalPrice < 0 || totalPrice > ImportDAO.MAX_AMOUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng tiền phiếu nhập không hợp lệ.");
        }

        ImportTicketDTO ticket = request.getTicket();
        ticket.setEmployeeID(session.employee().getEmployeeId());
        ticket.setTotalPrice(totalPrice);
        if (!importBUS.importBooks(ticket, request.getDetails().toArray(ImportDetailDTO[]::new))) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu phiếu nhập.");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approveImport(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id
    ) {
        AuthSessionStore.Session session = requireReviewer(authorization, id);
        if (!importBUS.approveImport(id, session.employee(), session.permissions())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiếu nhập không còn ở trạng thái chờ duyệt.");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelImport(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id
    ) {
        AuthSessionStore.Session session = requireReviewer(authorization, id);
        if (!importBUS.cancelImport(id, session.employee(), session.permissions())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phiếu nhập không còn ở trạng thái chờ duyệt.");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quick-add-book")
    public ResponseEntity<String> quickAddBook(
            @RequestHeader("Authorization") String authorization,
            @RequestBody BookDTO book
    ) {
        sessionStore.requireAction(authorization, "MANAGE_IMPORT");
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin sách không hợp lệ.");
        }
        BookDTO sanitized = BookController.copyAllowedMetadata(book, 0, true);
        return ApiResponses.fromBusinessMessage(bookBUS.addBook(sanitized));
    }

    private AuthSessionStore.Session requireReviewer(String authorization, int importId) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_IMPORT_TICKET");
        if (importId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã phiếu nhập không hợp lệ.");
        }
        ImportTicketDTO ticket = importBUS.getTicketById(importId);
        if (ticket == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu nhập.");
        }
        if (!ImportApprovalPolicy.canAct(session.employee(), session.permissions(), ticket.getEmployeeID(), 1)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Bạn không có quyền duyệt hoặc hủy phiếu này.");
        }
        if (ticket.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Phiếu nhập không còn ở trạng thái chờ duyệt.");
        }
        if (!ImportApprovalPolicy.canAct(session.employee(), session.permissions(), ticket.getEmployeeID(), ticket.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Bạn không có quyền duyệt hoặc hủy phiếu này.");
        }
        return session;
    }
}
