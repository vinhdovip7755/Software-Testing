package com.bookstore.api;

import com.bookstore.api.contract.ApiSessionDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.PermissionBUS;
import com.bookstore.dto.EmployeeDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountBUS accountBUS = new AccountBUS();
    private final PermissionBUS permissionBUS = new PermissionBUS();
    private final AuthSessionStore sessionStore;

    public AuthController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            EmployeeDTO employee = accountBUS.login(request.username(), request.password());
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Tài khoản không gắn với nhân viên hợp lệ."));
            }
            Map<String, com.bookstore.dto.PermissionDTO> permissions =
                    permissionBUS.getPermissionsByRoleId(employee.getRoleId());
            String token = sessionStore.createToken(employee, permissions);
            return ResponseEntity.ok(new ApiSessionDTO(token, employee, permissions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage() == null ? "Đăng nhập thất bại." : e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        sessionStore.remove(authorization.substring("Bearer ".length()));
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(
            @NotBlank(message = "Email không được để trống.") String username,
            @NotBlank(message = "Mật khẩu không được để trống.") String password
    ) {
    }
}
