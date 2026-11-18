package com.bookstore.uitest;

import io.appium.java_client.windows.WindowsDriver;
import io.appium.java_client.windows.WindowsElement;
import org.junit.After;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.List;

/** Shared launch/attach helpers for the bookstore Swing desktop application. */
public abstract class WinAppDriverTestBase {
    protected static final String WINAPPDRIVER_URL = "http://127.0.0.1:4723";
    protected static final String APP_TITLE = "Ứng Dụng Quản Lý Bán Sách";
    protected WindowsDriver<WindowsElement> driver;
    private Process appProcess;

    protected void launchApplication() throws Exception {
        String jarPath = requiredEnv("BOOKSTORE_APP_JAR");
        File jarFile = new File(jarPath);
        if (!jarFile.isFile()) {
            throw new IllegalStateException("BOOKSTORE_APP_JAR không trỏ tới file JAR: " + jarFile);
        }

        File javaw = new File(System.getProperty("java.home"), "bin/javaw.exe");
        String javaCommand = javaw.isFile() ? javaw.getAbsolutePath() : "java";
        appProcess = new ProcessBuilder(javaCommand, "-jar", jarFile.getAbsolutePath())
                .directory(jarFile.getParentFile())
                .start();
    }

    protected WindowsDriver<WindowsElement> attachToWindow(String windowName, String expectedText) throws Exception {
        URL server = new URL(WINAPPDRIVER_URL);
        DesiredCapabilities rootCapabilities = new DesiredCapabilities();
        rootCapabilities.setCapability("app", "Root");

        WindowsDriver<WindowsElement> desktop = new WindowsDriver<>(server, rootCapabilities);
        try {
            WebDriverWait wait = new WebDriverWait(desktop, 30);
            wait.until(d -> {
                List<WebElement> matches = d.findElements(By.name(windowName));
                for (WebElement match : matches) {
                    String source = match.toString();
                    if (expectedText == null || source.contains(expectedText)) return true;
                }
                return desktop.getPageSource().contains(windowName)
                        && (expectedText == null || desktop.getPageSource().contains(expectedText));
            });

            WebElement window = desktop.findElement(By.name(windowName));
            String nativeHandle = window.getAttribute("NativeWindowHandle");
            if (nativeHandle == null || nativeHandle.isBlank()) {
                throw new IllegalStateException("Không lấy được NativeWindowHandle của cửa sổ: " + windowName
                        + ". Hãy kiểm tra Java Access Bridge và cây UI Automation.");
            }
            String hexHandle = Long.toHexString(Long.parseLong(nativeHandle));

            DesiredCapabilities appCapabilities = new DesiredCapabilities();
            appCapabilities.setCapability("appTopLevelWindow", hexHandle);
            WindowsDriver<WindowsElement> attached = new WindowsDriver<>(server, appCapabilities);
            new WebDriverWait(attached, 20).until(d -> d.getPageSource().contains(expectedText == null ? windowName : expectedText));
            return attached;
        } finally {
            desktop.quit();
        }
    }

    protected void loginWithTestAccount() throws Exception {
        String email = requiredEnv("BOOKSTORE_TEST_EMAIL");
        String password = requiredEnv("BOOKSTORE_TEST_PASSWORD");
        driver = attachToWindow(APP_TITLE, "Đăng nhập");
        driver.findElement(By.name("login.email")).sendKeys(email);
        driver.findElement(By.name("login.password")).sendKeys(password);
        driver.findElement(By.name("login.submit")).click();

        WebDriverWait wait = new WebDriverWait(driver, 20);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("Đăng nhập thành công")));
        driver.findElement(By.name("OK")).click();
        driver.quit();
        driver = attachToWindow(APP_TITLE, "Chào Mừng Trở Lại Hệ Thống");
    }

    protected static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Cần thiết lập biến môi trường " + name + " trước khi chạy test.");
        }
        return value;
    }

    @After
    public void cleanup() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) { }
        }
        if (appProcess != null && appProcess.isAlive()) {
            appProcess.destroy();
            try {
                if (!appProcess.waitFor(Duration.ofSeconds(5).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)) {
                    appProcess.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                appProcess.destroyForcibly();
            }
        }
    }
}
