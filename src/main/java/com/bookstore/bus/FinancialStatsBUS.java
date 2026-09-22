package com.bookstore.bus;

import com.bookstore.dao.FinancialStatsDAO;
import com.bookstore.dto.FinancialStatsDTO;

import java.util.Date;
import java.util.List;

public class FinancialStatsBUS {
    private final FinancialStatsDAO financialStatsDAO = new FinancialStatsDAO();

    
    public FinancialStatsDTO getThongKeTong() {
        return financialStatsDAO.getThongKeTong();
    }

    public List<FinancialStatsDTO> getThongKeTheoNgay(Date tuNgay, Date denNgay) {
        return financialStatsDAO.getThongKeTheoNgay(tuNgay, denNgay);
    }

    public List<FinancialStatsDTO> getThongKeTheoThang(int nam) {
        return financialStatsDAO.getThongKeTheoThang(nam);
    }

    public void validateThongKeTheoNgay(Date tuNgay, Date denNgay) {
        if (tuNgay == null || denNgay == null) {
            throw new IllegalArgumentException("Vui lòng chọn đầy đủ khoảng thời gian.");
        }

        if (tuNgay.after(denNgay)) {
            throw new IllegalArgumentException("'Từ ngày' không được lớn hơn 'Đến ngày'.");
        }
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
        List<FinancialStatsDTO> dataTheoThang = getThongKeTheoThang(nam);
        if (dataTheoThang.isEmpty()) {
            return null;
        }

        double[] doanhThuTheoQuy = new double[4];
        boolean[] coDuLieuQuy = new boolean[4];
        for (FinancialStatsDTO item : dataTheoThang) {
            int thang = extractMonth(item.getThoiGian());
            if (thang <= 0) {
                continue;
            }
            int indexQuy = (thang - 1) / 3;
            doanhThuTheoQuy[indexQuy] += item.getDoanhThu();
            coDuLieuQuy[indexQuy] = true;
        }

        int quyMax = -1;
        int quyMin = -1;
        double doanhThuMax = Double.NEGATIVE_INFINITY;
        double doanhThuMin = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 4; i++) {
            if (!coDuLieuQuy[i]) {
                continue;
            }

            double doanhThu = doanhThuTheoQuy[i];
            int quy = i + 1;


            if (doanhThu > doanhThuMax) {
                doanhThuMax = doanhThu;
                quyMax = quy;
            }

            if (doanhThu < doanhThuMin) {
                doanhThuMin = doanhThu;
                quyMin = quy;
            }
        }

        if (quyMax == -1 || quyMin == -1) {
            return null;
        }

        return new QuyDoanhThuSummary(nam, quyMax, doanhThuMax, quyMin, doanhThuMin);
    }

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