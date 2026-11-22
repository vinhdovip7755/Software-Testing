# Kiểm thử giao diện BookStore bằng WinAppDriver (13 Test Cases)

Chạy từ thư mục gốc dự án (`d:\Code\Testing\Software-Testing`) với JDK 17, MySQL đang chạy và Windows Developer Mode đã bật (cho chế độ WinAppDriver). Script tự khởi chạy một phiên bản API server kiểm thử riêng trên cổng cục bộ trống (hỗ trợ bắt mã OTP nội bộ không cần cấu hình SMTP) và tự tắt sạch khi chạy xong.

## Chạy nhanh bằng 1 lệnh

**1. Chạy bằng WinAppDriver thật (mặc định, nhịp demo `700ms` mỗi thao tác để dễ quan sát / quay màn hình):**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver\run.ps1 -Email "vinhyt7755@gmail.com" -Password "07072006"
```

Script tự tìm JDK 17, tự bật/tắt `WinAppDriver.exe` và API kiểm thử, cache file `.class`, và chạy trọn bộ **13 test case** trong 1 phiên JVM.

**2. Chạy ở chế độ Swing trực tiếp (không cần WinAppDriver / không chiếm chuột bàn phím):**

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver\run.ps1 -Driver swing -Email "vinhyt7755@gmail.com" -Password "07072006"
```

**3. Tùy chỉnh tốc độ, gửi Gmail OTP thật & chạy lẻ:**
- Đổi tốc độ dừng giữa các thao tác: `-StepDelayMs 1200` (chậm rõ hơn) hoặc `-StepDelayMs 0` (chạy nhanh nhất).
- Chạy lẻ 1 ca: `-Case <tên_ca>` (vd: `-Case forgotPasswordSuccess` hoặc `-Case bookFormValidation`).
- Chạy tách từng cửa sổ riêng biệt cho từng ca: thêm `-Isolated`.
- **Chế độ gửi mail Gmail thật & dừng đợi bạn nhập OTP (`-ManualOtp`):**
  1. Điền `BOOKSTORE_SMTP_USERNAME` (Gmail gửi) và `BOOKSTORE_SMTP_PASSWORD` (Google App Password 16 ký tự) vào file `.env` ở thư mục gốc dự án (file này đã nằm trong `.gitignore`).
  2. Mặc định ca `forgotPasswordSuccess` gửi mã OTP tới tài khoản **`lowres14@gmail.com`** (mật khẩu gốc `01082006`, có thể đổi qua `-ForgotEmail` và `-ForgotPassword`).
  3. Chạy kèm cờ `-ManualOtp` (mặc định đợi tối đa `120` giây, đổi được bằng `-ManualOtpWaitSec`):
     ```powershell
     powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver\run.ps1 -ManualOtp -Email "vinhyt7755@gmail.com" -Password "07072006"
     ```
     Hoặc nhấp đúp `run-login-test.bat` (đã bật sẵn `-ManualOtp` gửi tới `lowres14@gmail.com`), hoặc chạy riêng ca đổi mật khẩu:
     ```powershell
     powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver\run-login.ps1 -ManualOtp -Case forgotPasswordSuccess -Email "vinhyt7755@gmail.com" -Password "07072006"
     ```
  Khi đến bước `forgotPasswordSuccess`, server sẽ gửi OTP thật về `lowres14@gmail.com` và hiện hộp thoại dừng chờ bạn nhập 6 chữ số OTP rồi tự chạy tiếp phần còn lại.

## Danh sách 13 Test Cases

| # | Ca | Phân hệ | Thao tác & Điều kiện PASS |
|---|---|---|---|
| 1 | `emptyEmail` | Đăng nhập | Bỏ trống Email → bấm Đăng Nhập Ngay → hiện `Cảnh báo`: `Vui lòng nhập Email!` |
| 2 | `invalidEmail` | Đăng nhập | Nhập email sai định dạng (`abc_invalid_mail`) → hiện `Cảnh báo`: `Định dạng Email không hợp lệ!` |
| 3 | `emptyPassword` | Đăng nhập | Nhập email hợp lệ, bỏ trống mật khẩu → hiện `Cảnh báo`: `Vui lòng nhập mật khẩu!` |
| 4 | `wrongPassword` | Đăng nhập | Nhập email thật + mật khẩu sai → hiện `Đăng nhập thất bại`: `Mật khẩu không chính xác!` |
| 5 | `forgotPassword` | Quên mật khẩu | Mở `Quên mật khẩu?` → kiểm tra Step 2 khóa ban đầu, email trống/sai định dạng → gửi OTP với email chưa đăng ký (HTTP 204 anti-enumeration) mở khóa Step 2 + đếm ngược → kiểm tra bỏ trống OTP/MK, OTP không đủ 6 số, MK xác nhận không khớp, bật/tắt con mắt MK, MK quá ngắn (400), OTP không hợp lệ/hết hạn (422) |
| 6 | `forgotPasswordSuccess` | Quên mật khẩu | Nhập email tài khoản thật → nhận mã OTP 6 số thật → thử OTP sai (`Mã OTP không chính xác.`) → nhập **đúng mã OTP** và mật khẩu mới → bấm `Đổi mật khẩu` → hiện `Đổi mật khẩu thành công! Vui lòng đăng nhập lại.` và tự đóng dialog → xác minh mật khẩu cũ bị từ chối → đăng nhập thành công bằng **mật khẩu mới** vào `MainFrame` → đăng xuất về `LoginFrame` và tự khôi phục lại mật khẩu ban đầu trong CSDL |
| 7 | `validLogin` | Đăng nhập | Đăng nhập tài khoản đúng (mật khẩu đã khôi phục) → xác nhận hộp thành công → `MainFrame` hiển thị |
| 8 | `bookSearch` | Bán Hàng | Tìm sách theo mã khớp đúng tập kết quả; nối chuỗi không khớp trả về `0` dòng; xóa từ khóa khôi phục bảng |
| 9 | `addToCart` | Bán Hàng | Chọn sách còn hàng → `Thêm vào hóa đơn` (mặc định `1`) → giỏ có đúng 1 dòng khớp mã, tên, SL `1` |
| 10 | `editCartQuantity` | Bán Hàng | Chọn dòng trong giỏ → `Sửa số lượng` thành `2` → giỏ cập nhật SL `2` |
| 11 | `removeFromCart` | Bán Hàng | Chọn dòng trong giỏ → `Xóa sản phẩm` → giỏ trở về `0` dòng |
| 12 | `bookFormValidation` | Sản Phẩm | Mở `+ THÊM SÁCH` → bấm `+ Thêm tác giả` khi tên sách trống: `Chọn tác giả` mở sạch không bị `Cảnh báo` đè lên; bấm `Hoàn thành` hiện `Lỗi`: `Vui lòng nhập tên sách!` |
| 13 | `logout` | Hệ thống | Bấm `Đăng xuất` → xác nhận `Xác nhận đăng xuất` → quay về màn hình `LoginFrame` |

> Toàn bộ 13 ca không ghi hóa đơn hay để lại thay đổi trong database (mật khẩu sau `forgotPasswordSuccess` luôn được tự động khôi phục trong `finally`). Kết quả được xuất ra `WinAppDriver/results/<thời-gian>/summary.csv`.

## Bộ test riêng cho phần Đăng nhập & Quên mật khẩu (13 Test Cases)

Chạy nhanh riêng phần đăng nhập & quên mật khẩu bằng file `run-login-test.bat` ở thư mục gốc (nhấp đúp là chạy đủ 13 ca, hoặc thêm `-OfflineOnly` để chạy 7 ca UI offline), hoặc chạy bằng PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver\run-login.ps1 -Email "vinhyt7755@gmail.com" -Password "07072006"
```

| Nhóm | Các Test Cases trong `LoginUiTest.java` |
|---|---|
| Offline UI (7 ca, không cần API) | `emptyEmail`, `whitespaceEmail`, `invalidEmailNoAt`, `invalidEmailBadDomain`, `emptyPassword`, `togglePasswordVisibility`, `forgotPasswordValidation` |
| Online API (6 ca) | `forgotPasswordResetFlow` (validation Bước 2 & lỗi 400/422), `forgotPasswordSuccess` (nhập đúng OTP, đổi mật khẩu thành công, đăng nhập mật khẩu mới & khôi phục mật khẩu cũ), `nonExistentAccount`, `wrongPassword`, `validLoginWithEnter`, `logout` |
