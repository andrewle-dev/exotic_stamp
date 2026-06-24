# Waiting Update Features

Tài liệu này dùng để ghi nhận các tính năng đang cân nhắc/đang thiếu để nâng cấp hệ thống.
Mục tiêu: giúp team có một danh sách rõ ràng để ưu tiên “đóng” các gap bảo mật và vận hành.

## How to use

Mỗi hạng mục nên bao gồm:
- **Motivation**: vì sao cần nâng cấp.
- **Current state**: hiện tại đang làm tới đâu.
- **Proposed approach**: hướng giải quyết (khái quát).
- **Acceptance criteria**: tiêu chí “xong”.
- **Risks / Open questions**: rủi ro, câu hỏi còn bỏ ngỏ.

---

## Waiting list

### 1. Revoke access token ngay (server-side invalidation)

**Motivation**  
Hiện tại việc “revoke” access token nếu chỉ dựa trên rotation của refresh token hoặc logic ở tầng phát hành thường không đủ để đảm bảo access token cũ bị từ chối ngay lập tức.
Trong mô hình banking/fintech, cần “server-side invalidation” để việc revoke là **thực sự enforced** ở pipeline xử lý request.

**Current state**  
- Đã triển khai: access JWT có `jti` + `tokenVersion` (khớp cột `users.token_version`); Redis denylist (`denylist:{jti}`) + cache `user:{id}:tokenVersion`; slot `auth:access_jti:{userId}:{deviceFp}` cho refresh; `JwtAuthFilter` gọi `AccessTokenRevocationValidator` (Redis → DB cache-aside → fail-open nếu đọc DB lỗi).
- Password reset / logout-all / reuse attack: tăng `token_version` + sync Redis. Refresh: denylist access `jti` cũ theo device. Logout (thiết bị): denylist `jti` từ header `Authorization` khi parse được.
- Token access phát hành trước khi deploy (không có `jti`/`tokenVersion`) sẽ không còn hợp lệ — client cần đăng nhập lại.

**Requirement**  
Nếu muốn policy “revoke access ngay khi refresh/logout/password reset”, thì cần cơ chế invalidate access token cũ trực tiếp trên server.

**Proposed approach (high level options)**  
Chọn 1 trong các hướng phổ biến sau:

1. **Denylist theo `jti` hoặc theo `tokenHash` của access token**  
   - Khi revoke/rotate, server ghi token id/hash vào denylist (Redis thường dùng).
   - `JwtAuthFilter` phải kiểm tra denylist cho từng request chứa access token.

2. **Token version / session id (token-versioning)**  
   - JWT access token mang thêm claim kiểu `tokenVersion` (hoặc `sessionId`).
   - Server lưu “version/session hiện hành” ở DB/Redis.
   - `JwtAuthFilter` so sánh claim với version đang active; nếu lệch thì từ chối.

3. **Very short TTL + rotation policy** (fallback / bổ trợ)  
   - Giảm `accessTokenTtl` để thu hẹp cửa sổ lộ token.
   - Đây thường là bổ sung, không thay thế fully enforced invalidation nếu yêu cầu revoke “ngay”.

**Acceptance criteria**
- Sau khi thực hiện `refresh`, `logout`, hoặc `password reset`, access token cũ ở device cùng/khác (theo policy) sẽ bị từ chối ngay lập tức trong request pipeline.
- Không chỉ “refresh token bị revoke”, mà **access token** cũng phải bị invalidated theo cơ chế server-side (denylist hoặc token-version).
- Multi-device behavior phải rõ ràng và được test (device-level vs user-level revoke).

**Risks / Open questions**
- Nên revoke theo phạm vi nào trong multi-device:
  - Theo `deviceFingerprint` (device-level) hay toàn bộ user (user-level)?
- Performance & cache:
  - Kiểm tra denylist/version cho mỗi request cần tối ưu để không tăng latency quá mức.
- Xử lý trường hợp denylist/Redis bị lỗi:
  - Fail-open hay fail-safe là gì cho yêu cầu bảo mật hiện tại?

---

## Future items (placeholders)

// DÙng pentest test lại endpoint API, 
// Tắt xem payload request response trên dev tool
// Dùng proxy nginx protect endpoint backend
// Tắt swagger trên production

### 2. [TBD]

### 3. [TBD]