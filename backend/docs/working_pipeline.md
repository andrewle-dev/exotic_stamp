# WORKING PIPELINE - EXOTIC STAMP

> Pipeline chuẩn để phát triển Exotic/Metro Stamp backend, bám sát kiến trúc hiện tại trong repo.

---

## 1. Boundary của tài liệu này

- `working_pipeline.md` tập trung vào **cách triển khai theo từng bước**.
- `EXOTIC_STAMP_CONTEXT.md` tập trung vào **ngữ cảnh + kiến trúc tổng thể**.
- Có thể trùng một phần thông tin, nhưng quy ước là:
  - Quy trình thực thi, checklist, flow công việc đặt ở file này.
  - Giải thích sâu về kiến trúc/module/rule nền tảng đặt ở context file.

---

## 2. Architecture guardrails (quick reference)

- Pattern: Spring-pragmatic DDD.
- Dependency direction: `presentation -> application -> domain <- infrastructure`.
- Không import `JpaRepository` vào `application`/`domain`.
- Service write dùng `@Transactional`, query dùng `readOnly=true` khi phù hợp.
- Viết API bằng DTO, không lộ dữ liệu nhạy cảm.
- Viết flow có ghi dữ liệu thì phải có cache invalidation rõ ràng.

---

## 3. Requirement -> Scope

1. Xác định feature thuộc module nào: `metro`, `collection`, `reward`, `monetization`, `community`, `auth`, `user`, `rbac`.
2. Chốt rõ:
   - input/output API
   - business rules
   - anti-abuse/anti-cheat
   - metric cần theo dõi
3. Phân loại mức độ:
   - MVP core
   - Growth/monetization
   - Nice-to-have

---

## 4. Data-first design

1. Thiết kế bảng/constraint/index trước bằng Flyway migration mới (`V{n}__...sql`).
2. Bắt buộc có:
   - unique constraint cho invariant quan trọng
   - check constraint cho enum/state
   - index cho query hot path
3. Review migration theo production thinking:
   - lock impact
   - data volume
   - backward compatibility

---

## 5. Module implementation order

1. `domain`:
   - model
   - repository interface
   - domain exception / domain rule
2. `infrastructure`:
   - JPA repository
   - repository adapter
   - redis/cache adapter nếu cần
3. `application`:
   - command/query object
   - command service / query service
   - mapper
4. `presentation`:
   - controller
   - request/response DTO
   - validation annotation

---

## 6. Scan-to-stamp runtime pipeline (core nghiệp vụ)

1. App gửi scan request (NFC tag hoặc QR token + GPS/device metadata).
2. Backend resolve station theo hot-path key (`nfc_tag_id` / `qr_code_token`).
3. Validate điều kiện collect:
   - station active
   - campaign active (nếu có)
   - chưa collect trùng
4. Ghi `user_stamps`.
5. Trigger milestone evaluation:
   - nếu đạt mốc -> issue reward/voucher.
6. Trả response stamp + tiến độ + reward mới (nếu có).
7. Nếu có ad slot: log impression/click theo module monetization.

---

## 7. Reward pipeline

1. Tính số lượng stamp hợp lệ theo line/campaign.
2. So khớp milestone active.
3. Kiểm tra user đã nhận milestone chưa (`uq_user_rewards_once`).
4. Nếu reward type là voucher:
   - lấy mã từ `voucher_pool` theo cơ chế lock an toàn.
5. Tạo `user_rewards` + notification cho user.

---

## 8. Monetization pipeline

1. Chọn ad/banner hợp lệ theo thời gian + trạng thái.
2. Trả creative cho client theo ngữ cảnh (pre-stamp/home swiper/event).
3. Track:
   - ad impression
   - ad click
   - affiliate click
4. Batch aggregate counter về bảng tổng (`total_impressions`, `total_clicks`).

---

## 9. Community/Growth pipeline

1. Sau register: cấp `referral_code` cho user.
2. Khi user mới nhập referral:
   - validate code
   - tạo quan hệ referral pending
3. Khi user referred hoàn thành điều kiện (vd: verify email):
   - cập nhật referral completed/rewarded
4. Track share events theo platform.
5. Push notification và hiển thị inbox trong app.

---

## 10. Test pipeline

1. Unit test cho domain rule, command/query service.
2. Integration test cho repository + migration + security filter.
3. API test cho auth/collect/reward flows:
   - happy path
   - duplicate scan
   - invalid token
   - expired campaign
4. Load test cho hot path:
   - scan stamping
   - ad impression ingest

---

## 11. Release pipeline

1. Merge theo module nhỏ, tránh PR quá lớn.
2. Chạy compile/test trước merge.
3. Apply migration theo thứ tự version.
4. Smoke test các flow chính sau deploy:
   - auth login/refresh
   - scan collect stamp
   - reward issue
   - ad tracking
5. Theo dõi logs + metrics 24h đầu sau release.

---

## 12. Working agreements

- Không viết business logic trong controller.
- Không import JPA trực tiếp vào layer application/domain.
- Không hardcode rule/TTL quan trọng.
- Feature nào có write data thì phải có chiến lược consistency và rollback rõ ràng.
- Ưu tiên thiết kế để scale từ đầu cho các bảng high-volume (`user_stamps`, `ad_impressions`, `affiliate_banner_clicks`).
- Nếu rule thay đổi kiến trúc/module-level, cập nhật thêm tại `EXOTIC_STAMP_CONTEXT.md`.
