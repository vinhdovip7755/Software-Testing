package com.bookstore.util;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;

import java.util.HashMap;
import java.util.Map;

public class SharedData {
    public static EmployeeDTO currentUser;
    public static Map<String, PermissionDTO> userPermissions = new HashMap<>();
}
