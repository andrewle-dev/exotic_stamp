# Release Versioning Policy

**Batch:** F.0
**Status:** Binding for staging and production releases
**Immutable tags only — never overwrite a published semantic release tag.**

## 1. Application semantic version

Format: `MAJOR.MINOR.PATCH`

| Component | When to bump |
|-----------|--------------|
| MAJOR | Breaking API or data contract changes |
| MINOR | Backward-compatible features / additive Flyway migrations |
| PATCH | Backward-compatible fixes / ops hardening |

Current pre-production baseline for Batch F.0: **0.1.0**.

## 2. Pre-production tags

| Form | Example | Rules |
|------|---------|-------|
| Release candidate | v0.1.0-rc.1 | Immutable once pushed |
| Git tag | v0.1.0-rc.N | Points at exact commit SHA |
| Do not | latest as sole identity | Mutable pointers are not rollback sources |

Public production tags (v1.0.0+) require explicit authorization. Batch F.0 does not create a public production release tag.

## 3. Docker image tags

| Tag | Example | Purpose |
|-----|---------|---------|
| Commit-derived | git-ba6cb60 | Immutable rebuild identity |
| Semantic | v0.1.0-rc.1 | Human-readable release |
| Optional promotion | staging, prod | Pointer only — never sole rollback reference |

Prefer registry digests (sha256:…) for deploy and rollback.

## 4. Release manifest required fields

application, version, commitSha, shortSha, branch, buildTimestampUtc, javaVersion, mavenVersion, dockerImageRepository, dockerImageTags, dockerImageDigest, flywaySchemaVersion, configurationProfile, artifactChecksums, sourceDirty, releaseNotesPath.

Example: `infra/release/release-manifest.example.json`
Generator: `scripts/release/generate_release_manifest.{py,sh,ps1}`

## 5. Rollback identity

Rollback must reference image digest, commit-derived tag, or immutable semantic tag. Never reverse Flyway migrations.

## 6. Traceability

Every release: Git SHA, semantic/RC version, Docker tags, digest (or unresolved), Flyway version, UTC build timestamp.

## 7. Forbidden

Overwrite v* tags; ship only latest; embed secrets; claim release from dirty tree without non-release mode; edit applied Flyway migrations.
