import com.bookstore.gui.main.LoginFrame;
import com.bookstore.gui.main.MainFrame;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;

public class BookstoreUiTest {
    private WinKeys desktop;
    private LoginFrame login;

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
            Thread.sleep(100);
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

    // Swing identifies and focuses the target. WinAppDriver delivers the actual input.
    private void type(Component component, String keys) throws Exception {
        ui(() -> { SwingUtilities.getWindowAncestor(component).toFront(); component.requestFocusInWindow(); return null; });
        await("focus " + component.getClass().getSimpleName(), () -> component.isFocusOwner() ? true : null);
        desktop.send(keys);
    }

    private JDialog dialog(String title) throws Exception {
        return await(title, () -> {
            for (Window w : login.getOwnedWindows())
                if (w instanceof JDialog d && d.isShowing() && title.equals(d.getTitle())) return d;
            return null;
        });
    }

    private void run(String test) throws Exception {
        try (WinKeys keys = new WinKeys()) {
            desktop = keys;
            login = ui(() -> {
                FlatLightLaf.setup();
                LoginFrame frame = new LoginFrame();
                frame.setVisible(true);
                return frame;
            });
            try {
                switch (test) {
                    case "emptyEmail" -> emptyEmail();
                    case "wrongPassword" -> wrongPassword();
                    case "validLogin" -> loginSuccessfully();
                    case "bookSearch" -> bookSearch();
                    case "addToCart" -> addToCart();
                    default -> throw new IllegalArgumentException(test);
                }
            } finally {
                ui(() -> { for (Window w : Window.getWindows()) w.dispose(); return null; });
            }
        }
    }

    public static void main(String[] args) {
        try {
            new BookstoreUiTest().run(args[0]);
            System.out.println("PASS " + args[0]);
            System.exit(0);
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    private void submit() throws Exception {
        type(control(login, JButton.class, b -> "Đăng Nhập Ngay".equals(b.getText()), "nút đăng nhập"), " ");
    }

    private void emptyEmail() throws Exception {
        submit();
        JDialog warning = dialog("Cảnh báo");
        control(warning, JLabel.class, l -> "Vui lòng nhập Email!".equals(l.getText()), "nội dung cảnh báo email");
    }

    private static String env(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) throw new IllegalStateException("Thiếu " + key);
        return value;
    }

    private void enterAccount(String password) throws Exception {
        type(control(login, JTextField.class, t -> !(t instanceof JPasswordField), "ô email"),
                env("BOOKSTORE_TEST_EMAIL"));
        type(control(login, JPasswordField.class, t -> true, "ô mật khẩu"), password);
        submit();
    }

    private void wrongPassword() throws Exception {
        enterAccount(env("BOOKSTORE_TEST_PASSWORD") + "x");
        JDialog failure = dialog("Đăng nhập thất bại");
        control(failure, JLabel.class, l -> "Mật khẩu không chính xác!".equals(l.getText()), "lỗi sai mật khẩu");
    }

    private MainFrame loginSuccessfully() throws Exception {
        enterAccount(env("BOOKSTORE_TEST_PASSWORD"));
        JDialog ok = dialog("Đăng nhập thành công");
        type(control(ok, JButton.class, b -> b == ok.getRootPane().getDefaultButton(), "nút xác nhận"), " ");
        return await("màn hình chính", () -> {
            MainFrame f = MainFrame.getInstance();
            return f != null && f.isShowing() ? f : null;
        });
    }

    private record Book(String id, String name) {}
    private static List<Book> books(JTable table) {
        List<Book> rows = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++)
            rows.add(new Book(table.getValueAt(i, 0).toString(), table.getValueAt(i, 1).toString()));
        return rows;
    }

    private MainFrame openSales() throws Exception {
        MainFrame main = loginSuccessfully();
        type(control(main, JButton.class, b -> "Bán Hàng".equals(b.getText()), "menu Bán Hàng"), " ");
        return main;
    }

    private void addToCart() throws Exception {
        MainFrame main = openSales();
        JTable products = control(main, JTable.class, t -> t.getColumnCount() == 5
                && "Tên sản phẩm".equals(t.getColumnName(1)), "bảng sách");
        JTable cart = control(main, JTable.class, t -> t.getColumnCount() == 6
                && "Thành tiền".equals(t.getColumnName(5)), "giỏ hàng");
        if (ui(cart::getRowCount) != 0) throw new AssertionError("Giỏ ban đầu phải trống");
        int row = await("ít nhất một sách còn hàng", () -> {
            for (int i = 0; i < products.getRowCount(); i++)
                if (((Number) products.getValueAt(i, 4)).intValue() > 0) return i;
            return null;
        });
        Book expected = ui(() -> books(products).get(row));
        // WebDriver Control+Home selects the first cell. NULL releases Control.
        type(products, "\uE009\uE011\uE000");
        for (int i = 0; i < row; i++) type(products, "\uE015");
        await("đúng dòng sách", () -> products.getSelectedRow() == row ? true : null);
        type(control(main, JButton.class, b -> "Thêm vào hóa đơn".equals(b.getText()), "nút thêm"), " ");
        JDialog prompt = await("hộp nhập số lượng", () -> {
            for (Window w : main.getOwnedWindows())
                if (w instanceof JDialog d && d.isShowing() &&
                        find(d, JLabel.class, l -> l.getText() != null &&
                                l.getText().startsWith("Nhập số lượng cần thêm")) != null) return d;
            return null;
        });
        control(prompt, JTextField.class, t -> "1".equals(t.getText()), "số lượng mặc định 1");
        type(control(prompt, JButton.class, b -> b == prompt.getRootPane().getDefaultButton(), "nút OK"), " ");
        await("giỏ có đúng một dòng khớp mã, tên và số lượng 1", () ->
                cart.getRowCount() == 1 && books(cart).get(0).equals(expected)
                        && ((Number) cart.getValueAt(0, 2)).intValue() == 1 ? true : null);
    }

    private void bookSearch() throws Exception {
        MainFrame main = openSales();
        JTextField search = control(main, JTextField.class, t -> "Tìm kiếm theo tên hoặc mã sách...".equals(
                t.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT)), "ô tìm sách");
        JTable table = control(main, JTable.class, t -> t.getColumnCount() == 5
                && "Tên sản phẩm".equals(t.getColumnName(1)), "bảng sách");
        List<Book> before = await("danh sách sách đang bán", () -> table.getRowCount() > 0 ? books(table) : null);
        String code = before.get(0).id();
        List<Book> expected = before.stream()
                .filter(b -> b.id().contains(code) || b.name().contains(code)).toList();
        type(search, code);
        await("kết quả khớp mã hoặc tên sách", () ->
                search.getText().equals(code) && books(table).equals(expected) ? true : null);
        String suffix = java.util.UUID.randomUUID().toString();
        type(search, suffix);
        await("từ khóa không khớp cho 0 dòng", () ->
                search.getText().equals(code + suffix) && table.getRowCount() == 0 ? true : null);
    }
}
