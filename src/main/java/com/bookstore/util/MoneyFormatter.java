package com.bookstore.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MoneyFormatter {
    // DecimalFormat is NOT thread-safe; use ThreadLocal to avoid concurrent corruption
    private static final ThreadLocal<DecimalFormat> FORMATTER = ThreadLocal.withInitial(() -> {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return new DecimalFormat("###,###,###", symbols);
    });

    public static String toVND(double price) {
        if (price == 0) return "0đ";
        return FORMATTER.get().format(price) + "đ";
    }

    public static double toDouble(String priceString) {
        try {
            if (priceString == null || priceString.isEmpty()) return 0;

            String cleanString = priceString.replace(".", "")
                    .replace(",",".")
                    .replace(" ","")
                    .replace("đ", "")
                    .trim();
            return Double.parseDouble(cleanString);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}