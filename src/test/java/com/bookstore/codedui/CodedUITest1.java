package com.bookstore.codedui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * CodedUITest1 - Bo kiem thu giao dien tu dong (Automated Coded UI Test) cho BookStoreManager (Java Swing).
 * Cau truc va luong goi ham tuong ung 1-1 voi CodedUITest1.cs trong tai lieu PDF:
 *   this.getUIMap().<ActionMethod>();
 *   this.getUIMap().<AssertMethod>();
 *
 * Huong dan demo Test Failed giong Buoc 5-6 trang 13 tai lieu PDF:
 *   - Mo file UIMap.java, sua gia tri AssertWelcomeExpectedValues.uIWelcomeTextDisplayText
 *     hoac AssertImportExpectedValues.uIResultTextDisplayText, sau do chay lai test de xem
 *     thong bao loi trong Test Explorer.
 */
@TestMethodOrder(MethodOrderer.DisplayName.class)
public class CodedUITest1 {

    private UIMap map;

    public CodedUITest1() {
    }

    @BeforeEach
    void myTestInitialize() {
        ApplicationUnderTest.disposeAllWindows();
        this.map = new UIMap();
    }

    @AfterEach
    void myTestCleanup() {
        ApplicationUnderTest.disposeAllWindows();
    }

    @Test
    @DisplayName("CodedUITestMethod1 - Dang nhap Admin va kiem tra DisplayText tren MainFrame")
    public void codedUITestMethod1() {
        this.getUIMap().loginValidAdmin();
        this.getUIMap().assertWelcomeHeaderOnMainFrame();
    }

    @Test
    @DisplayName("CodedUITestMethod2 - Kiem tra nut con mat an/hien mat khau tren LoginFrame")
    public void codedUITestMethod2() {
        this.getUIMap().togglePasswordEyeOnLogin();
        this.getUIMap().assertPasswordEyeUnmasked();
    }

    @Test
    @DisplayName("CodedUITestMethod3 - Nhap so luong & chiet khau NXB va kiem tra ket qua xac nhan")
    public void codedUITestMethod3() {
        this.getUIMap().inputImportQuantityAndDiscount();
        this.getUIMap().assertImportQuantityAndDiscountValues();
    }

    @Test
    @DisplayName("CodedUITestMethod4 - Bo trong Email dang nhap va kiem tra thong bao canh bao")
    public void codedUITestMethod4() {
        this.getUIMap().clickLoginWithEmptyEmail();
        this.getUIMap().assertEmptyEmailWarningDialog();
    }

    public UIMap getUIMap() {
        if (this.map == null) {
            this.map = new UIMap();
        }
        return this.map;
    }
}
