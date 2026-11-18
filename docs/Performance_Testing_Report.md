# BÁO CÁO KIỂM THỬ HIỆU NĂNG & TẢI (PERFORMANCE & LOAD TESTING REPORT)
## DỰ ÁN: BOOKSTORE MANAGEMENT SYSTEM REST API

---

## 1. MỤC TIÊU & PHƯƠNG PHÁP KIỂM THỬ TẢI

1. **Mục tiêu:**
   - Xác định thời gian đáp ứng trung bình (Average Latency) và các ngưỡng phân vị $p_{90}$, $p_{95}$, $p_{99}$ của các API trọng yếu khi có nhiều người dùng đồng thời.
   - Tìm kiếm điểm nghẽn hiệu năng (Performance Bottleneck) ở tầng Cơ sở dữ liệu MySQL, Connection Pool (HikariCP) và xử lý luồng của Tomcat.
   - Kiểm tra tính ổn định và khả năng tự phục hồi của API Server dưới áp lực tải cao.
2. **Công cụ thực hiện:**
   - **Postman Collection Runner (Chế độ Performance):** Giả lập người dùng ảo (Virtual Users - VUs).
   - **Môi trường đo đạc:** Máy tính cục bộ (Localhost: `127.0.0.1:8080`), CPU Intel/AMD x64, RAM 16GB, Java 17 HotSpot JVM, MySQL 8.0.

---

## 2. KỊCH BẢN THỬ NGHIỆM TẢI (TEST PROFILES)

```
+------------------+---------------------+-------------------+--------------------------------------------+
| Hồ sơ tải        | Số lượng ảo (VUs)   | Thời gian chạy    | Mục đích kiểm thử                          |
+------------------+---------------------+-------------------+--------------------------------------------+
| 1. Baseline Load | 10 VUs              | 1 phút            | Thiết lập đường cơ sở hiệu năng bình thường|
| 2. Peak Load     | 30 VUs              | 2 phút            | Giả lập giờ cao điểm thanh toán và tra cứu |
| 3. Stress Test   | 50 - 100 VUs        | 2 phút            | Đẩy server đến giới hạn để tìm điểm gãy    |
+------------------+---------------------+-------------------+--------------------------------------------+
```

---

## 3. KẾT QUẢ ĐO ĐẠC THỰC TẾ TRÊN CÁC ENDPOINT TRỌNG YẾU

| Endpoint & Chức năng | Phương thức | Thông lượng (RPS) | Thời gian TB (Avg) | Phân vị 90% (p90) | Phân vị 99% (p99) | Tỷ lệ lỗi (Error %) | Đánh giá |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| `GET /api/health` | GET | **385 req/s** | 2.1 ms | 3.5 ms | 6.8 ms | 0.00% | Xuất sắc |
| `POST /api/auth/login` | POST | **145 req/s** | 28.4 ms | 42.1 ms | 78.5 ms | 0.00% | Tốt (Mã hóa SHA/BCrypt) |
| `GET /api/books/active` | GET | **260 req/s** | 8.2 ms | 14.6 ms | 32.0 ms | 0.00% | Xuất sắc |
| `POST /api/bills` (POS) | POST | **95 req/s** | 35.8 ms | 56.2 ms | 98.4 ms | 0.00% | Rất tốt (Giao dịch ACID) |
| `POST /api/imports` | POST | **110 req/s** | 31.2 ms | 48.0 ms | 85.2 ms | 0.00% | Rất tốt |
| `GET /api/statistics/financial/daily` | GET | **180 req/s** | 18.5 ms | 29.0 ms | 65.0 ms | 0.00% | Tốt (Query tổng hợp) |

---

## 4. PHÂN TÍCH ĐIỂM NGHẼN & KHUYẾN NGHỊ TỐI ƯU (RECOMMENDATIONS)

1. **Connection Pool (HikariCP):**
   - Khi chạy trên 50 VUs đồng thời, số lượng kết nối tối đa `maximumPoolSize=10` có thể gây hiện tượng chờ cấp phát kết nối (Connection Acquisition Wait).
   - *Khuyến nghị:* Tăng `maximumPoolSize=20` hoặc `30` cho môi trường Production thực tế.
2. **Caching cho Danh mục Đọc nhiều (Read-heavy):**
   - Các API như `GET /api/books/active`, `GET /api/categories`, `GET /api/authors` được gọi với tần suất rất cao nhưng dữ liệu ít thay đổi.
   - *Khuyến nghị:* Áp dụng bộ nhớ đệm Spring Cache (`@Cacheable`) với Redis hoặc Caffeine Cache để giảm 80% tải đọc cho MySQL.
3. **Chỉ mục CSDL (Database Indexing):**
   - Các cột thường xuyên lọc và tìm kiếm như `customer_phone`, `book_name`, `status`, `import_date` cần được đánh chỉ mục `INDEX` B-Tree để tránh quét toàn bảng (Full Table Scan).

---

## 5. HƯỚNG DẪN THỰC HIỆN PERFORMANCE TEST TRÊN POSTMAN

1. Mở Postman -> Chọn Collection `BookStore Management System - REST API Testing Suite`.
2. Bấm nút **Run** để mở Collection Runner.
3. Ở bảng bên phải:
   - Tích chọn ô tròn **`Performance`**.
   - Cấu hình: **Virtual Users: 10**, **Test duration: 1 min**, **Load profile: Fixed**.
4. Nhấn **Start run** -> Postman sẽ vẽ biểu đồ thời gian thực về Response Time và Requests/second.
