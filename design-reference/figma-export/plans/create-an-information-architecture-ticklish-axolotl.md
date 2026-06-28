# Context

The Exotic Stamp Admin is a desktop-first internal back-office tool (1440px) used by founders and operators to manage the full lifecycle of the gamified metro stamp collection platform: metro lines, stations, scan key management, campaigns, stamp designs, partners, milestones, rewards/vouchers, analytics, and access control.

The goal of this plan is to define a complete screen map and navigation structure, grouped into five phased implementation milestones, before any code is written.

---

## Design System Tokens (theme.css)

Update `src/styles/theme.css` `:root` block to match brand spec. Preserve `@theme inline` contract.

| Token | Value |
|---|---|
| `--background` | `#FFFFFF` |
| `--foreground` | `#1D2433` |
| `--card` | `#FFFFFF` |
| `--card-foreground` | `#1D2433` |
| `--primary` | `#01599D` |
| `--primary-foreground` | `#FFFFFF` |
| `--secondary` | `#F4F8FC` |
| `--secondary-foreground` | `#1D2433` |
| `--muted` | `#F4F8FC` |
| `--muted-foreground` | `#6B7280` |
| `--accent` | `#FDEDEB` |
| `--accent-foreground` | `#E83B28` |
| `--destructive` | `#E83B28` |
| `--destructive-foreground` | `#FFFFFF` |
| `--border` | `#E4E7EC` |
| `--ring` | `#01599D` |
| `--radius` | `0.5rem` |

Fonts: **Inter** (UI body) + **JetBrains Mono** (scan keys, codes, masked values). Add both via `src/styles/fonts.css` Google Fonts import.

---

## Global Shell Layout

```
┌─────────────────────────────────────────────────────────┐
│  TOP HEADER                                             │
│  [Logo] Exotic Stamp Admin   [ENV badge]  [User] [Logout]│
├──────────┬──────────────────────────────────────────────┤
│ SIDEBAR  │  MAIN CONTENT AREA                           │
│          │  [Breadcrumb]                                │
│ nav items│  [Page title + primary action button]        │
│          │  [Filter bar]                                │
│          │  [Table / Cards / Charts]                    │
│          │  [Pagination]                                │
└──────────┴──────────────────────────────────────────────┘
```

**Sidebar width:** 240px fixed, collapsible to 64px icon-only rail.  
**Header height:** 56px.  
**Content area:** fluid, max-width 1200px, padded 24px.

### Sidebar Nav Items (in order)

| # | Label | Icon | Route |
|---|---|---|---|
| 1 | Dashboard | LayoutDashboard | `/` |
| 2 | Metro Lines | Train | `/metro-lines` |
| 3 | Stations | MapPin | `/stations` |
| 4 | Campaigns | Megaphone | `/campaigns` |
| 5 | Stamp Designs | Stamp | `/stamp-designs` |
| 6 | Partners | Handshake | `/partners` |
| 7 | Milestones | Trophy | `/milestones` |
| 8 | Rewards & Vouchers | Gift | `/rewards` |
| 9 | Analytics | BarChart2 | `/analytics` |
| 10 | RBAC | ShieldCheck | `/rbac` |
| 11 | Settings | Settings | `/settings` |

---

## Screen Map

### Phase 1 — Foundation Shell

#### S1: Login
- Route: `/login` (full-page, no sidebar/header)
- Centered card, 480px wide
- Logo placeholder, email + password fields, Login button
- States: default / loading / error (invalid credentials)
- Footer: "Internal admin console"

#### S2: Admin Shell (persistent layout)
- Top header: logo, env badge (Local / Staging / Production), admin avatar + name, logout
- Left sidebar: nav items with active highlight, collapse toggle
- Content slot: rendered by router

#### S3: Dashboard
- Route: `/`
- **Stat cards row (6):** Total Stamps Collected, Active Campaigns, Active Stations, Active Partners, Available Vouchers, Top Station
- **Charts row:** Stamps per Campaign (bar), Top 5 Stations by Collector Count (horizontal bar)
- **Operational warnings card:** list of stations missing GPS / scan keys
- **Quick actions:** Add Station, Create Campaign, Upload Asset, Import Vouchers

---

### Phase 2 — Metro Infrastructure

#### S4: Metro Lines List
- Route: `/metro-lines`
- Filters: Search (text), Status (All / Draft / Active / Inactive)
- Table columns: Code, Name, Display Name, Color swatch, Total Stations, Status badge, Sort Order, Updated At, Actions (View → S5, Edit → Drawer, Soft Delete)
- Primary action: **+ Create Line** → opens Drawer D1
- States: loading skeleton, empty state, error state, pagination

#### S5: Metro Line Detail
- Route: `/metro-lines/:id`
- Breadcrumb: Metro Lines > {Line Name}
- Header: Line name, color badge, status badge, Edit button
- Cards: Line info, Station list for this line (linked to S6)

#### Drawer D1: Create / Edit Metro Line
- Fields: Code*, Name*, Display Name, Description, Color hex (with swatch preview), Sort Order, Status
- Footer: Save / Cancel; unsaved-changes warning on close

#### S6: Stations List
- Route: `/stations`
- Filters: Search, Line (dropdown), Status, Scan Key Status, Readiness (All / Ready / Missing GPS / Missing Scan Key)
- Table columns: Code, Name, Line badge, Address, Status, Scan Key badge, GPS badge, Collector Count, Updated At, Actions (View → S7, Edit → Drawer D2, Soft Delete)
- Primary action: **+ Add Station** → Drawer D2

#### S7: Station Detail
- Route: `/stations/:id`
- Breadcrumb: Stations > {Station Name}
- Header: Station name, Line badge, Status badge, Scan Key badge, Edit button
- **Cards:**
  - Station Information (code, name, display name, address, sort order)
  - GPS / Geofence (lat, lng, radius meters, map placeholder)
  - Public Assets (station image preview, stamp preview image)
  - Scan Key Management
    - NFC Tag ID: masked + Reveal button
    - QR Code Value: masked + Reveal button
    - Last QR rotated at, Last updated at
    - Actions: Update Scan Keys, **Rotate QR** (requires confirmation modal with station name/code), Soft Delete (confirmation modal)

#### Drawer D2: Create / Edit Station
- Fields: Line*, Code*, Name*, Display Name, Description, Address, Sort Order, Lat, Lng, Zone Radius (m), Image URL (with Asset Upload trigger), Stamp Preview URL, Status
- Map/geofence preview placeholder

#### Modal M1: Asset Upload
- Trigger from any URL field or Quick Action
- Drag-and-drop zone + file picker
- Shows: file name, image preview, uploaded public URL, Copy URL button, "Use this URL" button
- States: idle, uploading (progress), success, failed, unsupported file type

---

### Phase 3 — Campaigns & Stamp Designs

#### S8: Campaigns List
- Route: `/campaigns`
- Filters: Search, Type (Standard / Seasonal / Event), Status (Draft / Active / Inactive / Archived)
- Table columns: Code, Name, Display Name, Type badge, Status badge, Start Date, End Date, Priority, Updated At, Actions (View → S9, Edit → Drawer D3, Soft Delete)
- Primary action: **+ Create Campaign** → Drawer D3
- Warning row highlight: Active campaign with no stations assigned

#### S9: Campaign Detail
- Route: `/campaigns/:id`
- Breadcrumb: Campaigns > {Campaign Name}
- Header: Campaign name, Type badge, Status badge, date range, Edit button
- **Cards:**
  - Campaign Information
  - Banner / Thumbnail preview
  - Assigned Stations table: Code, Name, Line, Sort Order, Remove action
  - Warning card if no stations assigned
- Actions: Add Station to Campaign (modal selector), Archive / Soft Delete (confirmation)

#### Drawer D3: Create / Edit Campaign
- Fields: Code*, Name*, Display Name, Description, Type*, Start Date/Time*, End Date/Time*, Banner Image URL, Thumbnail URL, Priority, Status
- Validation: start must precede end; warn if Active with no stations

#### S10: Stamp Designs
- Route: `/stamp-designs`
- Toggle: Grid view / Table view
- Filters: Campaign, Station, Rarity (Common / Rare / Epic / Legendary), Status
- Grid card: preview image, Name, Campaign tag, Station tag, Rarity badge, Status badge
- Table columns: Preview thumbnail, Name, Campaign, Station, Rarity, Status, Sort Order, Actions
- Primary action: **+ Create Stamp** → Drawer D4

#### Drawer D4: Create / Edit Stamp Design
- Fields: Campaign*, Station, Name*, Description, Image URL (Asset Upload), Preview Image URL, Rarity*, Status*, Sort Order
- Live image preview panel

---

### Phase 4 — Partners, Milestones & Rewards

#### S11: Partners
- Route: `/partners`
- Table columns: Logo, Name, Contact Email, Contract Start, Contract End, Contract Status badge (Active / Expiring Soon / Expired / Inactive), Actions (Edit → Drawer D5, Activate/Deactivate)
- Primary action: **+ Add Partner** → Drawer D5

#### Drawer D5: Create / Edit Partner
- Fields: Name*, Logo URL (Asset Upload), Contact Email*, Contract Start Date, Contract End Date
- Contract status badge preview

#### S12: Milestones
- Route: `/milestones`
- Table columns: Code, Name, Campaign, Required Stamp Count, Reward Type badge (Voucher / Digital Sticker / Bonus Stamp), Reward Title, Status, Sort Order, Actions
- Timeline preview strip: shows stamp count thresholds for active campaign's milestones
- Primary action: **+ Create Milestone** → Drawer D6

#### Drawer D6: Create / Edit Milestone
- Fields: Campaign*, Code*, Required Stamp Count*, Name*, Description, Reward Type*, Reward Title*, Reward Description, Reward Image URL, Status, Sort Order
- Milestone timeline preview: renders current milestones + new entry position

#### S13: Rewards & Vouchers
- Route: `/rewards`
- **Tabs:** Rewards | Voucher Pool | Import Vouchers

**Tab — Rewards:**
- Table: Name, Reward Type, Partner, Milestone, Value Amount, Expiry Days, Total Stock, Issued Count, Active badge, Actions (Edit → Drawer D7, Activate/Deactivate, Voucher Stats)
- Primary action: **+ Create Reward** → Drawer D7

**Tab — Voucher Pool:**
- Table: Code (masked, reveal per row), Milestone, Status badge (Available / Assigned / Expired / Disabled), Assigned User ID, Assigned At, Expires At, Actions (View, Disable)
- Filters: Milestone, Status

**Tab — Import Vouchers:**
- Select Milestone dropdown
- Textarea for bulk voucher codes (one per line)
- Expires At date picker
- Import button
- Result summary card: Imported / Duplicate / Rejected counts

#### Drawer D7: Create / Edit Reward
- Fields: Milestone*, Partner, Reward Type*, Name*, Description, Value Amount, Expiry Days, Total Stock

---

### Phase 5 — Analytics, RBAC & Settings

#### S14: Analytics
- Route: `/analytics`
- **Stat cards:** Total Stamps Collected, Active Campaigns, Voucher Availability %, Top Station, Stamps Today
- **Charts (recharts):**
  - Stamps per Campaign (vertical bar chart)
  - Station Collector Ranking (horizontal bar, top 10)
- **Operational Readiness panel:** table of stations with readiness issues (Missing GPS / Missing Scan Key / Inactive)
- No revenue analytics — operational data only

#### S15: RBAC
- Route: `/rbac`
- **Tabs:** Roles | Permissions | User Role Assignment

**Tab — Roles:**
- Table: Role Name, Description, Status, System Role badge, Actions (View, Edit → Drawer D8, Assign Permission)
- Confirmation required before editing system roles

**Tab — Permissions:**
- Table: Permission Name, Description (read-only listing)

**Tab — User Role Assignment:**
- User ID input + Lookup button
- Assigned Roles list with Revoke action (confirmation modal)
- Assign Role dropdown + Assign button (confirmation modal)

#### Drawer D8: Edit Role
- Fields: Name, Description, Status
- Warning banner if system role

#### S16: Settings
- Route: `/settings`
- Sections:
  - Admin Profile: display name, email (read-only)
  - API Environment: current env badge, environment selector (with confirmation if switching to Production)
  - Session: active session info, Logout button
  - App: version placeholder

---

## Shared UI Patterns (implement once, reuse everywhere)

| Pattern | Usage |
|---|---|
| `StatusBadge` | All status/type fields — color-coded pill, consistent variants |
| `MaskedValue` | Scan keys, voucher codes — `••••••••` + Reveal toggle |
| `ConfirmModal` | All destructive/sensitive actions — requires typed confirmation for irreversible ops |
| `SlideDrawer` | All create/edit forms — right-side, 480px, Save/Cancel footer |
| `EmptyState` | Every list/table when no records exist |
| `LoadingSkeleton` | Every table/card during data fetch |
| `ErrorState` | Every async surface on failure |
| `Pagination` | All tables — page size selector + prev/next |
| `AssetUpload` (M1) | Any URL field — triggers Modal M1 |
| `UnsavedChangesGuard` | All drawers — warns on close with dirty form |

---

## Implementation Phases

| Phase | Screens / Deliverables |
|---|---|
| **Phase 1** | Login (S1), Admin Shell (S2), Dashboard (S3) |
| **Phase 2** | Metro Lines List (S4), Line Detail (S5), Stations List (S6), Station Detail (S7), Asset Upload Modal (M1) |
| **Phase 3** | Campaigns List (S8), Campaign Detail (S9), Stamp Designs (S10) |
| **Phase 4** | Partners (S11), Milestones (S12), Rewards & Vouchers (S13) |
| **Phase 5** | Analytics (S14), RBAC (S15), Settings (S16) |

---

## Implementation Approach

All screens are rendered in `src/app/App.tsx` as a React single-page app with client-side routing using a `currentScreen` state variable (no external router needed for the prototype). Navigation via sidebar updates the current screen. Drawers and modals are rendered with conditional state. Sample data uses Ho Chi Minh City Metro Line 1 stations (Bến Thành, Nhà hát Thành phố, Ba Son, Văn Thánh, Tân Cảng, Thảo Điền, An Phú, Rạch Chiếc, Phước Long, Bình Thái, Thủ Đức, Khu Công Nghệ Cao, Suối Tiên, Bến Xe Miền Đông).

---

## Verification

1. Start dev server and open at 1440px viewport
2. Verify sidebar navigation switches screens correctly
3. Verify Login screen renders standalone (no sidebar)
4. Verify Dashboard stat cards, charts, and quick actions are visible
5. Verify at least one list screen (Stations) shows table with status badges, filters, and pagination
6. Verify one drawer (Create Station) opens, validates, and closes with unsaved-changes guard
7. Verify one confirmation modal (Rotate QR) requires confirmation text before proceeding
8. Verify MaskedValue hides scan key until Reveal is clicked
9. Verify design tokens match brand spec (blue #01599D primary, red #E83B28 destructive)
