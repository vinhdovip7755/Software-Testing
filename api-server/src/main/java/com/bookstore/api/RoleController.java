package com.bookstore.api;

import com.bookstore.api.contract.RoleActionPermissionDTO;
import com.bookstore.bus.ActionBUS;
import com.bookstore.bus.PermissionBUS;
import com.bookstore.bus.RoleBUS;
import com.bookstore.dto.ActionDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.dto.RoleDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private static final String ROLE_NAME_PATTERN =
            "^(?=.*\\p{L})[\\p{L}0-9\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$";

    private final RoleBUS roleBUS = new RoleBUS();
    private final ActionBUS actionBUS = new ActionBUS();
    private final PermissionBUS permissionBUS = new PermissionBUS();
    private final AuthSessionStore sessionStore;

    public RoleController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
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
            @Valid @RequestBody RoleSaveRequest request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ROLE");
        PreparedPermissions permissions = preparePermissions(request.permissions());
        RoleDTO role = new RoleDTO(0, request.roleName().trim());
        int roleId = roleBUS.insertRole(role);
        if (roleId <= 0) {
            return ApiResponses.fromBusinessMessage("Thêm thất bại! Tên chức vụ có thể đã tồn tại.");
        }
        persistPermissions(roleId, permissions);
        return ApiResponses.fromBusinessMessage("Thêm chức vụ mới thành công!");
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<String> updateRole(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("roleId") int roleId,
            @Valid @RequestBody RoleSaveRequest request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ROLE");
        if (roleId <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chức vụ mặc định không thể chỉnh sửa.");
        }
        PreparedPermissions permissions = preparePermissions(request.permissions());
        RoleDTO role = new RoleDTO(roleId, request.roleName().trim());
        if (!roleBUS.updateRole(role)) {
            return ApiResponses.fromBusinessMessage("Cập nhật tên chức vụ thất bại!");
        }
        persistPermissions(roleId, permissions);
        return ApiResponses.fromBusinessMessage("Cập nhật phân quyền thành công!");
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

    private void persistPermissions(int roleId, PreparedPermissions prepared) {
        if (!permissionBUS.saveRolePermissions(roleId, prepared.permissions(), prepared.actionIds())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể lưu phân quyền.");
        }
    }

    private record PreparedPermissions(List<PermissionDTO> permissions, List<Integer> actionIds) {
    }

    public record RoleSaveRequest(
            @NotBlank(message = "Tên chức vụ không được để trống.")
            @Pattern(regexp = ROLE_NAME_PATTERN, message = "Tên chức vụ không hợp lệ.")
            String roleName,
            @NotNull(message = "Danh sách quyền không được để trống.")
            @Size(min = 1, message = "Danh sách quyền không được để trống.")
            List<@Valid RoleActionPermissionDTO> permissions
    ) {
    }
}
