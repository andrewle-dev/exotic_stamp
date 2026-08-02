#!/usr/bin/env python3
"""Generate a deterministic Exotic Stamp release manifest (no secrets)."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
import sys
from datetime import datetime, timezone
from pathlib import Path


def repo_root() -> Path:
    return Path(__file__).resolve().parents[2]


def run(cmd: list[str], cwd: Path) -> str:
    p = subprocess.run(cmd, cwd=cwd, capture_output=True, text=True)
    if p.returncode != 0:
        raise RuntimeError(f"command failed ({p.returncode}): {' '.join(cmd)}\n{p.stderr}")
    return (p.stdout or "").strip()


def git_dirty(root: Path) -> bool:
    return bool(run(["git", "status", "--porcelain"], root))


def detect_flyway(root: Path) -> str:
    mig = root / "src" / "main" / "resources" / "db" / "migration"
    if not mig.is_dir():
        return "unknown"
    versions: list[int] = []
    for p in mig.glob("V*.sql"):
        m = re.match(r"V(\d+)__", p.name)
        if m:
            versions.append(int(m.group(1)))
    return str(max(versions)) if versions else "unknown"


def sha256_file(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def first_line(cmd: list[str]) -> str:
    try:
        p = subprocess.run(cmd, capture_output=True, text=True)
        out = (p.stdout or p.stderr or "").splitlines()
        return out[0].strip() if out else "unknown"
    except Exception:
        return "unknown"


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate release manifest JSON")
    parser.add_argument("--allow-dirty", action="store_true", help="Non-release prep mode")
    parser.add_argument("--version", default=None, help="Semantic/RC version (no leading v)")
    parser.add_argument(
        "--image-repo",
        default=os.environ.get("DOCKER_IMAGE_REPOSITORY", "exotic-stamp-backend"),
    )
    parser.add_argument(
        "--digest",
        default=os.environ.get("DOCKER_IMAGE_DIGEST", "unresolved"),
        help="Image digest or 'unresolved'",
    )
    parser.add_argument("--out", default=None, help="Output JSON path")
    parser.add_argument(
        "--profile",
        default=os.environ.get("CONFIGURATION_PROFILE", "prod"),
    )
    parser.add_argument(
        "--notes",
        default=os.environ.get(
            "RELEASE_NOTES_PATH", "docs/deployment/BATCH_F0_IMPLEMENTATION_REPORT.md"
        ),
    )
    args = parser.parse_args()

    root = repo_root()
    if not (root / ".git").exists() and not (root / ".git").is_file():
        # allow worktrees / nested
        try:
            run(["git", "rev-parse", "--is-inside-work-tree"], root)
        except Exception:
            print("ERROR: not a git repository", file=sys.stderr)
            return 1

    dirty = git_dirty(root)
    if dirty and not args.allow_dirty:
        print(
            "ERROR: working tree is dirty. Use --allow-dirty for non-release prep only.",
            file=sys.stderr,
        )
        return 1

    commit_sha = run(["git", "rev-parse", "HEAD"], root)
    short_sha = run(["git", "rev-parse", "--short=7", "HEAD"], root)
    try:
        branch = run(["git", "branch", "--show-current"], root) or "DETACHED"
    except Exception:
        branch = "DETACHED"

    build_ts = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    version = args.version or f"0.1.0-rc.{datetime.now(timezone.utc).strftime('%Y%m%d%H%M')}-{short_sha}"
    tag_semver = f"v{version}"
    tag_git = f"git-{short_sha}"

    jar_path = None
    jar_sha = "unresolved"
    jars = sorted((root / "target").glob("ExoticStamp-*.jar")) if (root / "target").is_dir() else []
    # Prefer non -sources / -javadoc
    jars = [j for j in jars if not j.name.endswith("-sources.jar") and not j.name.endswith("-javadoc.jar")]
    if jars:
        jar_path = jars[0]
        jar_sha = sha256_file(jar_path)

    out = Path(args.out) if args.out else root / "artifacts" / "release" / f"release-manifest-{short_sha}.json"
    out.parent.mkdir(parents=True, exist_ok=True)

    # Deterministic key order (insertion order preserved in modern Python).
    manifest = {
        "application": "ExoticStamp",
        "version": version,
        "commitSha": commit_sha,
        "shortSha": short_sha,
        "branch": branch,
        "buildTimestampUtc": build_ts,
        "javaVersion": first_line(["java", "-version"]),
        "mavenVersion": first_line(["mvn", "-version"]),
        "dockerImageRepository": args.image_repo,
        "dockerImageTags": [tag_semver, tag_git],
        "dockerImageDigest": args.digest,
        "flywaySchemaVersion": detect_flyway(root),
        "configurationProfile": args.profile,
        "artifactChecksums": {
            "jarSha256": jar_sha,
            "jarPath": str(jar_path.relative_to(root)).replace("\\", "/") if jar_path else None,
        },
        "sourceDirty": dirty,
        "releaseNotesPath": args.notes,
    }

    text = json.dumps(manifest, indent=2, ensure_ascii=False) + "\n"
    out.write_text(text, encoding="utf-8", newline="\n")
    man_sha = hashlib.sha256(text.encode("utf-8")).hexdigest()
    checksum_path = out.with_suffix(out.suffix + ".sha256")
    checksum_path.write_text(f"{man_sha}  {out.name}\n", encoding="utf-8", newline="\n")

    # Augment checksums with manifest hash (rewrite once for completeness).
    manifest["artifactChecksums"]["manifestSha256"] = man_sha
    text2 = json.dumps(manifest, indent=2, ensure_ascii=False) + "\n"
    out.write_text(text2, encoding="utf-8", newline="\n")
    man_sha2 = hashlib.sha256(text2.encode("utf-8")).hexdigest()
    checksum_path.write_text(f"{man_sha2}  {out.name}\n", encoding="utf-8", newline="\n")

    print(str(out))
    print(f"Manifest SHA-256: {man_sha2}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
