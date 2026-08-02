# Staging Release Checklist

**Batch:** F.0 — AWS items marked PENDING USER ACTION.

## A. Code

- [ ] Clean Git state for official release (or documented allow-dirty prep)
- [ ] Approved commit SHA
- [ ] Remote CI green (GHA) — PARTIAL until observed
- [ ] Coverage green (JaCoCo)
- [ ] Tests green (Surefire + Failsafe)

## B. Database

- [ ] Preflight clean
- [ ] Backup complete
- [ ] Backup verified
- [ ] Flyway version known (packaged V23)

## C. Secrets

- [ ] Generated with required entropy
- [ ] Stored outside Git
- [ ] Staging-only values
- [ ] Previously exposed secrets rotated (ops)

## D. AWS — PENDING USER ACTION (F.1)

- [ ] Lightsail created
- [ ] Firewall correct
- [ ] S3 bucket created
- [ ] IAM least privilege
- [ ] Static IP
- [ ] DNS

## E. Runtime

- [ ] Image immutable tags
- [ ] Image inspected (non-root)
- [ ] Caddy validated
- [ ] Health checks pass

## F. Smoke

- [ ] Auth / station / upload / scan / collect / idempotency / reward

## G. Rollback

- [ ] Prior image retained
- [ ] Rollback command dry-run/tested
- [ ] Backup restore rehearsed on disposable DB
