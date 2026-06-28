# Admin Web QA Bug Template

**Project:** Exotic Stamp Admin Web Dashboard (`/web`)
**Use with:** `ADMIN_WEB_STAGING_QA_CHECKLIST.md`

Copy the **Bug Report Template** block below for every defect found during staging QA / UAT. One block per bug. Keep the **Bug Log** table updated as a running index.

> Reporting rules
> - Never paste real credentials, raw auth tokens, full voucher codes, or scan keys into a bug report. Mask them (e.g. `GRAB-****-1234`).
> - Attach screenshots/video instead of transcribing sensitive screens.
> - One defect per report. Split unrelated issues.

---

## Severity definitions

| Severity | Meaning |
|----------|---------|
| **Blocker** | Prevents core admin workflow entirely; no workaround. Blocks release. |
| **Critical** | Major function broken or data integrity/security issue; workaround painful or none. |
| **Major** | Important function broken but a workaround exists; significant UX impact. |
| **Minor** | Small functional or UX defect; limited impact; easy workaround. |
| **Trivial** | Cosmetic / copy / alignment; no functional impact. |

## Status values

`New` → `Triaged` → `In Progress` → `Fixed` → `Verified` → `Closed`
(Alternatives: `Won't Fix`, `Duplicate`, `Cannot Reproduce`, `Deferred`)

---

## Bug Report Template

```
Bug ID:            ESQA-___                  (e.g. ESQA-001)
Title:             <short, specific summary>
Module:            <Auth | Metro Lines | Stations | Station Detail | Scan Keys | Rotate QR |
                    Campaigns | Station Assignment | Stamp Designs | Partners | Milestones |
                    Rewards | Voucher Pool | Import Vouchers | Dashboard | Analytics | RBAC | Other>
Severity:          <Blocker | Critical | Major | Minor | Trivial>

Environment:       STAGING
  - Web build/commit:   <git sha or build id>
  - Backend version:    <version/sha if known>
  - Browser/OS:         <e.g. Chrome 126 / Windows 11>
  - VITE_API_BASE_URL:  <staging backend URL>

Account / role used:  <admin | restricted | other>  (DO NOT include the password)

Steps to reproduce:
  1.
  2.
  3.

Expected result:
  <what should happen>

Actual result:
  <what actually happened>

Screenshot / video:
  <attachment link or filename>

API request/response (if applicable):
  - Method + path:      <e.g. POST /api/v1/admin/metro/stations>
  - Status code:        <e.g. 409>
  - Request body:       <masked; remove secrets>
  - Response body:      <masked; remove secrets>

Console error (if applicable):
  <copy the console error text / stack>

Network error (if applicable):
  <CORS error / timeout / failed request details from the Network tab>

Suspected area:
  <component/route/API guess, optional>

Status:               New
Owner:                <assignee>
Fix verification notes:
  <how the fix was retested and the outcome; filled at Verified/Closed>
```

---

## Bug Log (running index)

| Bug ID | Module | Severity | Title | Status | Owner |
|--------|--------|----------|-------|--------|-------|
| ESQA-001 | | | | New | |
| ESQA-002 | | | | New | |
| ESQA-003 | | | | New | |

> Add rows as needed. Sort/triage by Severity, then Status.
