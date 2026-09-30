package com.bookstore.util;

public class AppConstant {
    public static final String FONT_NAME = "Segoe UI";
    public static final String GREEN_COLOR_CODE = "#114732";
    public static final String BUTTON_COLOR = "#4CAF50";

    // Chuẩn Regex họ tên tiếng Việt & đồng bào dân tộc thiểu số theo chuẩn VNeID / Ngân hàng
    public static final String REGEX_NAME = "^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$";
    // Số điện thoại di động cá nhân (Nhân viên, Khách hàng): 10 chữ số bắt đầu bằng số 0
    public static final String REGEX_PHONE = "^0\\d{9}$";
    // Số điện thoại Nhà xuất bản / Nhà cung cấp: Di động 10 số, số bàn cố định 11 số (đầu 02x), hoặc tổng đài Hotline 1800/1900
    public static final String REGEX_SUPPLIER_PHONE = "^(0\\d{9,10}|1[89]00\\d{4,6})$";
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
}
