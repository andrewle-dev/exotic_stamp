# GitHub Branch Protection Checklist

**Repository:** `itdept-studio/EXOTIC_STAMP` (remote `exotic`)  
**Default branch:** `main`  
**CI workflow:** `.github/workflows/backend-ci.yml` (job name: `Maven verify (CI profile)` / check context typically `backend-ci` / `verify`)  
**Date:** 2026-07-25  
**Scope:** Checklist only — do **not** apply live GitHub settings unless explicitly authorized.

## Required settings (apply on `main`)

| # | Setting | Recommended value | Why |
|---|---------|-------------------|-----|
| 1 | Default branch | `main` | Matches workflow push filter and remote HEAD |
| 2 | Require a pull request before merging | Enabled | No direct merge of unreviewed work |
| 3 | Require status checks to pass | Enabled | Gate on green CI |
| 4 | Required status check | `backend-ci` / `Maven verify (CI profile)` | Must match Actions check name after first green run |
| 5 | Require branches to be up to date | Enabled | Prevent merging stale PRs past a broken main |
| 6 | Prevent force pushes | Enabled (incl. admins if policy allows) | Protect history / release tags |
| 7 | Prevent deletions | Enabled | Protect default branch |
| 8 | Restrict who can push | Admins / release managers only | No casual direct push |
| 9 | Required approving reviewers | ≥ 1 (prefer 2 for production hotfixes) | Human review before merge |
| 10 | Dismiss stale reviews | Enabled | Re-review after new commits |
| 11 | Optional: signed commits | Org policy | Integrity of authorship |
| 12 | No production deploy from unreviewed PR | Process + separate deploy workflow | `backend-ci` must not deploy |

## Explicit non-goals of `backend-ci`

- Does **not** deploy to staging/production
- Does **not** use production secrets
- Runs `mvn -B -ntp clean verify -Pci` + Surefire/Failsafe assert + JaCoCo diagnostics

## Operator verification commands

```bash
gh api repos/itdept-studio/EXOTIC_STAMP --jq .default_branch
gh api repos/itdept-studio/EXOTIC_STAMP/branches/main/protection
gh run list --repo itdept-studio/EXOTIC_STAMP --workflow=backend-ci.yml --limit 5
```

## Status

**Not applied in Batch E.2 / F.0** — documentation only pending explicit authorization.

## Batch F.0 CI workflow notes

- PRs targeting `main`/`master` trigger `backend-ci`.
- Pushes to `main`/`master` and `chore/**` / `release/**` / `hotfix/**` trigger CI.
- Uploads Surefire, Failsafe, JaCoCo, and `artifacts/ci` diagnostics.
- Does not deploy; does not use production secrets.
- Remote green run still **unverified** when `gh`/Actions access unavailable.
