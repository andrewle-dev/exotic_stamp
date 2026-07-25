# Lightsail network + Caddy runbook (ExoticStamp backend)

## Goal

Expose the API only through Caddy (80/443). Postgres, Redis, and the JVM process stay on a private network with no public host ports.

## Topology

```
Internet → Lightsail firewall (80/443) → Caddy → backend:8080
                (internal) postgres / redis / optional LocalStack
```

Use `backend/docker-compose.prod-like.yml` as a local rehearsal of this layout.

## Lightsail firewall

| Port | Proto | Source | Purpose |
|------|-------|--------|---------|
| 22 | TCP | admin IP / VPN only | SSH |
| 80 | TCP | 0.0.0.0/0 | HTTP → ACME / redirect |
| 443 | TCP | 0.0.0.0/0 | HTTPS API |
| 8080 | — | **closed** | Backend never public |
| 5432 / 6379 | — | **closed** | DB/Redis never public |

## Caddy

1. Copy `infra/caddy/Caddyfile.example` and set `DOMAIN=api.example.com`.
2. Proxy `/api/*` and `/actuator/health/readiness` only.
3. Return 404 for `/swagger-ui*`, `/v3/api-docs*`, and other `/actuator/*` paths.
4. Keep security headers from the example file.
5. Access logs: stock Caddy emits JSON; redact `Authorization` / `Cookie` in your log pipeline (or rebuild Caddy with transform-encoder).

## Deploy notes

- Build image from **repo root**: `docker build -f backend/Dockerfile .`
- Compose file expects `env_file: .env.prod-like` (gitignored). Start from `.env.prod-like.example`.
- Backend `stop_grace_period` should exceed `server.lifecycle.timeout-per-shutdown-phase` (default 30s).
- HEALTHCHECK hits readiness inside the container network; public LBs should probe `https://$DOMAIN/actuator/health/readiness`.

## Checklist

- [ ] Domain DNS A/AAAA → Lightsail public IP
- [ ] Firewall allows only 80/443 (+ locked-down SSH)
- [ ] Caddy obtains certificate for `$DOMAIN`
- [ ] Curl readiness through Caddy returns 200 when DB/Redis are up
- [ ] Curl `/swagger-ui` through Caddy returns 404
- [ ] Direct `:8080` from the internet is refused
