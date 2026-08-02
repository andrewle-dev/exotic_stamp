# Vercel Deployment

Generated on Saturday, July 25, 2026.

## Source Requirements

- Deploy from the monorepo root repository, using the `web/` directory as the Vercel project root.
- Keep `VITE_API_BASE_URL` set to the HTTPS backend origin with no trailing slash.
- Keep `VITE_APP_ENV` as `staging` or `production` for non-local deployments.

## Committed Config

- `web/vercel.json`
  - Vite framework build
  - SPA rewrite to `index.html`
  - baseline security headers
- `web/.vercelignore`
  - excludes docs, local build output, and test files from upload

## Required Environment Variables

- `VITE_API_BASE_URL=https://api-staging.example.com`
- `VITE_APP_ENV=staging`
- `VITE_APP_VERSION=<release-sha-or-tag>`
- `VITE_BUILD_DATE=2026-07-25`

## Cookie / CORS Contract

- Backend refresh cookie must be `HttpOnly`, `Secure`, and `SameSite=None` when the web app is served from a different site on Vercel.
- Backend `CORS_ALLOWED_ORIGINS` must include the exact Vercel frontend origin.
- The frontend always sends `withCredentials: true`; staging should fail closed if the cookie or CORS contract is wrong.

## Do Not Assume

- Vercel env vars are build-time only.
- Changing backend origin requires a rebuild/redeploy of the web app.
- This repository change did not create or modify any live Vercel project.
