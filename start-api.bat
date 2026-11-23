@echo off
setlocal
title BookStore REST API Server (Port 8080)

if exist "%~dp0.env" (
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%~dp0.env") do (
        if not "%%~A"=="" if not "%%~B"=="" set "%%~A=%%~B"
    )
)
if defined BOOKSTORE_SMTP_PASSWORD set "BOOKSTORE_SMTP_PASSWORD=%BOOKSTORE_SMTP_PASSWORD: =%"

if not defined JAVA_HOME goto :java_on_path
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] JAVA_HOME khong hop le. Hay tro JAVA_HOME den thu muc cai JDK 17 tro len.
    goto :failed
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

:java_on_path
where java.exe >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Khong tim thay Java. Hay cai JDK 17 tro len va cau hinh JAVA_HOME hoac PATH.
    goto :failed
)
pushd "%~dp0"
if errorlevel 1 goto :failed

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
    where mvn.cmd >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Khong tim thay Maven. Hay them thu muc bin cua Maven vao PATH va mo lai terminal.
        goto :failed_in_repo
    )
    call mvn.cmd -f "%~dp0pom.xml" -pl api-server -am package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Build that bai. Xem loi Maven o tren.
        goto :failed_in_repo
    )
)
if not exist "%~dp0api-server\target\bookstore-api-1.0-SNAPSHOT.jar" (
    echo [ERROR] Khong tim thay file JAR cua API sau khi build.
    goto :failed_in_repo
)

echo [*] Nhan Ctrl + C hoac dong cua so nay de tat server.
echo.
java.exe -jar "%~dp0api-server\target\bookstore-api-1.0-SNAPSHOT.jar"
if errorlevel 1 goto :failed_in_repo
popd
pause
exit /b 0

:failed_in_repo
popd
:failed
pause
exit /b 1
