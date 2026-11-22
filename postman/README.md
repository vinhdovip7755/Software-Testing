# Bộ Kiểm Thử RESTful API với Postman - BookStore Management System

Thư mục này chứa trọn bộ cấu hình kiểm thử tự động RESTful API cho dịch vụ **`bookstore-api`** (`Spring Boot 3.4.4` chạy tại `http://127.0.0.1:8080`), được thiết kế bám sát tài liệu **API Testing với Postman** và tiêu chuẩn quốc tế **IEEE 829 / ISTQB**.

---

## 1. Danh mục tệp tin & Tài liệu bàn giao

| Tệp tin / Đường dẫn | Mô tả chi tiết |
| :--- | :--- |
| `BookStore_API.postman_collection.json` | **Postman Collection v2.1.0** gồm **21 Folder nghiệp vụ** và **107 Request**, bao phủ **100% toàn bộ 84 endpoint** hiện có trong `api-server` (`49 GET`, `20 POST`, `14 PUT`, `1 DELETE`), kèm kịch bản âm tính, phân quyền RBAC và chuỗi liên hoàn E2E. Đã test **100% PASS (377/377 assertions)**. |
| `BookStore_API_Local.postman_environment.json` | **Postman Environment** chứa biến môi trường `baseUrl`, tài khoản theo từng vai trò RBAC (`Quản lý`, `Admin`, `Nhân viên Nhập hàng`, `Nhân viên Bán hàng`), các biến Bearer Token động và tham số ID mẫu. |
| `docs/10_Kiem_Thu_API_RESTful_API_Testing/API_Test_Cases_Specification.md` | **Đặc tả chi tiết 188 Test Cases** chuẩn IEEE 829 phân loại theo 20 phân hệ và 5 mức nghiệp vụ. |
| `docs/10_Kiem_Thu_API_RESTful_API_Testing/BookStore_API_TestCases_Specification.xlsx` | **File bảng tính Excel 3 Sheets** (Dashboard, Chi tiết 188 Test Cases có màu sắc và AutoFilter, E2E Workflows) tiện nộp bài và in ấn. |
| `docs/10_Kiem_Thu_API_RESTful_API_Testing/BookStore_API_Test_Report.html` | **Báo cáo HTML Dashboard tương tác** mở xem ngay trên trình duyệt (có KPI cards, thanh tìm kiếm, bộ lọc động). |
| `docs/12_Kiem_Thu_Hieu_Nang_Performance_Testing/Performance_Testing_Report.md` | Báo cáo kiểm thử hiệu năng và tải (Response time, p90, p99, throughput, khuyến nghị tối ưu). |
| `docs/11_Kiem_Thu_Bao_Mat_Security_Testing_OWASP/OWASP_API_Security_Report.md` | Báo cáo kiểm thử bảo mật API theo chuẩn quốc tế **OWASP API Security Top 10 (2023)**. |
| `start-api.bat` | Phím tắt 1-click nhấp đúp chuột khởi động API Server trực tiếp từ file JAR siêu tốc (~1.5 giây). |
| `stop-api.bat` | Phím tắt 1-click giải phóng cổng 8080 nếu bị kẹt. |
| `.github/workflows/api-testing.yml` | Pipeline CI/CD tự động build, test và chạy Newman trên GitHub Actions. |

---

## 2. Độ bao phủ 21 Phân hệ & Phương thức HTTP

1. **01. Kiểm tra Sức khỏe Hệ thống (`HealthController`)**: `GET /api/health`
2. **02. Xác thực & Phiên đăng nhập (`AuthController`)**: `POST /api/auth/login` (4 vai trò RBAC), `POST /api/auth/logout`
3. **03. Khôi phục Mật khẩu & OTP (`PasswordResetController`)**: `POST /api/auth/password-reset/request`, `POST /api/auth/password-reset/confirm`
4. **04. Hồ sơ Cá nhân (`ProfileController`)**: `GET /api/profile`, `PUT /api/profile`
5. **05. Quản lý Tác giả (`AuthorController`)**: `GET`, `POST`, `PUT /api/authors/...`
6. **06. Quản lý Thể loại Sách (`CategoryController`)**: `GET`, `GET /{id}`, `POST`, `PUT /api/categories/...`
7. **07. Quản lý Nhà cung cấp (`SupplierController`)**: `GET`, `GET /{id}`, `POST`, `PUT /api/suppliers/...`
8. **08. Quản lý Sách / Sản phẩm (`BookController`)**: `GET`, `GET /active`, `GET /{id}`, `POST`, `PUT /api/books/...`
9. **09. Quản lý Nhập kho & Duyệt phiếu Maker-Checker (`ImportController`)**: `GET`, `GET /{id}/details`, `POST /quick-add-book`, `POST` lập phiếu, `POST /{id}/approve`, `POST /{id}/cancel`
10. **10. Quản lý Tồn kho & Lô sách (`InventoryController`)**: `GET /books`, `GET /books/{bookId}/lots`, `GET /logs`
11. **11. Quản lý Bảng giá (`PriceController`)**: `GET`, `GET /search`, `PUT /{bookId}`, `PUT /bulk`
12. **12. Quản lý Chương trình Khuyến mãi (`PromotionController`)**: `GET`, `GET /search`, `GET /books`, `GET /book/{bookId}`, `GET /{id}/books`, `POST`, `PUT /{id}`
13. **13. Quản lý Khách hàng (`CustomerController`)**: `GET`, `GET /phone/{phone}`, `GET /{id}/statistics`, `POST`, `PUT /{id}`
14. **14. Quản lý Hạng thành viên - Full CRUD (`MembershipRankController`)**: `GET`, `GET /by-points/{point}`, `POST`, `PUT /{id}`, `DELETE /{id}`
15. **15. Tham số Thuế VAT & Hóa đơn POS (`SystemParameterController`, `BillController`)**: `GET /api/system-parameters/vat`, `GET /api/bills`, `GET /{id}`, `GET /{id}/details`, `POST /api/bills`
16. **16. Quản lý Nhân sự (`EmployeeController`)**: `GET`, `GET /without-account`, `GET /{employeeId}/activity`, `POST`, `PUT /{employeeId}`
17. **17. Quản lý Chức vụ & Phân quyền RBAC (`RoleController`)**: `GET`, `GET /{roleId}/permissions`, `POST`, `PUT /{roleId}`
18. **18. Quản lý Tài khoản Hệ thống (`AccountController`)**: `GET`, `POST`, `PUT /{employeeId}`, `PUT /admin`, `POST /{employeeId}/reset-password`
19. **19. Báo cáo & Thống kê Đa chiều (`StatisticsController`)**: Trọn bộ 12 API thống kê Tài chính, Sản phẩm (ngày/tháng/quý/năm/top bán chạy/top bán chậm) và Khách hàng
20. **20. Kiểm thử Bảo mật RBAC, Ràng buộc Biên & Xử lý Lỗi**: Kiểm chứng các mã trạng thái `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict`, `422 Unprocessable Entity`
21. **21. Kịch bản Kiểm thử Chuỗi Nghiệp vụ Liên hoàn (End-to-End E2E Workflows)**:
    - Vòng đời Nhập hàng & Cập nhật Lô kho tự động
    - Vòng đời Bán hàng POS & Thăng hạng Khách hàng tự động
    - Vòng đời Kỷ luật Nhân sự & Thu hồi Phiên làm việc tức thời

---

## 3. Hướng dẫn sử dụng trên Postman

### Bước 1: Khởi động Máy chủ API
* Nhấp đúp chuột vào file **`start-api.bat`** ở thư mục gốc (khởi động siêu tốc trong ~1.5 giây).
* Hoặc mở dự án trong IntelliJ IDEA -> mở `ApiApplication.java` -> bấm nút **Run ▶️**.

### Bước 2: Import Collection & Environment vào Postman
1. Mở **Postman** -> nhấn nút **Import** ở góc trên bên trái.
2. Kéo thả 2 file trong thư mục `postman/`:
   - `BookStore_API.postman_collection.json`
   - `BookStore_API_Local.postman_environment.json`
3. Ở góc trên bên phải giao diện Postman (ô chọn **Environment**), chọn **`BookStore API - Local Development (127.0.0.1:8080)`**.

### Bước 3: Thực thi kiểm thử tự động
1. Bấm chuột phải vào Collection `BookStore Management System - REST API Testing Suite` -> chọn **Run collection**.
2. Chọn **Run type: Functional** (Iterations: 1).
3. Bấm nút cam **Run BookStore Management System...**. Toàn bộ 107 Requests sẽ được thực thi tuần tự và hiển thị kết quả **100% PASS (377/377 assertions)**.
