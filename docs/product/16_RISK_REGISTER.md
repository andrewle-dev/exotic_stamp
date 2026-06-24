# 16 — Risk Register: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Founder / Tech Lead / Product / Operations  
> Purpose: Track product, business, technical, data integrity, security, operational, and release risks before MVP execution and go-live.

---

## 1. Feasibility Check

The project is feasible, but the risk profile is higher than a normal mobile CRUD app.

The biggest risks are not code volume. They are:

- dependency on physical station cooperation;
- scan fraud and duplicate data;
- GPS/NFC hardware unreliability;
- reward/voucher liability;
- revenue assumptions depending on MAU/DAU and brand partners;
- junior-team execution risk;
- insufficient production hardening.

The MVP must be treated as a controlled pilot, not a full commercial launch.

---

## 2. Risk Rating Scale

### Probability

| Value | Meaning |
|---|---|
| Low | Unlikely but possible. |
| Medium | Realistic chance. |
| High | Expected unless actively mitigated. |

### Impact

| Value | Meaning |
|---|---|
| Low | Minor inconvenience or isolated bug. |
| Medium | Affects feature quality, timeline, or support load. |
| High | Breaks core product, damages trust, or creates financial/security issue. |
| Critical | Can kill MVP or block launch. |

---

## 3. Critical Business Risks

| ID | Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---|---:|---:|---|---|---|
| BIZ-01 | No MOU/LOI or operational permission from Metro/station authority. | Medium | Critical | Secure written pilot permission before physical NFC/QR deployment. Build demo mode separately. | Founder/BD | Open |
| BIZ-02 | No brand partners before reward launch. | Medium | High | Use internal digital rewards for MVP; do not promise real voucher until LOI/pool exists. | Founder/BD | Open |
| BIZ-03 | DAU/MAU lower than revenue model assumptions. | High | High | Treat monetization as Phase 2; validate retention before scaling cost. | Product/Growth | Open |
| BIZ-04 | Reward cost exceeds expected user value. | Medium | High | Cap reward issuance, configure campaign budget, monitor voucher pool. | Product/Ops | Open |
| BIZ-05 | User sees app as gimmick and churns after first scan. | Medium | Medium | Improve Stamp Book, milestone loop, station content, and sharing. | Product | Open |

---

## 4. Technical Risks

| ID | Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---|---:|---:|---|---|---|
| TECH-01 | NFC compatibility differs across Android devices. | Medium | High | QR fallback, physical device testing, device compatibility matrix. | Mobile Lead | Open |
| TECH-02 | GPS inaccurate in underground or dense urban station areas. | High | Medium/High | Station-specific radius, GPS accuracy threshold, fallback policy. | Backend/Mobile | Open |
| TECH-03 | QR token abuse by screenshot sharing. | Medium | High | Dynamic QR, short TTL, one-time consume, server-side Redis validation. | Backend | Open |
| TECH-04 | Scan key leakage or raw key logging. | Medium | High | Hash/redact scan key, protect logs, audit scan-key admin changes. | Backend | Open |
| TECH-05 | Redis outage affects QR/auth/cache. | Medium | High | Define fail-safe per feature; health check; degraded mode for cache only. | Backend/DevOps | Open |
| TECH-06 | Async reward event lost or processed twice. | Medium | High | Idempotent reward service, DB unique constraint, retry/outbox if needed. | Backend | Open |
| TECH-07 | Ad impression ingestion overloads DB. | Medium | Medium | Append-only design, indexes, rate limit, batch aggregation later. | Backend | Phase 2 |

---

## 5. Data Integrity Risks

| ID | Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---|---:|---:|---|---|---|
| DATA-01 | Duplicate stamp created under concurrent scan. | Medium | Critical | DB unique `(user_id, station_id, campaign_id)` + transaction handling + idempotency key. | Backend | Open |
| DATA-02 | Mobile retry creates duplicate reward. | Medium | High | Idempotency key for collect + unique `(user_id, milestone_id)`. | Backend | Open |
| DATA-03 | Same voucher code allocated to two users. | Medium | Critical | Row-level lock / atomic update + unique voucher assignment. | Backend | Open |
| DATA-04 | Stamp Book returns stale cache after collect. | Medium | Medium | Cache eviction after collect and admin station/campaign updates. | Backend | Open |
| DATA-05 | Admin station edit corrupts historical stamp meaning. | Low/Medium | Medium | Preserve historical stamp fields; avoid hard delete; audit admin changes. | Backend/Ops | Open |
| DATA-06 | Campaign config change reissues old rewards unexpectedly. | Medium | High | Milestone versioning or explicit no-retroactive policy. | Product/Backend | Open |
| DATA-07 | Client-supplied financial/ad metadata pollutes revenue reports. | Medium | High | Server-side ad creative lookup and validation only. | Backend | Phase 2 |

---

## 6. Security & Privacy Risks

| ID | Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---|---:|---:|---|---|---|
| SEC-01 | Access token remains valid after logout/reset. | Medium | High | Server-side access token revocation via jti/version/denylist. | Backend | In progress/Review |
| SEC-02 | Admin endpoint lacks permission check. | Medium | Critical | `@PreAuthorize` on all admin writes; RBAC tests. | Backend | Open |
| SEC-03 | Public API leaks scan key/hash. | Medium | High | Separate public DTO, snapshot tests. | Backend | Open |
| SEC-04 | Voucher code exposed to wrong user/admin. | Medium | Critical | User-scoped queries, permission tiers, audit reveal events. | Backend | Open |
| SEC-05 | File upload allows malicious content/path. | Medium | High | MIME/extension/size validation; storage path isolation. | Backend | Open |
| SEC-06 | Swagger exposed in production. | Medium | Medium/High | Disable/protect Swagger by profile. | DevOps | Open |
| SEC-07 | GPS/device data collected without clear policy. | Medium | Medium | Privacy policy and data minimization. | Founder/Product | Open |

---

## 7. Operational Risks

| ID | Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---|---:|---:|---|---|---|
| OPS-01 | NFC tag damaged/removed at station. | Medium | High | QR fallback, station health checks, physical maintenance plan. | Ops | Open |
| OPS-02 | Station QR display fails to refresh dynamic token. | Medium | High | NFC fallback, monitoring, token display health indicator. | Ops/Backend | Open |
| OPS-03 | Partner voucher pool runs out mid-campaign. | Medium | High | Inventory threshold alerts, pending fulfillment state. | Ops | Open |
| OPS-04 | Admin misconfigures campaign/station status. | Medium | Medium | Admin validation, preview mode, audit log, optional approval workflow. | Product/Ops | Open |
| OPS-05 | Support team cannot diagnose failed scan. | Medium | Medium | Redacted scan diagnostics and error codes. | Backend/Ops | Open |
| OPS-06 | No backup before migration. | Low/Medium | Critical | Release checklist requires backup. | DevOps | Open |

---

## 8. Delivery Risks

| ID | Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---|---:|---:|---|---|---|
| DEL-01 | Team codes before requirements are locked. | High | High | Stage 0 product/technical gate. | Tech Lead | Open |
| DEL-02 | Junior team underestimates anti-cheat/reward complexity. | High | High | Senior review on collection/reward, concurrency tests. | Tech Lead | Open |
| DEL-03 | Scope creep into ads/referral before scan core stable. | High | High | P0/P1/P2 scope enforcement. | Founder/Tech Lead | Open |
| DEL-04 | Mobile starts before API contract stabilizes. | Medium | Medium | Mock contract first; integration after P0 contract. | Mobile/Backend | Open |
| DEL-05 | No real-device testing until late. | Medium | High | Device test starts during Stage 5, not after release. | QA/Mobile | Open |
| DEL-06 | Production hardening treated as optional. | Medium | High | Stage 8 exit gate required before go-live. | Tech Lead | Open |

---

## 9. Revenue / Monetization Risks

| ID | Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---|---:|---:|---|---|---|
| REV-01 | Ad revenue too low at early DAU. | High | Medium | Do not rely on ads for early breakeven. Focus partner validation. | Founder/Growth | Open |
| REV-02 | Fake scans create fake impressions. | Medium | High | Anti-cheat, anomaly detection, server-side validation. | Backend/Product | Open |
| REV-03 | Brand partners require reporting before tracking is reliable. | Medium | High | Build transparent but conservative reporting; label beta metrics. | Product/Backend | Open |
| REV-04 | Pre-stamp ads damage user experience. | Medium | Medium | Frequency cap, short duration, feature flag, A/B testing. | Product | Open |
| REV-05 | Affiliate link tracking inaccurate. | Medium | Medium | UTM/click IDs, server-side click logs, partner reconciliation. | Backend/Growth | Phase 2 |

---

## 10. Risk Mitigation Priorities

### Must fix before MVP pilot

1. `DATA-01` Duplicate stamp.
2. `DATA-02` Duplicate reward.
3. `DATA-03` Voucher double allocation if real vouchers included.
4. `SEC-02` Admin permission gaps.
5. `SEC-03` Public scan-key leak.
6. `TECH-02` GPS policy.
7. `TECH-03` QR abuse policy.
8. `OPS-06` Backup before migration.

### Can defer if not in MVP

1. Full ad mediation stack.
2. Partner dashboard.
3. Referral reward.
4. Web ads.
5. Multi-campaign seasonal logic.
6. iOS release.

---

## 11. Risk Triggers

| Trigger | Action |
|---|---|
| Duplicate stamp found in DB | Freeze collection release, inspect constraint/idempotency. |
| Duplicate voucher issued | Disable voucher reward, audit pool, contact ops. |
| GPS failure rate above threshold | Review station radius/device GPS data. |
| QR abuse reported | Reduce TTL, force NFC-only at affected station, rotate token source. |
| Scan resolve failure spike | Check scan-key cache, station status, Redis. |
| Reward issue failure spike | Disable reward auto-issue if needed, retry/backfill. |
| Auth anomaly/reuse attack | Force logout affected users, rotate secrets if needed. |
| Production migration failure | Stop deploy, restore/forward-fix per release plan. |

---

## 12. Edge Cases / Failure Modes

1. **Real voucher included without inventory monitoring**  
   Users can earn rewards that cannot be fulfilled. This is a business trust failure, not just a backend bug.

2. **Metro permission delayed after dev completion**  
   The product becomes a demo with no physical deployment. Need pilot/demo mode and BD gating.

3. **GPS false negatives at one station**  
   Users at the correct station are rejected. Requires station-specific radius and support diagnostics.

4. **Scan-key rotation not propagated to cache**  
   Old tags continue working or new tags fail. Cache invalidation and audit are mandatory.

5. **Reward policy changes retroactively**  
   Users may receive unexpected rewards or lose expected rewards. Define retroactive policy before admin reward editing.

6. **Ad data presented as guaranteed revenue proof**  
   If anti-cheat and dedup are weak, partner trust collapses. Mark early analytics as beta until validated.

---

## 13. Risk Review Cadence

| Phase | Cadence |
|---|---|
| Stage 0–2 | Weekly risk review. |
| Stage 3–5 | Twice weekly for collection/reward/mobile risks. |
| Pre-release week | Daily risk review. |
| First 7 days post-launch | Daily production health review. |
| After pilot stable | Weekly/monthly depending user volume. |

---

## 14. Risk Register Acceptance Gate

This risk register is accepted only when:

- every critical/high risk has an owner;
- every P0 risk has mitigation or explicit acceptance;
- risks tied to MVP scope are reviewed before implementation;
- deferred risks are not silently shipped;
- release plan includes rollback for SEV-1 risks;
- product/business risks are reviewed alongside technical risks.
