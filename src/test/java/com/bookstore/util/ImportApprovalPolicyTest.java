package com.bookstore.util;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ImportApprovalPolicyTest {
    @Test
    void allowsOnlyManagersAndAdminsWhoDidNotCreateTheTicket() {
        assertTrue(ImportApprovalPolicy.canAct(1, 10, 20));
        assertTrue(ImportApprovalPolicy.canAct(2, 10, 20));
        assertFalse(ImportApprovalPolicy.canAct(4, 10, 20));
        assertFalse(ImportApprovalPolicy.canAct(2, 10, 10));
        assertFalse(ImportApprovalPolicy.canAct(2, 0, 20));
    }

    @Test
    void deniesImportActionsToWarehouseStaffEvenWithStalePermissionData() {
        EmployeeDTO previousUser = SharedData.currentUser;
        Map<String, PermissionDTO> previousPermissions = SharedData.userPermissions;
        try {
            Map<String, PermissionDTO> permissions = new HashMap<>();
            permissions.put("MANAGE_IMPORT_TICKET", new PermissionDTO("MANAGE_IMPORT_TICKET", true, true));
            SharedData.userPermissions = permissions;

            EmployeeDTO warehouseStaff = new EmployeeDTO();
            warehouseStaff.setRoleId(4);
            SharedData.currentUser = warehouseStaff;
            assertFalse(PermissionUtil.hasActionPermission("MANAGE_IMPORT_TICKET"));

            EmployeeDTO manager = new EmployeeDTO();
            manager.setRoleId(2);
            SharedData.currentUser = manager;
            assertTrue(PermissionUtil.hasActionPermission("MANAGE_IMPORT_TICKET"));
        } finally {
            SharedData.currentUser = previousUser;
            SharedData.userPermissions = previousPermissions;
        }
    }
}
