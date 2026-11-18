@echo off
title Giai phong Cong 8080 (Stop API Server)
echo Dang kiem tra tien trinh chiem cong 8080...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Tim thay tien trinh PID %%a dang chay cong 8080. Dang dung tien trinh...
    taskkill /F /PID %%a
    echo [OK] Da dung thanh cong!
    goto :end
)
echo Khong co tien trinh nao dang chiem cong 8080.
:end
pause
