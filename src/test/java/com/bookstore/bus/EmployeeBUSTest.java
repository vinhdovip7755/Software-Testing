package com.bookstore.bus;

import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.AuthorDTO;
import com.bookstore.dto.EmployeeDTO;
import org.junit.jupiter.api.Test;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void addEmployeeRejectsAdminRoleAndInvalidSalaryAndLongEmail() {
        EmployeeDTO admin = validEmployee();
        admin.setRoleId(1);
        assertEquals("Không được phép tạo nhân viên với chức vụ Admin!", employeeBUS.addEmployee(admin));

        EmployeeDTO badSalary = validEmployee();
        badSalary.setBaseSalary(Double.NaN);
        assertEquals("Lương cơ bản phải là số dương hợp lệ!", employeeBUS.addEmployee(badSalary));

        EmployeeDTO badFactor = validEmployee();
        badFactor.setSalaryFactor(-1);
        assertEquals("Hệ số lương phải là số dương hợp lệ!", employeeBUS.addEmployee(badFactor));

        EmployeeDTO longEmail = validEmployee();
        longEmail.setEmail("a".repeat(45) + "@example.com");
        assertEquals("Email không được vượt quá 50 ký tự!", employeeBUS.addEmployee(longEmail));
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

    @Test
    void updateEmployeeProtectsSoleAdminSeparatelyFromManagersAndBlocksZeroSalaryFactor() {
        EmployeeDTO zeroFactor = validEmployee();
        zeroFactor.setSalaryFactor(0);
        assertEquals("Hệ số lương phải là số dương hợp lệ!", employeeBUS.updateEmployee(zeroFactor));

        EmployeeDAO fakeDao = new EmployeeDAO() {
            @Override
            public boolean isPhoneExist(String phone, int ignoreId) { return false; }

            @Override
            public EmployeeDTO selectById(int id) {
                EmployeeDTO existing = validEmployee();
                existing.setEmployeeId(id);
                existing.setRoleId(id == 1 ? 1 : 2);
                return existing;
            }

            @Override
            public int countActiveAdminsExcluding(int employeeId) { return 0; }

            @Override
            public int countActiveManagersExcluding(int employeeId) { return 2; }
        };
        EmployeeBUS isolatedBus = new EmployeeBUS(fakeDao, new AccountBUS());

        // Deactivating sole Admin must fail even when 2 active Managers exist
        EmployeeDTO deactivateAdmin = validEmployee();
        deactivateAdmin.setEmployeeId(1);
        deactivateAdmin.setRoleId(1);
        deactivateAdmin.setStatus(0);
        assertTrue(isolatedBus.updateEmployee(deactivateAdmin).contains("Admin duy nhất"));

        // Promoting Manager to Admin must fail
        EmployeeDTO promoteToAdmin = validEmployee();
        promoteToAdmin.setEmployeeId(2);
        promoteToAdmin.setRoleId(1);
        assertEquals("Không được phép gán chức vụ Admin cho nhân viên!", isolatedBus.updateEmployee(promoteToAdmin));
    }

    @Test
    void authorBusHandlesNullFieldsWithoutNullPointerException() {
        AuthorBUS authorBUS = new AuthorBUS();
        assertEquals("Tên tác giả không được để trống!", authorBUS.addAuthor(null));
        assertEquals("Tên tác giả không được để trống!", authorBUS.addAuthor(new AuthorDTO()));
        assertEquals("Tên tác giả không được để trống!", authorBUS.updateAuthor(new AuthorDTO()));

        AuthorDTO missingNationality = new AuthorDTO(1, "Nguyen Nhat Anh", null);
        assertEquals("Quốc tịch tác giả không được để trống!", authorBUS.updateAuthor(missingNationality));
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
