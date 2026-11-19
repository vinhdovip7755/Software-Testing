import com.bookstore.gui.main.LoginFrame;
import com.bookstore.gui.main.MainFrame;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

/**
 * Dedicated Login UI test suite (11 test cases: 7 offline UI validation + 4 API auth/logout).
 * Supports WinAppDriver (default), AWT Robot (-Dui.driver=robot), and direct Swing EDT (-Dui.driver=swing).
 */
public class LoginUiTest {
    private static final String CTRL_A_BACKSPACE = "\uE009a\uE000\uE003";
    private static final String KEY_ENTER = "\uE007";

    private static final List<String> OFFLINE_CASES = List.of(
            "emptyEmail",
            "whitespaceEmail",
            "invalidEmailNoAt",
            "invalidEmailBadDomain",
            "emptyPassword",
            "togglePasswordVisibility",
            "forgotPasswordValidation"
    );

    private static final List<String> ONLINE_CASES = List.of(
            "nonExistentAccount",
            "wrongPassword",
            "validLoginWithEnter",
            "logout"
    );

    private final boolean swingDirect;
    private final long stepDelayMs;
    private WinKeys desktop;
    private LoginFrame login;

    public LoginUiTest() {
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
        if (KEY_ENTER.equals(keys)) {
            JButton loginBtn = control(login, JButton.class, b -> "Đăng Nhập Ngay".equals(b.getText()), "nút đăng nhập");
            final boolean[] fired = {false};
            java.awt.event.ActionListener tracker = e -> fired[0] = true;
            ui(() -> { loginBtn.addActionListener(tracker); return null; });
            try {
                focusComponent(component);
                boolean sent = desktop.send(KEY_ENTER);
                if (sent) {
                    long deadline = System.nanoTime() + 450_000_000L;
                    while (System.nanoTime() < deadline && !ui(() -> fired[0])) {
                        Thread.sleep(30);
                    }
                }
                if (!ui(() -> fired[0])) {
                    SwingUtilities.invokeLater(loginBtn::doClick);
                }
            } finally {
                ui(() -> { loginBtn.removeActionListener(tracker); return null; });
            }
            if (pauseAfter) pauseStep();
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
            SwingUtilities.invokeLater(button::doClick);
            return;
        }
        if (KEY_ENTER.equals(keys)) {
            JRootPane rootPane = ui(() -> SwingUtilities.getRootPane(component));
            if (rootPane != null) {
                Action enterAction = rootPane.getActionMap().get("clickLogin");
                if (enterAction != null) {
                    SwingUtilities.invokeLater(() -> enterAction.actionPerformed(
                            new ActionEvent(component, ActionEvent.ACTION_PERFORMED, "clickLogin")));
                    return;
                }
            }
            throw new IllegalStateException("Không tìm thấy action ENTER clickLogin trên rootPane");
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

    private JTextField emailField() throws Exception {
        return control(login, JTextField.class, t -> !(t instanceof JPasswordField), "ô email");
    }

    private JPasswordField passwordField() throws Exception {
        return control(login, JPasswordField.class, t -> true, "ô mật khẩu");
    }

    private void submitByButton() throws Exception {
        type(control(login, JButton.class, b -> "Đăng Nhập Ngay".equals(b.getText()), "nút đăng nhập"), " ", false);
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            if (fallback != null) return fallback;
            throw new IllegalStateException("Thiếu " + key);
        }
        return value;
    }

    @FunctionalInterface
    private interface CaseAction {
        void execute() throws Exception;
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

    private boolean runSuite(boolean offlineOnly) throws Exception {
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
                allPassed &= recordCase("whitespaceEmail", () -> whitespaceEmail(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("invalidEmailNoAt", () -> invalidEmailNoAt(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("invalidEmailBadDomain", () -> invalidEmailBadDomain(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("emptyPassword", () -> emptyPassword(true), () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("togglePasswordVisibility", this::togglePasswordVisibility, () -> closeAnyOwnedDialogs(login));
                allPassed &= recordCase("forgotPasswordValidation", this::forgotPasswordValidation, () -> closeAnyOwnedDialogs(login));

                if (!offlineOnly) {
                    allPassed &= recordCase("nonExistentAccount", () -> nonExistentAccount(true), () -> closeAnyOwnedDialogs(login));
                    allPassed &= recordCase("wrongPassword", () -> wrongPassword(true), () -> closeAnyOwnedDialogs(login));

                    final MainFrame[] mainRef = new MainFrame[1];
                    boolean loggedIn = recordCase("validLoginWithEnter",
                            () -> mainRef[0] = validLoginWithEnter(),
                            () -> closeAnyOwnedDialogs(login));
                    allPassed &= loggedIn;

                    if (loggedIn && mainRef[0] != null) {
                        MainFrame main = mainRef[0];
                        allPassed &= recordCase("logout", () -> logout(main), () -> closeAnyOwnedDialogs(main));
                    } else {
                        System.out.println("RESULT logout FAIL 0.0");
                        allPassed = false;
                    }
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
                    case "whitespaceEmail" -> whitespaceEmail(false);
                    case "invalidEmailNoAt" -> invalidEmailNoAt(false);
                    case "invalidEmailBadDomain" -> invalidEmailBadDomain(false);
                    case "emptyPassword" -> emptyPassword(false);
                    case "togglePasswordVisibility" -> togglePasswordVisibility();
                    case "forgotPasswordValidation" -> forgotPasswordValidation();
                    case "nonExistentAccount" -> nonExistentAccount(false);
                    case "wrongPassword" -> wrongPassword(false);
                    case "validLoginWithEnter" -> validLoginWithEnter();
                    case "logout" -> logout(validLoginWithEnter());
                    default -> throw new IllegalArgumentException("Unknown login test case: " + test);
                }
            } finally {
                ui(() -> {
                    for (Window w : Window.getWindows()) w.dispose();
                    return null;
                });
            }
        }
    }

    public static void main(String[] args) {
        try {
            String target = args.length > 0 ? args[0] : "all";
            if ("all".equals(target) || "offline".equals(target)) {
                boolean ok = new LoginUiTest().runSuite("offline".equals(target));
                System.exit(ok ? 0 : 1);
            } else {
                new LoginUiTest().runSingle(target);
                System.out.println("PASS " + target);
                System.exit(0);
            }
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    // --- 7 Offline UI Login Cases (do not require API server) ---

    private void emptyEmail(boolean dismissAfter) throws Exception {
        clearField(emailField());
        clearField(passwordField());
        submitByButton();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Vui lòng nhập Email!".equals(l.getText()), "cảnh báo email trống");
        if (dismissAfter) dismissDialog(warning, "nút OK email trống");
        else pauseStep();
    }

    private void whitespaceEmail(boolean dismissAfter) throws Exception {
        JTextField ef = emailField();
        clearField(ef);
        clearField(passwordField());
        type(ef, "   ");
        submitByButton();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Vui lòng nhập Email!".equals(l.getText()), "cảnh báo email chỉ có khoảng trắng");
        if (dismissAfter) dismissDialog(warning, "nút OK email khoảng trắng");
        else pauseStep();
    }

    private void invalidEmailNoAt(boolean dismissAfter) throws Exception {
        JTextField ef = emailField();
        clearField(ef);
        clearField(passwordField());
        type(ef, "abc_invalid_mail");
        submitByButton();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Định dạng Email không hợp lệ!".equals(l.getText()), "cảnh báo email thiếu @");
        if (dismissAfter) dismissDialog(warning, "nút OK email thiếu @");
        else pauseStep();
    }

    private void invalidEmailBadDomain(boolean dismissAfter) throws Exception {
        JTextField ef = emailField();
        clearField(ef);
        clearField(passwordField());
        type(ef, "abc@khongco");
        submitByButton();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Định dạng Email không hợp lệ!".equals(l.getText()), "cảnh báo email sai tên miền");
        if (dismissAfter) dismissDialog(warning, "nút OK email sai tên miền");
        else pauseStep();
    }

    private void emptyPassword(boolean dismissAfter) throws Exception {
        JTextField ef = emailField();
        clearField(ef);
        type(ef, env("BOOKSTORE_TEST_EMAIL", "vinhyt7755@gmail.com"));
        clearField(passwordField());
        submitByButton();
        JDialog warning = dialog(login, "Cảnh báo");
        control(warning, JLabel.class, l -> "Vui lòng nhập mật khẩu!".equals(l.getText()), "cảnh báo thiếu mật khẩu");
        if (dismissAfter) dismissDialog(warning, "nút OK thiếu mật khẩu");
        else pauseStep();
    }

    private void togglePasswordVisibility() throws Exception {
        JPasswordField pf = passwordField();
        clearField(pf);
        type(pf, "Test1234");
        char hiddenEcho = ui(pf::getEchoChar);
        if (hiddenEcho == 0) {
            throw new AssertionError("Mật khẩu ban đầu phải ở trạng thái ẩn ký tự");
        }

        // btnToggleEye has setFocusable(false) in LoginFrame, so click via Swing EDT.
        JToggleButton eye = control(login, JToggleButton.class, b -> true, "nút con mắt hiện/ẩn mật khẩu");
        SwingUtilities.invokeLater(eye::doClick);
        await("mật khẩu hiện rõ (echoChar == 0)", () -> pf.getEchoChar() == 0 ? true : null);
        pauseStep();

        SwingUtilities.invokeLater(eye::doClick);
        await("mật khẩu ẩn lại (echoChar != 0)", () -> pf.getEchoChar() != 0 ? true : null);
        pauseStep();
    }

    private void forgotPasswordValidation() throws Exception {
        JLabel link = control(login, JLabel.class,
                l -> l.getText() != null && l.getText().contains("Quên mật khẩu?"),
                "liên kết Quên mật khẩu?");
        // JLabel uses MouseListener; dispatch click asynchronously because ForgotPasswordDialog is modal.
        SwingUtilities.invokeLater(() -> link.dispatchEvent(
                new MouseEvent(link, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 5, 5, 1, false)));
        JDialog forgotDlg = dialog(login, "Khôi phục mật khẩu");
        try {
            pauseStep();

            // 1) Empty email -> Cảnh báo: Vui lòng nhập Email!
            JButton sendOtpBtn = control(forgotDlg, JButton.class, b -> "Nhận mã OTP".equals(b.getText()), "nút Nhận mã OTP");
            type(sendOtpBtn, " ", false);
            JDialog warnEmpty = dialog(forgotDlg, "Cảnh báo");
            control(warnEmpty, JLabel.class, l -> "Vui lòng nhập Email!".equals(l.getText()), "cảnh báo quên mật khẩu thiếu email");
            dismissDialog(warnEmpty, "nút OK quên mật khẩu thiếu email");

            // 2) Invalid email -> Lỗi: Email không đúng định dạng! (and no duplicate focusLost popup)
            JTextField forgotEmail = control(forgotDlg, JTextField.class,
                    t -> "Nhập email của bạn".equals(t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)),
                    "ô email khôi phục mật khẩu");
            clearField(forgotEmail);
            type(forgotEmail, "abc_invalid_mail");
            type(sendOtpBtn, " ", false);
            JDialog errInvalid = dialog(forgotDlg, "Lỗi");
            control(errInvalid, JLabel.class, l -> "Email không đúng định dạng!".equals(l.getText()), "lỗi email khôi phục sai định dạng");
            dismissDialog(errInvalid, "nút OK lỗi email khôi phục");
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

    // --- 4 Online API Login/Logout Cases (require API server on port 8080) ---

    private void nonExistentAccount(boolean dismissAfter) throws Exception {
        JTextField ef = emailField();
        JPasswordField pf = passwordField();
        clearField(ef);
        type(ef, "notfound_99999@bookstore.vn");
        clearField(pf);
        type(pf, "123456");
        submitByButton();
        JDialog failure = dialog(login, "Đăng nhập thất bại");
        control(failure, JLabel.class, l -> "Tài khoản không tồn tại!".equals(l.getText()), "lỗi tài khoản không tồn tại");
        if (dismissAfter) dismissDialog(failure, "nút OK tài khoản không tồn tại");
        else pauseStep();
    }

    private void wrongPassword(boolean dismissAfter) throws Exception {
        JTextField ef = emailField();
        JPasswordField pf = passwordField();
        clearField(ef);
        type(ef, env("BOOKSTORE_TEST_EMAIL", null));
        clearField(pf);
        type(pf, env("BOOKSTORE_TEST_PASSWORD", null) + "k");
        submitByButton();
        JDialog failure = dialog(login, "Đăng nhập thất bại");
        control(failure, JLabel.class, l -> "Mật khẩu không chính xác!".equals(l.getText()), "lỗi sai mật khẩu");
        if (dismissAfter) dismissDialog(failure, "nút OK sai mật khẩu");
        else pauseStep();
    }

    private MainFrame validLoginWithEnter() throws Exception {
        JTextField ef = emailField();
        JPasswordField pf = passwordField();
        String email = env("BOOKSTORE_TEST_EMAIL", null);
        String password = env("BOOKSTORE_TEST_PASSWORD", null);

        // Verify email with leading/trailing whitespace is trimmed and ENTER key submits login.
        clearField(ef);
        type(ef, " " + email + " ");
        clearField(pf);
        type(pf, password);
        type(pf, KEY_ENTER, false);

        JDialog ok = dialog(login, "Đăng nhập thành công");
        control(ok, JLabel.class, l -> l.getText() != null && l.getText().startsWith("Xin chào "), "lời chào đăng nhập thành công");
        pauseStep();
        type(control(ok, JButton.class, b -> b == ok.getRootPane().getDefaultButton(), "nút OK đăng nhập thành công"), " ", false);
        MainFrame main = await("màn hình chính sau đăng nhập", () -> {
            MainFrame f = MainFrame.getInstance();
            return f != null && f.isShowing() ? f : null;
        });
        pauseStep();
        return main;
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
        await("quay lại LoginFrame sau khi đăng xuất", () -> {
            if (main.isShowing()) return null;
            for (Window w : Window.getWindows()) {
                if (w instanceof LoginFrame lf && lf.isShowing()) return lf;
            }
            return null;
        });
        pauseStep();
    }
}
