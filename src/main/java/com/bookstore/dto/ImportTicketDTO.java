package com.bookstore.dto;

import java.sql.Timestamp;

public class ImportTicketDTO {
    private int importID;
    private int employeeID;
    private String employeeName;
    private int supplierID;
    private String supplierName;
    private String approverName;
    private double totalPrice;
    private Timestamp createdDate;
    private int status;

    public ImportTicketDTO() {}

    public ImportTicketDTO(int importID, String supplierName, Timestamp createdDate, String employeeName, double totalPrice, int status) {
        this.importID = importID;
        this.supplierName = supplierName;
        this.createdDate = createdDate;
        this.employeeName = employeeName;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public ImportTicketDTO(int employeeID, int supplierID, double totalPrice) {
        this.employeeID = employeeID;
        this.supplierID = supplierID;
        this.totalPrice = totalPrice;
    }

    public int getImportID() { return importID; }
    public void setImportID(int importID) { this.importID = importID; }

    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public int getSupplierID() { return supplierID; }
    public void setSupplierID(int supplierID) { this.supplierID = supplierID; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getApproverName() {return approverName; }
    public void setApproverName(String approverName) {this.approverName = approverName;}
}