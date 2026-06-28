# Admin Web Recommended E2E Test Plan

**Project:** Exotic Stamp Admin Web Dashboard (`/web`)
**Milestone:** 9.0 — Admin Web Staging QA Preparation
**Status:** Recommendation only. No E2E framework is added in this milestone.

---

## Why no framework was added now

Per Milestone 9.0 scope, no new large dependencies are introduced. The project currently has **no test framework** installed (no Vitest, Jest, Playwright, or Cypress — see `web/package.json`). Adding Playwright/Cypress is intentionally deferred so staging QA can begin with the manual checklist first. This document captures the recommended automated coverage to add later.

## Recommended tooling

- **Unit/component:** Vitest + React Testing Library (lightweight, integrates with the existing Vite setup).
- **End-to-end:** Playwright (preferred) for cross-browser, auth, and deep-link coverage. Cypress is an acceptable alternative.
- Keep E2E in a separate workspace task (`npm run test:e2e`) so it does not affect `build`/`lint`.

## Priority 1 — Smoke tests (add first)

These map directly to the highest-value manual checks and are cheap to automate.

1. **Route rendering (authenticated):** For each route, the page mounts without console errors and renders its primary heading/landmark:
   - `/`, `/metro-lines`, `/stations`, `/stations/:id`, `/campaigns`, `/campaigns/:id`, `/stamp-designs`, `/partners`, `/milestones`, `/rewards`, `/analytics`, `/rbac`, `/settings`.
2. **Auth redirect:** Visiting any protected route while unauthenticated redirects to `/login` and preserves the return path; after login the user lands on the originally requested route.
3. **Login happy path:** Valid credentials → dashboard. Invalid credentials → inline error, no navigation.
4. **Logout:** Clears session and redirects to `/login`; protected routes redirect afterward.
5. **Session refresh:** A 401 on a data request triggers a silent refresh and retry; a failed refresh redirects to `/login`.
6. **Deep-link / SPA fallback:** Hard-navigating directly to `/stations/:id` and `/campaigns/:id` renders the page (no host 404).

## Priority 2 — Critical CRUD happy paths

One create→edit→delete cycle per core module, asserting list refresh and confirmation dialogs:

- Metro Lines, Stations, Campaigns, Stamp Designs, Partners, Milestones, Rewards.
- Campaign station assignment incl. the 409 duplicate-assignment path.

## Priority 3 — Security / data-masking assertions

Automate the sensitive-data rules from the readiness report:

- Station list never renders NFC tag ID or QR code value.
- Scan key drawer masks values by default; reveal only on click.
- Voucher table masks codes; detail reveals only on click.
- Dashboard operational warnings show masked voucher references.
- No auth token is written to `localStorage`/`sessionStorage` or logged to the console.

## Priority 4 — Error-state coverage

Drive mocked/stubbed API responses to assert UI behavior for: 401, 403, 404, 409, 422/400, 500, and network failure (per QA checklist section 20).

## Suggested structure (when implemented)

```
web/
  e2e/
    fixtures/        # test users, seeded data helpers
    auth.spec.ts     # login/logout/refresh/redirect (Priority 1)
    routes.spec.ts   # route rendering smoke (Priority 1)
    crud/*.spec.ts   # per-module CRUD (Priority 2)
    security.spec.ts # masking assertions (Priority 3)
    errors.spec.ts   # status-code handling (Priority 4)
  playwright.config.ts
```

## Environment for E2E

- Run against a dedicated, disposable staging/test backend (never production).
- Use a seeded admin account and a restricted account (see `ADMIN_WEB_STAGING_READINESS_NOTES.md`).
- Provide `VITE_API_BASE_URL` pointing at the test backend; ensure CORS allows the test origin.
- Prefer test-data isolation (unique prefixes/cleanup) so CRUD specs are repeatable.

## Acceptance for "smoke tests done"

- Priority 1 specs pass headless in CI on Chromium.
- E2E job is separate from `build`/`lint` and does not block them.
- A `README` note documents how to run `npm run test:e2e` locally and in CI.
