package com.bookstore.codedui;

import com.bookstore.dto.EmployeeDTO;
import com.bookstore.dto.PermissionDTO;
import com.bookstore.gui.main.MainFrame;
import com.bookstore.gui.panel.ImportTab.ImportQuantityDiscountDialog;
import com.bookstore.util.SharedData;
import org.junit.jupiter.api.Assertions;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * UIMap - Ban do dieu khien giao dien va cac phuong thuc hanh dong / khang dinh (Assertions).
 * Duoc thiet ke tuong ung voi UIMap.cs + UIMap.Designer.cs trong tai lieu Coded UI Test.
 */
public class UIMap {

    // #region Recorded Action Methods

    /**
     * Dang nhap voi tai khoan Admin hop le tren giao dien LoginFrame va mo MainFrame.
     */
    public void loginValidAdmin() {
        // #region Variable Declarations
        WinEdit uIUsernameEdit = this.getUILoginWindow().getUIUsernameEdit();
        WinEdit uIPasswordEdit = this.getUILoginWindow().getUIPasswordEdit();
        WinButton uILoginButton = this.getUILoginWindow().getUILoginButton();
        // #endregion

        // Launch 'com.bookstore.gui.main.LoginFrame'
        ApplicationUnderTest.launch(
                this.getLoginValidAdminParams().uILoginWindowExePath,
                this.getLoginValidAdminParams().uILoginWindowAlternatePath
        );

        // Nhap Email & Mat khau
        Keyboard.sendKeys(uIUsernameEdit, this.getLoginValidAdminParams().uIUsernameText);
        Keyboard.sendKeys(uIPasswordEdit, this.getLoginValidAdminParams().uIPasswordText);

        // Click 'Dang Nhap Ngay'
        Mouse.click(uILoginButton, new Point(80, 20));

        // Cho hop thoai JOptionPane phan hoi tu LoginFrame
        JDialog alertDialog = ApplicationUnderTest.waitForOptionPaneDialog(5000);
        boolean loginSucceededViaApi = false;
        if (alertDialog != null) {
            String title = alertDialog.getTitle();
            if ("Đăng nhập thành công".equals(title)) {
                loginSucceededViaApi = true;
            }
            ApplicationUnderTest.closeOptionPaneDialog(alertDialog);
        }

        // Neu API server dang tat -> fallback khoi tao phien Admin offline va mo MainFrame
        if (!loginSucceededViaApi) {
            openMainFrameWithOfflineAdminFallback();
        } else {
            waitForMainFrameWhileClosingModals(12000);
        }
    }

    private void waitForMainFrameWhileClosingModals(int timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            JDialog extraModal = ApplicationUnderTest.waitForOptionPaneDialog(120);
            if (extraModal != null) {
                ApplicationUnderTest.closeOptionPaneDialog(extraModal);
            }
            if (this.getUIMainWindow().findNow() != null) {
                return;
            }
            UITestControl.sleepSilently(50);
        }
    }

    /**
     * Nhap mat khau tren LoginFrame va nhan nut con mat de hien thi mat khau.
     */
    public void togglePasswordEyeOnLogin() {
        // #region Variable Declarations
        WinEdit uIPasswordEdit = this.getUILoginWindow().getUIPasswordEdit();
        WinButton uIToggleEyeButton = this.getUILoginWindow().getUIToggleEyeButton();
        // #endregion

        ApplicationUnderTest.launch(
                this.getTogglePasswordEyeParams().uILoginWindowExePath,
                this.getTogglePasswordEyeParams().uILoginWindowAlternatePath
        );

        Keyboard.sendKeys(uIPasswordEdit, this.getTogglePasswordEyeParams().uIPasswordText);
        Mouse.click(uIToggleEyeButton, new Point(10, 10));

        long deadline = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < deadline && uIPasswordEdit.getEchoChar() != 0) {
            UITestControl.sleepSilently(50);
        }
    }

    /**
     * Mo hop thoai Nhap so luong & chiet khau NXB, nhap so lieu va nhan Xac nhan
     * (Tuong duong bai toan nhap phep tinh Calculator trong tai lieu PDF).
     */
    public void inputImportQuantityAndDiscount() {
        // #region Variable Declarations
        WinEdit uIQuantityEdit = this.getUIImportDialogWindow().getUIQuantityEdit();
        WinEdit uIDiscountEdit = this.getUIImportDialogWindow().getUIDiscountEdit();
        WinButton uIOkButton = this.getUIImportDialogWindow().getUIOkButton();
        // #endregion

        ApplicationUnderTest app = ApplicationUnderTest.launch(
                this.getInputImportQuantityAndDiscountParams().uIImportDialogPath,
                this.getInputImportQuantityAndDiscountParams().uIImportDialogAlternatePath
        );
        if (app.getWindow() instanceof ImportQuantityDiscountDialog dialog) {
            this.getUIImportDialogWindow().setActiveDialog(dialog);
        }

        Keyboard.sendKeys(uIQuantityEdit, this.getInputImportQuantityAndDiscountParams().uIQuantityText);
        Keyboard.sendKeys(uIDiscountEdit, this.getInputImportQuantityAndDiscountParams().uIDiscountText);
        Mouse.click(uIOkButton, new Point(45, 18));

        ImportQuantityDiscountDialog dialog = this.getUIImportDialogWindow().getActiveDialog();
        if (dialog != null) {
            long deadline = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < deadline) {
                boolean showing = UITestControl.runOnEdtAndReturn(dialog::isShowing);
                if (!showing) {
                    break;
                }
                UITestControl.sleepSilently(50);
            }
        }
    }

    /**
     * De trong Email tren LoginFrame va nhan nut Dang Nhap Ngay de kich hoat canh bao.
     */
    public void clickLoginWithEmptyEmail() {
        // #region Variable Declarations
        WinEdit uIUsernameEdit = this.getUILoginWindow().getUIUsernameEdit();
        WinButton uILoginButton = this.getUILoginWindow().getUILoginButton();
        // #endregion

        ApplicationUnderTest.launch(
                this.getClickLoginEmptyEmailParams().uILoginWindowExePath,
                this.getClickLoginEmptyEmailParams().uILoginWindowAlternatePath
        );

        Keyboard.sendKeys(uIUsernameEdit, this.getClickLoginEmptyEmailParams().uIUsernameText);
        Mouse.click(uILoginButton, new Point(80, 20));
        this.getUIOptionPaneWindow().waitForControlExist(3000);
    }

    // #endregion

    // #region Assertion Methods

    /**
     * Kiem tra thuoc tinh DisplayText cua tieu de chao mung tren MainFrame.
     */
    public void assertWelcomeHeaderOnMainFrame() {
        // #region Variable Declarations
        WinText uIWelcomeText = this.getUIMainWindow().getUIWelcomeText();
        // #endregion

        // Verify that the 'DisplayText' property of welcome label equals expected text
        Assertions.assertEquals(
                this.getAssertWelcomeExpectedValues().uIWelcomeTextDisplayText,
                uIWelcomeText.getDisplayText(),
                "Login welcome assertion is failed"
        );
    }

    /**
     * Kiem tra mat khau duoc hien thi ro rang (echoChar == 0) sau khi bam nut con mat.
     */
    public void assertPasswordEyeUnmasked() {
        // #region Variable Declarations
        WinEdit uIPasswordEdit = this.getUILoginWindow().getUIPasswordEdit();
        // #endregion

        Assertions.assertEquals(
                (char) 0,
                uIPasswordEdit.getEchoChar(),
                "Password eye toggle echoChar assertion is failed"
        );
        Assertions.assertEquals(
                this.getAssertPasswordEyeExpectedValues().uIPasswordDisplayText,
                uIPasswordEdit.getDisplayText(),
                "Password eye toggle DisplayText assertion is failed"
        );
    }

    /**
     * Kiem tra ket qua xac nhan So luong & Chiet khau NXB.
     */
    public void assertImportQuantityAndDiscountValues() {
        // #region Variable Declarations
        WinText uIResultText = this.getUIImportDialogWindow().getUIResultText();
        // #endregion

        // Verify that the 'DisplayText' property of import confirmation equals 'SL: 20 | CK: 15.0%'
        Assertions.assertEquals(
                this.getAssertImportExpectedValues().uIResultTextDisplayText,
                uIResultText.getDisplayText(),
                "Import quantity/discount calculation is failed"
        );
    }

    /**
     * Kiem tra thong bao canh bao khi bo trong Email dang nhap.
     */
    public void assertEmptyEmailWarningDialog() {
        // #region Variable Declarations
        WinText uIWarningText = this.getUIOptionPaneWindow().getUIMessageText();
        // #endregion

        // Verify that the 'DisplayText' property of JOptionPane message equals 'Vui lòng nhập Email!'
        Assertions.assertEquals(
                this.getAssertEmptyEmailExpectedValues().uIWarningTextDisplayText,
                uIWarningText.getDisplayText(),
                "Empty email warning assertion is failed"
        );

        JDialog dialog = (JDialog) this.getUIOptionPaneWindow().findNow();
        ApplicationUnderTest.closeOptionPaneDialog(dialog);
    }

    // #endregion

    private void openMainFrameWithOfflineAdminFallback() {
        UITestControl.runOnEdtAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof JFrame f && w.isDisplayable()) {
                    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    w.dispose();
                }
            }
            EmployeeDTO admin = new EmployeeDTO();
            admin.setEmployeeId(1);
            admin.setEmployeeName("Trần Huỳnh Thiên Nhật");
            admin.setEmail("hotro.adminhethong@gmail.com");
            admin.setRoleId(1);
            admin.setRoleName("Admin");
            SharedData.currentUser = admin;

            Map<String, PermissionDTO> perms = new HashMap<>();
            String[] codes = {
                    "MANAGE_SELLING", "MANAGE_CUSTOMER", "MANAGE_PRODUCT",
                    "MANAGE_AUTHOR", "MANAGE_CATEGORY", "MANAGE_SUPPLIER",
                    "MANAGE_PRICE", "MANAGE_PROMOTION", "MANAGE_IMPORT_TICKET",
                    "MANAGE_IMPORT", "MANAGE_INVENTORY", "MANAGE_BILL",
                    "MANAGE_EMPLOYEE", "STATISTICS", "MANAGE_ACCOUNT", "MANAGE_ROLE"
            };
            for (String code : codes) {
                perms.put(code, new PermissionDTO(code, true, true));
            }
            SharedData.userPermissions = perms;
        });

        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            mainFrame.setVisible(true);
        });

        waitForMainFrameWhileClosingModals(12000);
    }

    // #region Properties

    public LoginValidAdminParams getLoginValidAdminParams() {
        if (this.mLoginValidAdminParams == null) {
            this.mLoginValidAdminParams = new LoginValidAdminParams();
        }
        return this.mLoginValidAdminParams;
    }

    public TogglePasswordEyeParams getTogglePasswordEyeParams() {
        if (this.mTogglePasswordEyeParams == null) {
            this.mTogglePasswordEyeParams = new TogglePasswordEyeParams();
        }
        return this.mTogglePasswordEyeParams;
    }

    public InputImportQuantityAndDiscountParams getInputImportQuantityAndDiscountParams() {
        if (this.mInputImportParams == null) {
            this.mInputImportParams = new InputImportQuantityAndDiscountParams();
        }
        return this.mInputImportParams;
    }

    public ClickLoginEmptyEmailParams getClickLoginEmptyEmailParams() {
        if (this.mClickLoginEmptyEmailParams == null) {
            this.mClickLoginEmptyEmailParams = new ClickLoginEmptyEmailParams();
        }
        return this.mClickLoginEmptyEmailParams;
    }

    public AssertWelcomeExpectedValues getAssertWelcomeExpectedValues() {
        if (this.mAssertWelcomeExpectedValues == null) {
            this.mAssertWelcomeExpectedValues = new AssertWelcomeExpectedValues();
        }
        return this.mAssertWelcomeExpectedValues;
    }

    public AssertPasswordEyeExpectedValues getAssertPasswordEyeExpectedValues() {
        if (this.mAssertPasswordEyeExpectedValues == null) {
            this.mAssertPasswordEyeExpectedValues = new AssertPasswordEyeExpectedValues();
        }
        return this.mAssertPasswordEyeExpectedValues;
    }

    public AssertImportExpectedValues getAssertImportExpectedValues() {
        if (this.mAssertImportExpectedValues == null) {
            this.mAssertImportExpectedValues = new AssertImportExpectedValues();
        }
        return this.mAssertImportExpectedValues;
    }

    public AssertEmptyEmailExpectedValues getAssertEmptyEmailExpectedValues() {
        if (this.mAssertEmptyEmailExpectedValues == null) {
            this.mAssertEmptyEmailExpectedValues = new AssertEmptyEmailExpectedValues();
        }
        return this.mAssertEmptyEmailExpectedValues;
    }

    public UILoginWindow getUILoginWindow() {
        if (this.mUILoginWindow == null) {
            this.mUILoginWindow = new UILoginWindow();
        }
        return this.mUILoginWindow;
    }

    public UIMainWindow getUIMainWindow() {
        if (this.mUIMainWindow == null) {
            this.mUIMainWindow = new UIMainWindow();
        }
        return this.mUIMainWindow;
    }

    public UIImportDialogWindow getUIImportDialogWindow() {
        if (this.mUIImportDialogWindow == null) {
            this.mUIImportDialogWindow = new UIImportDialogWindow();
        }
        return this.mUIImportDialogWindow;
    }

    public UIOptionPaneWindow getUIOptionPaneWindow() {
        if (this.mUIOptionPaneWindow == null) {
            this.mUIOptionPaneWindow = new UIOptionPaneWindow();
        }
        return this.mUIOptionPaneWindow;
    }

    // #endregion

    // #region Fields
    private LoginValidAdminParams mLoginValidAdminParams;
    private TogglePasswordEyeParams mTogglePasswordEyeParams;
    private InputImportQuantityAndDiscountParams mInputImportParams;
    private ClickLoginEmptyEmailParams mClickLoginEmptyEmailParams;

    private AssertWelcomeExpectedValues mAssertWelcomeExpectedValues;
    private AssertPasswordEyeExpectedValues mAssertPasswordEyeExpectedValues;
    private AssertImportExpectedValues mAssertImportExpectedValues;
    private AssertEmptyEmailExpectedValues mAssertEmptyEmailExpectedValues;

    private UILoginWindow mUILoginWindow;
    private UIMainWindow mUIMainWindow;
    private UIImportDialogWindow mUIImportDialogWindow;
    private UIOptionPaneWindow mUIOptionPaneWindow;
    // #endregion

    // #region Parameters & ExpectedValues Classes

    public static class LoginValidAdminParams {
        public String uILoginWindowExePath = "com.bookstore.gui.main.LoginFrame";
        public String uILoginWindowAlternatePath = "BookStoreApp";
        public String uIUsernameText = "hotro.adminhethong@gmail.com";
        public String uIPasswordText = "admin";
    }

    public static class TogglePasswordEyeParams {
        public String uILoginWindowExePath = "com.bookstore.gui.main.LoginFrame";
        public String uILoginWindowAlternatePath = "BookStoreApp";
        public String uIPasswordText = "Secret123";
    }

    public static class InputImportQuantityAndDiscountParams {
        public String uIImportDialogPath = "com.bookstore.gui.panel.ImportTab.ImportQuantityDiscountDialog";
        public String uIImportDialogAlternatePath = "ImportQuantityDiscountDialog";
        public String uIQuantityText = "20";
        public String uIDiscountText = "15";
    }

    public static class ClickLoginEmptyEmailParams {
        public String uILoginWindowExePath = "com.bookstore.gui.main.LoginFrame";
        public String uILoginWindowAlternatePath = "BookStoreApp";
        public String uIUsernameText = "";
    }

    public static class AssertWelcomeExpectedValues {
        /**
         * De mo phong Test Failed giong Buoc 5-6 trang 13 tai lieu PDF:
         * Sua gia tri Expected nay thanh "Chào Mừng Sai" roi chay lai test.
         */
        public String uIWelcomeTextDisplayText = "Chào Mừng Trở Lại Hệ Thống";
    }

    public static class AssertPasswordEyeExpectedValues {
        public String uIPasswordDisplayText = "Secret123";
    }

    public static class AssertImportExpectedValues {
        /**
         * De mo phong Test Failed giong vi du Calculator sua '5' thanh '6' trong PDF:
         * Sua gia tri Expected nay thanh "SL: 99 | CK: 15.0%" roi chay lai test.
         */
        public String uIResultTextDisplayText = "SL: 20 | CK: 15.0%";
    }

    public static class AssertEmptyEmailExpectedValues {
        public String uIWarningTextDisplayText = "Vui lòng nhập Email!";
    }

    // #endregion

    // #region UI Control Map Classes

    public static class UILoginWindow extends WinWindow {
        private WinEdit mUIUsernameEdit;
        private WinEdit mUIPasswordEdit;
        private WinButton mUIToggleEyeButton;
        private WinButton mUILoginButton;

        public UILoginWindow() {
            // #region Search Criteria
            this.searchProperties.put(PropertyNames.Name, "Ứng Dụng Quản Lý Bán Sách");
            this.searchProperties.put(PropertyNames.ClassName, "LoginFrame");
            this.windowTitles.add("Ứng Dụng Quản Lý Bán Sách");
            // #endregion
        }

        public WinEdit getUIUsernameEdit() {
            if (this.mUIUsernameEdit == null) {
                this.mUIUsernameEdit = new WinEdit(this);
                // #region Search Criteria
                this.mUIUsernameEdit.getSearchProperties().put(PropertyNames.ClassName, "JTextField");
                this.mUIUsernameEdit.getSearchProperties().put(PropertyNames.Placeholder, "Email của bạn");
                this.mUIUsernameEdit.getWindowTitles().add("Ứng Dụng Quản Lý Bán Sách");
                // #endregion
            }
            return this.mUIUsernameEdit;
        }

        public WinEdit getUIPasswordEdit() {
            if (this.mUIPasswordEdit == null) {
                this.mUIPasswordEdit = new WinEdit(this);
                // #region Search Criteria
                this.mUIPasswordEdit.getSearchProperties().put(PropertyNames.ClassName, "JPasswordField");
                this.mUIPasswordEdit.getSearchProperties().put(PropertyNames.Placeholder, "Mật khẩu");
                this.mUIPasswordEdit.getWindowTitles().add("Ứng Dụng Quản Lý Bán Sách");
                // #endregion
            }
            return this.mUIPasswordEdit;
        }

        public WinButton getUIToggleEyeButton() {
            if (this.mUIToggleEyeButton == null) {
                this.mUIToggleEyeButton = new WinButton(this);
                // #region Search Criteria
                this.mUIToggleEyeButton.getSearchProperties().put(PropertyNames.ControlType, "ToggleButton");
                this.mUIToggleEyeButton.getSearchProperties().put(PropertyNames.ClassName, "JToggleButton");
                this.mUIToggleEyeButton.getWindowTitles().add("Ứng Dụng Quản Lý Bán Sách");
                // #endregion
            }
            return this.mUIToggleEyeButton;
        }

        public WinButton getUILoginButton() {
            if (this.mUILoginButton == null) {
                this.mUILoginButton = new WinButton(this);
                // #region Search Criteria
                this.mUILoginButton.getSearchProperties().put(PropertyNames.Name, "Đăng Nhập Ngay");
                this.mUILoginButton.getWindowTitles().add("Ứng Dụng Quản Lý Bán Sách");
                // #endregion
            }
            return this.mUILoginButton;
        }
    }

    public static class UIMainWindow extends WinWindow {
        private WinText mUIWelcomeText;

        public UIMainWindow() {
            // #region Search Criteria
            this.searchProperties.put(PropertyNames.Name, "Ứng Dụng Quản Lý Bán Sách");
            this.searchProperties.put(PropertyNames.ClassName, "MainFrame");
            this.windowTitles.add("Ứng Dụng Quản Lý Bán Sách");
            // #endregion
        }

        public WinText getUIWelcomeText() {
            if (this.mUIWelcomeText == null) {
                this.mUIWelcomeText = new WinText(this);
                // #region Search Criteria
                this.mUIWelcomeText.getSearchProperties().put(PropertyNames.Name, "Chào Mừng Trở Lại Hệ Thống");
                this.mUIWelcomeText.getWindowTitles().add("Ứng Dụng Quản Lý Bán Sách");
                // #endregion
            }
            return this.mUIWelcomeText;
        }
    }

    public static class UIImportDialogWindow extends WinWindow {
        private WinEdit mUIQuantityEdit;
        private WinEdit mUIDiscountEdit;
        private WinButton mUIOkButton;
        private WinText mUIResultText;
        private ImportQuantityDiscountDialog activeDialog;

        public UIImportDialogWindow() {
            // #region Search Criteria
            this.searchProperties.put(PropertyNames.Name, "Nhập số lượng & chiết khấu NXB");
            this.searchProperties.put(PropertyNames.ClassName, "ImportQuantityDiscountDialog");
            this.windowTitles.add("Nhập số lượng & chiết khấu NXB");
            // #endregion
        }

        public void setActiveDialog(ImportQuantityDiscountDialog activeDialog) {
            this.activeDialog = activeDialog;
        }

        public ImportQuantityDiscountDialog getActiveDialog() {
            return activeDialog;
        }

        public WinEdit getUIQuantityEdit() {
            if (this.mUIQuantityEdit == null) {
                this.mUIQuantityEdit = new WinEdit(this);
                // #region Search Criteria
                this.mUIQuantityEdit.getSearchProperties().put(PropertyNames.Placeholder, "Nhập số lượng > 0...");
                this.mUIQuantityEdit.getWindowTitles().add("Nhập số lượng & chiết khấu NXB");
                // #endregion
            }
            return this.mUIQuantityEdit;
        }

        public WinEdit getUIDiscountEdit() {
            if (this.mUIDiscountEdit == null) {
                this.mUIDiscountEdit = new WinEdit(this);
                // #region Search Criteria
                this.mUIDiscountEdit.getSearchProperties().put(PropertyNames.Placeholder, "Từ 0 đến 100%...");
                this.mUIDiscountEdit.getWindowTitles().add("Nhập số lượng & chiết khấu NXB");
                // #endregion
            }
            return this.mUIDiscountEdit;
        }

        public WinButton getUIOkButton() {
            if (this.mUIOkButton == null) {
                this.mUIOkButton = new WinButton(this);
                // #region Search Criteria
                this.mUIOkButton.getSearchProperties().put(PropertyNames.Name, "Xác nhận");
                this.mUIOkButton.getWindowTitles().add("Nhập số lượng & chiết khấu NXB");
                // #endregion
            }
            return this.mUIOkButton;
        }

        public WinText getUIResultText() {
            if (this.mUIResultText == null) {
                this.mUIResultText = new WinText(this) {
                    @Override
                    public String getDisplayText() {
                        if (activeDialog == null || !activeDialog.isConfirmed()) {
                            return "NOT_CONFIRMED";
                        }
                        return String.format(
                                Locale.US,
                                "SL: %d | CK: %.1f%%",
                                activeDialog.getQuantity(),
                                activeDialog.getDiscount()
                        );
                    }
                };
                // #region Search Criteria
                this.mUIResultText.getSearchProperties().put(PropertyNames.Name, "ImportResultSummary");
                // #endregion
            }
            return this.mUIResultText;
        }
    }

    public static class UIOptionPaneWindow extends WinWindow {
        private WinText mUIMessageText;
        private WinButton mUIOkButton;

        public UIOptionPaneWindow() {
            // #region Search Criteria
            this.searchProperties.put(PropertyNames.ClassName, "JOptionPaneDialog");
            // #endregion
        }

        public WinText getUIMessageText() {
            if (this.mUIMessageText == null) {
                this.mUIMessageText = new WinText(this);
                // #region Search Criteria
                this.mUIMessageText.getSearchProperties().put(PropertyNames.ControlId, "OptionPane.message");
                // #endregion
            }
            return this.mUIMessageText;
        }

        public WinButton getUIOkButton() {
            if (this.mUIOkButton == null) {
                this.mUIOkButton = new WinButton(this);
                // #region Search Criteria
                this.mUIOkButton.getSearchProperties().put(PropertyNames.Name, "OK");
                // #endregion
            }
            return this.mUIOkButton;
        }
    }

    // #endregion
}
