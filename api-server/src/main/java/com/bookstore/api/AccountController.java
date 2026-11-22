package com.bookstore.api;

import com.bookstore.api.contract.AccountViewDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.bus.EmployeeBUS;
import com.bookstore.dao.AccountDAO;
import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.EmailUtil;
import com.bookstore.util.PasswordUtil;
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
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private final AccountBUS accountBUS;
    private final AccountDAO accountDAO;
    private final EmployeeDAO employeeDAO;
    private final EmployeeBUS employeeBUS;
    private final AuthSessionStore sessionStore;
    private final BiPredicate<String, String> passwordEmailSender;
    private final Supplier<String> temporaryPasswordGenerator;

    @Autowired
    public AccountController(AuthSessionStore sessionStore) {
        this(sessionStore, new AccountBUS(), new AccountDAO(), new EmployeeDAO(), new EmployeeBUS(),
                EmailUtil::sendNewPassword, EmailUtil::generateTemporaryPassword);
    }

    AccountController(
            AuthSessionStore sessionStore,
            AccountBUS accountBUS,
            AccountDAO accountDAO,
            EmployeeDAO employeeDAO,
            EmployeeBUS employeeBUS,
            BiPredicate<String, String> passwordEmailSender,
            Supplier<String> temporaryPasswordGenerator
    ) {
        this.sessionStore = sessionStore;
        this.accountBUS = accountBUS;
        this.accountDAO = accountDAO;
        this.employeeDAO = employeeDAO;
        this.employeeBUS = employeeBUS;
        this.passwordEmailSender = passwordEmailSender;
        this.temporaryPasswordGenerator = temporaryPasswordGenerator;
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
            @RequestBody(required = false) CreateAccountRequest request
    ) {
        sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        if (request == null || request.username() == null || request.username().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được để trống.");
        }
        String username = request.username().trim();
        if (!EMAIL_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không đúng định dạng.");
        }
        if (username.length() > AccountBUS.MAX_USERNAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được vượt quá 50 ký tự.");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu không được để trống.");
        }
        if (request.password().length() < 4) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu phải có ít nhất 4 ký tự.");
        }
        if (request.employeeId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã nhân viên không hợp lệ.");
        }
        if (request.status() != 0 && request.status() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái tài khoản không hợp lệ.");
        }

        EmployeeDTO targetEmployee = employeeDAO.selectById(request.employeeId());
        if (targetEmployee != null && targetEmployee.getRoleId() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể tạo hoặc thay đổi tài khoản Admin tại đây.");
        }

        boolean eligibleEmployee = employeeBUS.getEmployeesWithoutAccount().stream()
                .anyMatch(employee -> employee.getEmployeeId() == request.employeeId());
        if (!eligibleEmployee) {
            return ApiResponses.fromBusinessMessage("Nhân viên không hợp lệ hoặc đã có tài khoản.");
        }

        AccountDTO account = new AccountDTO(
                username,
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
            @RequestBody(required = false) UpdateAccountRequest request
    ) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        if (employeeId <= 0 || request == null || (request.status() != 0 && request.status() != 1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin tài khoản không hợp lệ.");
        }
        if (session.employee() != null && session.employee().getEmployeeId() == employeeId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể tự chỉnh sửa hoặc khóa tài khoản của chính mình tại đây.");
        }

        boolean changePassword = false;
        String newPassword = request.newPassword();
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được chỉ chứa khoảng trắng.");
            }
            if (newPassword.length() < 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 4 ký tự.");
            }
            changePassword = true;
        }

        AccountDTO account = accountDAO.selectByEmployeeId(employeeId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản nhân viên.");
        }
        EmployeeDTO targetEmployee = employeeDAO.selectById(employeeId);
        if (targetEmployee != null && targetEmployee.getRoleId() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể khóa hoặc chỉnh sửa tài khoản Admin tại đây.");
        }

        if (changePassword) {
            account.setPassword(newPassword);
        }
        account.setStatus(request.status());
        String result = accountBUS.updateAccount(account, changePassword);
        if (isSuccess(result) && (request.status() == 0 || changePassword)) {
            sessionStore.revokeByEmployeeId(employeeId);
        }
        return ApiResponses.fromBusinessMessage(result);
    }

    @PutMapping("/admin")
    public ResponseEntity<String> updateAdminAccount(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) AdminUpdateRequest request
    ) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        if (session.employee() == null || session.employee().getRoleId() != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Chỉ Admin mới có quyền cập nhật tài khoản Admin.");
        }
        if (request == null || request.oldUsername() == null || request.oldUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email hiện tại không được để trống.");
        }
        if (request.newUsername() == null || request.newUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email mới không được để trống.");
        }
        String oldUsername = request.oldUsername().trim();
        String newUsername = request.newUsername().trim();
        if (!EMAIL_PATTERN.matcher(newUsername).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email mới không đúng định dạng.");
        }
        if (newUsername.length() > AccountBUS.MAX_USERNAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email mới không được vượt quá 50 ký tự.");
        }

        boolean changePassword = false;
        String newPassword = request.newPassword();
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được chỉ chứa khoảng trắng.");
            }
            if (newPassword.length() < 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 4 ký tự.");
            }
            changePassword = true;
        }

        AccountDTO account = accountBUS.selectByUsername(oldUsername);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản Admin.");
        }
        EmployeeDTO employee = employeeDAO.selectById(account.getEmployeeId());
        if (employee == null || employee.getRoleId() != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản Admin.");
        }
        if (session.employee().getEmployeeId() > 0 && employee.getEmployeeId() != session.employee().getEmployeeId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể cập nhật tài khoản Admin khác.");
        }

        if (changePassword && PasswordUtil.isSamePassword(newPassword, account.getPassword())) {
            return ApiResponses.fromBusinessMessage("Mật khẩu mới không được trùng với mật khẩu cũ!");
        }
        if (!oldUsername.equalsIgnoreCase(newUsername)) {
            AccountDTO duplicateAccount = accountDAO.selectByUsername(newUsername);
            if (duplicateAccount != null && duplicateAccount.getEmployeeId() != employee.getEmployeeId()) {
                return ApiResponses.fromBusinessMessage("Email này đã được sử dụng bởi người khác!");
            }
            if (employeeDAO.isEmailExist(newUsername, employee.getEmployeeId())) {
                return ApiResponses.fromBusinessMessage("Email này đã được sử dụng bởi người khác!");
            }
        }

        String hashedPassword = changePassword ? PasswordUtil.hash(newPassword) : null;
        boolean updated = accountDAO.updateAdminCredentialsAtomic(
                employee.getEmployeeId(),
                account.getUsername(),
                newUsername,
                hashedPassword
        );
        if (!updated) {
            return ApiResponses.fromBusinessMessage("Cập nhật thất bại. Vui lòng kiểm tra lại hệ thống!");
        }
        if (changePassword || !oldUsername.equals(newUsername)) {
            sessionStore.revokeByEmployeeIdExcept(employee.getEmployeeId(), AuthSessionStore.extractToken(authorization));
        }
        return ApiResponses.fromBusinessMessage("Cập nhật tài khoản thành công!");
    }

    @PostMapping("/{employeeId}/reset-password")
    public ResponseEntity<String> sendNewPassword(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("employeeId") int employeeId
    ) {
        AuthSessionStore.Session session = sessionStore.requireAction(authorization, "MANAGE_ACCOUNT");
        if (employeeId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã nhân viên không hợp lệ.");
        }
        if (session.employee() != null && session.employee().getEmployeeId() == employeeId) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể tự đặt lại mật khẩu của chính mình tại đây.");
        }

        AccountDTO account = accountDAO.selectByEmployeeId(employeeId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản nhân viên.");
        }
        EmployeeDTO targetEmployee = employeeDAO.selectById(employeeId);
        if (targetEmployee != null && targetEmployee.getRoleId() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Không thể đặt lại mật khẩu tài khoản Admin tại đây.");
        }

        String previousPasswordHash = account.getPassword();
        String temporaryPassword = temporaryPasswordGenerator.get();
        account.setPassword(temporaryPassword);
        String updateResult = accountBUS.updateAccount(account, true);
        if (!isSuccess(updateResult)) {
            return ApiResponses.fromBusinessMessage(updateResult);
        }

        try {
            if (!passwordEmailSender.test(account.getUsername(), temporaryPassword)) {
                accountDAO.restorePasswordHash(account.getUsername(), previousPasswordHash);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không thể gửi email.");
            }
        } catch (IllegalStateException e) {
            accountDAO.restorePasswordHash(account.getUsername(), previousPasswordHash);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage(), e);
        } catch (RuntimeException e) {
            if (e instanceof ResponseStatusException rse) {
                throw rse;
            }
            accountDAO.restorePasswordHash(account.getUsername(), previousPasswordHash);
            throw e;
        }

        sessionStore.revokeByEmployeeId(employeeId);
        return ApiResponses.fromBusinessMessage(updateResult);
    }

    private boolean isSuccess(String message) {
        return message != null && ("ok".equalsIgnoreCase(message) || message.toLowerCase(Locale.ROOT).contains("thành công"));
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
            String username,
            String password,
            int employeeId,
            int status
    ) { }

    public record UpdateAccountRequest(
            String newPassword,
            int status
    ) { }

    public record AdminUpdateRequest(
            String oldUsername,
            String newUsername,
            String newPassword
    ) { }
}
