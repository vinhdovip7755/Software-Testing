package com.bookstore.util;

import com.bookstore.dto.PermissionDTO;

public class PermissionUtil {
    public static boolean hasViewPermission(String actionCode) {
        if (SharedData.userPermissions == null) return false;
        PermissionDTO perm = SharedData.userPermissions.get(actionCode);
        return perm != null && perm.isView();
    }

    public static boolean hasActionPermission(String actionCode) {
        if (SharedData.userPermissions == null) return false;
        PermissionDTO perm = SharedData.userPermissions.get(actionCode);
        return perm != null && perm.isAction();
    }
}