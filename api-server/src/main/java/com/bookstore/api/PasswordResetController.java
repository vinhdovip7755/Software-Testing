package com.bookstore.api;

import com.bookstore.api.contract.PasswordResetRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<Void> requestOtp(@RequestBody(required = false) PasswordResetRequestDTO request) {
        passwordResetService.requestOtp(request == null ? null : request.email());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmReset(@RequestBody(required = false) PasswordResetRequestDTO request) {
        passwordResetService.confirmReset(
                request == null ? null : request.email(),
                request == null ? null : request.otp(),
                request == null ? null : request.newPassword()
        );
        return ResponseEntity.noContent().build();
    }
}
