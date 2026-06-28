# Admin Web MVP Readiness Report

**Project:** Exotic Stamp Admin Web Dashboard (`/web`)  
**Milestone:** 8.0 — QA Hardening + MVP Readiness Review  
**Date:** 2026-06-27  
**OpenAPI source of truth:** `backend/docs/api/openapi.json`  
**Auditor:** Automated + manual code review pass

---

## 1. Executive Summary

The Admin Web Dashboard has completed all planned feature milestones (1.0–7.0) and is structurally sound for MVP launch. Architecture follows a consistent feature-module pattern with centralized API services, React Query data fetching, shared UI primitives, and route-aligned navigation.

A full QA hardening pass was performed across architecture, API contracts, sensitive data handling, error states, form validation, RBAC UX, and routing. **Seven localized fixes** were applied (voucher masking in operational warnings, optional query-param hygiene, UUID validation on select form fields). No backend or mobile changes were made.

**Build:** `npm run build` — **PASS**  
**Lint:** `npm run lint` — **PASS** (2 pre-existing React Compiler warnings on `react-hook-form` `watch()`)

### MVP Readiness Verdict: **PASS WITH NON-BLOCKING GAPS**

The admin console is ready for staged MVP use. Remaining gaps are documented limitations (settings placeholder, station picker pagination cap, role-only frontend guards, dead placeholder component) that do not block core admin workflows.

---

## 2. Implemented Modules

| Module | Routes | Status |
|--------|--------|--------|
| Auth (login/logout/session) | `/login` | Complete |
| Dashboard | `/` | Complete |
| Metro Lines | `/metro-lines` | Complete |
| Stations + Detail | `/stations`, `/stations/:id` | Complete |
| Scan Keys + Rotate QR | Drawers/dialogs on station pages | Complete |
| Campaigns + Detail | `/campaigns`, `/campaigns/:id` | Complete |
| Station Assignment | Campaign detail | Complete |
| Stamp Designs | `/stamp-designs` | Complete |
| Public Asset Upload | Form field component | Complete |
| Partners | `/partners` | Complete |
| Reward Milestones | `/milestones` | Complete |
| Rewards | `/rewards` (Rewards tab) | Complete |
| Voucher Pool | `/rewards` (Voucher Pool tab) | Complete |
| Import Vouchers | `/rewards` (Import tab) | Complete |
| Analytics | `/analytics` | Complete |
| RBAC | `/rbac` | Complete |
| Settings | `/settings` | Placeholder (profile display only) |

---

## 3. API Coverage

Comparison of implemented `lib/api/*.api.ts` + `features/auth/api.ts` services against OpenAPI admin-related endpoints.

### Auth

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/auth/login` | POST | `features/auth/api.ts` | Yes |
| `/api/v1/auth/logout` | POST | `features/auth/api.ts` | Yes |
| `/api/v1/auth/refresh` | POST | `lib/api/client.ts`, `features/auth/api.ts` | Yes |

### Users / Me

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/users/me` | GET | `features/auth/api.ts` | Yes (direct `UserResponse`, not wrapped — matches OpenAPI) |

### Metro Lines

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/metro/lines` | GET, POST | `metro-lines.api.ts` | Yes |
| `/api/v1/admin/metro/lines/{id}` | GET, PATCH, DELETE | `metro-lines.api.ts` | Yes |

Pagination: 0-based `page` default 0, `size` default 20. PageResponse unwrapped via `unwrapApiResponse`.

### Stations

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/metro/stations` | GET, POST | `stations.api.ts` | Yes |
| `/api/v1/admin/metro/stations/{id}` | GET, PATCH, DELETE | `stations.api.ts` | Yes |
| `/api/v1/admin/metro/stations/{id}/scan-keys` | PATCH | `stations.api.ts` | Yes |
| `/api/v1/admin/metro/stations/{id}/rotate-qr` | POST | `stations.api.ts` | Yes |
| `/api/v1/admin/metro/stations/stats` | GET | `stations.api.ts`, `analytics.api.ts` | Yes |

Query params: `lineId`, `status`, `search`, `page`, `size`, `sort` — only sent when provided (no invented params).

### Campaigns

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/campaigns` | GET, POST | `campaigns.api.ts` | Yes |
| `/api/v1/admin/campaigns/{id}` | GET, PATCH, DELETE | `campaigns.api.ts` | Yes |
| `/api/v1/admin/campaigns/{id}/stations` | GET, POST | `campaigns.api.ts` | Yes |
| `/api/v1/admin/campaigns/{id}/stations/{stationId}` | DELETE | `campaigns.api.ts` | Yes |

### Stamp Designs

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/stamp-designs` | GET, POST | `stamp-designs.api.ts` | Yes |
| `/api/v1/admin/stamp-designs/{id}` | GET, PATCH, DELETE | `stamp-designs.api.ts` | Yes |

### Uploads

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/uploads/public` | POST (multipart) | `uploads.api.ts` | Yes |

### Partners

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/partners` | GET, POST | `partners.api.ts` | Yes |
| `/api/v1/admin/partners/{id}` | GET, PUT | `partners.api.ts` | Yes (PUT per OpenAPI) |
| `/api/v1/admin/partners/{id}/activate` | PATCH | `partners.api.ts` | Yes |
| `/api/v1/admin/partners/{id}/deactivate` | PATCH | `partners.api.ts` | Yes |

### Milestones

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/rewards/milestones` | GET, POST | `milestones.api.ts` | Yes |
| `/api/v1/admin/rewards/milestones/{id}` | GET, PATCH, DELETE | `milestones.api.ts` | Yes |

### Rewards

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/rewards` | GET, POST | `rewards.api.ts` | Yes |
| `/api/v1/admin/rewards/{id}` | GET, PUT | `rewards.api.ts` | Yes (PUT per OpenAPI) |
| `/api/v1/admin/rewards/{id}/activate` | PATCH | `rewards.api.ts` | Yes |
| `/api/v1/admin/rewards/{id}/deactivate` | PATCH | `rewards.api.ts` | Yes |
| `/api/v1/admin/rewards/{id}/vouchers/stats` | GET | `rewards.api.ts` | Yes |
| `/api/v1/admin/rewards/{id}/vouchers/bulk-upload` | POST | `rewards.api.ts` | Yes |

### Voucher Pool

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/rewards/vouchers` | GET | `vouchers.api.ts` | Yes |
| `/api/v1/admin/rewards/vouchers/{id}` | GET, PATCH | `vouchers.api.ts` | Yes (PATCH = disable) |
| `/api/v1/admin/rewards/vouchers/import` | POST | `vouchers.api.ts` | Yes |

### Analytics / Collections

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/admin/collections/stats` | GET | `analytics.api.ts` | Yes |
| `/api/v1/admin/metro/stations/stats` | GET | `analytics.api.ts`, `stations.api.ts` | Yes |

### RBAC

| Endpoint | Method | Frontend | Match |
|----------|--------|----------|-------|
| `/api/v1/roles` | GET, POST | `rbac.api.ts` | Yes |
| `/api/v1/roles/{roleId}` | GET, PATCH | `rbac.api.ts` | Yes |
| `/api/v1/roles/{roleId}/permissions` | GET, POST | `rbac.api.ts` | Yes |
| `/api/v1/roles/{roleId}/permissions/{permissionId}` | DELETE | `rbac.api.ts` | Yes |
| `/api/v1/roles/assign` | POST | `rbac.api.ts` | Yes |
| `/api/v1/roles/revoke` | POST | `rbac.api.ts` | Yes |
| `/api/v1/roles/{userId}/roles` | GET | `rbac.api.ts` | Yes |
| `/api/v1/permissions` | GET, POST | `rbac.api.ts` | Yes |

### Not implemented (intentionally — no UI requirement)

- Admin user CRUD (`/api/v1/users`, `/api/v1/users/{id}`)
- Public/mobile collection, notification, community APIs
- `incrementCollectorCount` internal station endpoint

---

## Part A — Architecture Audit

### Architecture status

**Healthy.** The codebase follows a clear layered structure:

```
web/src/
├── app/           # Router, providers, App entry
├── components/    # Shared layout, UI primitives, feedback
├── features/      # Domain modules (pages, hooks, schemas, components)
├── lib/           # API client, auth, query keys, formatting, utils
└── types/         # Shared TypeScript API types
```

- **Feature module isolation:** Each domain (`stations`, `campaigns`, `rbac`, etc.) owns pages, hooks, schemas, and feature-specific components.
- **Shared component reuse:** `DataTable`, `FormDrawer`, `ConfirmDialog`, `StatusBadge`, `ApiErrorAlert`, `SecretField`, etc. are used consistently.
- **API service boundaries:** All HTTP calls go through `lib/api/*.api.ts` (auth exception: `features/auth/api.ts`).
- **No duplicate API logic in components:** Components use hooks that call API modules.
- **TypeScript:** Strong typing via `types/` and Zod-inferred form types. No `any` usage (only HTML `step="any"` on number inputs).
- **Query keys:** Consistent factory pattern in `lib/query/keys/` per domain.
- **Query invalidation:** Mutations invalidate appropriate list/detail/stats keys across all modules.
- **Routes:** `lib/constants/routes.ts` + `app/router.tsx` align with `AppSidebar` `NAV_ITEMS`.

### Issues found

| ID | Severity | Issue |
|----|----------|-------|
| A1 | Low | Dead placeholder `features/scan-keys/components/ScanKeyPanel.tsx` (superseded by `ScanKeyDrawer`) |
| A2 | Low | Auth API lives in `features/auth/api.ts` while other domains use `lib/api/` |
| A3 | Low | `features/uploads/api.ts` is a thin re-export only |
| A4 | Info | Production bundle >500 kB (Vite chunk size warning) |

### Fixes applied

None required for architecture (structural issues are non-blocking).

### Remaining non-blocking gaps

- Remove or repurpose unused `ScanKeyPanel` in a future cleanup pass.
- Consider code-splitting route modules to reduce initial bundle size.

---

## 4. Security / Sensitive Data Review

### Rules checked

| Rule | Status |
|------|--------|
| Station table must not show `nfcTagId` or `qrCodeValue` | **PASS** — columns show code, name, line, address, status, GPS, collectors only |
| Scan key drawer masks values by default | **PASS** — `SecretField` with reveal toggle |
| Voucher table masks code by default | **PASS** — `MaskedValue` + `maskVoucherCode` |
| Voucher detail reveals only on explicit click | **PASS** — `SecretField` in `VoucherDetailDrawer` |
| No `console.log` of sensitive payloads | **PASS** — none found in `web/src` |
| Auth token not printed | **PASS** — stored in `tokenStore` (memory), sent via interceptor only |
| Dangerous operations use `ConfirmDialog` | **PASS** — delete, rotate QR, scan key save, RBAC sensitive permissions, voucher disable, etc. |

### Issue found and fixed

| ID | Issue | Fix |
|----|-------|-----|
| S1 | Dashboard operational warnings exposed full `voucher.code` in warning messages | **Fixed** — now uses `maskVoucherCode()` in `operational-warnings.ts` |

### Scan key form note

Edit fields in `ScanKeyDrawer` show plaintext while editing (expected for admin entry). Display/read-only sections use masking.

---

## 5. Data Integrity Review

- **Pagination:** All list endpoints use 0-based `page` with defaults; UI pagination components respect API `totalPages` / `totalElements`.
- **PageResponse:** Unwrapped consistently via `unwrapApiResponse` → `.content`, `.page`, `.size`, `.totalElements`.
- **PUT vs PATCH:** Rewards and partners use PUT for full updates; stations, campaigns, milestones, stamp designs, scan keys use PATCH — matches OpenAPI.
- **Mutation invalidation:** List + detail (+ stats where relevant) invalidated after create/update/delete across modules.
- **No mock business data** on production pages — all data from live API queries.
- **Import deduplication:** Voucher import/bulk upload deduplicates client-side before POST.

---

## 6. Error Handling Review

Central error parsing in `lib/api/errors.ts` (`ApiError` class) with helpers: `isForbiddenError`, `isNotFoundError`, `isConflictError`, `isValidationError`, etc.

| Status | Handling |
|--------|----------|
| 400 | `ApiError` with backend message; shown in `ApiErrorAlert` / `FormDrawer` |
| 401 | Interceptor attempts refresh; falls back to login redirect via `RequireAuth` |
| 403 | `PermissionDeniedState` on list pages; `AnalyticsSection` inline; drawer-level for detail fetches |
| 404 | `ErrorState` on detail pages/drawers with back navigation |
| 409 | Explicit conflict message display (e.g. `AddStationDrawer` duplicate assignment) |
| 422 | Parsed as validation error via `isValidationError` (400/422) |
| 500 | Generic fallback message; section-level error UI without crash |

**Dashboard/Analytics resilience:** `useOperationalOverviewData` uses independent queries; `MetricCard` and `AnalyticsSection` degrade gracefully when individual queries fail — one failing analytics query does not crash the whole dashboard.

### Issues found

None requiring code changes. Error handling is consistent across audited modules.

---

## 7. RBAC Review

| Check | Status |
|-------|--------|
| `/rbac` page with tabs (Roles, Permissions, Role Permissions, User Roles, Matrix) | Complete |
| Role-based guard helpers (`lib/auth/permissions.ts`, `useRoleGuards.ts`) | Role names only — documented |
| Self-lockout warning (`SELF_LOCKOUT_WARNING` in User Roles tab) | Present |
| System role safety (cannot edit/delete system roles in UI) | Implemented in Roles tab |
| Sensitive permission confirmation | `RolePermissionsTab` uses `isSensitivePermission` + `ConfirmDialog` |
| Backend 403 authoritative | UI copy and helpers explicitly state backend 403 is authoritative |

**Important:** Frontend only knows role names from `AuthResponse.userInfo.roles`, not fine-grained permissions. Action visibility is not permission-code-aware beyond coarse role helpers. This is correct per auth contract.

---

## 8. Known Backend / API Limitations

- No admin settings/profile update API beyond `/users/me` GET (settings page is read-only profile display).
- No admin user list/search API wired in UI (User Roles tab requires manual UUID entry).
- Station picker in campaign assignment loads first page only (max 100 stations) — no server-side search in picker beyond client filter.
- Voucher pool has no bulk export endpoint.
- `incrementCollectorCount` is internal — not exposed in admin UI (correct).
- Reward `redeem` endpoint is deprecated (410) — not used in admin UI (correct).

---

## 9. Known Frontend Limitations

- **Settings page:** Placeholder with current profile display only.
- **Station assignment picker:** Limited to first 100 stations from list API page 0.
- **Operational warnings:** Voucher warnings based on first 100 vouchers sample, not full pool.
- **Analytics voucher status distribution:** Page sample only (documented in UI copy).
- **Dead code:** `ScanKeyPanel.tsx` placeholder unused.
- **Role-only guards:** No per-permission UI gating (by design — auth exposes roles only).
- **Bundle size:** Single large JS chunk; no route-based code splitting yet.
- **Map preview on station detail:** Static placeholder, not an interactive map.

---

## 10. Manual QA Checklist

Use this checklist against a running backend (`VITE_API_BASE_URL`) with an admin account.

### Login / Logout / Session

- [ ] Login with valid credentials redirects to dashboard (or prior `from` path)
- [ ] Login with invalid credentials shows inline error
- [ ] Refresh page while authenticated restores session via refresh cookie
- [ ] Logout clears session and redirects to login
- [ ] Accessing `/stations` while logged out redirects to `/login` with return path
- [ ] Session expiry (401) triggers refresh or login redirect

### Metro Lines

- [ ] List loads with pagination
- [ ] Create line with required fields (code, name)
- [ ] Edit line — color hex validation (#RRGGBB)
- [ ] Delete line shows ConfirmDialog
- [ ] 403 shows PermissionDeniedState for restricted user

### Stations + Scan Keys + Rotate QR

- [ ] List loads; search/filter by line and status work
- [ ] Table does **not** show NFC tag ID or QR code value
- [ ] Create station with line, code, name
- [ ] Station detail page loads after direct URL refresh
- [ ] Edit station — GPS fields validate range
- [ ] Scan key drawer: values masked by default; reveal works
- [ ] Scan key save requires ConfirmDialog
- [ ] Rotate QR requires ConfirmDialog; success message shown
- [ ] Soft delete requires ConfirmDialog; redirects to list
- [ ] 404 station shows not-found state

### Campaigns + Station Assignment

- [ ] List/create/edit/delete campaigns
- [ ] Date range validation (start before end)
- [ ] Campaign detail loads after refresh
- [ ] Assign station from drawer
- [ ] Duplicate assignment shows 409 conflict message
- [ ] Remove assigned station with confirmation

### Stamp Designs + Upload

- [ ] Grid/table views load
- [ ] Create design with campaign, station, name, image URL
- [ ] Upload field: drag/drop or file pick uploads asset
- [ ] Upload shows "not saved until form submit" hint when form dirty
- [ ] Edit/delete with confirmations

### Partners

- [ ] List with pagination
- [ ] Create partner (name required)
- [ ] Contract date range validation
- [ ] Activate/deactivate with confirmation
- [ ] Detail drawer loads

### Milestones

- [ ] List with campaign filter
- [ ] Create with required fields (campaign, code, name, stamp count, reward title, type)
- [ ] Edit/delete milestone
- [ ] Detail drawer 404/403 handling

### Rewards

- [ ] List/create/edit rewards
- [ ] Activate/deactivate reward
- [ ] Reward detail drawer
- [ ] Bulk upload vouchers per reward

### Voucher Pool

- [ ] List with milestone/status filters
- [ ] Codes masked in table
- [ ] Detail drawer: code masked until reveal click
- [ ] Disable voucher requires ConfirmDialog

### Import Vouchers

- [ ] Paste codes (one per line)
- [ ] Client-side dedup summary shown
- [ ] Import success shows result counts
- [ ] Duplicate codes from API reflected in result

### Dashboard

- [ ] Metric cards load (degrade gracefully if one API fails)
- [ ] Collection overview chart/table
- [ ] Top stations table
- [ ] Operational warnings list (voucher codes masked in messages)
- [ ] Refresh all button works

### Analytics

- [ ] Collection / Stations / Rewards tabs switch correctly
- [ ] Per-section error states with retry
- [ ] Page-level 403 shows PermissionDeniedState
- [ ] One failing section does not crash other tabs

### RBAC

- [ ] Roles tab: create, edit (non-system), list
- [ ] Permissions tab: create, list
- [ ] Role permissions: assign/revoke with sensitive permission confirmation
- [ ] User roles: load by UUID, assign/revoke with self-lockout warning
- [ ] Permission matrix read-only view
- [ ] 403 on roles list shows PermissionDeniedState

### Error States

- [ ] 403 on list pages → PermissionDeniedState
- [ ] 404 on detail → ErrorState with navigation
- [ ] 409 on campaign station assign → visible message
- [ ] 422/400 on forms → inline field or alert errors
- [ ] Network error → user-friendly message

### Sensitive Data Masking

- [ ] Station list: no scan keys visible
- [ ] Scan key drawer: masked by default
- [ ] Voucher table: masked codes
- [ ] Voucher detail: reveal on click only
- [ ] Dashboard warnings: masked voucher references
- [ ] Browser devtools/network: no token logged to console

---

## 11. MVP Readiness Verdict

### **PASS WITH NON-BLOCKING GAPS**

**Rationale:**
- All planned admin modules are implemented and wired to documented OpenAPI endpoints.
- Sensitive data handling meets security requirements after hardening fix.
- Error handling is consistent; partial API failures do not crash dashboard/analytics.
- Build and lint pass.
- Remaining gaps (settings placeholder, station picker cap, dead ScanKeyPanel, bundle size) do not block core MVP admin operations.

---

## 12. Recommended Next Steps

1. **Manual QA execution** — Run the checklist above against staging backend with admin and restricted-role test accounts.
2. **Remove dead code** — Delete or archive `ScanKeyPanel.tsx` placeholder.
3. **Route code splitting** — Lazy-load feature pages to reduce initial bundle.
4. **Settings module** — Implement when backend exposes admin preferences or profile update beyond `/users/me` PUT.
5. **User admin UI** — Add user search/list when backend admin user APIs are prioritized.
6. **Station picker** — Add server-side search or paginated picker for large station inventories.
7. **E2E tests** — Add Playwright/Cypress smoke tests for login, CRUD happy paths, and sensitive data masking assertions.

---

## Appendix: Files Reviewed

**Scope:** ~168 TypeScript/TSX files under `web/src/`, plus `backend/docs/api/openapi.json`, `web/package.json`, router, and query key modules.

Key areas reviewed:
- `web/src/app/` (router, providers)
- `web/src/lib/api/` (all 15 API modules + client + errors + response + pagination)
- `web/src/lib/query/keys/` (all 10 key factories)
- `web/src/lib/auth/`, `web/src/lib/formatting/`, `web/src/lib/metro/`
- `web/src/components/` (layout, UI, feedback, navigation)
- `web/src/features/**` (all 86 feature files)
- `web/src/types/` (API domain types)

## Appendix: Files Changed (Milestone 8.0 Fixes)

| File | Change |
|------|--------|
| `web/src/features/analytics/utils/operational-warnings.ts` | Mask voucher codes in warning messages |
| `web/src/lib/api/rewards.api.ts` | Omit `activeOnly` param when undefined |
| `web/src/lib/api/partners.api.ts` | Omit `activeOnly` param when undefined |
| `web/src/features/milestones/schemas.ts` | UUID validation on `campaignId` |
| `web/src/features/rewards/schemas.ts` | UUID validation on `milestoneId` |
| `web/src/features/stamp-designs/schemas.ts` | UUID validation on `campaignId`, `stationId` |
| `web/src/features/vouchers/schemas.ts` | UUID validation on `milestoneId` |
| `web/docs/ADMIN_WEB_MVP_READINESS_REPORT.md` | This report (new) |

## Appendix: Build / Lint Results

```
npm run build  → EXIT 0 (tsc + vite build succeeded)
npm run lint   → EXIT 0 (0 errors, 2 warnings)

Pre-existing warnings:
- BulkUploadVouchersDrawer.tsx: react-hooks/incompatible-library (watch)
- ImportVouchersTab.tsx: react-hooks/incompatible-library (watch)
```
