-- FILE 2/2: Dữ liệu nền và dữ liệu mẫu để test.
-- Chỉ chạy sau khi bookstore_schema.sql đã chạy thành công.

use bookstore_db;

insert into role (role_name) values
('Admin'),
('Quản lý'),
('Nhân viên Bán hàng'),
('Nhân viên Nhập hàng');

insert into action (action_id, action_code, action_name) values
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

insert into permission(role_id, action_id, is_view, is_action) values
(1, 15, 1, 1),
(1, 16, 1, 1);

insert into permission(role_id, action_id, is_view, is_action) values
(2, 1, 1, 1), (2, 2, 1, 1), (2, 3, 1, 1), (2, 4, 1, 1),
(2, 5, 1, 1), (2, 6, 1, 1), (2, 7, 1, 1), (2, 8, 1, 1),
(2, 9, 1, 1), (2, 10, 1, 1), (2, 11, 1, 1), (2, 12, 1, 1),
(2, 13, 1, 1), (2, 14, 1, 1);

insert into permission(role_id, action_id, is_view, is_action) values
(3, 1, 1, 1),
(3, 12, 1, 1),
(3, 2, 1, 0),
(3, 3, 1, 0);

insert into permission(role_id, action_id, is_view, is_action) values
(4, 9, 1, 1),
(4, 10, 1, 1),
(4, 11, 1, 0),
(4, 3, 1, 1),
(4, 4, 1, 1),
(4, 5, 1, 1),
(4, 6, 1, 1);

insert into payment_method(payment_method_name) values
('Tiền mặt'),
('Chuyển khoản ngân hàng'),
('Thẻ tín dụng/Ghi nợ');

insert into system_parameter(param_key, param_value, description) values
('VAT', '8', 'Thuế giá trị gia tăng mặc định (8%)'),
('EARNED_POINTS_PER_10K', '100', 'Số điểm nhận được trên mỗi 10K mua');

insert into employee (employee_id, employee_name, employee_phone, birthday, base_salary, day_in, role_id) values
(1, 'Quý Nhân Vy Ngọc San - Admin', '0912345678', '2000-01-01', 0, '2020-01-01', 1);

insert into account (username, password, employee_id) values
('admin', 'admin', 1);

insert into employee (employee_name, employee_phone, birthday, base_salary, day_in, role_id) values
('Nguyễn Thị Hồng Anh', '0914349584', '2000-04-15', 12000000, '2025-11-23', 2),
('Ngọc Quý', '0934129959', '2004-06-18', 8000000, '2025-12-11', 3),
('Tòng Nhân', '0912357394', '2002-04-11', 7500000, '2025-02-17', 4),
('Ngọc', '0998929485', '2000-08-12', 8000000, '2025-5-15', 3);

insert into account (username, password, employee_id) values
('0914349584', '15042000', 2),
('0934129959', '18062004', 3),
('0912357394', '11042002', 4),
('0998929485', '12082000', 5);

insert into supplier (supplier_name, supplier_address, supplier_phone) values
    ('NXB Trẻ', '161B Lý Chính Thắng, Phường Xuân Hoà , TP. Hồ Chí Minh', '0842839316289'),
    ('NXB Văn Học', '18 Nguyễn Trường Tộ, Ba Đình, Hà Nội', '0842437161518'),
    ('NXB Hội Nhà Văn', 'Số 65 Nguyễn Du, Phường Hai Bà Trưng, Hà Nội', '02438222135'),
    ('NXB Dân Trí', 'Số 9, ngõ 26, phố Hoàng Cầu, phường Ô Chợ Dừa, quận Đống Đa, Hà Nội', '02466860751'),
    ('NXB Thanh Niên', 'Toà nhà Ô D29 Phạm Văn Bạch, Yên Hoà, Cầu Giấy, Hà Nội, Việt Nam', '04982526569'),
    ('NXB Công Thương', 'Tòa nhà Bộ Công Thương, 655 Đ. Phạm Văn Đồng, phường Bắc Từ Liêm, Hà Nội', '02439341562'),
    ('Amazon Shop', 'Settle, Washington D.C', '18882804331'),
    ('NXB Tri Thức', 'Tòa nhà Liên hiệp các Hội Khoa học và Kỹ thuật Việt Nam - Lô D20, ngõ 19 Duy Tân, Phường Cầu Giấy, TP. Hà Nội', '02466878415'),
    ('NXB Tổng Hợp TPHCM', '62 Nguyễn Thị Minh Khai, Phường Sài Gòn, TP.HCM', '02838256804'),
    ('NXB Thế Giới', '59 Thợ Nhuộm, Hoàn Kiếm, Hà Nội', '02438253841'),
    ('NXB Lao Động', 'Tầng 12, Số 175 Đường Giảng Võ, Phường Ô Chợ Dừa, Thành phố Hà Nội', '0438515380'),
    ('NXB Kim Đồng', 'Số 55 Quang Trung, Phường Hai Bà Trưng, Thành phố Hà Nội', '01900571595'),
    ('NXB Quân Đội Nhân Dân', '23 Lý Nam Đế, Phường Hoàn Kiếm, Hà Nội', '02438455766'),
    ('NXB Penguin Putnam Inc', '20 Vauxhall Bridge Rd, London, United Kingdom', '18885352334'),
    ('NXB Phụ Nữ', '39 Hàng Chuối, Q. Hai Bà Trưng, Hà Nội', '02439710717'),
    ('NXB Chính Trị Quốc Gia Sự Thật', '6/86 Duy Tân, Cầu Giấy, Hà Nội', '02438221581');

insert into category (category_name) values
    ('Văn học - Nghệ thuật'),
    ('Công nghệ'),
    ('Truyện tranh'),
    ('Kỹ năng sống'),
    ('Giáo dục - Học tập'),
    ('Tâm lý - Khoa học xã hội');

insert into author (author_name, nationality) values
    ('Nguyễn Nhật Ánh', 'Việt Nam'),
    ('Đoàn Minh Phương', 'Việt Nam'),
    ('Alice Munro', 'Canada'),
    ('Lewis Carrol', 'Anh'),
    ('J.R.R. Tolkien', 'Anh'),
    ('Christopher Paolini', 'Mỹ'),
    ('Sir Arthur Conan Doyle', 'Scotland'),
    ('Changwon Pyo ', 'Hàn'),
    ('Mục Qua', 'Việt Nam'),
    ('Nguyên Anh', 'Việt Nam'),
    ('Thương Thái Vi', 'Trung Quốc'),
    ('Di Li', 'Việt Nam'),
    ('Nguyễn Anh Dũng', 'Việt Nam'),
    ('Haruki Murakami', 'Nhật Bản'),
    ('Eran Katz', 'Israel'),
    ('Rosie Nguyễn', 'Việt Nam'),
    ('Paulo Coelho', 'Brazil'),
    ('Dale Carnegie', 'Mỹ'),
    ('Nhiều Tác Giả', ''),
    ('Cao Minh', 'Trung Quốc'),
    ('TS David J. Lieberman', 'Mỹ'),
    ('Diệp Hồng Vũ', 'Trung Quốc'),
    ('Peter Swanson', 'Mỹ'),
    ('Antoine de Saint-Exupéry', 'Pháp'),
    ('Fujiko F Fujio', 'Nhật Bản'),
    ('Tony Buổi Sáng', 'Việt Nam'),
    ('Harper Lee', 'Mỹ'),
    ('Ernest Hemingway', 'Mỹ'),
    ('Victor Hugo', 'Pháp'),
    ('J.K Rowling', 'Anh'),
    ('Margaret Mitchell', 'Mỹ'),
    ('Emily Brontë', 'Anh'),
    ('Andrew Matthews', 'Úc'),
    ('Don Gabor', 'Mỹ'),
    ('Quất Tử Bất Toan', 'Trung Quốc'),
    ('Gosho Aoyama', 'Nhật Bản'),
    ('Lighthouse Writers', 'Việt Nam'),
    ('Dr Lin Lougheed', 'Mỹ'),
    ('Nguyễn Ngọc Anh', 'Việt Nam'),
    ('Lê Bảo Ngọc', 'Việt Nam'),
    ('Baird T.Spalding', 'Mỹ'),
    ('Cảnh Thiên', 'Trung Quốc'),
    ('Shannon Thomas', 'Mỹ'),
    ('Eiichiro Oda', 'Nhật Bản'),
    ('Alexandre Dumas', 'Pháp'),
    ('Akira Toriyama', 'Nhật Bản'),
    ('Chu Lai', 'Việt Nam'),
    ('Miguel de Cervantes Saavedra', 'Tây Ban Nha'),
    ('Nam Cao', 'Việt Nam'),
    ('Vũ Trọng Phụng', 'Việt Nam'),
    ('Ngô Tất Tố', 'Việt Nam'),
    ('Kim Lân', 'Việt Nam'),
    ('Nguyên Hồng', 'Việt Nam'),
    ('Nguyễn Ngọc Tư', 'Việt Nam'),
    ('Đoàn Giỏi', 'Việt Nam'),
    ('Gustave Le Bon', 'Pháp'),
    ('Tô Hoài', 'Việt Nam'),
    ('José Mauro de Vasconcelos', 'Brazil'),
    ('Colleen McCullough', 'Úc'),
    ('Từ Khắc Thành', 'Trung Quốc'),
    ('Thomas H. Cormen', 'Mỹ'),
    ('Charles E. Leiserson', 'Na Uy'),
    ('Ronald L. Rivest', 'Mỹ'),
    ('Clifford Stein', 'Mỹ'),
    ('Erich Gamma', 'Thụy Sĩ'),
    ('Richard Helm', 'Úc'),
    ('Ralph Johnson', 'Mỹ'),
    ('John Vlissides', 'Mỹ'),
    ('Kent Beck', 'Mỹ'),
    ('Peter Norvig', 'Mỹ'),
    ('Stuart Russel', 'Anh'),
    ('Martin Fowler', 'Anh'),
    ('John Brant', 'Mỹ'),
    ('William Opdyke', 'Mỹ'),
    ('Don Roberts', 'Mỹ'),
    ('Pramod Sadalage', 'Mỹ'),
    ('Scott W. Ambler', 'Canada'),
    ('Terry Pratchett', 'Anh'),
    ('Neil Gaiman', 'Anh');

insert into book (book_name, publication_year, selling_price, quantity, translator, image, description, status, category_id, supplier_id, tag_detail) values
('Bàn Có Năm Chỗ Ngồi', 2022, 85000, 0, null, "bàn_có_năm_chỗ_ngồi.jpg", 'Bàn có năm chỗ ngồi xoay quanh câu chuyện tình bạn giữa 5 người bạn...', 1, 1, 1, 'Truyện dài, Tình bạn, Thiếu nhi, Nguyễn Nhật Ánh'),
('Thằng Quỷ Nhỏ', 2023, 75000, 0, null, "thằng_quỷ_nhỏ.jpg", 'Chuông reo, cả lớp xếp hàng...', 1, 1, 1, 'Truyện dài, Tình bạn, Thiếu nhi, Nguyễn Nhật Ánh'),
('Út Quyên Và Tôi', 2021, 65000, 0, null, "út_quyên_và_tôi.jpg", 'Nếu chẳng may được sinh ra trên cõi đời này...', 1, 1, 1, 'Truyện ngắn, Gia đình, Nguyễn Nhật Ánh'),
('Và Khi Tro Bụi', 2020, 95000, 0, null, "và_khi_tro_bụi.jpg", 'Người đàn bà đi tìm cái chết bằng một cách rất lạ...', 1, 1, 1, 'Truyện ngắn, Bi kịch'),
('Ghét,Thân,Thương,Yêu,Cưới', 2019, 120000, 0, 'Trần Hạnh, Đặng Xuân Thảo, Hạnh Mai', "ghét,_thân,_thương,_yêu,_cưới.jpg", 'Mỗi khi nhấc một tập truyện mới của Alice Munro lên...', 1, 1, 2, 'Truyện ngắn, Tình cảm'),
('Alice ở xứ sở diệu kì - Alice ở xứ sở trong gương', 2018, 180000, 0, 'Lê Thị Oanh', "alice_ở_xứ_sở_diệu_kì_và_alice_ở_xứ_sở_trong_gương.jpg", 'Alice ở Xứ Sở thần tiên là cuốn tiểu thuyết thiếu nhi nổi tiếng...', 1, 1, 2, 'Tiểu thuyết, Phiêu lưu, Kì ảo, Kinh điển'),
('Anh Chàng Hobbit', 2020, 145000, 0, 'Nguyễn Tâm', "anh_chàng_hobbit.jpg", '"Cộng đồng người Anh ngữ được phân làm hai:..."', 1, 1, 3, 'Tiểu thuyết, Phiêu lưu, Giả tưởng'),
('Eragon - Cậu bé cưỡi rồng 1', 2021, 160000, 0, 'Đặng Phi Bằng', "eragon_cậu_bé_cưỡi_rồng_1.jpg", 'Tình cờ trong một lần đi săn, Eragon nhặt được một viên đá màu xanh...', 1, 1, 6, 'Tiểu thuyết, Phiêu lưu, Giả tưởng'),
('Eragon - Cậu bé cưỡi rồng 2', 2021, 160000, 0, 'Đặng Phi Bằng', "eragon_cậu_bé_cưỡi_rồng_2.jpg", 'Tình cờ trong một lần đi săn, Eragon nhặt được một viên đá màu xanh...', 1, 1, 6, 'Tiểu thuyết, Phiêu lưu, Giả tưởng'),
('Eldest - Đại Ca 1', 2022, 170000, 0, 'Đặng Phi Bằng', "eldest_đại_ca_1.jpg", 'Vừa hoàn tất xong nhiệm vụ...', 1, 1, 6, 'Tiểu thuyết, Phiêu lưu, Giả tưởng'),
('Eldest - Đại Ca 2', 2022, 170000, 0, 'Đặng Phi Bằng', "eldest_đại_ca_2.jpg", 'Vừa hoàn tất xong nhiệm vụ...', 1, 1, 6, 'Tiểu thuyết, Phiêu lưu, Giả tưởng'),
('Brisingr - Hỏa Kiếm 1', 2023, 175000, 0, 'Đặng Phi Bằng', "brisingr_hỏa_kiếm_1.jpg", 'Hai Rázac xuất hiện từ một đường hầm...', 1, 1, 6, 'Tiểu thuyết, Phiêu lưu, Giả tưởng'),
('Brisingr - Hỏa Kiếm 2', 2023, 175000, 0, 'Đặng Phi Bằng', "brisingr_hỏa_kiếm_2.jpg", 'Hai Rázac xuất hiện từ một đường hầm...', 1, 1, 6, 'Tiểu thuyết, Phiêu lưu, Giả tưởng'),
('Sherlock Holmes - Toàn tập', 2019, 250000, 0, 'Lê Khánh, Đỗ Tư Nghĩa, Vương Thảo...', "sherlock_holmes_toàn_tập.jpg", '"Holmes is a mesmerizing creation..."', 1, 1, 2, 'Trinh thám, Toàn tập, Phiêu lưu, Kinh điển'),
('Khách Lạ Và Người Lái Taxi', 2021, 220000, 0, null, "khách_lạ_và_người_lái_taxi.jpg", 'Khách lạ và người lái taxi là cuốn sách tập hợp những câu chuyện kinh dị ngắn...', 1, 1, 4, 'Truyện ngắn, Kinh dị, Tâm lý'),
('Tội Phạm Tâm Thần Và Những Lỗ Hổng Công Lý', 2022, 115000, 0, 'Bùi Thị Huyền', "tội_phạm_tâm thần_và_những lỗ_hổng_tâm_lý.png", 'Bằng kinh nghiệm làm cố vấn tâm lý tội phạm...', 1, 6, 5, 'Tâm lý, Xã hội, Hiện thực, Tội phạm'),
('Hồ Sơ Tâm Lý Học Tâm Thần Hay Kẻ Điên', 2023, 135000, 0, 'Tú Phương', "hồ_sơ_tâm_lý_học_tâm_thần_hay_kẻ_điên.jpg", 'Cuốn sách “Hồ sơ tâm lý học - Tâm thần hay kẻ điên”...', 1, 6, 5, 'Tâm lý, Hiện thực, Xã hội, Tư duy'),
('Đứa Trẻ Hiểu Chuyện Thường Không Có Kẹo Ăn', 2020, 125000, 0, 'Nguyệt Lạc', "đứa_trẻ_hiểu_chuyện_thường_không_có_kẹo_ăn.jpg", 'Đứa trẻ hiểu chuyện thường không có kẹo ăn...', 1, 4, 2, 'Tâm lý, Kĩ năng sống'),
('Bến Xe', 2018, 110000, 0, 'Greenrosetq', "bến_xe.jpg", 'Thứ tôi có thể cho em trong cuộc đời này...', 1, 1, 2, 'Tiểu thuyết, Tình cảm, Bi kịch, Xã hội'),
('Tư Duy Ngược', 2021, 95000, 0, null, "tư_duy_ngược.png", 'Chúng ta thực sự có hạnh phúc không?...', 1, 4, 4, 'Kĩ năng sống, Tư duy, Phát triển bản thân'),
('Tư Duy Mở', 2022, 95000, 0, null, "tư_duy_mở.jpg", 'Con người đang sống trong thời đại công nghệ...', 1, 4, 4, 'Kĩ năng sống, Tư duy, Phát triển bản thân'),
('Kafka Bên Bờ Biển', 2019, 155000, 0, 'Dương Tường', "kafka_bên_bờ_biển.jpg", '"Có những điều một khi đã mất đi thì không bao giờ tìm lại được..."', 1, 1, 2, 'Truyện dài, Kì ảo, Trừu tượng'),
('Trí Tuệ Do Thái', 2020, 130000, 0, 'Phương Oanh', "trí_tuệ_do_thái.png", 'Trí tuệ Do Thái là một cuốnsách tư duy...', 1, 4, 8, 'Tư duy, Triết lý'),
('Tuổi Trẻ Đáng Giá Bao Nhiêu?', 2021, 90000, 0, null, "tuổi_trẻ_đáng_giá_bao_nhiêu.jpg", '“Bạn hối tiếc vì không nắm bắt lấy một cơ hội nào đó..."', 1, 4, 3, 'Triết lý, Phát triển bản thân'),
('Nhà Giả Kim', 2017, 85000, 0, 'Lê Chu Cầu', "nhà_giả_kim.jpg", '"Nhà Giả Kim" không đơn thuần là một cuốn tiểu thuyết...', 1, 1, 3, 'Tiểu thuyết, Phiêu lưu, Kì ảo, Kinh điển'),
('Đắc Nhân Tâm', 2020, 100000, 0, 'Minh Chương', "đắc_nhân_tâm_minh_chương.jpg", '“Kiếm một triệu đô la còn dễ hơn tạo ra một cụm từ thông dụng trong tiếng Anh”...', 1, 4, 4, 'Kĩ năng sống, Giao tiếp, Phát triển bản thân'),
('Đắc Nhân Tâm', 2018, 95000, 0, 'Trần Cẩm', "đắc_nhân_tâm_trần_cẩm.jpg", 'Đắc nhân tâm của Dale Carnegie là quyển sách của mọi thời đại...', 1, 4, 2, 'Kĩ năng sống, Giao tiếp, Phát triển bản thân'),
('Tôi thấy hoa vàng trên cỏ xanh', 2021, 95000, 0, null, "tôi_thấy_hoa_vàng_trên_cỏ_xanh.jpg", 'Những câu chuyện nhỏ xảy ra ở một ngôi làng nhỏ...', 1, 1, 1, 'Tiểu thuyết, Gia đình, Thiếu nhi, Tình cảm, Nguyễn Nhật Ánh'),
('Cho tôi xin một vé đi tuổi thơ', 2020, 95000, 0, null, "cho_tôi_xin_một_vé_đi_tuổi_thơ.jpg", 'Câu chuyện xoay quanh cu Mùi, Tí sún, Hải cò và Tủn...', 1, 1, 1, 'Tiểu thuyết, Thiếu nhi, Tình cảm, Nguyễn Nhật Ánh'),
('Thiên tài bên trái, Kẻ điên bên phải', 2022, 125000, 0, 'Thu Hương', "thiên_tài_bên_trái,_kẻ_điên_bên_phải.jpg", 'Hỡi những con người đang oằn mình trong cuộc sống...', 1, 6, 10, 'Tâm lý, Triết lý, Hiện thực, Tư duy');


insert into book_author (book_id, author_id) values
    (1, 1), (2, 1), (3, 1), (4, 2), (5, 3), (6, 4), (7, 5), (8, 6), (9, 6),
    (10, 6), (11, 6), (12, 6), (13, 6), (14, 7), (15, 12), (16, 8), (17, 9),
    (18, 10), (19, 11), (20, 13), (21, 13), (22, 14), (23, 15), (24, 16),
    (25, 17), (26, 18), (27, 18), (28, 1), (29, 1), (30, 20);

insert into membership_rank(rank_name, min_point, discount_percent) values
('Thành viên', 0, 0),
('Bạc', 2000, 5),
('Vàng', 7000, 10),
('Bạch Kim', 20000, 15);

insert into customer(customer_name, customer_phone, point, rank_id) values
('Ý Vy', '0909901421', 2100, 2),
('Tòng Nhân', '0909901422', 0, 1),
('Hồng Anh', '0909909233', 7500, 3),
('Lê Ngọc Quý', '0934129959', 0, 1);

insert into promotion (promotion_name, percent, start_date, end_date, status) values
('Mừng Xuân 2026 - Giảm giá Văn học', 10.00, '2026-03-01 00:00:00', '2026-04-01 23:59:59', 1),
('Xả kho Truyện Tranh - Flash Sale', 30.00, '2026-02-10 00:00:00', '2026-03-01 23:59:59', 0),
('Tuần lễ Kỹ Năng Sống', 10.00, '2026-03-01 00:00:00', '2026-04-01 23:59:59', 0);

insert into promotion_detail (promotion_id, book_id)
SELECT 1, book_id FROM book WHERE category_id = 1;

insert into promotion_detail (promotion_id, book_id)
SELECT 2, book_id FROM book WHERE category_id = 3;

insert into promotion_detail (promotion_id, book_id)
SELECT 3, book_id FROM book WHERE category_id = 4;

insert into import_ticket (created_date, total_import_quantity, total_import_price, status, employee_id, supplier_id, approver_id)
values ('2026-02-01 10:00:00', 500, 43066000, 1, 1, 1, null);

insert into import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price)
values (1, 1, 18, 52000), (1, 2, 69, 20000), (1, 3, 26, 80000), 
       (1, 4, 70, 59000), (1, 28, 110, 107000), (1, 29, 207, 110000);

insert into import_ticket (created_date, total_import_quantity, total_import_price, status, employee_id, supplier_id, approver_id)
values ('2026-03-01 3:00:00', 764, 42397000, 1, 1, 2, null);

insert into import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price)
values (2, 5, 33, 44000), (2, 6, 26, 78000), (2, 14, 39, 90000),
       (2, 18, 188, 25000), (2, 19, 157, 35000), (2, 22, 199, 66000), 
       (2, 27, 122, 99000);

insert into import_ticket (created_date, total_import_quantity, total_import_price, status, employee_id, supplier_id, approver_id)
values ('2026-04-12 6:20:00', 430, 22518000, 1, 1, 4, null);

insert into import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price)
values (3, 15, 116, 30000), (3, 20, 178, 49000), (3, 21, 59, 60000),
       (3, 26, 77, 88000);

insert into import_ticket (created_date, total_import_quantity, total_import_price, status, employee_id, supplier_id, approver_id)
values ('2026-02-22 6:20:00', 286, 5810000, 1, 1, 6, null);

insert into import_ticket_detail (import_ticket_id, book_id, import_quantity, import_price)
values (4, 8, 90, 12000), (4, 9, 29, 25000), (4, 10, 71, 30000),
       (4, 11, 19, 15000), (4, 12, 67, 20000), (4, 13, 10, 25000);
