package com.bookstore.dto;

public class ProductStatsDTO {
    private String thoiGian;
    private int bookId;
    private String tenSach;
    private int soLuongBan;

    public ProductStatsDTO(){}

    public ProductStatsDTO(String thoiGian, int bookId, String tenSach, int soLuongBan){
        this.thoiGian = thoiGian;
        this.bookId = bookId;
        this.tenSach = tenSach;
        this.soLuongBan = soLuongBan;
    }

    public String getThoiGian(){return thoiGian;}
    public void setThoiGian(String thoiGian){this.thoiGian = thoiGian;}

    public int getBookId(){return bookId;}
    public void setBookId(int bookId){this.bookId = bookId;}

    public String getTenSach(){return tenSach;}
    public void setTenSach(String tenSach){this.tenSach = tenSach;}

    public int getSoLuongBan(){return soLuongBan;}
    public void setSoLuongBan(int soLuongBan){this.soLuongBan = soLuongBan;}
}