package com.bookstore.api;

import com.bookstore.api.contract.ProfileDTO;
import com.bookstore.api.contract.ProfileUpdateRequestDTO;
import com.bookstore.bus.AccountBUS;
import com.bookstore.dao.AccountDAO;
import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dto.AccountDTO;
import com.bookstore.dto.EmployeeDTO;
import com.bookstore.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");

    private final AuthSessionStore sessionStore;
    private final AccountDAO accountDAO;
    private final EmployeeDAO employeeDAO;

    @Autowired
    public ProfileController(AuthSessionStore sessionStore) {
        this(sessionStore, new AccountDAO(), new EmployeeDAO());
    }

    ProfileController(AuthSessionStore sessionStore, AccountDAO accountDAO, EmployeeDAO employeeDAO) {
        this.sessionStore = sessionStore;
        this.accountDAO = accountDAO;
        this.employeeDAO = employeeDAO;
    }

    @GetMapping
    public ProfileDTO getProfile(@RequestHeader("Authorization") String authorization) {
        AuthSessionStore.Session session = sessionStore.requireAuthenticated(authorization);
        int employeeId = session.employee().getEmployeeId();
        EmployeeDTO employee = employeeDAO.selectById(employeeId);
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin nhân viên.");
        }
        if (employee.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản này đã bị khóa.");
        }
        AccountDTO account = accountDAO.selectByEmployeeId(employeeId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        }
        if (account.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản này đã bị khóa.");
        }
        return new ProfileDTO(employee, account.getUsername());
    }

    @PutMapping
    public ProfileDTO updateProfile(
            @RequestHeader("Authorization") String authorization,
            @RequestBody(required = false) ProfileUpdateRequestDTO request
    ) {
        AuthSessionStore.Session session = sessionStore.requireAuthenticated(authorization);
        int employeeId = session.employee().getEmployeeId();

        EmployeeDTO current = employeeDAO.selectById(employeeId);
        if (current == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin nhân viên.");
        }
        if (current.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản này đã bị khóa.");
        }
        AccountDTO account = accountDAO.selectByEmployeeId(employeeId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        }
        if (account.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản này đã bị khóa.");
        }

        if (request == null || request.employeeName() == null || request.employeePhone() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin hồ sơ không hợp lệ.");
        }

        String name = request.employeeName().trim();
        if (name.isEmpty() || name.length() > 100 || !NAME_PATTERN.matcher(name).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên nhân viên không hợp lệ.");
        }

        String phone = request.employeePhone().trim();
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số điện thoại phải có 10 số và bắt đầu bằng 0.");
        }
        if (employeeDAO.isPhoneExist(phone, employeeId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Số điện thoại này đã tồn tại trong hệ thống.");
        }

        String username = request.username() == null || request.username().isBlank()
                ? account.getUsername()
                : request.username().trim();
        if (!EMAIL_PATTERN.matcher(username).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không đúng định dạng.");
        }
        if (username.length() > AccountBUS.MAX_USERNAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được vượt quá 50 ký tự.");
        }

        Date birthday = request.birthday() == null ? current.getBirthday() : request.birthday();
        if (birthday != null && birthday.toLocalDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ngày sinh không hợp lệ.");
        }

        String rawNewPassword = request.newPassword();
        String newPassword = "";
        if (rawNewPassword != null && !rawNewPassword.isEmpty()) {
            if (rawNewPassword.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được chỉ chứa khoảng trắng.");
            }
            newPassword = rawNewPassword.trim();
            if (newPassword.length() < 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 4 ký tự.");
            }
            if (PasswordUtil.isSamePassword(newPassword, account.getPassword())) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Mật khẩu mới không được trùng với mật khẩu cũ.");
            }
        }

        if (!username.equalsIgnoreCase(account.getUsername())) {
            AccountDTO duplicateAccount = accountDAO.selectByUsername(username);
            if (duplicateAccount != null && duplicateAccount.getEmployeeId() != employeeId) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Email này đã được sử dụng bởi người khác.");
            }
            if (employeeDAO.isEmailExist(username, employeeId)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Email này đã được sử dụng bởi người khác.");
            }
        }

        String hashedNewPassword = newPassword.isEmpty() ? null : PasswordUtil.hash(newPassword);
        boolean updated = employeeDAO.updateSelfProfileAtomic(
                employeeId,
                name,
                phone,
                username,
                birthday,
                account.getUsername(),
                username,
                hashedNewPassword
        );
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cập nhật hồ sơ thất bại.");
        }

        if (hashedNewPassword != null || !username.equals(account.getUsername())) {
            sessionStore.revokeByEmployeeIdExcept(employeeId, AuthSessionStore.extractToken(authorization));
        }

        EmployeeDTO refreshed = employeeDAO.selectById(employeeId);
        return new ProfileDTO(refreshed != null ? refreshed : current, username);
    }
}
