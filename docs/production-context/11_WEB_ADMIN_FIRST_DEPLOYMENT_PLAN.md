# Web Admin First Deployment Plan

Smallest safe target:

`Vercel admin web -> HTTPS staging backend domain -> Caddy -> backend Docker container -> staging PostgreSQL -> staging Redis -> staging S3`

## A. Source Closure

1. Resolve `GAP-001` and declare the single release Git root.
2. Bind runtime secrets from environment delivery using the committed examples and runbooks.
3. Verify the restored active S3 implementation against Docker-backed CI and staging smoke.
4. Confirm web admin uses only required first-release screens.

Operator action:
- Open **GitHub** to confirm the repository/branch/SHA that will be the sole deployment source of truth.

## B. Remote CI

1. Run backend GitHub Actions on the chosen release SHA.
2. Add or run web admin CI build/test on the same SHA.
3. Preserve backend test reports and JaCoCo artifacts.

Operator action:
- Open **GitHub Actions** and confirm backend CI passes on the chosen SHA.

## C. Vercel Project Preparation

1. Create or select the staging Vercel project for the admin web.
2. Set `VITE_API_BASE_URL` to the future staging backend HTTPS URL.
3. Set `VITE_APP_ENV=staging`.
4. Record any build metadata vars (`VITE_APP_VERSION`, `VITE_BUILD_DATE`) if desired.
5. Apply the committed `web/vercel.json` and `web/.vercelignore` expectations.

Operator action:
- Open **Vercel** and configure project env vars for staging.

## D. AWS Staging Resources

1. Provision staging PostgreSQL.
2. Provision staging Redis.
3. Provision staging S3 bucket and public/base URL strategy.
4. Provision or select Lightsail instance for backend container + Caddy.

Operator action:
- Open **AWS Console** for Lightsail, database/Redis hosting, and S3 setup.

## E. Backend Staging Deployment

1. Build immutable backend image from release SHA.
2. Inject staging env vars:
   - DB / Redis
   - JWT
   - rate-limit pepper
   - `FRONTEND_URL`
   - `BACKEND_URL`
   - `CORS_ALLOWED_ORIGINS`
   - S3 vars
3. Run DB migrations.
4. Start backend container behind internal network only.

## F. Caddy / DNS / TLS

1. Configure Caddy with staging domain.
2. Expose only HTTPS frontend-facing backend domain.
3. Block Swagger and non-health actuator paths.
4. Point staging DNS to Lightsail host.

Operator action:
- Open your **DNS provider** and add/update staging records for the backend hostname.

## G. Web Admin Vercel Deployment

1. Deploy staging admin web from same release SHA.
2. Verify browser calls target the HTTPS backend domain.
3. Confirm cross-site cookie policy works in a real browser.

Operator action:
- Open **Vercel** again to trigger/verify the staging deployment.

## H. Admin Smoke Tests

1. Login
2. Silent refresh after reload
3. Lines CRUD
4. Stations CRUD
5. Campaigns CRUD
6. Stamp designs CRUD
7. Partners CRUD
8. Milestones CRUD
9. Rewards/voucher import/read paths
10. Public asset upload
11. RBAC-sensitive view denial/allow behavior

## I. Rollback

1. Keep prior backend image ref and release manifest.
2. Repoint backend container to previous image.
3. Revert Vercel deployment to previous release if needed.
4. Restore DB from backup only if schema/data corruption occurred.

## J. Promotion Criteria

- Backend CI passes on release SHA
- Web admin build/test passes on release SHA
- Staging login/refresh works from Vercel origin
- Asset upload persists correctly using staging storage path
- No Swagger exposure
- Health endpoints clean
- Admin core CRUD smoke suite passes
