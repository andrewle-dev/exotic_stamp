#!/usr/bin/env python3
"""Generate Batch B test inventory from src/test sources."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("src/test/java")


def classify(path: Path, text: str) -> tuple[str, bool, str, str, str]:
    name = path.stem
    suffix = "IT" if name.endswith("IT") else "Test"
    docker = bool(
        re.search(r"@Testcontainers|PostgreSQLContainer|GenericContainer", text)
    )
    spring_boot = "@SpringBootTest" in text
    webmvc = "@WebMvcTest" in text or "@WebFluxTest" in text
    datajpa = "@DataJpaTest" in text
    arch = "ArchUnit" in text or "architectur" in text.lower()
    migration = "Flyway" in name or "Migration" in name
    concurrency = "Concurrency" in name or "concurrent" in text.lower()
    security = "Security" in name or "UploadSecurity" in name or "Jwt" in name

    if docker and migration:
        cat = "Migration tests (Testcontainers)"
    elif docker and concurrency:
        cat = "Concurrency tests (Testcontainers)"
    elif docker and security and spring_boot:
        cat = "Security integration tests (Testcontainers)"
    elif docker:
        cat = "Testcontainers / integration"
    elif arch:
        cat = "Architecture tests"
    elif webmvc or datajpa:
        cat = "Spring slice tests"
    elif spring_boot and not docker:
        cat = "Spring Boot tests (non-Docker)"
    else:
        cat = "Unit tests"

    # Maven lifecycle
    if suffix == "IT" or name.endswith("IntegrationTest"):
        current = "failsafe (verify)"
        target = "failsafe (verify)"
        renamed = "unchanged" if suffix == "IT" else "needs *IT rename"
    else:
        current = "surefire (test)"
        target = "surefire (test)"
        renamed = "unchanged"

    reason = cat
    if docker and suffix == "IT":
        reason += "; Docker-backed IT"
    return cat, docker, current, target, renamed


def main() -> None:
    rows = []
    for path in sorted(ROOT.rglob("*.java")):
        text = path.read_text(encoding="utf-8", errors="replace")
        if not re.search(r"@(Test|ParameterizedTest|SpringBootTest|Testcontainers)", text):
            # still inventory if looks like a test class
            if not path.name.endswith(("Test.java", "IT.java", "Tests.java")):
                continue
        cat, docker, current, target, renamed = classify(path, text)
        rows.append(
            (
                str(path.relative_to(ROOT)).replace("\\", "/"),
                path.stem,
                "IT" if path.stem.endswith("IT") else "Test",
                cat,
                "yes" if docker else "no",
                current,
                target,
                renamed,
            )
        )

    print("| class | suffix | category | Docker | current lifecycle | target lifecycle | renamed |")
    print("|---|---|---|---|---|---|---|")
    for r in rows:
        print(f"| `{r[1]}` | {r[2]} | {r[3]} | {r[4]} | {r[5]} | {r[6]} | {r[7]} |")
    print(f"\nTotal classes: {len(rows)}")
    print(f"Docker: {sum(1 for r in rows if r[4]=='yes')}")
    print(f"IT suffix: {sum(1 for r in rows if r[2]=='IT')}")


if __name__ == "__main__":
    main()
