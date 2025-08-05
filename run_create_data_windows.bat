@echo off
chcp 65001 >nul
REM ================================================
REM SCRIPT TẠO DỮ LIỆU TEST CHO WINDOWS
REM ================================================

echo 🚀 Tạo dữ liệu test cho hệ thống E-Learning
echo ==========================================

REM Check if Spring Boot is running on port 8082
echo 🔍 Kiểm tra server Spring Boot...
curl -s --max-time 3 "http://localhost:8082/api/courses" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Server đang chạy trên port 8082!
) else (
    echo ❌ Server không chạy!
    echo 💡 Vui lòng start Spring Boot trước:
    echo    mvn spring-boot:run
    pause
    exit /b 1
)

REM Check if SQL file exists
if not exist "create_simple_test_data.sql" (
    echo ❌ Không tìm thấy file create_simple_test_data.sql
    pause
    exit /b 1
)

echo.
echo 🛠️ Cách tạo dữ liệu test trên Windows:
echo.
echo 📋 Dữ liệu sẽ tạo:
echo   • 6 users (4 tutors, 1 admin, 1 student)
echo   • 5 courses: Hiragana, Katakana, Kanji, Ngữ pháp N5, Từ vựng N5
echo   • Modules, lessons, exercises với FILL_IN_BLANK và MULTIPLE_CHOICE
echo   • Questions và options cho bài tập
echo.

echo 💡 CHỌN MỘT TRONG CÁC CÁCH SAU:
echo.
echo 1️⃣ CÁCH 1: pgAdmin (Recommended)
echo    - Mở pgAdmin
echo    - Connect đến database PostgreSQL
echo    - Right-click database → Query Tool
echo    - Copy toàn bộ nội dung file: create_simple_test_data.sql
echo    - Paste vào Query Tool và Execute (F5)
echo.
echo 2️⃣ CÁCH 2: DBeaver
echo    - Connect đến PostgreSQL database
echo    - New SQL Script (Ctrl+Shift+N)
echo    - Copy nội dung file: create_simple_test_data.sql
echo    - Execute script (Ctrl+Enter)
echo.
echo 3️⃣ CÁCH 3: Command line (nếu có psql)
echo    psql -h localhost -U postgres -d japanese_learning_db -f create_simple_test_data.sql
echo.

set /p "choice=Chọn cách nào? (1/2/3) hoặc Enter để xem file SQL: "
echo.

if "%choice%"=="1" (
    echo 📂 Mở file SQL để copy...
    start notepad create_simple_test_data.sql
    echo.
    echo 👆 Copy toàn bộ nội dung file này vào pgAdmin Query Tool
    echo.
) else if "%choice%"=="2" (
    echo 📂 Mở file SQL để copy...
    start notepad create_simple_test_data.sql
    echo.
    echo 👆 Copy toàn bộ nội dung file này vào DBeaver SQL Script
    echo.
) else if "%choice%"=="3" (
    echo 🔧 Chạy psql command...
    set /p "dbuser=Nhập database username (mặc định: postgres): "
    if "%dbuser%"=="" set dbuser=postgres
    set /p "dbname=Nhập database name (mặc định: japanese_learning_db): "
    if "%dbname%"=="" set dbname=japanese_learning_db
    
    echo Đang chạy: psql -h localhost -U %dbuser% -d %dbname% -f create_simple_test_data.sql
    psql -h localhost -U %dbuser% -d %dbname% -f create_simple_test_data.sql
    
    if %errorlevel% equ 0 (
        goto success
    ) else (
        echo ❌ Lỗi khi chạy psql. Hãy thử cách 1 hoặc 2.
        pause
        exit /b 1
    )
) else (
    echo 📂 Mở file SQL để xem...
    start notepad create_simple_test_data.sql
)

echo 📄 File SQL location: %cd%\create_simple_test_data.sql
echo.
echo ⚠️  SAU KHI CHẠY SQL THÀNH CÔNG, bạn sẽ có:
echo.
echo 🎯 6 Users với password: password123
echo   • akiko@example.com (TUTOR) 
echo   • hiroshi@example.com (TUTOR)
echo   • yuki@example.com (TUTOR)
echo   • kenji@example.com (TUTOR)
echo   • admin@example.com (ADMIN)
echo   • student@example.com (STUDENT)
echo.
echo 🎯 5 Courses:
echo   • Hiragana hoàn chỉnh (299,000 VND)
echo   • Katakana hoàn chỉnh (249,000 VND)  
echo   • Kanji cơ bản (399,000 VND)
echo   • Ngữ pháp N5 (499,000 VND)
echo   • Từ vựng N5 - MIỄN PHÍ (0 VND)
echo.
echo 🎯 Exercises: FILL_IN_BLANK và MULTIPLE_CHOICE sẵn sàng test
echo.

pause
goto end

:success
echo.
echo ✅ TẠO DỮ LIỆU THÀNH CÔNG!
echo.
echo 🎉 Kiểm tra tại:
echo   Frontend: http://localhost:3000
echo   Backend:  http://localhost:8082
echo.
echo 💡 Test ngay:
echo   1. Vào http://localhost:3000
echo   2. Login: student@example.com / password123
echo   3. Chọn course → lesson → exercises tab
echo   4. Test FILL_IN_BLANK và MULTIPLE_CHOICE
echo.
pause

:end 