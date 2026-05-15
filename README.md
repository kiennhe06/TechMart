# TechMart - Ứng dụng Thương mại Điện tử Hiện đại 🚀

TechMart là một ứng dụng Android mua sắm thiết bị công nghệ được xây dựng với công nghệ tiên tiến nhất, mang lại trải nghiệm mượt mà, giao diện "Premium" và các tính năng đầy đủ của một sàn thương mại điện tử.

## 📱 Tính năng chính

### 1. Phía Người dùng (Android App)
*   **Trang chủ (Home):** Banner động, danh mục sản phẩm, Flash Sale với thanh tiến trình bán hàng và danh sách sản phẩm gợi ý.
*   **Chi tiết sản phẩm:** Xem thông tin chi tiết, mô tả và thông số kỹ thuật tùy chỉnh từ Admin.
*   **Hệ thống Yêu thích (Wishlist):** Thả tym để lưu sản phẩm vào danh sách yêu thích. Dữ liệu được lưu trữ vĩnh viễn trên máy.
*   **Giỏ hàng (Shopping Cart):** Thêm sản phẩm, tăng/giảm số lượng, xóa sản phẩm và tính tổng tiền tự động.
*   **Hồ sơ người dùng:** Quản lý thông tin cá nhân và truy cập nhanh danh sách yêu thích.
*   **Tìm kiếm:** Tìm kiếm sản phẩm thông minh.

### 2. Phía Quản trị (Admin Web Panel)
*   **Quản lý sản phẩm:** Thêm, Sửa, Xóa sản phẩm trực quan trên nền tảng Web.
*   **Thông số kỹ thuật:** Tự tay nhập các thông số chi tiết (RAM, CPU, Pin...) cho từng sản phẩm.
*   **Giao diện Glassmorphism:** Thiết kế hiện đại, sang trọng với hiệu ứng kính mờ.

## 🛠 Công nghệ sử dụng

### Android (Mobile)
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Modern UI)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Networking:** Retrofit & OkHttp
*   **Image Loading:** Coil
*   **Local Storage:** SharedPreferences + Gson (Lưu trữ giỏ hàng và yêu thích vĩnh viễn)
*   **Navigation:** Jetpack Compose Navigation

### Backend (Server)
*   **Platform:** Node.js & Express
*   **Database:** JSON-based database (Dễ dàng di chuyển và quản lý)
*   **View Engine:** EJS

## 🚀 Hướng dẫn vận hành

### 1. Khởi chạy Backend
1. Mở thư mục `techMartBackend`.
2. Chạy lệnh: `npm install` để cài đặt thư viện.
3. Chạy lệnh: `npm start` để khởi động server tại cổng `3000`.

### 2. Khởi chạy Android App
1. Mở dự án trong Android Studio.
2. Đảm bảo Emulator của bạn có quyền truy cập Internet.
3. Địa chỉ IP kết nối mặc định: `http://10.0.2.2:3000` (Địa chỉ chuẩn để Emulator truy cập Localhost).
4. Nhấn **Run** và trải nghiệm!

## 📸 Giao diện ứng dụng
*(Bạn có thể tự chụp ảnh màn hình và thêm vào thư mục `screenshots` để README thêm sinh động nhé!)*

---
**Phát triển bởi:** Team TechMart
**Trạng thái dự án:** Đang hoàn thiện các tính năng nâng cao.
