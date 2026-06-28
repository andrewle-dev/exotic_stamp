Design a professional desktop web admin dashboard for “Exotic Stamp / Metro Stamp”, a gamified metro stamp collection platform.

Product context:
Exotic Stamp is a mobile app where users collect digital stamps at metro stations using NFC or QR scan. Backend verifies station scan keys, GPS/geofence, campaign eligibility, and issues rewards when users reach stamp milestones. The admin web app is used by internal operators/founders to manage metro data, campaigns, stamp designs, partners, milestones, rewards, voucher pools, and operational analytics.

Design goal:
Create a clean, modern, production-ready Admin Web UI for data operations. It must look like a serious SaaS back-office tool, not a playful mobile app. Prioritize clarity, data integrity, validation states, and safe destructive actions.

Platform:
Desktop-first web app, 1440px width. Also provide responsive tablet behavior if possible.

Visual style:

* Clean SaaS dashboard
* Light mode
* Primary color: metro blue
* Secondary/accent color: red for scan/stamp CTA and dangerous actions
* Neutral gray backgrounds
* Rounded cards
* Dense but readable data tables
* Clear status badges
* Professional typography
* Avoid childish gamification visuals in admin
* Use icons for sidebar navigation

Global layout:

* Left sidebar navigation
* Top header with page title, environment badge “Local / Production”, current admin profile, logout
* Main content area with cards, tables, filters, drawers, modals
* Use breadcrumbs for detail pages
* Use confirmation dialogs for risky actions
* Use empty states and error states

Sidebar navigation:

1. Dashboard
2. Metro Lines
3. Stations
4. Campaigns
5. Stamp Designs
6. Partners
7. Milestones
8. Rewards & Vouchers
9. Analytics
10. RBAC
11. Settings

Screen 1: Admin Login

* Centered login card
* Exotic Stamp / Metro Stamp logo placeholder
* Email field
* Password field
* Login button
* Error state for invalid credentials
* Loading state
* Footer text: “Internal admin console”

Screen 2: Dashboard Overview
Create overview cards:

* Total stamps collected
* Active campaigns
* Active stations
* Active partners
* Available vouchers
* Top collecting station
  Charts/cards:
* Stamps per campaign
* Top 5 stations by collector count
* Recent operational warnings
  Quick actions:
* Add station
* Create campaign
* Upload asset
* Import vouchers

Screen 3: Metro Lines List
Data table columns:

* Code
* Name
* Display name
* Color
* Total stations
* Status badge: DRAFT / ACTIVE / INACTIVE
* Sort order
* Updated at
* Actions: View, Edit, Soft delete
  Filters:
* Search
* Status
  Actions:
* Create Line button
  Create/Edit drawer fields:
* Code
* Name
* Display name
* Description
* Color hex
* Sort order
* Status
  Validation:
* Code and name are required
* Status must be clear

Screen 4: Stations List
Data table columns:

* Station code
* Name
* Line
* Address
* Status
* Scan key status
* GPS readiness
* Collector count
* Updated at
* Actions: View, Edit, Soft delete
  Filters:
* Search
* Line
* Status
* Scan key status
* Readiness: Ready / Missing GPS / Missing scan key
  Create/Edit drawer fields:
* Line
* Station code
* Name
* Display name
* Description
* Address
* Sort order
* Latitude
* Longitude
* Zone radius meters
* Image URL
* Stamp preview URL
* Status
  Include map/geofence preview placeholder.

Screen 5: Station Detail
Header:

* Station name
* Line badge
* Status badge
* Scan key status badge
* “Edit station” button
  Cards:
* Station information
* GPS / Geofence information
* Public assets preview: station image, stamp preview
* Scan key management
  Scan key section:
* NFC Tag ID masked by default, reveal button
* QR Code Value masked by default, reveal button
* Last QR rotated at
* Last scan key updated at
  Actions:
* Update scan keys
* Rotate QR
* Soft delete station
  Important: rotate QR must require confirmation modal with station name/code.

Screen 6: Asset Upload Modal
Drag-and-drop upload zone.
Show:

* File name
* Preview image
* Uploaded public URL
* Copy URL button
* “Use this URL in current form” button
  States:
* Uploading
* Success
* Failed
* Unsupported file

Screen 7: Campaigns List
Data table columns:

* Code
* Name
* Display name
* Type: STANDARD / SEASONAL / EVENT
* Status: DRAFT / ACTIVE / INACTIVE / ARCHIVED
* Start date
* End date
* Priority
* Updated at
* Actions: View, Edit, Soft delete
  Filters:
* Search
* Type
* Status
  Create/Edit drawer fields:
* Code
* Name
* Display name
* Description
* Campaign type
* Start date/time
* End date/time
* Banner image URL
* Thumbnail image URL
* Priority
* Status
  Validation:
* Start date must be before end date
* Active campaign should warn if no assigned stations

Screen 8: Campaign Detail
Header:

* Campaign name
* Type badge
* Status badge
* Date range
  Cards:
* Campaign information
* Banner/thumbnail preview
* Assigned stations
  Assigned stations table:
* Station code
* Name
* Line
* Sort order
* Actions: Remove
  Actions:
* Add station to campaign
* Edit campaign
* Archive/soft delete campaign
  Include warning card if campaign has no station assigned.

Screen 9: Stamp Designs
Grid/table hybrid view.
Filters:

* Campaign
* Station
* Rarity
* Status
  Columns/cards:
* Preview image
* Name
* Campaign
* Station
* Rarity: COMMON / RARE / EPIC / LEGENDARY
* Status: DRAFT / ACTIVE / INACTIVE
* Sort order
* Actions: View, Edit, Soft delete
  Create/Edit drawer fields:
* Campaign
* Station
* Name
* Description
* Image URL
* Preview image URL
* Rarity
* Status
* Sort order
  Show image preview.

Screen 10: Partners
Data table columns:

* Logo
* Name
* Contact email
* Contract start date
* Contract end date
* Active status
* Actions: View, Edit, Activate/Deactivate
  Create/Edit drawer fields:
* Name
* Logo URL
* Contact email
* Contract start date
* Contract end date
  Show contract status badge:
* Active
* Expiring soon
* Expired
* Inactive

Screen 11: Reward Milestones
Data table columns:

* Code
* Name
* Campaign
* Required stamp count
* Reward type: VOUCHER / DIGITAL_STICKER / BONUS_STAMP
* Reward title
* Status
* Sort order
* Actions: View, Edit, Soft delete
  Create/Edit drawer fields:
* Campaign
* Code
* Required stamp count
* Name
* Description
* Reward type
* Reward title
* Reward description
* Reward image URL
* Status
* Sort order
  Show a milestone timeline preview: 3 stamps, 7 stamps, 14 stamps.

Screen 12: Rewards & Voucher Pool
Top tabs:

* Rewards
* Voucher Pool
* Import Vouchers
  Rewards table:
* Name
* Reward type
* Partner
* Milestone
* Value amount
* Expiry days
* Total stock
* Issued count
* Active
* Actions: View, Edit, Activate/Deactivate, Voucher stats
  Create/Edit reward drawer:
* Milestone
* Partner
* Reward type
* Name
* Description
* Value amount
* Expiry days
* Total stock
  Voucher Pool table:
* Code masked by default
* Milestone
* Status
* Assigned user ID
* Assigned at
* Expires at
* Actions: View, Disable
  Import Vouchers modal:
* Select milestone
* Paste multiple voucher codes
* Expires at
* Import button
* Result summary: imported / duplicate / rejected

Screen 13: Analytics
Cards:

* Total stamps collected
* Stamps per campaign
* Top stations by collector count
* Active campaigns
* Voucher availability
  Charts:
* Stamps per campaign bar chart
* Station collector ranking
* Operational readiness warnings
  Do not invent revenue analytics unless API exists. Keep analytics operational.

Screen 14: RBAC
Tabs:

* Roles
* Permissions
* User Role Assignment
  Roles table:
* Role
* Description
* Status
* System role
* Actions: View, Edit, Assign permission
  Permissions table:
* Permission
* Description
  User role assignment panel:
* User ID input
* List assigned roles
* Assign role
* Revoke role
  Warning:
* Show confirmation before modifying roles or permissions.
* Show warning when modifying system roles.

Screen 15: Settings
Sections:

* Current admin profile
* API environment
* Session actions
* Logout
* App version placeholder

Important UX requirements:

* Every list screen must include loading, empty, error, and pagination states.
* Every create/edit drawer must include Save, Cancel, validation error, and unsaved changes warning.
* Every destructive or sensitive action must use confirmation modal.
* Use status badges consistently.
* Do not expose sensitive scan keys or voucher codes directly in tables.
* Design should be realistic for implementation with React + REST API.

Deliverable:
Generate a complete clickable admin web UI wireframe/prototype with all screens above, using realistic sample data for Ho Chi Minh City Metro Line 1 stations such as Bến Thành, Nhà hát Thành phố, Ba Son, Văn Thánh, Tân Cảng, Thảo Điền, An Phú, Rạch Chiếc, Phước Long, Bình Thái, Thủ Đức, Khu Công Nghệ Cao, Suối Tiên, Bến Xe Miền Đông.
