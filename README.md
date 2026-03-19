# ☕ Daily Coffee - Hệ thống Đặt Đồ Uống Trực Tuyến (App & Web)

**Daily Coffee** là một hệ thống đặt đồ uống trực tuyến toàn diện bao gồm hai thành phần chính: **Ứng dụng di động (Android)** dành cho khách hàng để đặt món và **Nền tảng Web (Admin Panel)** dành cho quản lý cửa hàng. Hệ thống được thiết kế nhằm mang lại trải nghiệm tiện lợi, mượt mà cho người dùng và cung cấp công cụ kiểm soát đơn hàng, doanh thu theo thời gian thực cho ban quản trị.

---

## ✨ Tính năng nổi bật (Key Features)

### 📱 Dành cho Khách hàng (Mobile App - Android)
- **Xác thực người dùng:** Đăng nhập/Đăng ký an toàn qua Email & Google, tích hợp tính năng Khôi phục mật khẩu.
- **Khám phá đồ uống:** Tìm kiếm, xem chi tiết sản phẩm (giá, kích cỡ, mô tả) và phân loại theo danh mục (Chocolate, Capuchino, Latte...).
- **Giỏ hàng & Thanh toán:** Tùy chỉnh số lượng, kích cỡ đồ uống. Hỗ trợ áp dụng mã giảm giá (Vouchers) và thanh toán đa nền tảng: **Tiền mặt (COD), PayPal, Ví điện tử MoMo**.
- **Theo dõi đơn hàng:** Cập nhật trạng thái đơn hàng theo thời gian thực (Đang xử lý, Đang giao, Đã giao).
- **Tương tác & Cá nhân hóa:** Thêm sản phẩm vào mục Yêu thích (Favorites), quản lý hồ sơ cá nhân và hệ thống Đánh giá/Bình luận (Rating & Review) sau khi nhận hàng.

### 💻 Dành cho Quản trị viên (Web Admin Panel)
- **Dashboard Thống kê:** Theo dõi tổng đơn hàng, tổng doanh thu, giá trị trung bình đơn và biểu đồ doanh thu theo tháng.
- **Quản lý Đơn hàng:** Xem chi tiết đơn hàng, duyệt đơn và cập nhật trạng thái vận chuyển.
- **Quản lý Sản phẩm & Danh mục:** Thêm, sửa, xóa đồ uống và phân loại danh mục.
- **Quản lý Khuyến mãi:** Tạo và quản lý các mã giảm giá (Vouchers), chiến dịch khuyến mãi (Promotions).
- **Quản lý Người dùng & Đánh giá:** Xem danh sách khách hàng, kiểm soát bình luận và phản hồi từ người dùng.

---

## 🛠 Công nghệ sử dụng (Tech Stack)

### Front-End
- **Mobile App:** Kotlin, Android Studio (UI Material Design).
- **Web Admin:** HTML5, CSS3, JavaScript.

### Back-End & Database
- **Cơ sở dữ liệu:** Firebase Realtime Database (Đồng bộ dữ liệu thời gian thực cho cả App và Web).
- **Xác thực:** Firebase Authentication.
- **Thông báo:** Firebase Cloud Messaging (FCM).
- **Lưu trữ đa phương tiện:** Cloudinary (Quản lý, tối ưu hóa hình ảnh sản phẩm) và Firebase Storage.

---

## 🏗 Kiến trúc Hệ thống (System Architecture)
Dự án được xây dựng với cấu trúc dữ liệu rõ ràng, bao gồm các thực thể chính:
- `Users`: Quản lý thông tin khách hàng và Admin.
- `Items` & `Categories`: Quản lý danh mục và chi tiết đồ uống.
- `Orders` & `Cart`: Xử lý luồng đặt hàng và giỏ hàng.
- `Vouchers` & `Promotions`: Quản lý logic tính toán giảm giá.
- `Comments`: Lưu trữ đánh giá của người dùng.
