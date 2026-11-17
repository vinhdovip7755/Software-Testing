package com.bookstore.util;

import com.bookstore.dto.PermissionDTO;

public class PermissionUtil {
    public static boolean hasViewPermission(String actionCode) {
        if ("MANAGE_IMPORT_TICKET".equals(actionCode)
                && SharedData.currentUser != null
                && SharedData.currentUser.getRoleId() == 1) {
            return true;
        }
        if (SharedData.userPermissions == null) return false;
        PermissionDTO perm = SharedData.userPermissions.get(actionCode);
        return perm != null && perm.isView();
    }

    public static boolean hasActionPermission(String actionCode) {
        if ("MANAGE_IMPORT_TICKET".equals(actionCode)) {
            if (SharedData.currentUser == null || SharedData.currentUser.getRoleId() > 2) {
                return false;
            }
            if (SharedData.currentUser.getRoleId() == 1) {
                return true;
            }
        }
        if (SharedData.userPermissions == null) return false;
        PermissionDTO perm = SharedData.userPermissions.get(actionCode);
        return perm != null && perm.isAction();
    }
}