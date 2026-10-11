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

    // Ràng buộc khoảng ngày hệ thống toàn cục: 01/01/2026 đến 31/12/2099
    public static final java.util.Date SYSTEM_MIN_DATE;
    public static final java.util.Date SYSTEM_MAX_DATE;
    static {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(2026, java.util.Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        SYSTEM_MIN_DATE = cal.getTime();
        cal.set(2099, java.util.Calendar.DECEMBER, 31, 23, 59, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        SYSTEM_MAX_DATE = cal.getTime();
    }

    public static boolean isDateOutOfBounds(java.util.Date date) {
        if (date == null) return false;
        return date.before(SYSTEM_MIN_DATE) || date.after(SYSTEM_MAX_DATE);
    }
}

