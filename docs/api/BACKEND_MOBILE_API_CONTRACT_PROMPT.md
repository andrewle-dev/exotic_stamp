# Cursor Prompt - Generate Mobile API Contract From Backend

Use this prompt in the backend AI agent / Cursor window.

```text
You are working in the Exotic Stamp backend repository.

Goal:
Generate a mobile-focused API contract for the Flutter app so the mobile agent can implement API integration without scanning the entire backend source.

Read first:
- docs/architecture.md
- docs/working_pipeline.md
- docs/product/12_API_CONTRACT.md if present
- src/main/java/metro/ExoticStamp/config/OpenApiConfig.java
- all controllers under src/main/java/metro/ExoticStamp/modules/**/presentation/controller

Output files to create/update:
1. backend/docs/api/MOBILE_API_CONTRACT.md
2. backend/docs/api/openapi.json if local app can be run and /v3/api-docs is available

Required API groups for mobile:

1. Auth
- register
- login
- refresh
- logout
- forgot password
- reset password
- me/profile

2. Home Dashboard
- summary endpoint or composed endpoints for:
  - collectedCount
  - totalStations
  - nextReward
  - recentStamps
  - activeBanner
  - socialProof

3. Metro / Stations
- lines list
- station list with line filter, distance, collected state
- station detail
- station media URLs

4. Scan / Collection
- NFC-first collect endpoint
- QR fallback if implemented
- request fields:
  - scanType
  - scanKey
  - latitude
  - longitude
  - accuracyMeters
  - deviceFingerprint
  - idempotencyKey
  - clientTimestamp
  - appVersion
- response fields:
  - stampId
  - station
  - stampDesign
  - collectedAt
  - progress
  - rewardUnlocked nullable
  - nextReward nullable
- error codes:
  - STAMP_DUPLICATE
  - NFC_INVALID
  - QR_EXPIRED
  - GPS_OUTSIDE_RANGE
  - STATION_INACTIVE
  - CAMPAIGN_INACTIVE
  - UNAUTHORIZED
  - RATE_LIMITED
  - INTERNAL_ERROR

5. Stamp Book
- stamp book by line/default campaign
- stamp detail

6. Rewards / Voucher
- rewards summary
- milestone progress
- voucher detail
- voucher redeem
- states: AVAILABLE, USED, EXPIRED, PENDING_FULFILLMENT

7. Memories / Community
- share event tracking
- profile memories if implemented

8. Monetization
- pre-stamp sponsor/ad selection if mobile needs to render it
- impression/click tracking if implemented

For every endpoint include:
- Method + path
- Auth requirement
- Request body/query params
- Response body example
- Error response example
- Backend controller/service owning it
- Current status: IMPLEMENTED / PARTIAL / MISSING
- Mobile screen(s) consuming it

Rules:
- Do not invent endpoints. Mark missing endpoints as MISSING with proposed contract.
- Do not expose sensitive fields: password hash, refresh token body if cookie-only, raw NFC secret unless required, voucher secret unless authorized.
- Validate idempotency behavior for collect flow.
- If Swagger differs from docs, Swagger/source code wins.

After creating the docs, run backend tests if possible:
- mvn clean test

Final response:
- Summarize implemented vs missing endpoints.
- List blockers for Flutter integration.
```
