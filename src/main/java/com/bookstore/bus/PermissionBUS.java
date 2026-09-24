package com.bookstore.bus;

import com.bookstore.dao.PermissionDAO;
import com.bookstore.dto.PermissionDTO;

import java.util.List;
import java.util.Map;

public class PermissionBUS {
    PermissionDAO permissionDAO = new PermissionDAO();

    public Map<String, PermissionDTO> getPermissionsByRoleId(int roleId) {
        return permissionDAO.getPermissionsByRoleId(roleId);
    }

    public boolean saveRolePermissions(int roleId, List<PermissionDTO> newPerms, List<Integer> actionIds) {
        return permissionDAO.saveRolePermissions(roleId, newPerms, actionIds);
    }
}
