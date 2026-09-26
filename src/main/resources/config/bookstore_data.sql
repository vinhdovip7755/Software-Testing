-- =============================================================================
-- FILE: bookstore_data.sql
-- BỘ DỮ LIỆU MẪU CHUẨN THỰC TẾ CHO HỆ THỐNG QUẢN LÝ NHÀ SÁCH
-- Bao gồm: Nhân viên, Tài khoản, Phân quyền, Thể loại, Tác giả, Nhà cung cấp,
-- Sách, Bảng giá, Khuyến mãi, Phiếu nhập, Lô sách, Tồn kho, Khách hàng, Hóa đơn
-- Chạy sau khi bookstore_schema.sql đã được thực thi thành công.
-- =============================================================================

USE bookstore_db;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE inventory_log;
TRUNCATE TABLE bill_detail;
TRUNCATE TABLE bill;
TRUNCATE TABLE book_lot;
TRUNCATE TABLE import_ticket_detail;
TRUNCATE TABLE import_ticket;
TRUNCATE TABLE price;
TRUNCATE TABLE promotion_detail;
TRUNCATE TABLE promotion;
TRUNCATE TABLE book_author;
TRUNCATE TABLE book;
TRUNCATE TABLE customer;
TRUNCATE TABLE permission;
TRUNCATE TABLE account;
TRUNCATE TABLE employee;
TRUNCATE TABLE author;
TRUNCATE TABLE category;
TRUNCATE TABLE supplier;
TRUNCATE TABLE payment_method;
TRUNCATE TABLE action;
TRUNCATE TABLE role;
TRUNCATE TABLE membership_rank;
TRUNCATE TABLE system_parameter;

SET FOREIGN_KEY_CHECKS = 1;

-- 1. VAI TRÒ (ROLES)
INSERT INTO role (role_id, role_name) VALUES
(1, 'Admin'),
(2, 'Quản lý'),
(3, 'Nhân viên Bán hàng'),
(4, 'Nhân viên Nhập hàng');

-- 2. CHỨC NĂNG / HÀNH ĐỘNG (ACTIONS)
INSERT INTO action (action_id, action_code, action_name) VALUES
(1, 'MANAGE_SELLING', 'Quản lý Bán hàng'),
(2, 'MANAGE_CUSTOMER', 'Quản lý Khách hàng'),
(3, 'MANAGE_PRODUCT', 'Quản lý Sản phẩm'),
(4, 'MANAGE_AUTHOR', 'Quản lý Tác giả'),
(5, 'MANAGE_CATEGORY', 'Quản lý Thể loại'),
(6, 'MANAGE_SUPPLIER', 'Quản lý Nhà cung cấp'),
(7, 'MANAGE_PRICE', 'Quản lý Giá Bán'),
(8, 'MANAGE_PROMOTION', 'Quản lý Khuyến Mãi'),
(9, 'MANAGE_IMPORT_TICKET', 'Quản Lý Phiếu Nhập'),
(10, 'MANAGE_IMPORT', 'Quản Lý Nhập Hàng'),
(11, 'MANAGE_INVENTORY', 'Quản lý Tồn kho'),
(12, 'MANAGE_BILL', 'Quản lý Hóa đơn'),
(13, 'MANAGE_EMPLOYEE', 'Quản Lý Nhân Viên'),
(14, 'STATISTICS', 'Thống Kê'),
(15, 'MANAGE_ACCOUNT', 'Quản Lý Tài Khoản'),
(16, 'MANAGE_ROLE', 'Quản Lý Quyền');

-- 3. MA TRẬN PHÂN QUYỀN (PERMISSIONS)
INSERT INTO permission (role_id, action_id, is_view, is_action) VALUES
(1, 15, 1, 1),
(1, 16, 1, 1),
(2, 1, 1, 1),
(2, 2, 1, 1),
(2, 3, 1, 1),
(2, 4, 1, 1),
(2, 5, 1, 1),
(2, 6, 1, 1),
(2, 7, 1, 1),
(2, 8, 1, 1),
(2, 9, 1, 1),
(2, 10, 1, 1),
(2, 11, 1, 1),
(2, 12, 1, 1),
(2, 13, 1, 1),
(2, 14, 1, 1),
(3, 1, 1, 1),
(3, 12, 1, 1),
(3, 2, 1, 1),
(3, 3, 1, 0),
(4, 9, 1, 1),
(4, 10, 1, 1),
(4, 11, 1, 1),
(4, 3, 1, 1),
(4, 4, 1, 1),
(4, 5, 1, 1),
(4, 6, 1, 1);

-- 4. PHƯƠNG THỨC THANH TOÁN (PAYMENT METHODS)
INSERT INTO payment_method (payment_method_id, payment_method_name) VALUES
(1, 'Tiền mặt'),
(2, 'Chuyển khoản ngân hàng'),
(3, 'Thẻ tín dụng/Ghi nợ');

-- 5. THAM SỐ HỆ THỐNG (SYSTEM PARAMETERS)
INSERT INTO system_parameter (param_key, param_value, description) VALUES
('VAT', '8', 'Thuế giá trị gia tăng mặc định (8%)'),
('EARNED_POINTS_PER_10K', '100', 'Số điểm nhận được trên mỗi 10K mua');

-- 6. HẠNG THÀNH VIÊN (MEMBERSHIP RANKS)
INSERT INTO membership_rank (rank_id, rank_name, min_point, discount_percent) VALUES
(1, 'Thành viên', 0, 0.00),
(2, 'Bạc', 2000, 5.00),
(3, 'Vàng', 7000, 10.00),
(4, 'Bạch Kim', 20000, 15.00);

-- 7. NHÂN VIÊN & TÀI KHOẢN ĐĂNG NHẬP
INSERT INTO employee (employee_id, employee_name, employee_phone, email, birthday, base_salary, salary_factor, day_in, status, role_id) VALUES
(1, 'Trần Huỳnh Thiên Nhật', '0901234567', 'hotro.adminhethong@gmail.com', '2006-07-11', 18000000, 1.50, '2023-01-01', 1, 1),
(2, 'Đỗ Hoàng Vinh', '0583458548', 'vinhyt7755@gmail.com', '2006-07-07', 14000000, 1.30, '2024-03-01', 1, 2),
(3, 'Lê Ngọc Quý', '0934129959', 'quy.ngoc@bookstore.com', '2006-06-18', 8500000, 1.10, '2024-06-15', 1, 3),
(4, 'Nguyễn Cao Tòng Nhân', '0912357394', 'nhan15042006@gmail.com', '2006-04-15', 8500000, 1.10, '2024-07-01', 1, 4),
(5, 'Nguyễn Hữu Nghĩa', '0998929485', 'lowres14@gmail.com', '2001-08-01', 8000000, 1.00, '2025-01-10', 1, 3);

INSERT INTO account (username, password, employee_id, status) VALUES
('hotro.adminhethong@gmail.com', 'admin', 1, 1),
('vinhyt7755@gmail.com', '07072006', 2, 1),
('quy.ngoc@bookstore.com', '18062004', 3, 1),
('nhan15042006@gmail.com', '15042006', 4, 1),
('lowres14@gmail.com', '01082006', 5, 1);

-- 8. NHÀ CUNG CẤP / NHÀ XUẤT BẢN
INSERT INTO supplier (supplier_id, supplier_name, supplier_address, supplier_phone, status) VALUES
(1, 'NXB Trẻ', '161B Lý Chính Thắng, Phường Võ Thị Sáu, Quận 3, TP. Hồ Chí Minh', '02839316289', 1),
(2, 'NXB Kim Đồng', '55 Quang Trung, Phường Nguyễn Du, Quận Hai Bà Trưng, Hà Nội', '02439434730', 1),
(3, 'Công ty Văn hóa & Truyền thông Nhã Nam', '59 Đỗ Quang, Trung Hòa, Cầu Giấy, Hà Nội', '02435146875', 1),
(4, 'First News - Trí Việt', '11H Nguyễn Thị Minh Khai, Bến Nghé, Quận 1, TP. Hồ Chí Minh', '02838227979', 1),
(5, 'NXB Tổng Hợp TP.HCM', '62 Nguyễn Thị Minh Khai, Bến Nghé, Quận 1, TP. Hồ Chí Minh', '02838225340', 1),
(6, 'Công ty Cổ phần Sách Alpha (Alphabooks)', 'Tầng 3, Dream Center Home, 11A ngõ 282 Nguyễn Huy Tưởng, Thanh Xuân, Hà Nội', '0932329986', 1),
(7, 'NXB Dân Trí', 'Số 9 ngõ 26 Hoàng Cầu, Ô Chợ Dừa, Đống Đa, Hà Nội', '02466860751', 1),
(8, 'NXB Văn Học', '18 Nguyễn Trường Tộ, Ba Đình, Hà Nội', '02437161518', 1);

-- 9. THỂ LOẠI SÁCH
INSERT INTO category (category_id, category_name) VALUES
(1, 'Văn học - Tiểu thuyết'),
(2, 'Công nghệ thông tin - Lập trình'),
(3, 'Truyện tranh - Manga'),
(4, 'Kỹ năng sống - Phát triển bản thân'),
(5, 'Giáo dục - Ngoại ngữ'),
(6, 'Tâm lý - Khoa học xã hội'),
(7, 'Kinh tế - Tài chính - Kinh doanh');

-- 10. TÁC GIẢ
INSERT INTO author (author_id, author_name, nationality) VALUES
(1, 'Nguyễn Nhật Ánh', 'Việt Nam'),
(2, 'Nam Cao', 'Việt Nam'),
(3, 'Vũ Trọng Phụng', 'Việt Nam'),
(4, 'Nguyễn Ngọc Tư', 'Việt Nam'),
(5, 'Tô Hoài', 'Việt Nam'),
(6, 'Robert C. Martin', 'Mỹ'),
(7, 'Martin Fowler', 'Anh'),
(8, 'Erich Gamma', 'Thụy Sĩ'),
(9, 'Richard Helm', 'Mỹ'),
(10, 'Ralph Johnson', 'Mỹ'),
(11, 'John Vlissides', 'Mỹ'),
(12, 'Dale Carnegie', 'Mỹ'),
(13, 'Paulo Coelho', 'Brazil'),
(14, 'James Clear', 'Mỹ'),
(15, 'Morgan Housel', 'Mỹ'),
(16, 'Robert Kiyosaki', 'Mỹ'),
(17, 'Gosho Aoyama', 'Nhật Bản'),
(18, 'Fujiko F. Fujio', 'Nhật Bản'),
(19, 'Eiichiro Oda', 'Nhật Bản'),
(20, 'Akira Toriyama', 'Nhật Bản'),
(21, 'Antoine de Saint-Exupéry', 'Pháp'),
(22, 'J.K. Rowling', 'Anh'),
(23, 'Sir Arthur Conan Doyle', 'Anh'),
(24, 'Haruki Murakami', 'Nhật Bản'),
(25, 'Koga Fumitake', 'Nhật Bản'),
(26, 'Kishimi Ichiro', 'Nhật Bản'),
(27, 'Cao Minh', 'Trung Quốc'),
(28, 'TS. David J. Lieberman', 'Mỹ'),
(29, 'Raymond Murphy', 'Anh'),
(30, 'Thomas H. Cormen', 'Mỹ');

-- 11. DANH SÁCH SÁCH (SẢN PHẨM)
INSERT INTO book (book_id, book_name, publication_year, selling_price, quantity, translator, image, description, status, category_id, tag_detail, supplier_id) VALUES
(1, 'Cho Tôi Xin Một Vé Đi Tuổi Thơ', 2022, 95000, 85, NULL, 'cho_toi_xin_mot_ve_di_tuoi_tho.jpg', 'Tác phẩm kinh điển về tuổi thơ hồn nhiên trong sáng của nhà văn Nguyễn Nhật Ánh.', 1, 1, 'Văn học, Tuổi thơ, Nguyễn Nhật Ánh', 1),
(2, 'Mắt Biếc', 2023, 110000, 97, NULL, 'mat_biec.jpg', 'Câu chuyện tình buồn man mác của Ngạn và Hà Lan gắn với làng Đo Đo.', 1, 1, 'Văn học, Tình cảm, Nguyễn Nhật Ánh', 1),
(3, 'Cánh Đồng Bất Tận', 2021, 85000, 38, NULL, 'canh_dong_bat_tan.jpg', 'Tập truyện ngắn đoạt nhiều giải thưởng danh giá của nhà văn Nguyễn Ngọc Tư.', 1, 1, 'Truyện ngắn, Hiện thực, Miền Tây', 1),
(4, 'Dế Mèn Phiêu Lưu Ký', 2022, 60000, 49, NULL, 'de_men_phieu_luu_ky.jpg', 'Cuốn sách thiếu nhi gối đầu giường của nhiều thế hệ độc giả Việt Nam.', 1, 1, 'Thiếu nhi, Phiêu lưu, Kinh điển', 2),
(5, 'Số Đỏ', 2020, 75000, 48, NULL, 'so_do.jpg', 'Kiệt tác văn học hiện thực trào phúng đỉnh cao của Vũ Trọng Phụng.', 1, 1, 'Văn học, Trào phúng, Hiện thực', 8),
(6, 'Chí Phèo', 2021, 65000, 38, NULL, 'chi_pheo.jpg', 'Tập truyện ngắn bất hủ của Nam Cao viết về người nông dân trước cách mạng.', 1, 1, 'Văn học, Hiện thực, Nam Cao', 8),
(7, 'Rừng Na Uy', 2020, 145000, 44, 'Trịnh Lữ', 'rung_na_uy.jpg', 'Tiểu thuyết nổi tiếng nhất của Haruki Murakami lay động hàng triệu trái tim.', 1, 1, 'Tiểu thuyết, Tình cảm, Nhật Bản', 3),
(8, 'Hoàng Tử Bé', 2022, 70000, 58, 'Trịnh Thu Hồng', 'hoang_tu_be.jpg', 'Cuốn sách triết lý sâu sắc và trong trẻo dành cho cả trẻ em và người lớn.', 1, 1, 'Kinh điển, Triết lý, Pháp', 3),
(9, 'Harry Potter và Hòn Đá Phù Thủy', 2021, 155000, 48, 'Lý Lan', 'harry_potter_1.jpg', 'Tập đầu tiên mở ra thế giới phù thủy kỳ diệu tại trường Hogwarts.', 1, 1, 'Giả tưởng, Phép thuật, Thiếu nhi', 1),
(10, 'Sherlock Holmes Toàn Tập - Tập 1', 2020, 180000, 40, 'Đỗ Tư Nghĩa', 'sherlock_holmes_1.jpg', 'Bộ truyện trinh thám kinh điển thế giới với thám tử tài ba Sherlock Holmes.', 1, 1, 'Trinh thám, Kinh điển, Arthur Conan Doyle', 3),
(11, 'Clean Code - Mã Sạch', 2021, 230000, 43, 'Phạm Bình', 'clean_code.jpg', 'Cẩm nang hướng dẫn viết code chuyên nghiệp, dễ đọc và dễ bảo trì.', 1, 2, 'Lập trình, Clean Code, Phần mềm', 6),
(12, 'Design Patterns: Gang of Four', 2020, 280000, 34, 'Nguyễn Văn Tuấn', 'design_patterns.jpg', '23 mẫu thiết kế hướng đối tượng kinh điển trong kỹ nghệ phần mềm.', 1, 2, 'Kiến trúc phần mềm, Design Patterns, OOP', 6),
(13, 'Refactoring: Improving Code Design', 2022, 250000, 38, 'Trần Quang', 'refactoring.jpg', 'Tái cấu trúc mã nguồn nâng cao chất lượng dự án phần mềm.', 1, 2, 'Lập trình, Refactoring, Software Engineering', 6),
(14, 'Introduction to Algorithms', 2021, 350000, 29, 'Lê Hữu', 'intro_to_algorithms.jpg', 'Giáo trình thuật toán và cấu trúc dữ liệu toàn diện nhất thế giới.', 1, 2, 'Thuật toán, Khoa học máy tính, IT', 6),
(15, 'Lập Trình Java Căn Bản Đến Nâng Cao', 2023, 160000, 59, NULL, 'java_core.jpg', 'Học lập trình Java thực chiến từ cơ bản đến ứng dụng phần mềm nâng cao.', 1, 2, 'Java, Lập trình, Giáo trình', 5),
(16, 'Thám Tử Lừng Danh Conan - Tập 100', 2022, 35000, 105, 'Nguyễn Hương', 'conan_100.jpg', 'Cột mốc tập 100 của thám tử nhí lừng danh Conan Edogawa.', 1, 3, 'Manga, Trinh thám, Conan', 2),
(17, 'Doraemon Tuyển Tập Tranh Màu - Tập 1', 2021, 45000, 72, 'Kim Đồng Books', 'doraemon_color_1.jpg', 'Chú mèo máy thông minh đến từ thế kỷ 22 với các bảo bối kỳ diệu.', 1, 3, 'Manga, Thiếu nhi, Doraemon', 2),
(18, 'One Piece - Tập 100', 2022, 35000, 101, 'Kim Đồng Books', 'one_piece_100.jpg', 'Hành trình tìm kiếm kho báu vĩ đại của Vua Hải Tặc tương lai Monkey D. Luffy.', 1, 3, 'Manga, Phiêu lưu, Hành động', 2),
(19, 'Dragon Ball - 7 Viên Ngọc Rồng - Tập 1', 2020, 35000, 86, 'Kim Đồng Books', 'dragon_ball_1.jpg', 'Tác phẩm huyền thoại của họa sĩ đại tài Akira Toriyama.', 1, 3, 'Manga, Hành động, Võ thuật', 2),
(20, 'Đắc Nhân Tâm', 2021, 95000, 146, 'Nguyễn Hiến Lê', 'dac_nhan_tam.jpg', 'Nghệ thuật thu phục lòng người và giao tiếp đỉnh cao của Dale Carnegie.', 1, 4, 'Kỹ năng sống, Giao tiếp, Dale Carnegie', 4),
(21, 'Nhà Giả Kim', 2020, 85000, 77, 'Lê Chu Cầu', 'nha_gia_kim.jpg', 'Hành trình đi tìm kho báu và thực hiện vận mệnh cuộc đời của chàng Santiago.', 1, 4, 'Triết lý, Tiểu thuyết, Paulo Coelho', 3),
(22, 'Atomic Habits - Thay Đổi Tí Hon', 2022, 165000, 108, 'Hà Nguyễn', 'atomic_habits.jpg', 'Cách xây dựng thói quen tốt và từ bỏ thói quen xấu từng bước nhỏ.', 1, 4, 'Kỹ năng sống, Năng suất, Thói quen', 6),
(23, 'Quẳng Gánh Lo Đi Và Vui Sống', 2021, 90000, 50, 'Nguyễn Hiến Lê', 'quang_ganh_lo_di.jpg', 'Bí quyết giải tỏa căng thẳng và sống vui vẻ trọn vẹn mỗi ngày.', 1, 4, 'Kỹ năng sống, Tâm lý, Dale Carnegie', 4),
(24, 'Tuổi Trẻ Đáng Giá Bao Nhiêu', 2021, 90000, 58, NULL, 'tuoi_tre_dang_gia_bao_nhieu.jpg', 'Cuốn sách truyền cảm hứng sống và học tập cho hàng triệu bạn trẻ Việt Nam.', 1, 4, 'Kỹ năng sống, Tuổi trẻ, Phát triển bản thân', 3),
(25, 'English Grammar in Use', 2022, 195000, 49, NULL, 'grammar_in_use.jpg', 'Bộ sách ngữ pháp tiếng Anh chuẩn Cambridge được tin cậy nhất toàn cầu.', 1, 5, 'Tiếng Anh, Ngữ pháp, Cambridge', 5),
(26, 'Luyện Thi IELTS General & Academic', 2023, 210000, 40, 'IELTS Team', 'ielts_practice.jpg', 'Tổng hợp đề thi thử và chiến lược làm bài IELTS 8.0 chuyên sâu.', 1, 5, 'IELTS, Ngoại ngữ, Luyện thi', 5),
(27, 'Tự Học 2000 Từ Vựng Tiếng Anh', 2022, 120000, 45, 'Vũ Thùy Linh', 'tu_hoc_tu_vung.jpg', 'Phương pháp nhớ từ vựng tiếng Anh giao tiếp nhanh và lâu qua ngữ cảnh.', 1, 5, 'Tiếng Anh, Từ vựng, Giao tiếp', 7),
(28, 'Dám Bị Ghét', 2021, 125000, 54, 'Trịnh Lữ', 'dam_bi_ghet.jpg', 'Tâm lý học Alfred Adler về tự do và dũng khí thay đổi bản thân.', 1, 6, 'Tâm lý học, Triết học, Phát triển cá nhân', 3),
(29, 'Thiên Tài Bên Trái Kẻ Điên Bên Phải', 2022, 140000, 50, 'Thu Hương', 'thien_tai_ben_trai.jpg', 'Góc nhìn độc đáo về thế giới quan của những bệnh nhân tâm thần.', 1, 6, 'Tâm lý học, Tư duy, Xã hội', 7),
(30, 'Đọc Vị Bất Kỳ Ai', 2021, 105000, 60, 'Quỳnh Lê', 'doc_vi_bat_ky_ai.jpg', 'Phương pháp tâm lý học để thấu hiểu suy nghĩ và động cơ của người đối diện.', 1, 6, 'Tâm lý, Giao tiếp, David Lieberman', 4),
(31, 'Tâm Lý Học Tội Phạm', 2022, 135000, 45, NULL, 'tam_ly_hoc_toi_pham.jpg', 'Phân tích hành vi, động cơ và tâm lý tội phạm học hiện đại.', 1, 6, 'Tâm lý, Tội phạm học, Xã hội', 7),
(32, 'Tâm Lý Học Về Tiền', 2022, 175000, 63, 'Huy Hoàng', 'the_psychology_of_money.jpg', 'Những bài học vĩnh cửu về sự giàu có, lòng tham và hạnh phúc tài chính.', 1, 7, 'Tài chính cá nhân, Tâm lý, Kinh tế', 6),
(33, 'Cha Giàu Cha Nghèo - Tập 1', 2021, 115000, 72, 'Hà Linh', 'cha_giau_cha_ngheo_1.jpg', 'Tư duy tài chính xuất chúng thay đổi cuộc đời và cách tiền làm việc cho bạn.', 1, 7, 'Tài chính cá nhân, Làm giàu, Robert Kiyosaki', 4),
(34, 'Nhà Đầu Tư Thông Minh', 2020, 260000, 39, 'Trần Thảo', 'nha_dau_tu_thong_minh.jpg', 'Kinh thánh về đầu tư giá trị của Benjamin Graham được Warren Buffett khuyên đọc.', 1, 7, 'Đầu tư, Chứng khoán, Kinh tế', 6),
(35, 'Sổ Tay Lập Trình Pascal Cũ (Ngừng xuất bản)', 2010, 45000, 0, NULL, 'pascal_old.jpg', 'Tài liệu ngôn ngữ Pascal cũ của những năm 2000 không còn lưu hành.', 0, 2, 'Lập trình cũ, Lịch sử, Ngừng bán', 5);

-- 12. QUAN HỆ SÁCH - TÁC GIẢ
INSERT INTO book_author (book_id, author_id) VALUES
(1, 1),
(2, 1),
(3, 4),
(4, 5),
(5, 3),
(6, 2),
(7, 24),
(8, 21),
(9, 22),
(10, 23),
(11, 6),
(12, 8),
(12, 9),
(12, 10),
(12, 11),
(13, 7),
(14, 30),
(15, 6),
(16, 17),
(17, 18),
(18, 19),
(19, 20),
(20, 12),
(21, 13),
(22, 14),
(23, 12),
(24, 1),
(25, 29),
(26, 29),
(27, 29),
(28, 25),
(28, 26),
(29, 27),
(30, 28),
(31, 4),
(32, 15),
(33, 16),
(34, 16),
(35, 6);

-- 13. BẢNG GIÁ HIỆN HÀNH & LỊCH SỬ ĐIỀU CHỈNH GIÁ
INSERT INTO price (book_id, base_price, profit_rate, selling_price, effective_date, end_date, is_active) VALUES
(1, 52000, 35.00, 85000, '2024-01-01 00:00:00', '2025-09-30 23:59:59', 0),
(11, 120000, 35.00, 200000, '2024-01-01 00:00:00', '2025-09-30 23:59:59', 0),
(20, 50000, 35.00, 85000, '2024-01-01 00:00:00', '2025-09-30 23:59:59', 0),
(32, 95000, 35.00, 155000, '2024-01-01 00:00:00', '2025-09-30 23:59:59', 0),
(1, 57000, 66.67, 95000, '2025-10-01 00:00:00', NULL, 1),
(2, 66000, 66.67, 110000, '2025-10-01 00:00:00', NULL, 1),
(3, 51000, 66.67, 85000, '2025-10-01 00:00:00', NULL, 1),
(4, 36000, 66.67, 60000, '2025-10-01 00:00:00', NULL, 1),
(5, 45000, 66.67, 75000, '2025-10-01 00:00:00', NULL, 1),
(6, 39000, 66.67, 65000, '2025-10-01 00:00:00', NULL, 1),
(7, 87000, 66.67, 145000, '2025-10-01 00:00:00', NULL, 1),
(8, 42000, 66.67, 70000, '2025-10-01 00:00:00', NULL, 1),
(9, 93000, 66.67, 155000, '2025-10-01 00:00:00', NULL, 1),
(10, 108000, 66.67, 180000, '2025-10-01 00:00:00', NULL, 1),
(11, 138000, 66.67, 230000, '2025-10-01 00:00:00', NULL, 1),
(12, 168000, 66.67, 280000, '2025-10-01 00:00:00', NULL, 1),
(13, 150000, 66.67, 250000, '2025-10-01 00:00:00', NULL, 1),
(14, 210000, 66.67, 350000, '2025-10-01 00:00:00', NULL, 1),
(15, 96000, 66.67, 160000, '2025-10-01 00:00:00', NULL, 1),
(16, 21000, 66.67, 35000, '2025-10-01 00:00:00', NULL, 1),
(17, 27000, 66.67, 45000, '2025-10-01 00:00:00', NULL, 1),
(18, 21000, 66.67, 35000, '2025-10-01 00:00:00', NULL, 1),
(19, 21000, 66.67, 35000, '2025-10-01 00:00:00', NULL, 1),
(20, 57000, 66.67, 95000, '2025-10-01 00:00:00', NULL, 1),
(21, 51000, 66.67, 85000, '2025-10-01 00:00:00', NULL, 1),
(22, 99000, 66.67, 165000, '2025-10-01 00:00:00', NULL, 1),
(23, 54000, 66.67, 90000, '2025-10-01 00:00:00', NULL, 1),
(24, 54000, 66.67, 90000, '2025-10-01 00:00:00', NULL, 1),
(25, 117000, 66.67, 195000, '2025-10-01 00:00:00', NULL, 1),
(26, 126000, 66.67, 210000, '2025-10-01 00:00:00', NULL, 1),
(27, 72000, 66.67, 120000, '2025-10-01 00:00:00', NULL, 1),
(28, 75000, 66.67, 125000, '2025-10-01 00:00:00', NULL, 1),
(29, 84000, 66.67, 140000, '2025-10-01 00:00:00', NULL, 1),
(30, 63000, 66.67, 105000, '2025-10-01 00:00:00', NULL, 1),
(31, 81000, 66.67, 135000, '2025-10-01 00:00:00', NULL, 1),
(32, 105000, 66.67, 175000, '2025-10-01 00:00:00', NULL, 1),
(33, 69000, 66.67, 115000, '2025-10-01 00:00:00', NULL, 1),
(34, 156000, 66.67, 260000, '2025-10-01 00:00:00', NULL, 1);

-- 14. CHƯƠNG TRÌNH KHUYẾN MÃI
INSERT INTO promotion (promotion_id, promotion_name, percent, start_date, end_date, status) VALUES
(1, 'Mừng Năm Mới 2026 - Giảm 10% Sách Văn Học & Kỹ Năng Sống', 10.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
(2, 'Tháng Công Nghệ 2026 - Giảm 15% Sách IT', 15.00, '2026-06-01 00:00:00', '2026-12-31 23:59:59', 1),
(3, 'Flash Sale Manga Hè 2025', 25.00, '2025-06-01 00:00:00', '2025-08-31 23:59:59', 0),
(4, 'Đại Tiệc Tri Thức 2027', 20.00, '2027-01-01 00:00:00', '2027-01-15 23:59:59', 0);

INSERT INTO promotion_detail (promotion_id, book_id) VALUES
(1, 1),
(1, 2),
(1, 3),
(1, 4),
(1, 5),
(1, 6),
(1, 7),
(1, 8),
(1, 9),
(1, 10),
(1, 20),
(1, 21),
(1, 22),
(1, 23),
(1, 24),
(2, 11),
(2, 12),
(2, 13),
(2, 14),
(2, 15),
(3, 16),
(3, 17),
(3, 18),
(3, 19);

-- 15. KHÁCH HÀNG
INSERT INTO customer (customer_id, customer_name, customer_phone, point, rank_id) VALUES
(1, 'Nguyễn Ý Vy', '0909901421', 3200, 2),
(2, 'Phan Tòng Nhân', '0909901422', 450, 1),
(3, 'Nguyễn Thị Hồng Anh', '0909909233', 8800, 3),
(4, 'Lê Ngọc Quý', '0934129959', 22500, 4),
(5, 'Trần Văn Hoàng', '0987654321', 1100, 1),
(6, 'Đặng Mai Phương', '0918273645', 7200, 3),
(7, 'Võ Minh Khôi', '0933445566', 0, 1);

-- 16. PHIẾU NHẬP HÀNG & CHI TIẾT PHIẾU NHẬP
INSERT INTO import_ticket (import_ticket_id, created_date, total_import_quantity, total_import_price, status, employee_id, supplier_id, approver_id, approved_date) VALUES
(1, '2025-10-15 09:30:00', 200, 10650000, 2, 4, 1, 2, '2025-10-15 10:15:00'),
(2, '2025-12-05 14:00:00', 150, 24390000, 2, 4, 6, 2, '2025-12-05 15:30:00'),
(3, '2026-01-10 10:00:00', 400, 8880000, 2, 4, 2, 2, '2026-01-10 11:20:00'),
(4, '2026-02-15 08:45:00', 360, 22650000, 2, 4, 4, 2, '2026-02-15 09:30:00'),
(5, '2026-04-20 13:15:00', 300, 23730000, 2, 4, 3, 2, '2026-04-20 14:00:00'),
(6, '2026-06-18 09:00:00', 285, 25665000, 2, 4, 7, 2, '2026-06-18 10:00:00'),
(7, '2026-08-10 11:30:00', 285, 23700000, 2, 4, 5, 2, '2026-08-10 13:45:00'),
(8, '2026-09-05 15:20:00', 170, 11730000, 2, 4, 1, 2, '2026-09-05 16:10:00'),
(9, '2026-09-25 10:15:00', 80, 1860000, 1, 4, 2, NULL, NULL),
(10, '2026-09-26 08:30:00', 40, 2880000, 0, 4, 3, 2, '2026-09-26 08:50:00');

INSERT INTO import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price, cover_price, discount_percent) VALUES
(1, 1, 50, 57000, 95000, 40.00),
(1, 2, 60, 66000, 110000, 40.00),
(1, 3, 40, 51000, 85000, 40.00),
(1, 4, 50, 36000, 60000, 40.00),
(2, 11, 45, 138000, 230000, 40.00),
(2, 12, 35, 168000, 280000, 40.00),
(2, 13, 40, 150000, 250000, 40.00),
(2, 14, 30, 210000, 350000, 40.00),
(3, 16, 120, 21000, 35000, 40.00),
(3, 17, 80, 27000, 45000, 40.00),
(3, 18, 110, 21000, 35000, 40.00),
(3, 19, 90, 21000, 35000, 40.00),
(4, 20, 100, 57000, 95000, 40.00),
(4, 21, 80, 51000, 85000, 40.00),
(4, 22, 70, 99000, 165000, 40.00),
(4, 23, 50, 54000, 90000, 40.00),
(4, 24, 60, 54000, 90000, 40.00),
(5, 7, 45, 87000, 145000, 40.00),
(5, 8, 60, 42000, 70000, 40.00),
(5, 9, 50, 93000, 155000, 40.00),
(5, 10, 40, 108000, 180000, 40.00),
(5, 28, 55, 75000, 125000, 40.00),
(5, 29, 50, 84000, 140000, 40.00),
(6, 30, 60, 63000, 105000, 40.00),
(6, 31, 45, 81000, 135000, 40.00),
(6, 32, 65, 105000, 175000, 40.00),
(6, 33, 75, 69000, 115000, 40.00),
(6, 34, 40, 156000, 260000, 40.00),
(7, 5, 50, 45000, 75000, 40.00),
(7, 6, 40, 39000, 65000, 40.00),
(7, 15, 60, 96000, 160000, 40.00),
(7, 25, 50, 117000, 195000, 40.00),
(7, 26, 40, 126000, 210000, 40.00),
(7, 27, 45, 72000, 120000, 40.00),
(8, 1, 40, 57000, 95000, 40.00),
(8, 2, 40, 66000, 110000, 40.00),
(8, 20, 50, 57000, 95000, 40.00),
(8, 22, 40, 99000, 165000, 40.00),
(9, 16, 50, 21000, 35000, 40.00),
(9, 17, 30, 27000, 45000, 40.00),
(10, 9, 20, 93000, 155000, 40.00),
(10, 21, 20, 51000, 85000, 40.00);

-- 17. LÔ SÁCH (BOOK LOTS TỪ CÁC PHIẾU NHẬP ĐÃ DUYỆT)
INSERT INTO book_lot (lot_id, book_id, import_ticket_id, import_date, cover_price, discount_percent, import_price, selling_price, quantity_initial, quantity_remain) VALUES
(1, 1, 1, '2025-10-15 10:15:00', 95000, 40.00, 57000, 95000, 50, 45),
(2, 2, 1, '2025-10-15 10:15:00', 110000, 40.00, 66000, 110000, 60, 57),
(3, 3, 1, '2025-10-15 10:15:00', 85000, 40.00, 51000, 85000, 40, 38),
(4, 4, 1, '2025-10-15 10:15:00', 60000, 40.00, 36000, 60000, 50, 49),
(5, 11, 2, '2025-12-05 15:30:00', 230000, 40.00, 138000, 230000, 45, 43),
(6, 12, 2, '2025-12-05 15:30:00', 280000, 40.00, 168000, 280000, 35, 34),
(7, 13, 2, '2025-12-05 15:30:00', 250000, 40.00, 150000, 250000, 40, 38),
(8, 14, 2, '2025-12-05 15:30:00', 350000, 40.00, 210000, 350000, 30, 29),
(9, 16, 3, '2026-01-10 11:20:00', 35000, 40.00, 21000, 35000, 120, 105),
(10, 17, 3, '2026-01-10 11:20:00', 45000, 40.00, 27000, 45000, 80, 72),
(11, 18, 3, '2026-01-10 11:20:00', 35000, 40.00, 21000, 35000, 110, 101),
(12, 19, 3, '2026-01-10 11:20:00', 35000, 40.00, 21000, 35000, 90, 86),
(13, 20, 4, '2026-02-15 09:30:00', 95000, 40.00, 57000, 95000, 100, 96),
(14, 21, 4, '2026-02-15 09:30:00', 85000, 40.00, 51000, 85000, 80, 77),
(15, 22, 4, '2026-02-15 09:30:00', 165000, 40.00, 99000, 165000, 70, 68),
(16, 23, 4, '2026-02-15 09:30:00', 90000, 40.00, 54000, 90000, 50, 50),
(17, 24, 4, '2026-02-15 09:30:00', 90000, 40.00, 54000, 90000, 60, 58),
(18, 7, 5, '2026-04-20 14:00:00', 145000, 40.00, 87000, 145000, 45, 44),
(19, 8, 5, '2026-04-20 14:00:00', 70000, 40.00, 42000, 70000, 60, 58),
(20, 9, 5, '2026-04-20 14:00:00', 155000, 40.00, 93000, 155000, 50, 48),
(21, 10, 5, '2026-04-20 14:00:00', 180000, 40.00, 108000, 180000, 40, 40),
(22, 28, 5, '2026-04-20 14:00:00', 125000, 40.00, 75000, 125000, 55, 54),
(23, 29, 5, '2026-04-20 14:00:00', 140000, 40.00, 84000, 140000, 50, 50),
(24, 30, 6, '2026-06-18 10:00:00', 105000, 40.00, 63000, 105000, 60, 60),
(25, 31, 6, '2026-06-18 10:00:00', 135000, 40.00, 81000, 135000, 45, 45),
(26, 32, 6, '2026-06-18 10:00:00', 175000, 40.00, 105000, 175000, 65, 63),
(27, 33, 6, '2026-06-18 10:00:00', 115000, 40.00, 69000, 115000, 75, 72),
(28, 34, 6, '2026-06-18 10:00:00', 260000, 40.00, 156000, 260000, 40, 39),
(29, 5, 7, '2026-08-10 13:45:00', 75000, 40.00, 45000, 75000, 50, 48),
(30, 6, 7, '2026-08-10 13:45:00', 65000, 40.00, 39000, 65000, 40, 38),
(31, 15, 7, '2026-08-10 13:45:00', 160000, 40.00, 96000, 160000, 60, 59),
(32, 25, 7, '2026-08-10 13:45:00', 195000, 40.00, 117000, 195000, 50, 49),
(33, 26, 7, '2026-08-10 13:45:00', 210000, 40.00, 126000, 210000, 40, 40),
(34, 27, 7, '2026-08-10 13:45:00', 120000, 40.00, 72000, 120000, 45, 45),
(35, 1, 8, '2026-09-05 16:10:00', 95000, 40.00, 57000, 95000, 40, 40),
(36, 2, 8, '2026-09-05 16:10:00', 110000, 40.00, 66000, 110000, 40, 40),
(37, 20, 8, '2026-09-05 16:10:00', 95000, 40.00, 57000, 95000, 50, 50),
(38, 22, 8, '2026-09-05 16:10:00', 165000, 40.00, 99000, 165000, 40, 40);

-- 18. HÓA ĐƠN BÁN HÀNG & CHI TIẾT HÓA ĐƠN
INSERT INTO bill (bill_id, created_date, total_bill_price, tax, employee_id, customer_id, payment_method_id, earned_points) VALUES
(1, '2025-11-05 14:20:00', 256500, 0.08, 3, 1, 1, 2500),
(2, '2025-11-20 18:40:00', 302400, 0.08, 5, NULL, 1, 0),
(3, '2025-12-15 10:15:00', 719280, 0.08, 3, 3, 2, 7100),
(4, '2025-12-28 16:30:00', 780300, 0.08, 5, 4, 3, 7800),
(5, '2026-01-15 11:00:00', 334800, 0.08, 3, 2, 1, 3300),
(6, '2026-02-02 15:45:00', 302400, 0.08, 5, NULL, 2, 0),
(7, '2026-02-25 17:10:00', 369360, 0.08, 3, 1, 2, 3600),
(8, '2026-03-12 14:00:00', 372600, 0.08, 5, 5, 1, 3700),
(9, '2026-04-25 09:30:00', 277020, 0.08, 3, 6, 3, 2700),
(10, '2026-05-18 19:20:00', 422820, 0.08, 5, 3, 1, 4200),
(11, '2026-06-22 16:15:00', 876690, 0.08, 3, 4, 2, 8700),
(12, '2026-07-14 10:45:00', 810000, 0.08, 5, NULL, 1, 0),
(13, '2026-08-18 15:30:00', 364230, 0.08, 3, 1, 3, 3600),
(14, '2026-09-10 11:15:00', 461700, 0.08, 5, 6, 2, 4600),
(15, '2026-09-18 17:50:00', 374220, 0.08, 3, 3, 1, 3700),
(16, '2026-09-24 14:35:00', 394200, 0.08, 5, 5, 2, 3900);

INSERT INTO bill_detail (bill_id, book_id, quantity, unit_price, lot_id) VALUES
(1, 1, 2, 95000, 1),
(1, 4, 1, 60000, 4),
(2, 2, 1, 110000, 2),
(2, 3, 2, 85000, 3),
(3, 11, 2, 230000, 5),
(3, 12, 1, 280000, 6),
(4, 13, 2, 250000, 7),
(4, 14, 1, 350000, 8),
(5, 16, 5, 35000, 9),
(5, 17, 3, 45000, 10),
(6, 18, 4, 35000, 11),
(6, 19, 4, 35000, 12),
(7, 20, 2, 95000, 13),
(7, 21, 2, 85000, 14),
(8, 22, 1, 165000, 15),
(8, 24, 2, 90000, 17),
(9, 7, 1, 145000, 18),
(9, 8, 2, 70000, 19),
(10, 9, 2, 155000, 20),
(10, 28, 1, 125000, 22),
(11, 32, 2, 175000, 26),
(11, 33, 3, 115000, 27),
(11, 34, 1, 260000, 28),
(12, 16, 10, 35000, 9),
(12, 17, 5, 45000, 10),
(12, 18, 5, 35000, 11),
(13, 15, 1, 160000, 31),
(13, 25, 1, 195000, 32),
(14, 1, 3, 95000, 1),
(14, 20, 2, 95000, 13),
(15, 2, 2, 110000, 2),
(15, 22, 1, 165000, 15),
(16, 5, 2, 75000, 29),
(16, 6, 2, 65000, 30),
(16, 21, 1, 85000, 14);

-- 19. NHẬT KÝ BIẾN ĐỘNG KHO (INVENTORY LOGS)
INSERT INTO inventory_log (action, change_quantity, remain_quantity, reference_id, created_date, book_id) VALUES
('Nhập hàng', 50, 50, 1, '2025-10-15 10:15:00', 1),
('Nhập hàng', 60, 60, 1, '2025-10-15 10:15:00', 2),
('Nhập hàng', 40, 40, 1, '2025-10-15 10:15:00', 3),
('Nhập hàng', 50, 50, 1, '2025-10-15 10:15:00', 4),
('Bán hàng', -2, 88, 1, '2025-11-05 14:20:00', 1),
('Bán hàng', -1, 49, 1, '2025-11-05 14:20:00', 4),
('Bán hàng', -1, 99, 2, '2025-11-20 18:40:00', 2),
('Bán hàng', -2, 38, 2, '2025-11-20 18:40:00', 3),
('Nhập hàng', 45, 45, 2, '2025-12-05 15:30:00', 11),
('Nhập hàng', 35, 35, 2, '2025-12-05 15:30:00', 12),
('Nhập hàng', 40, 40, 2, '2025-12-05 15:30:00', 13),
('Nhập hàng', 30, 30, 2, '2025-12-05 15:30:00', 14),
('Bán hàng', -2, 43, 3, '2025-12-15 10:15:00', 11),
('Bán hàng', -1, 34, 3, '2025-12-15 10:15:00', 12),
('Bán hàng', -2, 38, 4, '2025-12-28 16:30:00', 13),
('Bán hàng', -1, 29, 4, '2025-12-28 16:30:00', 14),
('Nhập hàng', 120, 120, 3, '2026-01-10 11:20:00', 16),
('Nhập hàng', 80, 80, 3, '2026-01-10 11:20:00', 17),
('Nhập hàng', 110, 110, 3, '2026-01-10 11:20:00', 18),
('Nhập hàng', 90, 90, 3, '2026-01-10 11:20:00', 19),
('Bán hàng', -5, 115, 5, '2026-01-15 11:00:00', 16),
('Bán hàng', -3, 77, 5, '2026-01-15 11:00:00', 17),
('Bán hàng', -4, 106, 6, '2026-02-02 15:45:00', 18),
('Bán hàng', -4, 86, 6, '2026-02-02 15:45:00', 19),
('Nhập hàng', 100, 100, 4, '2026-02-15 09:30:00', 20),
('Nhập hàng', 80, 80, 4, '2026-02-15 09:30:00', 21),
('Nhập hàng', 70, 70, 4, '2026-02-15 09:30:00', 22),
('Nhập hàng', 50, 50, 4, '2026-02-15 09:30:00', 23),
('Nhập hàng', 60, 60, 4, '2026-02-15 09:30:00', 24),
('Bán hàng', -2, 148, 7, '2026-02-25 17:10:00', 20),
('Bán hàng', -2, 78, 7, '2026-02-25 17:10:00', 21),
('Bán hàng', -1, 109, 8, '2026-03-12 14:00:00', 22),
('Bán hàng', -2, 58, 8, '2026-03-12 14:00:00', 24),
('Nhập hàng', 45, 45, 5, '2026-04-20 14:00:00', 7),
('Nhập hàng', 60, 60, 5, '2026-04-20 14:00:00', 8),
('Nhập hàng', 50, 50, 5, '2026-04-20 14:00:00', 9),
('Nhập hàng', 40, 40, 5, '2026-04-20 14:00:00', 10),
('Nhập hàng', 55, 55, 5, '2026-04-20 14:00:00', 28),
('Nhập hàng', 50, 50, 5, '2026-04-20 14:00:00', 29),
('Bán hàng', -1, 44, 9, '2026-04-25 09:30:00', 7),
('Bán hàng', -2, 58, 9, '2026-04-25 09:30:00', 8),
('Bán hàng', -2, 48, 10, '2026-05-18 19:20:00', 9),
('Bán hàng', -1, 54, 10, '2026-05-18 19:20:00', 28),
('Nhập hàng', 60, 60, 6, '2026-06-18 10:00:00', 30),
('Nhập hàng', 45, 45, 6, '2026-06-18 10:00:00', 31),
('Nhập hàng', 65, 65, 6, '2026-06-18 10:00:00', 32),
('Nhập hàng', 75, 75, 6, '2026-06-18 10:00:00', 33),
('Nhập hàng', 40, 40, 6, '2026-06-18 10:00:00', 34),
('Bán hàng', -2, 63, 11, '2026-06-22 16:15:00', 32),
('Bán hàng', -3, 72, 11, '2026-06-22 16:15:00', 33),
('Bán hàng', -1, 39, 11, '2026-06-22 16:15:00', 34),
('Bán hàng', -10, 105, 12, '2026-07-14 10:45:00', 16),
('Bán hàng', -5, 72, 12, '2026-07-14 10:45:00', 17),
('Bán hàng', -5, 101, 12, '2026-07-14 10:45:00', 18),
('Nhập hàng', 50, 50, 7, '2026-08-10 13:45:00', 5),
('Nhập hàng', 40, 40, 7, '2026-08-10 13:45:00', 6),
('Nhập hàng', 60, 60, 7, '2026-08-10 13:45:00', 15),
('Nhập hàng', 50, 50, 7, '2026-08-10 13:45:00', 25),
('Nhập hàng', 40, 40, 7, '2026-08-10 13:45:00', 26),
('Nhập hàng', 45, 45, 7, '2026-08-10 13:45:00', 27),
('Bán hàng', -1, 59, 13, '2026-08-18 15:30:00', 15),
('Bán hàng', -1, 49, 13, '2026-08-18 15:30:00', 25),
('Nhập hàng', 40, 90, 8, '2026-09-05 16:10:00', 1),
('Nhập hàng', 40, 100, 8, '2026-09-05 16:10:00', 2),
('Nhập hàng', 50, 150, 8, '2026-09-05 16:10:00', 20),
('Nhập hàng', 40, 110, 8, '2026-09-05 16:10:00', 22),
('Bán hàng', -3, 85, 14, '2026-09-10 11:15:00', 1),
('Bán hàng', -2, 146, 14, '2026-09-10 11:15:00', 20),
('Bán hàng', -2, 97, 15, '2026-09-18 17:50:00', 2),
('Bán hàng', -1, 108, 15, '2026-09-18 17:50:00', 22),
('Bán hàng', -2, 48, 16, '2026-09-24 14:35:00', 5),
('Bán hàng', -2, 38, 16, '2026-09-24 14:35:00', 6),
('Bán hàng', -1, 77, 16, '2026-09-24 14:35:00', 21);
