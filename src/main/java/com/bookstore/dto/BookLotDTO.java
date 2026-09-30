package com.bookstore.dto;

import java.sql.Timestamp;

public class BookLotDTO {
    private int lotId;
    private int bookId;
    private int importTicketId;
    private Timestamp importDate;
    private double coverPrice;
    private double discountPercent;
    private double importPrice;
    private double sellingPrice;
    private int quantityInitial;
    private int quantityRemain;

    public BookLotDTO() {}

    public BookLotDTO(int lotId, int bookId, int importTicketId, Timestamp importDate, double coverPrice, double discountPercent, double importPrice, double sellingPrice, int quantityInitial, int quantityRemain) {
        this.lotId = lotId;
        this.bookId = bookId;
        this.importTicketId = importTicketId;
        this.importDate = importDate;
        this.coverPrice = coverPrice;
        this.discountPercent = discountPercent;
        this.importPrice = importPrice;
        this.sellingPrice = sellingPrice;
        this.quantityInitial = quantityInitial;
        this.quantityRemain = quantityRemain;
    }

    public int getLotId() { return lotId; }
    public void setLotId(int lotId) { this.lotId = lotId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getImportTicketId() { return importTicketId; }
    public void setImportTicketId(int importTicketId) { this.importTicketId = importTicketId; }

    public Timestamp getImportDate() { return importDate; }
    public void setImportDate(Timestamp importDate) { this.importDate = importDate; }

    public double getCoverPrice() { return coverPrice; }
    public void setCoverPrice(double coverPrice) { this.coverPrice = coverPrice; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }

    public double getImportPrice() { return importPrice; }
    public void setImportPrice(double importPrice) { this.importPrice = importPrice; }

    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }

    public int getQuantityInitial() { return quantityInitial; }
    public void setQuantityInitial(int quantityInitial) { this.quantityInitial = quantityInitial; }

    public int getQuantityRemain() { return quantityRemain; }
    public void setQuantityRemain(int quantityRemain) { this.quantityRemain = quantityRemain; }
}
