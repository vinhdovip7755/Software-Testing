** Thứ tự thực hiện (trên xuống)

- Hoàn thiện code Chắc là xong...
- Kiểm tra lại tính năng Chắc là xong rồi...
- Ưu tiên viết TRD trước BRD (full tất cả tính năng), số lượng T>50 Done
- Sửa lại BRD Done
- Viết Test Scenario (đợi nhóm Tài gửi code và BRD và TRD)
- Viết Test Case theo Test Scenario
- Tạo bảng ma trận BR | TR | Test Case
- Kiểm thử nhóm Tài theo kịch bản (sửa code nhóm còn lại được cộng điểm !!!)
- Tự sửa code nhóm mình (không sửa được trừ điểm !!!)
- Viết báo cáo theo mẫu

**Yêu cầu bài (chưa đầy đủ, các ý ở dưới là 1 vài ý của cô):

- Viết TR phải đầy đủ tất cả những gì có trên form
- Nút tắt dấu X cần viết nếu kỹ (Có thể bỏ qua)
- Bổ sung triển khai chi tiết
- Mô tả yêu cầu phi chức năng
- Bảo mật dữ liệu
- Phân quyền rõ ràng
- Thống kê lại yêu cầu kỹ thuật của từng chức năng (>50)
- Viết tiếng Việt
- Tạo bảng ma trận
- Test Scenario viết trước
- BR | TR | Test Case (điền sau khi có Test Scenario)

**TR (1 vài ý cô đã nhăc):

- Số điện thoại tìm kiếm khách hàng Done
- Con mắt hiển thị mật khẩu Done
- Sửa lại mục tài khoản Done
- Họ và tên khoản trống thì sao, font tiếng Việt kí tự là gì Done
- Chú ý từ ngữ ngày tháng chọn từ định dạng Done
- Đánh dấu * vào trường thông tin bắt buộc Done
- Điền thông tin vào trường đánh dấu * Done

## Chạy API (đợt chuyển đổi Swing sang API)

API chạy riêng trên `127.0.0.1:8080`. Từ thư mục gốc repository, cài desktop artifact trước:

```powershell
mvn -pl desktop-client -am -DskipTests install
```

Mở một terminal để chạy API:

```powershell
mvn -f api-server/pom.xml spring-boot:run
```

Mở terminal thứ hai để chạy ứng dụng Swing:

```powershell
mvn -pl desktop-client exec:java
```

Để gửi OTP hoặc mật khẩu tạm thời, cấu hình `BOOKSTORE_SMTP_USERNAME` và
`BOOKSTORE_SMTP_PASSWORD` trong môi trường chạy API. Không lưu thông tin SMTP trong mã nguồn;
nếu thông tin cũ từng được commit, hãy thu hồi và tạo thông tin mới.

Kiểm tra API bằng `GET http://127.0.0.1:8080/api/health`. Đăng nhập qua
`POST http://127.0.0.1:8080/api/auth/login` với JSON `{"username":"...","password":"..."}`.
Phản hồi chứa access token; các API nghiệp vụ yêu cầu header
`Authorization: Bearer <accessToken>`.

Các endpoint hiện có:

- `GET /api/books`, `GET /api/books/active`, `GET /api/books/{id}`
- `POST /api/books`, `PUT /api/books/{id}`
- `GET /api/authors`, `POST /api/authors`, `PUT /api/authors/{id}`
- `GET /api/categories`, `GET /api/categories/{id}`, `POST /api/categories`, `PUT /api/categories/{id}`
- `GET /api/suppliers`, `GET /api/suppliers/{id}`, `POST /api/suppliers`, `PUT /api/suppliers/{id}`
- `GET /api/imports`, `GET /api/imports/{id}/details`
- `POST /api/imports`, `POST /api/imports/{id}/approve`, `POST /api/imports/{id}/cancel`
- `POST /api/imports/quick-add-book`
- `GET /api/inventory/books`, `GET /api/inventory/books/{bookId}/lots`
- `GET /api/inventory/logs`
- `GET /api/employees`, `GET /api/employees/without-account`
- `GET /api/employees/{employeeId}/activity`
- `POST /api/employees`, `PUT /api/employees/{employeeId}`
- `GET /api/roles`, `GET /api/roles/{roleId}/permissions`
- `POST /api/roles`, `PUT /api/roles/{roleId}`
- `GET /api/accounts`
- `POST /api/accounts`, `PUT /api/accounts/{employeeId}`, `PUT /api/accounts/admin`
- `POST /api/accounts/{employeeId}/reset-password`
- `GET /api/profile`, `PUT /api/profile`
- `POST /api/auth/password-reset/request`, `POST /api/auth/password-reset/confirm`
- `GET /api/customers`, `GET /api/customers/phone/{phone}`, `GET /api/customers/{id}/statistics`
- `POST /api/customers`, `PUT /api/customers/{id}`
- `GET /api/membership-ranks`, `GET /api/membership-ranks/by-points/{point}`
- `POST /api/membership-ranks`, `PUT /api/membership-ranks/{id}`, `DELETE /api/membership-ranks/{id}`
- `GET /api/promotions`, `GET /api/promotions/search`
- `GET /api/promotions/books`, `GET /api/promotions/{id}/books`, `GET /api/promotions/book/{bookId}`
- `POST /api/promotions`, `PUT /api/promotions/{id}`
- `GET /api/prices`, `GET /api/prices/search?keyword=...&type=...`
- `PUT /api/prices/{bookId}`, `PUT /api/prices/bulk`
- `GET /api/system-parameters/vat`
- `GET /api/bills`, `GET /api/bills/{id}`, `GET /api/bills/{id}/details`
- `POST /api/bills`
- `GET /api/statistics/financial/daily?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/statistics/financial/total`
- `GET /api/statistics/financial/quarter-summary?year=YYYY`
- `GET /api/statistics/products/daily?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/statistics/products/monthly?year=YYYY`, `/quarterly?year=YYYY`, `/yearly`
- `GET /api/statistics/products/top-selling?year=YYYY&quarter=N`
- `GET /api/statistics/products/top-slow?year=YYYY&quarter=N`
- `GET /api/statistics/customers/top-quantity?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/statistics/customers/top-spending?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `GET /api/statistics/customers/{customerId}/history?from=YYYY-MM-DD&to=YYYY-MM-DD`
- `POST /api/auth/logout`

Tạo phiếu yêu cầu quyền `MANAGE_IMPORT`; xem, duyệt và hủy phiếu dùng quyền
`MANAGE_IMPORT_TICKET`. API xác định người lập từ phiên đăng nhập và lấy giá bìa hiện tại
từ dữ liệu sách khi tính giá nhập.

Tạo hóa đơn yêu cầu quyền `MANAGE_SELLING`; xem hóa đơn yêu cầu quyền `MANAGE_BILL`.
API lấy nhân viên từ phiên đăng nhập, tính lại giá khuyến mãi, giảm giá thành viên, VAT
và điểm thưởng từ dữ liệu/cấu hình hiện tại; tồn kho được kiểm tra trong giao dịch lưu hóa đơn.
Quản lý khuyến mãi yêu cầu quyền `MANAGE_PROMOTION`; tìm sách, cập nhật trạng thái theo lịch
và lưu khuyến mãi đều chạy qua API.

Màn hình đăng nhập, danh mục sách, quy trình nhập hàng, quản lý/tìm khách hàng, hạng
thành viên, bán hàng, thanh toán hóa đơn và lịch sử/chi tiết hóa đơn trong Swing hiện gọi
API. Màn hình tồn kho, chi tiết lô nhập và lịch sử biến động kho cũng gọi API, với quyền
xem `MANAGE_INVENTORY`. Các màn hình thống kê tài chính, sản phẩm và khách hàng cũng gọi
API; các endpoint thống kê chỉ yêu cầu quyền xem `STATISTICS`.
Màn hình bảng giá tìm kiếm qua API; thay đổi giá từng sách và hàng loạt yêu cầu quyền thao tác
`MANAGE_PRICE`.
Danh sách, thêm/sửa và hồ sơ hoạt động nhân viên gọi API; xem và cập nhật chức vụ/phân quyền
cũng gọi API. Các thao tác nhân viên yêu cầu `MANAGE_EMPLOYEE`, còn quản lý chức vụ yêu cầu
`MANAGE_ROLE`. Danh sách nhân viên chưa có tài khoản cho phép quyền xem `MANAGE_ACCOUNT`
hoặc `MANAGE_EMPLOYEE`.
Quản lý tài khoản yêu cầu `MANAGE_ACCOUNT`; hồ sơ cá nhân dùng phiên đăng nhập hiện tại.
API chỉ trả thông tin tài khoản đã làm sạch, không trả mật khẩu. OTP đặt lại mật khẩu được
tạo, lưu tạm tối đa 2 phút và xác minh tại API; sau 5 lần nhập sai, mã bị vô hiệu hóa.
