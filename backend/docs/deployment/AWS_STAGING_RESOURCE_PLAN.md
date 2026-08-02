# AWS Staging Resource Plan (Operator-Ready — No Live Creation)

**Batch:** F.0
**STOP POINT: Do not create these resources during Batch F.0.**

Estimated users: 3,000–5,000. Component list only — verify prices in live AWS billing.

Placeholders: `<AWS_ACCOUNT_ID>`, `<DOMAIN>`, `<REGION>` (proposed `ap-southeast-1`).

## Resource inventory

| # | Resource | Name template | User action |
|---|----------|---------------|-------------|
| 1 | Region | `<REGION>` | YES |
| 2 | Lightsail instance/container | exotic-stamp-staging-app | YES |
| 3 | Instance size | Layout A or B | YES |
| 4 | Static IP | exotic-stamp-staging-ip | YES |
| 5 | DNS hostname | api-staging.`<DOMAIN>` | YES |
| 6 | Firewall | 22/80/443 only; deny 5432/6379/8080 | YES |
| 7 | S3 bucket | exotic-stamp-staging-assets | YES |
| 8 | Public media design | CDN or website endpoint | YES |
| 9 | IAM user/role | exotic-stamp-staging-s3 | YES |
| 10 | PostgreSQL | On-box (A) or isolated (B) | YES |
| 11 | Redis | On-box (A) or isolated (B) | YES |
| 12 | Backup storage | Snapshots + off-box dumps | YES |
| 13 | Monitoring | Metrics + uptime on readiness | YES |
| 14 | Monthly cost components | Verify in billing console | YES |

## Layouts

**A. Cost-minimized (staging only):** app + Caddy + PostgreSQL + Redis on one instance. Resource-contention and OOM risk.

**B. Production-like (preferred):** app/Caddy separated from data services.

## JVM by RAM tier

| Tier | Guidance |
|------|----------|
| 1 GB all-in-one | Not recommended |
| 2 GB app-only | MaxRAMPercentage=75 OK |
| 4 GB all-in-one | Lower JVM % (50–60) for PG/Redis headroom |
| OOM | Container killed; Caddy 502; readiness fails |

Do not change Dockerfile default (75) unless local validation proves a defect — override via runtime env.

## Exact AWS Console open point (Batch F.1)

1. Sign in to AWS account `<AWS_ACCOUNT_ID>` (non-prod).
2. Select region `<REGION>`.
3. Lightsail → create instance/container.
4. S3 → create staging bucket + media policy.
5. IAM → least-privilege staging principal.
6. DNS + firewall.
7. Do not perform steps 3–6 during F.0.
