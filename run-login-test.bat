@echo off
title BookStore Login UI Tests (11 Cases)
cd /d "%~dp0"

echo ===============================================================
echo   BOOKSTORE LOGIN UI TEST SUITE (WinAppDriver - 11 Cases)
echo ===============================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0WinAppDriver_5Ca\run-login.ps1" -Email "vinhyt7755@gmail.com" -Password "07072006" %*

echo.
pause
