package com.bookstore.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MoneyFormatter {
    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        formatter = new DecimalFormat("###,###,###", symbols);
    }

    public static String toVND(double price) {
        if (price == 0) return "0đ";
        return formatter.format(price) + "đ";
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