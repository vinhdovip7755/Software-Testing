package com.bookstore.dto;

public class ImportDetailDTO {
    private int importID;
    private int bookID;
    private int quantity;
    private double price; // Giá nhập
    private String bookName;
    private double coverPrice;
    private double discountPercent;

    public ImportDetailDTO() {}

    public ImportDetailDTO(int importID, int bookID, int quantity, double price) {
        this.importID = importID;
        this.bookID = bookID;
        this.quantity = quantity;
        this.price = price;
    }

    public ImportDetailDTO(int importID, int bookID, int quantity, double price, double coverPrice, double discountPercent) {
        this.importID = importID;
        this.bookID = bookID;
        this.quantity = quantity;
        this.price = price;
        this.coverPrice = coverPrice;
        this.discountPercent = discountPercent;
    }

    public int getImportID() { return importID; }
    public void setImportID(int importID) { this.importID = importID; }

    public int getBookID() { return bookID; }
    public void setBookID(int bookID) { this.bookID = bookID; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getCoverPrice() { return coverPrice; }
    public void setCoverPrice(double coverPrice) { this.coverPrice = coverPrice; }

    public double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
}