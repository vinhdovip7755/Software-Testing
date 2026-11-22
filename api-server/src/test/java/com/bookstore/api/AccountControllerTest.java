package com.bookstore.api;

import com.bookstore.api.contract.ProfileDTO;
import com.bookstore.api.contract.ProfileUpdateRequestDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dao.AccountDAO;
import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountControllerTest {
    @Test
    void accountListRequiresAccountViewPermission() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(new EmployeeDTO(), Map.of());
        AccountController controller = new AccountController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getAccounts("Bearer " + token)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void updateRejectsInvalidEmployeeIdBeforeDatabaseAccess() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(
                new EmployeeDTO(),
                Map.of("MANAGE_ACCOUNT", new PermissionDTO("MANAGE_ACCOUNT", true, true))
        );
        AccountController controller = new AccountController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateAccount(
                        "Bearer " + token,
                        0,
                        new AccountController.UpdateAccountRequest(null, 1)
                )
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void createAccountEnforcesRbacBeforeValidation() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        String token = sessionStore.createToken(employee(3, 3), Map.of());
        AccountController controller = new AccountController(sessionStore);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.createAccount("Bearer " + token, new AccountController.CreateAccountRequest("", "", 0, 9))
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void blocksSelfLockoutAndAdminLockoutAndAdminHijack() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        AccountBUS accountBUS = mock(AccountBUS.class);
        AccountDAO accountDAO = mock(AccountDAO.class);
        EmployeeDAO employeeDAO = mock(EmployeeDAO.class);
        EmployeeBUS employeeBUS = mock(EmployeeBUS.class);

        AccountController controller = new AccountController(
                sessionStore, accountBUS, accountDAO, employeeDAO, employeeBUS,
                (email, pwd) -> true, () -> "TempPass1234"
        );

        String adminToken = sessionStore.createToken(
                employee(1, 1),
                Map.of("MANAGE_ACCOUNT", new PermissionDTO("MANAGE_ACCOUNT", true, true))
        );
        String customManagerToken = sessionStore.createToken(
                employee(2, 2),
                Map.of("MANAGE_ACCOUNT", new PermissionDTO("MANAGE_ACCOUNT", true, true))
        );

        // Self-lockout via PUT /api/accounts/{id} -> 403
        ResponseStatusException selfLock = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateAccount("Bearer " + adminToken, 1,
                        new AccountController.UpdateAccountRequest(null, 0))
        );
        assertEquals(403, selfLock.getStatusCode().value());

        // Non-admin trying to lock Admin via PUT /api/accounts/1 -> 403
        when(accountDAO.selectByEmployeeId(1)).thenReturn(new AccountDTO("admin@example.com", "hash", 1, 1));
        when(employeeDAO.selectById(1)).thenReturn(employee(1, 1));
        ResponseStatusException adminLock = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateAccount("Bearer " + customManagerToken, 1,
                        new AccountController.UpdateAccountRequest(null, 0))
        );
        assertEquals(403, adminLock.getStatusCode().value());

        // Non-admin with MANAGE_ACCOUNT trying PUT /api/accounts/admin -> 403
        ResponseStatusException adminHijack = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateAdminAccount("Bearer " + customManagerToken,
                        new AccountController.AdminUpdateRequest("admin@example.com", "evil@example.com", "hacked123"))
        );
        assertEquals(403, adminHijack.getStatusCode().value());

        // Non-admin trying POST /api/accounts/1/reset-password -> 403
        ResponseStatusException adminReset = assertThrows(
                ResponseStatusException.class,
                () -> controller.sendNewPassword("Bearer " + customManagerToken, 1)
        );
        assertEquals(403, adminReset.getStatusCode().value());
    }

    @Test
    void lockingOrResettingAccountRevokesSessionsAndRollsBackOnEmailFailure() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        AccountBUS accountBUS = mock(AccountBUS.class);
        AccountDAO accountDAO = mock(AccountDAO.class);
        EmployeeDAO employeeDAO = mock(EmployeeDAO.class);
        EmployeeBUS employeeBUS = mock(EmployeeBUS.class);

        AtomicBoolean emailShouldSucceed = new AtomicBoolean(false);
        AccountController controller = new AccountController(
                sessionStore, accountBUS, accountDAO, employeeDAO, employeeBUS,
                (email, pwd) -> emailShouldSucceed.get(), () -> "AbcDef123456"
        );

        String adminToken = sessionStore.createToken(
                employee(1, 1),
                Map.of("MANAGE_ACCOUNT", new PermissionDTO("MANAGE_ACCOUNT", true, true))
        );
        String staffToken = sessionStore.createToken(employee(3, 3), Map.of());

        AccountDTO staffAcc = new AccountDTO("staff@example.com", "$2a$10$oldhash", 3, 1);
        when(accountDAO.selectByEmployeeId(3)).thenReturn(staffAcc);
        when(employeeDAO.selectById(3)).thenReturn(employee(3, 3));
        when(accountBUS.updateAccount(any(AccountDTO.class), anyBoolean()))
                .thenReturn("Cập nhật tài khoản thành công!");

        // Email failure rolls back password hash and returns 503
        ResponseStatusException mailFail = assertThrows(
                ResponseStatusException.class,
                () -> controller.sendNewPassword("Bearer " + adminToken, 3)
        );
        assertEquals(503, mailFail.getStatusCode().value());
        verify(accountDAO).restorePasswordHash("staff@example.com", "$2a$10$oldhash");

        // Email success revokes target employee sessions
        emailShouldSucceed.set(true);
        ResponseEntity<String> resetOk = controller.sendNewPassword("Bearer " + adminToken, 3);
        assertEquals(200, resetOk.getStatusCode().value());
        assertNull(sessionStore.find(staffToken));

        // Locking account also revokes target employee sessions
        String staffToken2 = sessionStore.createToken(employee(3, 3), Map.of());
        ResponseEntity<String> lockOk = controller.updateAccount("Bearer " + adminToken, 3,
                new AccountController.UpdateAccountRequest(null, 0));
        assertEquals(200, lockOk.getStatusCode().value());
        assertNull(sessionStore.find(staffToken2));
    }

    @Test
    void adminUpdateValidatesDuplicatePasswordBeforeCommittingAndUpdatesAtomically() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        AccountBUS accountBUS = mock(AccountBUS.class);
        AccountDAO accountDAO = mock(AccountDAO.class);
        EmployeeDAO employeeDAO = mock(EmployeeDAO.class);
        EmployeeBUS employeeBUS = mock(EmployeeBUS.class);

        AccountController controller = new AccountController(
                sessionStore, accountBUS, accountDAO, employeeDAO, employeeBUS,
                (email, pwd) -> true, () -> "AbcDef123456"
        );

        String adminToken1 = sessionStore.createToken(
                employee(1, 1),
                Map.of("MANAGE_ACCOUNT", new PermissionDTO("MANAGE_ACCOUNT", true, true))
        );
        String adminToken2 = sessionStore.createToken(
                employee(1, 1),
                Map.of("MANAGE_ACCOUNT", new PermissionDTO("MANAGE_ACCOUNT", true, true))
        );

        String currentHash = PasswordUtil.hash("admin123");
        when(accountBUS.selectByUsername("admin@example.com"))
                .thenReturn(new AccountDTO("admin@example.com", currentHash, 1, 1));
        when(employeeDAO.selectById(1)).thenReturn(employee(1, 1));

        // Duplicate password must return 422 without committing username change
        ResponseEntity<String> dupResp = controller.updateAdminAccount("Bearer " + adminToken1,
                new AccountController.AdminUpdateRequest("admin@example.com", "newadmin@example.com", "admin123"));
        assertEquals(422, dupResp.getStatusCode().value());
        verify(accountDAO, never()).updateAdminCredentialsAtomic(anyInt(), anyString(), anyString(), any());

        // Valid atomic update commits both tables and revokes other admin sessions only
        when(accountDAO.selectByUsername("newadmin@example.com")).thenReturn(null);
        when(employeeDAO.isEmailExist("newadmin@example.com", 1)).thenReturn(false);
        when(accountDAO.updateAdminCredentialsAtomic(eq(1), eq("admin@example.com"), eq("newadmin@example.com"), anyString()))
                .thenReturn(true);

        ResponseEntity<String> okResp = controller.updateAdminAccount("Bearer " + adminToken1,
                new AccountController.AdminUpdateRequest("admin@example.com", "newadmin@example.com", "BrandNew99"));
        assertEquals(200, okResp.getStatusCode().value());
        assertNotNull(sessionStore.find(adminToken1));
        assertNull(sessionStore.find(adminToken2));
    }

    @Test
    void profileControllerReadsFreshDbAndUsesSelfProfileUpdateOnly() {
        AuthSessionStore sessionStore = new AuthSessionStore();
        AccountDAO accountDAO = mock(AccountDAO.class);
        EmployeeDAO employeeDAO = mock(EmployeeDAO.class);
        ProfileController profileController = new ProfileController(sessionStore, accountDAO, employeeDAO);

        EmployeeDTO cachedSessionEmp = employee(2, 2);
        cachedSessionEmp.setEmployeeName("Old Name");
        String token = sessionStore.createToken(cachedSessionEmp, Map.of());

        // Stale session cannot unlock locked DB employee
        EmployeeDTO lockedFresh = employee(2, 3);
        lockedFresh.setStatus(0);
        when(employeeDAO.selectById(2)).thenReturn(lockedFresh);
        ResponseStatusException lockedEx = assertThrows(
                ResponseStatusException.class,
                () -> profileController.updateProfile("Bearer " + token,
                        new ProfileUpdateRequestDTO("Nguyen Van A", "0912345678", Date.valueOf("2000-01-01"),
                                "manager@example.com", ""))
        );
        assertEquals(401, lockedEx.getStatusCode().value());
        verify(employeeDAO, never()).updateEmployee(any());

        // Active employee updates ONLY self-profile columns atomically and enforces email <= 50
        EmployeeDTO activeFresh = employee(2, 3);
        activeFresh.setEmployeeName("Fresh Name");
        activeFresh.setEmail("manager@example.com");
        AccountDTO activeAcc = new AccountDTO("manager@example.com", PasswordUtil.hash("oldpass"), 2, 1);
        when(employeeDAO.selectById(2)).thenReturn(activeFresh);
        when(accountDAO.selectByEmployeeId(2)).thenReturn(activeAcc);
        when(employeeDAO.isPhoneExist("0912345678", 2)).thenReturn(false);

        String longEmail = "a".repeat(45) + "@example.com";
        ResponseStatusException tooLongEmail = assertThrows(
                ResponseStatusException.class,
                () -> profileController.updateProfile("Bearer " + token,
                        new ProfileUpdateRequestDTO("Nguyen Van A", "0912345678", Date.valueOf("2000-01-01"),
                                longEmail, ""))
        );
        assertEquals(400, tooLongEmail.getStatusCode().value());

        when(employeeDAO.updateSelfProfileAtomic(
                eq(2), eq("Nguyen Van A"), eq("0912345678"), eq("manager@example.com"),
                eq(Date.valueOf("2000-01-01")), eq("manager@example.com"), eq("manager@example.com"), isNull()))
                .thenReturn(true);

        ProfileDTO updated = profileController.updateProfile("Bearer " + token,
                new ProfileUpdateRequestDTO("Nguyen Van A", "0912345678", Date.valueOf("2000-01-01"),
                        "manager@example.com", ""));
        assertEquals("manager@example.com", updated.username());
        assertEquals("Old Name", cachedSessionEmp.getEmployeeName());
        verify(employeeDAO, never()).updateEmployee(any());
        verify(employeeDAO).updateSelfProfileAtomic(
                eq(2), eq("Nguyen Van A"), eq("0912345678"), eq("manager@example.com"),
                eq(Date.valueOf("2000-01-01")), eq("manager@example.com"), eq("manager@example.com"), isNull());
        assertTrue(updated.employee().getRoleId() == 3);
    }

    private static EmployeeDTO employee(int employeeId, int roleId) {
        EmployeeDTO emp = new EmployeeDTO();
        emp.setEmployeeId(employeeId);
        emp.setRoleId(roleId);
        emp.setStatus(1);
        return emp;
    }
}
