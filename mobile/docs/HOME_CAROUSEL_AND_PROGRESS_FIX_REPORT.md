# Home Carousel and Stamp Progress Fix Report

**Date:** 2026-07-15  
**Scope:** Flutter Home carousel (partner banners + 4-slot train indicator) and Home stamp progress data correctness.

---

## 1. Root cause of `0/0 STAMPS`

Home progress is **not** a dedicated Home Summary API. The app composes Home from several endpoints; the progress card uses:

`GET /api/v1/collection/progress?lineId={first ACTIVE line}`

JSON fields: `collected`, `total`, `percentage` (wrapped in `{ data: ... }`).

**Backend semantics before the fix**

| Field | Query |
|-------|--------|
| `collected` | Distinct stations collected for the **active default campaign** |
| `total` | `COUNT(campaign_stations)` for that campaign |

**Why UI showed `0/0`**

1. Legitimate `0/0` when the default campaign had **zero** `campaign_stations` rows **and** the user had **zero** stamps on that campaign.
2. A common data skew: stamp designs / user stamps exist, but `campaign_stations` was empty or out of sync → `total = 0` while the product expectation was “active stamp designs”.
3. Mobile previously used `?? 0` for missing `collected`/`total`, so a malformed payload (e.g. `collectedCount`/`totalStations` from old docs) would also render as `0/0` with no error.

**Not the cause**

- No hardcoded `14` on Home.
- Mock Home is off by default (`USE_MOCK_DATA`).
- Partner banner count is unrelated to stamp progress.

**Collected vs available**

- “14 stamps” may mean **14 ACTIVE stamp designs** (total), **14 collected** (user), or both `14/14`.
- Progress is campaign-scoped, not global all-time history across campaigns.

---

## 2. Endpoint and response fields

| Item | Value |
|------|--------|
| Endpoint | `GET /api/v1/collection/progress` |
| Auth | Bearer required |
| Query | `lineId` (optional; disambiguates default campaign / cache key) |
| Fields | `lineId`, `collected`, `total`, `percentage` |

Existing names kept (`collected` / `total`). Equivalent to suggested `collectedCount` / `totalCount`.

---

## 3. Mock / hardcoded data

- Production path: live APIs via `HomeRepositoryImpl`.
- Mock: `MockHomeRepository` only when `USE_MOCK_DATA=true` (non-release).
- No hardcoded stamp counts in Home UI.

---

## 4. Backend files changed

| File | Change |
|------|--------|
| `StampDesignRepository.java` | `countActiveByCampaignId` |
| `JpaStampDesignRepository.java` | ACTIVE + not-deleted count query |
| `StampDesignRepositoryAdapter.java` | Adapter wiring |
| `CollectionQueryService.java` | `total` = active stamp designs; pct capped at 100 |
| `ProgressResponse.java` | Schema docs aligned with semantics |
| `UserStampCachePort.java` | `evictUserProgressAll` |
| `UserStampCacheRepository.java` | Evict all `user-progress:{userId}:*` on collect |
| `CollectionQueryServiceTest.java` | Progress scope / 14/14 / 0/14 / cap tests |
| `CollectionRuntimeControllerTest.java` | 14/14 + unauthenticated 401 |
| `UserStampCacheIntegrationIT.java` | Cross-line-key eviction (Docker/Testcontainers) |

---

## 5. Mobile files changed

| File | Change |
|------|--------|
| `home_banner_carousel.dart` | Full banner list; indicator capped via helpers |
| `metro_carousel_indicator.dart` | `slotCount` / `activeSlot`; `maxSlots = 4` |
| `home_summary_model.dart` | Strict progress parse (no silent `?? 0`) |
| `home_remote_datasource.dart` | Maps `FormatException` → Failure |
| `home_cubit.dart` / `home_state.dart` | Request sequencing; soft `refresh()` |
| `home_reload_signal.dart` | Notify Home after successful collect |
| `home_screen.dart` | Listens to reload signal |
| `scan_flow_cubit.dart` | Requests Home reload on new collect |
| `injection.dart` | Wires `HomeReloadSignal` |
| Tests | Carousel, indicator, progress model, screen, cubit |

---

## 6. Fixed four-slot indicator behavior

```text
slotCount = min(bannerCount, 4)   // 0 when bannerCount <= 1
activeSlot = currentPage % slotCount
```

- PageView `itemCount` = **full** banner list (never truncated to 4).
- Indicator never receives partner count as slot count when count ≥ 4.

---

## 7. Carousel behavior by banner count

| Count | Carousel | Autoplay | Indicator |
|------:|----------|----------|-----------|
| 0 | Fallback “Metro Hanoi” | Off | Hidden |
| 1 | Single banner | Off | Hidden |
| 2–3 | All banners | On | **Actual** slot count (no fake empty cars) |
| 4+ | All banners | On | **Exactly 4** slots, modulo mapping |

Chosen for 2–3: render real count (documented product choice).

---

## 8. Stamp progress scope definition

For the **active global default campaign**:

- **`collected`**: distinct stations the authenticated user collected in that campaign.
- **`total`**: count of **ACTIVE, non-deleted stamp designs** in that campaign.
- **`percentage`**: `floor(collected * 100 / total)`, capped at 100 when historical `collected > total`.

Same scope for collected and total. Total is **not** derived from collected rows only.

---

## 9. Cache / refresh behavior

**Backend**

- Progress cache key: `collection:user-progress:{userId}:{lineId}` (TTL ~10m).
- After successful collect, `evictUserProgressAll(userId)` clears **all** line variants (fixes stale zero when Home cached under a different `lineId` than the collected station’s line).

**Mobile**

- Soft `HomeCubit.refresh()` keeps previous summary visible.
- Request sequence discards out-of-order responses.
- After a **new** stamp collect, `HomeReloadSignal` triggers Home soft-refresh (IndexedStack keeps Home mounted).
- Pull-to-refresh still calls full `load()`.

---

## 10. Tests run and results

### Mobile (`flutter test` on Home test files)

```text
All tests passed!
+31
```

Includes:

- 10 banners → 10 pages + 4 indicator slots  
- Slot mapping 0→0, 3→3, 4→0, 9→1  
- 1 banner: no autoplay / no indicator  
- Empty banner fallback  
- Manual swipe + autoplay past index 3  
- Progress parse 14/14, 0/14, malformed fields  
- UI `14/14 STAMPS`, loading without STAMPS, missing progress ≠ `0/0`  
- Cubit soft refresh + stale-request discard  

### Backend

```text
.\mvnw.cmd "-Dtest=CollectionQueryServiceTest,CollectionRuntimeControllerTest" test
→ exit code 0
```

`UserStampCacheIntegrationIT` requires Docker/Testcontainers; **not run** in this environment (Docker unavailable). Cache eviction unit path covered via service/controller tests + code review of `evictUserProgressAll`.

---

## 11. Remaining risks

1. If the **default campaign has zero ACTIVE stamp designs**, UI correctly shows `0/0` (real empty campaign) — admin must assign designs.
2. User stamps on a **non-default / expired** campaign do not count toward Home progress (by design).
3. `UserStampCacheIntegrationIT` should be run where Docker works to confirm Redis pattern eviction.
4. OpenAPI JSON/docs table still mentioning `collectedCount`/`totalStations` in places should be regenerated/aligned when contracts are next published.
5. Soft Home refresh after collect depends on `ScanFlowCubit` wiring `HomeReloadSignal`; widget tests that construct cubits without Injection don’t exercise that path.

---

## Final verdict

| Area | Status |
|------|--------|
| **CAROUSEL DATA LOOP** | **READY** |
| **FOUR-SLOT INDICATOR** | **READY** |
| **STAMP PROGRESS DATA** | **READY** |

Stamp progress READY means: Home reads real `collected`/`total` from `GET /collection/progress` with campaign-scoped active stamp-design totals, without silent malformed → `0/0`, and refreshes after collect. Runtime correctness still depends on seed/admin data having ACTIVE stamp designs on the default campaign.
