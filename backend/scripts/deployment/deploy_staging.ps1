# Staging deploy preparation with dry-run and local simulation.
# Usage:
#   .\scripts\deployment\deploy_staging.ps1 -DryRun
#   .\scripts\deployment\deploy_staging.ps1 -LocalSimulate
param(
    [switch]$DryRun,
    [switch]$LocalSimulate,
    [string]$Image = "",
    [string]$BaseUrl = "",
    [int]$TimeoutSec = 180
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $Root

$targetEnv = if ($env:TARGET_ENV) { $env:TARGET_ENV } else { "staging" }
if ($targetEnv -ne "staging" -and -not $LocalSimulate) {
    throw "refuse non-staging target ($targetEnv)"
}
if ($targetEnv -in @("production", "prod")) {
    throw "production deploy is not supported by this script"
}

$appEnv = if ($env:APP_ENV) { $env:APP_ENV } else { "staging" }
if ($appEnv -in @("production", "prod")) {
    throw "APP_ENV must not be production for this script"
}

function Log([string]$msg) {
    $ts = (Get-Date).ToUniversalTime().ToString("HH:mm:ssZ")
    Write-Host "[deploy-staging $ts] $msg"
}

$art = Join-Path $Root "artifacts\deploy"
New-Item -ItemType Directory -Force -Path $art | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $Root "artifacts\release") | Out-Null
$ts = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$logFile = Join-Path $art "deploy-staging-$ts.log"

$imageRef = if ($Image) { $Image } elseif ($env:STAGING_IMAGE_REF) { $env:STAGING_IMAGE_REF } else { "exotic-stamp-backend:git-$((git rev-parse --short=7 HEAD).Trim())" }
$base = if ($BaseUrl) { $BaseUrl } elseif ($env:STAGING_BASE_URL) { $env:STAGING_BASE_URL } else { "" }
$prevFile = if ($env:PREV_VERSION_FILE) { $env:PREV_VERSION_FILE } else { Join-Path $Root "artifacts\release\current-staging-version.txt" }

$lines = @()
function L([string]$m) { $script:lines += $m; Log $m }

try {
    L "targetEnv=$targetEnv dryRun=$DryRun localSimulate=$LocalSimulate"
    L "1. validate env"
    L "2. record current version"
    if (Test-Path $prevFile) { L ("current=" + (Get-Content $prevFile -Raw).Trim()) } else { L "current=none" }
    L "3. database preflight"
    if ($DryRun) { L "DRY-RUN: would run run_db_preflight.ps1" }
    elseif ($env:DB_HOST) { & "$Root\scripts\deployment\run_db_preflight.ps1" }
    else { L "SKIP preflight (DB_HOST unset)" }
    L "4. database backup"
    if ($DryRun) { L "DRY-RUN: would run backup_postgres.ps1" }
    elseif ($env:DB_HOST) { & "$Root\scripts\deployment\backup_postgres.ps1" }
    else { L "SKIP backup (DB_HOST unset)" }
    L "5. pull/load immutable image imageRef=$imageRef"
    if ($DryRun) { L "DRY-RUN: would docker pull/load $imageRef" }
    elseif ($LocalSimulate) {
        docker image inspect $imageRef | Out-Null
        L "LOCAL: image present"
    } else {
        L "Operator must pull on Lightsail - F.0 stops before AWS mutation"
    }
    L "6. start new container (keep previous)"
    if ($DryRun) { L "DRY-RUN: would start container" }
    elseif ($LocalSimulate) { L "LOCAL: use compose recreate (operator)" }
    else { L "Remote start deferred (no AWS in F.0)" }
    L "7-8. wait liveness/readiness"
    if ($base -and -not $DryRun) {
        $deadline = (Get-Date).AddSeconds($TimeoutSec)
        do {
            try { Invoke-WebRequest -Uri "$base/actuator/health/liveness" -TimeoutSec 5 | Out-Null; break }
            catch { if ((Get-Date) -gt $deadline) { throw "liveness timeout" }; Start-Sleep 3 }
        } while ($true)
        do {
            try { Invoke-WebRequest -Uri "$base/actuator/health/readiness" -TimeoutSec 5 | Out-Null; break }
            catch { if ((Get-Date) -gt $deadline) { throw "readiness timeout" }; Start-Sleep 3 }
        } while ($true)
        L "health OK"
    } else { L "SKIP health wait" }
    L "9. smoke tests"
    if ($DryRun) { L "DRY-RUN: would run staging_smoke.ps1" }
    elseif ($base) {
        $env:BASE_URL = $base
        & "$Root\scripts\smoke\staging_smoke.ps1"
    } else { L "SKIP smoke" }
    L "10. record release manifest"
    if ($DryRun) { L "DRY-RUN: would generate release manifest" }
    else {
        $ver = if ($env:RELEASE_VERSION) { $env:RELEASE_VERSION } else { "0.1.0-rc.1" }
        python "$Root\scripts\release\generate_release_manifest.py" --allow-dirty --version $ver
    }
    Set-Content -Path $prevFile -Value $imageRef -Encoding utf8
    L "DEPLOY MARKED SUCCESS (gates as applicable)"
} finally {
    $lines | Set-Content -Path $logFile -Encoding utf8
    Log "log=$logFile"
}
