# Exotic Stamp Mobile Design Reference

> Canonical design reference for the Flutter mobile app. This folder is used by AI agents and developers to implement UI screens consistently from the exported Visily design.

---

## 1. Purpose

This folder stores the user-facing mobile UI references exported from Visily.

Do **not** treat Visily-generated code as production source code. Visily output is a visual reference only. Flutter source must be implemented manually using the project architecture in `working_pipeline_flutter.md`.

---

## 2. Design Source Location

Canonical structure (normalized):

```text
mobile/docs/design/
├── README.md
├── UI_SCREEN_INVENTORY.md
└── visily/
    ├── exotic-stamp-multiscreens.pdf
    ├── visily-home.png
    ├── visily-stamp-book.png
    ├── visily-scan.png
    ├── visily-stations-list.png
    ├── visily-station-detail.png
    ├── visily-rewards.png
    ├── visily-profile.png
    ├── visily-location-verification.png
    ├── visily-stamp-collected-success.png
    ├── visily-scan-error.png
    ├── visily-stamp-detail.png
    ├── visily-voucher-detail.png
    ├── visily-photo-share.png
    ├── visily-tap-to-collect.png
    ├── visily-collect-&-share-rewards.png
    └── visily-welcome.png
```

All screen PNGs live **directly** under `docs/design/visily/`. Do not add nested export folders (for example `visily/exotic-stamp-multiscreens/`).

Design file path pattern for agents:

```text
docs/design/visily/{filename}
```

---

## 3. Brand Tokens

Use these exact HEX colors in Flutter. Do not create random blue/red variants per screen.

| Token | HEX | Usage |
|---|---:|---|
| Primary Blue | `#01599D` | Main brand color, selected tabs, active states, links, progress bars |
| Accent Red | `#E83B28` | Scan/collect CTA, destructive actions, important warning/action emphasis |
| Background White | `#FFFFFF` | Main page background |
| Text Primary | `#1D2433` | Primary text |
| Text Secondary | `#667085` | Supporting text |
| Border | `#E4E7EC` | Card/input borders |
| Surface | `#F8FAFC` | Subtle card background |

### Brand color enforcement

- **Primary Blue must be `#01599D`.** Do not use Visily-exported blues or ad-hoc variants.
- **Accent Red must be `#E83B28`.**
- **Background White must be `#FFFFFF`.**
- The current Flutter `AppColors.brandBlue` value `#09599E` is **wrong** and must be corrected in **M0 Foundation** before feature UI work.
- Prefer token names `primaryBlue`, `accentRed`, `backgroundWhite` (see `UI_SCREEN_INVENTORY.md`). Deprecate `brandBlue` / `brandRed` during M0.

Flutter token target:

```dart
class AppColors {
  static const primaryBlue = Color(0xFF01599D);
  static const accentRed = Color(0xFFE83B28);
  static const backgroundWhite = Color(0xFFFFFFFF);

  static const textPrimary = Color(0xFF1D2433);
  static const textSecondary = Color(0xFF667085);
  static const border = Color(0xFFE4E7EC);
  static const surface = Color(0xFFF8FAFC);
}
```

---

## 4. Product Direction — NFC-First (Strict)

Exotic Stamp is **NFC-first**. QR is **fallback only**.

### Required behavior

| Rule | Requirement |
|------|-------------|
| Default scan mode | **NFC** — never QR-first |
| Primary UI copy | **"Chạm NFC"** / NFC tap language |
| QR copy & placement | Secondary; visible only as fallback when NFC unavailable or user explicitly switches |
| Scan screen layout | NFC instruction and tap affordance must dominate; QR must not be the default tab or hero |
| Implementation | Do **not** implement scan as QR-first (current prototype violates this and must be fixed in M4) |

Correct priority:

```text
NFC tap at station
→ GPS verification
→ backend validation
→ pre-stamp sponsor/ad if configured
→ stamp collection success
→ milestone/reward update
→ share memory
```

QR is only for devices or stations where NFC fails. Do not let the Flutter UI become QR-first.

---

## 5. Implementation Rules

### Do

- Implement Flutter screens from `UI_SCREEN_INVENTORY.md`.
- Open design references from `docs/design/visily/{filename}`.
- Use reusable widgets from `lib/shared/widgets/`.
- Use tokens from `lib/app/theme/`.
- Keep screen state backend-driven.
- Add loading / empty / error states per feature.
- Keep NFC/GPS behavior explicit in scan flow.
- Default scan flow to NFC; offer QR only as fallback.

### Do not

- Do not paste Visily exported code into `lib/`.
- Do not hardcode colors in screens.
- Do not use `#09599E` or other off-brand blues.
- Do not fake successful collection locally.
- Do not unlock rewards locally.
- Do not mark vouchers as used locally without backend confirmation.
- Do not create business logic in widgets.
- Do not implement scan as QR-first.

---

## 6. Design Review Checklist

Before implementing a screen, verify:

- [ ] Screen exists in `UI_SCREEN_INVENTORY.md`.
- [ ] PNG exists at `docs/design/visily/{filename}`.
- [ ] Route is defined.
- [ ] Feature folder is defined.
- [ ] API dependency is known or marked `BACKEND_CONTRACT_REQUIRED`.
- [ ] Loading state exists.
- [ ] Empty state exists where relevant.
- [ ] Error state exists.
- [ ] Colors use design tokens (`#01599D`, `#E83B28`, `#FFFFFF`).
- [ ] NFC-first language is preserved ("Chạm NFC").
- [ ] QR is not visually dominant unless explicitly a fallback.
- [ ] Default scan mode is NFC, not QR.

---

## 7. AI Agent Usage

When asking an AI agent to implement Flutter UI, provide this context:

```text
Read these files first:
1. mobile/docs/design/README.md
2. mobile/docs/design/UI_SCREEN_INVENTORY.md
3. mobile/docs/working_pipeline_flutter.md
4. mobile/docs/api/MOBILE_API_CONTRACT.md if API integration is required

Design PNGs: docs/design/visily/
NFC-first: default scan mode NFC; QR fallback only; primary copy "Chạm NFC".
Brand colors: Primary Blue #01599D, Accent Red #E83B28, Background #FFFFFF.

Do not implement from screenshots alone. Follow the screen inventory, route map, state model, design tokens, and Flutter architecture rules.
```
