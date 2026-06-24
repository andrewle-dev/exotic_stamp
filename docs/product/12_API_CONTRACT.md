# 12 — API Contract: Exotic Stamp / Metro Stamp

> Status: Draft v0.1  
> Owner: Backend Lead / Mobile Lead / Admin Web Lead / QA  
> Purpose: Define the API behavior required for MVP integration across backend, Flutter mobile, admin web, and QA.

---

## 1. Feasibility Check

The API contract is feasible if the backend keeps one rule: every write endpoint must have deterministic behavior under retry, duplicate request, and concurrent request.

For Exotic Stamp, the dangerous endpoints are not normal CRUD endpoints. The dangerous endpoints are:

- scan collect;
- reward evaluation/claim;
- voucher allocation;
- ad impression/click ingestion;
- admin scan-key update.

These endpoints must document success, failure, idempotency, and security behavior before mobile/admin integration.

---

## 2. API Principles

### 2.1 Versioning

All MVP endpoints use:

```text
/api/v1/...
```

Breaking changes require either:

- new endpoint version;
- backward-compatible optional field;
- migration window with mobile version gating.

### 2.2 Response envelope

Recommended standard response:

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "timestamp": "2026-04-01T10:00:00Z"
}
```

Error response:

```json
{
  "success": false,
  "error": {
    "code": "STAMP_DUPLICATE",
    "message": "Stamp already collected",
    "status": 409,
    "path": "/api/v1/collection/stamps/collect",
    "timestamp": "2026-04-01T10:00:00Z"
  }
}
```

### 2.3 Auth

| Endpoint Type | Auth Requirement |
|---|---|
| Register/login/refresh/verify/reset | Public or cookie/token as required. |
| Mobile user features | Bearer access token. |
| Admin operations | Bearer access token + permission. |
| Public station assets | Public read, no sensitive metadata. |
| Monetization tracking | Bearer access token for user-context events; anonymous support only if explicitly approved. |

---

## 3. Error Code Standard

| HTTP | Code | Meaning |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | Request shape or field validation failed. |
| 401 | `AUTH_REQUIRED` | Missing/invalid access token. |
| 403 | `FORBIDDEN` | Authenticated but missing permission. |
| 404 | `RESOURCE_NOT_FOUND` | Resource does not exist or not visible to caller. |
| 409 | `DUPLICATE_RESOURCE` | Unique business invariant conflict. |
| 409 | `STAMP_DUPLICATE` | User already collected station in campaign. |
| 410 | `QR_TOKEN_EXPIRED` | QR token expired or already consumed. |
| 422 | `GPS_OUT_OF_RANGE` | User location not within allowed station radius. |
| 422 | `CAMPAIGN_NOT_ACTIVE` | Campaign is not collectible. |
| 422 | `STATION_NOT_ACTIVE` | Station is not collectible. |
| 429 | `RATE_LIMITED` | Too many requests. |
| 500 | `INTERNAL_ERROR` | Unexpected backend error. |

---

## 4. Authentication APIs

### 4.1 Register

```http
POST /api/v1/auth/register
```

#### Request

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!",
  "displayName": "Metro User"
}
```

#### Response

```json
{
  "userId": "uuid",
  "email": "user@example.com",
  "status": "PENDING_VERIFICATION"
}
```

#### Acceptance criteria

- Duplicate email returns deterministic conflict.
- Password is hashed, never returned.
- Verification mail is queued, not sent synchronously if mail queue exists.

---

### 4.2 Login

```http
POST /api/v1/auth/login
```

#### Request

```json
{
  "email": "user@example.com",
  "password": "StrongPassword123!",
  "deviceFingerprint": "client-generated-device-fingerprint"
}
```

#### Response

```json
{
  "accessToken": "jwt",
  "expiresInSeconds": 900,
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "displayName": "Metro User",
    "roles": ["USER"]
  }
}
```

Refresh token should be delivered by secure cookie or explicitly documented mobile storage strategy.

---

### 4.3 Refresh token

```http
POST /api/v1/auth/refresh
```

#### Requirements

- Detect refresh token reuse.
- Rotate refresh token if policy requires.
- Revoke/denylist old access token if immediate access revocation policy is enabled.

---

### 4.4 Logout

```http
POST /api/v1/auth/logout
```

#### Requirements

- Invalidate refresh token for current device.
- Invalidate current access token if server-side access revocation is enabled.

---

## 5. User APIs

### 5.1 Get current user

```http
GET /api/v1/users/me
Authorization: Bearer <access_token>
```

#### Response

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "displayName": "Metro User",
  "status": "ACTIVE"
}
```

#### Security rule

Do not expose password hash, token version, refresh tokens, reset tokens, or internal audit metadata.

---

## 6. Admin Metro APIs

### 6.1 Create line

```http
POST /api/v1/admin/metro/lines
Authorization: Bearer <admin_token>
Permission: METRO_LINE_MANAGE
```

#### Request

```json
{
  "code": "LINE_1",
  "name": "Metro Line 1",
  "status": "ACTIVE",
  "sortOrder": 1
}
```

#### Response

```json
{
  "id": "uuid",
  "code": "LINE_1",
  "name": "Metro Line 1",
  "status": "ACTIVE"
}
```

---

### 6.2 Create station

```http
POST /api/v1/admin/metro/stations
Authorization: Bearer <admin_token>
Permission: METRO_STATION_MANAGE
```

#### Request

```json
{
  "lineId": "uuid",
  "code": "BEN_THANH",
  "name": "Bến Thành",
  "description": "Central station",
  "latitude": 10.7721,
  "longitude": 106.6983,
  "gpsRadiusMeters": 100,
  "status": "ACTIVE",
  "stampImageUrl": "https://.../stamp.png"
}
```

#### Acceptance criteria

- Station code unique per line.
- Latitude/longitude range validated.
- GPS radius bounded by configured min/max.
- Cache invalidated after write.

---

### 6.3 Update scan key

```http
PUT /api/v1/admin/metro/stations/{stationId}/scan-key
Authorization: Bearer <admin_token>
Permission: METRO_STATION_MANAGE
```

#### Request

```json
{
  "scanType": "NFC",
  "rawKey": "raw-nfc-tag-or-token-source",
  "status": "ACTIVE",
  "rotateExisting": true
}
```

#### Security rules

- Raw scan key must not be logged.
- Raw scan key should be hashed before storage if technically possible.
- Old key must be revoked if `rotateExisting = true`.

---

## 7. Public Metro APIs

### 7.1 List active lines

```http
GET /api/v1/metro/lines
Authorization: optional or Bearer <access_token>
```

### 7.2 List stations by line

```http
GET /api/v1/metro/lines/{lineId}/stations
```

### 7.3 Get station detail

```http
GET /api/v1/metro/stations/{stationId}
```

#### Response fields

```json
{
  "id": "uuid",
  "lineId": "uuid",
  "code": "BEN_THANH",
  "name": "Bến Thành",
  "description": "Central station",
  "imageUrl": "https://...",
  "stampImageUrl": "https://...",
  "publicStatus": "ACTIVE"
}
```

#### Security rule

Public station detail must not expose raw scan key, hash, QR secret, admin notes, or internal fraud metadata.

---

## 8. Scan Resolve API

### 8.1 Resolve scan key

```http
POST /api/v1/metro/scan/resolve
Authorization: Bearer <access_token>
```

#### Request

```json
{
  "scanType": "NFC",
  "scanPayload": "payload-read-from-nfc-or-qr",
  "deviceFingerprint": "client-device-fingerprint"
}
```

#### Response

```json
{
  "station": {
    "id": "uuid",
    "name": "Bến Thành",
    "lineId": "uuid",
    "lineName": "Metro Line 1"
  },
  "collectable": true,
  "requiredGps": true
}
```

#### Data integrity rule

Resolve does not create a stamp.

---

## 9. Collection APIs

### 9.1 Collect stamp

```http
POST /api/v1/collection/stamps/collect
Authorization: Bearer <access_token>
Idempotency-Key: <uuid-or-client-generated-key>
```

#### Request

```json
{
  "scanType": "NFC",
  "scanPayload": "payload-read-from-nfc-or-qr",
  "campaignId": "uuid-or-null-if-default-is-auto-selected",
  "gps": {
    "latitude": 10.7721,
    "longitude": 106.6983,
    "accuracyMeters": 25
  },
  "deviceFingerprint": "client-device-fingerprint",
  "clientCollectedAt": "2026-04-01T10:00:00Z"
}
```

#### Response — success

```json
{
  "stamp": {
    "id": "uuid",
    "stationId": "uuid",
    "stationName": "Bến Thành",
    "campaignId": "uuid",
    "stampDesignUrl": "https://.../stamp.png",
    "collectedAt": "2026-04-01T10:00:05Z"
  },
  "progress": {
    "lineId": "uuid",
    "collectedCount": 3,
    "totalStations": 14,
    "completionPercent": 21.43
  },
  "newRewards": [
    {
      "id": "uuid",
      "milestoneRequiredStamps": 3,
      "rewardType": "DIGITAL_STICKER",
      "status": "ISSUED"
    }
  ]
}
```

#### Duplicate behavior

If user already collected the same station in the same campaign:

Option A — strict duplicate:

```http
409 STAMP_DUPLICATE
```

Option B — idempotent replay if same idempotency key:

```http
200 OK with original stamp response
```

#### Required decision

MVP should support Option B for network retry and Option A for unrelated duplicate scan.

#### Acceptance criteria

- Station resolved server-side.
- GPS is validated server-side.
- Active campaign is selected/validated server-side.
- Duplicate insert cannot happen under concurrent requests.
- Reward evaluation is idempotent.
- Response does not expose scan secrets.

---

### 9.2 Get stamp book

```http
GET /api/v1/collection/stamp-book?lineId={lineId}&campaignId={campaignId}
Authorization: Bearer <access_token>
```

#### Response

```json
{
  "lineId": "uuid",
  "campaignId": "uuid",
  "totalStations": 14,
  "collectedCount": 3,
  "stations": [
    {
      "stationId": "uuid",
      "stationName": "Bến Thành",
      "collected": true,
      "collectedAt": "2026-04-01T10:00:05Z",
      "stampImageUrl": "https://.../stamp.png",
      "lockedImageUrl": "https://.../locked.png"
    }
  ]
}
```

#### Cache rule

Cache may be used, but must be evicted after stamp collect, station update, stamp design update, or campaign config change.

---

## 10. Reward APIs

### 10.1 Get my rewards

```http
GET /api/v1/rewards/me
Authorization: Bearer <access_token>
```

#### Response

```json
{
  "rewards": [
    {
      "id": "uuid",
      "milestoneRequiredStamps": 7,
      "rewardType": "VOUCHER",
      "status": "ISSUED",
      "partnerName": "Highland Coffee",
      "issuedAt": "2026-04-01T10:00:05Z"
    }
  ]
}
```

### 10.2 Reveal voucher code

```http
POST /api/v1/rewards/{userRewardId}/reveal
Authorization: Bearer <access_token>
```

#### Security rule

User can reveal only their own voucher. This endpoint should be audited.

---

## 11. Admin Reward APIs

### 11.1 Create milestone

```http
POST /api/v1/admin/rewards/milestones
Authorization: Bearer <admin_token>
Permission: REWARD_MANAGE
```

#### Request

```json
{
  "campaignId": "uuid",
  "stampsRequired": 7,
  "status": "ACTIVE"
}
```

### 11.2 Upload voucher pool

```http
POST /api/v1/admin/rewards/{rewardId}/vouchers/import
Authorization: Bearer <admin_token>
Permission: REWARD_MANAGE
```

#### Requirements

- Reject duplicate voucher codes.
- Encrypt or protect voucher code storage.
- Do not expose full voucher pool to low-permission admin.

---

## 12. Monetization APIs — MVP Support / Phase 2

### 12.1 Get pre-stamp ad slot

```http
GET /api/v1/monetization/ad-slot?placement=PRE_STAMP&stationId={stationId}
Authorization: Bearer <access_token>
```

### 12.2 Track impression

```http
POST /api/v1/monetization/impressions
Authorization: Bearer <access_token>
```

#### Request

```json
{
  "advertisementId": "uuid",
  "placement": "PRE_STAMP",
  "stationId": "uuid",
  "campaignId": "uuid",
  "displayedAt": "2026-04-01T10:00:00Z",
  "clientEventId": "uuid"
}
```

#### Rule

Client event is not trusted for financial reporting until validated and deduplicated server-side.

---

## 13. Community APIs — Phase 2

### 13.1 Track share event

```http
POST /api/v1/community/share-events
Authorization: Bearer <access_token>
```

#### Request

```json
{
  "platform": "FACEBOOK",
  "shareType": "STAMP_BOOK",
  "stationId": "uuid",
  "campaignId": "uuid"
}
```

### 13.2 Get referral code

```http
GET /api/v1/community/referral-code
Authorization: Bearer <access_token>
```

---

## 14. API Data Integrity Rules

| Endpoint | Integrity Requirement |
|---|---|
| `/auth/refresh` | Old token reuse must be detected and handled. |
| `/admin/metro/stations/*` | Admin changes must invalidate station/scan caches. |
| `/metro/scan/resolve` | Must not mutate collection state. |
| `/collection/stamps/collect` | Must be idempotent for retry and conflict-safe for duplicates. |
| `/collection/stamp-book` | Must reflect latest successful collect after cache eviction. |
| `/rewards/*` | Must not expose another user's reward/voucher. |
| `/monetization/impressions` | Must deduplicate client event ID where applicable. |
| `/community/referrals` | Must block self-referral and duplicate referred user. |

---

## 15. API Edge Cases / Failure Modes

1. **Mobile times out after collect succeeds**  
   Retry with same `Idempotency-Key` should return original successful result.

2. **Same user sends NFC and QR collect concurrently**  
   One request succeeds. The other returns duplicate or original result if same idempotency group.

3. **GPS permission denied**  
   Mobile must not call collect unless backend supports NFC-only fallback. Backend returns validation error if GPS is required.

4. **Campaign auto-selection ambiguity**  
   If more than one active campaign matches station, backend must either reject with `CAMPAIGN_AMBIGUOUS` or use deterministic priority.

5. **Admin rotates scan key while mobile has old QR screen**  
   Old key should return `SCAN_KEY_REVOKED` or `RESOURCE_NOT_FOUND`, not map silently to station.

6. **Reward issuance delayed**  
   Collect response may return no reward if reward is async. Mobile must later refresh rewards. MVP should define sync vs async behavior.

---

## 16. API Acceptance Gate

API contract is accepted only when:

- all P0 mobile flows have request/response/error behavior;
- all admin writes define permission requirement;
- collect endpoint has idempotency and duplicate behavior;
- reward/voucher visibility is user-scoped;
- no endpoint returns raw scan keys, passwords, tokens, voucher pool secrets, or sensitive internals;
- Swagger/OpenAPI matches this contract;
- QA can derive test cases from every endpoint.
