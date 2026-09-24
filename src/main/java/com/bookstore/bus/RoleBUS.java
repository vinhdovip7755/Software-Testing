package com.bookstore.bus;

import com.bookstore.dao.RoleDAO;
import com.bookstore.dto.RoleDTO;

import java.util.List;

public class RoleBUS {
    private final RoleDAO roleDAO = new RoleDAO();

    public List<RoleDTO> getAllRoles() {
        return roleDAO.selectAllRoles();
    }

    public int insertRole(com.bookstore.dto.RoleDTO role) {
        return roleDAO.insertRole(role);
    }

    public boolean updateRole(RoleDTO role) {
        return roleDAO.updateRole(role);
    }
}