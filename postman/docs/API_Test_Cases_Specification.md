# BẢNG ĐẶC TẢ CHI TIẾT 188 TEST CASES RESTFUL API - BOOKSTORE MANAGEMENT SYSTEM
## BAO PHỦ TOÀN DIỆN MỌI THAO TÁC NGHIỆP VỤ THỰC TẾ & KỊCH BẢN LỖI NGƯỜI DÙNG

- **Dự án:** Hệ thống Quản lý Nhà sách (BookStore Management System)
- **Module API:** `api-server` (Spring Boot 3.4.4, Chạy tại `http://127.0.0.1:8080`)
- **Cơ sở dữ liệu:** MySQL 8.0 (`bookstore_db`)
- **Tiêu chuẩn kiểm thử:** IEEE 829 / ISTQB Standard for Software Test Documentation
- **Tác giả:** Đội ngũ Đảm bảo Chất lượng Phần mềm (QA/QC Team)

---

## 1. TỔNG QUAN & MA TRẬN PHÂN BỐ TEST CASE

| STT | Phân hệ Nghiệp vụ | Controller Phụ Trách | Số Lượng Test Cases | Mức Độ Nghiệp Vụ Bao Quát |
| :---: | :--- | :--- | :---: | :--- |
| 01 | **Sức khỏe Hệ thống (Health Check)** | `HealthController` | **4 TCs** | Mức 1, 2, 3, 4, 5 |
| 02 | **Xác thực & Quản lý Phiên Đăng nhập (Authentication)** | `AuthController` | **11 TCs** | Mức 1, 2, 3, 4, 5 |
| 03 | **Khôi phục Mật khẩu & Xác thực OTP (Password Reset)** | `PasswordResetController` | **7 TCs** | Mức 1, 2, 3, 4, 5 |
| 04 | **Hồ sơ Cá nhân (User Profile)** | `ProfileController` | **7 TCs** | Mức 1, 2, 3, 4, 5 |
| 05 | **Quản lý Tác giả (Authors)** | `AuthorController` | **8 TCs** | Mức 1, 2, 3, 4, 5 |
| 06 | **Quản lý Thể loại Sách (Categories)** | `CategoryController` | **8 TCs** | Mức 1, 2, 3, 4, 5 |
| 07 | **Quản lý Nhà cung cấp (Suppliers)** | `SupplierController` | **9 TCs** | Mức 1, 2, 3, 4, 5 |
| 08 | **Quản lý Sách & Danh mục Hàng hóa (Books)** | `BookController` | **13 TCs** | Mức 1, 2, 3, 4, 5 |
| 09 | **Quản lý Nhập kho & Phê duyệt Maker-Checker (Imports)** | `ImportController` | **15 TCs** | Mức 1, 2, 3, 4, 5 |
| 10 | **Quản lý Tồn kho & Lô hàng (Inventory)** | `InventoryController` | **6 TCs** | Mức 1, 2, 3, 4, 5 |
| 11 | **Quản lý Bảng giá & Lợi nhuận (Prices)** | `PriceController` | **8 TCs** | Mức 1, 2, 3, 4, 5 |
| 12 | **Quản lý Chương trình Khuyến mãi (Promotions)** | `PromotionController` | **10 TCs** | Mức 1, 2, 3, 4, 5 |
| 13 | **Quản lý Khách hàng & Điểm thưởng (Customers)** | `CustomerController` | **10 TCs** | Mức 1, 2, 3, 4, 5 |
| 14 | **Quản lý Hạng thành viên - Full CRUD (Membership Ranks)** | `MembershipRankController` | **9 TCs** | Mức 1, 2, 3, 4, 5 |
| 15 | **Bán hàng tại quầy & Hóa đơn POS (Bills)** | `BillController` | **14 TCs** | Mức 1, 2, 3, 4, 5 |
| 16 | **Quản lý Nhân sự & Tiền lương (Employees)** | `EmployeeController` | **10 TCs** | Mức 1, 2, 3, 4, 5 |
| 17 | **Phân quyền Chức vụ & Ma trận RBAC (Roles & Permissions)** | `RoleController` | **8 TCs** | Mức 1, 2, 3, 4, 5 |
| 18 | **Quản lý Tài khoản Hệ thống (Accounts)** | `AccountController` | **9 TCs** | Mức 1, 2, 3, 4, 5 |
| 19 | **Báo cáo & Thống kê Đa chiều (Statistics)** | `StatisticsController` | **11 TCs** | Mức 1, 2, 3, 4, 5 |
| 20 | **An toàn Bảo mật & Toàn vẹn Hệ thống (Security & Tampering)** | `SecurityFilter & Controllers` | **11 TCs** | Mức 1, 2, 3, 4, 5 |
| **Tổng** | **TOÀN BỘ 20 PHÂN HỆ** | **20 Controllers** | **188 TEST CASES** | **Bao phủ 100% nghiệp vụ thực tế** |

---

## 2. PHÂN LOẠI 5 MỨC ĐỘ KIỂM THỬ NGHIỆP VỤ

Mỗi test case trong tài liệu này được phân bổ chặt chẽ theo 5 tầng xử lý nghiệp vụ của phần mềm:

```
+-----------------------------------------------------------------------------------------+
| Mức 1: Luồng chuẩn (Happy Path)               -> HTTP 200 OK / 201 Created / 204        |
+-----------------------------------------------------------------------------------------+
| Mức 2: Dữ liệu đầu vào & Ràng buộc biên       -> HTTP 400 Bad Request / 422             |
+-----------------------------------------------------------------------------------------+
| Mức 3: Toàn vẹn Dữ liệu & Ràng buộc Duy nhất  -> HTTP 404 Not Found / 409 Conflict      |
+-----------------------------------------------------------------------------------------+
| Mức 4: Quy tắc nghiệp vụ Cốt lõi & Trạng thái -> HTTP 400 / 403 / 409 Conflict / 422    |
+-----------------------------------------------------------------------------------------+
| Mức 5: Bảo mật & Phân quyền Đa vai trò (RBAC) -> HTTP 401 Unauthorized / 403 Forbidden  |
+-----------------------------------------------------------------------------------------+
```

---

## 3. BẢNG ĐẶC TẢ CHI TIẾT 188 TEST CASES THEO TỪNG PHÂN HỆ

### Phân hệ 01: Sức khỏe Hệ thống (Health Check)
* **Controller:** `HealthController`
* **Mô tả nghiệp vụ:** Kiểm tra tính sẵn sàng hoạt động của dịch vụ Spring Boot REST API. Là endpoint cơ sở phục vụ cho giám sát hệ thống (Health Check / Uptime Monitor).

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_HLT_01** | Kiểm tra trạng thái hoạt động bình thường | Mức 1 (Happy Path) | Khách/Hệ thống | `GET /api/health` | None | Status: 200 OK | Body: {"status": "UP"} | Hệ thống giám sát hoặc tester kiểm tra xem server có đang chạy hay không. | High |
| **TC_HLT_02** | Kiểm tra tính tương thích Header Accept | Mức 1 (Happy Path) | Khách/Hệ thống | `GET /api/health` | `Header: Accept: application/json` | Status: 200 OK | Header: Content-Type: application/json | Client yêu cầu định dạng phản hồi chuẩn JSON. | Medium |
| **TC_HLT_03** | Gọi sai phương thức HTTP (POST thay vì GET) | Mức 2 (Boundary) | Khách/Hệ thống | `POST /api/health` | `Body: {}` | Status: 405 Method Not Allowed | Người dùng hoặc client gọi nhầm method POST cho endpoint chỉ hỗ trợ GET. | Low |
| **TC_HLT_04** | Gọi endpoint với tham số query thừa | Mức 2 (Boundary) | Khách/Hệ thống | `GET /api/health?param=redundant` | `Query Param: param=redundant` | Status: 200 OK | Body: {"status": "UP"} | Client vô tình gắn thêm query param rác trên URL nhưng server vẫn xử lý bình thường. | Low |

---

### Phân hệ 02: Xác thực & Quản lý Phiên Đăng nhập (Authentication)
* **Controller:** `AuthController`
* **Mô tả nghiệp vụ:** Xác thực danh tính người dùng thông qua Email và Mật khẩu, cấp phát Bearer Access Token tương ứng với vai trò và ma trận quyền.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTH_01** | Đăng nhập thành công với vai trò Quản lý | Mức 1 (Happy Path) | Quản lý | `POST /api/auth/login` | `{"username": "vinhyt7755@gmail.com", "password": "07072006"}` | Status: 200 OK | Body chứa accessToken và thông tin employee, permissions | Quản lý bắt đầu ca làm việc. | Critical |
| **TC_AUTH_02** | Đăng nhập thành công với vai trò Quản trị viên (Admin) | Mức 1 (Happy Path) | Admin | `POST /api/auth/login` | `{"username": "hotro.adminhethong@gmail.com", "password": "admin"}` | Status: 200 OK | Cấp quyền quản trị cao nhất | Admin đăng nhập để cấu hình phân quyền hoặc quản trị tài khoản. | Critical |
| **TC_AUTH_03** | Đăng nhập thành công với vai trò Nhân viên Nhập hàng | Mức 1 (Happy Path) | Nhân viên kho | `POST /api/auth/login` | `{"username": "nhan15042006@gmail.com", "password": "15042006"}` | Status: 200 OK | Cấp quyền lập phiếu nhập hàng | Thủ kho đăng nhập để chuẩn bị nhập kho lô sách mới. | High |
| **TC_AUTH_04** | Đăng xuất phiên làm việc hiện tại | Mức 1 (Happy Path) | Người dùng | `POST /api/auth/logout` | `Header: Authorization: Bearer {{token}}` | Status: 204 No Content | Hủy hiệu lực token hiện tại | Người dùng bấm Đăng xuất khi kết thúc ca làm việc. | High |
| **TC_AUTH_05** | Đăng nhập thất bại - Mật khẩu không chính xác | Mức 2 (Validation) | Người dùng | `POST /api/auth/login` | `{"username": "vinhyt7755@gmail.com", "password": "SaiMatKhau!@#"}` | Status: 401 Unauthorized | {"message": "Mật khẩu không chính xác!"} | Người dùng gõ nhầm mật khẩu, bật nhầm phím CapsLock hoặc quên mật khẩu. | Critical |
| **TC_AUTH_06** | Đăng nhập thất bại - Email để trống | Mức 2 (Validation) | Người dùng | `POST /api/auth/login` | `{"username": "", "password": "07072006"}` | Status: 400 Bad Request | {"message": "Email không được để trống."} | Người dùng nhấn Enter hoặc click Đăng nhập khi chưa kịp điền Email. | Medium |
| **TC_AUTH_07** | Đăng nhập thất bại - Mật khẩu để trống | Mức 2 (Validation) | Người dùng | `POST /api/auth/login` | `{"username": "vinhyt7755@gmail.com", "password": ""}` | Status: 400 Bad Request | Báo lỗi mật khẩu bắt buộc | Người dùng chỉ nhập Email và bấm Đăng nhập. | Medium |
| **TC_AUTH_08** | Đăng nhập với email không tồn tại trong hệ thống | Mức 3 (Not Found) | Khách lạ | `POST /api/auth/login` | `{"username": "khongtontai@gmail.com", "password": "123456"}` | Status: 401 Unauthorized | {"message": "Tài khoản không tồn tại!"} | Người dùng gõ sai chính tả địa chỉ email hoặc tài khoản chưa từng được đăng ký. | High |
| **TC_AUTH_09** | Đăng nhập bằng tài khoản đang bị khóa (status = 0) | Mức 4 (Business Rule) | Nhân viên cũ | `POST /api/auth/login` | `{"username": "quy.ngoc@bookstore.com", "password": "password123"}` | Status: 401 Unauthorized | {"message": "Tài khoản này đã bị khóa. Vui lòng liên hệ Quản Lý!"} | Nhân viên đã nghỉ việc hoặc bị kỷ luật cố tình truy cập vào hệ thống. | Critical |
| **TC_AUTH_10** | Sử dụng lại Token đã Đăng xuất (Replay Token) | Mức 5 (Security) | Kẻ tấn công | `GET /api/profile` | `Header: Authorization: Bearer {{loggedOutToken}}` | Status: 401 Unauthorized | {"message": "Phiên đăng nhập không hợp lệ hoặc đã hết hạn."} | Kẻ tấn công bắt lại gói tin mạng của phiên đã kết thúc để cố tình giả mạo truy cập. | Critical |
| **TC_AUTH_11** | Tấn công Brute-force mật khẩu đăng nhập | Mức 5 (Security) | Kẻ tấn công | `POST /api/auth/login` | `Gửi lặp 100 request liên tiếp với các mật khẩu khác nhau` | Status: 401 Unauthorized | Server phản hồi nhất quán, không crash connection pool | Kẻ xấu dùng tool tự động dò quét mật khẩu người dùng. | High |

---

### Phân hệ 03: Khôi phục Mật khẩu & Xác thực OTP (Password Reset)
* **Controller:** `PasswordResetController`
* **Mô tả nghiệp vụ:** Cung cấp quy trình lấy lại mật khẩu an toàn: gửi mã OTP xác nhận về địa chỉ Email đã đăng ký và cập nhật mật khẩu mới.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_PWD_01** | Gửi yêu cầu OTP với email hợp lệ đã đăng ký | Mức 1 (Happy Path) | Người dùng | `POST /api/auth/password-reset/request` | `{"email": "vinhyt7755@gmail.com"}` | Status: 204 No Content | Gửi mã OTP vào hộp thư | Người dùng quên mật khẩu và bấm Quên mật khẩu trên giao diện. | High |
| **TC_PWD_02** | Xác nhận OTP chính xác và đổi mật khẩu thành công | Mức 1 (Happy Path) | Người dùng | `POST /api/auth/password-reset/confirm` | `{"email": "vinhyt7755@gmail.com", "otp": "{{validOtp}}", "newPassword": "NewSecret123"}` | Status: 204 No Content | Mật khẩu được cập nhật thành công | Người dùng nhận được mã OTP từ email và nhập vào form đổi mật khẩu. | High |
| **TC_PWD_03** | Yêu cầu OTP với email không tồn tại trong hệ thống | Mức 3 (Anti-Enumeration) | Khách lạ | `POST /api/auth/password-reset/request` | `{"email": "email_chua_dang_ky@gmail.com"}` | Status: 204 No Content (Không gửi email, trả phản hồi trung tính chống dò quét tài khoản theo OWASP) | Người dùng gõ nhầm email cá nhân khác với email đã khai báo trên hệ thống. | Medium |
| **TC_PWD_04** | Yêu cầu OTP với chuỗi email sai định dạng cú pháp | Mức 2 (Validation) | Người dùng | `POST /api/auth/password-reset/request` | `{"email": "sai_dinh_dang_email"}` | Status: 400 Bad Request | Email không đúng định dạng | Người dùng gõ thiếu ký tự @ hoặc .com. | Medium |
| **TC_PWD_05** | Xác nhận đổi mật khẩu với mã OTP sai | Mức 4 (Business Rule) | Người dùng | `POST /api/auth/password-reset/confirm` | `{"email": "vinhyt7755@gmail.com", "otp": "999999", "newPassword": "NewSecret123"}` | Status: 422 Unprocessable Entity | {"message": "Mã OTP không hợp lệ hoặc đã hết hạn"} | Người dùng gõ sai một trong 6 chữ số của mã OTP gửi về máy. | High |
| **TC_PWD_06** | Xác nhận đổi mật khẩu với mã OTP đã quá hạn (> 5 phút) | Mức 4 (State Expired) | Người dùng | `POST /api/auth/password-reset/confirm` | `{"email": "vinhyt7755@gmail.com", "otp": "{{expiredOtp}}", "newPassword": "NewSecret123"}` | Status: 422 Unprocessable Entity | Báo OTP đã hết hạn | Người dùng kiểm tra email muộn sau khi mã OTP đã hết hiệu lực bảo mật. | High |
| **TC_PWD_07** | Đặt mật khẩu mới quá ngắn (< 4 ký tự) | Mức 2 (Boundary) | Người dùng | `POST /api/auth/password-reset/confirm` | `{"email": "vinhyt7755@gmail.com", "otp": "123456", "newPassword": "12"}` | Status: 400 Bad Request | {"message": "Mật khẩu mới phải có ít nhất 4 ký tự."} | Người dùng đặt mật khẩu quá đơn giản không đạt yêu cầu bảo mật. | High |

---

### Phân hệ 04: Hồ sơ Cá nhân (User Profile)
* **Controller:** `ProfileController`
* **Mô tả nghiệp vụ:** Quản lý thông tin hồ sơ của chính nhân viên đang đăng nhập, cho phép đổi thông tin liên lạc và cập nhật mật khẩu cá nhân.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_PROF_01** | Xem thông tin hồ sơ cá nhân của mình | Mức 1 (Happy Path) | Quản lý | `GET /api/profile` | `Header: Authorization: Bearer {{managerToken}}` | Status: 200 OK | Trả về đúng thông tin cá nhân của nhân viên đăng nhập | Người dùng mở trang Thông tin cá nhân để kiểm tra thông tin. | High |
| **TC_PROF_02** | Cập nhật hồ sơ cá nhân hợp lệ | Mức 1 (Happy Path) | Quản lý | `PUT /api/profile` | `{"fullName": "Võ Trường Vinh", "phone": "0583458548", "email": "vinhyt7755@gmail.com"}` | Status: 200 OK | Cập nhật thông tin thành công | Người dùng đổi số điện thoại hoặc cập nhật họ tên mới. | High |
| **TC_PROF_03** | Đổi mật khẩu mới trùng với mật khẩu cũ | Mức 4 (Business Rule) | Quản lý | `PUT /api/profile` | `{"fullName": "Võ Trường Vinh", "phone": "0583458548", "email": "vinhyt7755@gmail.com", "oldPassword": "07072006", "newPassword": "07072006"}` | Status: 400 Bad Request / 422 | {"message": "Mật khẩu mới không được trùng với mật khẩu cũ!"} | Người dùng nhập lại mật khẩu hiện tại khi phần mềm yêu cầu đổi mật khẩu định kỳ. | High |
| **TC_PROF_04** | Cập nhật email trùng với email của nhân viên khác | Mức 3 (Integrity 422) | Quản lý | `PUT /api/profile` | `{"fullName": "Võ Trường Vinh", "phone": "0583458548", "email": "hotro.adminhethong@gmail.com"}` | Status: 400 Bad Request / 422 | {"message": "Email này đã được sử dụng bởi người khác!"} | Người dùng nhập nhầm email của đồng nghiệp vào hồ sơ của mình. | High |
| **TC_PROF_05** | Cập nhật email không đúng định dạng cú pháp | Mức 2 (Validation) | Quản lý | `PUT /api/profile` | `{"fullName": "Võ Trường Vinh", "phone": "0583458548", "email": "vinh_sai_cu_phap"}` | Status: 400 Bad Request | {"message": "Email không đúng định dạng."} | Người dùng gõ thiếu ký tự @ hoặc miền .com. | Medium |
| **TC_PROF_06** | Đổi mật khẩu nhưng nhập sai mật khẩu cũ | Mức 2 (Validation) | Quản lý | `PUT /api/profile` | `{"fullName": "Võ Trường Vinh", "phone": "0583458548", "email": "vinhyt7755@gmail.com", "oldPassword": "WrongOldPass", "newPassword": "NewPass123"}` | Status: 400 Bad Request / 422 | Báo mật khẩu cũ không đúng | Người dùng nhớ nhầm mật khẩu hiện tại của mình khi thực hiện đổi mật khẩu. | High |
| **TC_PROF_07** | Cập nhật họ tên chứa toàn khoảng trắng | Mức 2 (Validation) | Quản lý | `PUT /api/profile` | `{"fullName": "   ", "phone": "0583458548", "email": "vinhyt7755@gmail.com"}` | Status: 400 Bad Request | Họ tên không được để trống | Người dùng xóa sạch ô họ tên và bấm Lưu. | Medium |

---

### Phân hệ 05: Quản lý Tác giả (Authors)
* **Controller:** `AuthorController`
* **Mô tả nghiệp vụ:** Quản lý danh mục tác giả viết sách, phục vụ cho việc gắn liên kết thông tin tác giả với từng đầu sách.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_AUTHORS_01** | Lấy danh sách tất cả tác giả | Mức 1 (Happy Path) | Quản lý/Bán hàng | `GET /api/authors` | `Header: Authorization: Bearer {{token}}` | Status: 200 OK | Danh sách tác giả dạng JSON Array | Giao diện danh mục tải danh sách tác giả lên combobox. | High |
| **TC_AUTHORS_02** | Thêm mới tác giả hợp lệ | Mức 1 (Happy Path) | Quản lý | `POST /api/authors` | `{"authorName": "Nguyễn Nhật Ánh Mới", "nationality": "Việt Nam"}` | Status: 200 OK | "Thêm tác giả thành công!" | Quản lý bổ sung tác giả mới vào danh mục. | High |
| **TC_AUTHORS_03** | Cập nhật thông tin tác giả theo ID | Mức 1 (Happy Path) | Quản lý | `PUT /api/authors/1` | `{"authorName": "Nguyễn Nhật Ánh", "nationality": "Việt Nam"}` | Status: 200 OK | "Cập nhật tác giả thành công!" | Chỉnh sửa quốc tịch hoặc đính chính họ tên tác giả. | Medium |
| **TC_AUTHORS_04** | Thêm tác giả với tên rỗng hoặc toàn khoảng trắng | Mức 2 (Validation) | Quản lý | `POST /api/authors` | `{"authorName": "   ", "nationality": "Việt Nam"}` | Status: 422 Unprocessable Entity | "Tên tác giả không được để trống!" | Người dùng bấm Lưu khi chưa điền tên tác giả. | High |
| **TC_AUTHORS_05** | Thêm tác giả với tên chứa số hoặc ký tự đặc biệt lạ | Mức 2 (Validation) | Quản lý | `POST /api/authors` | `{"authorName": "Tác Giả 123 @#$", "nationality": "Việt Nam"}` | Status: 422 Unprocessable Entity | "Tên tác giả không hợp lệ!" | Người dùng gõ nhầm số hoặc dán nhầm chuỗi ký tự lạ vào ô tên tác giả. | Medium |
| **TC_AUTHORS_06** | Thêm tác giả trùng tên đã có trong hệ thống | Mức 3 (Integrity 422) | Quản lý | `POST /api/authors` | `{"authorName": "Nguyễn Nhật Ánh", "nationality": "Việt Nam"}` | Status: 422 Unprocessable Entity | "Tên tác giả đã tồn tại trong hệ thống!" | Người dùng tạo mới tác giả mà không biết tác giả này đã có sẵn trong danh mục. | High |
| **TC_AUTHORS_07** | Thêm tác giả để trống quốc tịch | Mức 2 (Validation) | Quản lý | `POST /api/authors` | `{"authorName": "Arthur Conan Doyle", "nationality": ""}` | Status: 422 Unprocessable Entity | "Quốc tịch tác giả không được để trống!" | Người dùng bỏ qua trường quốc tịch bắt buộc. | Medium |
| **TC_AUTHORS_08** | Cập nhật tác giả với ID không tồn tại | Mức 3 (Not Found) | Quản lý | `PUT /api/authors/999999` | `{"authorName": "Tác Giả Lạ", "nationality": "Mỹ"}` | Status: 400/404 | "Cập nhật tác giả thất bại!" | Client gửi request với ID tác giả đã bị xóa. | Low |

---

### Phân hệ 06: Quản lý Thể loại Sách (Categories)
* **Controller:** `CategoryController`
* **Mô tả nghiệp vụ:** Quản lý phân loại sách (Văn học, Kinh tế, Thiếu nhi, Kỹ thuật, Ngoại ngữ,...), đảm bảo tính duy nhất và toàn vẹn dữ liệu.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_CAT_01** | Lấy danh sách tất cả thể loại sách | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/categories` | `Header: Bearer Token` | Status: 200 OK | Danh sách thể loại | Hiển thị cây phân loại sách cho nhân viên tra cứu. | High |
| **TC_CAT_02** | Xem chi tiết một thể loại theo ID hợp lệ | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/categories/1` | `Header: Bearer Token` | Status: 200 OK | Thông tin chi tiết thể loại ID=1 | Xem chi tiết thông tin một thể loại cụ thể. | Medium |
| **TC_CAT_03** | Thêm mới thể loại sách hợp lệ | Mức 1 (Happy Path) | Quản lý | `POST /api/categories` | `{"categoryName": "Khoa Học Viễn Tưởng"}` | Status: 200 OK | "Thêm thể loại thành công!" | Quản lý mở thêm nhóm sách mới. | High |
| **TC_CAT_04** | Cập nhật tên thể loại sách hợp lệ | Mức 1 (Happy Path) | Quản lý | `PUT /api/categories/1` | `{"categoryName": "Văn học Nghệ thuật"}` | Status: 200 OK | "Cập nhật thành công!" | Quản lý đổi tên chuẩn hóa danh mục thể loại. | Medium |
| **TC_CAT_05** | Thêm thể loại với tên để trống | Mức 2 (Validation) | Quản lý | `POST /api/categories` | `{"categoryName": "   "}` | Status: 422 Unprocessable Entity | "Tên thể loại không được để trống!" | Người dùng nhấn Lưu khi ô tên thể loại còn rỗng. | High |
| **TC_CAT_06** | Thêm thể loại chứa chữ số sai định dạng Regex | Mức 2 (Validation) | Quản lý | `POST /api/categories` | `{"categoryName": "Thể loại 123"}` | Status: 422 Unprocessable Entity | "Tên thể loại không hợp lệ!" | Người dùng gõ nhầm chữ số vào tên danh mục sách. | Medium |
| **TC_CAT_07** | Thêm thể loại trùng lặp tên đã tồn tại | Mức 3 (Conflict 409) | Quản lý | `POST /api/categories` | `{"categoryName": "Văn học"}` | Status: 409 Conflict / 422 | "Tên thể loại này đã tồn tại trong hệ thống!" | Người dùng vô tình thêm lại thể loại đã có trong cơ sở dữ liệu. | High |
| **TC_CAT_08** | Xem chi tiết thể loại với ID không tồn tại | Mức 3 (Not Found) | Quản lý | `GET /api/categories/999999` | `Header: Bearer Token` | Status: 404 Not Found | {"message": "Không tìm thấy thể loại."} | Người dùng gõ sai ID trên thanh địa chỉ URL. | Low |

---

### Phân hệ 07: Quản lý Nhà cung cấp (Suppliers)
* **Controller:** `SupplierController`
* **Mô tả nghiệp vụ:** Quản lý danh sách các Nhà xuất bản / Nhà phát hành cung ứng sách, thông tin liên lạc và mã số thuế.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_SUP_01** | Lấy danh sách tất cả nhà cung cấp | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/suppliers` | `Header: Bearer Token` | Status: 200 OK | Danh sách nhà cung cấp | Tải danh sách đối tác cung ứng sách lên giao diện. | High |
| **TC_SUP_02** | Xem chi tiết nhà cung cấp theo ID hợp lệ | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/suppliers/1` | `Header: Bearer Token` | Status: 200 OK | Chi tiết NXB Kim Đồng | Xem địa chỉ, số điện thoại của nhà cung cấp. | Medium |
| **TC_SUP_03** | Thêm mới nhà cung cấp hợp lệ | Mức 1 (Happy Path) | Quản lý | `POST /api/suppliers` | `{"supplierName": "NXB Tri Thức Trẻ", "supplierPhone": "0283999999", "supplierAddress": "Hà Nội"}` | Status: 200 OK | "Thêm nhà cung cấp thành công!" | Quản lý ký hợp đồng hợp tác với NXB mới. | High |
| **TC_SUP_04** | Cập nhật thông tin nhà cung cấp hợp lệ | Mức 1 (Happy Path) | Quản lý | `PUT /api/suppliers/1` | `{"supplierName": "NXB Kim Đồng", "supplierPhone": "02439434730", "supplierAddress": "Hà Nội"}` | Status: 200 OK | "Cập nhật thành công!" | Cập nhật số điện thoại bàn mới của đối tác. | Medium |
| **TC_SUP_05** | Thêm nhà cung cấp để trống tên | Mức 2 (Validation) | Quản lý | `POST /api/suppliers` | `{"supplierName": "", "supplierPhone": "0283999999"}` | Status: 422 Unprocessable Entity | "Tên nhà cung cấp không được để trống!" | Người dùng quên điền tên đối tác. | High |
| **TC_SUP_06** | Thêm nhà cung cấp số điện thoại sai định dạng | Mức 2 (Validation) | Quản lý | `POST /api/suppliers` | `{"supplierName": "NXB Lạ", "supplierPhone": "12345"}` | Status: 422 Unprocessable Entity | "Số điện thoại không hợp lệ! (Số di động 10 chữ số hoặc số bàn cố định 11 chữ số bắt đầu bằng số 0)" | Người dùng nhập số điện thoại thiếu số hoặc không bắt đầu bằng số 0. | High |
| **TC_SUP_07** | Thêm nhà cung cấp trùng tên đã có | Mức 3 (Integrity 422) | Quản lý | `POST /api/suppliers` | `{"supplierName": "NXB Kim Đồng", "supplierPhone": "0988888888"}` | Status: 422 Unprocessable Entity | "Tên nhà cung cấp này đã tồn tại trong hệ thống!" | Người dùng thêm đối tác trùng tên đã hợp tác từ trước. | High |
| **TC_SUP_08** | Thêm nhà cung cấp trùng số điện thoại với đối tác khác | Mức 3 (Integrity 422) | Quản lý | `POST /api/suppliers` | `{"supplierName": "NXB Mới", "supplierPhone": "02439434730"}` | Status: 422 Unprocessable Entity | "Số điện thoại này đã thuộc về nhà cung cấp khác!" | Người dùng nhập trùng số hotline của đối tác khác. | High |
| **TC_SUP_09** | Tra cứu nhà cung cấp với ID không tồn tại | Mức 3 (Not Found) | Quản lý | `GET /api/suppliers/999999` | `Header: Bearer Token` | Status: 404 Not Found | {"message": "Không tìm thấy nhà cung cấp."} | Client tra cứu đối tác đã bị ngừng hợp tác / xóa bỏ. | Low |

---

### Phân hệ 08: Quản lý Sách & Danh mục Hàng hóa (Books)
* **Controller:** `BookController`
* **Mô tả nghiệp vụ:** Quản lý thông tin chi tiết từng đầu sách: Tác giả, Thể loại, Nhà cung cấp, Năm xuất bản, Giá bìa, Giá bán và trạng thái kinh doanh.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_BOOK_01** | Lấy danh sách tất cả các đầu sách trong hệ thống | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/books` | `Header: Bearer Token` | Status: 200 OK | Danh sách sách toàn diện | Hiển thị kho sách trên giao diện quản trị. | High |
| **TC_BOOK_02** | Lấy danh sách các đầu sách đang kinh doanh (active = 1) | Mức 1 (Happy Path) | Thu ngân/Bán hàng | `GET /api/books/active` | `Header: Bearer Token` | Status: 200 OK | Chỉ các sách có status = 1 | Màn hình POS thu ngân tải danh mục sách được phép bán. | Critical |
| **TC_BOOK_03** | Xem chi tiết một đầu sách theo ID hợp lệ | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/books/1` | `Header: Bearer Token` | Status: 200 OK | Chi tiết sách Cho tôi xin một vé đi về tuổi thơ | Xem giá bán, tồn kho, tên tác giả cuốn sách. | High |
| **TC_BOOK_04** | Thêm mới đầu sách đầy đủ thông tin hợp lệ | Mức 1 (Happy Path) | Quản lý | `POST /api/books` | `{"bookName": "Kiểm Thử Phần Mềm Nâng Cao", "publicationYear": 2026, "coverPrice": 150000, "sellingPrice": 150000, "authorIdsList": [1], "categoryId": 1, "supplierId": 1}` | Status: 200 OK | "Thêm sách thành công!" | Quản lý phát hành đầu sách mới ra quầy. | High |
| **TC_BOOK_05** | Cập nhật thông tin đầu sách theo ID | Mức 1 (Happy Path) | Quản lý | `PUT /api/books/1` | `{"bookName": "Cho tôi xin một vé đi về tuổi thơ (Tái Bản)", "publicationYear": 2026, "coverPrice": 95000, "sellingPrice": 95000, "authorIdsList": [1], "categoryId": 1, "supplierId": 1}` | Status: 200 OK | "Cập nhật thành công!" | Đính chính thông tin tái bản của cuốn sách. | Medium |
| **TC_BOOK_06** | Thêm sách với tên để trống hoặc toàn khoảng trắng | Mức 2 (Validation) | Quản lý | `POST /api/books` | `{"bookName": "   ", "categoryId": 1, "authorIdsList": [1]}` | Status: 422 Unprocessable Entity | "Tên sách không được để trống!" | Người dùng bấm Lưu sách khi chưa nhập tiêu đề sách. | High |
| **TC_BOOK_07** | Thêm sách không chọn tác giả (authorIdsList rỗng) | Mức 2 (Validation) | Quản lý | `POST /api/books` | `{"bookName": "Sách Không Tác Giả", "authorIdsList": []}` | Status: 422 Unprocessable Entity | "Vui lòng chọn ít nhất một tác giả cho sách!" | Người dùng quên tích chọn tác giả trong bảng chọn tác giả. | High |
| **TC_BOOK_08** | Thêm sách không chọn thể loại (categoryId = 0) | Mức 2 (Validation) | Quản lý | `POST /api/books` | `{"bookName": "Sách Không Thể Loại", "authorIdsList": [1], "categoryId": 0}` | Status: 422 Unprocessable Entity | "Vui lòng chọn thể loại cho sách!" | Người dùng chưa chọn nhóm thể loại cho sách. | High |
| **TC_BOOK_09** | Thêm sách không chọn nhà cung cấp (supplierId = 0) | Mức 2 (Validation) | Quản lý | `POST /api/books` | `{"bookName": "Sách Không NXB", "authorIdsList": [1], "categoryId": 1, "supplierId": 0}` | Status: 422 Unprocessable Entity | "Vui lòng chọn nhà cung cấp cho sách!" | Người dùng bỏ qua trường nhà xuất bản. | High |
| **TC_BOOK_10** | Thêm sách với giá bán là số âm | Mức 2 (Boundary) | Quản lý | `POST /api/books` | `{"bookName": "Sách Giá Âm", "coverPrice": -50000, "sellingPrice": -50000, "authorIdsList": [1], "categoryId": 1, "supplierId": 1}` | Status: 422 Unprocessable Entity | "Giá bán của sách không được là số âm!" | Người dùng gõ nhầm dấu âm hoặc gạch nối vào ô giá tiền. | High |
| **TC_BOOK_11** | Thêm sách trùng lặp hoàn toàn với sách đã có | Mức 3 (Integrity 422) | Quản lý | `POST /api/books` | `Trùng bookName, authorIdsList, categoryId, supplierId của sách đã có` | Status: 422 Unprocessable Entity | "Sách đã tồn tại! (Trùng tên, tác giả, nhà cung cấp và người dịch)" | Người dùng tạo mới sách mà không kiểm tra sách đã có trong kho. | High |
| **TC_BOOK_12** | Tra cứu thông tin sách với ID không tồn tại | Mức 3 (Not Found) | Mọi vai trò | `GET /api/books/999999` | `Header: Bearer Token` | Status: 404 Not Found | {"message": "Không tìm thấy sách."} | Quét mã vạch hoặc gõ ID cuốn sách không có trong hệ thống. | Medium |
| **TC_BOOK_13** | Quản lý chuyển trạng thái ngừng kinh doanh sách (status = 0) | Mức 4 (State Transition) | Quản lý | `PUT /api/books/1` | `Cập nhật status = 0` | Status: 200 OK | Sách chuyển sang ngừng bán | Sách hết hạn bản quyền hoặc bị cấm lưu hành, quản lý tạm dừng bán. | High |

---

### Phân hệ 09: Quản lý Nhập kho & Phê duyệt Maker-Checker (Imports)
* **Controller:** `ImportController`
* **Mô tả nghiệp vụ:** Quản lý toàn bộ quy trình nhập sách từ Nhà cung cấp, áp dụng nguyên tắc kiểm soát kép Maker-Checker (Nhân viên lập phiếu, Quản lý phê duyệt).

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_IMP_01** | Lấy danh sách tất cả các phiếu nhập kho | Mức 1 (Happy Path) | Quản lý/Thủ kho | `GET /api/imports` | `Header: Bearer Token` | Status: 200 OK | Danh sách phiếu nhập kèm trạng thái 0/1/2 | Hiển thị danh sách phiếu nhập kho để theo dõi tiến độ. | High |
| **TC_IMP_02** | Xem chi tiết các dòng sách của một phiếu nhập | Mức 1 (Happy Path) | Quản lý/Thủ kho | `GET /api/imports/1/details` | `Header: Bearer Token` | Status: 200 OK | Danh sách chi tiết gồm bookId, số lượng, giá nhập | Kiểm tra danh mục sách và số lượng khi xe giao hàng đến. | High |
| **TC_IMP_03** | Thêm nhanh đầu sách mới trực tiếp khi đang lập phiếu nhập | Mức 1 (Happy Path) | Thủ kho | `POST /api/imports/quick-add-book` | `{"bookName": "Sách Mới Cập Bến", "categoryId": 1, "authorIdsList": [1], "supplierId": 1, "coverPrice": 100000}` | Status: 200 OK | Thêm sách thành công | Thủ kho nhận cuốn sách mới chưa từng có mã trên hệ thống. | High |
| **TC_IMP_04** | Lập phiếu nhập kho hợp lệ (Maker tạo phiếu) | Mức 1 (Happy Path) | Thủ kho | `POST /api/imports` | `{"supplierId": 1, "items": [{"bookId": 1, "quantity": 20, "importPrice": 50000}]}` | Status: 204 No Content | Phiếu tạo mới ở trạng thái 0 (Chờ duyệt) | Thủ kho kiểm đếm hàng xong và bấm Lưu phiếu nhập chờ quản lý duyệt. | Critical |
| **TC_IMP_05** | Quản lý phê duyệt phiếu nhập (Checker duyệt phiếu) | Mức 1 (Happy Path) | Quản lý | `POST /api/imports/{{id}}/approve` | `Header: managerToken | Khác người tạo phiếu` | Status: 204 No Content | Phiếu chuyển status=1, cộng tồn kho và sinh lô hàng | Quản lý ký duyệt phiếu nhập, tồn kho tự động được cập nhật. | Critical |
| **TC_IMP_06** | Hủy phiếu nhập kho đang ở trạng thái chờ duyệt | Mức 1 (Happy Path) | Quản lý | `POST /api/imports/{{id}}/cancel` | `Header: managerToken` | Status: 204 No Content | Phiếu chuyển status=2 (Đã hủy), không cộng kho | Hàng giao bị lỗi hoặc sai quy cách, quản lý từ chối nhập kho. | High |
| **TC_IMP_07** | Lập phiếu nhập rỗng không có cuốn sách nào trong giỏ | Mức 2 (Validation) | Thủ kho | `POST /api/imports` | `{"supplierId": 1, "items": []}` | Status: 400 Bad Request | {"message": "Phiếu nhập cần có nhà cung cấp và ít nhất một sách."} | Thủ kho bấm Lưu phiếu nhập khi chưa kịp quét hay chọn cuốn sách nào. | High |
| **TC_IMP_08** | Lập phiếu nhập với số lượng sách nhập bằng 0 hoặc số âm | Mức 2 (Boundary) | Thủ kho | `POST /api/imports` | `{"supplierId": 1, "items": [{"bookId": 1, "quantity": 0, "importPrice": 50000}]}` | Status: 400 Bad Request | Báo số lượng sách nhập không hợp lệ | Người dùng gõ nhầm số lượng 0 hoặc âm. | High |
| **TC_IMP_09** | Lập phiếu nhập với giá nhập sách là số âm | Mức 2 (Boundary) | Thủ kho | `POST /api/imports` | `{"supplierId": 1, "items": [{"bookId": 1, "quantity": 10, "importPrice": -50000}]}` | Status: 400 Bad Request | Giá nhập không hợp lệ | Người dùng gõ nhầm dấu âm vào ô giá nhập hàng. | High |
| **TC_IMP_10** | Chọn sách không thuộc danh mục của nhà cung cấp đã chọn | Mức 4 (Business Rule) | Thủ kho | `POST /api/imports` | `{"supplierId": 1, "items": [{"bookId": 99, "quantity": 10, "importPrice": 50000}]}` | Status: 400 Bad Request | "Sách không tồn tại hoặc không thuộc nhà cung cấp đã chọn." | Thủ kho chọn nhầm đối tác cung cấp trên tiêu đề phiếu nhập. | High |
| **TC_IMP_11** | Maker cố tình tự duyệt phiếu nhập của chính mình (Vi phạm Maker-Checker) | Mức 4 (Maker-Checker) | Thủ kho | `POST /api/imports/{{ownId}}/approve` | `Header: importToken của người vừa lập phiếu này` | Status: 403 Forbidden | {"message": "Bạn không có quyền duyệt hoặc hủy phiếu này."} | Nhân viên kho cố tình tự duyệt để gian lận tồn kho hàng hóa. | Critical |
| **TC_IMP_12** | Duyệt lại phiếu nhập đã ở trạng thái ĐÃ DUYỆT (Double Click) | Mức 4 (State Conflict) | Quản lý | `POST /api/imports/{{approvedId}}/approve` | `Header: managerToken` | Status: 409 Conflict | {"message": "Phiếu nhập không còn ở trạng thái chờ duyệt."} | Quản lý vô tình click nút Duyệt 2 lần liên tiếp trên giao diện. | High |
| **TC_IMP_13** | Duyệt phiếu nhập đã bị HỦY | Mức 4 (State Conflict) | Quản lý | `POST /api/imports/{{cancelledId}}/approve` | `Header: managerToken` | Status: 409 Conflict | {"message": "Phiếu nhập không còn ở trạng thái chờ duyệt."} | Người dùng cố tình duyệt lại một phiếu nhập đã bị hủy trước đó. | High |
| **TC_IMP_14** | Hủy một phiếu nhập đã được duyệt thành công trước đó | Mức 4 (State Conflict) | Quản lý | `POST /api/imports/{{approvedId}}/cancel` | `Header: managerToken` | Status: 409 Conflict | {"message": "Phiếu nhập không còn ở trạng thái chờ duyệt."} | Cố tình hủy phiếu nhập sau khi hàng đã được nhập kho và bán ra. | Critical |
| **TC_IMP_15** | Nhân viên Bán hàng gọi API Duyệt phiếu nhập kho | Mức 5 (RBAC 403) | Thu ngân | `POST /api/imports/1/approve` | `Header: salesToken (không có quyền MANAGE_IMPORT_TICKET)` | Status: 403 Forbidden | Từ chối truy cập do sai thẩm quyền | Nhân viên bán hàng tò mò bấm vào chức năng duyệt của quản lý. | Critical |

---

### Phân hệ 10: Quản lý Tồn kho & Lô hàng (Inventory)
* **Controller:** `InventoryController`
* **Mô tả nghiệp vụ:** Theo dõi số lượng tồn kho tức thời của từng đầu sách, chi tiết từng lô nhập hàng và nhật ký biến động kho.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_INV_01** | Xem tổng hợp tồn kho hiện tại của tất cả sách | Mức 1 (Happy Path) | Quản lý/Thủ kho | `GET /api/inventory/books` | `Header: Bearer Token` | Status: 200 OK | Danh sách tồn kho theo từng bookId | Kiểm tra lượng sách tồn trên kệ và trong kho. | High |
| **TC_INV_02** | Xem danh sách các lô nhập của một cuốn sách theo ID | Mức 1 (Happy Path) | Quản lý/Thủ kho | `GET /api/inventory/books/1/lots` | `Header: Bearer Token` | Status: 200 OK | Danh sách lô nhập gồm mã lô, ngày nhập, giá nhập, số lượng | Thủ kho kiểm tra hạn sử dụng và xuất xứ của lô sách. | Medium |
| **TC_INV_03** | Xem nhật ký biến động kho hàng hóa | Mức 1 (Happy Path) | Quản lý/Thủ kho | `GET /api/inventory/logs` | `Header: Bearer Token` | Status: 200 OK | Nhật ký xuất - nhập - tồn | Đối soát biến động hàng hóa khi phát hiện chênh lệch kiểm kê. | High |
| **TC_INV_04** | Tra cứu lô nhập với ID sách âm hoặc bằng 0 | Mức 2 (Boundary) | Quản lý/Thủ kho | `GET /api/inventory/books/-1/lots` | `Header: Bearer Token` | Status: 400 Bad Request | {"message": "Mã sách không hợp lệ."} | Client gửi URL với mã sách âm. | Low |
| **TC_INV_05** | Tra cứu lô nhập của cuốn sách không tồn tại | Mức 3 (Not Found) | Quản lý/Thủ kho | `GET /api/inventory/books/999999/lots` | `Header: Bearer Token` | Status: 200 OK mảng rỗng [] hoặc 404 | Người dùng tìm lô của cuốn sách chưa từng có trong hệ thống. | Low |
| **TC_INV_06** | Nhân viên không có quyền xem kho truy cập API tồn kho | Mức 5 (RBAC 403) | Khách/User ngoài | `GET /api/inventory/books` | `Không có token hoặc token không có quyền MANAGE_INVENTORY` | Status: 401 Unauthorized / 403 Forbidden | Người không có thẩm quyền cố tình xem dữ liệu tồn kho mật của cửa hàng. | High |

---

### Phân hệ 11: Quản lý Bảng giá & Lợi nhuận (Prices)
* **Controller:** `PriceController`
* **Mô tả nghiệp vụ:** Điều chỉnh giá bán niêm yết của từng đầu sách, cập nhật giá hàng loạt dựa trên tỷ suất lợi nhuận (Profit Margin) so với giá vốn.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_PRICE_01** | Lấy danh sách bảng giá bán hiện tại của các sách | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/prices` | `Header: Bearer Token` | Status: 200 OK | Bảng giá niêm yết | Nhân viên xem bảng giá bán lẻ cập nhật. | High |
| **TC_PRICE_02** | Cập nhật giá bán lẻ mới cho một cuốn sách | Mức 1 (Happy Path) | Quản lý | `PUT /api/prices/1` | `{"newPrice": 125000}` | Status: 204 No Content | Cập nhật giá mới thành công | Quản lý điều chỉnh giá bán theo thông báo tăng giá của NXB. | High |
| **TC_PRICE_03** | Cập nhật giá bán hàng loạt theo tỷ suất lợi nhuận | Mức 1 (Happy Path) | Quản lý | `PUT /api/prices/bulk` | `{"bookIds": [1, 2], "profitMargin": 25.0}` | Status: 204 No Content | Tính lại giá bán lẻ = Giá vốn * (1 + 25%) | Quản lý áp dụng chính sách lợi nhuận 25% cho toàn bộ nhóm sách. | High |
| **TC_PRICE_04** | Cập nhật giá bán lẻ bằng 0 hoặc số âm | Mức 2 (Boundary) | Quản lý | `PUT /api/prices/1` | `{"newPrice": -1000}` | Status: 400 Bad Request | {"message": "Giá bán mới vượt quá giới hạn hợp lệ."} | Người dùng gõ nhầm số âm hoặc để giá bằng 0đ (bán miễn phí ngoài ý muốn). | High |
| **TC_PRICE_05** | Cập nhật giá hàng loạt nhưng không chọn cuốn sách nào | Mức 2 (Validation) | Quản lý | `PUT /api/prices/bulk` | `{"bookIds": [], "profitMargin": 20.0}` | Status: 400 Bad Request | {"message": "Cần chọn ít nhất một sách để cập nhật giá."} | Người dùng bấm nút Áp dụng giá khi chưa tích chọn checkbox cuốn sách nào. | High |
| **TC_PRICE_06** | Cập nhật giá hàng loạt với tỷ suất lợi nhuận âm | Mức 2 (Boundary) | Quản lý | `PUT /api/prices/bulk` | `{"bookIds": [1], "profitMargin": -15.0}` | Status: 400 Bad Request | {"message": "Tỷ suất lợi nhuận phải là số không âm hợp lệ."} | Người dùng nhập tỷ suất âm (bán dưới giá vốn dẫn đến lỗ). | High |
| **TC_PRICE_07** | Cập nhật giá cho cuốn sách không có giá bán hoạt động | Mức 3 (Not Found) | Quản lý | `PUT /api/prices/999999` | `{"newPrice": 100000}` | Status: 400/404 | {"message": "Không tìm thấy giá hiện tại của sách."} | Sách chưa từng được nhập giá ban đầu. | Medium |
| **TC_PRICE_08** | Nhân viên Bán hàng gọi API cập nhật giá bán | Mức 5 (RBAC 403) | Thu ngân | `PUT /api/prices/1` | `{"newPrice": 1000}` | Status: 403 Forbidden | Không có quyền MANAGE_PRICE | Thu ngân cố tình tự hạ giá sách để bán rẻ cho người quen. | Critical |

---

### Phân hệ 12: Quản lý Chương trình Khuyến mãi (Promotions)
* **Controller:** `PromotionController`
* **Mô tả nghiệp vụ:** Thiết lập các đợt giảm giá theo thời gian (Từ ngày - Đến ngày) và danh sách các cuốn sách được áp dụng khuyến mãi.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_PROM_01** | Lấy danh sách tất cả các chương trình khuyến mãi | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/promotions` | `Header: Bearer Token` | Status: 200 OK | Danh sách khuyến mãi | Nhân viên kiểm tra các chương trình giảm giá hiện có. | High |
| **TC_PROM_02** | Tìm kiếm chương trình khuyến mãi theo từ khóa Tên | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/promotions/search?keyword=Hè&type=Tên` | `Header: Bearer Token` | Status: 200 OK | Kết quả lọc chứa từ khóa 'Hè' | Quản lý tìm đợt khuyến mãi Chào Hè. | Medium |
| **TC_PROM_03** | Tạo mới chương trình khuyến mãi hợp lệ | Mức 1 (Happy Path) | Quản lý | `POST /api/promotions` | `{"promotionName": "Khuyến Mãi Quốc Tế Thiếu Nhi", "discountPercent": 20.0, "startDate": "2026-06-01", "endDate": "2026-06-05", "bookIds": [1, 2]}` | Status: 200 OK | Lưu khuyến mãi thành công | Quản lý lên kế hoạch giảm giá cho dịp lễ 1/6. | High |
| **TC_PROM_04** | Cập nhật chương trình khuyến mãi theo ID | Mức 1 (Happy Path) | Quản lý | `PUT /api/promotions/1` | `{"promotionName": "Khuyến Mãi Tri Ân Cập Nhật", "discountPercent": 15.0, "startDate": "2026-05-01", "endDate": "2026-05-10", "bookIds": [1]}` | Status: 200 OK | Cập nhật thành công | Quản lý gia hạn thêm thời gian kết thúc khuyến mãi. | Medium |
| **TC_PROM_05** | Tạo khuyến mãi với Ngày bắt đầu LỚN HƠN Ngày kết thúc | Mức 4 (Business Rule) | Quản lý | `POST /api/promotions` | `{"promotionName": "Lỗi ngày", "discountPercent": 15, "startDate": "2026-12-31", "endDate": "2026-01-01", "bookIds": [1]}` | Status: 400 Bad Request | {"message": "Thông tin chương trình khuyến mãi không hợp lệ."} | Người dùng chọn nhầm lịch ngày bắt đầu và kết thúc trên datepicker. | High |
| **TC_PROM_06** | Tạo khuyến mãi có năm ngoài phạm vi 2026 - 2099 | Mức 2 (Boundary) | Quản lý | `POST /api/promotions` | `{"promotionName": "Lỗi năm", "discountPercent": 15, "startDate": "2020-01-01", "endDate": "2020-02-01", "bookIds": [1]}` | Status: 400 Bad Request | Báo khoảng ngày nằm ngoài quy định | Người dùng gõ nhầm số năm ở quá khứ. | Medium |
| **TC_PROM_07** | Áp dụng khuyến mãi cho cuốn sách không tồn tại | Mức 3 (Integrity 400) | Quản lý | `POST /api/promotions` | `{"promotionName": "Sách lạ", "discountPercent": 15, "startDate": "2026-06-01", "endDate": "2026-06-30", "bookIds": [999999]}` | Status: 400 Bad Request | {"message": "Danh sách có sách không tồn tại."} | Sách đã bị xóa khỏi hệ thống nhưng vẫn được đưa vào đợt khuyến mãi. | Medium |
| **TC_PROM_08** | Cập nhật khuyến mãi với ID không tồn tại | Mức 3 (Not Found) | Quản lý | `PUT /api/promotions/999999` | `{"promotionName": "KM", "discountPercent": 10, "startDate": "2026-06-01", "endDate": "2026-06-10", "bookIds": [1]}` | Status: 404 Not Found | {"message": "Không tìm thấy chương trình khuyến mãi."} | Client cập nhật một đợt khuyến mãi đã bị xóa. | Low |
| **TC_PROM_09** | Tạo khuyến mãi với tỷ lệ giảm giá âm hoặc > 100% | Mức 2 (Boundary) | Quản lý | `POST /api/promotions` | `{"promotionName": "Giảm 150%", "discountPercent": 150.0, "startDate": "2026-06-01", "endDate": "2026-06-10", "bookIds": [1]}` | Status: 400 Bad Request | Tỷ lệ giảm giá không hợp lệ | Người dùng nhập nhầm số phần trăm giảm giá vượt trần 100%. | High |
| **TC_PROM_10** | Nhân viên Bán hàng gọi API tạo khuyến mãi | Mức 5 (RBAC 403) | Thu ngân | `POST /api/promotions` | `Body khuyến mãi` | Status: 403 Forbidden | Không có quyền MANAGE_PROMOTION | Thu ngân tự ý tạo chương trình giảm giá cho người thân mà không có sự đồng ý của quản lý. | Critical |

---

### Phân hệ 13: Quản lý Khách hàng & Điểm thưởng (Customers)
* **Controller:** `CustomerController`
* **Mô tả nghiệp vụ:** Quản lý hồ sơ khách hàng, tra cứu nhanh theo số điện thoại khi tính tiền, cộng điểm thưởng và thống kê lịch sử mua sắm.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_CUST_01** | Tra cứu khách hàng theo số điện thoại hợp lệ | Mức 1 (Happy Path) | Thu ngân | `GET /api/customers/phone/0909901421` | `Header: Bearer Token` | Status: 200 OK | Trả về thông tin khách hàng, số điểm tích lũy và mã hạng | Thu ngân hỏi số điện thoại khách hàng khi bắt đầu quét sách tại quầy. | Critical |
| **TC_CUST_02** | Tạo mới khách hàng hợp lệ | Mức 1 (Happy Path) | Thu ngân | `POST /api/customers` | `{"customerName": "Trần Văn An", "customerPhone": "0912345678"}` | Status: 200 OK | "Thêm khách hàng thành công!" | Khách hàng mới đồng ý đăng ký thẻ thành viên để tích điểm. | High |
| **TC_CUST_03** | Cập nhật thông tin khách hàng theo ID | Mức 1 (Happy Path) | Thu ngân/Quản lý | `PUT /api/customers/1` | `{"customerName": "Nguyễn Thị Lan", "customerPhone": "0909901421"}` | Status: 200 OK | "Cập nhật thành công!" | Khách hàng đính chính lại họ tên trên thẻ tích điểm. | Medium |
| **TC_CUST_04** | Xem lịch sử thống kê chi tiêu của khách hàng | Mức 1 (Happy Path) | Quản lý | `GET /api/customers/1/statistics` | `Header: Bearer Token` | Status: 200 OK | Tổng tiền đã mua, số đơn hàng | Quản lý xem mức độ thân thiết của khách hàng. | Low |
| **TC_CUST_05** | Tra cứu số điện thoại khách hàng chưa đăng ký | Mức 3 (Not Found) | Thu ngân | `GET /api/customers/phone/0999999999` | `Header: Bearer Token` | Status: 404 Not Found | {"message": "Không tìm thấy khách hàng."} | Khách hàng lần đầu đến mua sách và chưa có thẻ thành viên. | High |
| **TC_CUST_06** | Tạo khách hàng để trống Tên | Mức 2 (Validation) | Thu ngân | `POST /api/customers` | `{"customerName": "", "customerPhone": "0912345678"}` | Status: 422 Unprocessable Entity | "Tên khách hàng không được để trống!" | Thu ngân bấm Lưu khi chưa kịp hỏi tên khách hàng. | High |
| **TC_CUST_07** | Tạo khách hàng để trống Số điện thoại | Mức 2 (Validation) | Thu ngân | `POST /api/customers` | `{"customerName": "Khách Hàng A", "customerPhone": ""}` | Status: 422 Unprocessable Entity | "Số điện thoại không được để trống!" | Thu ngân bỏ qua trường số điện thoại định danh. | High |
| **TC_CUST_08** | Tạo khách hàng với Số điện thoại sai định dạng (không phải 10 số, không bắt đầu bằng 0) | Mức 2 (Validation) | Thu ngân | `POST /api/customers` | `{"customerName": "Khách Test", "customerPhone": "1234567890"}` | Status: 422 Unprocessable Entity | "Số điện thoại không hợp lệ (Phải có 10 số và bắt đầu bằng 0)!" | Khách đọc thiếu số hoặc thu ngân gõ nhầm đầu số di động. | High |
| **TC_CUST_09** | Tạo khách hàng trùng số điện thoại đã tồn tại | Mức 3 (Conflict 409) | Thu ngân | `POST /api/customers` | `{"customerName": "Khách Trùng", "customerPhone": "0909901421"}` | Status: 422 Unprocessable Entity | "Số điện thoại này đã tồn tại!" | Khách đã có thẻ nhưng tưởng chưa có nên yêu cầu làm lại thẻ mới. | High |
| **TC_CUST_10** | Tạo khách hàng với tên chứa ký tự số hoặc biểu tượng lạ | Mức 2 (Validation) | Thu ngân | `POST /api/customers` | `{"customerName": "Khách 123 @#$", "customerPhone": "0987654321"}` | Status: 422 Unprocessable Entity | "Tên khách hàng không hợp lệ!" | Thu ngân gõ nhầm bàn phím số vào ô họ tên. | Medium |

---

### Phân hệ 14: Quản lý Hạng thành viên - Full CRUD (Membership Ranks)
* **Controller:** `MembershipRankController`
* **Mô tả nghiệp vụ:** Quản lý các cấp độ thành viên (Đồng, Bạc, Vàng, Kim Cương), ngưỡng điểm tối thiểu và tỷ lệ chiết khấu giảm giá đơn hàng.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_RANK_01** | Lấy danh sách tất cả các hạng thành viên | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/membership-ranks` | `Header: Bearer Token` | Status: 200 OK | Danh sách hạng kèm điểm tối thiểu và % giảm giá | Hệ thống tải danh sách hạng thành viên lên bảng cấu hình. | High |
| **TC_RANK_02** | Tra cứu hạng thành viên theo điểm tích lũy | Mức 1 (Happy Path) | Mọi vai trò | `GET /api/membership-ranks/by-points/3500` | `Header: Bearer Token` | Status: 200 OK | Trả về hạng Vàng (ngưỡng >= 3000) | Tự động xác định hạng thành viên khi khách tích lũy đủ điểm. | High |
| **TC_RANK_03** | Thêm mới một hạng thành viên hợp lệ | Mức 1 (Happy Path) | Quản lý | `POST /api/membership-ranks` | `{"rankName": "Bạch Kim", "minPoints": 4000, "discountPercent": 8.0}` | Status: 200 OK | Tạo hạng thành công | Quản lý bổ sung thêm phân khúc khách hàng mới. | Medium |
| **TC_RANK_04** | Cập nhật thông tin hạng thành viên theo ID | Mức 1 (Happy Path) | Quản lý | `PUT /api/membership-ranks/2` | `{"rankName": "Bạc Cập Nhật", "minPoints": 1200, "discountPercent": 4.0}` | Status: 200 OK | Cập nhật thành công | Quản lý điều chỉnh mức ưu đãi cho hạng Bạc. | Medium |
| **TC_RANK_05** | Xóa hạng thành viên mới tạo chưa có khách liên kết | Mức 1 (Happy Path) | Quản lý | `DELETE /api/membership-ranks/{{newRankId}}` | `Header: Bearer Token` | Status: 200 OK | Xóa thành công | Quản lý dọn dẹp hạng thử nghiệm. | Medium |
| **TC_RANK_06** | Thêm hạng thành viên trùng tên đã có | Mức 3 (Conflict 409) | Quản lý | `POST /api/membership-ranks` | `{"rankName": "Đồng", "minPoints": 0, "discountPercent": 0}` | Status: 409 Conflict | {"message": "Tên hạng đã tồn tại hoặc không thể tạo hạng."} | Quản lý tạo mới hạng trùng lặp với hạng đã có. | High |
| **TC_RANK_07** | Xóa hạng thành viên MẶC ĐỊNH của hệ thống (ID = 1) | Mức 4 (System Constraint) | Quản lý | `DELETE /api/membership-ranks/1` | `Header: Bearer Token` | Status: 409 Conflict | {"message": "Không thể xóa hạng Thành viên mặc định của hệ thống!"} | Quản lý vô tình bấm nút xóa trên dòng hạng Đồng mặc định. | Critical |
| **TC_RANK_08** | Xóa hạng thành viên đang có khách hàng liên kết | Mức 3 (Integrity 409) | Quản lý | `DELETE /api/membership-ranks/2` | `Header: Bearer Token` | Status: 409 Conflict | "Hạng này đang được áp dụng cho khách hàng. Vui lòng chuyển hạng cho khách trước khi xóa!" | Quản lý xóa một hạng đang được hàng trăm khách hàng sở hữu. | Critical |
| **TC_RANK_09** | Tra cứu hạng thành viên với điểm số âm | Mức 2 (Boundary) | Mọi vai trò | `GET /api/membership-ranks/by-points/-500` | `Header: Bearer Token` | Status: 200 OK hạng mặc định hoặc 400 Bad Request | Hệ thống truyền điểm lỗi do tính toán số âm. | Low |

---

### Phân hệ 15: Bán hàng tại quầy & Hóa đơn POS (Bills)
* **Controller:** `BillController`
* **Mô tả nghiệp vụ:** Tạo đơn hàng POS, tự động tính thuế VAT, chiết khấu khuyến mãi theo sách, chiết khấu hạng thành viên, trừ tồn kho và tích điểm.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_BILL_01** | Thanh toán đơn hàng POS cho Khách Lẻ (customerId = 0) | Mức 1 (Happy Path) | Thu ngân | `POST /api/bills` | `{"customerId": 0, "paymentMethodId": 1, "details": [{"bookId": 1, "quantity": 1}]}` | Status: 200 OK | Không áp dụng giảm giá thành viên, không tích điểm | Khách mua lẻ không có thẻ thành viên thanh toán tiền mặt. | Critical |
| **TC_BILL_02** | Thanh toán đơn hàng POS cho Khách Thành Viên có chiết khấu | Mức 1 (Happy Path) | Thu ngân | `POST /api/bills` | `{"customerId": 1, "paymentMethodId": 1, "details": [{"bookId": 1, "quantity": 2}]}` | Status: 200 OK | Áp dụng đúng % giảm giá hạng, tích điểm tự động | Khách hàng thân thiết mua sách và được giảm giá theo hạng thẻ. | Critical |
| **TC_BILL_03** | Thanh toán bằng phương thức Chuyển khoản (paymentMethodId = 2) | Mức 1 (Happy Path) | Thu ngân | `POST /api/bills` | `{"customerId": 1, "paymentMethodId": 2, "details": [{"bookId": 1, "quantity": 1}]}` | Status: 200 OK | Ghi nhận thanh toán chuyển khoản ngân hàng | Khách hàng quét mã VietQR thanh toán tại quầy. | High |
| **TC_BILL_04** | Xem chi tiết hóa đơn theo ID hợp lệ | Mức 1 (Happy Path) | Thu ngân/Quản lý | `GET /api/bills/1/details` | `Header: Bearer Token` | Status: 200 OK | Danh sách chi tiết các cuốn sách trong đơn | In lại hóa đơn bán lẻ hoặc tra cứu đối soát khi khách đổi trả hàng. | High |
| **TC_BILL_05** | Bấm thanh toán khi giỏ hàng rỗng (details rỗng) | Mức 2 (Validation) | Thu ngân | `POST /api/bills` | `{"customerId": 0, "paymentMethodId": 1, "details": []}` | Status: 400 Bad Request | {"message": "Hóa đơn cần phương thức thanh toán hợp lệ và ít nhất một sách."} | Thu ngân vô tình bấm nút Thanh toán khi khách chưa đặt sách nào lên bàn. | High |
| **TC_BILL_06** | Hóa đơn chứa sách trùng lặp nhiều dòng (Duplicate BookId) | Mức 4 (Business Rule) | Thu ngân | `POST /api/bills` | `{"customerId": 0, "paymentMethodId": 1, "details": [{"bookId": 1, "quantity": 1}, {"bookId": 1, "quantity": 2}]}` | Status: 400 Bad Request | {"message": "Hóa đơn chứa sách trùng lặp."} | Thu ngân quét mã vạch 2 lần nhưng hệ thống không gộp dòng mà tạo dòng trùng. | High |
| **TC_BILL_07** | Bán số lượng vượt quá tồn kho thực tế (Hết hàng / Out of Stock) | Mức 4 (Inventory Limit) | Thu ngân | `POST /api/bills` | `{"customerId": 0, "paymentMethodId": 1, "details": [{"bookId": 1, "quantity": 999999}]}` | Status: 400/409 Conflict | "Không thể hoàn tất hóa đơn; vui lòng kiểm tra tồn kho và thử lại." | Khách mua số lượng lớn hơn số sách thực tế còn lại trong kho. | Critical |
| **TC_BILL_08** | Bán cuốn sách đã ngừng kinh doanh (status = 0) | Mức 4 (Business Rule) | Thu ngân | `POST /api/bills` | `{"customerId": 0, "paymentMethodId": 1, "details": [{"bookId": {{inactiveBookId}}, "quantity": 1}]}` | Status: 400 Bad Request | {"message": "Sách không tồn tại hoặc đã ngừng bán."} | Khách hàng mang cuốn sách cũ từ kho phế liệu ra quầy tính tiền. | High |
| **TC_BILL_09** | Nhập số lượng sách mua bằng 0 hoặc số âm | Mức 2 (Boundary) | Thu ngân | `POST /api/bills` | `{"customerId": 0, "paymentMethodId": 1, "details": [{"bookId": 1, "quantity": -2}]}` | Status: 400 Bad Request | {"message": "Thông tin sách trong hóa đơn không hợp lệ."} | Thu ngân bấm nhầm dấu trừ khi chỉnh số lượng trong giỏ hàng. | High |
| **TC_BILL_10** | Thanh toán với phương thức không hợp lệ (paymentMethodId <= 0) | Mức 2 (Boundary) | Thu ngân | `POST /api/bills` | `{"customerId": 0, "paymentMethodId": 0, "details": [{"bookId": 1, "quantity": 1}]}` | Status: 400 Bad Request | Báo phương thức thanh toán không hợp lệ | Thu ngân chưa chọn hình thức thanh toán Tiền mặt hay Chuyển khoản. | Medium |
| **TC_BILL_11** | Thanh toán cho mã khách hàng không tồn tại | Mức 3 (Not Found) | Thu ngân | `POST /api/bills` | `{"customerId": 999999, "paymentMethodId": 1, "details": [{"bookId": 1, "quantity": 1}]}` | Status: 404 Not Found | {"message": "Không tìm thấy khách hàng."} | Thu ngân nhập sai mã thẻ thành viên của khách. | High |
| **TC_BILL_12** | Trừ điểm thưởng vượt quá số điểm hiện có của khách hàng | Mức 4 (Points Limit) | Thu ngân | `POST /api/bills` | `Body chứa pointsUsed vượt quá số dư điểm` | Status: 400 Bad Request | {"message": "Số điểm thưởng vượt giới hạn."} | Thu ngân nhập nhầm số tiền giảm giá vào ô số điểm thưởng của khách. | High |
| **TC_BILL_13** | Tra cứu chi tiết hóa đơn với mã hóa đơn <= 0 | Mức 2 (Boundary) | Thu ngân | `GET /api/bills/0/details` | `Header: Bearer Token` | Status: 400 Bad Request | {"message": "Mã hóa đơn không hợp lệ."} | Client gửi URL với billId không hợp lệ. | Low |
| **TC_BILL_14** | Tra cứu chi tiết hóa đơn không tồn tại trong hệ thống | Mức 3 (Not Found) | Thu ngân | `GET /api/bills/999999/details` | `Header: Bearer Token` | Status: 404 Not Found / Mảng rỗng | Khách hàng cầm tờ hóa đơn giả hoặc hóa đơn của cửa hàng khác đến khiếu nại. | Medium |

---

### Phân hệ 16: Quản lý Nhân sự & Tiền lương (Employees)
* **Controller:** `EmployeeController`
* **Mô tả nghiệp vụ:** Quản lý hồ sơ nhân sự, tiền lương cơ bản, hệ số lương, ngày vào làm, chức vụ và danh sách nhân viên chưa có tài khoản.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_EMP_01** | Lấy danh sách tất cả nhân viên | Mức 1 (Happy Path) | Quản lý/Admin | `GET /api/employees` | `Header: Bearer Token` | Status: 200 OK | Danh sách nhân viên kèm chức vụ | Quản lý xem danh sách nhân sự cửa hàng. | High |
| **TC_EMP_02** | Lấy danh sách nhân viên chưa có tài khoản đăng nhập | Mức 1 (Happy Path) | Admin | `GET /api/employees/without-account` | `Header: adminToken` | Status: 200 OK | Danh sách nhân viên mới chưa được cấp tài khoản | Admin kiểm tra để cấp tài khoản cho nhân viên mới vào làm. | High |
| **TC_EMP_03** | Thêm mới nhân viên hợp lệ | Mức 1 (Happy Path) | Quản lý/Admin | `POST /api/employees` | `{"employeeName": "Lê Văn Toàn", "employeePhone": "0988776655", "email": "toan.le@bookstore.com", "dayIn": "2026-05-01", "baseSalary": 8000000, "salaryFactor": 1.0, "roleId": 3}` | Status: 200 OK | "Thêm nhân viên thành công!" | Phòng nhân sự tiếp nhận nhân viên mới. | High |
| **TC_EMP_04** | Cập nhật lương và chức vụ của nhân viên | Mức 1 (Happy Path) | Quản lý/Admin | `PUT /api/employees/3` | `{"employeeName": "Lê Văn Toàn", "employeePhone": "0988776655", "email": "toan.le@bookstore.com", "baseSalary": 9500000, "salaryFactor": 1.2, "roleId": 3, "status": 1}` | Status: 200 OK | Cập nhật thành công | Tăng lương định kỳ hoặc thăng chức cho nhân viên. | Medium |
| **TC_EMP_05** | Thêm nhân viên với số điện thoại sai định dạng | Mức 2 (Validation) | Quản lý/Admin | `POST /api/employees` | `{"employeeName": "Nguyễn Test", "employeePhone": "123456"}` | Status: 422 Unprocessable Entity | "Số điện thoại phải có 10 số và bắt đầu bằng 0!" | Nhân sự nhập thiếu chữ số trên hồ sơ xin việc. | High |
| **TC_EMP_06** | Thêm nhân viên với số điện thoại đã tồn tại | Mức 3 (Integrity 422) | Quản lý/Admin | `POST /api/employees` | `{"employeeName": "Trùng SĐT", "employeePhone": "0583458548"}` | Status: 422 Unprocessable Entity | "Số điện thoại này đã tồn tại trong hệ thống!" | Nhân sự vô tình thêm lại nhân viên đã có hồ sơ. | High |
| **TC_EMP_07** | Thêm nhân viên với email sai định dạng cú pháp | Mức 2 (Validation) | Quản lý/Admin | `POST /api/employees` | `{"employeeName": "Nguyễn Test", "employeePhone": "0911223344", "email": "toan_le_sai_email"}` | Status: 422 Unprocessable Entity | "Email không đúng định dạng!" | Gõ thiếu ký tự @ trong địa chỉ email. | Medium |
| **TC_EMP_08** | Thêm nhân viên với lương cơ bản hoặc hệ số lương âm | Mức 2 (Boundary) | Quản lý/Admin | `POST /api/employees` | `{"employeeName": "Lương Âm", "employeePhone": "0911223344", "email": "luongam@gmail.com", "baseSalary": -5000000}` | Status: 422 Unprocessable Entity | "Lương cơ bản phải lớn hơn hoặc bằng 0!" | Gõ nhầm dấu âm vào ô tiền lương. | High |
| **TC_EMP_09** | Cho nghỉ việc hoặc đổi chức vụ của Quản lý duy nhất cuối cùng | Mức 4 (Business Rule) | Admin | `PUT /api/employees/2` | `Đổi role hoặc status = 0 của Quản lý duy nhất` | Status: 422 Unprocessable Entity | "Không thể thay đổi chức vụ hoặc cho nghỉ việc, vì hệ thống phải có ít nhất 1 Quản lý/Admin đang làm việc!" | Admin vô tình vô hiệu hóa tài khoản quản lý điều hành duy nhất của nhà sách. | Critical |
| **TC_EMP_10** | Nhân viên Bán hàng gọi API cập nhật hồ sơ lương nhân sự | Mức 5 (RBAC 403) | Thu ngân | `PUT /api/employees/3` | `Body tăng lương` | Status: 403 Forbidden | Không có quyền MANAGE_EMPLOYEE | Nhân viên tự ý gọi API để tự tăng lương cho chính mình. | Critical |

---

### Phân hệ 17: Phân quyền Chức vụ & Ma trận RBAC (Roles & Permissions)
* **Controller:** `RoleController`
* **Mô tả nghiệp vụ:** Quản lý các chức vụ trong nhà sách và phân bổ ma trận quyền hạn (Xem - Thao tác) cho từng chức năng nghiệp vụ.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_ROLE_01** | Lấy danh sách tất cả các chức vụ | Mức 1 (Happy Path) | Admin | `GET /api/roles` | `Header: adminToken` | Status: 200 OK | Danh sách chức vụ (Admin, Quản lý, Nhân viên bán hàng,...) | Admin xem sơ đồ phân cấp tổ chức. | High |
| **TC_ROLE_02** | Xem ma trận phân quyền chi tiết của một chức vụ | Mức 1 (Happy Path) | Admin | `GET /api/roles/1/permissions` | `Header: adminToken` | Status: 200 OK | Ma trận quyền của Admin | Kiểm tra quyền hạn của từng vị trí công việc. | High |
| **TC_ROLE_03** | Tạo mới chức vụ và thiết lập ma trận quyền | Mức 1 (Happy Path) | Admin | `POST /api/roles` | `{"roleName": "Nhân viên Marketing", "permissions": [{"actionCode": "MANAGE_PROMOTION", "view": true, "action": true}]}` | Status: 200 OK | Tạo chức vụ thành công | Cửa hàng mở thêm bộ phận tiếp thị truyền thông. | Medium |
| **TC_ROLE_04** | Cập nhật tên chức vụ và ma trận quyền | Mức 1 (Happy Path) | Admin | `PUT /api/roles/{{roleId}}` | `{"roleName": "Marketing Lead", "permissions": [...]}` | Status: 200 OK | Cập nhật thành công | Điều chỉnh quyền hạn của chức vụ phụ trách khuyến mãi. | Medium |
| **TC_ROLE_05** | Chỉnh sửa chức vụ MẶC ĐỊNH của hệ thống (Admin roleId = 1, Quản lý roleId = 2) | Mức 4 (System Constraint) | Admin | `PUT /api/roles/1` | `{"roleName": "Admin Mod", "permissions": [{"actionCode": "MANAGE_PRODUCT", "view": true, "action": true}]}` | Status: 400 Bad Request | {"message": "Chức vụ mặc định không thể chỉnh sửa."} | Quản trị viên vô tình chỉnh sửa vai trò lõi của hệ thống gây sụp đổ phân quyền. | Critical |
| **TC_ROLE_06** | Cập nhật chức vụ với danh sách quyền rỗng | Mức 2 (Validation) | Admin | `PUT /api/roles/3` | `{"roleName": "Nhân viên", "permissions": []}` | Status: 400 Bad Request | {"message": "Danh sách quyền không được để trống."} | Người dùng bỏ chọn toàn bộ quyền và bấm Lưu. | High |
| **TC_ROLE_07** | Tạo chức vụ với tên rỗng hoặc toàn khoảng trắng | Mức 2 (Validation) | Admin | `POST /api/roles` | `{"roleName": "   ", "permissions": [...] }` | Status: 400 Bad Request | Tên chức vụ không được để trống | Người dùng nhấn Lưu khi chưa nhập tên vị trí. | Medium |
| **TC_ROLE_08** | Quản lý gọi API chỉnh sửa phân quyền Chức vụ | Mức 5 (RBAC 403) | Quản lý | `PUT /api/roles/1` | `Body phân quyền` | Status: 403 Forbidden | Quyền chỉ dành riêng cho Admin tối cao | Quản lý muốn tự cấp quyền tối cao cho chức vụ của mình. | Critical |

---

### Phân hệ 18: Quản lý Tài khoản Hệ thống (Accounts)
* **Controller:** `AccountController`
* **Mô tả nghiệp vụ:** Quản trị tài khoản đăng nhập của nhân viên, cấp phát tài khoản mới, đổi mật khẩu Admin, khóa tài khoản và reset mật khẩu qua email.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_ACC_01** | Lấy danh sách tài khoản hệ thống (Kiểm chứng ẩn mật khẩu) | Mức 1 (Happy Path) | Admin | `GET /api/accounts` | `Header: adminToken` | Status: 200 OK | Tuyệt đối không trả về mật khẩu thô dạng plaintext | Admin theo dõi danh sách tài khoản đang hoạt động. | Critical |
| **TC_ACC_02** | Cấp tài khoản đăng nhập mới cho nhân viên chưa có tài khoản | Mức 1 (Happy Path) | Admin | `POST /api/accounts` | `{"employeeId": {{withoutAccountEmployeeId}}, "username": "newemp@bookstore.com", "password": "123456"}` | Status: 200/422 OK | Cấp tài khoản thành công | Nhân viên mới nhận việc, Admin tạo tài khoản để làm việc. | High |
| **TC_ACC_03** | Cập nhật thông tin tài khoản Quản trị viên (Admin) | Mức 1 (Happy Path) | Admin | `PUT /api/accounts/admin` | `{"oldUsername": "hotro.adminhethong@gmail.com", "newUsername": "hotro.adminhethong@gmail.com", "newPassword": "admin"}` | Status: 200 OK | Cập nhật thông tin Admin thành công | Admin đổi mật khẩu quản trị định kỳ. | High |
| **TC_ACC_04** | Khóa tài khoản nhân viên (status = 0) | Mức 1 (Happy Path) | Admin | `PUT /api/accounts/3` | `{"status": 0}` | Status: 200 OK | Tài khoản chuyển trạng thái Khóa | Nhân viên bị đình chỉ công tác, Admin khóa quyền truy cập. | Critical |
| **TC_ACC_05** | Mở khóa tài khoản nhân viên (status = 1) | Mức 1 (Happy Path) | Admin | `PUT /api/accounts/3` | `{"status": 1}` | Status: 200 OK | Tài khoản được kích hoạt lại | Nhân viên đi làm lại sau đợt đình chỉ, Admin mở lại tài khoản. | High |
| **TC_ACC_06** | Đổi mật khẩu tài khoản Admin với mật khẩu quá ngắn (< 4 ký tự) | Mức 2 (Boundary) | Admin | `PUT /api/accounts/admin` | `{"oldUsername": "hotro.adminhethong@gmail.com", "newUsername": "hotro.adminhethong@gmail.com", "newPassword": "12"}` | Status: 400 Bad Request | {"message": "Mật khẩu mới phải có ít nhất 4 ký tự."} | Admin đặt mật khẩu yếu 2 ký tự. | High |
| **TC_ACC_07** | Cập nhật tài khoản cho nhân viên không tồn tại | Mức 3 (Not Found) | Admin | `PUT /api/accounts/999999` | `{"status": 0}` | Status: 404 Not Found | {"message": "Không tìm thấy tài khoản nhân viên."} | Admin thao tác trên nhân viên đã bị xóa khỏi hệ thống. | Medium |
| **TC_ACC_08** | Cấp tài khoản lần 2 cho nhân viên ĐÃ CÓ TÀI KHOẢN RỒI | Mức 4 (Business Rule) | Admin | `POST /api/accounts` | `{"employeeId": 2, "username": "dup@bookstore.com", "password": "123456"}` | Status: 422 Unprocessable Entity | Báo nhân viên đã có tài khoản | Admin chọn nhầm nhân viên đã có tài khoản. | High |
| **TC_ACC_09** | Nhân viên Bán hàng gọi API khóa tài khoản của người khác | Mức 5 (RBAC 403) | Thu ngân | `PUT /api/accounts/2` | `{"status": 0}` | Status: 403 Forbidden | Quyền chỉ dành cho Admin | Nhân viên cố tình phá hoại bằng cách khóa tài khoản của Quản lý. | Critical |

---

### Phân hệ 19: Báo cáo & Thống kê Đa chiều (Statistics)
* **Controller:** `StatisticsController`
* **Mô tả nghiệp vụ:** Trích xuất báo cáo doanh thu tài chính (ngày, tháng, quý, năm, tổng thể) và sản lượng hàng hóa (top bán chạy, top bán chậm, top khách hàng).

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_STAT_01** | Thống kê doanh thu theo ngày hợp lệ | Mức 1 (Happy Path) | Quản lý | `GET /api/statistics/financial/daily?date=2026-11-18` | `Header: managerToken` | Status: 200 OK | Doanh thu, chi phí trong ngày | Thu ngân và Quản lý chốt ca cuối ngày. | High |
| **TC_STAT_02** | Thống kê tổng quan tài chính toàn hệ thống | Mức 1 (Happy Path) | Quản lý | `GET /api/statistics/financial/total` | `Header: managerToken` | Status: 200 OK | Tổng doanh thu, tổng chi phí nhập, lợi nhuận gộp | Chủ cửa hàng xem bức tranh tài chính toàn diện. | High |
| **TC_STAT_03** | Thống kê quý có doanh thu cao nhất và thấp nhất | Mức 1 (Happy Path) | Quản lý | `GET /api/statistics/financial/quarter-summary?year=2026` | `Header: managerToken` | Status: 200 OK | Tóm tắt quý 1, 2, 3, 4 | Đánh giá mùa vụ kinh doanh trong năm. | Medium |
| **TC_STAT_04** | Xem Top 3 đầu sách bán chạy nhất theo quý | Mức 1 (Happy Path) | Quản lý | `GET /api/statistics/products/top-selling?year=2026&quarter=1` | `Header: managerToken` | Status: 200 OK | Danh sách top 3 sách bán chạy nhất kèm số lượng | Quản lý lên kế hoạch nhập thêm sách bán chạy cho quý tới. | High |
| **TC_STAT_05** | Xem Top 3 đầu sách bán chậm nhất theo quý | Mức 1 (Happy Path) | Quản lý | `GET /api/statistics/products/top-slow?year=2026&quarter=1` | `Header: managerToken` | Status: 200 OK | Danh sách top 3 sách bán ít nhất | Quản lý lên kế hoạch giảm giá xả kho sách tồn đọng. | High |
| **TC_STAT_06** | Xem Top khách hàng mua nhiều sách nhất | Mức 1 (Happy Path) | Quản lý | `GET /api/statistics/customers/top-quantity` | `Header: managerToken` | Status: 200 OK | Top khách mua sỉ/lẻ số lượng lớn | Tri ân khách hàng thân thiết cuối năm. | Medium |
| **TC_STAT_07** | Thống kê doanh thu ngày trước ngày quy định (trước 01/01/2026) | Mức 2 (Boundary) | Quản lý | `GET /api/statistics/financial/daily?date=2020-05-01` | `Header: managerToken` | Status: 400 Bad Request | Báo ngày nằm ngoài khoảng quy định 2026-2099 | Người dùng gõ nhầm năm quá khứ khi xem báo cáo. | Medium |
| **TC_STAT_08** | Thống kê top bán chạy với quý không hợp lệ (quarter = 5) | Mức 2 (Boundary) | Quản lý | `GET /api/statistics/products/top-selling?year=2026&quarter=5` | `Header: managerToken` | Status: 400 Bad Request | {"message": "Quý thống kê không hợp lệ."} | Người dùng gõ số quý lớn hơn 4 (một năm chỉ có 4 quý). | High |
| **TC_STAT_09** | Thống kê top bán chạy với quý số âm (quarter = -1) | Mức 2 (Boundary) | Quản lý | `GET /api/statistics/products/top-selling?year=2026&quarter=-1` | `Header: managerToken` | Status: 400 Bad Request | {"message": "Quý thống kê không hợp lệ."} | Người dùng nhập số quý âm. | Medium |
| **TC_STAT_10** | Thống kê lịch sử giao dịch của khách hàng không tồn tại | Mức 3 (Not Found) | Quản lý | `GET /api/statistics/customers/999999/history?from=2026-01-01&to=2026-12-31` | `Header: managerToken` | Status: 200 OK mảng rỗng [] hoặc 404 | Tra cứu lịch sử của khách hàng chưa từng phát sinh đơn hàng. | Low |
| **TC_STAT_11** | Nhân viên Bán hàng gọi API xem tổng quan tài chính | Mức 5 (RBAC 403) | Thu ngân | `GET /api/statistics/financial/total` | `Header: salesToken` | Status: 403 Forbidden | Quyền chỉ dành cho Quản lý | Thu ngân tò mò xem doanh thu tổng và lợi nhuận của toàn bộ cửa hàng. | Critical |

---

### Phân hệ 20: An toàn Bảo mật & Toàn vẹn Hệ thống (Security & Tampering)
* **Controller:** `SecurityFilter & Controllers`
* **Mô tả nghiệp vụ:** Kiểm tra cơ chế phòng thủ của toàn bộ API Server: Chống tấn công tiêm nhiễm mã độc (SQL Injection, XSS), chống giả mạo quyền và kiểm soát dữ liệu rác.

| Mã TC | Tên Kịch Bản Nghiệp Vụ | Mức Độ | Vai Trò (Actor) | Method & URL | Dữ Liệu Gửi Lên (Request Payload) | Kết Quả Mong Đợi (Status & Message) | Thao Tác Thực Tế Dễ Sinh Lỗi Của Người Dùng | Mức Độ |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC_SEC_01** | Truy cập Endpoint yêu cầu bảo mật nhưng KHÔNG truyền Token | Mức 5 (Missing Token) | Khách lạ | `GET /api/profile` | `Không có Header Authorization` | Status: 401 Unauthorized | {"message": "Phiên đăng nhập không hợp lệ hoặc đã hết hạn."} | Client bị hết session hoặc hacker quét endpoint bảo mật công khai. | Critical |
| **TC_SEC_02** | Truy cập Endpoint với Bearer Token rác / sai chữ ký JWT | Mức 5 (Invalid Token) | Kẻ tấn công | `GET /api/books` | `Header: Authorization: Bearer FakeTamperedToken12345` | Status: 401 Unauthorized | Từ chối kết nối | Kẻ xấu cố tình tự sinh chuỗi token ngẫu nhiên để vượt qua lớp bảo vệ. | Critical |
| **TC_SEC_03** | Truy cập với Token mang cấu trúc sai (không có tiền tố 'Bearer ') | Mức 5 (Malformed Header) | Client lỗi | `GET /api/books` | `Header: Authorization: Basic dXNlcjpwYXNz` | Status: 401 Unauthorized | Từ chối truy cập | Lập trình viên client gọi sai cấu hình xác thực Basic thay vì Bearer. | High |
| **TC_SEC_04** | Tấn công SQL Injection vào ô tìm kiếm sách (' OR '1'='1) | Mức 5 (SQL Injection) | Kẻ tấn công | `GET /api/books?search=' OR '1'='1` | `Header: Bearer Token` | Status: 200 OK | Server xử lý an toàn qua Prepared Statement, không bị lỗi cú pháp 500 | Hacker tiêm mã SQL độc nhằm dump toàn bộ cơ sở dữ liệu sách. | Critical |
| **TC_SEC_05** | Tấn công SQL Injection dạng UNION SELECT vào ô tìm kiếm khách hàng | Mức 5 (SQL Injection) | Kẻ tấn công | `GET /api/customers/phone/0909901421' UNION SELECT 1,2,3--` | `Header: Bearer Token` | Status: 404 Not Found hoặc 400 | Tuyệt đối không lộ cấu trúc bảng CSDL | Hacker cố tình dò tìm tên các cột và bảng nhạy cảm trong DB. | Critical |
| **TC_SEC_06** | Tấn công lưu trữ mã độc XSS Script vào trường Tên tác giả | Mức 5 (Stored XSS) | Kẻ tấn công | `POST /api/authors` | `{"authorName": "<script>alert('HACKED')</script>", "nationality": "VN"}` | Status: 422 Unprocessable Entity (Chặn regex) hoặc 200 OK lưu an toàn dưới dạng text | Kẻ xấu lưu script vào DB để khi người khác mở giao diện sẽ bị dính mã độc. | Critical |
| **TC_SEC_07** | Tấn công lưu trữ mã độc XSS Script vào tiêu đề sách | Mức 5 (Stored XSS) | Kẻ tấn công | `POST /api/books` | `{"bookName": "<img src=x onerror=alert(1)>", ...}` | Status: 422 Unprocessable Entity hoặc Lưu an toàn không thực thi script | Hacker chèn thẻ HTML nguy hiểm vào danh mục sản phẩm. | High |
| **TC_SEC_08** | Leo quyền ngang (IDOR - Insecure Direct Object Reference) trong đổi mật khẩu | Mức 5 (IDOR) | Nhân viên A | `PUT /api/profile` | `Nhân viên A cố đổi mật khẩu bằng cách truyền employeeId của Nhân viên B` | Status: 400/403 | Server định danh theo Token của phiên, không cho phép đổi của người khác | Nhân viên A cố tình đổi mật khẩu của đồng nghiệp để chiếm đoạt tài khoản. | Critical |
| **TC_SEC_09** | Leo quyền dọc: Nhân viên Bán hàng gọi API Reset mật khẩu nhân viên khác | Mức 5 (Privilege Escalation) | Thu ngân | `POST /api/accounts/2/reset-password` | `Header: salesToken` | Status: 403 Forbidden | Từ chối truy cập | Nhân viên cấp dưới cố tình reset mật khẩu của tài khoản Quản lý. | Critical |
| **TC_SEC_10** | Gửi payload JSON bị hỏng cú pháp (Malformed JSON) | Mức 2 (Malformed JSON) | Client lỗi | `POST /api/books` | `Body: { "bookName": "Lỗi JSON",, "coverPrice": 100000 } (thừa dấu phẩy)` | Status: 400 Bad Request | Server bắt lỗi JSON parse an toàn | Client gửi lên chuỗi JSON bị đứt đoạn hoặc sai cú pháp ngữ pháp. | Medium |
| **TC_SEC_11** | Gửi Payload kích thước siêu lớn (Buffer Overflow / DoS Body) | Mức 5 (DoS Protection) | Kẻ tấn công | `POST /api/books` | `Body chứa trường description có độ dài 10 Megabytes` | Status: 413 Payload Too Large hoặc 400 Bad Request | Không làm tràn bộ nhớ JVM | Kẻ xấu cố tình gửi chuỗi văn bản khổng lồ để làm sụp máy chủ API. | High |

---

## 4. MA TRẬN PHÂN PHỐI MÃ LỖI HTTP STATUS CODE TOÀN DIỆN

| HTTP Status Code | Số lượng TCs | Mục đích nghiệp vụ kiểm chứng |
| :---: | :---: | :--- |
| **200 OK / 204 No Content** | **64 TCs** | Các thao tác nghiệp vụ thành công: CRUD, đăng nhập, thanh toán, duyệt phiếu, xuất báo cáo. |
| **400 Bad Request** | **42 TCs** | Dữ liệu đầu vào sai: rỗng, số lượng <= 0, giá bán âm, khoảng ngày ngược, số quý > 4,... |
| **401 Unauthorized** | **12 TCs** | Chưa đăng nhập, sai mật khẩu, tài khoản bị khóa, token hết hạn, token giả mạo,... |
| **403 Forbidden** | **14 TCs** | Vi phạm phân quyền RBAC (Bán hàng duyệt kho, Maker tự duyệt phiếu, nhân viên sửa lương,...). |
| **404 Not Found** | **11 TCs** | Tra cứu ID sách, thể loại, nhà cung cấp, khách hàng, tài khoản không tồn tại. |
| **409 Conflict** | **7 TCs** | Trùng lặp dữ liệu Unique (tên hạng), hết hàng trong kho, duyệt phiếu đã duyệt. |
| **422 Unprocessable Entity** | **8 TCs** | Sai quy tắc nghiệp vụ BUS (Tên chứa số, SĐT sai 10 số, mật khẩu mới trùng mật khẩu cũ,...). |
| **Tổng cộng** | **188 TCs** | **Đầy đủ 100% các tình huống kiểm thử trong thực tế** |

---

## 5. HƯỚNG DẪN THỰC THI & TÍCH HỢP BÁO CÁO

1. **Thực thi trên Postman:** Các ca bán hàng POS hiện có trong [BookStore_POS_All.postman_collection.json](../BookStore_POS_All.postman_collection.json). Xem [hướng dẫn chuẩn bị dữ liệu](../POS_SALES_ADDITIONS.md).
2. **Chạy tự động bằng Collection Runner:**
   - Mở Postman và chọn collection **BookStore POS | Kiểm thử bán hàng**.
   - Chọn Environment: `BookStore API - Local Development (127.0.0.1:8080)`.
   - Chọn Run type: **`Functional`** -> Bấm **Run** để chạy các ca POS.
3. **Sử dụng làm minh chứng báo cáo:** Tester và sinh viên có thể trích dẫn trực tiếp bảng 188 Test Cases này vào báo cáo Đồ án / Bài tập lớn môn Kiểm thử Phần mềm (Software Testing).
