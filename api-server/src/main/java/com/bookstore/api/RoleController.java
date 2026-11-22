package com.bookstore.api;

import com.bookstore.api.contract.RoleActionPermissionDTO;
import com.bookstore.bus.ActionBUS;
import com.bookstore.bus.PermissionBUS;
import com.bookstore.bus.RoleBUS;
import com.bookstore.dao.RoleDAO;
import com.bookstore.dto.ActionDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.dto.RoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private static final Pattern ROLE_NAME_PATTERN =
            Pattern.compile("^(?=.*\\p{L})[\\p{L}0-9\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$");
    private static final int MAX_ROLE_NAME_LENGTH = 50;

    private final RoleBUS roleBUS;
    private final ActionBUS actionBUS;
    private final PermissionBUS permissionBUS;
    private final RoleDAO roleDAO;
    private final AuthSessionStore sessionStore;

    @Autowired
    public RoleController(AuthSessionStore sessionStore) {
        this(sessionStore, new RoleBUS(), new ActionBUS(), new PermissionBUS(), new RoleDAO());
    }

    RoleController(
            AuthSessionStore sessionStore,
            RoleBUS roleBUS,
            ActionBUS actionBUS,
            PermissionBUS permissionBUS,
            RoleDAO roleDAO
    ) {
        this.sessionStore = sessionStore;
        this.roleBUS = roleBUS;
        this.actionBUS = actionBUS;
        this.permissionBUS = permissionBUS;
        this.roleDAO = roleDAO;
    }

    @GetMapping
    public List<RoleDTO> getRoles(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_ROLE", "MANAGE_EMPLOYEE", "MANAGE_ACCOUNT");
        return roleBUS.getAllRoles();
    }

    @GetMapping("/{roleId}/permissions")
    public List<RoleActionPermissionDTO> getRolePermissions(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("roleId") int roleId
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_ROLE");
        if (roleId < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã chức vụ không hợp lệ.");
        }
        Map<String, PermissionDTO> permissions = permissionBUS.getPermissionsByRoleId(roleId);
        List<RoleActionPermissionDTO> result = new ArrayList<>();
        for (ActionDTO action : actionBUS.selectAllActions()) {
            PermissionDTO permission = permissions.get(action.getActionCode());
            result.add(new RoleActionPermissionDTO(
                    action.getActionId(),
                    action.getActionCode(),
                    action.getActionName(),
                    permission != null && permission.isView(),
                    permission != null && permission.isAction()
            ));
        }
        return result;
    }

    @PostMapping
    public ResponseEntity<String> createRole(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) RoleSaveRequest request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ROLE");
        String trimmedName = validateRoleRequest(request);
        PreparedPermissions permissions = preparePermissions(request.permissions());
        RoleDTO role = new RoleDTO(0, trimmedName);
        int roleId = roleDAO.insertRoleWithPermissions(role, permissions.permissions(), permissions.actionIds());
        if (roleId <= 0) {
            return ApiResponses.fromBusinessMessage("Thêm thất bại! Tên chức vụ có thể đã tồn tại.");
        }
        return ApiResponses.fromBusinessMessage("Thêm chức vụ mới thành công!");
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<String> updateRole(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("roleId") int roleId,
            @RequestBody(required = false) RoleSaveRequest request
    ) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_ROLE");
        if (roleId <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chức vụ mặc định không thể chỉnh sửa.");
        }
        if (session.employee() != null && session.employee().getRoleId() == roleId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể tự chỉnh sửa phân quyền chức vụ của chính mình.");
        }
        String trimmedName = validateRoleRequest(request);
        PreparedPermissions permissions = preparePermissions(request.permissions());
        RoleDTO role = new RoleDTO(roleId, trimmedName);
        if (!roleDAO.updateRoleWithPermissions(role, permissions.permissions(), permissions.actionIds())) {
            return ApiResponses.fromBusinessMessage("Cập nhật tên chức vụ hoặc phân quyền thất bại!");
        }
        sessionStore.revokeByRoleId(roleId);
        return ApiResponses.fromBusinessMessage("Cập nhật phân quyền thành công!");
    }

    private String validateRoleRequest(RoleSaveRequest request) {
        if (request == null || request.roleName() == null || request.roleName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên chức vụ không được để trống.");
        }
        String trimmedName = request.roleName().trim();
        if (trimmedName.length() > MAX_ROLE_NAME_LENGTH || !ROLE_NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên chức vụ không hợp lệ.");
        }
        if ("admin".equalsIgnoreCase(trimmedName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Không thể sử dụng tên chức vụ Admin.");
        }
        if (request.permissions() == null || request.permissions().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách quyền không được để trống.");
        }
        return trimmedName;
    }

    private PreparedPermissions preparePermissions(List<RoleActionPermissionDTO> requestedPermissions) {
        List<ActionDTO> actions = actionBUS.selectAllActions();
        if (requestedPermissions.size() != actions.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách quyền không đầy đủ.");
        }

        Map<Integer, RoleActionPermissionDTO> byId = new HashMap<>();
        Set<Integer> seenIds = new HashSet<>();
        for (RoleActionPermissionDTO permission : requestedPermissions) {
            if (permission == null || !seenIds.add(permission.getActionId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách quyền không hợp lệ.");
            }
            byId.put(permission.getActionId(), permission);
        }

        List<PermissionDTO> permissions = new ArrayList<>();
        List<Integer> actionIds = new ArrayList<>();
        for (ActionDTO action : actions) {
            RoleActionPermissionDTO requested = byId.get(action.getActionId());
            if (requested == null || !action.getActionCode().equals(requested.getActionCode())
                    || (requested.isCanAction() && !requested.isCanView())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách quyền không hợp lệ.");
            }
            permissions.add(new PermissionDTO(
                    action.getActionCode(),
                    requested.isCanView(),
                    requested.isCanAction()
            ));
            actionIds.add(action.getActionId());
        }

        return new PreparedPermissions(permissions, actionIds);
    }

    private record PreparedPermissions(List<PermissionDTO> permissions, List<Integer> actionIds) {
    }

    public record RoleSaveRequest(
            String roleName,
            List<RoleActionPermissionDTO> permissions
    ) {
    }
}
