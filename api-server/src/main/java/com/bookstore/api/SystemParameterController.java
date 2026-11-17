package com.bookstore.api;

import com.bookstore.bus.SystemParameterBUS;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system-parameters")
public class SystemParameterController {
    private final AuthSessionStore sessionStore;

    public SystemParameterController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping("/vat")
    public double getVatRate(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_SELLING");
        return new SystemParameterBUS().getVAT();
    }
}
