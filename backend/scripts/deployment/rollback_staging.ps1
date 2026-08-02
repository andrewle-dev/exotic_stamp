# Rollback staging to a prior immutable image. Never reverses Flyway.
param(
    [switch]$DryRun,
    [Parameter(Mandatory = $true)][string]$Image,
    [string]$BaseUrl = "",
    [int]$TimeoutSec = 180
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $Root

$targetEnv = if ($env:TARGET_ENV) { $env:TARGET_ENV } else { "staging" }
if ($targetEnv -ne "staging") { throw "rollback script is staging-only" }

function Log([string]$msg) {
    $ts = (Get-Date).ToUniversalTime().ToString("HH:mm:ssZ")
    Write-Host "[rollback-staging $ts] $msg"
}

$art = Join-Path $Root "artifacts\deploy"
New-Item -ItemType Directory -Force -Path $art | Out-Null
$ts = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$logFile = Join-Path $art "rollback-staging-$ts.log"
$base = if ($BaseUrl) { $BaseUrl } elseif ($env:STAGING_BASE_URL) { $env:STAGING_BASE_URL } else { "" }

$lines = @()
function L([string]$m) { $script:lines += $m; Log $m }

try {
    L "targetEnv=staging dryRun=$DryRun image=$Image"
    L "NOTE: Flyway down-migrations are FORBIDDEN. App rollback only."
    L "1. verify prior image available"
    if ($DryRun) { L "DRY-RUN: would docker image inspect $Image" }
    else {
        docker image inspect $Image | Out-Null
        if ($LASTEXITCODE -ne 0) { docker pull $Image }
    }
    L "2. prior image must tolerate current additive schema"
    L "3. start prior image"
    if ($DryRun) { L "DRY-RUN: would start $Image" }
    else { L "Operator starts prior container on Lightsail (no AWS in F.0)" }
    L "4. wait readiness"
    if ($base -and -not $DryRun) {
        $deadline = (Get-Date).AddSeconds($TimeoutSec)
        do {
            try { Invoke-WebRequest -Uri "$base/actuator/health/readiness" -TimeoutSec 5 | Out-Null; break }
            catch { if ((Get-Date) -gt $deadline) { throw "readiness timeout" }; Start-Sleep 3 }
        } while ($true)
        L "readiness OK"
    } else { L "SKIP readiness wait" }
    L "5. smoke tests"
    if ($DryRun) { L "DRY-RUN: would run staging smoke" }
    elseif ($base) {
        $env:BASE_URL = $base
        & "$Root\scripts\smoke\staging_smoke.ps1"
    } else { L "SKIP smoke" }

    $evt = Join-Path $art "rollback-event-$ts.json"
    $builtAt = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    "{`"event`":`"rollback`",`"image`":`"$Image`",`"timestampUtc`":`"$builtAt`",`"flywayReversed`":false}" |
        Set-Content -Path $evt -Encoding utf8
    L "ROLLBACK EVENT RECORDED ($evt)"
} finally {
    $lines | Set-Content -Path $logFile -Encoding utf8
}
