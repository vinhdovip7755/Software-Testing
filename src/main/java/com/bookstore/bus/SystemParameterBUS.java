package com.bookstore.bus;

import com.bookstore.dao.SystemParameterDAO;

public class SystemParameterBUS {
    private SystemParameterDAO parameterDAO = new SystemParameterDAO();

    public double getVAT() {
        String value = parameterDAO.getValueByKey("VAT");
        if (value != null && !value.isEmpty()) {
            try {
                return Double.parseDouble(value) / 100.0;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0.08;
    }

    public int getEarnedPointsPer10K() {
        String value = parameterDAO.getValueByKey("EARNED_POINTS_PER_10K");
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 100;
    }
}