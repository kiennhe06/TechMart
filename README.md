# TechMart - Ứng ứng di động mua sắm đồ công nghệ

TechMart là ứng dụng Android chuyên dùng để mua bán các thiết bị công nghệ (điện thoại, laptop, phụ kiện). Dự án này được thiết kế theo phong cách giao diện tối (dark mode) hiện đại và tích hợp đầy đủ quy trình mua sắm từ xem sản phẩm, giỏ hàng, thanh toán cho đến theo dõi đơn hàng và đánh giá.

⚠️ **Lưu ý**: Để ứng dụng chạy đầy đủ dữ liệu và chức năng, bạn cần khởi chạy máy chủ backend tại:
👉 **Mã nguồn Backend & Web Admin**: "https://github.com/kiennhe06/tech-mart-backend"
---

## 📱 Các tính năng nổi bật của App

### 1. Mua sắm & Flash Sale
* Giao diện trang chủ hiển thị danh sách banner và danh mục sản phẩm.
* Phần Flash Sale có đồng hồ đếm ngược thời gian thực và thanh hiển thị số lượng sản phẩm đã bán.
* Hỗ trợ tìm kiếm nhanh sản phẩm theo tên.

![Màn hình Trang chủ](<img width="1080" height="2400" alt="TrangChu" src="https://github.com/user-attachments/assets/71a104e7-0185-4457-aacb-d1af9f8d0b31" />
)

### 2. Xem chi tiết & Yêu thích
* Xem thông tin chi tiết, mô tả dài và danh sách thông số kỹ thuật (RAM, bộ nhớ, pin...).
* Thả tim (yêu thích) để lưu sản phẩm vào danh sách riêng, dữ liệu được lưu trên máy nên không bị mất khi tắt app.

![Màn hình Chi tiết sản phẩm](images/detail_screen.png)

### 3. Giỏ hàng & Thanh toán tự động (VietQR + Thẻ VISA)
* **Giỏ hàng**: Thêm, bớt số lượng, xóa sản phẩm và tự động tính tiền.
* **Chọn địa chỉ**: Chọn nhanh Tỉnh/Thành, Quận/Huyện, Xã/Phường bằng menu chọn tự động.
* **Thanh toán bằng Thẻ**: Nhập thông tin thẻ VISA trực quan, các thông tin sẽ hiển thị trực tiếp lên hình thẻ ảo 3D trên màn hình khi gõ.
* **Thanh toán VietQR**: Tự động sinh mã QR chuyển khoản chứa sẵn số tiền đơn hàng, thông tin tài khoản ngân hàng nhận tiền (`PHAM DUC KIEN` - Techcombank) và nội dung chuyển khoản động.
* **Mô phỏng giao dịch**: Hiển thị quá trình xử lý đơn hàng theo các bước bảo mật.

![Màn hình Thanh toán](images/checkout_screen.png)

### 4. Quản lý đơn hàng & Đánh giá
* Xem lại lịch sử các đơn hàng đã đặt.
* Theo dõi trạng thái đơn hàng (đang xử lý, đang giao, đã giao, đã hủy).
* Cho phép đánh giá số sao (1-5 sao) kèm bình luận nhận xét sau khi nhận hàng thành công.

![Màn hình Lịch sử đơn hàng](images/order_history.png)

### 5. Tài khoản cá nhân & Đăng nhập mạng xã hội (Firebase)
* **Đăng nhập truyền thống**: Đăng ký và đăng nhập nhanh bằng tài khoản Email và Mật khẩu cá nhân.
* **⚡ Đăng nhập một chạm (Social Login)**: Tích hợp nền tảng đám mây **Firebase** cho phép người dùng đăng nhập tức thì thông qua tài khoản **Google** và **Facebook** cực kỳ bảo mật và tiện lợi mà không cần ghi nhớ mật khẩu.
* **Cá nhân hóa hồ sơ**: Cập nhật trực tiếp họ tên hiển thị, số điện thoại giao hàng và hỗ trợ tải lên để thay đổi ảnh đại diện (avatar) cá nhân.

![Màn hình Cá nhân](images/profile_screen.png)

---

## 🛠️ Hướng dẫn cài đặt và chạy thử

### 1. Chuẩn bị
* Phần mềm Android Studio bản mới nhất.
* Thiết bị chạy: máy ảo Android (Emulator) hoặc điện thoại Android thật.
* Server Backend (nằm trong thư mục `techMartBackend`) đã khởi động thành công.

### 2. Cách chạy App
1. Mở thư mục `TechMart` này bằng Android Studio.
2. Đợi phần mềm đồng bộ (Gradle sync) hoàn tất.
3. Cấu hình IP kết nối server:
   * Mở file `RetrofitClient.kt` trong thư mục `network`.
   * Nếu dùng máy ảo Android Studio, giữ nguyên IP `"http://10.0.2.2:3000/"`.
   * Nếu dùng điện thoại thật, đổi thành IP Wi-Fi của máy tính bạn (ví dụ: `"http://192.168.1.15:3000/"`) và kết nối điện thoại chung Wi-Fi với máy tính.
4. Nhấn biểu tượng nút **Run** (mũi tên màu xanh) trong Android Studio để cài đặt và chạy app.
