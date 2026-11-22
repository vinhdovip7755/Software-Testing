package com.bookstore.util;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportApprovalPolicyTest {
    @Test
    void fourArgOverloadEnforcesDynamicPermissionsActiveStatusPendingTicketAndMakerChecker() {
        Map<String, PermissionDTO> withAction = Map.of(
                "MANAGE_IMPORT_TICKET", new PermissionDTO("MANAGE_IMPORT_TICKET", true, true)
        );
        Map<String, PermissionDTO> viewOnly = Map.of(
                "MANAGE_IMPORT_TICKET", new PermissionDTO("MANAGE_IMPORT_TICKET", true, false)
        );

        EmployeeDTO activeRole4 = employee(10, 4, 1);
        assertTrue(ImportApprovalPolicy.canAct(activeRole4, withAction, 20, 1));
        assertFalse(ImportApprovalPolicy.canAct(activeRole4, viewOnly, 20, 1));
        assertFalse(ImportApprovalPolicy.canAct(activeRole4, withAction, 10, 1)); // creator == actor
        assertFalse(ImportApprovalPolicy.canAct(activeRole4, withAction, 20, 2)); // already approved
        assertFalse(ImportApprovalPolicy.canAct(activeRole4, withAction, 20, 0)); // already canceled
        assertFalse(ImportApprovalPolicy.canAct(employee(10, 4, 0), withAction, 20, 1)); // inactive actor
        assertFalse(ImportApprovalPolicy.canAct(activeRole4, null, 20, 1));
    }

    @Test
    void threeArgOverloadUsesSharedDataPermissionsAndFallsBackWhenNull() {
        EmployeeDTO previousUser = SharedData.currentUser;
        Map<String, PermissionDTO> previousPermissions = SharedData.userPermissions;
        try {
            // Dynamic RBAC when SharedData.userPermissions is populated
            Map<String, PermissionDTO> granted = new HashMap<>();
            granted.put("MANAGE_IMPORT_TICKET", new PermissionDTO("MANAGE_IMPORT_TICKET", true, true));
            SharedData.userPermissions = granted;

            EmployeeDTO warehouseStaff = employee(10, 4, 1);
            SharedData.currentUser = warehouseStaff;
            assertTrue(PermissionUtil.hasActionPermission("MANAGE_IMPORT_TICKET"));
            assertTrue(ImportApprovalPolicy.canAct(warehouseStaff, 20, 1));
            assertFalse(ImportApprovalPolicy.canAct(warehouseStaff, 10, 1));
            assertFalse(ImportApprovalPolicy.canAct(warehouseStaff, 20, 2));

            granted.put("MANAGE_IMPORT_TICKET", new PermissionDTO("MANAGE_IMPORT_TICKET", true, false));
            assertFalse(PermissionUtil.hasActionPermission("MANAGE_IMPORT_TICKET"));
            assertFalse(ImportApprovalPolicy.canAct(warehouseStaff, 20, 1));

            // Headless unit test fallback when SharedData.userPermissions is null
            SharedData.userPermissions = null;
            assertTrue(ImportApprovalPolicy.canAct(employee(1, 1, 1), 20, 1));
            assertTrue(ImportApprovalPolicy.canAct(employee(2, 2, 1), 20, 1));
            assertFalse(ImportApprovalPolicy.canAct(employee(10, 4, 1), 20, 1));
            assertFalse(ImportApprovalPolicy.canAct(employee(2, 2, 0), 20, 1));
        } finally {
            SharedData.currentUser = previousUser;
            SharedData.userPermissions = previousPermissions;
        }
    }

    private static EmployeeDTO employee(int employeeId, int roleId, int status) {
        EmployeeDTO emp = new EmployeeDTO();
        emp.setEmployeeId(employeeId);
        emp.setRoleId(roleId);
        emp.setStatus(status);
        return emp;
    }
}
