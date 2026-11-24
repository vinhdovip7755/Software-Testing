package com.bookstore.bus;

import com.bookstore.dao.FinancialStatsDAO;
import com.bookstore.dto.FinancialStatsDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class FinancialStatsBUS {
    private final FinancialStatsDAO financialStatsDAO = new FinancialStatsDAO();

    
    public FinancialStatsDTO getThongKeTong() {
        return financialStatsDAO.getThongKeTong();
    }

    public List<FinancialStatsDTO> getThongKeTheoNgay(Date tuNgay, Date denNgay) {
        validateThongKeTheoNgay(tuNgay, denNgay);
        Date normalizedFrom = java.sql.Date.valueOf(toLocalDate(tuNgay));
        Date normalizedTo = java.sql.Date.valueOf(toLocalDate(denNgay));
        return financialStatsDAO.getThongKeTheoNgay(normalizedFrom, normalizedTo);
    }

    public List<FinancialStatsDTO> getThongKeTheoThang(int nam) {
        return financialStatsDAO.getThongKeTheoThang(nam);
    }

    public void validateThongKeTheoNgay(Date tuNgay, Date denNgay) {
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ khoảng thời gian.");
        }

        LocalDate fromDate = toLocalDate(tuNgay);
        LocalDate toDate = toLocalDate(denNgay);

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu (Từ ngày) không được lớn hơn ngày kết thúc (Đến ngày)!");
        }

        LocalDate minDate = toLocalDate(com.bookstore.util.AppConstant.SYSTEM_MIN_DATE);
        LocalDate maxDate = toLocalDate(com.bookstore.util.AppConstant.SYSTEM_MAX_DATE);
        if (fromDate.isBefore(minDate) || toDate.isAfter(maxDate)) {
            throw new IllegalArgumentException("Khoảng ngày thống kê phải nằm trong khoảng từ 01/01/2026 đến 31/12/2099!");
        }
    }

    private LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public void validateThongKeTheoNam(Date ngayChon) {
        if (ngayChon == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày để xác định năm thống kê.");
        }
    }

    public int extractYear(Date ngayChon) {
        return Integer.parseInt(new java.text.SimpleDateFormat("yyyy").format(ngayChon));
    }

    public QuyDoanhThuSummary getQuyDoanhThuSummary(int nam) {
    List<FinancialStatsDTO> data = getThongKeTheoThang(nam);        // 1
    double[] tong = new double[4];                                 // 1

    List<Double> mucCao = java.util.Arrays.asList(                 // 1
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

    List<Double> mucThap = java.util.Arrays.asList(                // 1
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    QuyDoanhThuSummary ketQua = null;                              // 1

    for (FinancialStatsDTO item : data) {                          // 2
        int thang = extractMonth(item.getThoiGian());              // 3

        if (thang >= 1 && thang <= 12) {                           // 4
            int i = (thang - 1) / 3;                               // 5
            tong[i] += item.getDoanhThu();                         // 5

            mucCao.set(i, tong[i]);                                // 5
            mucThap.set(i, tong[i]);                               // 5

            double max = java.util.Collections.max(mucCao);        // 5
            double min = java.util.Collections.min(mucThap);       // 5

            ketQua = new QuyDoanhThuSummary(                       // 5
                    nam,
                    mucCao.indexOf(max) + 1, max,
                    mucThap.indexOf(min) + 1, min);
        }
    }

    return ketQua;                                                 // 6
}                                                                  // 7

    private int extractMonth(String thoiGian) {
        if (thoiGian == null) {
            return -1;
        }

        String normalized = thoiGian.trim().toLowerCase();
        if (!normalized.startsWith("tháng ")) {
            return -1;
        }

        String[] parts = normalized.replace("tháng ", "").split("/");
        if (parts.length == 0) {
            return -1;
        }

        try {
            return Integer.parseInt(parts[0].trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static class QuyDoanhThuSummary {
        private final int nam;
        private final int quyCaoNhat;
        private final double doanhThuQuyCaoNhat;
        private final int quyThapNhat;
        private final double doanhThuQuyThapNhat;

        public QuyDoanhThuSummary(int nam, int quyCaoNhat, double doanhThuQuyCaoNhat, int quyThapNhat, double doanhThuQuyThapNhat) {
            this.nam = nam;
            this.quyCaoNhat = quyCaoNhat;
            this.doanhThuQuyCaoNhat = doanhThuQuyCaoNhat;
            this.quyThapNhat = quyThapNhat;
            this.doanhThuQuyThapNhat = doanhThuQuyThapNhat;
        }

        public int getNam() {
            return nam;
        }

        public int getQuyCaoNhat() {
            return quyCaoNhat;
        }

        public double getDoanhThuQuyCaoNhat() {
            return doanhThuQuyCaoNhat;
        }

        public int getQuyThapNhat() {
            return quyThapNhat;
        }

        public double getDoanhThuQuyThapNhat() {
            return doanhThuQuyThapNhat;
        }
    }
}