# 📱 Quản Lý Bài Tập Và Hạn Nộp Cho Sinh Viên - Nhóm 3

Ứng dụng Android giúp sinh viên dễ dàng quản lý bài tập, theo dõi thời gian và hạn nộp của từng môn học, tránh tình trạng trễ hạn nộp bài.

---

## 🚀 Tính Năng Chính

* **📋 Quản lý danh sách bài tập:**
  * Phân loại bài tập theo 2 trạng thái: **Cần làm** và **Đã hoàn thành** qua giao diện TabLayout tiện lợi.
  * Lọc bài tập linh hoạt theo từng môn học hoặc xem tất cả môn.
  * Tự động phát hiện và cảnh báo bài tập **Quá hạn** nộp.

* **🔍 Xem chi tiết bài tập:**
  * Hiển thị đầy đủ thông tin: Tên bài tập, môn học, hạn nộp (ngày & giờ), mô tả chi tiết bài tập.
  * Đánh dấu bài tập là đã hoàn thành hoặc chuyển ngược lại về cần làm.
  * Xóa bài tập khỏi hệ thống với hộp thoại xác nhận an toàn.

* **✍️ Thêm & Chỉnh sửa bài tập:**
  * Thêm bài tập mới hoặc cập nhật nội dung bài tập đã có.
  * Tích hợp bộ chọn ngày (**DatePicker**) và giờ (**TimePicker** định dạng 24h).
  * Quản lý danh mục môn học: Cho phép thêm môn học mới hoặc đổi tên môn học trực tiếp trong biểu mẫu.

* **💾 Lưu trữ dữ liệu:**
  * Sử dụng cơ sở dữ liệu **SQLite** (`quan_ly_bai_tap.db`) lưu trữ dữ liệu hoàn toàn offline trên thiết bị.
  * Tự động khởi tạo và nâng cấp cấu trúc bảng dữ liệu mượt mà.

---

## 🛠️ Công Nghệ Sử Dụng

* **Ngôn ngữ:** Java (JDK 17+)
* **Nền tảng:** Android SDK (Min SDK 24 / Target SDK 34)
* **Giao diện (UI):** XML Layouts, Material Design 3 (`MaterialComponents`, `TabLayout`)
* **Lưu trữ dữ liệu:** SQLite Database (`SQLiteOpenHelper`)
* **Kiến trúc:** Android Activity-based Architecture with Contract Launchers (`ActivityResultLauncher`)

---

## 📂 Cấu Trúc Dự Án

```text
app/src/main/
├── java/com/example/nhom3_quanlybaitapvahannopchosinhvien/
│   ├── BaiTapActivity.java        # Activity cơ sở (Base Activity quản lý DB & Utilities)
│   ├── MainActivity.java          # Màn hình chính (Danh sách bài tập & Lọc môn)
│   ├── BaiTapDetailActivity.java  # Màn hình chi tiết bài tập & Xác nhận xóa
│   ├── BaiTapFormActivity.java    # Màn hình thêm / sửa bài tập & quản lý môn học
│   └── DatabaseHelper.java       # Quản lý cơ sở dữ liệu SQLite
├── assets/
│   └── quan_ly_bai_tap.db        # Cơ sở dữ liệu SQLite khởi tạo sẵn
└── res/
    ├── layout/                    # Các tập tin giao diện XML
    └── values/                    # Strings, Colors, Themes
```

---

## 💻 Hướng Dẫn Cài Đặt Và Chạy Ứng Dụng

1. **Cloning Repository:**
   ```bash
   git clone https://github.com/hieulefsdev-fs/mobile-development-teamwork-.git
   cd mobile-development-teamwork-
   git checkout datlnt
   ```

2. **Mở dự án trong Android Studio:**
   * Mở **Android Studio** (bản Jellyfish / Koala hoặc mới hơn).
   * Chọn **Open** và dẫn tới thư mục chứa dự án.
   * Chờ Gradle Sync hoàn tất các dependency.

3. **Chạy ứng dụng:**
   * Kết nối thiết bị Android thật (bật USB Debugging) hoặc chạy trên Máy ảo Android (Emulator SDK 24+).
   * Nhấn nút **Run 'app'** (`Shift + F10`) để cài đặt và chạy ứng dụng.

---

## 👥 Nhóm Thực Hiện

**Nhóm 3 - Học phần Lập trình di động**
* **Nhánh phát triển:** `datlnt`
* **Repository:** [mobile-development-teamwork-](https://github.com/hieulefsdev-fs/mobile-development-teamwork-)
