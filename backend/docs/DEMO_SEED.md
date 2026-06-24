# MVP Demo Seed (dev profile)

Demo data is loaded automatically on startup when Spring profile **`dev`** is active.

## Bootstrap order

1. Flyway migrations (`V1`–`V17`)
2. `MetroLineSeeder` — Metro Line 1 (`M1`) + 14 stations with NFC/QR scan keys
3. `CollectionBootstrapper` — default campaign per active line
4. `AdminSeedBootstrap` — admin user (`admin`)
5. `MvpDemoSeedBootstrap` — campaign stations, stamp designs, milestones, vouchers, mobile test user

## Start locally

```bash
# PostgreSQL + Redis required
export SPRING_PROFILES_ACTIVE=dev
export JWT_SECRET=your-dev-jwt-secret-at-least-32-chars-long

mvn spring-boot:run
```

Or with explicit demo password:

```bash
export DEMO_USER_PASSWORD=TestPass123!
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Seeded entities

| Entity | Details |
|--------|---------|
| Metro line | `M1` — Metro Line 1 (Ben Thanh - Suoi Tien) |
| Stations | 14 active stations; first **5** linked to default campaign |
| Scan keys (station 1–5) | NFC: `M1-NFC-001` … `M1-NFC-005` · QR: `M1-QR-001` … `M1-QR-005` |
| Campaign | Auto default campaign for line `M1` |
| Stamp designs | One active design per linked station |
| Partner | Demo Coffee Partner |
| Milestones | `M1` (1 stamp), `M3` (3 stamps), `M5` (5 stamps) |
| Voucher pool | 10 demo codes per voucher milestone (`DEMO-M3-001`, etc.) |
| Admin | `admin` / `application.bootstrap.admin-password` (default `changeme-dev-only`) |
| Mobile test user | `mobiletest` / `application.bootstrap.demo-user-password` (default `changeme-demo-only`) |

## Manual collect test (Swagger or curl)

1. `POST /api/v1/auth/login` with `mobiletest` credentials.
2. Authorize Swagger with Bearer token.
3. `POST /api/v1/collection/collect`:

```json
{
  "scanType": "NFC",
  "payload": "M1-NFC-001",
  "latitude": 10.772,
  "longitude": 106.698,
  "accuracyMeters": 35,
  "devicePlatform": "android",
  "appVersion": "1.0.0",
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440099"
}
```

Use each station's coordinates from `GET /api/v1/metro/stations/{id}` (within ~150 m zone).

4. After a timeout, poll outcome:  
   `GET /api/v1/collection/collect/status?idempotencyKey=550e8400-e29b-41d4-a716-446655440099`

5. Check rewards: `GET /api/v1/rewards/my` after collecting 1/3/5 stamps.

## Security notes

- Demo passwords are **dev-only** placeholders — never use in production.
- Public station APIs do **not** expose NFC/QR secrets (`includeSensitive=false`).
- Voucher codes are only returned on authenticated reward detail endpoints.

## CORS (dev)

Configured in `application-dev.yml`:

- `http://localhost:3000`
- `http://localhost:5173`
- Override via `CORS_ALLOWED_ORIGINS`

## Swagger

- Dev: `http://localhost:8080/swagger-ui/index.html`
- Prod: disabled (`springdoc.api-docs.enabled=false` in `application-prod.yml`)
