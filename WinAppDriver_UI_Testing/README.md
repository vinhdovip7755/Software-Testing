# WinAppDriver UI tests for BookStore

This is a small Java/JUnit test project for the Java Swing bookstore app. It contains:

1. A login validation test: submitting an empty email should show `Vui lòng nhập Email!`.
2. An end-to-end test: sign in with a test account, open **Bán Hàng**, search for a configured book name/code, and confirm it appears in the UI tree.

## Compatibility note

The app uses Java Swing. WinAppDriver targets Windows UI Automation, so first confirm that the Java controls are exposed to the UI Automation tree on the Windows machine. Apply `source-patch/ui-accessibility.patch` to give the test fields stable accessible names. If `attachToWindow` cannot see the app title or controls, inspect the window with Inspect.exe and enable/configure Java Access Bridge for the JDK used to run the app. This package has not been executed against the Windows app in this Linux environment.

## Prerequisites on Windows

- Windows 10/11 with the bookstore project and MySQL database available.
- JDK 17 and Maven.
- WinAppDriver installed and running at `127.0.0.1:4723`.
- The SQL schema/data loaded and a test account with permission to use **Bán Hàng**.
- Apply `source-patch/ui-accessibility.patch` from the bookstore project root, then build the app:

```powershell
mvn -DskipTests package
```

## Run

Start WinAppDriver in a separate Administrator terminal if required. In PowerShell, set these values (use your actual local paths and test data):

```powershell
$env:BOOKSTORE_APP_JAR = "C:\path\to\Software-Testing\target\BookStoreApp-jar-with-dependencies.jar"
$env:BOOKSTORE_TEST_EMAIL = "test-account@example.com"
$env:BOOKSTORE_TEST_PASSWORD = "your-test-password"
$env:BOOKSTORE_SEARCH_KEYWORD = "a book title or code present in the database"

mvn -f .\WinAppDriver_UI_Testing\pom.xml test
```

To run only login validation:

```powershell
mvn -f .\WinAppDriver_UI_Testing\pom.xml -Dtest=LoginUiTest test
```

The tests launch the JAR themselves. Keep the test account separate from a personal account; credentials are read from environment variables and are not saved in this project.

## Notes

- Test code uses Appium Java Client 7.6.0 with Selenium 3 APIs because WinAppDriver implements the legacy WebDriver protocol. Newer clients may not be compatible with this server.
- The login-only validation test does not require database credentials. The book-search smoke test requires MySQL, valid login credentials, product data, and the account's selling permission.
- If the Java Swing accessibility tree does not expose the labels/controls to WinAppDriver, the locators need to be adjusted based on Inspect.exe output; this is an app/tool compatibility check, not a test assertion failure.
