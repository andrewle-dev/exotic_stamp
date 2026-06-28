# Admin Web Staging Readiness Notes

**Project:** Exotic Stamp Admin Web Dashboard (`/web`)
**Milestone:** 9.0 — Admin Web Staging QA Preparation
**Audience:** DevOps / backend / QA setting up the staging environment before manual QA + UAT.
**Companion docs:** `ADMIN_WEB_STAGING_QA_CHECKLIST.md`, `ADMIN_WEB_QA_BUG_TEMPLATE.md`, `ADMIN_WEB_MVP_READINESS_REPORT.md`.

---

## 1. Required backend URL

- The web app talks to the backend via `VITE_API_BASE_URL`.
- All admin endpoints are under `/api/v1/...` (e.g. `/api/v1/admin/metro/stations`).
- For staging, set `VITE_API_BASE_URL` to the staging backend origin (HTTPS), for example:
  - `VITE_API_BASE_URL=https://staging-api.exoticstamp.example`
- The value must be the scheme + host (+ optional port) **without** a trailing `/api/v1` and without a trailing slash.
- Default fallback if unset is `http://localhost:8080` (local only — do not rely on this in staging).

## 2. Required environment variables

Build-time Vite env vars (must be present when the static bundle is built, since Vite inlines them):

| Variable | Required | Example | Notes |
|----------|----------|---------|-------|
| `VITE_API_BASE_URL` | Yes | `https://staging-api.exoticstamp.example` | Backend origin; no trailing slash. |
| `VITE_APP_ENV` | Recommended | `STAGING` | Environment label. |

- A starter file exists at `web/.env.example`. Copy it to `web/.env` (or set vars in the CI/build environment) and adjust for staging.
- Because these are inlined at build time, **changing them requires a rebuild** (`npm run build`), not just a restart.

## 3. CORS requirement for frontend origin

- The web app is served from its own origin (e.g. `https://staging-admin.exoticstamp.example`) and calls the backend cross-origin.
- The backend **must** allow the web origin via CORS:
  - `Access-Control-Allow-Origin`: the exact web origin (not `*`, because credentials are used).
  - `Access-Control-Allow-Credentials: true`.
  - Allowed methods: `GET, POST, PATCH, PUT, DELETE, OPTIONS`.
  - Allowed headers: `Authorization, Content-Type, Accept`.
- The client sends requests with `withCredentials: true` (the refresh flow relies on the refresh cookie). The auth refresh cookie must be set with `SameSite=None; Secure` for cross-site staging, or the origins must be same-site.
- Verify in DevTools: no CORS errors and the refresh request (`POST /api/v1/auth/refresh`) succeeds with the cookie attached.

## 4. Required seed / admin account

- A working admin login is required before any QA can begin.
- Provision (or seed) at least one **admin account** with full admin permissions.
- Auth endpoints used: `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `POST /api/v1/auth/refresh`, and `GET /api/v1/users/me` for the profile.
- Note: there is **no admin user-management UI**. The User Roles tab requires entering a user UUID manually, so have user UUIDs on hand for RBAC testing.

## 5. Required RBAC permissions for test accounts

Provide two accounts so QA can verify both happy-path and 403 handling:

- **Admin (full)** — permissions across all admin domains: metro lines, stations, scan keys, campaigns, stamp designs, partners, milestones, rewards, vouchers, analytics/collections, and RBAC (`/api/v1/roles`, `/api/v1/permissions`).
- **Restricted** — a role intentionally missing one or more admin permissions, used to confirm the UI shows `PermissionDeniedState` / drawer-level 403 handling rather than crashing.

Notes:
- The frontend only knows **role names** from the auth response, not fine-grained permission codes. UI action visibility is coarse; the backend 403 is authoritative. QA should treat backend 403 as the source of truth for permission enforcement.
- For RBAC tab tests, ensure at least one **system role** exists (to confirm it cannot be edited/deleted in the UI) and one non-system role.

## 6. Known non-blocking gaps (from MVP readiness report)

These are accepted limitations for the MVP and should NOT be filed as Blocker/Critical bugs:

- **Settings page** is a read-only profile placeholder (no admin preference/profile update API yet).
- **Station assignment picker** loads only the first ~100 stations (page 0); no server-side search in the picker.
- **Operational warnings / analytics voucher distribution** are based on a page sample, not the full pool (noted in UI copy).
- **Role-only guards** — no per-permission UI gating by design (auth exposes roles only); backend 403 enforces.
- **Bundle size** — single large JS chunk (>500 kB); no route-based code splitting yet. Vite prints a chunk-size warning at build (non-blocking).
- **Map preview** on station detail is a static placeholder, not an interactive map.
- **No admin user list/search UI** — User Roles tab requires manual UUID entry.
- **No voucher bulk export** endpoint/UI.

See `ADMIN_WEB_MVP_READINESS_REPORT.md` sections 8–9 for the full list.

## 7. Smoke test command sequence

Run from the `web/` directory. This verifies the build/lint health and produces a deployable bundle.

```bash
# 1. Install dependencies (clean, reproducible)
npm ci

# 2. Configure staging env (one-time)
cp .env.example .env
# then edit .env: set VITE_API_BASE_URL to the staging backend, VITE_APP_ENV=STAGING

# 3. Type-check + production build (must exit 0)
npm run build

# 4. Lint (must exit 0, zero errors, zero warnings)
npm run lint

# 5. Serve the production bundle locally to smoke test
npm run preview
# open the printed URL, then in the browser:
#   - app loads with no console errors
#   - login with the seed admin account succeeds
#   - dashboard renders
#   - hard-refresh on /stations and /campaigns/:id (deep-link/SPA fallback works)
#   - logout redirects to /login
```

Expected results:
- `npm run build` → exit 0 (tsc + vite build succeed; only the non-blocking chunk-size warning may appear).
- `npm run lint` → exit 0 with **0 errors and 0 warnings** (the two prior `react-hook-form watch` warnings were fixed in Milestone 9.0).

## 8. Rollback / checkpoint notes

- **Pre-deploy checkpoint:** tag or note the current git commit before deploying to staging so it can be reproduced/reverted.
  - `git rev-parse HEAD` (record the SHA in the QA sign-off).
- **Frontend rollback:** the web app is a static bundle (`web/dist`). To roll back, redeploy the previously known-good bundle/commit and confirm `npm run build` succeeds on it.
- **No data migrations** are introduced by the frontend; rollback is purely redeploying the previous static build. No backend or DB changes are part of this milestone.
- **Env changes are build-time:** if a rollback also changes `VITE_API_BASE_URL`/`VITE_APP_ENV`, rebuild the bundle for the target environment.
- **Milestone 9.0 change surface (low risk):**
  - Removed dead component `features/scan-keys/components/ScanKeyPanel.tsx` (no imports referenced it).
  - Replaced `watch()` with `useWatch()` in `BulkUploadVouchersDrawer.tsx` and `ImportVouchersTab.tsx` (behavior-equivalent; clears lint warnings).
  - Added QA docs under `web/docs/`.
  - No routes, APIs, backend, or mobile code changed.
- **Verification after rollback or redeploy:** re-run the Section 7 smoke sequence and the auth + deep-link checks from the QA checklist.
