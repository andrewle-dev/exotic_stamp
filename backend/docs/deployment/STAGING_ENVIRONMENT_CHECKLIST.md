# Staging Environment Checklist

**Batch:** F.0
**STOP POINT:** Do not create AWS resources during F.0.

Spring Boot does **not** auto-load `.env`. Delivery must be via Docker Compose `env_file`, systemd `EnvironmentFile`, or process environment injection.

## Classification legend

| Tag | Meaning |
|-----|---------|
| R | Required |
| O | Optional |
| S | Secret |
| N | Non-secret |
| E | Environment-specific |
| G | Generated locally |
| X | External-resource-derived |
| F1 | Requires user/AWS input in Batch F.1 |

## Application / runtime

| Variable | Class | Staging notes |
|----------|-------|---------------|
| SPRING_PROFILES_ACTIVE | R N E | prod |
| APP_ENV | R N E | staging |
| SERVER_PORT | O N | 8080 internal only |
| BACKEND_URL | R N E X F1 | https://api-staging.<domain> |
| FRONTEND_URL | R N E X F1 | Vercel staging admin |
| CORS_ALLOWED_ORIGINS | R N E X F1 | No * with credentials |
| SERVER_SHUTDOWN_TIMEOUT | O N | 30s |
| JAVA_TOOL_OPTIONS | O N E | MaxRAMPercentage override per tier |

## Database

| Variable | Class | Notes |
|----------|-------|-------|
| DB_URL | R N E | jdbc:postgresql://… |
| DB_HOST / DB_PORT / DB_NAME | R N E | Ops scripts |
| DB_USERNAME | R S/N E G | |
| DB_PASSWORD | R S E G | openssl rand -base64 32 |
| DB_SSL_MODE | O N E F1 | topology-dependent |

## Redis

| Variable | Class | Notes |
|----------|-------|-------|
| REDIS_HOST | R N E | redis |
| REDIS_PORT | R N | 6379 |
| REDIS_PASSWORD | R S E G | openssl rand -base64 24 |
| REDIS_SSL | O N F1 | off when co-located |

## JWT / security

| Variable | Class | Generation | Rotation impact |
|----------|-------|------------|-----------------|
| JWT_SECRET | R S G | openssl rand -base64 32 | Invalidates sessions |
| JWT_ISSUER | R N | exotic-stamp | |
| RATE_LIMIT_KEY_PEPPER | R S G | openssl rand -base64 32 | Resets buckets |
| Cookie settings | O N E F1 | Lax / domain | |

## Storage

| Variable | Class | Notes |
|----------|-------|-------|
| STORAGE_PROVIDER | R N | s3 |
| AWS_REGION | R N E F1 | |
| AWS_S3_BUCKET | R N E X F1 | exotic-stamp-staging-assets |
| STORAGE_PUBLIC_BASE_URL | R N E X F1 | no trailing slash |
| Credential delivery | R S X F1 | Unresolved for Lightsail |

## Mail / ops

MAIL_* required in staging; MANAGEMENT endpoints health/info only; graceful shutdown via SERVER_SHUTDOWN_TIMEOUT.

## Delivery

Prefer Compose env_file or systemd EnvironmentFile at `/etc/exotic-stamp/staging.env` mode 0600. Never commit `.env.staging` / `.env.production`.
