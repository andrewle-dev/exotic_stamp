# Partner Banner Implementation Report

## Summary

Partners now support an optional landscape `bannerImageUrl` (URL string only) alongside the existing square `logoUrl`. Admins upload/manage it in the web Partners drawer. Mobile Home consumes eligible partner banners via a new public API and renders a metro-themed carousel.

## 1. Files changed (high level)

### Backend
- `db/migration/V19__partner_banner_image_url.sql`
- `modules/reward/domain/model/Partner.java` — field + contract/eligibility helpers
- `PartnerRepository` / `JpaPartnerRepository` / `PartnerRepositoryAdapter`
- Admin create/update commands, views, DTOs, mappers, `AdminRewardCommandService`
- `PartnerPromotionalQueryService`, `PartnerPublicController`, promotional response DTOs
- `SecurityConfig` — `GET /api/v1/partners/**` permitAll
- `docs/api/openapi.json`, `docs/api/MOBILE_API_CONTRACT.md` (repo root)
- Tests: `PartnerEligibilityTest`, `PartnerPromotionalQueryServiceTest`, `PartnerPublicControllerTest`, extended `AdminPartnerControllerTest`

### Web
- `types/partners.ts`, `features/partners/schemas.ts`
- `PartnerFormDrawer.tsx`, `PartnerDetailDrawer.tsx`
- `AssetImageFieldCard.tsx` — `previewAspect: 'square' | 'wide'`

### Mobile
- `HomeSummary` + `PartnerBanner` entity/model
- `HomeRemoteDataSource.getPromotionalBanners`
- `HomeRepositoryImpl` composition
- `HomeBannerCarousel`, `MetroCarouselIndicator`
- `home_screen.dart` wires carousel
- Mock fixtures + home tests

## 2. API changes

### Admin (backward compatible)
- `CreatePartnerRequest` / `UpdatePartnerRequest` / `PartnerResponse` optional field:
  - `bannerImageUrl` (string, max 255)

### Public (new)
- `GET /api/v1/partners/promotional-banners`
- Response `data`: array of
  - `partnerId`, `partnerName`, `logoUrl`, `bannerImageUrl`, `contractStart`, `contractEnd`

## 3. Flyway migration

- **`V19__partner_banner_image_url.sql`**
- `ALTER TABLE partners ADD COLUMN banner_image_url VARCHAR(255);`

## 4. Web admin behavior

- Brand assets section has two independent uploads: Partner logo (square) and Partner banner (wide ≈16:9).
- Upload flow unchanged: public upload → URL into form → save partner.
- Detail drawer shows banner preview when present.
- List table remains logo-only (no clutter).

## 5. Mobile behavior

- Home always shows the promo slot.
- Eligible partner banners drive `HomeBannerCarousel` (`PageView`).
- Empty list → stable “Metro Hanoi” fallback card, **no** indicator.
- `MetroCarouselIndicator`: active = `assets/animations/subway.png`, inactive = rail segments.
- Campaign `activeBanner` still fetched for milestones/progress composition; it no longer powers the Home promo card.

## 6. Banner eligibility rule

Include when **all** are true:

1. `active == true`
2. `bannerImageUrl` non-blank
3. Contract window valid for today (`start` null or `<= today`; `end` null or `>= today`)

Sort: repository `updatedAt DESC`, then contract filter preserves that order.

## 7. Test results

| Suite | Result |
|-------|--------|
| Backend `PartnerEligibilityTest`, `PartnerPromotionalQueryServiceTest`, `PartnerPublicControllerTest`, `AdminPartnerControllerTest` | PASS |
| Web `npm run build` (`tsc -b && vite build`) | PASS |
| Mobile home banner + existing home screen/cubit/repository tests | PASS |

## 8. Non-blocking gaps

- Web has no vitest harness; form behavior verified by TypeScript build only.
- `HomeOfferBanner` remains in the codebase but is unused by `HomeScreen` (campaign card superseded for promo slot).
- OpenAPI checked-in file patched manually (project practice: export from `/v3/api-docs` when needed).
- No impression/click tracking for partner banners (out of scope).

## Manual follow-up

1. Run backend so Flyway applies `V19`.
2. In admin Partners, upload a banner for an active partner with a valid/open contract.
3. Confirm `GET /api/v1/partners/promotional-banners` returns that partner.
4. Refresh mobile Home and verify carousel + subway indicator.
