# Quản lý bài tập và hạn nộp cho sinh viên

**Nhóm 3 · Học phần Lập trình di động**

Ứng dụng Android giúp sinh viên theo dõi bài tập theo môn, xem hạn nộp và cập nhật tiến độ. Dữ liệu lưu cục bộ bằng SQLite. Sinh viên có thể sử dụng các chức năng quản lý bài tập khi thiết bị ngoại tuyến.

## Chức năng

- **Danh sách bài tập.** Xem hai tab **Cần làm** và **Đã hoàn thành**, lọc theo từng môn hoặc chọn **Tất cả môn học**. Danh sách được sắp xếp theo hạn nộp. Bài chưa hoàn thành đã qua hạn được gắn nhãn **Quá hạn**.
- **Thêm và sửa bài tập.** Nhập tên bài, chọn môn và hạn nộp. Mô tả là tùy chọn. Năm ô **Ngày / Tháng / Năm / Giờ / Phút** nằm trên một hàng và dùng giờ 24 giờ.
- **Quản lý môn học.** Thêm môn hoặc đổi tên môn đã chọn trong biểu mẫu bài tập. Dấu **×** cạnh tên môn mở hộp thoại xác nhận. Nếu chọn **Xóa**, app xóa môn và toàn bộ bài tập thuộc môn đó trong cùng một giao dịch SQLite.
- **Chi tiết bài tập.** Xem thông tin, chỉnh sửa, đổi trạng thái hoặc xóa bài sau khi xác nhận. Khi đổi trạng thái, app quay về trang chính và mở tab tương ứng.

## Cài đặt và chạy

Mở thư mục dự án hiện có trong Android Studio hoặc lấy mã nguồn từ nhánh `datlnt` của repository.

```bash
git clone --branch datlnt https://github.com/hieulefsdev-fs/mobile-development-teamwork-.git
cd mobile-development-teamwork-
```

1. Trong Android Studio, chọn **Open** và mở thư mục chứa `settings.gradle`.
2. Cài **Android SDK 37** trong **SDK Manager** nếu IDE yêu cầu, rồi chờ Gradle Sync hoàn tất. Dự án dùng **Gradle Wrapper 9.5.0**, **Android Gradle Plugin 9.3.3** và cấu hình JVM **25** cho Gradle daemon. Mã Java được biên dịch với mức tương thích **Java 11**.
3. Chọn cấu hình **app**, sau đó chọn máy ảo hoặc thiết bị Android **API 24 trở lên**. Bấm **Run ▶** để cài và mở ứng dụng.

Có thể build bản debug từ thư mục gốc trên Windows bằng lệnh sau.

```powershell
.\gradlew.bat :app:assembleDebug
```

APK sau khi build nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

## Sử dụng và kiểm thử nhanh

1. Bấm **Thêm bài tập**, nhập tên và chọn môn. Để tạo môn mới, bấm **Thêm môn học**, nhập tên rồi bấm **Lưu môn học**.
2. Bấm **Chọn hạn nộp**, điền ngày và giờ, bấm **Xác nhận ngày và giờ**, rồi **Lưu bài tập**. Bài mới xuất hiện trong tab **Cần làm**.
3. Mở bài vừa tạo và bấm **Chỉnh sửa**. Đổi hạn nộp rồi lưu. Màn hình chi tiết hiển thị hạn mới.
4. Bấm **Đánh dấu hoàn thành**. App quay về tab **Đã hoàn thành**. Có thể mở lại bài và chọn **Chuyển về Cần làm**.
5. Đóng và mở lại app để kiểm tra dữ liệu còn được lưu. Thử xóa một bài dùng để kiểm thử, chọn **Hủy** lần đầu để kiểm tra bài vẫn còn, rồi xác nhận xóa ở lần tiếp theo.

## Cơ sở dữ liệu SQLite

`MonHoc` lưu mã và tên môn. `BaiTap` lưu mã bài, mã môn, tên bài, mô tả, ngày nộp, giờ nộp và trạng thái. Một môn có thể có nhiều bài tập. Ngày lưu theo dạng `yyyy-MM-dd`, giờ theo dạng `HH:mm`. Trạng thái `0` là **Cần làm**, `1` là **Đã hoàn thành**.

Khi chạy lần đầu, app sao chép [tệp SQLite khởi tạo](app/src/main/assets/quan_ly_bai_tap.db) từ `assets` vào vùng dữ liệu riêng trên thiết bị. Từ lần mở tiếp theo, app đọc và ghi vào bản trên thiết bị. Khi nâng cấp cơ sở dữ liệu từ phiên bản 1 lên 2, các bài cũ nhận giờ nộp mặc định **23:59**.

### Xem dữ liệu đang lưu trên Pixel 5

1. Giữ app chạy trên Pixel 5 hoặc thiết bị **API 26 trở lên**. Trong Android Studio, vào **View → Tool Windows → App Inspection → Database Inspector**.
2. Chọn tiến trình `com.example.nhom3_quanlybaitapvahannopchosinhvien` đang chạy. Nếu menu có hai dòng giống nhau, chọn dòng đang hoạt động. Dòng có **[DETACHED]** là phiên đã ngắt kết nối.
3. Mở `quan_ly_bai_tap.db` rồi nhấp đúp vào `BaiTap` hoặc `MonHoc`. Bật **Live updates** để theo dõi thay đổi, hoặc tắt chế độ này và bấm **↻** để làm mới thủ công.

Để xem dữ liệu trên Windows, dùng **Export Database** trong Database Inspector rồi mở tệp vừa xuất bằng DB Browser for SQLite. Bản xuất phản ánh dữ liệu tại thời điểm xuất. Tệp trong `assets` chứa dữ liệu khởi tạo.

## Cấu trúc mã nguồn

Các lớp Java nằm trong package `com.example.nhom3_quanlybaitapvahannopchosinhvien`.

- `MainActivity.java` hiển thị danh sách bằng ListView, hai tab trạng thái và bộ lọc môn.
- `BaiTapFormActivity.java` xử lý thêm và sửa bài tập, nhập hạn nộp và quản lý môn học.
- `BaiTapDetailActivity.java` hiển thị chi tiết, đổi trạng thái và xác nhận xóa bài.
- `BaiTapActivity.java` mở cơ sở dữ liệu và cung cấp các hàm dùng chung cho màn hình.
- `DatabaseHelper.java` sao chép, nâng cấp và truy vấn SQLite thông qua `SQLiteOpenHelper`.
- [`app/src/main/res/layout`](app/src/main/res/layout) chứa giao diện XML. [`app/src/main/res/values`](app/src/main/res/values) chứa chuỗi, màu và chủ đề.

Repository của Nhóm 3 và nhánh phát triển `datlnt`. [Mã nguồn dự án](https://github.com/hieulefsdev-fs/mobile-development-teamwork-/tree/datlnt)
