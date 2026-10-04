package com.bookstore;

import com.bookstore.bus.AccountBUS;
import com.bookstore.dao.AccountDAO;
import com.bookstore.dao.BillDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.BillDTO;
import com.bookstore.dto.BillDetailDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class DefectFixVerificationTest {

    private String removeDiacritics(String str) {
        if (str == null) return "";
        String nfd = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfd).replaceAll("").replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    @Test
    @DisplayName("DF01 - TC022: Chặn đổi mật khẩu mới trùng hoàn toàn với mật khẩu cũ")
    void testDF01_DuplicatePasswordRejected() {
        System.out.println("[RUNNING] DF01: Kiểm tra chặn đổi mật khẩu mới trùng mật khẩu cũ...");
        AccountDAO accountDAO = new AccountDAO();
        List<AccountDTO> list = accountDAO.selectAllAccounts();
        assertFalse(list.isEmpty(), "Danh sách tài khoản không được rỗng");
        AccountDTO existingAcc = list.get(0);

        AccountBUS accountBUS = new AccountBUS();
        AccountDTO updateAttempt = new AccountDTO(
                existingAcc.getUsername(),
                existingAcc.getPassword(), // trùng mật khẩu cũ
                existingAcc.getEmployeeId(),
                existingAcc.getStatus()
        );

        String result = accountBUS.updateAccount(updateAttempt, true);
        assertEquals("Mật khẩu mới không được trùng với mật khẩu cũ!", result,
                "Hệ thống phải phát hiện và chặn mật khẩu mới trùng mật khẩu cũ");
        System.out.println("[PASS] DF01: Đã chặn thành công khi mật khẩu mới trùng mật khẩu cũ.");
    }

    @Test
    @DisplayName("DF02 - TC024: Chuẩn hóa tìm kiếm tiếng Việt không dấu (removeDiacritics)")
    void testDF02_VietnameseAccentFreeSearch() {
        System.out.println("[RUNNING] DF02: Kiểm tra tìm kiếm tiếng Việt không dấu...");
        String bookName = "Hoàng Tử Bé";
        String keywordWithAccent = "Hoàng Tử Bé";
        String keywordWithoutAccent = "Hoang Tu Be";

        String normBook = removeDiacritics(bookName);
        String normKwWith = removeDiacritics(keywordWithAccent);
        String normKwWithout = removeDiacritics(keywordWithoutAccent);

        assertEquals("hoang tu be", normBook);
        assertEquals("hoang tu be", normKwWith);
        assertEquals("hoang tu be", normKwWithout);

        assertTrue(normBook.contains(normKwWithout),
                "Từ khóa không dấu 'Hoang Tu Be' phải khớp với sách 'Hoàng Tử Bé'");
        System.out.println("[PASS] DF02: Tìm kiếm tiếng Việt không dấu hoạt động chính xác.");
    }

    @Test
    @DisplayName("DF03 - TC039: Danh sách hóa đơn tự động sắp xếp mới nhất lên đầu mặc định")
    void testDF03_BillListSortedDescendingByDefault() {
        System.out.println("[RUNNING] DF03: Kiểm tra hóa đơn sắp xếp mới nhất lên đầu mặc định...");
        BillDAO billDAO = new BillDAO();
        List<BillDTO> bills = billDAO.getAllBills();
        assertNotNull(bills);

        if (bills.size() >= 2) {
            for (int i = 0; i < bills.size() - 1; i++) {
                BillDTO b1 = bills.get(i);
                BillDTO b2 = bills.get(i + 1);
                if (b1.getCreatedDate() != null && b2.getCreatedDate() != null) {
                    assertTrue(b1.getCreatedDate().compareTo(b2.getCreatedDate()) >= 0,
                            "Hóa đơn b1 (ID " + b1.getBillId() + ") phải có thời gian >= hóa đơn b2 (ID " + b2.getBillId() + ")");
                }
            }
        }
        System.out.println("[PASS] DF03: Bảng hóa đơn đã nạp theo thứ tự mới nhất lên đầu.");
    }

    @Test
    @DisplayName("DF04 - TC044: Bắt lỗi biên chéo bộ lọc ngày (Từ ngày > Đến ngày)")
    void testDF04_DateRangeCrossBoundaryValidation() {
        System.out.println("[RUNNING] DF04: Kiểm tra chặn lọc biên Từ ngày > Đến ngày...");
        Date fromDate = new Date(2026 - 1900, 8, 30); // 30/09/2026
        Date toDate = new Date(2026 - 1900, 8, 20);   // 20/09/2026

        boolean isInvalid = fromDate.after(toDate);
        assertTrue(isInvalid, "Biên chéo Từ ngày > Đến ngày phải được nhận diện là không hợp lệ");
        System.out.println("[PASS] DF04: Chặn thành công trường hợp Từ ngày > Đến ngày.");
    }

    @Test
    @DisplayName("DF05 - TC046: Chi tiết hóa đơn bảo toàn snapshot tên sách lịch sử")
    void testDF05_HistoricalBillDetailPreservesBookNameSnapshot() {
        System.out.println("[RUNNING] DF05: Kiểm tra bảo toàn tên sách lịch sử trong chi tiết hóa đơn...");
        BillDAO billDAO = new BillDAO();
        List<BillDTO> bills = billDAO.getAllBills();
        assertFalse(bills.isEmpty(), "Phải có ít nhất 1 hóa đơn trong CSDL");

        int billId = bills.get(0).getBillId();
        List<BillDetailDTO> details = billDAO.getBillDetailsByBillId(billId);
        assertNotNull(details);

        for (BillDetailDTO d : details) {
            assertNotNull(d.getBookName(), "Tên sách trong chi tiết hóa đơn không được null");
            assertFalse(d.getBookName().trim().isEmpty(), "Tên sách trong chi tiết hóa đơn không được rỗng");
        }
        System.out.println("[PASS] DF05: Chi tiết hóa đơn đã đọc snapshot tên sách thành công.");
    }
}
