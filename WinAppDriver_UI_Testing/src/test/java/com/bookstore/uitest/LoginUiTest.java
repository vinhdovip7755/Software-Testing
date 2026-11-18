package com.bookstore.uitest;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertTrue;

/** Login validation test that does not require a database account. */
public class LoginUiTest extends WinAppDriverTestBase {
    @Before
    public void openLogin() throws Exception {
        launchApplication();
        driver = attachToWindow(APP_TITLE, "Đăng nhập");
    }

    @Test
    public void emptyEmailShowsValidationMessage() {
        driver.findElement(By.name("login.submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("Cảnh báo")));
        assertTrue("Cần hiện cảnh báo yêu cầu nhập email",
                driver.getPageSource().contains("Vui lòng nhập Email!"));
    }
}
