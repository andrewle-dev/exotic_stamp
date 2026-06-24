# 13 — Stage Plan: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Founder / Tech Lead / Delivery Lead  
> Purpose: Convert product, feature, backlog, dependency, architecture, ERD, and API contract into implementation stages with acceptance gates.

---

## 1. Feasibility Check

The MVP can be delivered in staged form, but not if the team treats each stage as a UI milestone.

The stages must be ordered by data integrity and dependency risk:

```text
security foundation
↓
metro/station truth
↓
scan-key validity
↓
collection write correctness
↓
reward/voucher correctness
↓
mobile integration
↓
monetization/community
↓
production hardening
```

The delivery plan below assumes backend-first execution with Flutter integration once API contracts are stable.

---

## 2. Stage Overview

| Stage | Name | Primary Goal | MVP Criticality |
|---:|---|---|---:|
| 0 | Product & Technical Lock | Freeze documents and open decisions. | P0 |
| 1 | Foundation / Auth / RBAC | Secure user/admin foundation. | P0 |
| 2 | Metro Operations | Admin-controlled lines, stations, scan keys, assets. | P0 |
| 3 | Collection Core | Scan resolve, GPS validation, idempotent stamp collect. | P0 |
| 4 | Reward Engine | Milestones, rewards, voucher allocation. | P0/P1 |
| 5 | Mobile Integration | Flutter NFC/QR/GPS flow against real backend. | P0 |
| 6 | Monetization MVP Support | Pre-stamp ad contract and impression tracking if approved. | P1 |
| 7 | Community / Sharing Foundation | Share tracking, referral base, notification inbox. | P1/Phase 2 |
| 8 | Production Hardening | Observability, load, security, deployment, release. | P0 |

---

## 3. Stage 0 — Product & Technical Lock

### Objective

Ensure the team does not code against vague requirements.

### Inputs

- Product Overview.
- Product Analysis.
- Requirement Clarification.
- Feature Inventory.
- Feature Specification.
- MVP Scope.
- Epic List.
- Backlog.
- Dependency Map.
- Architecture Diagram.
- ERD.
- API Contract.
- Test Strategy.

### Deliverables

- All P0 requirements marked Confirmed/Locked.
- Open decisions list reduced to zero for P0 scope.
- Stage sequence accepted.
- Risk register reviewed.

### Exit criteria

- One-step vs two-step scan flow decided.
- Default campaign policy decided.
- GPS radius/accuracy policy decided.
- Milestone numbers decided.
- Reward/voucher MVP policy decided.
- Pre-stamp ad MVP status decided.

---

## 4. Stage 1 — Foundation / Auth / RBAC

### Objective

Deliver secure baseline for user and admin operations.

### Backend scope

- Auth register/login/refresh/logout.
- Email verification / forgot password if already in current scope.
- Access token revocation behavior.
- User profile read/update baseline.
- RBAC role/permission management.
- Security config and `@PreAuthorize` rules.
- Global exception handler.
- Swagger/OpenAPI baseline.
- Audit log baseline for sensitive actions.

### Mobile scope

- Login/register screens.
- Secure token storage.
- Authenticated API client.
- Refresh-token handling.
- Logout behavior.

### Admin scope

- Admin login.
- Protected admin layout.
- Permission-based UI hiding is optional; backend permission check is mandatory.

### QA scope

- Register/login/refresh/logout.
- Invalid password.
- Disabled user.
- Refresh token reuse.
- Access denied without permission.
- Token expiry and refresh.

### Exit criteria

- Admin and user tokens work.
- Permission-gated endpoints return `403` when role is missing.
- No sensitive auth data appears in API response.
- Automated tests pass.

---

## 5. Stage 2 — Metro Operations

### Objective

Make metro data admin-controlled and ready for collection.

### Backend scope

- Line CRUD.
- Station CRUD.
- Station status lifecycle.
- GPS metadata fields.
- Scan key management.
- Public station APIs.
- Public asset upload for station/stamp image.
- Cache invalidation after metro writes.
- Audit log for scan-key changes.

### Mobile scope

- Fetch lines.
- Fetch stations by line.
- Fetch station detail.
- Render placeholder stamp book metadata.

### Admin scope

- Manage line.
- Manage station.
- Upload station/stamp assets.
- Configure scan key status.

### QA scope

- Create/update/deactivate station.
- Invalid GPS values.
- Duplicate station code.
- Scan key rotation.
- Public API does not expose raw scan key.

### Exit criteria

- Admin can operate station data without code deploy.
- Public mobile can read active station data.
- Scan key lookup is indexed and cache-safe.

---

## 6. Stage 3 — Collection Core

### Objective

Deliver the core scan-to-stamp flow with correct anti-cheat and idempotency behavior.

### Backend scope

- Default campaign selection.
- Campaign-station eligibility.
- Stamp design lookup.
- Scan resolve.
- Collect stamp endpoint.
- GPS server-side validation.
- Duplicate scan handling.
- Idempotency key handling.
- Stamp book query.
- Stamp book cache and eviction.
- Collection audit metadata.

### Mobile scope

- NFC read.
- QR read fallback.
- GPS permission request.
- GPS accuracy capture.
- Collect request.
- Timeout/retry behavior.
- Stamp success screen.
- Stamp Book UI.

### Admin scope

- Validate campaign/station setup for collection.
- View basic collect count per station if available.

### QA scope

- NFC happy path.
- QR happy path.
- GPS out-of-range.
- Duplicate scan.
- Same idempotency retry.
- Concurrent duplicate requests.
- Station inactive.
- Campaign inactive.

### Exit criteria

- User can collect exactly one stamp per station per campaign.
- Duplicate/concurrent scans do not create duplicate rows.
- Stamp Book reflects successful collect.
- Backend controls campaign, station, and GPS validation.

---

## 7. Stage 4 — Reward Engine

### Objective

Issue milestone rewards exactly once.

### Backend scope

- Partner model if needed.
- Milestone config.
- Reward config.
- Reward evaluation after stamp collection.
- User reward creation.
- Voucher pool allocation if real voucher is in MVP.
- Reward query API.
- Notification creation if included.

### Mobile scope

- Reward list.
- Reward achievement display.
- Voucher reveal if included.
- Milestone progress UI.

### Admin scope

- Manage milestone.
- Manage reward.
- Import/manage voucher pool if included.
- View issued reward list.

### QA scope

- 3/7/14 milestone issuance or confirmed milestone set.
- Reward not duplicated under repeated event.
- Voucher allocated once.
- Voucher pool exhausted behavior.
- User cannot read another user's reward.

### Exit criteria

- Milestone reward is idempotent.
- Voucher allocation is atomic if included.
- Reward status behavior is deterministic.

---

## 8. Stage 5 — Mobile Integration

### Objective

Connect Flutter app to backend P0 APIs and validate real device behavior.

### Backend scope

- Stabilize API contract.
- Fix integration bugs.
- Add mobile-friendly error codes.
- Add logs for scan diagnostics.

### Mobile scope

- End-to-end login → station list → scan → collect → stamp book → reward.
- NFC read on Android devices.
- QR fallback.
- Offline/network timeout handling.
- Secure token storage.
- User-facing error states.

### QA scope

- Test on multiple Android devices.
- GPS permission denied.
- NFC unsupported device.
- QR fallback.
- Weak network/retry.
- App background/foreground during scan.

### Exit criteria

- Full P0 mobile loop works against staging backend.
- Error states are clear and deterministic.
- Device-specific NFC issues are documented.

---

## 9. Stage 6 — Monetization MVP Support

### Objective

Add minimal monetization instrumentation without corrupting core collection.

### Backend scope

- Advertisement creative config.
- Pre-stamp ad slot API if approved.
- Impression tracking.
- Click tracking if approved.
- Basic admin reporting.

### Mobile scope

- Display ad placement if approved.
- Track impression after actual display.
- Frequency cap UX if included.

### QA scope

- Expired ad not served.
- Impression cannot be spoofed trivially.
- Impression dedup works.
- Ad failure does not block stamp unless explicitly required.

### Exit criteria

- Monetization events are append-only and auditable.
- Core collection remains stable if ad service fails.

---

## 10. Stage 7 — Community / Sharing Foundation

### Objective

Add growth features after scan/reward foundation is stable.

### Backend scope

- Share event tracking.
- Referral code generation.
- Referral validation.
- Notification inbox.

### Mobile scope

- Share Stamp Book / station stamp.
- Referral code display.
- Enter referral code if included.
- Notification list.

### QA scope

- Self-referral blocked.
- Duplicate referred user blocked.
- Share event created only by authenticated user.
- Notification visibility scoped by user.

### Exit criteria

- Growth events do not affect collection/reward correctness.

---

## 11. Stage 8 — Production Hardening

### Objective

Prepare system for real users and physical deployment.

### Backend scope

- Rate limiting.
- Security headers / CORS review.
- Production profile hardening.
- Disable/protect Swagger in production.
- Monitoring/logging.
- Database backup.
- Migration verification.
- Load test scan endpoint.
- Deployment script / CI/CD.

### Mobile scope

- Crash reporting.
- Release build signing.
- Store/internal distribution setup.
- Device compatibility report.

### QA scope

- Smoke test.
- Regression suite.
- Load test.
- Security sanity test.
- Backup/restore drill if possible.

### Exit criteria

- Staging passes release candidate checklist.
- Production deployment plan approved.
- Rollback plan exists.
- Known risks accepted.

---

## 12. Data Integrity Gates by Stage

| Stage | Required Integrity Gate |
|---:|---|
| 1 | Token lifecycle and permission checks deterministic. |
| 2 | Station/scan-key uniqueness and public/private field separation. |
| 3 | Duplicate scan and idempotency proven under concurrency. |
| 4 | Reward/voucher uniqueness proven under retry/concurrency. |
| 5 | Mobile retry does not create data corruption. |
| 6 | Impression tracking does not affect stamp issuance integrity. |
| 7 | Referral/share cannot mutate collection/reward incorrectly. |
| 8 | Load/security/deploy checks pass before real users. |

---

## 13. Stage-Level Edge Cases

1. **Stage 2 proceeds without finalized GPS policy**  
   Station data will miss required radius/accuracy behavior and Stage 3 will rework schema/API.

2. **Stage 3 ships without idempotency**  
   Real mobile timeouts will create duplicate support cases and potentially duplicate rewards.

3. **Stage 4 uses async reward without idempotency**  
   Event retry will create duplicate rewards.

4. **Stage 5 tests only emulator**  
   NFC/GPS defects will be missed. Physical Android testing is mandatory.

5. **Stage 6 monetization blocks collection**  
   Ad SDK/network failure can break the core product unless explicitly isolated.

---

## 14. Stage Plan Acceptance Gate

Stage Plan is accepted only when:

- every stage has a measurable exit criterion;
- no later feature blocks P0 scan-to-stamp loop;
- every P0 data invariant is tested in the stage where it is introduced;
- mobile integration starts only after stable API contract for the relevant flow;
- production hardening is not treated as optional cleanup.
