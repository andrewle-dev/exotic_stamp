# MVP Known Limitations & Backlog — Exotic Stamp

> Last updated: 2026-06-25 (M10)  
> Honest scope boundary for MVP demo vs production.

---

## API / backend gaps

| ID | Limitation | Impact | Priority |
|----|------------|--------|----------|
| L-API-01 | No `GET /api/v1/home/summary` | Home makes 4–5 parallel calls; partial failure handling required | P2 |
| L-API-02 | No `GET /api/v1/collection/my-stamps/{stampId}` | Stamp detail uses stamp-book + my-stamps merge workaround | P2 |
| L-API-03 | No `rewardUnlocked` / `nextReward` on collect response | Success screen cannot show instant reward unlock from collect alone | P2 |
| L-API-04 | Voucher **redeem** returns `410 REDEEM_NOT_SUPPORTED` | Present-code-at-counter only; no in-app redeem confirmation | P1 (by design for MVP) |
| L-API-05 | No dedicated `GET /api/v1/mobile/profile` summary | Profile stats composed from progress + share count | P2 |
| L-API-06 | Station list has no `collected` flag | Mobile merges `/metro/stations` + `/collection/stamp-book` client-side | P2 |
| L-API-07 | No production monetization ad endpoints | No pre-stamp ad impression/click tracking in mobile | P3 |
| L-API-08 | No `integration_test` mobile suite | Device E2E not automated in CI | P1 for post-MVP |

### Resolved since earlier milestones

| Item | Status |
|------|--------|
| `GET /api/v1/collection/collect/status` | **IMPLEMENTED** (backend + mobile M9B) — timeout recovery via "Kiểm tra trạng thái" |

---

## Mobile limitations

| ID | Limitation | Notes |
|----|------------|-------|
| L-MOB-01 | `applicationId` = `com.example.metro_stamp_app` | Not store-ready |
| L-MOB-02 | Release APK uses debug signing | OK for demo; not Play Store |
| L-MOB-03 | `usesCleartextTraffic=true` | LAN HTTP demo; disable for prod HTTPS |
| L-MOB-04 | `ApiConfig` builds `http://` URLs | HTTPS prod needs dart-define/flavor extension |
| L-MOB-05 | Logo asset path missing on disk | `flutter_launcher_icons` not run; default/mipmap icon |
| L-MOB-06 | Default white splash only | No branded splash screen |
| L-MOB-07 | Welcome screen is placeholder | Full onboarding UI deferred |
| L-MOB-08 | No persisted memories gallery | Photo share is ephemeral + share-events tracking only |
| L-MOB-09 | No image upload API | Memories do not upload photos to backend |
| L-MOB-10 | iOS NFC may be limited on test builds | QR fallback required |

---

## Operational / demo requirements

| ID | Requirement |
|----|-------------|
| L-OPS-01 | **NFC must be tested on physical Android device** — emulator insufficient |
| L-OPS-02 | Backend `dev` profile seeds demo users/passwords — never use in production |
| L-OPS-03 | Redis required for auth refresh/OTP at runtime |
| L-OPS-04 | Physical device needs `--dart-define=API_HOST=<LAN-IP>` for local backend |
| L-OPS-05 | Swagger disabled in prod — use exported `openapi.json` for contract |

---

## Security / release debt

| ID | Item | Severity |
|----|------|----------|
| L-SEC-01 | `application-dev.yml` contains dev-only mail password default | Low (dev profile only) |
| L-SEC-02 | `docker-compose.yml` uses weak local DB password | Low (local only) |
| L-SEC-03 | No Spring Actuator health endpoint | Medium for k8s probes |
| L-SEC-04 | Local file storage not multi-instance safe | Medium for horizontal scale |

---

## Backlog (next phase)

1. Play Store: package ID, release signing, branded icon/splash, HTTPS API config
2. `GET /home/summary` and `GET /profile/summary` mobile DTOs
3. `GET /collection/my-stamps/{stampId}` for stamp detail
4. Station `collected` flag on list API
5. `rewardUnlocked` on collect response + celebration screen wiring
6. Voucher redeem partner integration (when business rules ready)
7. `integration_test/` + CI device farm or manual QA gate
8. Monetization pre-stamp ad slot (backend + mobile)
9. Memories gallery (if product requires persistence)
10. Staging profile + environment matrix (dev/staging/prod)
11. Spring Actuator `/actuator/health` for deployments
12. Object storage adapter (S3) for uploads

---

## What MVP does well (not limitations)

- NFC-first scan with QR fallback
- Backend-driven collect, duplicate, GPS validation
- Idempotent collect + status polling on timeout
- No mock production stamps/rewards/vouchers in `lib/`
- Auth session with secure token storage
- Share-events tracking (best-effort, non-blocking)
