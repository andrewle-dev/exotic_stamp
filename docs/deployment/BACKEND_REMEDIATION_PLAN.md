# Backend Remediation Plan

Generated on Saturday, July 25, 2026 from repository evidence only.

## Scope

This remediation pass closes the source-level blockers that prevented a controlled web-admin-first staging release:

- secret-like defaults removed from tracked backend config
- Spring profile selection no longer defaults silently to `dev`
- S3 implementation restored to the active compile/test path
- root GitHub Actions workflow added for backend CI execution from the monorepo root
- example environment files updated to document env-only secret injection

## Still Required Before Real Staging

- choose the single deployment source of truth between the root repo and the nested `backend/.git` repo
- run backend CI on GitHub Actions with Docker available
- supply real staging values for database, Redis, JWT, rate-limit pepper, mail, S3, CORS, and cookie domain settings
- perform browser validation against the real staging frontend/backend origins

## Operator Notes

- The backend now expects `JWT_SECRET` to be supplied explicitly in all environments.
- Production cookie defaults are now aligned for cross-site admin hosting with `SameSite=None` and `secure-always=true`.
- Production storage remains fail-fast: `STORAGE_PROVIDER=s3` and non-local public URLs are required.

## Evidence Produced In This Session

- local backend `mvn -B -ntp clean verify -Pci` previously failed only at `CiDockerAvailabilityIT` because Docker was unavailable
- no live AWS, Vercel, DNS, or deployment systems were modified
