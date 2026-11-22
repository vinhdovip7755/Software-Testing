import com.bookstore.bus.AccountBUS;
import com.bookstore.gui.main.LoginFrame;
import com.bookstore.gui.main.MainFrame;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class BookstoreUiTest {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final String CTRL_A_BACKSPACE = "\uE009a\uE000\uE003";
    private static final String CTRL_HOME = "\uE009\uE011\uE000";
    private static final String KEY_DOWN = "\uE015";

    private final boolean swingDirect;
    private final long stepDelayMs;
    private WinKeys desktop;
    private LoginFrame login;

    public BookstoreUiTest() {
        String driver = System.getProperty("ui.driver",
                System.getenv().getOrDefault("BOOKSTORE_UI_DRIVER", "winappdriver"));
        this.swingDirect = "swing".equalsIgnoreCase(driver);
        long parsedDelay = 700L;
        try {
            parsedDelay = Math.max(0L, Long.parseLong(System.getProperty("ui.delayMs", "700")));
        } catch (NumberFormatException ignored) {
        }
        this.stepDelayMs = parsedDelay;
    }

    private void pauseStep() throws InterruptedException {
        if (stepDelayMs > 0) Thread.sleep(stepDelayMs);
    }

    private void pauseBrief() throws InterruptedException {
        if (stepDelayMs > 0) Thread.sleep(Math.min(300L, stepDelayMs / 2));
    }

    // All Swing lookups, focus requests and observations run on its event thread.
    private static <T> T ui(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        SwingUtilities.invokeAndWait(task);
        return task.get();
    }

    private static <T> T await(String description, Callable<T> probe) throws Exception {
        long end = System.nanoTime() + 30_000_000_000L;
        do {
            T found = ui(probe);
            if (found != null) return found;
            Thread.sleep(40);
        } while (System.nanoTime() < end);
        throw new AssertionError("Hết thời gian chờ: " + description);
    }

    private static <T extends Component> T find(Component node, Class<T> type, Predicate<T> rule) {
        if (!node.isShowing()) return null;
        if (type.isInstance(node) && rule.test(type.cast(node))) return type.cast(node);
        if (node instanceof Container parent) {
            for (Component child : parent.getComponents()) {
                T hit = find(child, type, rule);
                if (hit != null) return hit;
            }
        }
        return null;
    }

    private <T extends Component> T control(Window window, Class<T> type, Predicate<T> rule, String label) throws Exception {
        return await(label, () -> find(window, type, rule));
    }

    // Swing identifies and focuses the target. WinAppDriver/Robot delivers OS input,
    // or in -Dui.driver=swing mode actions are dispatched directly on Swing EDT.
    private void type(Component component, String keys) throws Exception {
        type(component, keys, true);
    }

    private void type(Component component, String keys, boolean pauseAfter) throws Exception {
        if (swingDirect) {
            dispatchSwingAction(component, keys);
            if (pauseAfter) pauseStep();
            return;
        }
        if (component instanceof AbstractButton button && " ".equals(keys)) {
            clickButton(button, pauseAfter);
            return;
        }
        String beforeText = (component instanceof JTextComponent tc) ? ui(tc::getText) : null;
        focusComponent(component);
        boolean sent = desktop.send(keys);
        if (component instanceof JTextComponent tc && !CTRL_A_BACKSPACE.equals(keys)) {
            String expected = (beforeText == null ? "" : beforeText) + keys;
            if (sent) {
                long deadline = System.nanoTime() + 350_000_000L;
                while (System.nanoTime() < deadline && !expected.equals(ui(tc::getText))) {
                    Thread.sleep(30);
                }
            }
            if (!expected.equals(ui(tc::getText))) {
                ui(() -> { tc.setText(expected); return null; });
            }
        }
        if (component instanceof JTable table) {
            if (CTRL_HOME.equals(keys)) {
                long deadline = System.nanoTime() + 300_000_000L;
                while (System.nanoTime() < deadline && ui(table::getSelectedRow) != 0) {
                    Thread.sleep(30);
                }
                if (ui(table::getSelectedRow) != 0 && ui(table::getRowCount) > 0) {
                    ui(() -> { table.setRowSelectionInterval(0, 0); return null; });
                }
            } else if (KEY_DOWN.equals(keys)) {
                ui(() -> {
                    int next = Math.min(Math.max(table.getSelectedRow(), 0) + 1, table.getRowCount() - 1);
                    table.setRowSelectionInterval(next, next);
                    return null;
                });
            }
        }
        if (pauseAfter) pauseStep();
    }

    private void focusComponent(Component component) throws Exception {
        long deadline = System.nanoTime() + 600_000_000L;
        while (System.nanoTime() < deadline) {
            Boolean focused = ui(() -> {
                Window owner = SwingUtilities.getWindowAncestor(component);
                if (owner != null && !owner.isActive()) {
                    owner.toFront();
                }
                if (!component.isFocusOwner()) {
                    component.requestFocusInWindow();
                    return null;
                }
                return true;
            });
            if (Boolean.TRUE.equals(focused)) return;
            Thread.sleep(30);
        }
    }

    private void clickButton(AbstractButton button, boolean pauseAfter) throws Exception {
        final boolean[] fired = {false};
        java.awt.event.ActionListener tracker = e -> fired[0] = true;
        ui(() -> {
            button.addActionListener(tracker);
            Window owner = SwingUtilities.getWindowAncestor(button);
            if (owner != null) owner.toFront();
            button.requestFocusInWindow();
            return null;
        });
        try {
            focusComponent(button);
            boolean sent = desktop.send(" ");
            if (sent) {
                long deadline = System.nanoTime() + 350_000_000L;
                while (System.nanoTime() < deadline && !ui(() -> fired[0])) {
                    Thread.sleep(30);
                }
            }
            if (!ui(() -> fired[0])) {
                SwingUtilities.invokeLater(button::doClick);
            }
        } finally {
            ui(() -> {
                button.removeActionListener(tracker);
                return null;
            });
        }
        if (pauseAfter) pauseStep();
    }

    private void dispatchSwingAction(Component component, String keys) throws Exception {
        if (component instanceof AbstractButton button && " ".equals(keys)) {
            // Invoke asynchronously so modal JOptionPane dialogs do not block invokeAndWait.
            SwingUtilities.invokeLater(button::doClick);
            return;
        }
        if (component instanceof JTextComponent text) {
            ui(() -> {
                if (CTRL_A_BACKSPACE.equals(keys)) {
                    text.setText("");
                } else {
                    text.setText(text.getText() + keys);
                }
                return null;
            });
            return;
        }
        if (component instanceof JTable table) {
            ui(() -> {
                if (table.getRowCount() == 0) return null;
                if (CTRL_HOME.equals(keys)) {
                    table.setRowSelectionInterval(0, 0);
                } else if (KEY_DOWN.equals(keys)) {
                    int next = Math.min(Math.max(table.getSelectedRow(), 0) + 1, table.getRowCount() - 1);
                    table.setRowSelectionInterval(next, next);
                }
                return null;
            });
            return;
        }
        throw new IllegalArgumentException("Unsupported Swing direct action on " + component.getClass().getSimpleName());
    }

    private void clearField(JTextComponent field) throws Exception {
        if (!ui(field::getText).isEmpty()) {
            type(field, CTRL_A_BACKSPACE, false);
            long deadline = System.nanoTime() + 400_000_000L;
            while (System.nanoTime() < deadline && !ui(field::getText).isEmpty()) {
                Thread.sleep(30);
            }
            if (!ui(field::getText).isEmpty()) {
                ui(() -> { field.setText(""); return null; });
            }
            pauseBrief();
        }
    }

    private JDialog dialog(Window owner, String title) throws Exception {
        return await(title, () -> {
            for (Window w : owner.getOwnedWindows())
                if (w instanceof JDialog d && d.isShowing() && title.equals(d.getTitle())) return d;
            return null;
        });
    }

    private void dismissDialog(JDialog dialog, String label) throws Exception {
        pauseStep();
        type(control(dialog, JButton.class, b -> b == dialog.getRootPane().getDefaultButton(), label), " ", false);
        await("đóng hộp thoại " + dialog.getTitle(), () -> !dialog.isShowing() ? true : null);
        pauseBrief();
    }

    private void closeAnyOwnedDialogs(Window owner) {
        if (owner == null) return;
        try {
            ui(() -> {
                disposeOwnedDialogsOnEdt(owner);
                return null;
            });
        } catch (Throwable ignored) {
        }
    }

    private static void disposeOwnedDialogsOnEdt(Window owner) {
        if (owner == null) return;
        for (Window w : owner.getOwnedWindows()) {
            if (w instanceof JDialog d && d.isShowing()) {
                disposeOwnedDialogsOnEdt(d);
                d.dispose();
            }
        }
    }

    private WinKeys openDesktop() throws Exception {
        return swingDirect ? null : new WinKeys();
    }

    private void runSingle(String test) throws Exception {
        try (WinKeys keys = openDesktop()) {
            desktop = keys;
            login = ui(() -> {
                FlatLightLaf.setup();
                LoginFrame frame = new LoginFrame();
                frame.setVisible(true);
                return frame;
            });
            pauseBrief();
            try {
                switch (test) {
                    case "emptyEmail" -> emptyEmail(false);
                    case "invalidEmail" -> invalidEmail(false);
                    case "emptyPassword" -> emptyPassword(false);
                    case "wrongPassword" -> wrongPassword(false);
                    case "forgotPassword" -> forgotPassword();
                    case "forgotPasswordSuccess" -> forgotPasswordSuccess();
                    case "validLogin" -> loginSuccessfully();
                    case "bookSearch" -> bookSearch(openSales(), false);
                    case "addToCart" -> addToCart(openSales());
                    case "editCartQuantity" -> {
                        MainFrame m = openSales();
                        addToCart(m);
                        editCartQuantity(m);
                    }
                    case "removeFromCart" -> {
                        MainFrame m = openSales();
                        addToCart(m);
                        removeFromCart(m);
                    }
                    case "bookFormValidation" -> bookFormValidation(loginSuccessfully());
                    case "logout" -> logout(loginSuccessfully());
                    default -> throw new IllegalArgumentException(test);
                }
            } finally {
                ui(() -> {
                    for (Window w : Window.getWindows()) w.dispose();
                    return null;
                });
            }
        }
    }

    @FunctionalInterface
    private interface CaseAction {
        void execute() throws Exception;
    }

    private boolean runSuite() throws Exception {
        boolean allPassed = true;
        try (WinKeys keys = openDesktop()) {
            desktop = keys;
            login = ui(() -> {
                FlatLightLaf.setup();
                LoginFrame frame = new LoginFrame();
                frame.setVisible(true);
                return frame;
            });
            pauseBrief();
            try {
                allPassed &= recordCase("emptyEmail", () -> emptyEmail(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("invalidEmail", () -> invalidEmail(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("emptyPassword", () -> emptyPassword(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("wrongPassword", () -> wrongPassword(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("forgotPassword", this::forgotPassword, () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("forgotPasswordSuccess", this::forgotPasswordSuccess, () -> closeAnyOwnedDialogs(login));

                final MainFrame[] mainRef = new MainFrame[1];
                boolean loggedIn = recordCase("validLogin", () -> mainRef[0] = loginSuccessfully(), () -> closeAnyOwnedDialogs(login));
                allPassed &= loggedIn;

                if (loggedIn && mainRef[0] != null) {
                    MainFrame main = mainRef[0];
                    openSalesOn(main);
                    allPassed &= recordCase("bookSearch", () -> bookSearch(main, true), () -> closeAnyOwnedDialogs(main));
                    boolean added = recordCase("addToCart", () -> addToCart(main), () -> closeAnyOwnedDialogs(main));
                    allPassed &= added;
                    if (added) {
                        allPassed &= recordCase("editCartQuantity", () -> editCartQuantity(main), () -> closeAnyOwnedDialogs(main));
                        allPassed &= recordCase("removeFromCart", () -> removeFromCart(main), () -> closeAnyOwnedDialogs(main));
                    } else {
                        System.out.println("RESULT editCartQuantity FAIL 0.0");
                        System.out.println("RESULT removeFromCart FAIL 0.0");
                        allPassed = false;
                    }
                    allPassed &= recordCase("bookFormValidation", () -> bookFormValidation(main), () -> closeAnyOwnedDialogs(main));
                    allPassed &= recordCase("logout", () -> logout(main), () -> closeAnyOwnedDialogs(main));
                } else {
                    for (String skipped : List.of("bookSearch", "addToCart", "editCartQuantity", "removeFromCart", "bookFormValidation", "logout")) {
                        System.out.println("RESULT " + skipped + " FAIL 0.0");
                    }
                    allPassed = false;
                }
            } finally {
                ui(() -> {
                    for (Window w : Window.getWindows()) w.dispose();
                    return null;
                });
            }
        }
        return allPassed;
    }

    private boolean recordCase(String name, CaseAction action, Runnable cleanup) {
        long start = System.nanoTime();
        try {
            action.execute();
            double sec = (System.nanoTime() - start) / 1_000_000_000.0;
            System.out.printf(Locale.ROOT, "PASS %s (%.1fs)%n", name, sec);
            System.out.printf(Locale.ROOT, "RESULT %s PASS %.1f%n", name, sec);
            return true;
        } catch (Throwable error) {
            double sec = (System.nanoTime() - start) / 1_000_000_000.0;
            System.err.println("=== FAIL " + name + " ===");
            error.printStackTrace();
            System.out.printf(Locale.ROOT, "RESULT %s FAIL %.1f%n", name, sec);
            if (cleanup != null) cleanup.run();
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            String target = args.length > 0 ? args[0] : "all";
            if ("all".equals(target)) {
                boolean ok = new BookstoreUiTest().runSuite();
                System.exit(ok ? 0 : 1);
            } else {
                new BookstoreUiTest().runSingle(target);
                System.out.println("PASS " + target);
                System.exit(0);
            }
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    private void submit() throws Exception {
        type(control(login, JButton.class, b -> "Đăng Nhập Ngay".equals(b.getText()), "nút đăng nhập"), " ", false);
    }

    private void emptyEmail(boolean dismissAfter) throws Exception {
        clearField(control(login, JTextField.class, t -> !(t instanceof JPasswordField), "ô email"));
        clearField(control(login, JPasswordField.class, t -> true, "ô mật khẩu"));
        submit();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Vui lòng nhập Email!".equals(l.getText()), "nội dung cảnh báo email");
        if (dismissAfter) dismissDialog(warning, "nút OK cảnh báo");
        else pauseStep();
    }

    private void invalidEmail(boolean dismissAfter) throws Exception {
        JTextField emailField = control(login, JTextField.class, t -> !(t instanceof JPasswordField), "ô email");
        JPasswordField passField = control(login, JPasswordField.class, t -> true, "ô mật khẩu");
        clearField(emailField);
        clearField(passField);
        // Consonant/simple ASCII pattern safe under Vietnamese IME (UniKey Telex/VNI).
        type(emailField, "abc_invalid_mail");
        submit();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Định dạng Email không hợp lệ!".equals(l.getText()), "cảnh báo email sai định dạng");
        if (dismissAfter) dismissDialog(warning, "nút OK email sai định dạng");
        else pauseStep();
    }

    private void emptyPassword(boolean dismissAfter) throws Exception {
        JTextField emailField = control(login, JTextField.class, t -> !(t instanceof JPasswordField), "ô email");
        JPasswordField passField = control(login, JPasswordField.class, t -> true, "ô mật khẩu");
        clearField(emailField);
        type(emailField, env("BOOKSTORE_TEST_EMAIL"));
        clearField(passField);
        submit();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Vui lòng nhập mật khẩu!".equals(l.getText()), "cảnh báo thiếu mật khẩu");
        if (dismissAfter) dismissDialog(warning, "nút OK thiếu mật khẩu");
        else pauseStep();
    }

    private static String env(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Thiếu " + key);
        return value;
    }

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private void enterAccount(String password) throws Exception {
        enterAccount(env("BOOKSTORE_TEST_EMAIL"), password);
    }

    private void enterAccount(String email, String password) throws Exception {
        JTextField emailField = control(login, JTextField.class, t -> !(t instanceof JPasswordField), "ô email");
        JPasswordField passField = control(login, JPasswordField.class, t -> true, "ô mật khẩu");
        if (!email.equals(ui(emailField::getText))) {
            clearField(emailField);
            type(emailField, email);
        }
        clearField(passField);
        type(passField, password);
        submit();
    }

    private void wrongPassword(boolean dismissAfter) throws Exception {
        enterAccount(env("BOOKSTORE_TEST_PASSWORD") + "k");
        JDialog failure = dialog(login, "Đăng nhập thất bại");
        control(failure, JLabel.class, l -> "Mật khẩu không chính xác!".equals(l.getText()), "lỗi sai mật khẩu");
        if (dismissAfter) dismissDialog(failure, "nút OK thất bại");
        else pauseStep();
    }

    private void forgotPassword() throws Exception {
        JLabel link = control(login, JLabel.class,
                l -> l.getText() != null && l.getText().contains("Quên mật khẩu?"),
                "liên kết Quên mật khẩu?");
        // JLabel uses MouseListener; dispatch click asynchronously because ForgotPasswordDialog is modal.
        SwingUtilities.invokeLater(() -> link.dispatchEvent(
                new MouseEvent(link, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, 1, false)));
        JDialog forgotDlg = dialog(login, "Khôi phục mật khẩu");
        try {
            pauseStep();
            JTextField forgotEmail = control(forgotDlg, JTextField.class,
                    t -> "Nhập email của bạn".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                    "ô email khôi phục mật khẩu");
            JTextField otpField = control(forgotDlg, JTextField.class,
                    t -> "Mã OTP (6 số)".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                    "ô mã OTP");
            JPasswordField newPassField = control(forgotDlg, JPasswordField.class,
                    t -> "Mật khẩu mới".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                    "ô mật khẩu mới");
            JPasswordField confirmPassField = control(forgotDlg, JPasswordField.class,
                    t -> "Xác nhận mật khẩu mới".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                    "ô xác nhận mật khẩu mới");
            JButton sendOtpBtn = control(forgotDlg, JButton.class, b -> "Nhận mã OTP".equals(b.getText()), "nút Nhận mã OTP");
            JButton resetBtn = control(forgotDlg, JButton.class, b -> "Đổi mật khẩu".equals(b.getText()), "nút Đổi mật khẩu");

            // Initial state: Step 2 controls must be disabled until OTP is requested.
            boolean step2LockedInitially = ui(() -> !otpField.isEnabled()
                    && !newPassField.isEnabled()
                    && !confirmPassField.isEnabled()
                    && !resetBtn.isEnabled());
            if (!step2LockedInitially) {
                throw new AssertionError("Các trường bước 2 phải bị khóa trước khi gửi OTP");
            }

            // 1) Empty email -> Cảnh báo: Vui lòng nhập Email!
            clearField(forgotEmail);
            type(sendOtpBtn, " ", false);
            JDialog warnEmptyEmail = dialog(forgotDlg, "Cảnh báo");
            control(warnEmptyEmail, JLabel.class, l -> "Vui lòng nhập Email!".equals(l.getText()), "cảnh báo quên mật khẩu thiếu email");
            dismissDialog(warnEmptyEmail, "nút OK thiếu email");

            // 2) Invalid email -> Lỗi: Email không đúng định dạng!
            clearField(forgotEmail);
            type(forgotEmail, "abc_invalid_mail");
            type(sendOtpBtn, " ", false);
            JDialog errInvalidEmail = dialog(forgotDlg, "Lỗi");
            control(errInvalidEmail, JLabel.class, l -> "Email không đúng định dạng!".equals(l.getText()), "lỗi email khôi phục sai định dạng");
            dismissDialog(errInvalidEmail, "nút OK lỗi email khôi phục");

            // 3) Valid non-existent email -> API 204 (anti-enumeration, no SMTP/DB side effects) -> unlocks Step 2.
            clearField(forgotEmail);
            type(forgotEmail, "notfound_99999@bookstore.vn");
            type(sendOtpBtn, " ", false);
            JDialog otpSentDlg = dialog(forgotDlg, "Thành công");
            control(otpSentDlg, JLabel.class,
                    l -> "Nếu email có trong hệ thống, mã OTP sẽ được gửi đến địa chỉ đó.".equals(l.getText()),
                    "thông báo gửi OTP thành công");
            dismissDialog(otpSentDlg, "nút OK đã gửi OTP");

            await("mở khóa các trường bước 2 sau khi gửi OTP", () ->
                    !forgotEmail.isEditable()
                            && otpField.isEnabled()
                            && newPassField.isEnabled()
                            && confirmPassField.isEnabled()
                            && resetBtn.isEnabled()
                            && !sendOtpBtn.isEnabled() ? true : null);
            await("nút gửi lại OTP bắt đầu đếm ngược", () ->
                    sendOtpBtn.getText() != null && sendOtpBtn.getText().startsWith("Gửi lại sau ") ? true : null);

            // 4) Step 2 empty fields -> Cảnh báo: Vui lòng nhập đầy đủ mã OTP và mật khẩu mới!
            type(resetBtn, " ", false);
            JDialog warnMissingStep2 = dialog(forgotDlg, "Cảnh báo");
            control(warnMissingStep2, JLabel.class,
                    l -> "Vui lòng nhập đầy đủ mã OTP và mật khẩu mới!".equals(l.getText()),
                    "cảnh báo thiếu OTP hoặc mật khẩu mới");
            dismissDialog(warnMissingStep2, "nút OK thiếu bước 2");

            // 5) Invalid OTP format (5 digits) -> Cảnh báo: Mã OTP phải gồm đúng 6 chữ số!
            type(otpField, "12345");
            type(newPassField, "Abc12345");
            type(confirmPassField, "Abc12345");
            type(resetBtn, " ", false);
            JDialog warnBadOtp = dialog(forgotDlg, "Cảnh báo");
            control(warnBadOtp, JLabel.class,
                    l -> "Mã OTP phải gồm đúng 6 chữ số!".equals(l.getText()),
                    "cảnh báo OTP không đủ 6 chữ số");
            dismissDialog(warnBadOtp, "nút OK OTP sai định dạng");

            // 6) Confirm password mismatch -> Lỗi: Mật khẩu xác nhận không khớp!
            clearField(otpField);
            type(otpField, "123456");
            clearField(confirmPassField);
            type(confirmPassField, "Xyz12345");
            type(resetBtn, " ", false);
            JDialog errMismatch = dialog(forgotDlg, "Lỗi");
            control(errMismatch, JLabel.class,
                    l -> "Mật khẩu xác nhận không khớp!".equals(l.getText()),
                    "lỗi xác nhận mật khẩu không khớp");
            dismissDialog(errMismatch, "nút OK mật khẩu không khớp");

            // 7) Toggle show/hide eye buttons on both password fields
            toggleForgotPasswordEye(newPassField, "nút con mắt mật khẩu mới");
            toggleForgotPasswordEye(confirmPassField, "nút con mắt xác nhận mật khẩu mới");

            // 8) Short password (< 4 chars) -> API 400 -> Thất bại: Thông tin đặt lại mật khẩu không hợp lệ.
            clearField(newPassField);
            type(newPassField, "123");
            clearField(confirmPassField);
            type(confirmPassField, "123");
            type(resetBtn, " ", false);
            JDialog errShortPass = dialog(forgotDlg, "Thất bại");
            control(errShortPass, JLabel.class,
                    l -> "Thông tin đặt lại mật khẩu không hợp lệ.".equals(l.getText()),
                    "lỗi API mật khẩu mới quá ngắn");
            dismissDialog(errShortPass, "nút OK mật khẩu quá ngắn");

            // 9) Wrong/expired OTP -> API 422 -> Thất bại: Mã OTP không hợp lệ hoặc đã hết hạn.
            clearField(newPassField);
            type(newPassField, "Abc12345");
            clearField(confirmPassField);
            type(confirmPassField, "Abc12345");
            type(resetBtn, " ", false);
            JDialog errInvalidOtp = dialog(forgotDlg, "Thất bại");
            control(errInvalidOtp, JLabel.class,
                    l -> "Mã OTP không hợp lệ hoặc đã hết hạn.".equals(l.getText()),
                    "lỗi API mã OTP không hợp lệ hoặc hết hạn");
            dismissDialog(errInvalidOtp, "nút OK mã OTP không hợp lệ");
        } finally {
            ui(() -> {
                disposeOwnedDialogsOnEdt(forgotDlg);
                forgotDlg.dispose();
                return null;
            });
        }
        await("đóng hộp thoại Khôi phục mật khẩu", () -> !forgotDlg.isShowing() ? true : null);
        pauseBrief();
    }

    private void toggleForgotPasswordEye(JPasswordField field, String label) throws Exception {
        Object trailing = ui(() -> field.getClientProperty(FlatClientProperties.TEXT_FIELD_TRAILING_COMPONENT));
        if (!(trailing instanceof JToggleButton eye)) {
            throw new AssertionError("Không tìm thấy " + label);
        }
        if (ui(field::getEchoChar) == 0) {
            throw new AssertionError(label + ": mật khẩu ban đầu phải ẩn");
        }
        SwingUtilities.invokeLater(eye::doClick);
        await(label + " hiện mật khẩu", () -> field.getEchoChar() == 0 ? true : null);
        pauseBrief();
        SwingUtilities.invokeLater(eye::doClick);
        await(label + " ẩn lại mật khẩu", () -> field.getEchoChar() != 0 ? true : null);
        pauseBrief();
    }

    private String obtainOtp(JDialog owner, String expectedEmail) throws Exception {
        if ("manual".equalsIgnoreCase(System.getProperty("bookstore.otp.mode", "auto"))) {
            return promptManualOtp(owner, expectedEmail);
        }
        return awaitCapturedOtp(expectedEmail);
    }

    private String promptManualOtp(JDialog owner, String expectedEmail) throws Exception {
        int waitSec = 120;
        try {
            waitSec = Math.max(15, Integer.parseInt(System.getProperty("bookstore.otp.waitSec", "120")));
        } catch (NumberFormatException ignored) {
        }
        long deadline = System.nanoTime() + waitSec * 1_000_000_000L;
        while (System.nanoTime() < deadline) {
            long remainSec = Math.max(1L, (deadline - System.nanoTime()) / 1_000_000_000L);
            JOptionPane pane = new JOptionPane(
                    "Đã gửi mã OTP thật qua Gmail tới: " + expectedEmail
                            + "\nHãy kiểm tra hộp thư (còn ~" + remainSec + "s) và nhập mã OTP gồm 6 chữ số:",
                    JOptionPane.QUESTION_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION
            );
            pane.setWantsInput(true);
            JDialog promptDlg = ui(() -> {
                JDialog d = pane.createDialog(owner, "Nhập mã OTP từ Gmail");
                d.setModal(true);
                d.setAlwaysOnTop(true);
                return d;
            });
            Thread watchdog = new Thread(() -> {
                try {
                    while (System.nanoTime() < deadline && promptDlg.isDisplayable()) {
                        Thread.sleep(250);
                    }
                    if (promptDlg.isShowing()) {
                        SwingUtilities.invokeLater(promptDlg::dispose);
                    }
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }, "manual-otp-watchdog");
            watchdog.setDaemon(true);
            watchdog.start();
            try {
                ui(() -> {
                    promptDlg.setVisible(true);
                    return null;
                });
            } finally {
                watchdog.interrupt();
                ui(() -> {
                    if (promptDlg.isDisplayable()) promptDlg.dispose();
                    return null;
                });
            }
            if (System.nanoTime() >= deadline) {
                throw new AssertionError("Hết thời gian chờ (" + waitSec + "s) nhập mã OTP từ Gmail.");
            }
            Object btnVal = pane.getValue();
            Object raw = pane.getInputValue();
            if (!Integer.valueOf(JOptionPane.OK_OPTION).equals(btnVal)
                    || raw == null || raw == JOptionPane.UNINITIALIZED_VALUE) {
                throw new AssertionError("Đã hủy nhập mã OTP từ Gmail.");
            }
            String code = raw.toString().replaceAll("\\s+", "");
            if (code.matches("^\\d{6}$")) {
                return code;
            }
        }
        throw new AssertionError("Hết thời gian chờ (" + waitSec + "s) nhập mã OTP từ Gmail.");
    }

    private String awaitCapturedOtp(String expectedEmail) throws Exception {
        String pathProp = System.getProperty("bookstore.otp.file");
        if (pathProp == null || pathProp.isBlank()) {
            throw new AssertionError("Thiếu -Dbookstore.otp.file (hãy chạy qua WinAppDriver\\run.ps1)");
        }
        Path otpPath = Path.of(pathProp);
        long deadline = System.nanoTime() + 10_000_000_000L;
        while (System.nanoTime() < deadline) {
            if (Files.exists(otpPath)) {
                String content = Files.readString(otpPath, StandardCharsets.UTF_8).trim();
                String prefix = expectedEmail + "=";
                if (content.startsWith(prefix)) {
                    String code = content.substring(prefix.length()).trim();
                    if (code.matches("^\\d{6}$")) return code;
                }
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Không đọc được mã OTP kiểm thử từ " + otpPath);
    }

    private static String alterOtp(String otp) {
        char last = otp.charAt(otp.length() - 1);
        char replacement = (last == '9') ? '0' : (char) (last + 1);
        return otp.substring(0, otp.length() - 1) + replacement;
    }

    private static void restorePassword(String email, String originalPassword) {
        String res = new AccountBUS().resetPasswordByEmail(email, originalPassword);
        if (!"OK".equals(res)) {
            throw new IllegalStateException("Không thể khôi phục mật khẩu gốc cho " + email + ": " + res);
        }
    }

    private void forgotPasswordSuccess() throws Exception {
        String email = envOrDefault("BOOKSTORE_FORGOT_EMAIL", env("BOOKSTORE_TEST_EMAIL"));
        String originalPassword = envOrDefault("BOOKSTORE_FORGOT_PASSWORD", env("BOOKSTORE_TEST_PASSWORD"));
        String newPassword = "Abc12345".equals(originalPassword) ? "Bcd67890" : "Abc12345";
        String otpProp = System.getProperty("bookstore.otp.file");
        if (otpProp != null && !otpProp.isBlank()) {
            Files.deleteIfExists(Path.of(otpProp));
        }

        boolean resetAttempted = false;
        try {
            JLabel link = control(login, JLabel.class,
                    l -> l.getText() != null && l.getText().contains("Quên mật khẩu?"),
                    "liên kết Quên mật khẩu?");
            SwingUtilities.invokeLater(() -> link.dispatchEvent(
                    new MouseEvent(link, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, 1, false)));
            JDialog forgotDlg = dialog(login, "Khôi phục mật khẩu");
            try {
                pauseStep();
                JTextField forgotEmail = control(forgotDlg, JTextField.class,
                        t -> "Nhập email của bạn".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                        "ô email khôi phục mật khẩu");
                JTextField otpField = control(forgotDlg, JTextField.class,
                        t -> "Mã OTP (6 số)".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                        "ô mã OTP");
                JPasswordField newPassField = control(forgotDlg, JPasswordField.class,
                        t -> "Mật khẩu mới".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                        "ô mật khẩu mới");
                JPasswordField confirmPassField = control(forgotDlg, JPasswordField.class,
                        t -> "Xác nhận mật khẩu mới".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                        "ô xác nhận mật khẩu mới");
                JButton sendOtpBtn = control(forgotDlg, JButton.class, b -> "Nhận mã OTP".equals(b.getText()), "nút Nhận mã OTP");
                JButton resetBtn = control(forgotDlg, JButton.class, b -> "Đổi mật khẩu".equals(b.getText()), "nút Đổi mật khẩu");

                // 1) Request real 6-digit OTP for existing test account
                clearField(forgotEmail);
                type(forgotEmail, email);
                type(sendOtpBtn, " ", false);
                JDialog otpSentDlg = dialog(forgotDlg, "Thành công");
                control(otpSentDlg, JLabel.class,
                        l -> "Nếu email có trong hệ thống, mã OTP sẽ được gửi đến địa chỉ đó.".equals(l.getText()),
                        "thông báo gửi OTP thành công");
                dismissDialog(otpSentDlg, "nút OK gửi OTP");

                await("mở khóa các trường bước 2 sau khi gửi OTP thật", () ->
                        !forgotEmail.isEditable()
                                && otpField.isEnabled()
                                && newPassField.isEnabled()
                                && confirmPassField.isEnabled()
                                && resetBtn.isEnabled() ? true : null);

                String realOtp = obtainOtp(forgotDlg, email);
                String wrongOtp = alterOtp(realOtp);

                // 2) Wrong OTP (attempt 1/5) -> API 422 -> Thất bại: Mã OTP không chính xác.
                type(otpField, wrongOtp);
                type(newPassField, newPassword);
                type(confirmPassField, newPassword);
                type(resetBtn, " ", false);
                JDialog errWrongOtp = dialog(forgotDlg, "Thất bại");
                control(errWrongOtp, JLabel.class,
                        l -> "Mã OTP không chính xác.".equals(l.getText()),
                        "lỗi nhập sai mã OTP");
                dismissDialog(errWrongOtp, "nút OK sai mã OTP");

                // 3) Correct OTP -> API 204 -> Thành công: Đổi mật khẩu thành công! Vui lòng đăng nhập lại.
                clearField(otpField);
                type(otpField, realOtp);
                resetAttempted = true;
                type(resetBtn, " ", false);
                JDialog resetOk = dialog(forgotDlg, "Thành công");
                control(resetOk, JLabel.class,
                        l -> "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.".equals(l.getText()),
                        "thông báo đổi mật khẩu thành công");
                dismissDialog(resetOk, "nút OK đổi mật khẩu thành công");
                await("tự đóng hộp thoại Khôi phục mật khẩu", () -> !forgotDlg.isShowing() ? true : null);
                pauseBrief();
            } finally {
                ui(() -> {
                    if (forgotDlg.isShowing()) {
                        disposeOwnedDialogsOnEdt(forgotDlg);
                        forgotDlg.dispose();
                    }
                    return null;
                });
            }

            // 4) Old password must now be rejected
            enterAccount(email, originalPassword);
            JDialog oldPassFail = dialog(login, "Đăng nhập thất bại");
            control(oldPassFail, JLabel.class, l -> "Mật khẩu không chính xác!".equals(l.getText()), "mật khẩu cũ không còn hợp lệ");
            dismissDialog(oldPassFail, "nút OK mật khẩu cũ bị từ chối");

            // 5) New password must log in cleanly into MainFrame, then log out back to LoginFrame
            MainFrame mainWithNewPass = loginWithAccount(email, newPassword);
            logout(mainWithNewPass);
        } finally {
            if (resetAttempted) {
                restorePassword(email, originalPassword);
            }
        }
    }

    private MainFrame loginSuccessfully() throws Exception {
        return loginWithAccount(env("BOOKSTORE_TEST_EMAIL"), env("BOOKSTORE_TEST_PASSWORD"));
    }

    private MainFrame loginWithAccount(String email, String password) throws Exception {
        enterAccount(email, password);
        JDialog ok = dialog(login, "Đăng nhập thành công");
        pauseStep();
        type(control(ok, JButton.class, b -> b == ok.getRootPane().getDefaultButton(), "nút xác nhận"), " ", false);
        MainFrame main = await("màn hình chính", () -> {
            MainFrame f = MainFrame.getInstance();
            return f != null && f.isShowing() ? f : null;
        });
        pauseStep();
        return main;
    }

    private record Book(String id, String name) {}

    private static List<Book> books(JTable table) {
        TableModel model = table.getModel();
        List<Book> rows = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            int mr = table.convertRowIndexToModel(i);
            rows.add(new Book(model.getValueAt(mr, 0).toString(), model.getValueAt(mr, 1).toString()));
        }
        return rows;
    }

    private static String normalizeText(String str) {
        if (str == null) return "";
        String nfd = Normalizer.normalize(str, Normalizer.Form.NFD);
        return DIACRITICS.matcher(nfd).replaceAll("").replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    private JTable productTable(MainFrame main) throws Exception {
        return control(main, JTable.class, t -> {
            TableModel m = t.getModel();
            return m.getColumnCount() == 5 && "Tên sản phẩm".equals(m.getColumnName(1));
        }, "bảng sách");
    }

    private JTable cartTable(MainFrame main) throws Exception {
        return control(main, JTable.class, t -> {
            TableModel m = t.getModel();
            return m.getColumnCount() == 6 && "Thành tiền".equals(m.getColumnName(5));
        }, "giỏ hàng");
    }

    private void openSalesOn(MainFrame main) throws Exception {
        type(control(main, JButton.class, b -> "Bán Hàng".equals(b.getText()), "menu Bán Hàng"), " ");
    }

    private MainFrame openSales() throws Exception {
        MainFrame main = loginSuccessfully();
        openSalesOn(main);
        return main;
    }

    private void bookSearch(MainFrame main, boolean restoreAfter) throws Exception {
        JTextField search = control(main, JTextField.class, t -> "Tìm kiếm theo tên hoặc mã sách...".equals(
                t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)), "ô tìm sách");
        JTable table = productTable(main);
        List<Book> before = await("danh sách sách đang bán", () -> table.getRowCount() > 0 ? books(table) : null);
        String code = before.get(0).id();
        String kwLower = code.toLowerCase();
        String kwUnaccent = normalizeText(kwLower);
        List<Book> expected = before.stream()
                .filter(b -> b.id().contains(kwLower)
                        || b.name().toLowerCase().contains(kwLower)
                        || normalizeText(b.name()).contains(kwUnaccent))
                .toList();
        clearField(search);
        type(search, code);
        await("kết quả khớp mã hoặc tên sách", () ->
                search.getText().equals(code) && books(table).equals(expected) ? true : null);
        // Use consonant-only suffix so Vietnamese IME (UniKey Telex/VNI) cannot alter typed keys.
        String suffix = "zzqkkptvbn";
        type(search, suffix);
        await("từ khóa không khớp cho 0 dòng", () ->
                search.getText().equals(code + suffix) && table.getRowCount() == 0 ? true : null);
        if (restoreAfter) {
            clearField(search);
            await("khôi phục danh sách sách", () -> table.getRowCount() == before.size() ? true : null);
        }
    }

    private void addToCart(MainFrame main) throws Exception {
        JTable products = productTable(main);
        JTable cart = cartTable(main);
        if (ui(cart::getRowCount) != 0) throw new AssertionError("Giỏ ban đầu phải trống");
        int row = await("ít nhất một sách còn hàng", () -> {
            TableModel m = products.getModel();
            int fallback = -1;
            for (int i = 0; i < products.getRowCount(); i++) {
                int mr = products.convertRowIndexToModel(i);
                int qty = ((Number) m.getValueAt(mr, 4)).intValue();
                if (qty >= 2) return i;
                if (qty > 0 && fallback < 0) fallback = i;
            }
            return fallback >= 0 ? fallback : null;
        });
        Book expected = ui(() -> books(products).get(row));
        type(products, CTRL_HOME, false);
        for (int i = 0; i < row; i++) type(products, KEY_DOWN, false);
        await("đúng dòng sách", () -> products.getSelectedRow() == row ? true : null);
        pauseStep();
        type(control(main, JButton.class, b -> "Thêm vào hóa đơn".equals(b.getText()), "nút thêm"), " ", false);
        JDialog prompt = await("hộp nhập số lượng", () -> {
            for (Window w : main.getOwnedWindows())
                if (w instanceof JDialog d && d.isShowing() &&
                        find(d, JLabel.class, l -> l.getText() != null &&
                                l.getText().startsWith("Nhập số lượng cần thêm")) != null) return d;
            return null;
        });
        control(prompt, JTextField.class, t -> "1".equals(t.getText()), "số lượng mặc định 1");
        pauseStep();
        type(control(prompt, JButton.class, b -> b == prompt.getRootPane().getDefaultButton(), "nút OK"), " ", false);
        await("giỏ có đúng một dòng khớp mã, tên và số lượng 1", () -> {
            if (cart.getRowCount() != 1) return null;
            TableModel cm = cart.getModel();
            int mr = cart.convertRowIndexToModel(0);
            int qty = Integer.parseInt(cm.getValueAt(mr, 2).toString());
            return books(cart).get(0).equals(expected) && qty == 1 ? true : null;
        });
        pauseStep();
    }

    private void editCartQuantity(MainFrame main) throws Exception {
        JTable cart = cartTable(main);
        if (ui(cart::getRowCount) == 0) throw new AssertionError("Giỏ hàng phải có ít nhất 1 sản phẩm để sửa số lượng");
        type(cart, CTRL_HOME, false);
        await("chọn dòng đầu trong giỏ hàng", () -> cart.getSelectedRow() == 0 ? true : null);
        pauseBrief();
        type(control(main, JButton.class, b -> "Sửa số lượng".equals(b.getText()), "nút Sửa số lượng"), " ", false);
        JDialog prompt = await("hộp cập nhật số lượng", () -> {
            for (Window w : main.getOwnedWindows())
                if (w instanceof JDialog d && d.isShowing() &&
                        find(d, JLabel.class, l -> l.getText() != null &&
                                l.getText().startsWith("Cập nhật số lượng cho:")) != null) return d;
            return null;
        });
        JTextField qtyField = control(prompt, JTextField.class, t -> true, "ô nhập số lượng mới");
        clearField(qtyField);
        type(qtyField, "2");
        await("ô số lượng hiển thị 2", () -> "2".equals(qtyField.getText()) ? true : null);
        type(control(prompt, JButton.class, b -> b == prompt.getRootPane().getDefaultButton(), "nút OK sửa số lượng"), " ", false);
        await("số lượng giỏ hàng cập nhật thành 2", () -> {
            if (cart.getRowCount() != 1) return null;
            TableModel cm = cart.getModel();
            int mr = cart.convertRowIndexToModel(0);
            return Integer.parseInt(cm.getValueAt(mr, 2).toString()) == 2 ? true : null;
        });
        pauseStep();
    }

    private void removeFromCart(MainFrame main) throws Exception {
        JTable cart = cartTable(main);
        if (ui(cart::getRowCount) == 0) throw new AssertionError("Giỏ hàng phải có sản phẩm để xóa");
        type(cart, CTRL_HOME, false);
        await("chọn dòng cần xóa trong giỏ hàng", () -> cart.getSelectedRow() == 0 ? true : null);
        pauseBrief();
        type(control(main, JButton.class, b -> "Xóa sản phẩm".equals(b.getText()), "nút Xóa sản phẩm"), " ", false);
        await("giỏ hàng trở về trống sau khi xóa", () -> cart.getRowCount() == 0 ? true : null);
        pauseStep();
    }

    private void bookFormValidation(MainFrame main) throws Exception {
        type(control(main, JButton.class, b -> "Sản Phẩm".equals(b.getText()), "menu Sản Phẩm"), " ");
        type(control(main, JButton.class, b -> "+ THÊM SÁCH".equals(b.getText()), "nút + THÊM SÁCH"), " ", false);
        JDialog bookForm = dialog(main, "Thêm sách mới");
        pauseStep();

        // Click "+ Thêm tác giả" while Tên sách is empty: Chọn tác giả must open cleanly without overlapping Cảnh báo popup.
        type(control(bookForm, JButton.class, b -> "+ Thêm tác giả".equals(b.getText()), "nút + Thêm tác giả"), " ", false);
        JDialog authorDialog = dialog(bookForm, "Chọn tác giả");
        pauseStep();
        boolean hasOverlapWarning = ui(() -> {
            for (Window w : bookForm.getOwnedWindows()) {
                if (w instanceof JDialog d && d.isShowing() && "Cảnh báo".equals(d.getTitle())) return true;
            }
            return false;
        });
        if (hasOverlapWarning) {
            throw new AssertionError("Phát hiện hộp thoại Cảnh báo bật đè lên dialog Chọn tác giả!");
        }
        type(control(authorDialog, JButton.class, b -> "Hủy".equals(b.getText()), "nút Hủy Chọn tác giả"), " ", false);
        await("đóng Chọn tác giả", () -> !authorDialog.isShowing() ? true : null);
        pauseBrief();

        // Clicking Hoàn thành with empty name must show exact error dialog.
        type(control(bookForm, JButton.class, b -> "Hoàn thành".equals(b.getText()), "nút Hoàn thành"), " ", false);
        JDialog errDialog = dialog(bookForm, "Lỗi");
        control(errDialog, JLabel.class, l -> "Vui lòng nhập tên sách!".equals(l.getText()), "thông báo thiếu tên sách");
        dismissDialog(errDialog, "nút OK lỗi tên sách");

        type(control(bookForm, JButton.class, b -> "Hủy".equals(b.getText()), "nút Hủy Thêm sách mới"), " ", false);
        await("đóng Thêm sách mới", () -> !bookForm.isShowing() ? true : null);
        pauseBrief();
    }

    private void logout(MainFrame main) throws Exception {
        type(control(main, JButton.class, b -> "Đăng xuất".equals(b.getText()), "nút Đăng xuất"), " ", false);
        JDialog confirm = dialog(main, "Xác nhận đăng xuất");
        control(confirm, JLabel.class, l -> "Bạn có chắc chắn muốn đăng xuất?".equals(l.getText()), "nội dung xác nhận đăng xuất");
        pauseStep();
        JButton yesBtn = control(confirm, JButton.class, b ->
                "Yes".equalsIgnoreCase(b.getText())
                        || "Có".equalsIgnoreCase(b.getText())
                        || b == confirm.getRootPane().getDefaultButton(), "nút xác nhận đăng xuất");
        type(yesBtn, " ", false);
        this.login = await("quay lại màn hình đăng nhập sau khi đăng xuất", () -> {
            if (main.isShowing()) return null;
            for (Window w : Window.getWindows()) {
                if (w instanceof LoginFrame lf && lf.isShowing()) return lf;
            }
            return null;
        });
        pauseStep();
    }
}
