@echo off
title BookStore REST API Server (Port 8080)
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

if exist "%~dp0.env" (
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%~dp0.env") do (
        if not "%%~A"=="" if not "%%~B"=="" set "%%~A=%%~B"
    )
)
if defined BOOKSTORE_SMTP_PASSWORD set "BOOKSTORE_SMTP_PASSWORD=%BOOKSTORE_SMTP_PASSWORD: =%"

echo ===============================================================
echo   BOOKSTORE REST API SERVER (Spring Boot 3.4.4 - Port 8080)
echo ===============================================================
if defined BOOKSTORE_SMTP_USERNAME (
    echo [*] Da nap cau hinh SMTP Gmail: %BOOKSTORE_SMTP_USERNAME%
) else (
    echo [!] Chua cau hinh BOOKSTORE_SMTP_USERNAME trong file .env ^(gui mail OTP se bao loi SMTP^).
)
echo.

if not exist "%~dp0api-server\target\bookstore-api-1.0-SNAPSHOT.jar" (
    echo [*] Dang build file JAR lan dau tien, vui long cho...
    call "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.4\plugins\maven\lib\maven3\bin\mvn.cmd" -f "%~dp0api-server\pom.xml" package -DskipTests
)

echo [*] Nhan Ctrl + C hoac dong cua so nay de tat server.
echo.
"%JAVA_HOME%\bin\java.exe" -jar "%~dp0api-server\target\bookstore-api-1.0-SNAPSHOT.jar"
pause
