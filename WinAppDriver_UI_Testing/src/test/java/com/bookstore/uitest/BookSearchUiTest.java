package com.bookstore.uitest;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.Assert.assertTrue;

/** End-to-end smoke test for login followed by searching the sales book list. */
public class BookSearchUiTest extends WinAppDriverTestBase {
    @Before
    public void loginAndOpenMainWindow() throws Exception {
        launchApplication();
        loginWithTestAccount();
    }

    @Test
    public void searchBookByNameOrCodeShowsMatchingResult() {
        String keyword = requiredEnv("BOOKSTORE_SEARCH_KEYWORD");

        driver.findElement(By.name("Bán Hàng")).click();
        WebElement search = driver.findElement(By.name("selling.book-search"));
        search.clear();
        search.sendKeys(keyword);

        WebElement results = driver.findElement(By.name("selling.book-results"));
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(d -> results.getText().toLowerCase().contains(keyword.toLowerCase()));
        assertTrue("Bảng kết quả cần chứa sách khớp từ khóa",
                results.getText().toLowerCase().contains(keyword.toLowerCase()));
    }
}
