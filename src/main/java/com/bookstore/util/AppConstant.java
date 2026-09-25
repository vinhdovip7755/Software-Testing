package com.bookstore.util;

public class AppConstant {
    public static final String FONT_NAME = "Segoe UI";
    public static final String GREEN_COLOR_CODE = "#114732";
    public static final String BUTTON_COLOR = "#4CAF50";

    // Chuẩn Regex họ tên tiếng Việt & đồng bào dân tộc thiểu số theo chuẩn VNeID / Ngân hàng
    public static final String REGEX_NAME = "^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$";
    public static final String REGEX_PHONE = "^0\\d{9}$";
    public static final String REGEX_EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
}
