# 🚀 HƯỚNG DẪN TẠO DỮ LIỆU TEST

## 📋 Tổng quan

Hướng dẫn này sẽ giúp bạn tạo dữ liệu test cho hệ thống E-Learning tiếng Nhật.

## 🔧 Yêu cầu

-   Spring Boot đang chạy (port 8080)
-   PostgreSQL database đã được tạo
-   Tool quản lý database (pgAdmin, DBeaver, hoặc psql command line)

## 🏃‍♂️ Cách chạy nhanh

### **Bước 1: Start Spring Boot**

```bash
cd japanese-learning-platform
mvn spring-boot:run
```

### **Bước 2: Chạy script tạo dữ liệu**

#### **🪟 Windows Users:**

```cmd
# Chạy batch file hướng dẫn
run_create_data.bat

# Sau đó làm theo hướng dẫn để copy-paste SQL vào pgAdmin/DBeaver
```

#### **🐧 Linux/Mac Users:**

```bash
# Cách 1: Dùng script tự động (recommended)
chmod +x run_create_data.sh
./run_create_data.sh

# Cách 2: Chạy SQL trực tiếp
psql -h localhost -U your_username -d your_database -f create_simple_test_data.sql
```

## 🛠️ Chi tiết cho Windows

### **Option 1: pgAdmin (Recommended)**

1. Mở pgAdmin
2. Connect đến database `japanese_learning_db`
3. Right-click database → **Query Tool**
4. Copy toàn bộ nội dung file `create_simple_test_data.sql`
5. Paste vào Query Tool và click **Execute** (F5)

### **Option 2: DBeaver**

1. Connect đến PostgreSQL database
2. **New SQL Script** (Ctrl+Shift+N)
3. Copy nội dung file `create_simple_test_data.sql`
4. **Execute** script (Ctrl+Enter)

### **Option 3: IntelliJ IDEA/VSCode**

1. Install database plugin
2. Connect đến database
3. Open file `create_simple_test_data.sql`
4. Execute script

### **Option 4: Command line (nếu có psql)**

```cmd
psql -h localhost -U your_username -d japanese_learning_db -f create_simple_test_data.sql
```

## 📊 Dữ liệu được tạo

### 👨‍🏫 **6 Users**

| Email               | Role    | Password    | Mô tả                     |
| ------------------- | ------- | ----------- | ------------------------- |
| akiko@example.com   | TUTOR   | password123 | Chuyên Hiragana & Từ vựng |
| hiroshi@example.com | TUTOR   | password123 | Chuyên Katakana           |
| yuki@example.com    | TUTOR   | password123 | Chuyên Kanji              |
| kenji@example.com   | TUTOR   | password123 | Chuyên Ngữ pháp           |
| admin@example.com   | ADMIN   | password123 | Quản trị viên             |
| student@example.com | STUDENT | password123 | Học sinh test             |

### 📚 **5 Courses**

| Course                 | Level      | Price       | Tutor   |
| ---------------------- | ---------- | ----------- | ------- |
| Hiragana hoàn chỉnh    | BEGINNER   | 299,000 VND | Akiko   |
| Katakana hoàn chỉnh    | BEGINNER   | 249,000 VND | Hiroshi |
| Kanji cơ bản (100 chữ) | ELEMENTARY | 399,000 VND | Yuki    |
| Ngữ pháp N5            | ELEMENTARY | 499,000 VND | Kenji   |
| Từ vựng N5 (MIỄN PHÍ)  | BEGINNER   | 0 VND       | Akiko   |

### 🎯 **Exercises & Questions**

-   **FILL_IN_BLANK**: Điền ký tự vào chỗ trống (\_\_\_\_)
-   **MULTIPLE_CHOICE**: Trắc nghiệm 4 lựa chọn
-   **Questions**: Với hints và explanations
-   **Options**: Đúng/sai cho multiple choice

## 🧪 Testing

### **Frontend Testing**

1. Vào `http://localhost:3000`
2. Login với một trong các tài khoản trên
3. Chọn một course → lesson → exercises tab
4. Test các dạng bài tập:
    - **FILL_IN_BLANK**: User nhập đáp án vào ô trống
    - **MULTIPLE_CHOICE**: User chọn 1 trong 4 options

### **Backend API Testing**

```bash
# Get courses
curl http://localhost:8080/api/courses

# Get course detail
curl http://localhost:8080/api/courses/1

# Get exercises for a lesson
curl http://localhost:8080/api/lessons/1/exercises
```

## 🛠️ Troubleshooting

### **Lỗi: Server không chạy**

```bash
# Check nếu Spring Boot đang chạy
curl http://localhost:8080/api/courses

# Nếu không, start lại
cd japanese-learning-platform
mvn spring-boot:run
```

### **Lỗi: bash command not found (Windows)**

-   Dùng `run_create_data.bat` thay vì `.sh` file
-   Hoặc cài Git Bash, WSL, hoặc dùng GUI tools

### **Lỗi: psql command not found**

-   **Windows**: Cài PostgreSQL và thêm vào PATH, hoặc dùng pgAdmin/DBeaver
-   **Mac**: `brew install postgresql`
-   **Ubuntu**: `sudo apt-get install postgresql-client`

### **Lỗi: Permission denied**

```bash
# Give execute permission
chmod +x run_create_data.sh
```

### **Lỗi: Database connection**

1. Check `application.properties` có đúng database config
2. Đảm bảo PostgreSQL đang chạy
3. Database đã được tạo

## 📄 Files quan trọng

| File                          | Mô tả                     |
| ----------------------------- | ------------------------- |
| `create_simple_test_data.sql` | Script SQL tạo dữ liệu    |
| `run_create_data.sh`          | Script shell chạy tự động |
| `run_create_data.bat`         | Script batch cho Windows  |
| `QUICK_START_DATA.md`         | Hướng dẫn này             |

## 🔍 Kiểm tra dữ liệu

Sau khi chạy script, check trong database:

```sql
SELECT 'Users: ' || COUNT(*) FROM users;
SELECT 'Courses: ' || COUNT(*) FROM courses;
SELECT 'Exercises: ' || COUNT(*) FROM exercises;
SELECT 'Questions: ' || COUNT(*) FROM questions;
```

## 🎉 Kết quả mong đợi

Nếu thành công, bạn sẽ thấy:

-   ✅ Users created: 6
-   ✅ Tutors created: 4
-   ✅ Courses created: 5
-   ✅ Modules created: 3
-   ✅ Lessons created: 2
-   ✅ Exercises created: 2
-   ✅ Questions created: 2
-   ✅ Options created: 4

**🎯 Ready to test! Go to frontend and try the courses!**

## 🚀 Quick Commands Summary

### Windows:

```cmd
# Start server
mvn spring-boot:run

# Run data creation guide
run_create_data.bat
```

### Linux/Mac:

```bash
# Start server
mvn spring-boot:run

# Create data
chmod +x run_create_data.sh && ./run_create_data.sh
```
