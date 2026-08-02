# Repository And Git Inventory

## Repository Roots

### Root Git repository

- Absolute path: `D:\Part-time\ExoticStamp`
- Current branch: `main`
- Current commit: `cc152bd10ac206eadfd7b1cc5ddf176ed102bfd0`
- Remote `origin`: `https://github.com/andrewle-dev/exotic_stamp.git`
- Default remote branch: `origin/main`
- Status: **dirty**
- Tracked top-level modules: `backend/`, `web/`, `mobile/`, `infra/`, `docs/`, `design-reference/`

### Nested Git repository

- Absolute path: `D:\Part-time\ExoticStamp\backend`
- Current branch: `chore/batch-e2-quality-gate`
- Current commit: `ba6cb60e8eec76f2fd30c23d12edbd56b6955954`
- Remote `exotic`: `https://github.com/itdept-studio/EXOTIC_STAMP.git`
- Remote `origin`: `https://github.com/itdept-studio/EXOTIC_PC.git`
- Default remote branch: not resolvable from local symbolic ref
- Status: **very dirty**
- Risk: nested repo contains its own CI, ignores, artifacts, and SHA lineage independent of the monorepo root

## Git Risks

- The backend is both a monorepo subdirectory and a standalone Git root.
- SHA, branch, and remote identity differ between the monorepo and backend root.
- Release operators could build from one SHA and deploy/document another.
- The nested backend repo shows tracked cache/history content under `.m2/`, `.claude/`, `.cursor/`, and many deletions/untracked files, which is hostile to clean release provenance.

## Dirty-Tree Highlights

### Root repo dirty files

- Modified tracked files exist in `backend/` and `web/`.
- Untracked deployment and CI material exists under `backend/.github/`, `backend/docs/deployment/`, `backend/scripts/`, `backend/infra/`, and new migrations/tests.

### Nested backend repo dirty risks

- Backend nested repo tracks local-cache-like `.m2/` content and editor/agent history paths in status output.
- This strongly suggests the backend repo has previously mixed source and environment artifacts.

## Sanitized Remote Inventory

- Root: GitHub HTTPS remote, no embedded auth visible.
- Backend nested repo: two GitHub HTTPS remotes, no embedded auth visible.

## Top-Level Structure

- `backend/`: Spring Boot API, Flyway migrations, Docker, backend docs, CI, scripts
- `web/`: React/Vite admin web app
- `mobile/`: Flutter mobile app
- `infra/`: local env files and helper scripts
- `docs/`: product, API, release, and local-dev documentation
- `design-reference/`: screenshots, exported design reference, OpenAPI snapshot

## Ignored / Untracked Deployment Files

- Root `.gitignore` ignores `.env`, `.env.*`, Flutter build dirs, backend `target/`, and `backend/uploads/**`.
- Backend `.gitignore` ignores `.env`, `.env.*`, `artifacts/**`, `uploads/**`, logs, `.m2/`, and local agent/editor folders.
- Untracked backend deployment-related content present:
  - `backend/.github/`
  - `backend/docs/deployment/`
  - `backend/docs/security/`
  - `backend/infra/`
  - `backend/scripts/ci/`
  - `backend/scripts/deployment/`
  - `backend/scripts/release/`
  - `backend/scripts/smoke/`

## Secret-Like / Deployment-Sensitive Files

Only path/governance status is reported below.

| Path | Exists | Git tracked | .gitignore protected | Category |
|---|---:|---:|---:|---|
| `.env` | yes | no | yes | root local secrets |
| `.env.example` | yes | yes | no | root env template |
| `backend/.env.example` | yes | yes | no | backend env template |
| `backend/.env.prod-like.example` | yes | no | no | prod-like env template |
| `web/.env` | yes | no | yes | web local env |
| `web/.env.example` | yes | yes | no | web env template |
| `infra/environments/local.env` | yes | yes | no | local infra env |
| `infra/environments/dev.env` | yes | yes | no | dev infra env |
| `mobile/android/key.properties` | no | no | no | Android signing |
| `mobile/android/app/google-services.json` | no | no | no | Google service config |
| `mobile/ios/GoogleService-Info.plist` | no | no | no | iOS Google service config |

## Temporary / Disabled / Drift Indicators

- Disabled Java files:
  - `backend/src/main/java/metro/ExoticStamp/infra/storage/s3/S3ClientConfig.java.disabled`
  - `backend/src/main/java/metro/ExoticStamp/infra/storage/s3/S3ExceptionMapper.java.disabled`
  - `backend/src/main/java/metro/ExoticStamp/infra/storage/s3/S3StorageHealthIndicator.java.disabled`
  - `backend/src/main/java/metro/ExoticStamp/infra/storage/s3/S3StorageService.java.disabled`
- Hardcoded localhost usage exists across backend, web, mobile, infra, and docs.
- No ngrok-specific code was confirmed in production source paths, but multiple docs mention local-only and demo flows.

## Build Artifact Tracking Risk

- Root repo: no obvious tracked `dist/` or `target/` content under the root Git status.
- Nested backend repo: status output shows tracked cache/artifact-like content and large dirty state, which is a release-integrity problem even if some content is being removed.
