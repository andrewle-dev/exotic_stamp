# Exotic Stamp API

Spring Boot backend cho mobile app Metro Stamp.

## Chạy nhanh

```bash
# Từ root repo (Docker)
docker compose up -d postgres redis api

# Hoặc trực tiếp
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Tài liệu

- Kiến trúc & guardrails: `docs/`
- API contract (shared): `../docs/api/MOBILE_API_CONTRACT.md`
- Demo seed: `docs/DEMO_SEED.md`

## Cấu trúc mã nguồn

```
src/main/java/metro/ExoticStamp/
├── config/              # Security, seed, OpenAPI
├── modules/             # auth, metro, collection, reward, rbac, …
└── ExoticStampApplication.java
```

Mỗi module theo layered architecture: `domain` → `application` → `infrastructure` → `presentation`.
