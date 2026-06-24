# CI Test Requirements — ExoticStamp Backend

This document defines the minimum continuous-integration test environment for release readiness. **MVP release readiness must not be claimed without Docker-backed integration tests passing in CI.**

## Docker requirement

CI runners **must** provide a working Docker daemon accessible to [Testcontainers](https://www.testcontainers.org/).

Symptoms when Docker is unavailable locally or in CI:

- Tests annotated with `@Testcontainers(disabledWithoutDocker = true)` are **skipped** (not failures).
- `UserStampCacheIntegrationIT` and `CollectionPersistenceIT` use `@Testcontainers` **without** `disabledWithoutDocker` — they **fail** if Docker is unavailable.

## Required integration / smoke tests

| Test class | Purpose |
|------------|---------|
| `FlywayV12MigrationIT` | Stage 0 integrity constraints migration |
| `FlywayV13MigrationIT` | Metro master-data constraints |
| `FlywayV14MigrationIT` | Campaign/stamp design migration |
| `FlywayV15MigrationIT` | Collection runtime migration |
| `FlywayV16MigrationIT` | Reward/voucher migration + permission seeds |
| `ExoticStampApplicationTests` | Full Spring context smoke (PostgreSQL + Redis) |
| `ProdSwaggerDisabledTest` | Prod profile disables Swagger/OpenAPI |
| `UploadSecurityTest` | Public vs protected upload paths |
| `CollectionPersistenceIT` | User stamp unique constraint / persistence |
| `UserStampCacheIntegrationIT` | Redis cache integration |
| `RewardStampCollectedFlowIT` | Stamp collected → reward flow |
| `RewardConcurrencyIT` | Reward issuance concurrency |

## Required unit / slice tests (always run)

- `mvn clean test` — includes `ArchitectureBoundaryTest`, auth/user/rbac/metro/collection/reward controller tests.
- Minimum bar: **0 failures**; document **skipped** count when Docker ITs are skipped.

## Example GitHub Actions job (documentation only)

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven
      - name: Run tests with Testcontainers
        run: mvn clean test
        env:
          JWT_SECRET: test-jwt-secret-must-be-at-least-256-bits-long-for-hmac-sha
```

Docker is pre-installed on `ubuntu-latest` GitHub-hosted runners; no extra service container is required for Testcontainers PostgreSQL/Redis.

## Local developer note

If `mvn clean test` reports skipped Docker-gated tests, run Docker Desktop (or compatible engine) and re-run:

```bash
mvn test -Dtest="FlywayV*MigrationIT,ExoticStampApplicationTests,ProdSwaggerDisabledTest,UploadSecurityTest"
```

## Release gate

| Gate | Requirement |
|------|-------------|
| Compile | `mvn clean package` BUILD SUCCESS |
| Unit/slice | 0 test failures |
| Docker ITs | 0 failures in CI (skips unacceptable for release branch) |
| Flyway | Empty-DB migrate V1–V16 in Flyway ITs |
