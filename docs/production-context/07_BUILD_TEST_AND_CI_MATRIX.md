# Build Test And CI Matrix

## Backend

| Item | Evidence |
|---|---|
| Clean install / verify | `mvn -B -ntp clean verify -Pci` |
| Unit / slice tests | Maven Surefire |
| Integration tests | Maven Failsafe `*IT` |
| Coverage | JaCoCo report/check under CI profile |
| Expected artifact | `target/ExoticStamp-*.jar` |
| CI job | `backend/.github/workflows/backend-ci.yml` |

### Command result

- Command run: `mvn -B -ntp clean verify -Pci`
- Result: **failed**
- Root cause found in Failsafe:
  - `metro.ExoticStamp.config.CiDockerAvailabilityIT`
  - failure message: CI requires Docker when `ci.require-docker=true`
- Failsafe summary:
  - completed: `61`
  - failures: `1`
  - errors: `0`
  - skipped: `59`

### Interpretation

- Compile succeeded
- Unit/slice test phase progressed
- Integration/CI gate failed due environment prerequisite, not a compile error

## Web Admin

| Command | Result |
|---|---|
| `npm run lint` | passed |
| `npm run typecheck` | passed |
| `npm run test` | passed |
| `npm run build` | passed with chunk-size warning |

### Test evidence

- Vitest:
  - files: `2`
  - tests: `7`
  - all passed

### Build evidence

- Vite production build succeeded
- Warning: output JS chunk above `500 kB`

## Mobile

| Command | Result |
|---|---|
| `flutter doctor -v` | passed |
| `flutter pub get` | passed |
| `flutter analyze` | passed |
| `flutter test` | passed |

### Notes

- `flutter pub get` reported many outdated packages within current constraints, but no resolution failure.
- Test suite logs include expected debug/fail-open messaging around package-info plugin absence in tests, but overall result was `All tests passed!`

## CI Coverage / Gaps

- Backend: GitHub Actions exists and is the strongest CI evidence in repo.
- Web admin: no committed GitHub Actions workflow found in current monorepo root for web build/test.
- Mobile: no GitHub Actions workflow found; `codemagic.yaml` exists, but no verified CI run evidence was collected.

## Missing CI Jobs

- Web admin dedicated CI build/test pipeline
- Mobile dedicated CI build/analyze/test pipeline in the active repo root
- End-to-end staging smoke for full admin web + backend integration
