# 🚀 HƯỚNG DẪN DEPLOY LÊN RENDER (FREE PLAN)

## 📋 Tổng quan

Hướng dẫn chi tiết deploy ứng dụng Spring Boot lên Render với plan Hobby (Free).

## 🎯 Yêu cầu

-   Tài khoản GitHub
-   Tài khoản Render (free)
-   Code đã được build thành công

## 📝 Bước 1: Chuẩn bị code

### ✅ Đã hoàn thành:

-   [x] Tạo `Dockerfile`
-   [x] Tạo `.dockerignore`
-   [x] Tạo `application-prod.properties`
-   [x] Build project thành công

## 🌐 Bước 2: Đăng ký Render

### 2.1. Truy cập Render

1. Mở trình duyệt và vào: https://render.com
2. Click **"Start deploying"** (nút xanh)
3. Chọn **"Sign up with GitHub"**

### 2.2. Cấp quyền cho Render

1. Chọn tài khoản GitHub của bạn
2. Authorize Render để truy cập repositories
3. Hoàn tất đăng ký

## 📦 Bước 3: Tạo Web Service

### 3.1. Tạo service mới

1. Trong Dashboard Render, click **"New +"**
2. Chọn **"Web Service"**
3. Click **"Connect"** bên cạnh repository GitHub của bạn

### 3.2. Cấu hình service

```
Name: japanese-learning-platform
Environment: Java
Region: Singapore (gần Việt Nam nhất)
Branch: main (hoặc master)
Root Directory: (để trống)
```

### 3.3. Build & Deploy Settings

```
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/japanese-learning-platform-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 🗄️ Bước 4: Tạo PostgreSQL Database

### 4.1. Tạo database

1. Trong Dashboard, click **"New +"**
2. Chọn **"PostgreSQL"**
3. Cấu hình:
    ```
    Name: japanese-learning-db
    Database: japanese_learning
    User: japanese_learning_user
    Region: Singapore
    ```

### 4.2. Lưu thông tin database

Sau khi tạo, Render sẽ cung cấp:

-   **Internal Database URL**: `postgresql://user:pass@host:port/database`
-   **External Database URL**: `postgresql://user:pass@host:port/database`

## ⚙️ Bước 5: Cấu hình Environment Variables

### 5.1. Vào Web Service Settings

1. Click vào service `japanese-learning-platform`
2. Tab **"Environment"**
3. Click **"Add Environment Variable"**

### 5.2. Thêm các biến môi trường

#### Database:

```
Key: DATABASE_URL
Value: [Internal Database URL từ bước 4]
```

```
Key: DB_USERNAME
Value: japanese_learning_user
```

```
Key: DB_PASSWORD
Value: [password từ database]
```

#### JWT:

```
Key: JWT_SECRET
Value: [tạo một chuỗi ngẫu nhiên dài 32 ký tự]
```

```
Key: JWT_EXPIRATION
Value: 86400000
```

#### Cloudinary:

```
Key: CLOUDINARY_CLOUD_NAME
Value: [cloud name của bạn]
```

```
Key: CLOUDINARY_API_KEY
Value: [api key của bạn]
```

```
Key: CLOUDINARY_API_SECRET
Value: [api secret của bạn]
```

#### Email (Gmail):

```
Key: SPRING_MAIL_USERNAME
Value: [email của bạn@gmail.com]
```

```
Key: SPRING_MAIL_PASSWORD
Value: [app password từ Gmail]
```

#### VNPay:

```
Key: VNPAY_TMN_CODE
Value: [tmn code của bạn]
```

```
Key: VNPAY_HASH_SECRET
Value: [hash secret của bạn]
```

```
Key: VNPAY_RETURN_URL
Value: [URL frontend của bạn]/vnpay-return
```

#### AI APIs:

```
Key: SPEECH_GEMINI_API_KEY
Value: [gemini api key của bạn]
```

#### Frontend URL:

```
Key: APP_FRONTEND_URL
Value: [URL frontend của bạn]
```

#### Spring Profile:

```
Key: SPRING_PROFILES_ACTIVE
Value: prod
```

## 🚀 Bước 6: Deploy

### 6.1. Trigger deploy

1. Click **"Manual Deploy"**
2. Chọn **"Deploy latest commit"**
3. Chờ quá trình build và deploy (5-10 phút)

### 6.2. Kiểm tra logs

1. Tab **"Logs"** để xem quá trình build
2. Nếu có lỗi, kiểm tra và sửa

## 🔗 Bước 7: Cấu hình Custom Domain (Optional)

### 7.1. Thêm domain

1. Tab **"Settings"**
2. Section **"Custom Domains"**
3. Click **"Add Domain"**
4. Nhập domain của bạn

### 7.2. Cấu hình DNS

Thêm CNAME record:

```
Type: CNAME
Name: api (hoặc subdomain khác)
Value: [render-url].onrender.com
```

## ✅ Bước 8: Test API

### 8.1. Test endpoints

```bash
# Health check
curl https://your-app.onrender.com/api/health

# Swagger UI
https://your-app.onrender.com/api/swagger-ui.html

# API docs
https://your-app.onrender.com/api/api-docs
```

### 8.2. Test database

```bash
# Test connection
curl https://your-app.onrender.com/api/courses
```

## 🔧 Troubleshooting

### Lỗi thường gặp:

#### 1. Build failed

-   Kiểm tra `pom.xml` có lỗi không
-   Kiểm tra Java version (cần Java 17)
-   Kiểm tra dependencies

#### 2. Database connection failed

-   Kiểm tra `DATABASE_URL` có đúng không
-   Kiểm tra database đã được tạo chưa
-   Kiểm tra network access

#### 3. Environment variables missing

-   Kiểm tra tất cả biến môi trường đã được set
-   Kiểm tra tên biến có đúng không

#### 4. Port binding error

-   Render tự động set `PORT` environment variable
-   Code đã được cấu hình để sử dụng `${PORT:8082}`

## 📊 Monitoring

### 8.1. Kiểm tra performance

-   Tab **"Metrics"** để xem CPU, Memory usage
-   Tab **"Logs"** để xem application logs

### 8.2. Auto-scaling

-   Free plan không có auto-scaling
-   Cần upgrade lên paid plan để có tính năng này

## 💰 Chi phí

### Free Plan (Hobby):

-   **$0/tháng** cho web service
-   **$0/tháng** cho PostgreSQL (free tier)
-   **Compute costs**: ~$7-10/tháng khi có traffic
-   **Tổng**: ~$7-10/tháng

### Upgrade lên Professional ($19/tháng):

-   Không bị sleep
-   Auto-scaling
-   Custom domains
-   SSL certificates
-   Better performance

## 🎉 Hoàn thành!

Sau khi deploy thành công:

1. ✅ Backend API đã hoạt động
2. ✅ Database đã được kết nối
3. ✅ Environment variables đã được cấu hình
4. ✅ SSL certificate tự động
5. ✅ Custom domain (nếu có)

**URL API**: `https://your-app.onrender.com/api`
**Swagger UI**: `https://your-app.onrender.com/api/swagger-ui.html`

## 🔄 Cập nhật code

Mỗi khi push code lên GitHub:

1. Render sẽ tự động detect changes
2. Trigger build và deploy mới
3. Không cần làm gì thêm

---

**Lưu ý**: Free plan có thể bị sleep sau 15 phút không có traffic. Để tránh sleep, có thể:

1. Sử dụng uptime monitoring service
2. Upgrade lên paid plan
3. Tạo cron job ping API định kỳ
