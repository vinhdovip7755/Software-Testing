package com.bookstore.api;

import com.bookstore.api.contract.CustomerPurchaseSummaryDTO;
import com.bookstore.bus.CustomerBUS;
import com.bookstore.dto.CustomerDTO;
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

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerBUS customerBUS = new CustomerBUS();
    private final AuthSessionStore sessionStore;

    public CustomerController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<CustomerDTO> getCustomers(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_CUSTOMER");
        return customerBUS.selectAllCustomers();
    }

    @GetMapping("/phone/{phone}")
    public CustomerDTO getCustomerByPhone(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("phone") String phone
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_CUSTOMER", "MANAGE_SELLING");
        CustomerDTO customer = customerBUS.selectByPhone(phone);
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng.");
        }
        return customer;
    }

    @GetMapping("/{id}/statistics")
    public CustomerPurchaseSummaryDTO getCustomerStatistics(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id
    ) {
        sessionStore.requireAnyView(authorization, "MANAGE_CUSTOMER");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã khách hàng không hợp lệ.");
        }
        Object[] statistics = customerBUS.getCustomerStatistics(id);
        return new CustomerPurchaseSummaryDTO(
                ((Number) statistics[0]).intValue(),
                ((Number) statistics[1]).doubleValue(),
                (Timestamp) statistics[2]
        );
    }

    @PostMapping
    public ResponseEntity<String> addCustomer(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CustomerDTO customer
    ) {
        sessionStore.requireAnyAction(authorization, "MANAGE_CUSTOMER", "MANAGE_SELLING");
        validateCustomer(customer);
        return ApiResponses.fromBusinessMessage(customerBUS.insertCustomer(customer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCustomer(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") int id,
            @RequestBody CustomerDTO customer
    ) {
        sessionStore.requireAction(authorization, "MANAGE_CUSTOMER");
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã khách hàng không hợp lệ.");
        }
        validateCustomer(customer);
        customer.setCustomerId(id);
        return ApiResponses.fromBusinessMessage(customerBUS.updateCustomerInfo(customer));
    }

    private void validateCustomer(CustomerDTO customer) {
        if (customer == null || customer.getCustomerName() == null || customer.getCustomerPhone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên và số điện thoại khách hàng là bắt buộc.");
        }
    }
}
