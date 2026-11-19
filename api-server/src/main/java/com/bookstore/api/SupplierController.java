package com.bookstore.api;

import com.bookstore.bus.SupplierBUS;
import com.bookstore.dto.SupplierDTO;
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

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final SupplierBUS supplierBUS = new SupplierBUS();
    private final AuthSessionStore sessionStore;

    public SupplierController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<SupplierDTO> getSuppliers(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_SUPPLIER", "MANAGE_PRODUCT", "MANAGE_IMPORT");
        return supplierBUS.selectAll();
    }

    @GetMapping("/{id}")
    public SupplierDTO getSupplier(@RequestHeader("Authorization") String authorization, @PathVariable("id") int id) {
        sessionStore.requireAnyView(authorization, "MANAGE_SUPPLIER", "MANAGE_PRODUCT", "MANAGE_IMPORT",
                "MANAGE_SELLING");
        SupplierDTO supplier = supplierBUS.getById(id);
        if (supplier == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhà cung cấp.");
        }
        return supplier;
    }

    @PostMapping
    public ResponseEntity<String> addSupplier(
            @RequestHeader("Authorization") String authorization,
            @RequestBody SupplierDTO supplier
    ) {
        sessionStore.requireAction(authorization, "MANAGE_SUPPLIER");
        return ApiResponses.fromBusinessMessage(supplierBUS.addSupplier(supplier));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateSupplier(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id,
            @RequestBody SupplierDTO supplier
    ) {
        sessionStore.requireAction(authorization, "MANAGE_SUPPLIER");
        supplier.setSupplierId(id);
        return ApiResponses.fromBusinessMessage(supplierBUS.updateSupplier(supplier));
    }
}
