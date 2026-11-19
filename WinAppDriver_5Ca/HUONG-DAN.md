# Kiểm thử giao diện BookStore bằng WinAppDriver (11 Test Cases)

Chạy từ thư mục gốc dự án (`d:\Code\Testing\Software-Testing`) với JDK 17, API server đang chạy ở cổng `8080` ([start-api.bat](../start-api.bat)) và Windows Developer Mode đã bật (cho chế độ WinAppDriver).

## Chạy nhanh bằng 1 lệnh

**1. Chạy bằng WinAppDriver thật (mặc định, nhịp demo `700ms` mỗi thao tác để dễ quan sát / quay màn hình):**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver_5Ca\run.ps1 -Email "vinhyt7755@gmail.com" -Password "07072006"
```

Script tự tìm JDK 17, tự bật/tắt `WinAppDriver.exe`, cache file `.class`, và chạy trọn bộ **11 test case** trong 1 phiên JVM.

**2. Chạy ở chế độ Swing trực tiếp (không cần WinAppDriver / không chiếm chuột bàn phím):**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver_5Ca\run.ps1 -Driver swing -Email "vinhyt7755@gmail.com" -Password "07072006"
```

**3. Tùy chỉnh tốc độ & chạy lẻ:**
- Đổi tốc độ dừng giữa các thao tác: `-StepDelayMs 1200` (chậm rõ hơn) hoặc `-StepDelayMs 0` (chạy nhanh nhất).
- Chạy lẻ 1 ca: `-Case <tên_ca>` (vd: `-Case bookFormValidation`).
- Chạy tách từng cửa sổ riêng biệt cho từng ca: thêm `-Isolated`.

## Danh sách 11 Test Cases

| # | Ca | Phân hệ | Thao tác & Điều kiện PASS |
|---|---|---|---|
| 1 | `emptyEmail` | Đăng nhập | Bỏ trống Email → bấm Đăng Nhập Ngay → hiện `Cảnh báo`: `Vui lòng nhập Email!` |
| 2 | `invalidEmail` | Đăng nhập | Nhập email sai định dạng (`abc_invalid_mail`) → hiện `Cảnh báo`: `Định dạng Email không hợp lệ!` |
| 3 | `emptyPassword` | Đăng nhập | Nhập email hợp lệ, bỏ trống mật khẩu → hiện `Cảnh báo`: `Vui lòng nhập mật khẩu!` |
| 4 | `wrongPassword` | Đăng nhập | Nhập email thật + mật khẩu sai → hiện `Đăng nhập thất bại`: `Mật khẩu không chính xác!` |
| 5 | `validLogin` | Đăng nhập | Đăng nhập tài khoản đúng → xác nhận hộp thành công → `MainFrame` hiển thị |
| 6 | `bookSearch` | Bán Hàng | Tìm sách theo mã khớp đúng tập kết quả; nối chuỗi không khớp trả về `0` dòng; xóa từ khóa khôi phục bảng |
| 7 | `addToCart` | Bán Hàng | Chọn sách còn hàng → `Thêm vào hóa đơn` (mặc định `1`) → giỏ có đúng 1 dòng khớp mã, tên, SL `1` |
| 8 | `editCartQuantity` | Bán Hàng | Chọn dòng trong giỏ → `Sửa số lượng` thành `2` → giỏ cập nhật SL `2` |
| 9 | `removeFromCart` | Bán Hàng | Chọn dòng trong giỏ → `Xóa sản phẩm` → giỏ trở về `0` dòng |
| 10 | `bookFormValidation` | Sản Phẩm | Mở `+ THÊM SÁCH` → bấm `+ Thêm tác giả` khi tên sách trống: `Chọn tác giả` mở sạch không bị `Cảnh báo` đè lên; bấm `Hoàn thành` hiện `Lỗi`: `Vui lòng nhập tên sách!` |
| 11 | `logout` | Hệ thống | Bấm `Đăng xuất` → xác nhận `Xác nhận đăng xuất` → quay về màn hình `LoginFrame` |

> Toàn bộ 11 ca đều không ghi hóa đơn hay thay đổi dữ liệu trong database. Kết quả được xuất ra `WinAppDriver_5Ca/results/<thời-gian>/summary.csv`.

## Bộ test riêng cho phần Đăng nhập (11 Test Cases)

Chạy nhanh riêng phần đăng nhập bằng file `run-login-test.bat` ở thư mục gốc (nhấp đúp là chạy, tự nhận biết API đang bật để chạy đủ 11 ca hoặc tự động chạy 7 ca UI offline nếu API đang tắt), hoặc chạy bằng PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver_5Ca\run-login.ps1 -Email "vinhyt7755@gmail.com" -Password "07072006"
```

| Nhóm | Các Test Cases trong `LoginUiTest.java` |
|---|---|
| Offline UI (7 ca, không cần API) | `emptyEmail`, `whitespaceEmail`, `invalidEmailNoAt`, `invalidEmailBadDomain`, `emptyPassword`, `togglePasswordVisibility`, `forgotPasswordValidation` |
| Online API (4 ca, cần `start-api.bat`) | `nonExistentAccount`, `wrongPassword`, `validLoginWithEnter`, `logout` |
