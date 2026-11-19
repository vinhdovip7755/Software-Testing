@echo off
title UIMap - Coded UI Test Builder (Java Swing)
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.4\plugins\maven\lib\maven3\bin\mvn.cmd"

echo ===============================================================
echo   UIMap - CODED UI TEST BUILDER FOR BOOKSTORE MANAGER (SWING)
echo ===============================================================
echo.

cd /d "%~dp0"
echo [*] Dang bien dich test-classes...
call "%MVN_CMD%" -pl desktop-client test-compile -DskipTests
if errorlevel 1 (
    echo [!] Bien dich that bai.
    pause
    exit /b 1
)

if not exist "desktop-client\target\codedui-cp.txt" (
    echo [*] Dang tao classpath chay test...
    call "%MVN_CMD%" -pl desktop-client dependency:build-classpath -Dmdep.outputFile=target\codedui-cp.txt -DincludeScope=test -q
)

set "DEPS_CP="
for /f "usebackq delims=" %%A in ("desktop-client\target\codedui-cp.txt") do set "DEPS_CP=%%A"
set "FULL_CP=desktop-client\target\test-classes;desktop-client\target\classes;%DEPS_CP%"

echo [*] Dang mo giao dien CodedUITestBuilder...
"%JAVA_HOME%\bin\java.exe" -cp "%FULL_CP%" com.bookstore.codedui.CodedUITestBuilder
