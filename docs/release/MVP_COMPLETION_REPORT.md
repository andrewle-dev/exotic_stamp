# MVP Completion Report — Exotic Stamp

> Milestone: **M10 Release / Demo Readiness**  
> Date: 2026-06-25  
> Verdict: **PASS (stakeholder demo ready)** — **NOT** Play Store / production hardened

---

## Executive summary

Exotic Stamp MVP delivers an NFC-first metro stamp collection experience on Flutter Android with a Spring Boot backend. Core flows — auth, home, stations, scan/collect, stamp book, rewards, profile, photo share — are integrated against real APIs with backend-driven business outcomes.

Automated tests pass on both tiers. Release APK builds successfully. Stakeholder demo is **ready** with a physical Android device, LAN backend, and seeded dev data.

Production store release and horizontal backend deployment require items listed in `MVP_KNOWN_LIMITATIONS.md`.

---

## Completed mobile features

| Feature | Status | Notes |
|---------|--------|-------|
| Auth (login/register/logout/forgot) | Done | JWT + cookies, secure storage |
| Session restore / refresh | Done | Cookie refresh interceptor |
| Home dashboard | Done | Composed APIs |
| Stations list + detail | Done | Client merge for collected badge |
| NFC scan (primary) | Done | Physical device required |
| QR fallback | Done | Secondary UX |
| GPS verification + collect | Done | Backend validates range |
| Collect idempotency | Done | UUID per attempt |
| Collect status (timeout) | Done | M9B — "Kiểm tra trạng thái" |
| Stamp book + detail | Done | Merge workaround for detail |
| Rewards + voucher detail | Done | Redeem disabled |
| Profile + settings | Done | Composed stats |
| Photo share | Done | Local share + share-events |
| App shell / bottom nav | Done | NFC-first scan CTA |
| Backend-driven state policy | Done | No local stamp/reward issuance |

---

## Completed backend features

| Module | Status | Key endpoints |
|--------|--------|---------------|
| Auth / User | Done | login, register, refresh, logout, `/users/me` |
| RBAC / Admin | Done | Admin APIs |
| Metro | Done | lines, stations, scan resolve |
| Collection | Done | collect, status, stamp-book, progress, my-stamps |
| Campaigns | Done | active campaigns |
| Rewards | Done | milestones, my rewards, voucher detail |
| Community | Done | share-events |
| Monetization | Schema / partial | No mobile ad runtime |
| Dev demo seed | Done | `@Profile("dev")` only |
| Flyway migrations | Done | V1–V17 |
| File uploads | Done | `/uploads/**` local storage |
| Prod Swagger off | Done | `application-prod.yml` + test |

---

## API integration status

| Endpoint group | Mobile wired | Contract match |
|----------------|--------------|----------------|
| Auth | yes | yes |
| users/me | yes | yes |
| metro/lines, stations | yes | yes |
| metro/scan/resolve | yes | yes |
| collection/collect | yes | yes |
| collection/collect/status | yes | yes (M9B) |
| collection/stamp-book, progress, my-stamps | yes | yes |
| campaigns/active | yes | yes |
| rewards/my, milestones | yes | yes |
| community/share-events | yes | yes |
| home/summary | no | backend missing |
| rewards/redeem | intentionally not called | 410 by design |
| monetization ads | no | backend missing |

Contracts: `mobile/docs/api/MOBILE_API_CONTRACT.md`, `openapi.json` (synced from backend).

---

## Acceptance criteria (MVP)

| Criterion | Result |
|-----------|--------|
| NFC-first collect flow | **PASS** |
| Backend confirms stamps (no local fake) | **PASS** |
| GPS validation server-side | **PASS** |
| Duplicate handling | **PASS** |
| Timeout uncertain + status check | **PASS** |
| Stamp book from backend | **PASS** |
| Rewards/voucher from backend | **PASS** |
| Voucher present-code flow | **PASS** |
| Auth session lifecycle | **PASS** |
| Photo share without upload | **PASS** |
| Automated mobile tests | **PASS** (190 tests) |
| Automated backend tests | **PASS** (`mvn clean test`) |
| Release APK build | **PASS** |
| Play Store release | **FAIL** (signing, package ID, HTTPS) |
| Physical NFC QA | **PENDING** (manual, documented) |

---

## Test results summary (M10 run)

### Backend

```text
cd backend && ./mvnw clean test     → PASS (exit 0)
cd backend && ./mvnw clean package -DskipTests → PASS (JAR built)
```

### Mobile

```text
flutter pub get                     → PASS
dart format .                       → PASS
flutter analyze                     → PASS (no issues)
flutter test                        → PASS (190/190)
flutter build apk --debug           → PASS
flutter build apk --release         → PASS (80.5 MB)
flutter build apk --release --obfuscate --split-debug-info=build/symbols → PASS (77.1 MB)
```

**APK path:** `mobile/build/app/outputs/flutter-apk/app-release.apk`

**Integration tests:** not present (`integration_test/` folder missing).

---

## Release configuration audit

### Backend — PASS (with dev-only warnings)

- dev/prod profile separation: **yes**
- JWT from env: **yes** (no prod default)
- Swagger disabled prod: **yes**
- CORS explicit in prod: **yes**
- Demo seed `@Profile("dev")`: **yes**
- Upload path documented: **yes** (`/uploads/**`, `storage.*` in yml)
- Dev mail default in yml: **warn** (local only)

### Mobile — PASS (demo) / PARTIAL (store)

- API via dart-define: **yes**
- No token logs: **yes**
- Permissions declared: **yes** (internet, NFC, location, camera)
- Cleartext HTTP for LAN: **yes** (documented)
- Release signing: **debug only**
- applicationId: **com.example** (blocker for store)
- Icon/splash: **incomplete** (logo asset missing)

---

## Remaining API gaps

See `docs/release/MVP_KNOWN_LIMITATIONS.md`. Top items:

1. `GET /home/summary`
2. `GET /collection/my-stamps/{stampId}`
3. `rewardUnlocked` on collect response
4. Station `collected` on list API
5. Monetization impression/click APIs
6. Profile summary endpoint

---

## Production limitations

- HTTP cleartext on mobile for local demo
- Debug-signed release APK
- Local filesystem uploads (single instance)
- No actuator health probe
- Redis single point for auth session semantics
- No staging environment profile

---

## Next-phase recommendations

1. **Immediate (pre-external demo):** Physical NFC QA on 1–2 devices; install release APK with LAN `API_HOST`
2. **Short term:** Branded icon/splash; change `applicationId`; release keystore
3. **Backend:** `home/summary`, stamp detail by ID, profile summary
4. **Mobile:** HTTPS `ApiConfig`, remove cleartext for prod, `integration_test`
5. **Ops:** Actuator health, staging env, object storage for uploads

---

## Related documents

| Document | Path |
|----------|------|
| Backend deployment | `docs/release/BACKEND_DEPLOYMENT_CHECKLIST.md` |
| Mobile release | `mobile/docs/release/MOBILE_RELEASE_CHECKLIST.md` |
| Demo script | `docs/release/STAKEHOLDER_DEMO_SCRIPT.md` |
| Known limitations | `docs/release/MVP_KNOWN_LIMITATIONS.md` |
| E2E QA plan | `mobile/docs/qa/MVP_E2E_TEST_PLAN.md` |
| Demo seed | `backend/docs/DEMO_SEED.md` |

---

## Sign-off

| Gate | Status |
|------|--------|
| M10 automated QA | **PASS** |
| Stakeholder demo | **READY** (with physical device + LAN backend) |
| Play Store / production | **NOT READY** |
