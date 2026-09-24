package com.bookstore.dto;
import java.sql.Timestamp;

public class InventoryLogDTO {
    private int logId;
    private String action;
    private int changeQuantity;
    private int remainQuantity;
    private int referenceId;
    private Timestamp createdDate;
    private int bookId;
    private String bookName;

    public InventoryLogDTO() {}

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public int getChangeQuantity() { return changeQuantity; }
    public void setChangeQuantity(int changeQuantity) { this.changeQuantity = changeQuantity; }
    public int getRemainQuantity() { return remainQuantity; }
    public void setRemainQuantity(int remainQuantity) { this.remainQuantity = remainQuantity; }
    public int getReferenceId() { return referenceId; }
    public void setReferenceId(int referenceId) { this.referenceId = referenceId; }
    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
}