package com.bookstore.api;

import com.bookstore.api.contract.BillCreateRequestDTO;
import com.bookstore.api.contract.BillCreateResponseDTO;
import com.bookstore.api.contract.BillLineItemRequestDTO;
import com.bookstore.api.contract.BillReceiptLineDTO;
import com.bookstore.bus.BillBUS;
import com.bookstore.bus.BookBUS;
import com.bookstore.bus.CustomerBUS;
import com.bookstore.bus.MembershipRankBUS;
import com.bookstore.bus.PromotionBUS;
import com.bookstore.bus.SystemParameterBUS;
import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;
import com.bookstore.dto.BookDTO;
import com.bookstore.dto.CustomerDTO;
import com.bookstore.dto.MembershipRankDTO;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bills")
public class BillController {
    private final AuthSessionStore sessionStore;

    public BillController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<BillDTO> getBills(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_BILL");
        return new BillBUS().getAllBills();
    }

    @GetMapping("/{id}")
    public BillDTO getBill(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int id
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_BILL");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã hóa đơn không hợp lệ.");
        }
        BillDTO bill = new BillBUS().getBillById(id);
        if (bill == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn.");
        }
        return bill;
    }

    @GetMapping("/{id}/details")
    public List<BillDetailDTO> getBillDetails(
            @RequestHeader("Authorization") String authorization,
            @PathVariable int id
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_BILL");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã hóa đơn không hợp lệ.");
        }
        return new BillBUS().getBillDetailsByBillId(id);
    }

    @PostMapping
    public ResponseEntity<BillCreateResponseDTO> createBill(
            @RequestHeader("Authorization") String authorization,
            @RequestBody BillCreateRequestDTO request
    ) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_SELLING");
        validateRequest(request);

        List<BookDTO> activeBooks = new BookBUS().selectActiveBooks();
        Map<Integer, BookDTO> booksById = activeBooks.stream()
                .collect(Collectors.toMap(BookDTO::getBookId, book -> book));
        PromotionBUS promotionBUS = new PromotionBUS();
        promotionBUS.updateAutoStatus();
        List<BillDetailDTO> billDetails = new ArrayList<>();
        List<BillReceiptLineDTO> receiptDetails = new ArrayList<>();
        double subtotalAfterPromotion = 0;

        for (BillLineItemRequestDTO requestedLine : request.details()) {
            BookDTO book = booksById.get(requestedLine.bookId());
            if (book == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sách không tồn tại hoặc đã ngừng bán.");
            }

            double promotionPercent = promotionBUS.getPromotionPercentByBookId(book.getBookId());
            if (!Double.isFinite(promotionPercent) || promotionPercent < 0 || promotionPercent > 100
                    || !Double.isFinite(book.getSellingPrice()) || book.getSellingPrice() < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Giá bán hoặc khuyến mãi của sách không hợp lệ.");
            }

            double unitPrice = book.getSellingPrice() * (1 - promotionPercent / 100.0);
            double rawLineTotal = unitPrice * requestedLine.quantity();
            if (!Double.isFinite(rawLineTotal)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thành tiền của sách không hợp lệ.");
            }
            double lineTotal = roundVnd(rawLineTotal);
            subtotalAfterPromotion += lineTotal;
            if (!Double.isFinite(subtotalAfterPromotion)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng hóa đơn không hợp lệ.");
            }
            billDetails.add(new BillDetailDTO(
                    0,
                    book.getBookId(),
                    book.getBookName(),
                    requestedLine.quantity(),
                    lineTotal / requestedLine.quantity()
            ));
            receiptDetails.add(new BillReceiptLineDTO(
                    book.getBookId(),
                    book.getBookName(),
                    requestedLine.quantity(),
                    book.getSellingPrice(),
                    promotionPercent,
                    lineTotal
            ));
        }

        double memberDiscountPercent = 0;
        String customerName = "Khách lẻ";
        if (request.customerId() > 0) {
            CustomerDTO customer = new CustomerBUS().selectById(request.customerId());
            if (customer == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng.");
            }
            customerName = customer.getCustomerName();
            MembershipRankDTO rank = new MembershipRankBUS().getRankByPoint(customer.getPoint());
            memberDiscountPercent = rank.getDiscountPercent();
            if (!Double.isFinite(memberDiscountPercent)
                    || memberDiscountPercent < 0 || memberDiscountPercent > 100) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Mức giảm giá thành viên không hợp lệ.");
            }
        }

        SystemParameterBUS parameterBUS = new SystemParameterBUS();
        double taxRate = parameterBUS.getVAT();
        int pointsPer10K = parameterBUS.getEarnedPointsPer10K();
        if (!Double.isFinite(taxRate) || taxRate < 0 || taxRate > 1 || pointsPer10K < 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Cấu hình thuế hoặc điểm thưởng không hợp lệ.");
        }

        double totalAfterMemberDiscount = subtotalAfterPromotion * (1 - memberDiscountPercent / 100.0);
        double rawTotalBillPrice = totalAfterMemberDiscount * (1 + taxRate);
        if (!Double.isFinite(rawTotalBillPrice)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tổng hóa đơn không hợp lệ.");
        }
        double totalBillPrice = roundVnd(rawTotalBillPrice);
        double earnedPointsValue = Math.floor(totalBillPrice / 10000) * pointsPer10K;
        if (!Double.isFinite(earnedPointsValue) || earnedPointsValue > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điểm thưởng vượt giới hạn.");
        }
        int earnedPoints = (int) earnedPointsValue;

        BillDTO bill = new BillDTO(
                0,
                null,
                totalBillPrice,
                taxRate,
                session.employee().getEmployeeId(),
                request.customerId(),
                request.paymentMethodId(),
                earnedPoints
        );
        int billId = new BillBUS().createBill(bill, billDetails);
        if (billId <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Không thể hoàn tất hóa đơn; vui lòng kiểm tra tồn kho và thử lại.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BillCreateResponseDTO(
                        billId,
                        totalBillPrice,
                        taxRate,
                        earnedPoints,
                        customerName,
                        List.copyOf(receiptDetails)
                ));
    }

    private void validateRequest(BillCreateRequestDTO request) {
        if (request == null || request.customerId() < 0 || request.paymentMethodId() <= 0
                || request.details() == null || request.details().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hóa đơn cần phương thức thanh toán hợp lệ và ít nhất một sách.");
        }
        for (BillLineItemRequestDTO line : request.details()) {
            if (line == null || line.bookId() <= 0 || line.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin sách trong hóa đơn không hợp lệ.");
            }
        }
    }

    private double roundVnd(double amount) {
        return BigDecimal.valueOf(amount).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
    }
}
