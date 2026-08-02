# Prod-Like Validation

Generated on Saturday, July 25, 2026.

## Validations Run Locally

- Backend: `mvn -B -ntp clean verify -Pci`
  - result: failed at `CiDockerAvailabilityIT`
  - reason: Docker engine was not available for required Testcontainers execution
- Web:
  - `npm run lint` passed
  - `npm run typecheck` passed
  - `npm run test` passed
  - `npm run build` passed
- Mobile:
  - `flutter doctor -v` passed
  - `flutter pub get` passed
  - `flutter analyze` passed
  - `flutter test` passed

## Validations Added In Source

- root backend CI workflow executes from `backend/` and preserves reports as artifacts
- root web CI workflow performs install, lint, typecheck, test, and a staging-like build
- Vite now rejects staging/production builds that omit `VITE_API_BASE_URL`
- Vercel config now includes SPA rewrites and baseline security headers

## Remaining External Validation

- rerun backend CI with Docker available
- run browser login/refresh smoke test from the Vercel-hosted origin to the HTTPS staging backend
- run S3 upload smoke test against the real staging bucket/CDN base URL
