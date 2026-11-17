package com.bookstore.api.contract;

import java.util.List;

public class RoleSaveRequestDTO {
    private String roleName;
    private List<RoleActionPermissionDTO> permissions;

    public RoleSaveRequestDTO() {
    }

    public RoleSaveRequestDTO(String roleName, List<RoleActionPermissionDTO> permissions) {
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<RoleActionPermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<RoleActionPermissionDTO> permissions) {
        this.permissions = permissions;
    }
}
