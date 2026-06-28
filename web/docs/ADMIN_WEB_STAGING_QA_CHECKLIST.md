# Admin Web Staging QA Checklist

**Project:** Exotic Stamp Admin Web Dashboard (`/web`)
**Milestone:** 9.0 — Admin Web Staging QA Preparation
**Purpose:** Human-executable manual QA + UAT script for the staging environment.
**Verdict baseline:** MVP readiness = PASS WITH NON-BLOCKING GAPS (see `ADMIN_WEB_MVP_READINESS_REPORT.md`).

> How to use this document
> - Execute top to bottom in order; later sections assume earlier data exists (e.g. a metro line must exist before creating a station).
> - Mark each row: `[x]` pass, `[ ]` not run, or `[!]` fail (file a bug using `ADMIN_WEB_QA_BUG_TEMPLATE.md`).
> - Record the Bug ID next to any `[!]` row.
> - Do not test against production. Staging only.

---

## 1. Environment setup

- [ ] Confirm staging backend base URL is reachable (see `ADMIN_WEB_STAGING_READINESS_NOTES.md`).
- [ ] Confirm `VITE_API_BASE_URL` points to the staging backend.
- [ ] Confirm `VITE_APP_ENV=STAGING`.
- [ ] App loads at the staging web URL without console errors on first paint.
- [ ] Backend CORS allows the web origin (no CORS errors in DevTools Network/Console).
- [ ] HTTPS is used end-to-end (no mixed-content warnings).
- [ ] Browser matrix: latest Chrome (primary), plus one of Edge/Firefox/Safari.
- [ ] DevTools open (Console + Network) for the entire session to catch silent failures.

## 2. Test accounts needed

Coordinate with backend/devops to provision these before starting (details in `ADMIN_WEB_STAGING_READINESS_NOTES.md`).

- [ ] **Admin account** — full RBAC permissions (super admin / all admin scopes).
- [ ] **Restricted account** — limited role lacking one or more admin permissions (used for 403 checks).
- [ ] **Invalid credentials** — a known-bad email/password pair for negative login.
- [ ] Credentials stored securely (password manager / secrets vault), never pasted into the bug tracker.

## 3. Login / Logout / Session refresh

- [ ] Login with valid admin credentials redirects to the dashboard (`/`).
- [ ] Login honors the `from` return path (visit `/stations` while logged out → after login lands on `/stations`).
- [ ] Login with invalid credentials shows an inline error and does NOT navigate.
- [ ] Auth token is held in memory only — no access token in `localStorage`/`sessionStorage`.
- [ ] Browser refresh while authenticated restores the session via the refresh cookie (stays logged in).
- [ ] Let the access token expire (or simulate 401) → an in-flight request triggers a silent refresh and retries.
- [ ] If refresh fails, the app redirects to `/login` (no infinite loop, no blank screen).
- [ ] Logout clears the session and redirects to `/login`.
- [ ] After logout, visiting a protected route (e.g. `/campaigns`) redirects to `/login`.

## 4. Metro Lines CRUD (`/metro-lines`)

- [ ] List loads with pagination (0-based pages).
- [ ] Create a line with required fields (code, name); appears in the list.
- [ ] Edit a line; color hex validation enforces `#RRGGBB`.
- [ ] Invalid color hex is rejected with a field-level error.
- [ ] Delete a line shows a ConfirmDialog before deleting.
- [ ] Restricted account: list shows PermissionDeniedState on 403.

## 5. Stations CRUD (`/stations`)

- [ ] List loads; search works; filter by line and by status work.
- [ ] **Table does NOT show NFC tag ID or QR code value** (security).
- [ ] Create a station with line, code, name (and GPS if required).
- [ ] GPS latitude/longitude validate to valid ranges.
- [ ] Edit a station saves changes.
- [ ] Soft delete requires ConfirmDialog and redirects/refreshes list.
- [ ] Restricted account: 403 → PermissionDeniedState.

## 6. Station detail (`/stations/:id`)

- [ ] Detail page loads via in-app navigation.
- [ ] Detail page loads via **direct URL + browser refresh** (deep link works).
- [ ] Invalid/unknown station id shows a not-found (404) state with back navigation.
- [ ] Map preview renders (static placeholder is expected — documented limitation).

## 7. Scan key masking / update

- [ ] Scan key drawer opens from the station detail/list.
- [ ] NFC tag ID and QR code value are **masked by default**.
- [ ] Reveal toggle shows the plaintext value only on explicit click.
- [ ] Editing scan key fields shows plaintext while editing (expected for admin entry).
- [ ] Saving scan keys requires a ConfirmDialog.
- [ ] Save success refreshes the displayed (masked) values.
- [ ] Restricted account: 403 handled without crash.

## 8. Rotate QR

- [ ] Rotate QR action requires a ConfirmDialog.
- [ ] On confirm, success message is shown.
- [ ] The new QR value is NOT printed in plaintext in the list/table.
- [ ] Cancel on the ConfirmDialog performs no change.

## 9. Campaign CRUD (`/campaigns`)

- [ ] List/create/edit/delete campaigns.
- [ ] Date range validation: start date must be before end date.
- [ ] Delete shows ConfirmDialog.
- [ ] Campaign detail loads via direct URL refresh (deep link).
- [ ] Restricted account: 403 → PermissionDeniedState.

## 10. Campaign station assignment (`/campaigns/:id`)

- [ ] Assign a station to a campaign from the drawer/picker.
- [ ] Assigning an already-assigned station shows a 409 conflict message.
- [ ] Remove an assigned station requires confirmation.
- [ ] Station picker loads (note: limited to first 100 stations — documented limitation).

## 11. Stamp Designs + Upload (`/stamp-designs`)

- [ ] Grid view and table view both load.
- [ ] Create a design with campaign, station, name, image URL.
- [ ] Upload field: drag/drop OR file pick uploads a public asset successfully.
- [ ] Upload shows the "not saved until form submit" hint while the form is dirty.
- [ ] Edit and delete with confirmations.
- [ ] Invalid campaign/station selection rejected (UUID validation).

## 12. Partners (`/partners`)

- [ ] List loads with pagination.
- [ ] Create a partner (name required).
- [ ] Contract start/end date range validation.
- [ ] Activate / deactivate with confirmation.
- [ ] Partner detail drawer loads.
- [ ] Restricted account: 403 handled.

## 13. Milestones (`/milestones`)

- [ ] List loads with the campaign filter.
- [ ] Create with required fields (campaign, code, name, stamp count, reward title, type).
- [ ] Edit and delete a milestone.
- [ ] Milestone detail drawer handles 404/403 gracefully.

## 14. Rewards (`/rewards` → Rewards tab)

- [ ] List loads; create and edit rewards.
- [ ] Activate / deactivate a reward.
- [ ] Reward detail drawer loads.
- [ ] Bulk upload vouchers per reward (paste codes → upload).
- [ ] Bulk upload shows the pre-submit summary (total lines, valid codes, duplicates removed).
- [ ] Bulk upload result shows available/redeemed counts.

## 15. Voucher Pool (`/rewards` → Voucher Pool tab)

- [ ] List loads with milestone/status filters.
- [ ] **Codes are masked in the table.**
- [ ] Detail drawer: code masked until reveal click.
- [ ] Disable voucher requires a ConfirmDialog.

## 16. Import Vouchers (`/rewards` → Import tab)

- [ ] Select a milestone and paste codes (one per line).
- [ ] Pre-submit summary shows total lines, valid codes, and duplicates removed.
- [ ] Optional "Expires at" datetime applies when set.
- [ ] Import success shows the result counts (imported/duplicates).
- [ ] Duplicate codes reported by the API are reflected in the result.
- [ ] "Clear form" resets the form and result.

## 17. Dashboard (`/`)

- [ ] Metric cards load.
- [ ] If one underlying API fails, the dashboard degrades gracefully (no full-page crash).
- [ ] Collection overview chart/table renders.
- [ ] Top stations table renders.
- [ ] Operational warnings list shows (with **masked** voucher references).
- [ ] "Refresh all" re-fetches data.

## 18. Analytics (`/analytics`)

- [ ] Collection / Stations / Rewards tabs switch correctly.
- [ ] Each section has its own error state with a retry control.
- [ ] One failing section does NOT crash the other tabs.
- [ ] Page-level 403 shows PermissionDeniedState.
- [ ] Voucher status distribution copy notes it is a page sample (documented limitation).

## 19. RBAC (`/rbac`)

- [ ] Roles tab: create, edit (non-system), list.
- [ ] System roles cannot be edited/deleted in the UI.
- [ ] Permissions tab: create and list.
- [ ] Role Permissions tab: assign/revoke; sensitive permissions require confirmation.
- [ ] User Roles tab: load by UUID, assign/revoke.
- [ ] Self-lockout warning appears when modifying your own roles.
- [ ] Permission Matrix tab: read-only view renders.
- [ ] 403 on roles list shows PermissionDeniedState.

## 20. HTTP status handling (401 / 403 / 404 / 409 / 422 / 500)

- [ ] **401** — expired/invalid token triggers refresh, then login redirect if refresh fails.
- [ ] **403** — restricted account on list pages → PermissionDeniedState; detail fetches handled at drawer level.
- [ ] **404** — detail pages/drawers show ErrorState with back navigation.
- [ ] **409** — duplicate campaign station assignment shows a conflict message.
- [ ] **422 / 400** — form validation errors render inline on fields or in an alert.
- [ ] **500** — generic fallback message; section-level error UI; no full app crash.
- [ ] **Network error** (backend down / offline) → user-friendly message, no crash.

## 21. Sensitive data masking checks

- [ ] Station list: no scan keys (NFC tag ID / QR value) visible anywhere.
- [ ] Scan key drawer: masked by default, reveal on click only.
- [ ] Voucher table: codes masked.
- [ ] Voucher detail: code revealed on click only.
- [ ] Dashboard operational warnings: voucher references masked.
- [ ] DevTools Console: no auth token logged.
- [ ] DevTools Console/Network: no full voucher codes or scan keys logged in app code (backend response bodies in Network are expected; app must not re-log them).

## 22. Browser refresh / deep-link checks

For each, paste the URL into a fresh tab (or hard-refresh) **while authenticated**:

- [ ] `/` (dashboard)
- [ ] `/metro-lines`
- [ ] `/stations`
- [ ] `/stations/:id` (a real id)
- [ ] `/campaigns`
- [ ] `/campaigns/:id` (a real id)
- [ ] `/stamp-designs`
- [ ] `/partners`
- [ ] `/milestones`
- [ ] `/rewards`
- [ ] `/analytics`
- [ ] `/rbac`
- [ ] `/settings`
- [ ] Each route renders correctly after refresh (no blank screen, no 404 from the static host — SPA fallback works).
- [ ] Deep-linking any protected route while logged out redirects to `/login` and returns there after login.

## 23. Final sign-off checklist

- [ ] All Blocker and Critical bugs are resolved or have an accepted workaround.
- [ ] No sensitive data (scan keys, full voucher codes, tokens) is exposed in UI or app logs.
- [ ] All deep-link/refresh routes work.
- [ ] Auth lifecycle (login/refresh/logout/expiry) verified.
- [ ] Known non-blocking gaps acknowledged by the team (see readiness notes).
- [ ] Bug log reviewed; each bug has a status and owner.
- [ ] QA sign-off recorded below.

| Field | Value |
|-------|-------|
| Tester name | |
| Date | |
| Build / commit | |
| Backend version | |
| Environment | STAGING |
| Result | PASS / PASS-WITH-ISSUES / FAIL |
| Open Blocker/Critical count | |
| Notes | |
