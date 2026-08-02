#!/usr/bin/env bash
# Build immutable release image tags from repository root layout.
# Usage (from backend/):
#   ./scripts/release/build_image.sh --version 0.1.0-rc.1 [--allow-dirty] [--no-cache]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

ALLOW_DIRTY=0
NO_CACHE=0
VERSION=""
IMAGE_REPO="${DOCKER_IMAGE_REPOSITORY:-exotic-stamp-backend}"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --allow-dirty) ALLOW_DIRTY=1; shift ;;
    --no-cache) NO_CACHE=1; shift ;;
    --version) VERSION="$2"; shift 2 ;;
    --image-repo) IMAGE_REPO="$2"; shift 2 ;;
    -h|--help) sed -n '1,6p' "$0"; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; exit 2 ;;
  esac
done

if [[ -n "$(git status --porcelain)" && "$ALLOW_DIRTY" -ne 1 ]]; then
  echo "ERROR: dirty working tree. Use --allow-dirty for local prep only." >&2
  exit 1
fi

COMMIT_SHA="$(git rev-parse HEAD)"
SHORT_SHA="$(git rev-parse --short=7 HEAD)"
if [[ -z "$VERSION" ]]; then
  VERSION="0.1.0-rc.$(date -u +%Y%m%d%H%M)-${SHORT_SHA}"
fi
TAG_SEMVER="v${VERSION}"
TAG_GIT="git-${SHORT_SHA}"

# Parent directory is Docker context (Dockerfile copies backend/*).
PARENT="$(cd "$ROOT/.." && pwd)"
DOCKERFILE="$ROOT/Dockerfile"
if [[ ! -f "$DOCKERFILE" ]]; then
  echo "ERROR: Dockerfile not found at $DOCKERFILE" >&2
  exit 1
fi

# Ensure context-root dockerignore exists (secrets / .git / .m2).
IGNORE_SRC="$ROOT/.dockerignore"
IGNORE_DST="$PARENT/.dockerignore"
if [[ -f "$IGNORE_SRC" && ! -f "$IGNORE_DST" ]]; then
  cp "$IGNORE_SRC" "$IGNORE_DST"
  echo "Copied .dockerignore to context root: $IGNORE_DST"
fi

BUILD_ARGS=(build -f "$DOCKERFILE" -t "${IMAGE_REPO}:${TAG_SEMVER}" -t "${IMAGE_REPO}:${TAG_GIT}")
BUILD_ARGS+=(--label "org.opencontainers.image.revision=${COMMIT_SHA}")
BUILD_ARGS+=(--label "org.opencontainers.image.version=${VERSION}")
BUILD_ARGS+=(--label "org.opencontainers.image.title=ExoticStamp")
BUILD_ARGS+=(--label "exoticstamp.flyway.schema=23")
if [[ "$NO_CACHE" -eq 1 ]]; then
  BUILD_ARGS+=(--no-cache)
fi
BUILD_ARGS+=("$PARENT")

echo "Building ${IMAGE_REPO}:${TAG_SEMVER} and :${TAG_GIT} (context=$PARENT)"
# Never pass secrets as build-args.
docker "${BUILD_ARGS[@]}"

IMAGE_ID="$(docker image inspect "${IMAGE_REPO}:${TAG_GIT}" --format '{{.Id}}')"
DIGEST="$(docker image inspect "${IMAGE_REPO}:${TAG_GIT}" --format '{{index .RepoDigests 0}}' 2>/dev/null || true)"
if [[ -z "$DIGEST" || "$DIGEST" == "<no value>" ]]; then
  DIGEST="unresolved"
fi

mkdir -p "$ROOT/artifacts/release"
META="$ROOT/artifacts/release/image-build-${SHORT_SHA}.txt"
{
  echo "imageRepository=$IMAGE_REPO"
  echo "tags=${TAG_SEMVER},${TAG_GIT}"
  echo "imageId=$IMAGE_ID"
  echo "digest=$DIGEST"
  echo "commitSha=$COMMIT_SHA"
  echo "version=$VERSION"
  echo "builtAtUtc=$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
} > "$META"

echo "Wrote $META"
echo "IMAGE_ID=$IMAGE_ID"
echo "DIGEST=$DIGEST"
echo "TAGS=${IMAGE_REPO}:${TAG_SEMVER} ${IMAGE_REPO}:${TAG_GIT}"
