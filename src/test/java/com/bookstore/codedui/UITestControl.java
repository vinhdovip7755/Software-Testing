package com.bookstore.codedui;

import com.bookstore.api.client.ApiClient;
import com.bookstore.gui.main.LoginFrame;
import com.bookstore.gui.panel.ImportTab.ImportQuantityDiscountDialog;
import com.bookstore.util.SharedData;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Lop co so dai dien cho mot thanh phan giao dien Swing trong Coded UI Test Framework.
 * Mo phong truc tiep co che SearchProperties / WindowTitles cua Microsoft Coded UI
 * nhung hoat dong tren cay component Java Swing that (JFrame, JDialog, JButton, JTextField...).
 */
public class UITestControl {

    public static final class PropertyNames {
        public static final String Name = "Name";
        public static final String ClassName = "ClassName";
        public static final String ControlId = "ControlId";
        public static final String ControlType = "ControlType";
        public static final String TechnologyName = "TechnologyName";
        public static final String Placeholder = "Placeholder";

        private PropertyNames() {
        }
    }

    protected static final int DEFAULT_TIMEOUT_MS = 6000;
    protected static final int POLL_INTERVAL_MS = 50;

    protected final UITestControl searchLimitContainer;
    protected final Map<String, String> searchProperties = new LinkedHashMap<>();
    protected final List<String> windowTitles = new ArrayList<>();

    public UITestControl() {
        this(null);
    }

    public UITestControl(UITestControl searchLimitContainer) {
        this.searchLimitContainer = searchLimitContainer;
        this.searchProperties.put(PropertyNames.TechnologyName, "Swing");
    }

    public Map<String, String> getSearchProperties() {
        return searchProperties;
    }

    public List<String> getWindowTitles() {
        return windowTitles;
    }

    public UITestControl getSearchLimitContainer() {
        return searchLimitContainer;
    }

    /**
     * Tim kiem va cho den khi component thuc te xuat hien tren giao dien Swing.
     */
    public Component getComponent() {
        return getComponent(DEFAULT_TIMEOUT_MS);
    }

    public Component getComponent(int timeoutMs) {
        Component found = waitForComponent(timeoutMs);
        if (found == null) {
            throw new IllegalStateException(
                    "Khong tim thay UI control phu hop voi SearchProperties: " + searchProperties
                            + ", WindowTitles: " + windowTitles
            );
        }
        return found;
    }

    public boolean exists() {
        return findNow() != null;
    }

    public boolean waitForControlExist(int timeoutMs) {
        return waitForComponent(timeoutMs) != null;
    }

    protected Component waitForComponent(int timeoutMs) {
        long deadline = System.currentTimeMillis() + Math.max(timeoutMs, 0);
        while (true) {
            Component comp = findNow();
            if (comp != null) {
                return comp;
            }
            if (System.currentTimeMillis() >= deadline) {
                return null;
            }
            sleepSilently(POLL_INTERVAL_MS);
        }
    }

    /**
     * Quet cay Swing tren EDT de tim component khop voi SearchProperties.
     */
    public Component findNow() {
        return runOnEdtAndReturn(() -> {
            if (searchLimitContainer == null) {
                return findTopLevelWindow();
            }
            Component parent = findTopLevelOrChild(searchLimitContainer);
            if (!(parent instanceof Container container)) {
                return null;
            }
            return findDescendant(container);
        });
    }

    private Component findTopLevelOrChild(UITestControl control) {
        if (control.searchLimitContainer == null) {
            return control.findTopLevelWindow();
        }
        Component grandParent = findTopLevelOrChild(control.searchLimitContainer);
        if (!(grandParent instanceof Container container)) {
            return null;
        }
        return control.findDescendant(container);
    }

    protected Component findTopLevelWindow() {
        Window[] windows = Window.getWindows();
        for (int i = windows.length - 1; i >= 0; i--) {
            Window window = windows[i];
            if (!window.isShowing()) {
                continue;
            }
            if (matchesWindow(window)) {
                return window;
            }
        }
        return null;
    }

    private Component findDescendant(Container root) {
        Queue<Component> queue = new ArrayDeque<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Component current = queue.poll();
            if (current != root && matchesComponent(current)) {
                return current;
            }
            if (current instanceof Container container) {
                for (Component child : container.getComponents()) {
                    queue.add(child);
                }
            }
        }
        return null;
    }

    private boolean matchesWindow(Window window) {
        String expectedClass = searchProperties.get(PropertyNames.ClassName);
        if (expectedClass != null && !expectedClass.isEmpty()) {
            if ("JOptionPaneDialog".equals(expectedClass)) {
                if (!(window instanceof JDialog) || findOptionPaneIn(window) == null) {
                    return false;
                }
            } else {
                String simpleName = window.getClass().getSimpleName();
                String fullName = window.getClass().getName();
                if (!expectedClass.equals(simpleName) && !expectedClass.equals(fullName)) {
                    return false;
                }
            }
        }

        String title = getWindowTitle(window);
        String expectedName = searchProperties.get(PropertyNames.Name);
        if (expectedName != null && !expectedName.isEmpty()) {
            boolean matchesName = expectedName.equals(window.getName()) || expectedName.equals(title);
            if (!matchesName) {
                return false;
            }
        }

        if (!windowTitles.isEmpty()) {
            boolean titleMatched = false;
            for (String candidate : windowTitles) {
                if (candidate.equals(title)) {
                    titleMatched = true;
                    break;
                }
            }
            if (!titleMatched) {
                return false;
            }
        }

        return true;
    }

    protected boolean matchesComponent(Component comp) {
        if (!comp.isShowing()) {
            return false;
        }

        String expectedType = searchProperties.get(PropertyNames.ControlType);
        if (expectedType != null && !expectedType.isEmpty() && !matchesControlType(comp, expectedType)) {
            return false;
        }

        String expectedClass = searchProperties.get(PropertyNames.ClassName);
        if (expectedClass != null && !expectedClass.isEmpty()) {
            String simpleName = comp.getClass().getSimpleName();
            String fullName = comp.getClass().getName();
            if (!expectedClass.equals(simpleName) && !expectedClass.equals(fullName)) {
                return false;
            }
        }

        String expectedControlId = searchProperties.get(PropertyNames.ControlId);
        if (expectedControlId != null && !expectedControlId.isEmpty()) {
            if ("OptionPane.message".equals(expectedControlId)) {
                return comp instanceof JOptionPane;
            }
            if (!expectedControlId.equals(comp.getName())) {
                return false;
            }
        }

        String expectedPlaceholder = searchProperties.get(PropertyNames.Placeholder);
        if (expectedPlaceholder != null && !expectedPlaceholder.isEmpty()) {
            String actualPlaceholder = getPlaceholderText(comp);
            if (!expectedPlaceholder.equals(actualPlaceholder)) {
                return false;
            }
        }

        String expectedName = searchProperties.get(PropertyNames.Name);
        if (expectedName != null && !expectedName.isEmpty()) {
            if (!matchesComponentName(comp, expectedName)) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesControlType(Component comp, String expectedType) {
        return switch (expectedType) {
            case "Button" -> comp instanceof AbstractButton && !(comp instanceof JToggleButton);
            case "ToggleButton" -> comp instanceof JToggleButton;
            case "Edit" -> comp instanceof JTextComponent;
            case "Text" -> comp instanceof JLabel || comp instanceof JOptionPane;
            case "Window" -> comp instanceof Window || comp instanceof JPanel;
            default -> true;
        };
    }

    private static boolean matchesComponentName(Component comp, String expectedName) {
        if (expectedName.equals(comp.getName())) {
            return true;
        }
        if (comp instanceof AbstractButton btn) {
            String raw = btn.getText();
            if (expectedName.equals(raw) || expectedName.equals(stripHtml(raw))) {
                return true;
            }
            if (expectedName.equals(btn.getActionCommand())) {
                return true;
            }
            if (expectedName.equals(btn.getToolTipText())) {
                return true;
            }
        }
        if (comp instanceof JLabel label) {
            String raw = label.getText();
            if (expectedName.equals(raw) || expectedName.equals(stripHtml(raw))) {
                return true;
            }
        }
        if (comp instanceof JTextComponent tc) {
            String placeholder = getPlaceholderText(tc);
            if (expectedName.equals(placeholder)) {
                return true;
            }
            if (expectedName.equals(tc.getToolTipText())) {
                return true;
            }
        }
        if (comp instanceof JOptionPane optionPane) {
            return expectedName.equals(String.valueOf(optionPane.getMessage()));
        }
        return false;
    }

    public String getDisplayText() {
        Component comp = getComponent();
        return runOnEdtAndReturn(() -> extractDisplayText(comp));
    }

    static String extractDisplayText(Component comp) {
        if (comp instanceof JPasswordField pf) {
            if (pf.getEchoChar() == 0) {
                return new String(pf.getPassword());
            }
            char[] pwd = pf.getPassword();
            return String.valueOf(pf.getEchoChar()).repeat(pwd.length);
        }
        if (comp instanceof JTextComponent tc) {
            return tc.getText();
        }
        if (comp instanceof JLabel label) {
            String raw = label.getText();
            if (raw != null && raw.trim().toLowerCase(Locale.ROOT).startsWith("<html>")) {
                return stripHtml(raw);
            }
            return raw;
        }
        if (comp instanceof AbstractButton btn) {
            return btn.getText();
        }
        if (comp instanceof JOptionPane optionPane) {
            Object msg = optionPane.getMessage();
            return msg == null ? "" : String.valueOf(msg);
        }
        if (comp instanceof Frame f) {
            return f.getTitle();
        }
        if (comp instanceof Dialog d) {
            return d.getTitle();
        }
        return comp.getName() != null ? comp.getName() : "";
    }

    static String getWindowTitle(Window window) {
        if (window instanceof Frame f) {
            return f.getTitle();
        }
        if (window instanceof Dialog d) {
            return d.getTitle();
        }
        return "";
    }

    static String getPlaceholderText(Component comp) {
        if (comp instanceof JComponent jc) {
            Object val = jc.getClientProperty(FlatClientProperties.PLACEHOLDER_TEXT);
            return val != null ? String.valueOf(val) : null;
        }
        return null;
    }

    static JOptionPane findOptionPaneIn(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof JOptionPane pane) {
                return pane;
            }
            if (child instanceof Container sub) {
                JOptionPane nested = findOptionPaneIn(sub);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    static String stripHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "").replace("&nbsp;", " ").trim();
    }

    static <T> T runOnEdtAndReturn(Supplier<T> supplier) {
        if (SwingUtilities.isEventDispatchThread()) {
            return supplier.get();
        }
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    result.set(supplier.get());
                } catch (Throwable t) {
                    error.set(t);
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bi gian doan khi truy cap EDT", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
        if (error.get() != null) {
            throw new RuntimeException(error.get());
        }
        return result.get();
    }

    static void runOnEdtAndWait(Runnable task) {
        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Bi gian doan khi thuc thi tren EDT", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    static void sleepSilently(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class WinWindow extends UITestControl {
    public WinWindow() {
        super();
        this.searchProperties.put(PropertyNames.ControlType, "Window");
    }

    public WinWindow(UITestControl searchLimitContainer) {
        super(searchLimitContainer);
        this.searchProperties.put(PropertyNames.ControlType, "Window");
    }

    public void close() {
        Component comp = findNow();
        if (comp instanceof Window window) {
            runOnEdtAndWait(() -> {
                if (window instanceof JFrame frame) {
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                }
                window.dispose();
            });
        }
    }
}

class WinButton extends UITestControl {
    public WinButton(UITestControl searchLimitContainer) {
        super(searchLimitContainer);
        this.searchProperties.put(PropertyNames.ControlType, "Button");
    }

    public boolean isSelected() {
        Component comp = getComponent();
        return runOnEdtAndReturn(() -> comp instanceof AbstractButton btn && btn.isSelected());
    }
}

class WinEdit extends UITestControl {
    public WinEdit(UITestControl searchLimitContainer) {
        super(searchLimitContainer);
        this.searchProperties.put(PropertyNames.ControlType, "Edit");
    }

    public String getText() {
        Component comp = getComponent();
        return runOnEdtAndReturn(() -> comp instanceof JTextComponent tc ? tc.getText() : "");
    }

    public char getEchoChar() {
        Component comp = getComponent();
        return runOnEdtAndReturn(() -> comp instanceof JPasswordField pf ? pf.getEchoChar() : (char) 0);
    }
}

class WinText extends UITestControl {
    public WinText(UITestControl searchLimitContainer) {
        super(searchLimitContainer);
        this.searchProperties.put(PropertyNames.ControlType, "Text");
    }
}

class ApplicationUnderTest {
    private final Window window;

    private ApplicationUnderTest(Window window) {
        this.window = window;
    }

    public Window getWindow() {
        return window;
    }

    public static ApplicationUnderTest launch(String targetClass) {
        return launch(targetClass, targetClass);
    }

    public static ApplicationUnderTest launch(String targetClass, String alternateTarget) {
        ensureLookAndFeel();
        String effective = (targetClass != null && !targetClass.isBlank()) ? targetClass : alternateTarget;
        if (effective != null && effective.contains("ImportQuantityDiscountDialog")) {
            return launchModalDialog(() -> new ImportQuantityDiscountDialog(
                    null, "Nhập số lượng & chiết khấu NXB", 0, 0
            ));
        }
        return launchFrame(LoginFrame::new);
    }

    public static <T extends Window> ApplicationUnderTest launchFrame(Supplier<T> factory) {
        ensureLookAndFeel();
        AtomicReference<T> ref = new AtomicReference<>();
        UITestControl.runOnEdtAndWait(() -> {
            T w = factory.get();
            if (w instanceof JFrame frame) {
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            }
            w.setVisible(true);
            w.toFront();
            ref.set(w);
        });
        waitUntilShowing(ref.get(), UITestControl.DEFAULT_TIMEOUT_MS);
        return new ApplicationUnderTest(ref.get());
    }

    public static <T extends JDialog> ApplicationUnderTest launchModalDialog(Supplier<T> factory) {
        ensureLookAndFeel();
        AtomicReference<T> ref = new AtomicReference<>();
        UITestControl.runOnEdtAndWait(() -> ref.set(factory.get()));
        T dialog = ref.get();
        // Modal dialog blocks setVisible(true) inside EDT event pump -> must use invokeLater
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
        waitUntilShowing(dialog, UITestControl.DEFAULT_TIMEOUT_MS);
        return new ApplicationUnderTest(dialog);
    }

    public static void ensureLookAndFeel() {
        UITestControl.runOnEdtAndWait(() -> {
            if (!(UIManager.getLookAndFeel() instanceof FlatLightLaf)) {
                FlatLightLaf.setup();
            }
        });
    }

    public static void disposeAllWindows() {
        UITestControl.runOnEdtAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JFrame frame) {
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                }
                if (w.isDisplayable()) {
                    w.dispose();
                }
            }
            SharedData.currentUser = null;
            SharedData.userPermissions = new HashMap<>();
            ApiClient.clearAccessToken();
        });
    }

    public static JDialog waitForOptionPaneDialog(int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            JDialog dialog = UITestControl.runOnEdtAndReturn(() -> {
                Window[] windows = Window.getWindows();
                for (int i = windows.length - 1; i >= 0; i--) {
                    if (windows[i] instanceof JDialog d && d.isShowing()
                            && UITestControl.findOptionPaneIn(d) != null) {
                        return d;
                    }
                }
                return null;
            });
            if (dialog != null) {
                return dialog;
            }
            UITestControl.sleepSilently(UITestControl.POLL_INTERVAL_MS);
        }
        return null;
    }

    public static void closeOptionPaneDialog(JDialog dialog) {
        if (dialog == null) {
            return;
        }
        JButton okBtn = UITestControl.runOnEdtAndReturn(() -> findDefaultButtonInOptionPane(dialog));
        if (okBtn != null) {
            SwingUtilities.invokeLater(okBtn::doClick);
        } else {
            SwingUtilities.invokeLater(dialog::dispose);
        }
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline) {
            boolean stillShowing = UITestControl.runOnEdtAndReturn(dialog::isShowing);
            if (!stillShowing) {
                return;
            }
            UITestControl.sleepSilently(UITestControl.POLL_INTERVAL_MS);
        }
    }

    private static JButton findDefaultButtonInOptionPane(Container root) {
        List<JButton> buttons = new ArrayList<>();
        collectButtons(root, buttons);
        for (JButton b : buttons) {
            if ("OK".equalsIgnoreCase(b.getText()) || "Có".equalsIgnoreCase(b.getText())
                    || "Yes".equalsIgnoreCase(b.getText())) {
                return b;
            }
        }
        return buttons.isEmpty() ? null : buttons.get(0);
    }

    private static void collectButtons(Container container, List<JButton> out) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton b && b.isShowing()) {
                out.add(b);
            } else if (c instanceof Container sub) {
                collectButtons(sub, out);
            }
        }
    }

    private static void waitUntilShowing(Window window, int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            boolean showing = UITestControl.runOnEdtAndReturn(window::isShowing);
            if (showing) {
                return;
            }
            UITestControl.sleepSilently(UITestControl.POLL_INTERVAL_MS);
        }
    }
}

class Mouse {
    private Mouse() {
    }

    public static void click(UITestControl control) {
        click(control, new Point(10, 10));
    }

    public static void click(UITestControl control, Point point) {
        Component comp = control.getComponent();
        if (comp instanceof AbstractButton btn) {
            // Invoke asynchronously so modal JOptionPane opened inside ActionListener does not deadlock EDT wait
            SwingUtilities.invokeLater(() -> {
                btn.requestFocusInWindow();
                btn.doClick();
            });
            UITestControl.sleepSilently(180);
            return;
        }
        SwingUtilities.invokeLater(() -> {
            comp.requestFocusInWindow();
            int x = point != null ? Math.min(Math.max(1, point.x), Math.max(1, comp.getWidth() - 1)) : 5;
            int y = point != null ? Math.min(Math.max(1, point.y), Math.max(1, comp.getHeight() - 1)) : 5;
            long now = System.currentTimeMillis();
            comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_PRESSED, now, 0, x, y, 1, false, MouseEvent.BUTTON1));
            comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_RELEASED, now + 10, 0, x, y, 1, false, MouseEvent.BUTTON1));
            comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_CLICKED, now + 20, 0, x, y, 1, false, MouseEvent.BUTTON1));
        });
        UITestControl.sleepSilently(180);
    }
}

class Keyboard {
    private Keyboard() {
    }

    public static void sendKeys(UITestControl control, String text) {
        Component comp = control.getComponent();
        UITestControl.runOnEdtAndWait(() -> {
            comp.requestFocusInWindow();
            if (comp instanceof JTextComponent tc) {
                tc.setText(text);
                tc.setCaretPosition(text.length());
            }
        });
        UITestControl.sleepSilently(80);
    }
}
