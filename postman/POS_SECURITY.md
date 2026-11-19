# Kiểm thử bảo mật nghiệp vụ bán hàng

Import `BookStore_POS_Security.postman_collection.json` và chọn environment `BookStore_API_Local`.
Chạy toàn bộ collection theo thứ tự; collection tự đăng nhập, tạo phiên riêng để thử đăng xuất và dọn các phiên khi kết thúc bình thường.

## Dữ liệu chuẩn bị

- `baseUrl`: mặc định `http://127.0.0.1:8080`; khởi động API từ bản build mới.
- `salesUsername/salesPassword`: có quyền thao tác `MANAGE_SELLING`, xem sách và xem `MANAGE_BILL`.
- `importUsername/importPassword`: tài khoản nhân viên kho **không có** quyền thao tác `MANAGE_SELLING`. Collection kiểm tra điều kiện này sau đăng nhập.
- `bookId`: sách đang bán, có giá dương, tồn sách và tồn lô đủ ít nhất 2 cuốn.
- Chạy với DB test, không có giao dịch khác hoặc thay đổi giá/khuyến mãi trong lúc kiểm thử. Environment ghi đè biến collection cùng tên.

Hai request đối chứng/giả mạo tạo tổng cộng **2 hóa đơn và giảm tồn 2 cuốn** khi đạt. Các ca bị từ chối kiểm tra tồn trước/sau; chúng không thay thế kiểm tra rollback toàn bộ DB.

## 18 test case tự động trong Postman

| ID | Tình huống | Kỳ vọng |
| --- | --- | --- |
| SEC01 | Không gửi Authorization | 401 |
| SEC02 | Token giả | 401 |
| SEC03 | Bearer rỗng | 401 |
| SEC04 | Dùng Basic thay Bearer | 401 |
| SEC05 | Nhân viên kho gửi đơn | 403 |
| SEC06 | Nhân viên kho chèn roleId, employeeId và quyền bán hàng vào body | 403 |
| SEC07 | Dùng lại token sau đăng xuất | 401 |
| SEC08 | Số lượng âm nhằm tăng tồn | 400 |
| SEC09 | Hai dòng cùng sách, một dòng âm để bù trừ | 400 |
| SEC10 | Số lượng vượt phạm vi int | 400 |
| SEC11 | Chuỗi SQL injection trong bookId | 400 |
| SEC12 | Phần tử null trong giỏ | 400 |
| SEC13 | customerId âm | 400 |
| SEC14 | Không gửi phương thức thanh toán | 400 |
| SEC15 | pointsUsed âm | 400 |
| SEC16 | Khách lẻ gửi yêu cầu tiêu điểm | 400 |
| SEC17 | Giả giá, khuyến mãi, VAT, tổng tiền, điểm, mã hóa đơn và người lập | 201; giá trị tính toán giống đơn đối chứng, mã hóa đơn do server sinh |
| SEC18 | Đọc lại hóa đơn SEC17 | Người lập lấy từ phiên; tổng tiền, VAT và điểm lưu đúng |

SEC01–16 còn kiểm tra thông báo lỗi, không trả `billId` và tồn kho không đổi.
Collection có 25 request gồm 18 ca trên và 7 bước chuẩn bị/dọn phiên.
Quyền được kiểm tra theo permission, không theo tên chức vụ: quản lý có quyền bán hàng vẫn được tạo đơn.

## Các kịch bản cần môi trường riêng

Những kịch bản sau là checklist kiểm thử bổ sung, **chưa được tự động hóa hoặc xác nhận đạt**:

| Tình huống | Cách kiểm tra | Kỳ vọng cần đối chiếu |
| --- | --- | --- |
| Hai thu ngân tranh cuốn cuối | Chuẩn bị đúng 1 cuốn trong sách và lô; gửi đồng thời hai đơn số lượng 1 | Chỉ một đơn thành công, tồn không âm, đơn thất bại không để lại chi tiết/log/điểm |
| Gửi lại đơn do timeout hoặc bấm đúp | Gửi hai lần cùng một yêu cầu thanh toán | Cần thống nhất cơ chế idempotency; API hiện chưa khai báo khóa chống tạo trùng, không được coi là đã bảo vệ |
| Thu hồi quyền khi token còn sống | Đăng nhập, quản trị bỏ quyền bán rồi dùng token cũ | Xác định chính sách thu hồi tức thời; session hiện giữ bản chụp quyền lúc đăng nhập |
| Token hết hạn 8 giờ | Dùng đồng hồ kiểm thử hoặc phiên đã quá hạn | 401; khác với SEC07 chỉ kiểm tra đăng xuất |
| Tồn sách đủ nhưng tổng tồn lô thiếu | Tạo dữ liệu lệch trong DB test rồi bán | Không lưu hóa đơn dở dang; không thay đổi tồn, điểm và nhật ký sau rollback |

## Kiểm thử Java

`BillHttpTest` bao phủ thêm quyền chỉ xem bán hàng (`view=true`, `action=false`), các dạng token không hợp lệ và đầu vào bất thường qua HTTP MockMvc.
Test này không ghi DB. Ngày 19/11/2026, collection đã chạy end-to-end bằng Newman trên API local: 25 bước chính, 57 HTTP request (gồm đọc tồn kho), 103 assertion đạt, không lỗi. Lượt chạy tạo 2 hóa đơn đối chứng.

Đã sửa lỗi 500 khi Spring không tìm được tên tham số URL bằng cách khai báo tên rõ ràng trong `@PathVariable` và `@RequestParam`. Nếu đọc tồn kho gặp lỗi, script chuẩn bị ghi assertion thất bại thay vì ném lỗi làm dừng toàn bộ collection.

Chỉnh sửa generator `scripts/build-pos-security.cjs`, sau đó chạy:

```powershell
node postman/scripts/build-pos-security.cjs
```
