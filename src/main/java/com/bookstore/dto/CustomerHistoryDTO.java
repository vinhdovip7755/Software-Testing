package com.bookstore.dto;

import java.sql.Timestamp;

public class CustomerHistoryDTO {
    private int billId;
    private Timestamp createdDate;
    private String customerName;
    private String customerPhone;
    private int totalQuantity;
    private double totalPrice;

    public CustomerHistoryDTO() {}

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public int getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(int totalQuantity) { this.totalQuantity = totalQuantity; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}