# Build immutable release image tags.
# Usage:
#   .\scripts\release\build_image.ps1 -Version 0.1.0-rc.1 -AllowDirty
param(
    [switch]$AllowDirty,
    [switch]$NoCache,
    [string]$Version = "",
    [string]$ImageRepo = $(if ($env:DOCKER_IMAGE_REPOSITORY) { $env:DOCKER_IMAGE_REPOSITORY } else { "exotic-stamp-backend" })
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $Root

$dirty = git status --porcelain
if ($dirty -and -not $AllowDirty) {
    Write-Error "dirty working tree. Use -AllowDirty for local prep only."
    exit 1
}

$CommitSha = (git rev-parse HEAD).Trim()
$ShortSha = (git rev-parse --short=7 HEAD).Trim()
if (-not $Version) {
    $Version = "0.1.0-rc." + (Get-Date).ToUniversalTime().ToString("yyyyMMddHHmm") + "-" + $ShortSha
}
$TagSemver = "v$Version"
$TagGit = "git-$ShortSha"

$Parent = (Resolve-Path (Join-Path $Root "..")).Path
$Dockerfile = Join-Path $Root "Dockerfile"
if (-not (Test-Path $Dockerfile)) {
    Write-Error "Dockerfile not found: $Dockerfile"
    exit 1
}

$ignoreSrc = Join-Path $Root ".dockerignore"
$ignoreDst = Join-Path $Parent ".dockerignore"
if ((Test-Path $ignoreSrc) -and -not (Test-Path $ignoreDst)) {
    Copy-Item $ignoreSrc $ignoreDst
    Write-Host "Copied .dockerignore to context root: $ignoreDst"
}

$buildArgs = @(
    "build", "-f", $Dockerfile,
    "-t", "${ImageRepo}:${TagSemver}",
    "-t", "${ImageRepo}:${TagGit}",
    "--label", "org.opencontainers.image.revision=$CommitSha",
    "--label", "org.opencontainers.image.version=$Version",
    "--label", "org.opencontainers.image.title=ExoticStamp",
    "--label", "exoticstamp.flyway.schema=23"
)
if ($NoCache) { $buildArgs += "--no-cache" }
$buildArgs += $Parent

Write-Host "Building ${ImageRepo}:${TagSemver} and :${TagGit} (context=$Parent)"
# Never pass secrets as build-args.
& docker @buildArgs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$ImageId = (docker image inspect "${ImageRepo}:${TagGit}" --format "{{.Id}}").Trim()
$Digest = "unresolved"
try {
    $d = (docker image inspect "${ImageRepo}:${TagGit}" --format "{{index .RepoDigests 0}}").Trim()
    if ($d -and $d -ne "<no value>") { $Digest = $d }
} catch { }

$art = Join-Path $Root "artifacts\release"
New-Item -ItemType Directory -Force -Path $art | Out-Null
$meta = Join-Path $art "image-build-$ShortSha.txt"
$builtAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
@"
imageRepository=$ImageRepo
tags=$TagSemver,$TagGit
imageId=$ImageId
digest=$Digest
commitSha=$CommitSha
version=$Version
builtAtUtc=$builtAt
"@ | Set-Content -Path $meta -Encoding utf8

Write-Host "Wrote $meta"
Write-Host "IMAGE_ID=$ImageId"
Write-Host "DIGEST=$Digest"
Write-Host "TAGS=${ImageRepo}:${TagSemver} ${ImageRepo}:${TagGit}"
