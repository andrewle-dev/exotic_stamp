# Secret Rotation and Delivery Runbook

**Batch:** F.0 — placeholders and process only. No real secrets in git.

## 1. Minimum entropy

| Secret | Minimum |
|--------|---------|
| JWT_SECRET | 32 random bytes, Base64 (openssl rand -base64 32) |
| RATE_LIMIT_KEY_PEPPER | Independent; >=32 random bytes Base64 |
| DB_PASSWORD | >=24 random chars |
| REDIS_PASSWORD | >=24 random chars |
| MAIL_PASSWORD | Provider-issued |
| AWS keys | Break-glass only; prefer temporary credentials |

## 2. Where secrets live on Lightsail

| Store | Guidance |
|-------|----------|
| /etc/exotic-stamp/staging.env | Mode 0600; not in git |
| Docker Compose env_file | Path outside repo |
| systemd EnvironmentFile= | Restart after rotation |

Spring Boot does not read `.env` automatically.

## 3. Rotation impact

| Secret | Impact |
|--------|--------|
| JWT_SECRET | All sessions invalid |
| RATE_LIMIT_KEY_PEPPER | Active buckets reset |
| DB_PASSWORD | Coordinated DB + app restart |
| REDIS_PASSWORD | Coordinated Redis + app restart |
| AWS keys | Upload failures; no DB pointer on failed upload |

## 4. Lightsail to S3 delivery (F.1 decision)

| Option | Status |
|--------|--------|
| A. Static IAM user keys in encrypted env | Documented |
| B. Temporary / instance-linked identity | Unresolved — validate Lightsail SKU |
| C. Sidecar refresh | Future |

Do not assume EC2 instance profiles on Lightsail.

## 5. Isolation

Distinct staging vs production secrets, buckets, DB, Redis. Scripts reject production by default.

## 6. After rotation

Update secret file, restart, run smoke, confirm clean logs, revoke old keys.
