-- FILE 1/2: Khởi tạo lại database và cấu trúc bảng.
-- Chạy file này trước bookstore_data.sql.

drop database if exists bookstore_db;

create database bookstore_db character set utf8mb4 collate utf8mb4_unicode_ci;

use bookstore_db;

create table membership_rank (
	rank_id int auto_increment primary key,
    rank_name varchar(50) not null,
    min_point int default 0,
    discount_percent decimal(4, 2) default 0
);

create table role (
	role_id int auto_increment primary key,
    role_name varchar(50) not null unique
);

create table action (
	action_id int auto_increment primary key,
    action_code varchar(50) not null unique,
    action_name varchar(100) not null
);

create table category (
	category_id int auto_increment primary key,
    category_name varchar(100) unique not null
);

create table author (
	author_id int auto_increment primary key,
    author_name varchar(100) not null,
    nationality varchar(100)
);

create table supplier (
	supplier_id int auto_increment primary key,
    supplier_name varchar(100) not null,
    supplier_address varchar(255),
    supplier_phone varchar(20),
    status int default 1
);

create table payment_method (
	payment_method_id int auto_increment primary key,
    payment_method_name varchar(50) not null
);

create table employee (
	employee_id int auto_increment primary key,
    employee_name varchar(100) not null,
    employee_phone varchar(20) unique not null,
    email varchar(100) unique,
    birthday date,
    base_salary decimal(15, 0) default 0,
    salary_factor decimal(5, 2) default 1,
    day_in date,
    status int default 1,
    role_id int not null,
    foreign key (role_id) references role(role_id)
);

create table account (
	username varchar(50) primary key,
    password varchar(255) not null,
    employee_id int not null unique,
    foreign key (employee_id) references employee(employee_id),
    status int default 1
);

create table permission (
	permission_id int auto_increment primary key,
    role_id int not null,
    action_id int not null,
    is_view int default 1,
    is_action int default 1,
    foreign key (role_id) references role(role_id),
    foreign key (action_id) references action(action_id)
);

create table customer (
	customer_id int auto_increment primary key,
    customer_name varchar(100) not null,
    customer_phone varchar(20) unique not null,
    point int default 0,
    rank_id int default 1,
    foreign key (rank_id) references membership_rank(rank_id)
);

create table book (
	book_id int auto_increment primary key,
    book_name varchar(255) not null,
    publication_year int,
    selling_price decimal(15, 0) default 0,
    quantity int default 0,
	translator text,
    image varchar(255),
    description text,
    status int default 1,
    category_id int not null,
    tag_detail text,
    supplier_id int not null,
    foreign key (category_id) references category(category_id),
    foreign key (supplier_id) references supplier(supplier_id)
);

create table book_author (
	book_id int not null,
    author_id int not null,
    primary key (book_id, author_id),
    foreign key (book_id) references book(book_id),
    foreign key (author_id) references author(author_id)
);

create table promotion (
    promotion_id int auto_increment primary key,
    promotion_name varchar(255) not null,
    percent decimal(5, 2) default 0,
    start_date datetime not null,
    end_date datetime not null,
    status int default 1
);

create table promotion_detail (
    promotion_id int not null,
    book_id int not null,
    primary key (promotion_id, book_id),
    foreign key (promotion_id) references promotion(promotion_id),
    foreign key (book_id) references book(book_id)
);

create table inventory_log (
	log_id int auto_increment primary key,
    action varchar(50) not null,
    change_quantity int not null,
    remain_quantity int not null,
    reference_id int not null,
    created_date datetime default current_timestamp,
    book_id int not null,
    foreign key (book_id) references book(book_id)
);

create table bill (
	bill_id int auto_increment primary key,
    created_date datetime default current_timestamp,
    total_bill_price decimal(15, 0),
    tax decimal(2,2) default 0,
    employee_id int not null,
    customer_id int,
    payment_method_id int not null,
    earned_points int default 0,
    foreign key (employee_id) references employee(employee_id),
    foreign key (customer_id) references customer(customer_id),
    foreign key (payment_method_id) references payment_method(payment_method_id)
);

create table bill_detail (
	bill_id int not null,
    book_id int not null,
    quantity int not null,
    unit_price decimal(15, 0),
    lot_id int,
    primary key (bill_id, book_id),
    foreign key (bill_id) references bill(bill_id) on delete cascade,
    foreign key (book_id) references book(book_id)
);

create table import_ticket (
	import_ticket_id int auto_increment primary key,
    created_date datetime default current_timestamp,
    total_import_quantity int default 0,
    total_import_price decimal(15, 0),
    status int default 1,
    employee_id int not null,
    supplier_id int not null,
    approver_id int,
    approved_date datetime null,
    foreign key (employee_id) references employee(employee_id),
    foreign key (supplier_id) references supplier(supplier_id)
);

create table import_ticket_detail (
	import_ticket_id int not null,
    book_id int not null,
    import_quantity int not null,
    import_price decimal(15, 0),
    cover_price decimal(15, 0) default 0,
    discount_percent decimal(5, 2) default 0,
    primary key (import_ticket_id, book_id),
    foreign key (import_ticket_id) references import_ticket(import_ticket_id) on delete cascade,
    foreign key (book_id) references book(book_id)
);

create table book_lot (
    lot_id int auto_increment primary key,
    book_id int not null,
    import_ticket_id int not null,
    import_date datetime default current_timestamp,
    cover_price decimal(15, 0) default 0,
    discount_percent decimal(5, 2) default 0,
    import_price decimal(15, 0) not null,
    selling_price decimal(15, 0) not null,
    quantity_initial int not null,
    quantity_remain int not null,
    foreign key (book_id) references book(book_id),
    foreign key (import_ticket_id) references import_ticket(import_ticket_id) on delete cascade
);

alter table bill_detail
    add constraint fk_bill_detail_lot
    foreign key (lot_id) references book_lot(lot_id);

create table price (
    price_id INT auto_increment primary key,
    book_id INT not null,
    base_price DECIMAL(15, 0) not null,
    profit_rate DECIMAL(5, 2) default 40.0 not null,
    selling_price DECIMAL(15, 0) not null,
    effective_date DATETIME default current_timestamp,
    end_date DATETIME default null,
    is_active INT default 1,
    foreign key (book_id) references book(book_id)
);

create table system_parameter (
    param_key varchar(50) primary key,
    param_value varchar(255) not null,
    description varchar(255)
);

create index idx_inventory_date on inventory_log(created_date);

create index idx_inventory_book on inventory_log(book_id);

create index idx_bill_date on bill(created_date);
