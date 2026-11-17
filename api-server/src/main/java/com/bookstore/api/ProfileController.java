package com.bookstore.api;

import com.bookstore.api.contract.ProfileDTO;
import com.bookstore.api.contract.ProfileUpdateRequestDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dao.AccountDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.EmployeeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final AuthSessionStore sessionStore;
    private final AccountBUS accountBUS = new AccountBUS();
    private final AccountDAO accountDAO = new AccountDAO();
    private final EmployeeBUS employeeBUS = new EmployeeBUS();

    public ProfileController(AuthSessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @GetMapping
    public ProfileDTO getProfile(@RequestHeader("Authorization") String authorization) {
        EmployeeDTO employee = sessionStore.requireAuthenticated(authorization).employee();
        AccountDTO account = findAccount(employee.getEmployeeId());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        }
        return new ProfileDTO(employee, account.getUsername());
    }

    @PutMapping
    public ProfileDTO updateProfile(
            @RequestHeader("Authorization") String authorization,
            @RequestBody ProfileUpdateRequestDTO request
    ) {
        AuthSessionStore.Session session = sessionStore.requireAuthenticated(authorization);
        EmployeeDTO current = session.employee();
        AccountDTO account = findAccount(current.getEmployeeId());
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        }
        if (request == null || request.employeeName() == null || request.employeePhone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin hồ sơ không hợp lệ.");
        }

        String username = request.username() == null || request.username().isBlank()
                ? account.getUsername()
                : request.username().trim();
        if (!EMAIL_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không đúng định dạng.");
        }
        String newPassword = request.newPassword() == null ? "" : request.newPassword().trim();
        if (!newPassword.isEmpty() && newPassword.length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 4 ký tự.");
        }
        if (!newPassword.isEmpty() && newPassword.equals(account.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mật khẩu mới không được trùng với mật khẩu cũ.");
        }
        AccountDTO duplicate = accountDAO.selectByUsername(username);
        if (duplicate != null && !duplicate.getUsername().equals(account.getUsername())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Email này đã được sử dụng bởi người khác.");
        }

        EmployeeDTO updated = new EmployeeDTO(
                current.getEmployeeId(),
                request.employeeName().trim(),
                request.employeePhone().trim(),
                username,
                request.birthday() == null ? current.getBirthday() : request.birthday(),
                current.getBaseSalary(),
                current.getSalaryFactor(),
                current.getDayIn(),
                current.getStatus(),
                current.getRoleId(),
                current.getRoleName()
        );
        String employeeResult = employeeBUS.updateEmployee(updated);
        if (!employeeResult.toLowerCase().contains("thành công")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, employeeResult);
        }

        String usernameResult = accountBUS.updateUsername(account.getUsername(), username);
        if (!"OK".equals(usernameResult)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, usernameResult);
        }
        if (!newPassword.isEmpty()) {
            account.setUsername(username);
            account.setPassword(newPassword);
            String passwordResult = accountBUS.updateAccount(account, true);
            if (!passwordResult.toLowerCase().contains("thành công")) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, passwordResult);
            }
        }

        current.setEmployeeName(updated.getEmployeeName());
        current.setEmployeePhone(updated.getEmployeePhone());
        current.setEmail(updated.getEmail());
        current.setBirthday(updated.getBirthday());
        return new ProfileDTO(current, username);
    }

    private AccountDTO findAccount(int employeeId) {
        return accountDAO.selectAllAccounts().stream()
                .filter(account -> account.getEmployeeId() == employeeId)
                .findFirst()
                .orElse(null);
    }
}
