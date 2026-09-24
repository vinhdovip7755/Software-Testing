package com.bookstore.dto;

public class BillDetailDTO {
    private int billId;
    private int bookId;
    private String bookName;
    private int quantity;
    private double unitPrice;

    public BillDetailDTO(int billId, int bookId, String bookName, int quantity, double unitPrice) {
        this.billId = billId;
        this.bookId = bookId;
        this.bookName = bookName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BillDetailDTO(int billId, int bookId, int quantity, double unitPrice) {
        this.billId = billId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
