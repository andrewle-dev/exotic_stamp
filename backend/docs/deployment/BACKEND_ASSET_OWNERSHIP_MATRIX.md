# Exotic Stamp Backend — Asset Ownership Matrix (v2)

**Audit version:** 2  
**Revised:** 2026-07-23  
**Companion:** `BACKEND_PRODUCTION_READINESS_AUDIT.md` v2  

---

## Ownership classes

| Code | Owner | Caching |
|------|-------|---------|
| **A** | Mobile app binary | Until app release |
| **B** | Frontend / Vercel | Edge hashed assets |
| **C** | Backend classpath / server templates | Backend deploy |
| **D** | Amazon S3 (+ optional CDN) | Versioned keys; long cache for public |
| **E** | Never public | Secrets / DB / admin-only |

---

## Public URL derivation (v2)

| Rule | Detail |
|------|--------|
| Persist | `object_key`, `content_type`, `byte_size`, `checksum`, status (`ACTIVE` / `ORPHANED`) |
| Public URL | **`STORAGE_PUBLIC_BASE_URL` + `/` + `object_key`** (normalize slashes once) |
| Do not | Permanently store only a single hardcoded hostname; do not couple DB rows to one Cloudinary account |
| Private objects | No permanent public URL; short-lived presigned GET |

---

## Replace / delete lifecycle (v2)

| Step | Behavior |
|------|----------|
| Upload new version | Write new `object_key` (UUID); point DB to new key |
| Previous object | Mark **ORPHANED** immediately (DB metadata and/or S3 tag) |
| Immediate delete? | **No** — avoid breaking in-flight clients and CDN caches |
| Removal | Delayed lifecycle policy (e.g. 7–30 days) on orphaned prefix/tag |
| Failure modes | If DB commit fails after upload → mark new object orphaned for lifecycle; if upload fails → no DB pointer |

---

## Current backend resources

| Current path | Usage | Owner | Reason | Caching | Deploy impact | Migration risk |
|--------------|-------|-------|--------|---------|---------------|----------------|
| `application*.yml` | Config | **C** + env | Server-owned | N/A | Deploy + env | Low |
| `db/migration/V*.sql` | Schema | **C** | Flyway-first | N/A | Migrate | Medium if edited |
| `logback-spring.xml` | Logging | **C** | Server | N/A | Restart | Low |
| `messages/` | i18n | **C** | Server | N/A | Deploy | Low |
| `static/` (empty) | None | Keep empty | No business media in JAR | — | — | — |
| `infra/mail/template/*.java` | Email HTML | **C** | Server-rendered | Deploy | Copy changes need release | Low |
| Mail `logo-url` Cloudinary HTTPS | Email logo | **D** | Own CDN/S3 | Long | Update `MAIL_LOGO_URL` / `STORAGE_PUBLIC_BASE_URL` object | Low–medium |
| Runtime `uploads/` | Local admin media | **D** in prod | Not Lightsail disk SoT | Versioned keys | S3 cutover | Medium |
| Raw NFC / voucher codes / dumps | Secrets/data | **E** | Never CDN | — | — | Critical if leaked |

No binary image assets were present under `src/main/resources/static` at audit time.

---

## Product asset decisions

| Asset | Owner | Reason | Caching |
|-------|-------|--------|---------|
| App icon / splash / offline logo | **A** | Pre-network | Bundled |
| Critical mobile UI icons | **A** | Offline UX | Bundled |
| Admin favicon / shell | **B** | Vercel | Hashed |
| Email templates | **C** | PII + server | Deploy |
| Station / stamp / partner / campaign / reward images | **D** | Admin-managed without app release | Versioned keys + CDN |
| User private files (if introduced) | **D** private | Privacy | Presign only |
| Class E materials | **E** | Security | — |

---

## Proposed S3 key convention (implemented Batch D)

```text
public/stations/{stationId}/cover/{uuid}.{ext}
public/stamp-designs/{designId}/{uuid}.{ext}
public/partners/{partnerId}/logo/{uuid}.{ext}
public/partners/{partnerId}/banner/{uuid}.{ext}
public/campaigns/{campaignId}/{uuid}.{ext}
public/rewards/{milestoneId}/{uuid}.{ext}
public/temporary/{yyyy}/{mm}/{dd}/{uuid}.{ext}
private/users/{userId}/{uuid}.{ext}
```

Metadata table: `stored_assets` (V21). Public URL = `STORAGE_PUBLIC_BASE_URL` + `/` + `object_key`.
Orphan replace lifecycle: mark ORPHANED; delayed cleanup (default 14d); no immediate delete.
public/partners/{partnerId}/logo/{objectId}.{ext}
public/partners/{partnerId}/banner/{objectId}.{ext}
public/campaigns/{campaignId}/{objectId}.{ext}
public/rewards/{milestoneId}/{objectId}.{ext}
private/users/{userId}/{objectId}.{ext}
orphaned/{yyyy}/{mm}/{dd}/{objectId}.{ext}
temporary/{yyyy}/{mm}/{dd}/{objectId}.{ext}
```

Env isolation: distinct buckets (or IAM-enforced prefixes) for **dev**, **test**, **prod** — see ENV matrix `AWS_S3_BUCKET`.

---

## QR vs NFC media note

Scan payloads are not “assets” for CDN. Physical NFC tags hold NDEF URIs; QR posters are print/media workflows. Do not store raw scan secrets in S3 public prefixes.
