#!/usr/bin/env python3
"""Summarize JaCoCo XML coverage for Batch B reporting."""
from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def ratio(covered: int, missed: int) -> float:
    total = covered + missed
    return (covered / total) if total else 0.0


def counters(el: ET.Element) -> dict[str, tuple[int, int, float]]:
    out: dict[str, tuple[int, int, float]] = {}
    for c in el.findall("counter"):
        t = c.get("type")
        if t not in ("LINE", "BRANCH"):
            continue
        missed = int(c.get("missed", "0"))
        covered = int(c.get("covered", "0"))
        out[t] = (covered, missed + covered, ratio(covered, missed))
    return out


def main() -> int:
    path = Path(sys.argv[1] if len(sys.argv) > 1 else "target/site/jacoco/jacoco.xml")
    if not path.is_file():
        print(f"missing: {path}", file=sys.stderr)
        return 1
    root = ET.parse(path).getroot()
    overall = counters(root)
    for t, (cv, tot, r) in overall.items():
        print(f"OVERALL {t}: {cv}/{tot} = {r:.2%}")

    critical_prefixes = (
        "metro/ExoticStamp/modules/auth",
        "metro/ExoticStamp/modules/collection",
        "metro/ExoticStamp/modules/reward",
    )
    agg = {
        "auth": {"LINE": [0, 0], "BRANCH": [0, 0]},
        "collection": {"LINE": [0, 0], "BRANCH": [0, 0]},
        "reward": {"LINE": [0, 0], "BRANCH": [0, 0]},
    }
    print("\nPackages (auth/collection/reward):")
    for pkg in sorted(root.findall("package"), key=lambda p: p.get("name", "")):
        name = pkg.get("name", "")
        key = None
        for k in ("auth", "collection", "reward"):
            if f"modules/{k}" in name:
                key = k
                break
        if key is None:
            continue
        c = counters(pkg)
        line = c.get("LINE")
        branch = c.get("BRANCH")
        if line:
            print(
                f"  {name}: line={line[2]:.2%} ({line[0]}/{line[1]})"
                + (f" branch={branch[2]:.2%} ({branch[0]}/{branch[1]})" if branch else "")
            )
            agg[key]["LINE"][0] += line[0]
            agg[key]["LINE"][1] += line[1]
        if branch:
            agg[key]["BRANCH"][0] += branch[0]
            agg[key]["BRANCH"][1] += branch[1]

    print("\nCritical module aggregates:")
    for key, vals in agg.items():
        for t, (cv, tot) in vals.items():
            r = (cv / tot) if tot else 0.0
            print(f"  {key} {t}: {cv}/{tot} = {r:.2%}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
