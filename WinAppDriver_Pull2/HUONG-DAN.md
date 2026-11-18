# Kiểm thử giao diện BookStore bằng WinAppDriver

Dùng với Software-Testing(2).zip trên Windows, JDK 17 và Maven. Giải nén để có WinAppDriver_Pull2/run.ps1 ngay dưới thư mục dự án chứa pom.xml, desktop-client và api-server. Dùng bộ này độc lập với WinAppDriver_UI_Testing cũ. Có thể giữ cả hai trong dự án.

## Chuẩn bị một lần

1. Mở thư mục dự án bằng VS Code. Mở Terminal → New Terminal, chọn PowerShell. Các lệnh bên dưới chạy từ thư mục gốc, ví dụ E:\Software-Testing.
2. Kiểm tra Maven bằng `mvn -v`, kiểm tra JDK bằng `& "$env:JAVA_HOME\bin\javac.exe" -version`.
3. Trong bản vừa pull, DatabaseConnection.java đã dùng bookstore_db1. Tài khoản mẫu phải đăng nhập được và có quyền Bán Hàng. Database cần có ít nhất một sách đang bán.
4. Bật Developer Mode trong Windows Settings. Nếu chưa cài WinAppDriver, tải bộ cài MSI ở https://github.com/microsoft/WinAppDriver/releases rồi cài đặt.
5. Đóng cửa sổ BookStore đang mở và dừng API cũ bằng Ctrl+C tại terminal chạy API. Biên dịch lại bản vừa pull:

~~~powershell
mvn -pl desktop-client -am clean install -DskipTests
~~~

Đợi BUILD SUCCESS. Thực hiện lại sau khi thay đổi code ứng dụng.

## Chạy trong ba terminal của VS Code

**Terminal 1** khởi động API và giữ mở.

~~~powershell
mvn -f api-server/pom.xml spring-boot:run
~~~

**Terminal 2** khởi động WinAppDriver và giữ mở. Nếu cài ở thư mục khác, dùng đúng đường dẫn tới WinAppDriver.exe.

~~~powershell
& "${env:ProgramFiles(x86)}\Windows Application Driver\WinAppDriver.exe"
~~~

**Terminal 3** chạy thử ca email trống trước. Ca này chỉ cần WinAppDriver và JAR đã build.

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver_Pull2\run.ps1 -Case emptyEmail
~~~

Sau đó chạy đủ bốn ca bằng lệnh sau. Khi được hỏi, nhập email và mật khẩu tài khoản **BookStore** có quyền Bán Hàng. Ca sai mật khẩu tự thêm một ký tự vào mật khẩu đã nhập.

~~~powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\WinAppDriver_Pull2\run.ps1
~~~

Script tự mở và đóng một cửa sổ riêng cho mỗi ca. Giữ màn hình Windows mở khóa và tránh gõ hoặc chuyển cửa sổ lúc chạy. Để chạy riêng, thêm `-Case wrongPassword`, `-Case validLogin` hoặc `-Case bookSearch`.

## Các kết quả được kiểm tra

| Ca | Thao tác | Điều kiện PASS |
|---|---|---|
| emptyEmail | Bỏ trống rồi nhấn Đăng Nhập Ngay | Hộp Cảnh báo có nội dung Vui lòng nhập Email! |
| wrongPassword | Nhập email có thật và mật khẩu sai | Hộp Đăng nhập thất bại có nội dung Mật khẩu không chính xác! |
| validLogin | Nhập tài khoản đúng, xác nhận hộp thành công | Cửa sổ chính hiển thị |
| bookSearch | Đăng nhập, mở Bán Hàng, tìm theo mã sách lấy từ bảng, rồi nối chuỗi không khớp | Danh sách mã và tên trả về khớp chính xác tập mong đợi; từ khóa không khớp trả về 0 dòng |

WinKeys.java gửi phím qua phiên WinAppDriver Root. BookstoreUiTest.java dùng Swing để định vị và đặt focus vào đúng ô/nút, đọc nội dung hộp thoại và các dòng JTable. Các thao tác nhập và kích hoạt nút đi qua WinAppDriver. Bộ test không ghi đơn hàng hay sửa dữ liệu sách.

Mỗi lần chạy tạo WinAppDriver_Pull2/results/<thời điểm>/summary.csv và log riêng cho từng ca. PASS, FAIL hoặc TIMEOUT là kết quả thực tế của lần chạy. File .log.err chứa lỗi Java nếu có. Nếu chờ quá lâu ở bước đăng nhập thành công, thử đăng nhập thủ công bằng cùng tài khoản rồi kiểm tra API đang chạy với bookstore_db1. Nếu chờ ở danh sách sách, kiểm tra quyền Bán Hàng và ít nhất một sách có trạng thái đang bán. Nếu kiểm tra focus lỗi, giữ màn hình mở khóa, chạy VS Code và WinAppDriver cùng mức quyền rồi chạy lại ca lỗi.

Bộ test đã được kiểm tra biên dịch với JAR của bản pull. Kết quả giao diện cần được xác nhận trên máy Windows chạy WinAppDriver.
