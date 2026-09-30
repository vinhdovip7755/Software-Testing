package com.bookstore.dto;

public class FinancialStatsDTO {
    private String thoiGian;
    private double doanhThu;
    private double chiPhi;
    private double vonNhapHang;
    private double loiNhuan;

    public FinancialStatsDTO(String thoiGian, double doanhThu, double chiPhi, double vonNhapHang) {
        this.thoiGian = thoiGian;
        this.doanhThu = doanhThu;
        this.chiPhi = chiPhi;
        this.vonNhapHang = vonNhapHang;
        this.loiNhuan = doanhThu - chiPhi;
    }

    public String getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(String thoiGian) {
        this.thoiGian = thoiGian;
    }

    public double getDoanhThu() {
        return doanhThu;
    }

    public void setDoanhThu(double doanhThu) {
        this.doanhThu = doanhThu;
        this.loiNhuan = this.doanhThu - this.chiPhi;
    }

    public double getChiPhi() {
        return chiPhi;
    }

    public void setChiPhi(double chiPhi) {
        this.chiPhi = chiPhi;
        this.loiNhuan = this.doanhThu - this.chiPhi;
    }

    public double getVonNhapHang() {
        return vonNhapHang;
    }

    public void setVonNhapHang(double vonNhapHang) {
        this.vonNhapHang = vonNhapHang;
    }

    public double getLoiNhuan() {
        return loiNhuan;
    }
}