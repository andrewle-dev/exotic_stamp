# Web Admin Backend Dependency Map

## Major Screen To API Map

| Screen / module | Frontend route | Frontend API / hook area | HTTP endpoint(s) | Backend controller | Backend service area | Data dependency | Auth / permission | Deployment blocker | Test evidence |
|---|---|---|---|---|---|---|---|---|---|
| Login | `/login` | `features/auth`, `client.ts` | `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`, `POST /api/v1/auth/logout-all`, `GET /api/v1/users/me` | `AuthController`, `UserController` | auth command/query | PostgreSQL, Redis | public login; authenticated self profile | cookie/CORS cross-site alignment | web auth tests pass |
| Dashboard | `/` | `analytics.api.ts` | `GET /api/v1/admin/collections/stats`, `GET /api/v1/admin/metro/stations/stats` | `CollectionAdminController`, `AdminMetroStationController` | collection query, station query | PostgreSQL | admin + station manage for stats endpoint | none beyond auth | web build/test pass |
| Metro lines | `/metro-lines` | `metro-lines.api.ts` | `/api/v1/admin/metro/lines` CRUD + `/reorder` | `AdminMetroLineController` | line query/command | PostgreSQL | `ADMIN` + `METRO_LINE_MANAGE` | none | backend controller tests present |
| Stations list/detail | `/stations`, `/stations/:id` | `stations.api.ts`, `station-scan-keys.api.ts` | `/api/v1/admin/metro/stations` CRUD + `/stats` + `/reorder` + `/{id}/scan-keys` + `/{id}/rotate-qr`; `/api/v1/admin/metro/scan-keys/*` | `AdminMetroStationController`, `AdminStationScanKeyController` | station query/command, scan-key command/query | PostgreSQL, Redis cache | `ADMIN` + `METRO_STATION_MANAGE` | mobile/admin NFC tooling and scan-key governance still operator-sensitive | backend tests present |
| Campaigns | `/campaigns`, `/campaigns/:id` | `campaigns.api.ts` | `/api/v1/admin/campaigns` CRUD + `/{id}/stations` | `CampaignAdminController` | campaign command/query | PostgreSQL | `ADMIN` + `CAMPAIGN_MANAGE` | none | backend tests present |
| Stamp designs | `/stamp-designs` | `stamp-designs.api.ts` | `/api/v1/admin/stamp-designs` CRUD + `/reorder` | `StampDesignAdminController` | stamp design command/query | PostgreSQL, asset URLs | `ADMIN` + `STAMP_DESIGN_MANAGE` | upload/storage public URL correctness | backend tests present |
| Partners | `/partners` | `partners.api.ts` | `/api/v1/admin/partners` CRUD + `/activate` + `/deactivate` | `AdminPartnerController` | partner command/query | PostgreSQL, asset URLs | admin | upload/storage public URL correctness | backend tests present |
| Milestones | `/milestones` | `milestones.api.ts` | `/api/v1/admin/rewards/milestones` CRUD + `/reorder` | `AdminRewardMilestoneController` | milestone command/query | PostgreSQL | `ADMIN` + `REWARD_MILESTONE_MANAGE` | none | backend tests present |
| Rewards | `/rewards` | `rewards.api.ts`, `vouchers.api.ts` | `/api/v1/admin/rewards` CRUD + activate/deactivate + voucher stats + bulk upload; `/api/v1/admin/rewards/vouchers` list/detail/import/disable; `/reconcile` | `AdminRewardController`, `AdminRewardVoucherController` | reward command/query, voucher pool, reconcile | PostgreSQL, Redis, asset URLs | admin; some voucher endpoints require `VOUCHER_POOL_MANAGE` | reconcile and stock behavior need staging proof | backend tests present |
| Upload widget | embedded | `uploads.api.ts` | `POST /api/v1/admin/uploads/public` | `AdminPublicUploadController` | `PublicAssetUploadService` | local filesystem today, intended S3 in prod | `ADMIN` + `UPLOAD_PUBLIC_ASSET` | S3 path incomplete | backend upload tests present |
| RBAC | `/rbac` | `rbac.api.ts` | `/api/v1/roles*`, `/api/v1/permissions*` | `RoleController`, `PermissionController` | RBAC command/query | PostgreSQL | admin, `RBAC_ADMIN` for sensitive actions | frontend only partially aware of fine-grained authorities | backend tests present |

## Mandatory APIs For First Web Admin Deployment

- `/api/v1/auth/login`
- `/api/v1/auth/refresh`
- `/api/v1/auth/logout`
- `/api/v1/users/me`
- all `/api/v1/admin/metro/lines*`
- all `/api/v1/admin/metro/stations*`
- all `/api/v1/admin/campaigns*`
- all `/api/v1/admin/stamp-designs*`
- all `/api/v1/admin/partners*`
- all `/api/v1/admin/rewards/milestones*`
- all `/api/v1/admin/rewards*`
- `/api/v1/admin/rewards/vouchers*`
- `/api/v1/admin/uploads/public`
- `/api/v1/admin/collections/stats`
- `/api/v1/roles*`
- `/api/v1/permissions*`

## APIs Present But Not Evidently Used By Web Admin

- public metro APIs
- public campaign APIs
- public partner promotional banner API
- collection runtime public/mobile APIs
- notification/community APIs
- mobile app config API
- internal collector-count increment endpoint

## Mismatch / Risk Notes

- Frontend role guards are coarse and explicitly defer to backend `403`.
- Frontend error handling is clear for `403` and `409`, but dedicated UX for `429` / `503` is not strongly evidenced.
- Upload flows assume stable public URLs; this is safe only after production storage/public-base-url is finalized.
- Auth transport differs by client:
  - web uses cookie refresh (`X-Client-Transport: cookie`)
  - mobile uses body refresh (`X-Client-Transport: body`)
- Cross-origin Vercel deployment will require cookie settings stricter than base local defaults.
