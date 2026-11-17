package com.bookstore.api.contract;

import java.sql.Timestamp;

public class CustomerPurchaseSummaryDTO {
    private int totalBooks;
    private double totalSpent;
    private Timestamp lastPurchase;

    public CustomerPurchaseSummaryDTO() {
    }

    public CustomerPurchaseSummaryDTO(int totalBooks, double totalSpent, Timestamp lastPurchase) {
        this.totalBooks = totalBooks;
        this.totalSpent = totalSpent;
        this.lastPurchase = lastPurchase;
    }

    public int getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Timestamp getLastPurchase() {
        return lastPurchase;
    }

    public void setLastPurchase(Timestamp lastPurchase) {
        this.lastPurchase = lastPurchase;
    }
}
