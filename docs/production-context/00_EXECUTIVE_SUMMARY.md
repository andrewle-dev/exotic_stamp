# Executive Summary

Generated on Saturday, July 25, 2026 from repository evidence only.

## Verdict

The Exotic Stamp monorepo is **not production-ready today** for the stated "Web Admin first" objective, but it is **close enough for a controlled staging push after source and operator closure work**.

## High-Level Readiness

- Backend: **PARTIAL**
  - Strong coverage of admin APIs, Flyway migrations through `V23`, health probes, Docker image, Caddy examples, and GitHub Actions CI.
  - Main blockers are source-embedded secret/default values, nested Git-root confusion, S3 code currently disabled via `*.disabled`, and CI dependence on Docker availability.
- Web Admin: **READY WITH CONFIG**
  - Admin routes, auth bootstrap, refresh, in-memory token storage, upload flow, RBAC-aware UX, lint/type/test/build all pass locally.
  - Main blockers are missing Vercel project config, cross-site cookie/CORS production settings, and no production CSP/header implementation on the frontend.
- Mobile: **PARTIAL / LATER**
  - Flutter app is healthy for local development and tests, but Android package ID/signing and Android cleartext settings block store-grade release.
  - iOS bundle ID is non-placeholder, but store-signing and release governance remain operator work.

## Most Important Findings

- `backend/src/main/resources/application.yml` and `application-dev.yml` contain hardcoded default credentials-like values and localhost-safe fallbacks that are unacceptable for production governance.
- The repo has **two Git roots**: the monorepo root and `backend/`. This is a release-management risk for CI, SHA traceability, and deployment ownership.
- Backend CI failed in this session because `CiDockerAvailabilityIT` requires Docker when `-Pci` is used.
- S3 production intent exists in config and fail-fast validation, but S3 implementation classes remain checked in as `*.disabled`.
- Web admin assumes backend cookie refresh and `withCredentials: true`; staging/prod will require explicit `SameSite`/`Secure`/origin alignment.
- Mobile Android still uses placeholder `applicationId` and debug signing for release.

## Web Admin First-Release Blockers

- `GAP-001`: Nested backend Git root and dirty release state.
- `GAP-002`: Hardcoded secrets / unsafe defaults in backend config.
- `GAP-003`: Backend CI profile requires Docker and currently fails in this environment.
- `GAP-004`: S3 production path incomplete because active S3 classes are disabled.
- `GAP-005`: No committed Vercel deployment config or documented env binding for admin web.
- `GAP-006`: Cross-site refresh-cookie policy likely incomplete for Vercel-to-backend staging.

## Confidence Notes

- This pack is evidence-based from the current repository and safe local commands only.
- No cloud state, DNS, Vercel project, AWS resource, or live deployment was inspected directly.

## Batch F.0.1 Closure Update

- Root GitHub Actions workflows now exist for backend and web CI.
- Backend S3 source files are restored to the active compile path.
- Backend tracked secret-like defaults were removed from YAML in favor of explicit environment injection.
- Web staging/production builds now require `VITE_API_BASE_URL`, and committed Vercel config/runbook files are present.
