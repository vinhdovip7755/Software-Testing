package com.bookstore.bus;

import com.bookstore.dto.EmployeeDTO;
import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeBUSTest {
    private final EmployeeBUS employeeBUS = new EmployeeBUS();

    @Test
    void addEmployeeRejectsBlankName() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeeName("   ");

        assertEquals("Tên nhân viên không được để trống!", employeeBUS.addEmployee(employee));
    }

    @Test
    void addEmployeeRejectsInvalidName() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeeName("123");

        assertEquals("Tên nhân viên không hợp lệ!", employeeBUS.addEmployee(employee));
    }

    @Test
    void addEmployeeRejectsBlankPhone() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeePhone("   ");

        assertEquals("Số điện thoại không được để trống!", employeeBUS.addEmployee(employee));
    }

    @Test
    void addEmployeeRejectsInvalidPhone() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeePhone("1234567890");

        assertEquals("Số điện thoại phải có 10 số và bắt đầu bằng 0!", employeeBUS.addEmployee(employee));
    }

    @Test
    void addEmployeeRejectsBlankEmail() {
        EmployeeDTO employee = validEmployee();
        employee.setEmail("   ");

        assertEquals("Email không được để trống!", employeeBUS.addEmployee(employee));
    }

    @Test
    void addEmployeeRejectsInvalidEmail() {
        EmployeeDTO employee = validEmployee();
        employee.setEmail("invalid-email");

        assertEquals("Email không đúng định dạng!", employeeBUS.addEmployee(employee));
    }

    @Test
    void addEmployeeRejectsMissingStartDate() {
        EmployeeDTO employee = validEmployee();
        employee.setDayIn(null);

        assertEquals("Vui lòng chọn ngày vào làm!", employeeBUS.addEmployee(employee));
    }

    @Test
    void updateEmployeeRejectsBlankName() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeeName("   ");

        assertEquals("Tên nhân viên không được để trống!", employeeBUS.updateEmployee(employee));
    }

    @Test
    void updateEmployeeRejectsInvalidName() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeeName("123");

        assertEquals("Tên nhân viên không hợp lệ!", employeeBUS.updateEmployee(employee));
    }

    @Test
    void updateEmployeeRejectsBlankPhone() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeePhone("   ");

        assertEquals("Số điện thoại không được để trống!", employeeBUS.updateEmployee(employee));
    }

    @Test
    void updateEmployeeRejectsInvalidPhone() {
        EmployeeDTO employee = validEmployee();
        employee.setEmployeePhone("1234567890");

        assertEquals("Số điện thoại phải có 10 số và bắt đầu bằng 0!", employeeBUS.updateEmployee(employee));
    }

    private EmployeeDTO validEmployee() {
        EmployeeDTO employee = new EmployeeDTO();
        employee.setEmployeeId(1);
        employee.setEmployeeName("Nguyen Van A");
        employee.setEmployeePhone("0123456789");
        employee.setEmail("employee@example.com");
        employee.setBaseSalary(10_000_000);
        employee.setSalaryFactor(1);
        employee.setDayIn(Date.valueOf("2024-01-01"));
        employee.setStatus(1);
        employee.setRoleId(2);
        return employee;
    }
}
