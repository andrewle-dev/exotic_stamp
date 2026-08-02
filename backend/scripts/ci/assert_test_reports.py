#!/usr/bin/env python3
"""Assert Surefire/Failsafe results for Exotic Stamp backend CI (Batch B / B.1).

Fails when:
- Failsafe ran zero tests
- any failures/errors exist
- Docker/Testcontainers-required ITs were skipped because Docker was unavailable
- expected IT classes from the manifest are missing or skipped (under --strict / CI)

Usage:
  python scripts/ci/assert_test_reports.py
  python scripts/ci/assert_test_reports.py --strict
"""

from __future__ import annotations

import argparse
import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

DOCKER_SKIP_MARKERS = (
    "could not find a valid docker environment",
    "docker environment",
    "disabledwithoutdocker",
    "docker is required",
    "ci requires docker",
    "testcontainers",
)

DEFAULT_MANIFEST = Path("scripts/ci/expected_integration_tests.txt")


def load_manifest(path: Path) -> list[str]:
    if not path.is_file():
        return []
    classes: list[str] = []
    for line in path.read_text(encoding="utf-8").splitlines():
        text = line.strip()
        if not text or text.startswith("#"):
            continue
        classes.append(text)
    return classes


def parse_suite(path: Path) -> dict:
    root = ET.parse(path).getroot()
    name = root.attrib.get("name", path.stem)
    tests = int(float(root.attrib.get("tests", "0")))
    failures = int(float(root.attrib.get("failures", "0")))
    errors = int(float(root.attrib.get("errors", "0")))
    skipped = int(float(root.attrib.get("skipped", root.attrib.get("skips", "0"))))
    cases: list[dict] = []
    skipped_cases: list[dict] = []
    for tc in root.findall("testcase"):
        classname = tc.attrib.get("classname", name)
        case_name = tc.attrib.get("name", "")
        skip = tc.find("skipped")
        fail = tc.find("failure")
        err = tc.find("error")
        status = "executed"
        message = ""
        if skip is not None:
            status = "skipped"
            message = ((skip.attrib.get("message") or "") + " " + (skip.text or "")).strip()
            skipped_cases.append(
                {"classname": classname, "name": case_name, "message": message}
            )
        elif fail is not None or err is not None:
            status = "failed"
        cases.append(
            {
                "classname": classname,
                "name": case_name,
                "status": status,
                "message": message,
            }
        )
    return {
        "file": str(path),
        "name": name,
        "tests": tests,
        "failures": failures,
        "errors": errors,
        "skipped": skipped,
        "cases": cases,
        "skipped_cases": skipped_cases,
    }


def load_reports(directory: Path) -> list[dict]:
    if not directory.is_dir():
        return []
    return [parse_suite(path) for path in sorted(directory.glob("TEST-*.xml"))]


def summarize(suites: list[dict]) -> dict:
    return {
        "tests": sum(s["tests"] for s in suites),
        "failures": sum(s["failures"] for s in suites),
        "errors": sum(s["errors"] for s in suites),
        "skipped": sum(s["skipped"] for s in suites),
        "classnames": {s["name"] for s in suites},
    }


def is_docker_skip(message: str) -> bool:
    lower = message.lower()
    return any(marker in lower for marker in DOCKER_SKIP_MARKERS)


def classify_expected(expected: str, failsafe: list[dict]) -> str:
    """Return executed | skipped | missing for an expected classname."""
    short = expected.split(".")[-1]
    saw_executed = False
    saw_skipped = False
    saw_any = False
    for suite in failsafe:
        for case in suite["cases"]:
            cn = case["classname"] or ""
            if expected == cn or short == cn.split(".")[-1] or expected in cn or short in cn:
                saw_any = True
                if case["status"] == "executed":
                    saw_executed = True
                elif case["status"] == "skipped":
                    saw_skipped = True
        if expected == suite["name"] or short == suite["name"].split(".")[-1]:
            saw_any = True
            if suite["tests"] > 0 and suite["skipped"] == suite["tests"]:
                saw_skipped = True
            elif suite["tests"] > suite["skipped"]:
                saw_executed = True
    if saw_executed:
        return "executed"
    if saw_skipped:
        return "skipped"
    if saw_any:
        return "skipped"
    return "missing"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--surefire-dir", default="target/surefire-reports")
    parser.add_argument("--failsafe-dir", default="target/failsafe-reports")
    parser.add_argument(
        "--manifest",
        default=str(DEFAULT_MANIFEST),
        help="Expected IT classname list (one per line)",
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Fail when expected ITs are skipped/missing (default when ci.require-docker=true)",
    )
    args = parser.parse_args()

    strict = args.strict or os.environ.get("ci.require-docker", "").lower() in (
        "true",
        "1",
        "yes",
    )

    surefire = load_reports(Path(args.surefire_dir))
    failsafe = load_reports(Path(args.failsafe_dir))
    s_sum = summarize(surefire)
    f_sum = summarize(failsafe)
    expected = load_manifest(Path(args.manifest))

    print("=== Surefire ===")
    print(
        f"tests={s_sum['tests']} failures={s_sum['failures']} "
        f"errors={s_sum['errors']} skipped={s_sum['skipped']} suites={len(surefire)}"
    )
    print("=== Failsafe ===")
    print(
        f"tests={f_sum['tests']} failures={f_sum['failures']} "
        f"errors={f_sum['errors']} skipped={f_sum['skipped']} suites={len(failsafe)}"
    )

    problems: list[str] = []

    if not failsafe:
        problems.append(
            f"No Failsafe reports found under {args.failsafe_dir}. "
            "Integration tests did not run (expected *IT via maven-failsafe-plugin)."
        )
    if f_sum["tests"] == 0:
        problems.append("Failsafe integration-test count is zero.")
    if s_sum["failures"] or s_sum["errors"]:
        problems.append(
            f"Surefire has failures={s_sum['failures']} errors={s_sum['errors']}."
        )
    if f_sum["failures"] or f_sum["errors"]:
        problems.append(
            f"Failsafe has failures={f_sum['failures']} errors={f_sum['errors']}."
        )

    for suite in failsafe:
        for case in suite["skipped_cases"]:
            if is_docker_skip(case["message"]):
                problems.append(
                    "Docker/Testcontainers IT skipped because Docker was unavailable: "
                    f"{case['classname']}#{case['name']}"
                )

    print("=== Expected IT manifest ===")
    if not expected:
        problems.append(f"Expected IT manifest empty or missing: {args.manifest}")
    categories = {"executed": [], "skipped": [], "missing": []}
    for cls in expected:
        status = classify_expected(cls, failsafe)
        categories[status].append(cls)
        print(f"  [{status}] {cls}")
        if strict and status != "executed":
            problems.append(f"Expected IT not executed ({status}): {cls}")
        elif not strict and status == "missing":
            problems.append(f"Expected IT class not discovered: {cls}")

    print(
        "=== Manifest summary === "
        f"executed={len(categories['executed'])} "
        f"skipped={len(categories['skipped'])} "
        f"missing={len(categories['missing'])} strict={strict}"
    )

    if problems:
        print("=== ASSERTION FAILURES ===")
        for problem in problems:
            print(f"- {problem}")
        return 1

    print("=== ASSERTION OK ===")
    return 0


if __name__ == "__main__":
    sys.exit(main())
