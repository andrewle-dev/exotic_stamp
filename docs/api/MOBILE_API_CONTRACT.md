# Mobile API Contract — Exotic Stamp (Flutter)

> Generated from backend source + `docs/api/openapi.json` (Swagger).  
> **Source of truth:** running app `/v3/api-docs` and Java controllers under `src/main/java/metro/ExoticStamp/modules/**/presentation`.  
> **Base URL (local):** `http://localhost:8080`  
> **Base URL (prod):** `https://backend.facewashfox.com`

---

## Conventions

| Topic | Contract |
|-------|----------|
| **Auth header** | `Authorization: Bearer <accessToken>` on protected routes |
| **Refresh token** | HttpOnly cookie `refresh_token`, path `/api/v1/auth/refresh` only — **never** read from JSON body |
| **Success envelope** | Most mobile APIs: `{ "success": true, "message": "Success", "data": { ... }, "timestamp": "..." }` |
| **Error envelope** | `{ "code": "ERROR_CODE", "message": "...", "status": 4xx, "path": "/api/...", "timestamp": "..." }` |
| **Pagination** | `page` (0-based), `size` (optional; backend default applies when omitted) |
| **Media URLs** | Relative paths like `/uploads/public/...` — prefix with server base URL. Served by `StaticFileController` when `storage.provider=local` |
| **Scan payload** | Mobile `scanKey` maps to request field **`payload`** (raw NFC/QR value from device; not stored server-side) |
| **Idempotency** | Optional UUID `idempotencyKey` on collect; server auto-generates one if omitted. Replay window: `collection.idempotency-window` (default **1 hour**) |

### Scan types

| Value | Meaning |
|-------|---------|
| `NFC` | NFC tag id payload |
| `QR_STATIC` | Static QR token |
| `QR_DYNAMIC_PLACEHOLDER` | Reserved / placeholder |

---

## Status legend

| Status | Meaning |
|--------|---------|
| **IMPLEMENTED** | Endpoint exists and is wired for mobile use |
| **PARTIAL** | Exists but missing fields, composition, or mobile-specific behavior |
| **MISSING** | Not implemented; proposed contract included |

---

## 1. Auth

### 1.1 Register

| | |
|---|---|
| **Method + path** | `POST /api/v1/auth/register` |
| **Auth** | None |
| **Owner** | `AuthController` → `AuthCommandService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | RegisterScreen, OnboardingScreen |

**Request body**

```json
{
  "firstname": "An",
  "lastname": "Nguyen",
  "username": "an.nguyen",
  "email": "an@example.com",
  "phoneNumber": "+84901234567",
  "password": "SecurePass123"
}
```

**Response `200`**

```json
"Registered successfully! Please check your email for verification."
```

**Error example `409`**

```json
{
  "code": "EMAIL_TAKEN",
  "message": "Email already taken",
  "status": 409,
  "path": "/api/v1/auth/register",
  "timestamp": "2026-06-24T10:00:00"
}
```

---

### 1.2 Login

| | |
|---|---|
| **Method + path** | `POST /api/v1/auth/login` |
| **Auth** | None |
| **Owner** | `AuthController` → `AuthCommandService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | LoginScreen |

**Request body**

```json
{
  "identifier": "an@example.com",
  "password": "SecurePass123",
  "deviceFingerprint": "optional-device-id"
}
```

**Response `200`**

```json
{
  "accessToken": "eyJhbG...",
  "tokenType": "Bearer",
  "userInfo": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "an@example.com",
    "username": "an.nguyen",
    "roles": ["USER"]
  }
}
```

> Refresh token is set as HttpOnly cookie; not present in JSON.

**Error example `401`**

```json
{
  "code": "INVALID_CREDENTIALS",
  "message": "Invalid email or password",
  "status": 401,
  "path": "/api/v1/auth/login",
  "timestamp": "2026-06-24T10:00:00"
}
```

---

### 1.3 Refresh

| | |
|---|---|
| **Method + path** | `POST /api/v1/auth/refresh` |
| **Auth** | Cookie `refresh_token` (no Bearer required) |
| **Owner** | `AuthController` → `AuthCommandService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | App bootstrap, TokenRefreshInterceptor |

**Request body** — none. Flutter must send cookies (`Cookie` header or platform cookie jar).

**Response `200`** — same shape as login (`accessToken`, `tokenType`, `userInfo`).

**Error example `401`**

```json
{
  "code": "TOKEN_EXPIRED",
  "message": "Refresh token expired",
  "status": 401,
  "path": "/api/v1/auth/refresh",
  "timestamp": "2026-06-24T10:00:00"
}
```

---

### 1.4 Logout

| | |
|---|---|
| **Method + path** | `POST /api/v1/auth/logout` |
| **Auth** | Bearer + refresh cookie (recommended) |
| **Owner** | `AuthController` → `AuthCommandService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | ProfileScreen, SettingsScreen |

**Response `200`** — empty body; refresh cookie cleared.

---

### 1.5 Forgot password

| | |
|---|---|
| **Method + path** | `POST /api/v1/auth/forgot-password` |
| **Auth** | None |
| **Owner** | `AuthController` → `AuthCommandService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | ForgotPasswordScreen |

**Request body**

```json
{ "email": "an@example.com" }
```

**Response `200`** — empty body (always succeeds from client perspective).

---

### 1.6 Reset password

| | |
|---|---|
| **Method + path** | `POST /api/v1/auth/reset-password` |
| **Auth** | None |
| **Owner** | `AuthController` → `AuthCommandService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | ResetPasswordScreen |

**Request body**

```json
{
  "email": "an@example.com",
  "otp": "123456",
  "newPassword": "NewSecurePass123"
}
```

**Error example `400`**

```json
{
  "code": "OTP_INVALID",
  "message": "Invalid OTP",
  "status": 400,
  "path": "/api/v1/auth/reset-password",
  "timestamp": "2026-06-24T10:00:00"
}
```

---

### 1.7 Me / profile

| | |
|---|---|
| **Method + path** | `GET /api/v1/users/me` |
| **Auth** | Bearer |
| **Owner** | `UserController` → `UserQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | ProfileScreen, HomeScreen (header) |

**Response `200`** — direct `UserResponse` (no `ApiResponse` wrapper):

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstname": "An",
  "lastname": "Nguyen",
  "username": "an.nguyen",
  "email": "an@example.com",
  "phoneNumber": "+84901234567",
  "dob": null,
  "gender": false,
  "bio": null,
  "avatarUrl": null,
  "status": "ACTIVE",
  "created_at": "2026-01-15T08:30:00"
}
```

**Update profile** — `PUT /api/v1/users/me` with optional `firstname`, `lastname`, `bio`, `avatarUrl`, `gender`, `dob`.

---

### 1.8 Auth extras (mobile-relevant)

| Endpoint | Status | Notes |
|----------|--------|-------|
| `POST /api/v1/auth/verify-account` | IMPLEMENTED | `{ "email": "...", "otp": "123456" }` — activates account |
| `POST /api/v1/auth/resend-verification-otp` | IMPLEMENTED | `{ "email": "..." }` — 429 `RESEND_COOLDOWN` on cooldown |
| `POST /api/v1/auth/resend-otp` | IMPLEMENTED | Forgot-password OTP resend |
| `POST /api/v1/auth/logout-all` | IMPLEMENTED | Revoke all sessions |

---

## 2. Home Dashboard

> No single aggregated dashboard endpoint. Compose from the endpoints below.

### 2.1 Dashboard summary (composed)

| | |
|---|---|
| **Method + path** | **MISSING** — proposed: `GET /api/v1/home/summary` |
| **Auth** | Bearer |
| **Status** | **MISSING** |
| **Mobile screens** | HomeScreen |

**Proposed response**

```json
{
  "success": true,
  "data": {
    "collectedCount": 12,
    "totalStations": 20,
    "nextReward": { "milestoneId": "...", "requiredStampCount": 15, "rewardTitle": "..." },
    "recentStamps": [],
    "activeBanner": { "imageUrl": "...", "campaignId": "..." },
    "socialProof": { "totalCollectors": 12500 }
  }
}
```

### 2.2 Composed endpoints (current)

| Field | Endpoint | Status |
|-------|----------|--------|
| `collectedCount`, `totalStations` | `GET /api/v1/collection/progress?lineId={uuid}` | **IMPLEMENTED** |
| `recentStamps` | `GET /api/v1/collection/my-stamps?lineId={uuid}&page=0&size=5` | **IMPLEMENTED** |
| `nextReward` | `GET /api/v1/rewards/milestones?campaignId={uuid}` + client logic | **PARTIAL** |
| `activeBanner` | `GET /api/v1/campaigns/active` → `bannerImageUrl` | **IMPLEMENTED** |
| `socialProof` | — | **MISSING** (collector counts exist only on admin `GET /api/v1/admin/metro/stations/stats`) |

#### `GET /api/v1/collection/progress`

**Owner:** `CollectionRuntimeController` → `CollectionQueryService`  
**Response `data`:**

```json
{
  "lineId": "line-uuid",
  "collected": 12,
  "total": 20,
  "percentage": 60
}
```

#### `GET /api/v1/collection/my-stamps`

**Response `data`:** paginated `UserStampResponse` list.

```json
{
  "content": [
    {
      "stampId": "stamp-uuid",
      "stationId": "station-uuid",
      "lineId": "line-uuid",
      "campaignId": "campaign-uuid",
      "stationName": "Ben Thanh",
      "stampDesignUrl": "/uploads/public/stamps/ben-thanh.png",
      "collectedAt": "2026-06-20T14:22:00",
      "collectMethod": "NFC"
    }
  ],
  "totalElements": 12,
  "totalPages": 1,
  "page": 0,
  "size": 5
}
```

#### `GET /api/v1/campaigns/active`

**Owner:** `CampaignPublicController` → `ActiveCampaignQueryService`  
**Public** (no auth).

```json
{
  "success": true,
  "data": {
    "campaigns": [
      {
        "id": "campaign-uuid",
        "code": "METRO_2026",
        "name": "Metro Stamp 2026",
        "bannerImageUrl": "/uploads/public/campaigns/banner.png",
        "thumbnailImageUrl": "/uploads/public/campaigns/thumb.png",
        "stations": []
      }
    ]
  }
}
```

---

## 3. Metro / Stations

### 3.1 Lines list

| | |
|---|---|
| **Method + path** | `GET /api/v1/metro/lines` |
| **Auth** | None |
| **Owner** | `PublicMetroLineController` → `LineQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | MetroMapScreen, LinePickerScreen, StampBookScreen |

**Response `data`:** array of lines.

```json
[
  {
    "id": "line-uuid",
    "code": "L1",
    "name": "Line 1",
    "displayName": "Metro Line 1",
    "colorHex": "#FF5722",
    "sortOrder": 1,
    "totalStations": 14,
    "status": "ACTIVE"
  }
]
```

---

### 3.2 Station list (line filter)

| | |
|---|---|
| **Method + path** | `GET /api/v1/metro/stations?lineId={uuid}` |
| **Alt paths** | `GET /api/v1/metro/lines/{lineId}/stations` |
| **Auth** | None |
| **Owner** | `PublicMetroStationController` → `StationQueryService` |
| **Status** | **PARTIAL** |
| **Mobile screens** | StationListScreen, MetroMapScreen |

**Query params:** `lineId` (optional UUID).

**Gap vs mobile spec:** no server-side `distance` or `collected` flags.  
- **Distance:** compute client-side from user GPS + `latitude`/`longitude`.  
- **Collected state:** use `GET /api/v1/collection/stamp-book?lineId={uuid}` and merge by `stationId`.

**Response `data`:** array of stations (scan keys **not** included on public list).

```json
[
  {
    "id": "station-uuid",
    "lineId": "line-uuid",
    "code": "S01",
    "name": "Ben Thanh",
    "displayName": "Ben Thanh Station",
    "latitude": 10.772,
    "longitude": 106.698,
    "zoneRadiusMeters": 80,
    "imageUrl": "/uploads/public/stations/ben-thanh.jpg",
    "stampPreviewUrl": "/uploads/public/stamps/preview.png",
    "sortOrder": 1,
    "status": "ACTIVE"
  }
]
```

---

### 3.3 Station detail

| | |
|---|---|
| **Method + path** | `GET /api/v1/metro/stations/{id}` |
| **Auth** | None |
| **Owner** | `PublicMetroStationController` → `StationQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | StationDetailScreen, ScanConfirmScreen |

**Sensitive fields:** `nfcTagId`, `qrCodeValue`, `scanKeyStatus` are **null** on public detail (`includeSensitive=false`). Do not rely on them in Flutter.

```json
{
  "id": "station-uuid",
  "lineId": "line-uuid",
  "lineName": "Line 1",
  "name": "Ben Thanh",
  "description": "...",
  "address": "District 1, HCMC",
  "imageUrl": "/uploads/public/stations/ben-thanh.jpg",
  "stampPreviewUrl": "/uploads/public/stamps/preview.png",
  "latitude": 10.772,
  "longitude": 106.698,
  "zoneRadiusMeters": 80,
  "nfcTagId": null,
  "qrCodeValue": null
}
```

---

### 3.4 Station media URLs

| | |
|---|---|
| **Method + path** | `GET /uploads/**` |
| **Auth** | None (public assets under `/uploads/public/**`) |
| **Owner** | `StaticFileController` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | All image-heavy screens |

Use absolute URL: `{baseUrl}/uploads/public/...` from `imageUrl`, `stampPreviewUrl`, `stampDesignUrl`, campaign banners.

---

### 3.5 Scan resolve (pre-collect preview)

| | |
|---|---|
| **Method + path** | `POST /api/v1/metro/scan/resolve` |
| **Auth** | None |
| **Owner** | `MetroScanResolveController` → `MetroScanResolveService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | ScanScreen, NfcScanScreen, QrScanScreen |

**Request**

```json
{
  "scanType": "NFC",
  "payload": "nfc-tag-id-from-chip",
  "devicePlatform": "android",
  "appVersion": "1.0.0"
}
```

**Response `data`**

```json
{
  "station": {
    "id": "station-uuid",
    "name": "Ben Thanh",
    "lineName": "Line 1",
    "latitude": 10.772,
    "longitude": 106.698,
    "zoneRadiusMeters": 80,
    "imageUrl": "/uploads/public/stations/ben-thanh.jpg",
    "stampPreviewUrl": "/uploads/public/stamps/preview.png"
  },
  "scan": {
    "scanType": "NFC",
    "resolved": true
  }
}
```

**Error example `404`**

```json
{
  "code": "SCAN_KEY_NOT_FOUND",
  "message": "No station matches this scan payload",
  "status": 404,
  "path": "/api/v1/metro/scan/resolve",
  "timestamp": "2026-06-24T10:00:00"
}
```

---

## 4. Scan / Collection

### 4.1 Collect stamp (canonical)

| | |
|---|---|
| **Method + path** | `POST /api/v1/collection/collect` |
| **Auth** | Bearer |
| **Owner** | `CollectionRuntimeController` → `CollectionCommandService` |
| **Status** | **IMPLEMENTED** (reward fields **PARTIAL**) |
| **Mobile screens** | ScanScreen, CollectSuccessScreen, StampBookScreen |

**Request body**

```json
{
  "scanType": "NFC",
  "payload": "nfc-tag-id-from-chip",
  "latitude": 10.7721,
  "longitude": 106.6983,
  "accuracyMeters": 12.5,
  "devicePlatform": "android",
  "appVersion": "1.0.0",
  "idempotencyKey": "550e8400-e29b-41d4-a716-446655440099"
}
```

| Prompt field | API field | Status |
|--------------|-----------|--------|
| `scanType` | `scanType` | IMPLEMENTED |
| `scanKey` | `payload` | IMPLEMENTED (renamed) |
| `latitude` / `longitude` | same | IMPLEMENTED |
| `accuracyMeters` | same | IMPLEMENTED (required, max 200) |
| `deviceFingerprint` | — | **MISSING** on runtime path (login only; legacy `/api/v1/collections` deprecated) |
| `idempotencyKey` | same (UUID) | IMPLEMENTED |
| `clientTimestamp` | — | **MISSING** |
| `appVersion` | same | IMPLEMENTED |

**Response `201`**

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "stamp": {
      "stampId": "stamp-uuid",
      "stationId": "station-uuid",
      "stationName": "Ben Thanh",
      "lineName": "Line 1",
      "lineId": "line-uuid",
      "campaignId": "campaign-uuid",
      "stampDesignUrl": "/uploads/public/stamps/ben-thanh.png",
      "collectedAt": "2026-06-24T10:05:00"
    },
    "progress": {
      "lineId": "line-uuid",
      "collected": 13,
      "total": 20,
      "percentage": 65
    },
    "scan": {
      "scanType": "NFC",
      "gpsDistanceMeters": 15.2,
      "gpsAccuracyMeters": 12.5
    },
    "isNew": true
  }
}
```

| Prompt field | API field | Status |
|--------------|-----------|--------|
| `stampId` | `data.stamp.stampId` | IMPLEMENTED |
| `station` | embedded in `stamp` | IMPLEMENTED |
| `stampDesign` | `stamp.stampDesignUrl` | IMPLEMENTED |
| `collectedAt` | `stamp.collectedAt` | IMPLEMENTED |
| `progress` | `data.progress` | IMPLEMENTED |
| `rewardUnlocked` | — | **MISSING** (rewards issued async via `StampCollectedEvent`) |
| `nextReward` | — | **MISSING** on collect response |

**Idempotency behavior**

1. Client may send `idempotencyKey` (UUID). If omitted, server generates one per request.
2. Within **1 hour** (`collection.idempotency-window`), same key + same user → **201** with prior stamp, `isNew: false`.
3. Same key + **different user** → **409** `IDEMPOTENCY_KEY_CONFLICT`.
4. Duplicate station collect (unique DB constraint) → **409** `STAMP_ALREADY_COLLECTED`.

**Collect error codes (mobile mapping)**

| Mobile code | Backend code | HTTP | When |
|-------------|--------------|------|------|
| `STAMP_DUPLICATE` | `STAMP_ALREADY_COLLECTED` | 409 | Already collected this station/campaign |
| `NFC_INVALID` | `SCAN_KEY_NOT_FOUND`, `SCAN_PAYLOAD_INVALID`, `INVALID_SCAN_METHOD` | 404/400 | Bad or unknown scan payload |
| `QR_EXPIRED` | `SCAN_KEY_INACTIVE` | 422 | QR/NFC keys disabled (closest match; no dedicated `QR_EXPIRED`) |
| `GPS_OUTSIDE_RANGE` | `GPS_OUT_OF_RANGE` | 422 | Outside station zone |
| `STATION_INACTIVE` | `STATION_INACTIVE` | 422 | Station not active |
| `CAMPAIGN_INACTIVE` | `CAMPAIGN_NOT_ACTIVE`, `CAMPAIGN_ARCHIVED` | 422 | Campaign not eligible |
| `UNAUTHORIZED` | `UNAUTHORIZED`, `TOKEN_EXPIRED`, `INVALID_TOKEN` | 401 | Missing/invalid JWT |
| `RATE_LIMITED` | `RESEND_COOLDOWN`, `OTP_MAX_ATTEMPTS_EXCEEDED` | 429 | Auth OTP flows (not collect-specific) |
| `INTERNAL_ERROR` | `INTERNAL_ERROR` | 500 | Unhandled server error |

**Error example `422` GPS**

```json
{
  "code": "GPS_OUT_OF_RANGE",
  "message": "You must be within 80m of the station",
  "status": 422,
  "path": "/api/v1/collection/collect",
  "timestamp": "2026-06-24T10:05:00"
}
```

### 4.2 Collect status (timeout recovery)

| | |
|---|---|
| **Method + path** | `GET /api/v1/collection/collect/status?idempotencyKey={uuid}` |
| **Auth** | Bearer |
| **Owner** | `CollectionRuntimeController` → `CollectionQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | ScanScreen (retry / timeout recovery) |

**Query params**

| Param | Type | Required |
|-------|------|----------|
| `idempotencyKey` | UUID | yes |

**Response `200`**

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "status": "SUCCESS",
    "stamp": {
      "stampId": "stamp-uuid",
      "stationId": "station-uuid",
      "stationName": "Ben Thanh",
      "lineName": "Line 1",
      "lineId": "line-uuid",
      "campaignId": "campaign-uuid",
      "stampDesignUrl": "/uploads/public/stamps/ben-thanh.png",
      "collectedAt": "2026-06-24T10:05:00"
    },
    "progress": {
      "lineId": "line-uuid",
      "collected": 1,
      "total": 5,
      "percentage": 20
    },
    "scan": {
      "scanType": "NFC",
      "gpsDistanceMeters": 15.2,
      "gpsAccuracyMeters": 12.5
    },
    "createdAt": "2026-06-24T10:05:00",
    "resolvedAt": "2026-06-24T10:05:00",
    "errorCode": null
  }
}
```

**Status values**

| Status | Meaning |
|--------|---------|
| `SUCCESS` | Stamp persisted for this user + idempotency key (outside replay window) |
| `DUPLICATE` | Stamp found within `collection.idempotency-window` (safe idempotent retry) |
| `NOT_FOUND` | No stamp for this user + key — collect may not have completed |
| `FAILED` | Reserved — no persisted failure log in MVP schema |
| `PENDING` | Reserved — no in-flight tracking in MVP schema |

**Rules**

- Read-only — never creates stamps.
- Scoped to authenticated user only (other users' keys return `NOT_FOUND`).
- Does not expose raw scan payload or NFC/QR secrets.

**Error example `401`**

```json
{
  "code": "UNAUTHORIZED",
  "message": "Full authentication is required",
  "status": 401,
  "path": "/api/v1/collection/collect/status",
  "timestamp": "2026-06-24T10:00:00"
}
```

### 4.3 Legacy collect (do not use)

`POST /api/v1/collections/collect` — **deprecated**, hidden from Swagger. Use `/api/v1/collection/collect` only.

---

## 5. Stamp Book

### 5.1 Stamp book grid

| | |
|---|---|
| **Method + path** | `GET /api/v1/collection/stamp-book?lineId={uuid}` |
| **Auth** | Bearer |
| **Owner** | `CollectionRuntimeController` → `CollectionQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | StampBookScreen, LineProgressScreen |

**Query:** `lineId` optional — defaults to user's primary line / default campaign.

**Response `data`**

```json
{
  "lineId": "line-uuid",
  "lineName": "Line 1",
  "campaignId": "campaign-uuid",
  "campaignName": "Metro Stamp 2026",
  "progress": { "lineId": "line-uuid", "collected": 12, "total": 20, "percentage": 60 },
  "stations": [
    {
      "stationId": "station-uuid",
      "stationName": "Ben Thanh",
      "sequence": 1,
      "collected": true,
      "stampDesignUrl": "/uploads/public/stamps/ben-thanh.png",
      "collectedAt": "2026-06-20T14:22:00"
    }
  ]
}
```

### 5.2 Stamp detail

| | |
|---|---|
| **Method + path** | **MISSING** — proposed: `GET /api/v1/collection/my-stamps/{stampId}` |
| **Workaround** | Filter `GET /api/v1/collection/my-stamps` or read from stamp-book cell |
| **Status** | **PARTIAL** |
| **Mobile screens** | StampDetailScreen, MemoriesScreen |

---

## 6. Rewards / Voucher

### 6.1 Rewards summary (my rewards)

| | |
|---|---|
| **Method + path** | `GET /api/v1/rewards/my` or `GET /api/v1/rewards/me` |
| **Auth** | Bearer |
| **Owner** | `UserRewardController` → `UserRewardQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | RewardsScreen, WalletScreen |

**Query:** `status` (optional), `page`, `size`.

**Filter values (API):** `ISSUED`, `REDEEMED`, `EXPIRED` (`RewardStatusApi`).  
Domain also has `PENDING_STOCK`, `FAILED`, `CANCELLED` — map in UI as needed.

**Response `data.content[]`**

```json
{
  "id": "user-reward-uuid",
  "campaignId": "campaign-uuid",
  "milestoneId": "milestone-uuid",
  "milestoneCode": "M5",
  "milestoneName": "5 Stamps",
  "rewardType": "VOUCHER",
  "rewardTitle": "Coffee Voucher",
  "rewardDescription": "Free coffee at partner",
  "rewardImageUrl": "/uploads/public/rewards/coffee.png",
  "issuedAt": "2026-06-20T15:00:00",
  "expiresAt": "2026-07-20T15:00:00",
  "redeemedAt": null,
  "status": "ISSUED",
  "voucher": { "id": "voucher-uuid", "code": "COFFEE-ABC123" }
}
```

**Mobile state mapping**

| Mobile state | Backend `status` |
|--------------|------------------|
| `AVAILABLE` | `ISSUED` (not expired, not redeemed) |
| `USED` | `REDEEMED` |
| `EXPIRED` | `EXPIRED` |
| `PENDING_FULFILLMENT` | `PENDING_STOCK` |

### 6.2 Milestone progress

| | |
|---|---|
| **Method + path** | `GET /api/v1/rewards/milestones?campaignId={uuid}` |
| **Auth** | Bearer |
| **Owner** | `UserRewardController` → `MilestoneQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | RewardsScreen, HomeScreen (next reward) |

Returns paginated active milestones with `requiredStampCount`, `rewardTitle`, etc. Client computes progress using `collection/progress`.

### 6.3 Voucher detail

| | |
|---|---|
| **Method + path** | `GET /api/v1/rewards/my/{id}` |
| **Auth** | Bearer |
| **Owner** | `UserRewardController` → `UserRewardQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | VoucherDetailScreen |

Voucher `code` included only for owning user. Do not log or share in analytics.

### 6.4 Voucher redeem

| | |
|---|---|
| **Method + path** | `POST /api/v1/rewards/{id}/redeem` |
| **Auth** | Bearer |
| **Owner** | `UserRewardController` → `RewardCommandService` |
| **Status** | **DISABLED** (returns **410** `REDEEM_NOT_SUPPORTED` in MVP) |
| **Mobile screens** | VoucherDetailScreen |

Treat as **MISSING** for Flutter until MVP gate removed.

---

## 7. Memories / Community

### 7.1 Share event tracking

| | |
|---|---|
| **Method + path** | `POST /api/v1/community/share-events` |
| **Auth** | Bearer |
| **Owner** | `CommunityShareController` → `ShareEventCommandService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | ShareSheet, StampDetailScreen, RewardsScreen |

**Request**

```json
{
  "platform": "facebook",
  "shareType": "stamp_collected",
  "targetId": "stamp-uuid",
  "metadata": { "stationName": "Ben Thanh" }
}
```

**Response `201` `data`**

```json
{
  "id": "share-event-uuid",
  "platform": "facebook",
  "shareType": "stamp_collected",
  "targetId": "stamp-uuid",
  "sharedAt": "2026-06-24T10:10:00"
}
```

### 7.2 Profile memories / share history

| | |
|---|---|
| **Method + path** | `GET /api/v1/community/share-events/me?page=0&size=20` |
| **Auth** | Bearer |
| **Owner** | `CommunityShareController` → `ShareEventQueryService` |
| **Status** | **IMPLEMENTED** |
| **Mobile screens** | MemoriesScreen, ProfileScreen |

### 7.3 Referrals (growth)

| Endpoint | Status | Screen |
|----------|--------|--------|
| `GET /api/v1/community/referral-code` | IMPLEMENTED | ReferralScreen |
| `POST /api/v1/community/referrals/apply` | IMPLEMENTED | OnboardingScreen |
| `GET /api/v1/community/referrals/me` | IMPLEMENTED | ReferralScreen |

### 7.4 Notifications

| Endpoint | Status | Screen |
|----------|--------|--------|
| `GET /api/v1/notifications` | IMPLEMENTED | InboxScreen |
| `PATCH /api/v1/notifications/{id}/read` | IMPLEMENTED | InboxScreen |
| `PATCH /api/v1/notifications/read-all` | IMPLEMENTED | InboxScreen |

---

## 8. Monetization

| Capability | Status | Notes |
|------------|--------|-------|
| Pre-stamp sponsor/ad selection | **MISSING** | Flyway V5 tables exist; no public controller |
| Impression tracking | **MISSING** | Proposed: `POST /api/v1/monetization/impressions` |
| Click tracking | **MISSING** | Proposed: `POST /api/v1/monetization/clicks` |
| Affiliate banners | **MISSING** | Schema only |

**Proposed pre-stamp ad contract**

```
GET /api/v1/monetization/ads/pre-stamp?campaignId={uuid}&stationId={uuid}
→ { "adId", "imageUrl", "clickUrl", "sponsorName" }

POST /api/v1/monetization/impressions
→ { "adId", "placement": "PRE_STAMP", "stationId", "metadata" }
```

---

## Endpoint inventory summary

### Implemented (mobile-ready)

| Group | Count | Key paths |
|-------|-------|-----------|
| Auth | 10 | `/api/v1/auth/*` |
| User profile | 2 | `/api/v1/users/me` |
| Metro | 5 | `/api/v1/metro/lines`, `/stations`, `/scan/resolve` |
| Collection | 4 | `/api/v1/collection/collect`, `/progress`, `/stamp-book`, `/my-stamps` |
| Campaigns | 3 | `/api/v1/campaigns/active`, `/{id}`, `/{id}/stations` |
| Rewards | 4 | `/api/v1/rewards/my`, `/my/{id}`, `/milestones` |
| Community | 7 | share-events, referrals, notifications |
| Static media | 1 | `/uploads/**` |

### Partial

| Item | Gap |
|------|-----|
| Home dashboard | No single summary endpoint; compose 3–4 calls |
| Station list | No server distance / collected flags |
| Collect response | No `rewardUnlocked` / `nextReward` inline |
| Stamp detail | No `GET .../my-stamps/{id}` |
| Reward states | Domain `PENDING_STOCK` vs mobile `PENDING_FULFILLMENT` naming |
| Voucher redeem | Endpoint exists but returns 410 |

### Missing

| Item | Impact |
|------|--------|
| `GET /api/v1/home/summary` | Extra round-trips on home screen |
| `clientTimestamp` on collect | Client/server clock skew diagnostics |
| `deviceFingerprint` on runtime collect | Weaker device binding on scan path |
| Social proof API | Home “X collectors” UI |
| Monetization APIs | Pre-stamp ads and tracking |
| `QR_EXPIRED` dedicated code | Map to `SCAN_KEY_INACTIVE` for now |

---

## Flutter integration blockers

1. **Refresh token cookie** — Flutter HTTP client must persist cookies for `/api/v1/auth/refresh` (path-scoped). Pure Bearer-only clients will fail silent refresh.
2. **Mixed response shapes** — Auth/login and `GET /users/me` return raw DTOs; collection/metro/rewards use `ApiResponse<T>`. Codegen should model both.
3. **Home screen composition** — Plan 3–4 parallel calls (`progress`, `my-stamps`, `campaigns/active`, `rewards/milestones`) until summary API exists.
4. **Collect reward UX** — Milestone unlock is async; poll `GET /rewards/my` or listen to push notifications after collect — do not wait for `rewardUnlocked` in collect response.
5. **Station list merge** — Join `metro/stations` + `collection/stamp-book` client-side for collected badges; compute distance locally.
6. **GPS required** — Collect always requires `latitude`, `longitude`, `accuracyMeters`; handle `GPS_*` errors with user-facing copy.
7. **Voucher redeem** — Disabled (410); show voucher code as “present at partner” only.
8. **Monetization** — Not available; hide ad slots or use static placeholders.

---

## Related artifacts

| File | Purpose |
|------|---------|
| `docs/api/openapi.json` | Full OpenAPI 3 spec (all endpoints including admin) |
| `docs/architecture.md` | Module boundaries and runtime flows |
| `docs/working_pipeline.md` | Scan-to-stamp pipeline |
| `src/main/java/metro/ExoticStamp/config/OpenApiConfig.java` | Swagger metadata |

---

*Last generated: 2026-06-24 from backend source audit.*
