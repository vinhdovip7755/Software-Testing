package com.bookstore.dto;

public class SupplierDTO {
    private int supplierId;
    private String supplierName;
    private String supplierAddress;
    private String supplierPhone;

    public SupplierDTO(){}

    public SupplierDTO(int supplierId, String supplierName, String supplierAddress, String supplierPhone) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.supplierAddress = supplierAddress;
        this.supplierPhone = supplierPhone;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplier_address) {
        this.supplierAddress = supplier_address;
    }

    public String getSupplierPhone() {
        return supplierPhone;
    }

    public void setSupplierPhone(String supplier_phone) {
        this.supplierPhone = supplier_phone;
    }
}