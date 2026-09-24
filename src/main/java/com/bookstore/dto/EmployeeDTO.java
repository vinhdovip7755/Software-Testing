package com.bookstore.dto;


import java.sql.Date;

public class EmployeeDTO {
    private int employeeId;
    private String employeeName;
    private String employeePhone;
    private Date birthday;
    private double baseSalary;
    private double salaryFactor;
    private Date dayIn;
    private int status;
    private int roleId;
    private String roleName;

    public EmployeeDTO() {}

    public EmployeeDTO(int employeeId, String employeeName, String employeePhone, Date birthday, double baseSalary, double salaryFactor, Date dayIn, int status, int roleId, String roleName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.employeePhone = employeePhone;
        this.birthday = birthday;
        this.baseSalary = baseSalary;
        this.salaryFactor = salaryFactor;
        this.dayIn = dayIn;
        this.status = status;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeePhone() {
        return employeePhone;
    }

    public void setEmployeePhone(String employeePhone) {
        this.employeePhone = employeePhone;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public double getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(double baseSalary) {
        this.baseSalary = baseSalary;
    }

    public double getSalaryFactor() {
        return salaryFactor;
    }

    public void setSalaryFactor(double salaryFactor) {
        this.salaryFactor = salaryFactor;
    }

    public Date getDayIn() {
        return dayIn;
    }

    public void setDayIn(Date dayIn) {
        this.dayIn = dayIn;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
