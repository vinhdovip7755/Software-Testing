# Bộ kiểm thử bán hàng POS

Import `BookStore_POS_All.postman_collection.json`, chọn **Replace** nếu Postman hỏi, rồi chọn environment `BookStore_API_Local`. Dùng cơ sở dữ liệu kiểm thử và đặt `baseUrl` theo cổng API. Hai collection Bills và Security dùng để chạy riêng từng nhóm. Khi tổng hợp kết quả POS, lấy số ca từ collection All.

## Các ca đã gộp

| Biến thể hoặc bước lặp | Ca đại diện | Căn cứ |
| --- | --- | --- |
| SEC08, SEC09, BH07A và BH07B | TC_BILL_09 | Số lượng âm và hai dòng bù trừ bị chặn ở điều kiện số lượng không dương. Thiếu hoặc null quantity được đọc thành 0. TC_BILL_09 dùng biên 0 và kiểm tồn kho không đổi. |
| SEC14 | TC_BILL_10 | Thiếu paymentMethodId được đọc thành 0. Cả hai đi qua cùng điều kiện từ chối phương thức. |
| BH05 | TC_BILL_07 | Hết hàng là trường hợp tồn bằng 0. Ca đại diện đặt số lượng bằng tồn hiện tại cộng 1 và kiểm tồn không đổi. |
| BH08 | TC_BILL_08 | Cả hai mã đều vắng trong danh sách sách đang bán. Ca đại diện xác minh sách tồn tại và có status = 0. |
| BH01 | TC_BILL_03 | Thẻ và chuyển khoản cùng đi qua luồng ghi phương thức thanh toán. Ca đại diện kiểm trường paymentMethodId trên hóa đơn đã lưu. |
| SEC03 và SEC04 | SEC01 và SEC02 | Chọn một đại diện cho thiếu xác thực và một cho Bearer token không hợp lệ. SEC07 vẫn kiểm việc thu hồi một token từng hợp lệ. |
| SEC05 | SEC06 | Giữ ca tài khoản kho bị từ chối dù body khai quyền bán hàng. |
| Bước tạo hóa đơn đối chứng | TC_BILL_01 | SEC17 dùng lại hóa đơn khách lẻ đã tạo với cùng sách, số lượng và phương thức. |

TC_BILL_01 kiểm tổng tiền có VAT cho khách lẻ. TC_BILL_02 đọc hạng và điểm trước khi bán, kiểm mức giảm giá rồi đọc lại số dư điểm. TC_BILL_03 đọc lại phương thức thanh toán đã lưu. BH03 vẫn kiểm trừ tồn từng sách trong đơn nhiều dòng. BH06 vẫn kiểm sai kiểu số lượng tại bước đọc JSON. BH09 kiểm không lưu hóa đơn và giữ nguyên tồn của cả hai sách khi một dòng thiếu hàng.

Đặt `customerPhone` thành số của thành viên có chiết khấu dương và chọn `bookId` có giá đủ để đơn hai cuốn phát sinh điểm. Chuẩn bị tối thiểu 5 cuốn cho sách chính trước lượt chạy, đủ tồn lô, và đủ hai mã sách đang bán cho các ca nhiều dòng. Có thể bỏ trống `inactiveBookId` để tự chọn sách ngừng bán. Nếu ca báo lỗi chuẩn bị, bổ sung dữ liệu fixture rồi chạy lại. Lượt chạy đầy đủ và đạt dự kiến tạo 5 hóa đơn.

Tái tạo collection sau khi chỉnh script:

```powershell
node postman/scripts/build-pos-combined.cjs
```

## Kết quả của bộ cũ

Các số liệu dưới đây thuộc bộ 42 bước trước khi gộp ca.

Newman chạy đủ 42 bước: 104 HTTP request, 150 assertion, 146 đạt và 4 thất bại, tất cả thuộc BH06. API nhận số lượng 1.5 nhưng tạo hóa đơn 201 với số lượng 1; vì vậy sai cả mã phản hồi, thông báo, tính bất biến hóa đơn và tồn kho. Đây là lỗi ứng dụng được test phát hiện, không phải lỗi script. Lượt chạy đã tạo thêm 1 hóa đơn ngoài dự kiến do lỗi này, tức tổng cộng 8 hóa đơn.

Log lần phát hiện lỗi: `target/pos-expanded-run.log`.

Đã sửa BH06 bằng bộ đọc JSON riêng cho `quantity`, chỉ nhận số nguyên, không tự cắt số thập phân. Kiểm thử lại BH06, BH07A và BH07B cùng bước đăng nhập/đăng xuất bằng Newman: 15/15 assertion đạt, không tạo hóa đơn hoặc thay đổi tồn kho. Log: `target/pos-quantity-regression.log`. Các kiểm thử Java liên quan đạt 42/42. Chưa chạy lại toàn bộ collection sau bản sửa này.
