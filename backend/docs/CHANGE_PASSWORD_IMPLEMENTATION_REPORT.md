# Change Password Implementation Report

**Date:** 2026-07-17  
**Feature:** Authenticated user change-password (not forgot/reset OTP)

---

## 1. Endpoint and contract

| Item | Value |
|------|--------|
| Method / path | `POST /api/v1/auth/change-password` |
| Auth | Bearer JWT required (not in `PUBLIC_ENDPOINTS`) |
| Controller | `AuthController` |
| Request | `{ "currentPassword", "newPassword", "confirmNewPassword" }` |
| Success | HTTP 200 `ApiResponse`: `{ success: true, message: "Password changed successfully", data: null }` |
| Tokens in response | **None** (refresh cookie cleared) |

Chosen over `PUT /api/v1/users/me/password` because credential lifecycle (`resetPassword`, `logout`, `logout-all`) already lives on `AuthController`.

---

## 2. Files changed

### Backend
- `AuthCommandService` — `changePassword(...)`; shared `PasswordPolicy` on register/reset
- `AuthController` — endpoint + cookie clear
- `AuthPresentationMapper`, `ChangePasswordRequest`, `ChangePasswordCommand`
- `PasswordPolicy` + domain exceptions (`CurrentPasswordIncorrect`, `PasswordConfirmationMismatch`, `NewPasswordSameAsCurrent`, `PasswordPolicyViolation`)
- `GlobalExceptionHandler` — error code mappings
- `AccessToken.REASON_PASSWORD_CHANGED`
- `RegisterRequest` / `ResetPasswordRequest` — shared min/max lengths
- OpenAPI: `backend/docs/api/openapi.json`, `docs/api/openapi.json`
- Tests: `AuthCommandServiceTest`, `AuthControllerTest`, `AuthWebMvcTestSecurityConfig`

### Mobile
- Auth stack: request model, remote DS, repository, use case, cubit, screen
- Routes: `RouteNames.changePassword`, `app_router.dart`
- Privacy & Security tile enabled; Help Center copy updated
- `Validators` password min aligned to 8; `ErrorMapper` codes
- OpenAPI: `mobile/docs/api/openapi.json`
- Tests: cubit, screen, privacy_security_screen

---

## 3. Password validation policy

Shared `PasswordPolicy`:
- Min length **8**, max length **50**
- Used by: register, reset-password, change-password (DTO `@Size` + service `validatePlaintext`)
- Confirmation match checked in service → `PASSWORD_CONFIRMATION_MISMATCH`
- Same-as-current checked **after** current-password verification → `NEW_PASSWORD_SAME_AS_CURRENT`

---

## 4. Session invalidation policy

After password persist (same path as reset / logout-all):

1. `accessTokenRepository.revokeAllByUserId(..., REASON_PASSWORD_CHANGED)`
2. `refreshTokenStore.revokeAllForUser(userId)`
3. `bumpTokenVersionAndSyncRedis(userId)` — invalidates all access JWTs via `tokenVersion`
4. Clear refresh cookie on the response

No replacement tokens issued. Client must log in again.

---

## 5. Transaction boundary

Single `@Transactional` on `changePassword`:
- User load → verify → encode → save password + `passwordUpdateAt`
- Refresh revoke + `tokenVersion` increment in the same transaction
- Redis tokenVersion cache update follows DB increment (existing `bumpTokenVersionAndSyncRedis`)
- Audit write is best-effort (`AuditLogService` swallows failures)

---

## 6. Concurrency strategy

`User` has no `@Version`. Policy:

- Second concurrent request with the **old** password fails with `CURRENT_PASSWORD_INCORRECT` after the first request replaced the hash.
- Documented and covered by unit test `changePassword_concurrentSecondRequestFailsWhenCurrentNoLongerMatches`.

---

## 7. Audit behavior

On success only: `PASSWORD_CHANGED` on table `users`, metadata `{ "sessionsRevoked": true }`, optional IP.  
No passwords, hashes, or JWTs in audit or logs. Failures log `userId` + generic reason only.

---

## 8. Security configuration

- Endpoint **not** in `PUBLIC_ENDPOINTS` → authenticated via JWT filter
- User ID from `@AuthenticationPrincipal` / `resolveUserId` only
- No admin role required
- CSRF remains disabled (stateless JWT model unchanged)

**Rate limiting:** Project has no HTTP rate limiter for auth endpoints (OTP cooldown only). **Gap documented:** no per-user/IP failed-attempt throttle on change-password; do not lock the account permanently.

---

## 9. Mobile integration status

**Implemented.**

Settings → Privacy & Security → Change Password:
- Current / new / confirm fields with obscure toggles
- Client validation + backend error mapping
- Submit loading + duplicate-submit guard in cubit
- On success: `apiClient.clearSession()` → `markUnauthenticated` → navigate to login + snackbar

---

## 10. Tests added

### Backend unit
- Success encode + revoke + audit + tokenVersion once
- Wrong current / mismatch / same password / weak / inactive / not found
- Authenticated userId used (not alternate identity)
- Concurrent second request fails
- Old password cannot login / new can

### Backend MVC
- Unauthenticated → 401
- Authenticated USER (no admin) → 200 success envelope
- Current password incorrect → 400 `CURRENT_PASSWORD_INCORRECT`
- Blank fields → 400 `INVALID_INPUT`

### Mobile
- Cubit success / current-password error / duplicate submit
- Screen: confirmation mismatch, success clears session + login redirect, backend error mapping
- Privacy screen shows enabled change-password entry

---

## 11. Exact test results

### Backend
```
.\mvnw.cmd clean test
Tests run: 417, Failures: 0, Errors: 0, Skipped: 4
BUILD SUCCESS
```
(Exit code 0 — re-run after fixing register fixture passwords to satisfy shared policy.)

### Mobile (modified / related)
```
flutter test test/features/auth/presentation/change_password_cubit_test.dart \
  test/features/auth/presentation/change_password_screen_test.dart \
  test/features/profile/presentation/privacy_security_screen_test.dart

All tests passed! (9 tests)
```

---

## 12. Remaining risks

1. **No HTTP rate limit** on change-password (project gap).
2. **Redis ahead of DB on rollback:** existing `bumpTokenVersionAndSyncRedis` updates Redis inside the transaction before commit; same residual risk as logout-all/reset (not newly introduced).
3. **No `@Version` on User:** concurrency relies on hash mismatch after first write; rare double-success under extreme races is unlikely but not DB-locked.
4. **Client retry after timeout:** password may already be changed; retry with old password returns `CURRENT_PASSWORD_INCORRECT` — client should send user to login.
5. **Current access token** becomes invalid after `tokenVersion` bump; response still returns 200 — client must clear local session (mobile does).

---

## 13. Manual verification commands

```bash
# Login
curl -c cookies.txt -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"identifier\":\"user@example.com\",\"password\":\"CurrentPass1!\",\"deviceFingerprint\":\"manual-test-device-001\"}"

# Change password (use accessToken from login)
curl -b cookies.txt -X POST http://localhost:8080/api/v1/auth/change-password \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d "{\"currentPassword\":\"CurrentPass1!\",\"newPassword\":\"NewSecurePass2!\",\"confirmNewPassword\":\"NewSecurePass2!\"}"

# Expect: old access token rejected on /users/me; old password login fails; new password login succeeds
```

---

## 14. Edge-case deterministic behavior

| Case | Behavior |
|------|----------|
| New == current (after verify) | `NEW_PASSWORD_SAME_AS_CURRENT` 400 |
| Concurrent same old password | First succeeds; second `CURRENT_PASSWORD_INCORRECT` |
| DB OK, Redis sync fails | Password + DB tokenVersion committed; validator falls back to DB version |
| Redis OK, DB rolls back | Same residual risk as existing logout-all path |
| Access token invalid before response finishes | 200 still returned; client clears session |
| Multiple devices | All refresh rows + tokenVersion invalidate all |
| Inactive between auth and update | `USER_NOT_ACTIVE` 403 |
| Encoder upgrade on verify | N/A for change — password replaced entirely |
| Extremely long strings | DTO `@Size(max=50)` → `INVALID_INPUT` |
| Client retry after success | Old password fails; user must login with new |
| Audit failure after persist | Password change kept; audit error logged only |
| Refresh used during change | Revoked / version bump rejects reuse |

---

## 15. Final verdict

| Area | Verdict |
|------|---------|
| **API** | **READY** |
| **SESSION REVOCATION** | **READY** (refresh revoke + tokenVersion bump; same as logout-all/reset) |
| **MOBILE FLOW** | **READY** |
