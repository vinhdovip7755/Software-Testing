package com.bookstore.api.contract;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;

import java.util.Map;

public class ApiSessionDTO {
    private String accessToken;
    private EmployeeDTO employee;
    private Map<String, PermissionDTO> permissions;

    public ApiSessionDTO() {
    }

    public ApiSessionDTO(String accessToken, EmployeeDTO employee, Map<String, PermissionDTO> permissions) {
        this.accessToken = accessToken;
        this.employee = employee;
        this.permissions = permissions;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public EmployeeDTO getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeDTO employee) {
        this.employee = employee;
    }

    public Map<String, PermissionDTO> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, PermissionDTO> permissions) {
        this.permissions = permissions;
    }
}
