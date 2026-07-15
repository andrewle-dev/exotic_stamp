# SortOrder Reorder — Implementation Summary

Phase 1 and Phase 2 of the SortOrder → drag-and-drop strategy are done. Ordering is persisted via backend batch APIs, not frontend-only state.

## Phase 1 — Metro

### Backend
- `PATCH /api/v1/admin/metro/lines/reorder` — renumbers all lines to `0..n-1`
- `PATCH /api/v1/admin/metro/stations/reorder` — renumbers all stations on a line (two-phase update for unique `sort_order`)

### Admin Web
- Shared `ReorderDrawer` with drag-and-drop (`@dnd-kit`)
- Metro Lines / Stations pages with scoped Reorder buttons

## Phase 2 — Stamp designs & milestones

### Scope
- **Stamp designs:** `campaignId` (catalog order across stations in a campaign)
- **Milestones:** `campaignId` (all non-deleted milestones in a campaign)

### Backend
- `PATCH /api/v1/admin/stamp-designs/reorder` — `{ campaignId, orderedIds }`
- `PATCH /api/v1/admin/rewards/milestones/reorder` — `{ campaignId, orderedIds }`
- Shared helpers in `metro.ExoticStamp.common.reorder`
- Stamp designs list accepts optional `campaignId` (returns full campaign set ordered by `sortOrder`)
- Errors: `400 INVALID_REORDER`, `409 REORDER_CONFLICT`
- OpenAPI updated

### Admin Web
- Stamp Designs / Milestones: **Reorder** enabled only when a campaign is selected
- Uses shared `ReorderDrawer`; Sort columns remain removed from tables

## What did not change
- No campaign-station join-table order
- No inline table dragging
- Mobile unchanged
- Create/edit forms can still set numeric `sortOrder` if needed
