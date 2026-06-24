# Exotic Stamp

Monorepo cho hệ thống Metro Stamp: Flutter mobile app và Spring Boot API.

## Tech Stack

- **Mobile:** Flutter (clean architecture, feature modules)
- **Backend:** Java 21, Spring Boot, Maven
- **Database:** PostgreSQL
- **Cache:** Redis

## Cấu trúc repository

```
exotic_stamp/
├── backend/          # Spring Boot API (Maven)
├── mobile/           # Flutter app
├── docs/             # Tài liệu dùng chung (API, product, release)
├── infra/            # Docker, env mẫu, script chạy local
└── docker-compose.yml
```

| Thư mục | Mô tả |
|---------|--------|
| `backend/` | API server, migrations Flyway, tests |
| `mobile/` | App người dùng cuối (auth, scan, stamp book, rewards, …) |
| `docs/api/` | API contract và OpenAPI spec |
| `docs/product/` | Product specs, backlog, architecture |
| `docs/release/` | Checklist deploy, demo script, MVP report |
| `infra/` | Dockerfile backend, env dev/local, scripts |

Tài liệu chi tiết theo từng phần:

- Backend: `backend/docs/`
- Mobile: `mobile/docs/`

## Chạy local

### Yêu cầu

- Docker Desktop
- Java 21 (nếu chạy backend ngoài Docker)
- Flutter SDK (cho mobile)

### 1. Khởi động PostgreSQL, Redis và API

Từ thư mục gốc repo:

```bash
docker compose up -d postgres redis api
```

Hoặc dùng script:

```bash
./infra/scripts/start-local.sh
```

Dừng stack:

```bash
./infra/scripts/stop-local.sh
```

API mặc định: `http://localhost:8080`  
Health: `http://localhost:8080/actuator/health`

### 2. Backend (không dùng Docker)

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Copy `backend/.env.example` thành `.env` nếu cần cấu hình thêm.

### 3. Mobile

```bash
cd mobile
flutter pub get
flutter run
```

## Demo seed

Xem `backend/docs/DEMO_SEED.md` để biết tài khoản demo và dữ liệu mẫu sau khi API khởi động.
