#!/usr/bin/env python3
"""Report JaCoCo critical package match counts and coverage ratios."""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

GROUPS = {
    "auth": "metro/ExoticStamp/modules/auth",
    "collection": "metro/ExoticStamp/modules/collection",
    "reward": "metro/ExoticStamp/modules/reward",
}


def main() -> int:
    path = Path(sys.argv[1] if len(sys.argv) > 1 else "target/site/jacoco/jacoco.xml")
    if not path.is_file():
        print(f"missing: {path}", file=sys.stderr)
        return 1
    root = ET.parse(path).getroot()
    problems = []
    for name, prefix in GROUPS.items():
        classes = 0
        line_c = line_m = branch_c = branch_m = instr_c = instr_m = 0
        for pkg in root.findall("package"):
            pname = pkg.get("name", "")
            if not pname.startswith(prefix):
                continue
            for cls in pkg.findall("class"):
                classes += 1
            for c in pkg.findall("counter"):
                t = c.get("type")
                missed = int(c.get("missed", "0"))
                covered = int(c.get("covered", "0"))
                if t == "LINE":
                    line_m += missed
                    line_c += covered
                elif t == "BRANCH":
                    branch_m += missed
                    branch_c += covered
                elif t == "INSTRUCTION":
                    instr_m += missed
                    instr_c += covered
        line_t = line_c + line_m
        branch_t = branch_c + branch_m
        instr_t = instr_c + instr_m
        line_r = (line_c / line_t) if line_t else 0.0
        branch_r = (branch_c / branch_t) if branch_t else 0.0
        print(
            f"{name}: classes={classes} instructions={instr_c}/{instr_t} "
            f"lines={line_c}/{line_t} ({line_r:.2%}) "
            f"branches={branch_c}/{branch_t} ({branch_r:.2%})"
        )
        if classes == 0:
            problems.append(f"critical group matched zero classes: {name}")
    if problems:
        for p in problems:
            print(f"FAIL: {p}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
