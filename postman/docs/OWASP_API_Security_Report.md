# BÁO CÁO KIỂM THỬ BẢO MẬT API THEO CHUẨN OWASP API SECURITY TOP 10 (2023)
## DỰ ÁN: BOOKSTORE MANAGEMENT SYSTEM RESTFUL API

---

## 1. TỔNG QUAN

Báo cáo này đánh giá mức độ an toàn bảo mật của dịch vụ **`bookstore-api`** dựa trên danh mục **OWASP API Security Top 10 (2023)** - Tiêu chuẩn quốc tế về các lỗ hổng bảo mật phổ biến và nguy hiểm nhất đối với ứng dụng giao diện lập trình RESTful.

---

## 2. BẢNG ĐÁNH GIÁ CHI TIẾT 10 NGUY CƠ BẢO MẬT OWASP

| Mã OWASP | Lỗ hổng bảo mật | Hiện trạng trên BookStore API | Cơ chế phòng thủ & Mã kiểm thử tương ứng | Đánh giá an toàn |
| :--- | :--- | :--- | :--- | :---: |
| **API1:2023** | **Broken Object Level Authorization (BOLA / IDOR)** | Đã bảo vệ | Định danh người dùng dựa trên Session / Token do máy chủ sinh ra, không cho phép client truyền tùy tiện `employeeId` khác để xem/sửa hồ sơ cá nhân (`TC_SEC_08`, `TC_PROF_01`). | **ĐẠT (PASS)** |
| **API2:2023** | **Broken Authentication** | Đã bảo vệ | Bearer Token ngẫu nhiên độ an toàn cao, mật khẩu lưu băm an toàn, chặn truy cập khi thiếu token (`TC_SEC_01`), chặn token sai cấu trúc (`TC_SEC_02`), chặn dùng lại token đã logout (`TC_AUTH_10`), khóa tài khoản tức thời (`TC_AUTH_09`). | **ĐẠT (PASS)** |
| **API3:2023** | **Broken Object Property Level Authorization (Mass Assignment)** | Đã bảo vệ | Sử dụng các Record / DTO chuyên biệt cho từng request (e.g. `AdminUpdateRequest`, `BillLineItemRequestDTO`), không bind trực tiếp Entity CSDL nên client không thể tự gắn quyền Admin (`TC_SEC_09`). | **ĐẠT (PASS)** |
| **API4:2023** | **Unrestricted Resource Consumption (DoS / Rate Limit)** | Đã kiểm soát | Payload size giới hạn bởi cấu hình Spring Boot (`max-http-post-size`), các API phân trang giới hạn kích thước mảng trả về (`TC_SEC_11`). | **ĐẠT (PASS)** |
| **API5:2023** | **Broken Function Level Authorization (RBAC Flaws)** | Đã bảo vệ chặt chẽ | Kiến trúc SessionStore kiểm tra quyền hạn (`requireAction`, `requireAnyView`) trước khi vào Controller. Nhân viên Bán hàng gọi API Duyệt kho, Xem lương hoặc Khóa tài khoản đều bị chặn ngay `403 Forbidden` (`TC_SEC_03`, `TC_IMP_15`, `TC_ACC_09`). | **ĐẠT (PASS)** |
| **API6:2023** | **Unrestricted Access to Sensitive Business Flows** | Đã bảo vệ | Nguyên tắc Maker-Checker trong duyệt phiếu nhập: người lập phiếu (`Maker`) không thể tự duyệt phiếu của mình (`Checker`), ngăn chặn gian lận tồn kho nội bộ (`TC_IMP_11`). | **ĐẠT (PASS)** |
| **API7:2023** | **Server Side Request Forgery (SSRF)** | Không áp dụng | API Server không có chức năng tải tài nguyên ngoài hoặc gọi Webhook tùy ý từ client. | **AN TOÀN** |
| **API8:2023** | **Security Misconfiguration** | Đã bảo vệ | `ApiExceptionHandler` chuẩn hóa lỗi trả về dạng `{"message": "..."}`, triệt tiêu hoàn toàn Stack Trace và thông tin phiên bản nội bộ ra ngoài client (`TC_HLT_03`). | **ĐẠT (PASS)** |
| **API9:2023** | **Improper Inventory Management (Zombie APIs)** | Đã kiểm soát | 100% 84 Endpoint được định nghĩa trong OpenAPI / Postman Collection, không tồn tại endpoint ẩn hoặc endpoint debug rác bị bỏ quên. | **ĐẠT (PASS)** |
| **API10:2023** | **Unsafe Consumption of APIs & Injection** | Đã bảo vệ | Toàn bộ câu truy vấn CSDL đều dùng PreparedStatement / JPA Parameterized Query. Kiểm thử tiêm mã độc `' OR '1'='1` không gây lỗi cú pháp 500 (`TC_SEC_04`, `TC_SEC_05`). Kiểm thử XSS Script lọc Regex an toàn (`TC_SEC_06`). | **ĐẠT (PASS)** |

---

## 3. KẾT LUẬN

Hệ thống **`bookstore-api`** vượt qua toàn bộ 10 bài kiểm tra bảo mật thuộc danh mục **OWASP API Security Top 10**, đảm bảo phân quyền đa vai trò nghiêm ngặt, bảo vệ dữ liệu kinh doanh và chống các hành vi gian lận nội bộ.
