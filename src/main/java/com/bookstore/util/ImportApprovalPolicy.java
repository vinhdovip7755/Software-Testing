package com.bookstore.util;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;

import java.util.Map;

public final class ImportApprovalPolicy {
    public static final String ACTION_MANAGE_IMPORT_TICKET = "MANAGE_IMPORT_TICKET";
    private static final int PENDING_STATUS = 1;

    private ImportApprovalPolicy() {
    }

    public static boolean canAct(EmployeeDTO actor, Map<String, PermissionDTO> permissions, int creatorId, int status) {
        if (actor == null || actor.getStatus() != 1 || status != PENDING_STATUS) {
            return false;
        }
        if (actor.getEmployeeId() <= 0 || creatorId <= 0 || actor.getEmployeeId() == creatorId) {
            return false;
        }
        if (permissions == null) {
            return false;
        }
        PermissionDTO permission = permissions.get(ACTION_MANAGE_IMPORT_TICKET);
        return permission != null && permission.isAction();
    }

    public static boolean canAct(EmployeeDTO actor, int creatorId, int status) {
        if (actor == null || actor.getStatus() != 1 || status != PENDING_STATUS) {
            return false;
        }
        if (actor.getEmployeeId() <= 0 || creatorId <= 0 || actor.getEmployeeId() == creatorId) {
            return false;
        }
        if (SharedData.userPermissions != null) {
            return canAct(actor, SharedData.userPermissions, creatorId, status);
        }
        return actor.getRoleId() == 1 || actor.getRoleId() == 2;
    }

    public static boolean canAct(int roleId, int actorId, int creatorId) {
        if (actorId <= 0 || creatorId <= 0 || actorId == creatorId) {
            return false;
        }
        if (SharedData.userPermissions != null) {
            PermissionDTO permission = SharedData.userPermissions.get(ACTION_MANAGE_IMPORT_TICKET);
            return permission != null && permission.isAction();
        }
        return roleId == 1 || roleId == 2;
    }
}
