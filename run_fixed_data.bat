@echo off
chcp 65001 >nul

echo 🚀 Chạy script tạo dữ liệu test đã sửa lỗi
echo ==========================================

REM Check if fixed SQL file exists
if not exist "create_fixed_test_data.sql" (
    echo ❌ Không tìm thấy file create_fixed_test_data.sql
    pause
    exit /b 1
)

echo ✅ Tìm thấy file create_fixed_test_data.sql
echo.

echo 💡 CHỌN CÁCH CHẠY:
echo 1. Chạy bằng psql command (nếu có)
echo 2. Mở file để copy vào pgAdmin
echo 3. Mở file để copy vào DBeaver
echo.

set /p "choice=Chọn (1/2/3): "

if "%choice%"=="1" (
    goto run_psql
) else if "%choice%"=="2" (
    goto open_pgadmin
) else if "%choice%"=="3" (
    goto open_dbeaver
) else (
    echo ❌ Lựa chọn không hợp lệ
    pause
    exit /b 1
)

:run_psql
echo.
echo 🔧 Chạy bằng psql command...
echo.

REM Check if psql is available
where psql >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ psql không có sẵn!
    echo 💡 Hãy chọn cách 2 hoặc 3
    pause
    exit /b 1
)

set /p "dbuser=Nhập database username (mặc định: postgres): "
if "%dbuser%"=="" set dbuser=postgres

set /p "dbname=Nhập database name (mặc định: japanese_learning_db): "
if "%dbname%"=="" set dbname=japanese_learning_db

echo.
echo ⏳ Đang chạy: psql -h localhost -U %dbuser% -d %dbname% -f create_fixed_test_data.sql
echo.

psql -h localhost -U %dbuser% -d %dbname% -f create_fixed_test_data.sql

if %errorlevel% equ 0 (
    echo.
    echo ✅ THÀNH CÔNG! Dữ liệu test đã được tạo
    echo.
    echo 🎯 Bạn có thể test ngay:
    echo   Frontend: http://localhost:3000
    echo   Backend:  http://localhost:8082
    echo.
    echo 🔑 Login credentials:
    echo   student@example.com / password123
    echo   akiko@example.com / password123 (tutor)
    echo.
    echo 📚 Tìm courses có chữ "TEST" trong tên
    echo.
) else (
    echo.
    echo ❌ Có lỗi xảy ra! 
    echo 💡 Hãy thử cách 2 hoặc 3 (pgAdmin/DBeaver)
    echo.
)
pause
exit /b 0

:open_pgadmin
echo.
echo 📂 Mở file SQL cho pgAdmin...
start notepad create_fixed_test_data.sql
echo.
echo 👨‍💻 HƯỚNG DẪN PGADMIN:
echo 1. Mở pgAdmin
echo 2. Connect to PostgreSQL server
echo 3. Right-click database "japanese_learning_db" → Query Tool
echo 4. Copy toàn bộ nội dung file đã mở ở Notepad
echo 5. Paste vào Query Tool
echo 6. Click Execute (F5)
echo.
pause
exit /b 0

:open_dbeaver
echo.
echo 📂 Mở file SQL cho DBeaver...
start notepad create_fixed_test_data.sql
echo.
echo 👨‍💻 HƯỚNG DẪN DBEAVER:
echo 1. Mở DBeaver
echo 2. Connect to PostgreSQL database
echo 3. New SQL Script (Ctrl+Shift+N)
echo 4. Copy toàn bộ nội dung file đã mở ở Notepad
echo 5. Paste vào SQL Script
echo 6. Execute (Ctrl+Enter)
echo.
pause
exit /b 0 