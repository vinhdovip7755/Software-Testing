@echo off
title BookStore Login UI Tests (13 Cases)
cd /d "%~dp0"

echo ===============================================================
echo   BOOKSTORE LOGIN UI TEST SUITE (WinAppDriver - 13 Cases)
echo ===============================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0WinAppDriver\run-login.ps1" -ManualOtp -Email "vinhyt7755@gmail.com" -Password "07072006" -ForgotEmail "lowres14@gmail.com" -ForgotPassword "01082006" %*

echo.
pause