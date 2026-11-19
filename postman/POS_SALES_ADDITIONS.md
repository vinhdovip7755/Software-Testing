# Các ca bán hàng bổ sung

Import `BookStore_POS_All.postman_collection.json`, chọn Replace và dùng environment BookStore_API_Local. Collection có 42 bước, bao gồm chuẩn bị và đăng xuất.

| Mã | Kiểm tra | Kỳ vọng |
| --- | --- | --- |
| BH01 | Thanh toán bằng thẻ | 201; lưu phương thức 3, đúng người bán, trừ tồn đúng |
| BH03 | Mua 2 sách khác nhau, số lượng 1 và 2 | 201; lưu đủ dòng, trừ đúng từng sách |
| BH05 | Sách đang bán nhưng tồn 0 | 409; không tạo hóa đơn hoặc giảm tồn |
| BH06 | Số lượng 1.5 | 400; không tự chuyển thành 1 cuốn |
| BH07A | Không gửi quantity | 400; không tạo hóa đơn hoặc giảm tồn |
| BH07B | quantity bằng null | 400; không tạo hóa đơn hoặc giảm tồn |
| BH08 | Mã sách chưa tồn tại | 400; không tạo hóa đơn hoặc giảm tồn |
| BH09 | Sách A đủ hàng, sách B thiếu | 409; không lưu hóa đơn; giữ nguyên tồn cả A và B |

Các ca tự đọc dữ liệu để chọn sách. Cần ít nhất 2 sách đang bán có giá dương, tồn sách/lô đủ để bán, cùng một sách đang bán có tồn 0. Trước mỗi ca cần sách thử còn ít nhất 3 cuốn. Thiếu điều kiện sẽ báo assertion chuẩn bị thất bại và bỏ qua request phụ thuộc, không coi là đạt.

Chạy trên DB thử nghiệm, không có giao dịch khác hoặc thay đổi giá trong lúc chạy. Hai ca mới thành công tạo thêm 2 hóa đơn, bán tổng cộng 4 cuốn. Toàn bộ collection dự kiến tạo 7 hóa đơn khi đạt. BH09 đối chiếu tồn sách và danh sách hóa đơn; chưa kiểm tra trực tiếp rollback chi tiết, tồn lô, log và điểm.

Đã bỏ TC_BILL_12, SEC15 và SEC16 khỏi collection chung vì đổi điểm chưa được hỗ trợ. Chưa đưa vào lượt chạy chung các ca mua hết kho, sửa dữ liệu lô, gửi đồng thời, lên hạng, mốc tích điểm và quy tắc thanh toán chưa thống nhất. Những ca này cần bộ dữ liệu hoặc môi trường kiểm thử riêng.

## Kết quả chạy thực tế

Newman chạy đủ 42 bước: 104 HTTP request, 150 assertion, 146 đạt và 4 thất bại, tất cả thuộc BH06. API nhận số lượng 1.5 nhưng tạo hóa đơn 201 với số lượng 1; vì vậy sai cả mã phản hồi, thông báo, tính bất biến hóa đơn và tồn kho. Đây là lỗi ứng dụng được test phát hiện, không phải lỗi script. Lượt chạy đã tạo thêm 1 hóa đơn ngoài dự kiến do lỗi này, tức tổng cộng 8 hóa đơn.

Log lần phát hiện lỗi: `target/pos-expanded-run.log`.

Đã sửa BH06 bằng bộ đọc JSON riêng cho `quantity`, chỉ nhận số nguyên, không tự cắt số thập phân. Kiểm thử lại BH06, BH07A và BH07B cùng bước đăng nhập/đăng xuất bằng Newman: 15/15 assertion đạt, không tạo hóa đơn hoặc thay đổi tồn kho. Log: `target/pos-quantity-regression.log`. Các kiểm thử Java liên quan đạt 42/42. Chưa chạy lại toàn bộ collection sau bản sửa này.

Tái tạo collection:

```powershell
node postman/scripts/build-pos-combined.cjs
```
