package com.bookstore.dto;

public class AccountDTO {
    private String username;
    private String password;
    private int employeeId;
    private int status;

    public AccountDTO() {}

    public AccountDTO(String username, String password, int employeeId, int status) {
        this.username = username;
        this.password = password;
        this.employeeId = employeeId;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
