package com.bookstore.api;

import com.bookstore.api.contract.AccountViewDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dao.AccountDAO;
import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.EmailUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
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
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountBUS accountBUS = new AccountBUS();
    private final AccountDAO accountDAO = new AccountDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final AuthSessionStore sessionStore;

    public AccountController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public List<AccountViewDTO> getAccounts(@RequestHeader("Authorization") String authorization) {
        sessionStore.requireAnyView(authorization, "MANAGE_ACCOUNT");
        List<AccountViewDTO> result = new ArrayList<>();
        for (AccountDTO account : accountDAO.selectAllAccounts()) {
            EmployeeDTO employee = employeeDAO.selectById(account.getEmployeeId());
            result.add(toView(account, employee));
        }
        return result;
    }

    @PostMapping
    public ResponseEntity<String> createAccount(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CreateAccountRequest request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        if (request.status() != 0 && request.status() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái tài khoản không hợp lệ.");
        }
        boolean eligibleEmployee = employeeBUS.getEmployeesWithoutAccount().stream()
                .anyMatch(employee -> employee.getEmployeeId() == request.employeeId());
        if (!eligibleEmployee) {
            return ApiResponses.fromBusinessMessage("Nhân viên không hợp lệ hoặc đã có tài khoản.");
        }

        AccountDTO account = new AccountDTO(
                request.username().trim(),
                request.password(),
                request.employeeId(),
                request.status()
        );
        return ApiResponses.fromBusinessMessage(accountBUS.addAccount(account));
    }

    @PutMapping("/{employeeId}")
    public ResponseEntity<String> updateAccount(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("employeeId") int employeeId,
            @Valid @RequestBody UpdateAccountRequest request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        if (employeeId <= 0 || (request.status() != 0 && request.status() != 1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin tài khoản không hợp lệ.");
        }
        AccountDTO account = findAccountByEmployeeId(employeeId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản nhân viên.");
        }
        boolean changePassword = request.newPassword() != null && !request.newPassword().isBlank();
        if (changePassword) {
            if (request.newPassword().length() < 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 4 ký tự.");
            }
            account.setPassword(request.newPassword());
        }
        account.setStatus(request.status());
        return ApiResponses.fromBusinessMessage(accountBUS.updateAccount(account, changePassword));
    }

    @PutMapping("/admin")
    public ResponseEntity<String> updateAdminAccount(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody AdminUpdateRequest request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        AccountDTO account = accountBUS.selectByUsername(request.oldUsername());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản Admin.");
        }
        EmployeeDTO employee = employeeDAO.selectById(account.getEmployeeId());
        if (employee == null || employee.getRoleId() != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản Admin.");
        }
        boolean changePassword = request.newPassword() != null && !request.newPassword().isBlank();
        if (changePassword && request.newPassword().length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 4 ký tự.");
        }

        String usernameResult = accountBUS.updateUsername(request.oldUsername(), request.newUsername().trim());
        if (!"OK".equals(usernameResult)) {
            return ApiResponses.fromBusinessMessage(usernameResult);
        }
        account.setUsername(request.newUsername().trim());
        if (changePassword) {
            account.setPassword(request.newPassword());
        }
        return ApiResponses.fromBusinessMessage(accountBUS.updateAccount(account, changePassword));
    }

    @PostMapping("/{employeeId}/reset-password")
    public ResponseEntity<String> sendNewPassword(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("employeeId") int employeeId
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        AccountDTO account = findAccountByEmployeeId(employeeId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản nhân viên.");
        }

        String temporaryPassword = EmailUtil.generateOTP();
        try {
            if (!EmailUtil.sendNewPassword(account.getUsername(), temporaryPassword)) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi email.");
            }
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
        }

        account.setPassword(temporaryPassword);
        return ApiResponses.fromBusinessMessage(accountBUS.updateAccount(account, true));
    }

    private AccountDTO findAccountByEmployeeId(int employeeId) {
        return accountDAO.selectAllAccounts().stream()
                .filter(account -> account.getEmployeeId() == employeeId)
                .findFirst()
                .orElse(null);
    }

    private AccountViewDTO toView(AccountDTO account, EmployeeDTO employee) {
        return new AccountViewDTO(
                account.getEmployeeId(),
                account.getUsername(),
                account.getStatus(),
                employee == null ? "N/A" : employee.getEmployeeName(),
                employee == null ? 0 : employee.getRoleId(),
                employee == null ? "N/A" : employee.getRoleName()
        );
    }

    public record CreateAccountRequest(
            @NotBlank(message = "Email không được để trống.")
            @Email(message = "Email không đúng định dạng.")
            @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                    message = "Email không đúng định dạng.")
            String username,
            @NotBlank(message = "Mật khẩu không được để trống.")
            @Size(min = 4, message = "Mật khẩu phải có ít nhất 4 ký tự.")
            String password,
            @Positive(message = "Mã nhân viên không hợp lệ.")
            int employeeId,
            int status
    ) { }

    public record UpdateAccountRequest(
            String newPassword,
            int status
    ) { }

    public record AdminUpdateRequest(
            @NotBlank(message = "Email hiện tại không được để trống.")
            String oldUsername,
            @NotBlank(message = "Email mới không được để trống.")
            @Email(message = "Email mới không đúng định dạng.")
            @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                    message = "Email mới không đúng định dạng.")
            String newUsername,
            String newPassword
    ) { }
}
