# Batch E.2 Coverage Gap Analysis

**Date:** 2026-07-24  
**Source:** `target/site/jacoco/jacoco.xml` (post early E.2 unit additions; recalculated before full `-Pci`)  
**Rule:** Do not lower thresholds; no trivial getter/Lombok coverage.

## Configured gates (unchanged)

| Scope | LINE | BRANCH |
|-------|------|--------|
| Overall | >= 60% | >= 50% |
| Auth | >= 70% | >= 60% |
| Collection | >= 70% | >= 60% |
| Reward | >= 70% | >= 60% |

## Baseline snapshot (E.2 mid-batch after targeted unit tests)

| Scope | LINE | BRANCH | Verdict vs gate |
|-------|------|--------|-----------------|
| Overall | ~58.44% | ~44.44% | FAIL |
| Auth | ~66.09% | ~60.58% | LINE FAIL / BRANCH PASS |
| Collection | ~65.47% | ~50.26% | FAIL |
| Reward | ~56.89% | ~37.64% | FAIL |

E.1 reference (pre-E.2): Overall ~53.91%/40.74%; Collection ~64.73%/47.53%; Reward ~38.29%/28.00%.

## Priority inventory (business-critical first)

### 1. Reward (highest priority)

| Class | Missed L (approx) | Criticality | Existing tests | Proposed meaningful tests |
|-------|-------------------|-------------|----------------|---------------------------|
| RewardEvaluationService | high remaining | P0 issuance | unit + IT | fulfillPendingStock concurrency, unique race |
| RewardReconcileService | residual | P0 multi-instance | unit expanded | advisory lock two-worker IT |
| AdminRewardController | residual | P0 admin | WebMvc added | 401/403/busy/dry-run/bounds |
| AdminRewardQuery/Command | high | P1 admin | query unit added | command activate/deactivate/bulk |
| VoucherAllocationService | residual | P0 stock | concurrency IT | pending-stock multi-worker |
| RewardCacheRepository | residual | P2 | unit added | — |
| Milestone/VoucherPool Query | ~0 covered | P2 | none | list/get/not-found |
| Repository adapters | high | P2 | IT indirect | adapter null/edge |
| Domain models | high branches | P2 | sparse | isEvaluable / status transitions only |

### 2. Auth

| Class | Missed L (approx) | Criticality | Proposed |
|-------|-------------------|-------------|----------|
| Redis repositories/adapters | ~128 | P0 security deps | mock Redis failure/success |
| AuthController residual | ~31 | P1 | WebMvc error branches |
| JwtAuthFilter residual | covered in E.2 | P0 | issuer/type/denylist 503 |
| CookieAuthOriginFilter | residual | P1 | CORS negative |
| Persistence adapters | ~14 | P2 | thin adapter tests |

### 3. Collection

| Class | Missed L (approx) | Criticality | Proposed |
|-------|-------------------|-------------|----------|
| CollectionQueryService | ~47 | P1 | validation + query edges |
| CampaignPresentationMapper | ~88 | P2 | realistic mapping |
| UserStampCacheRepository | ~45 | P2 | redis miss/fail |
| Repository adapters | ~80 | P2 | PG IT + unit |
| CollectionPolicyService | residual | P0 | fingerprint conflict/legacy |
| StationScanResolverPortAdapter | ~20 | P2 | resolve inactive |

### 4. Overall (after critical packages)

Prioritize: exception mapping, config validation, security filters, scheduled-job limits, storage failure mapping.  
Do **not** prioritize DTOs/entities for percentage fill.

## Explicit non-priorities

- Getter/setter/constructor-only tests
- Broad JaCoCo exclusions
- Changing production semantics solely for coverage