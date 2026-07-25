# Web Admin Current State

## Stack

- Framework: React `19.2.7`
- Build tool: Vite `8.1.0`
- TypeScript: `~6.0.2`
- Router: `react-router-dom 7.18.0`
- Data fetching: `@tanstack/react-query 5.101.1`
- Forms/validation: `react-hook-form 7.80.0`, `zod 4.4.3`
- HTTP client: `axios 1.18.1`
- Package manager evidence: `package-lock.json` present, so npm is current source of truth

## Node / Commands

- Scripts:
  - `npm run lint`
  - `npm run typecheck`
  - `npm run test`
  - `npm run build`
- No `engines` field was found in `web/package.json`

## Vercel Status

- No `vercel.json` found
- No `.vercelignore` found
- No middleware-based header enforcement found
- This means Vercel is the intended platform, but project-specific deployment config is not committed in current source

## Environment Variables

Frontend code explicitly references:

- `VITE_API_BASE_URL`
- `VITE_APP_ENV`
- `VITE_APP_VERSION` (settings screen fallback only)
- `VITE_BUILD_DATE` (settings screen fallback only)

## Auth / Session Model

- API client uses `withCredentials: true`
- Access token is held **in memory only**
- Refresh is done through `POST /api/v1/auth/refresh`
- `tokenStore` intentionally avoids localStorage/sessionStorage for access tokens
- Legacy persisted token key is actively removed if found
- Session bootstrap:
  - silent refresh first
  - then `/users/me`
  - blocked requests wait for bootstrap completion
- 401 interceptor performs single-flight refresh and retries original request once

## Route Guards / RBAC

- Global auth guard: `RequireAuth`
- Pages/routes:
  - `/login`
  - `/`
  - `/metro-lines`
  - `/stations`
  - `/stations/:id`
  - `/campaigns`
  - `/campaigns/:id`
  - `/stamp-designs`
  - `/partners`
  - `/milestones`
  - `/rewards`
  - `/analytics`
  - `/rbac`
  - `/settings`
- Frontend role guard note in source:
  - backend `403` is authoritative for permission checks

## Error Handling

Custom API error mapping exists for:

- `403`: permission denied
- `409`: conflict
- generic parse path for other API failures

Observed gaps:

- No specific bespoke UI handling was confirmed for `429` and `503`
- React Query global retry suppression exists for `401`, `403`, `404`

## Upload Flow

- Upload endpoint: `POST /api/v1/admin/uploads/public`
- Multipart upload with `purpose` query param
- Reused image-upload UI for stations, campaigns, stamp designs, milestones, partners
- Frontend assumes backend returns public asset URL

## Hardcoded / Local-Only Assumptions

- Default API base fallback: `http://localhost:8080`
- Settings page fallback build metadata:
  - version fallback `v1.0.0-beta`
  - build date fallback `2024-06-25`
  - env fallback `STAGING`
- Local storage is used for:
  - sidebar collapse state
  - table column widths
- Session storage is used in settings diagnostics

## Mock / Debug / Dev-Only Signals

- No broad mock-data layer was found in web admin production source
- Debug-only commented badge in topbar
- Design/reference and docs folders exist alongside app source but are not runtime code

## Build Evidence

Commands run safely in this pass:

- `npm run lint`: passed
- `npm run typecheck`: passed
- `npm run test`: passed (`2` files, `7` tests)
- `npm run build`: passed

Build warning:

- Vite reported a large production JS chunk: `dist/assets/index-BMN7Y59Q.js` around `830.51 kB` before gzip, which is a performance concern for first release but not a hard deployment blocker.

## Major Page To Backend Areas

- Dashboard / Analytics
  - collection stats
  - station stats
- Metro Lines
  - line list/detail/create/update/delete/reorder
- Stations
  - station list/stats/detail/create/update/delete/reorder
  - scan-key and QR operations
- Campaigns
  - campaign CRUD and station assignment
- Stamp Designs
  - design CRUD and reorder
- Partners
  - partner CRUD and activation state
- Milestones
  - milestone CRUD and reorder
- Rewards / Vouchers
  - reward CRUD and activation
  - voucher import/list/detail/disable
  - bulk reward voucher upload
- RBAC
  - roles, permissions, assignments
- Settings
  - informational only, no deployment-critical backend dependency confirmed

## First-Deployment Feature Classification

| Feature | Status | Notes |
|---|---|---|
| Authentication / session | READY WITH CONFIG | Needs cookie/CORS/prod env alignment |
| Lines | READY | CRUD + reorder wired |
| Stations | READY | CRUD + stats + scan-key adjacencies wired |
| Campaigns | READY | CRUD + station assignment wired |
| Stamp designs | READY | CRUD + reorder wired |
| Partners | READY | CRUD + activate/deactivate wired |
| Rewards / vouchers | READY WITH CONFIG | Needs backend voucher/reconcile closure and prod ops policy |
| Analytics | PARTIAL | Basic collection + station stats only |
| RBAC | PARTIAL | UX exists; backend remains authoritative; permission granularity not fully surfaced client-side |
| Asset upload | READY WITH CONFIG | Depends on backend storage provider and public URL correctness |

## Web Summary

- The admin web app is operational and build-clean.
- Its main release dependencies are backend cookie/CORS/storage correctness and Vercel operator configuration, not missing core screens.
