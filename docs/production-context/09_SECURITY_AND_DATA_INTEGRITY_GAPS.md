# Security And Data Integrity Gaps

## Security Findings

### Confirmed

1. Source-embedded secret/default values in backend config
   - Evidence: `backend/src/main/resources/application.yml`, `application-dev.yml`
   - Impact: secret leakage, accidental prod fallback, weak governance

2. Localhost-safe defaults in production-adjacent code paths
   - Evidence: base backend YAML, web API fallback, mobile API fallback
   - Impact: accidental misrouting and weak deployment hygiene

3. Cross-site web auth requires stricter cookie policy than current base config
   - Evidence: web uses `withCredentials: true`; backend base cookie config uses `Lax` and non-forced secure
   - Impact: Vercel staging/prod login/refresh can fail or behave inconsistently unless prod env is exact

4. Mobile Android cleartext traffic enabled
   - Evidence: `mobile/android/app/src/main/AndroidManifest.xml`
   - Impact: release transport policy unsuitable for production/mobile store expectations

5. Internal/admin tooling present in mobile code
   - Evidence: `NFC Tag Writer`, debug API override screen
   - Impact: must remain gated out of release UX and release builds

### Not Reconfirmed As Current Defects

- Broad wildcard CORS with credentials: not currently allowed by code
- Swagger exposure in prod: prod profile disables it and staging Caddy example blocks it
- Access-token persistence to browser storage: current web admin intentionally avoids it

## Data Integrity Findings

### Positive Evidence

- Flyway migrations through `V23`
- Explicit idempotency and reconcile migrations
- reward concurrency and reconcile integration tests exist
- JPA uses `ddl-auto: validate`
- collection/reward cache and idempotency tests exist

### Remaining Risks

1. S3/DB consistency path is not staging-proven
   - Active prod S3 implementation is disabled
   - Upload lifecycle logic exists but target prod path is incomplete

2. Nested repo / dirty state weakens release traceability
   - This is not a direct row-level data bug, but it undermines trustworthy promotion of schema and code together

3. Dev seeding and demo behavior still present in source
   - Needs strict prod exclusion validation during deployment

4. Reward reconcile correctness depends on staging validation
   - Source and tests exist, but no remote staging proof was available

## Summary

The highest-confidence current defects are configuration and release-governance defects, not obvious missing integrity logic in the admin business flows.
