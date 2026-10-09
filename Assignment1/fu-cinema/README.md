# FUCinemaBookingSystem – Cinema Ticket Booking System

Hệ thống đặt vé xem phim trực tuyến xây dựng theo kiến trúc Microservices với Spring Boot 4 / Spring Cloud 2025, sử dụng API Gateway (Spring Cloud Gateway WebMVC) làm single entry point, xác thực JWT HS256 tập trung và kết nối đa cơ sở dữ liệu (SQL Server, MongoDB, MySQL).

---

## 1. Kiến trúc hệ thống

| Dịch vụ | Cổng (Port) | Cơ sở dữ liệu | Trách nhiệm chính |
|---|---|---|---|
| **api-gateway** | `9000` | — | Single entry point, xác thực JWT, ủy quyền theo role, strip & forward header `X-User-*` |
| **customer-service** | `8081` | SQL Server 2022 (`cinema_customer`) | Đăng ký, đăng nhập, cấp JWT, quản lý thông tin khách hàng (BCrypt, Flyway, NVARCHAR Unicode) |
| **movie-service** | `8082` | MongoDB 7.0.5 (`cinema_movie`) | Quản lý thể loại, phòng chiếu, phim, lịch chiếu (Decimal128 giá vé, DataSeeder) |
| **booking-service** | `8083` | MySQL 8.3.0 (`cinema_booking`) | Đặt vé xem phim, sơ đồ ghế, lịch sử đặt vé, hủy vé, báo cáo doanh thu, gọi `movie-service` qua OpenFeign |

---

## 2. Danh sách tài khoản kiểm thử

| Vai trò | Email | Mật khẩu | Trạng thái | Ghi chú |
|---|---|---|---|---|
| **ADMIN** | `admin@fucinema.com` | `@@abc123@@` | ACTIVE | Quản trị viên hệ thống, CRUD phim, phòng, suất chiếu, xem báo cáo |
| **CUSTOMER** | `an@gmail.com` | `123456` | ACTIVE | Khách hàng mẫu (ID: 1) |
| **CUSTOMER** | `binh@gmail.com` | `123456` | ACTIVE | Khách hàng mẫu (ID: 2) |
| **CUSTOMER** | `chi@gmail.com` | `123456` | INACTIVE | Tài khoản bị vô hiệu hóa (dùng test chặn login 403) |

---

## 3. Hướng dẫn khởi chạy hệ thống

### 3.1 Khởi động Database với Docker Compose

Tại thư mục `fu-cinema`:

```bash
docker compose up -d
```

Kiểm tra trạng thái các container:
- `cinema-sqlserver`: cổng `1433` (DB: `cinema_customer`)
- `cinema-mongo`: cổng `27017` (DB: `cinema_movie`)
- `cinema-mysql`: cổng `3306` (DB: `cinema_booking`)

### 3.2 Khởi động các Microservices (theo thứ tự khuyến nghị)

Mở 4 terminal riêng biệt cho 4 dịch vụ:

```bash
# Terminal 1: customer-service (Port 8081)
cd customer-service
mvn spring-boot:run

# Terminal 2: movie-service (Port 8082)
cd movie-service
mvn spring-boot:run

# Terminal 3: booking-service (Port 8083)
cd booking-service
mvn spring-boot:run

# Terminal 4: api-gateway (Port 9000)
cd api-gateway
mvn spring-boot:run
```

---

## 4. Hướng dẫn kiểm thử tự động với Postman

Toàn bộ test suites bao quát 100% chức năng (F1 đến F10) và 16 ràng buộc nghiệp vụ (BR01 đến BR16) đã được đóng gói sẵn trong thư mục `postman/`:

1. Khởi động Postman Desktop.
2. **Import Environment:** File `postman/FUCinema-Local.postman_environment.json`.
3. **Import Collection:** File `postman/FUCinemaBookingSystem.postman_collection.json`.
4. Chọn active environment là **FUCinema-Local**.
5. Nhấp chuột phải vào Collection **FUCinemaBookingSystem** → chọn **Run collection**.
6. Giữ nguyên thứ tự 8 folder kiểm thử:
   - `01-Auth` (F1): Đăng nhập Admin, Customer, validation, token sai
   - `02-Customer` (F2, F3): Đăng ký, đổi mật khẩu, profile Unicode, Admin CRUD khách hàng
   - `03-Genre-Room` (F4): CRUD thể loại, phòng chiếu, kiểm tra ràng buộc xóa
   - `04-Movie` (F5): CRUD phim, tìm kiếm tiêu chí, kiểm tra tồn tại thể loại
   - `05-Showtime` (F6): Tạo lịch chiếu, kiểm tra trùng giờ, tính `endTime`, hủy suất
   - `06-Booking` (F7): Sơ đồ ghế, đặt vé, chống trùng ghế, kiểm tra phòng và trạng thái
   - `07-History-Cancel` (F8): Lịch sử đặt vé, phân quyền xem, hủy vé trước 2h, giải phóng ghế
   - `08-Report` (F9): Báo cáo doanh thu theo ngày, sắp xếp giảm dần, thống kê theo phim
7. Nhấp **Run FUCinemaBookingSystem** và xem kết quả Passed.

---

## 5. Ràng buộc nghiệp vụ (Business Rules) đã triển khai

- **BR01 & BR02**: BCrypt mã hóa mật khẩu, JWT HS256 cấp quyền phân biệt ADMIN / CUSTOMER.
- **BR03**: Không xóa thể loại / phòng chiếu khi đang có phim hoặc suất chiếu liên quan.
- **BR04 & BR05**: Suất chiếu tự tính `endTime = startTime + duration`, chống trùng giờ cùng phòng chiếu.
- **BR06**: Chỉ tạo suất chiếu cho phim đang chiếu (`NOW_SHOWING`) và phòng hoạt động (`ACTIVE`).
- **BR07 & BR08**: Đặt vé không trùng ghế trong cùng request; gọi kiểm tra suất chiếu qua OpenFeign.
- **BR09 & BR10**: Chặn đặt ghế đã bán của suất chiếu; giá vé và tổng tiền tính từ server.
- **BR11 & BR12**: Customer chỉ xem/hủy vé của mình; chỉ cho phép hủy trước giờ chiếu tối thiểu 2 giờ.
- **BR13**: Báo cáo doanh thu bắt buộc `startDate <= endDate`, sắp xếp doanh thu giảm dần.
- **BR14**: Xử lý lỗi FeignException trả về HTTP 503 khi Movie Service không khả dụng.
- **BR15**: Kiểm tra thủ công tính tồn tại của tham chiếu ObjectId trong MongoDB.
- **BR16**: Lưu trữ Unicode chuẩn `NVARCHAR` trong SQL Server, `Decimal128` trong MongoDB.
