# Read-only database preflight for staging/local launch gates.
# Usage:
#   $env:DB_HOST='localhost'; $env:DB_PORT='5432'; $env:DB_NAME='exotic_stamp'
#   $env:DB_USERNAME='exotic_app'; $env:PGPASSWORD='...'
#   .\scripts\deployment\run_db_preflight.ps1
param(
    [switch]$Strict = $true
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $Root

function Require-Env([string]$Name) {
    $v = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($v)) {
        throw "Required environment variable missing: $Name"
    }
    return $v
}

$dbHost = Require-Env "DB_HOST"
$dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }
$dbName = Require-Env "DB_NAME"
$dbUser = Require-Env "DB_USERNAME"
if (-not $env:PGPASSWORD -and $env:DB_PASSWORD) {
    $env:PGPASSWORD = $env:DB_PASSWORD
}
if (-not $env:PGPASSWORD) {
    throw "Set PGPASSWORD or DB_PASSWORD (never pass on argv)"
}

$env:PGHOST = $dbHost
$env:PGPORT = $dbPort
$env:PGDATABASE = $dbName
$env:PGUSER = $dbUser

$art = Join-Path $Root "artifacts\preflight"
New-Item -ItemType Directory -Force -Path $art | Out-Null
$ts = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$report = Join-Path $art "db-preflight-$dbName-$ts.txt"
$fail = 0

function Redact([string]$text) {
    $t = $text
    $t = [regex]::Replace($t, '(?i)(password=)[^&\s]+', '${1}***')
    $t = [regex]::Replace($t, 'jdbc:postgresql://\S+', 'jdbc:postgresql://***')
    return $t
}

function Invoke-PsqlRo([string]$sql) {
    $out = & psql -v ON_ERROR_STOP=1 -X -q -c "SET default_transaction_read_only = on;" -c $sql 2>&1 | Out-String
    return (Redact $out)
}

$lines = @()
$lines += "=== Exotic Stamp DB preflight (READ-ONLY) ==="
$lines += "timestampUtc=$ts"
$lines += "host=$dbHost port=$dbPort db=$dbName user=$dbUser"
$lines += "jdbc=jdbc:postgresql://***/$dbName (credentials redacted)"
$lines += ""

try {
    $lines += "=== PostgreSQL version ==="
    $lines += (Invoke-PsqlRo "SELECT version();")
} catch {
    $lines += "FAIL: cannot connect/query"
    $fail = 1
}

$lines += "=== Flyway schema history ==="
try {
    $lines += (Invoke-PsqlRo "SELECT version, description, success, installed_rank, installed_on FROM flyway_schema_history ORDER BY installed_rank;")
} catch {
    $lines += "WARN: flyway_schema_history missing"
    $fail = 1
}

$lines += "=== Failed Flyway rows ==="
$failOut = ""
try {
    $failOut = Invoke-PsqlRo "SELECT version, description, success FROM flyway_schema_history WHERE success = false;"
    $lines += $failOut
    if ($failOut -match '(?m)^\s*\d+') {
        $lines += "LAUNCH-BLOCKING: failed Flyway rows present"
        $fail = 1
    }
} catch { }

$lines += "=== Current schema version ==="
try {
    $lines += (Invoke-PsqlRo "SELECT MAX(version::int) AS current_version FROM flyway_schema_history WHERE success = true AND version ~ '^[0-9]+$';")
} catch { }

$preSql = Join-Path $Root "docs\deployment\BATCH_E_DATA_PREFLIGHT.sql"
$lines += "=== Batch E data preflight SQL ==="
if (Test-Path $preSql) {
    $out = & psql -v ON_ERROR_STOP=1 -X -q -c "SET default_transaction_read_only = on;" -f $preSql 2>&1 | Out-String
    $out = Redact $out
    $lines += $out
    if ($Strict -and ($out -match 'cnt\s+\|\s*[2-9]|cnt\s+\|\s*[1-9][0-9]')) {
        $lines += "LAUNCH-BLOCKING: duplicate/integrity cnt rows detected (review report)"
        $fail = 1
    }
} else {
    $lines += "ERROR: missing BATCH_E_DATA_PREFLIGHT.sql"
    $fail = 1
}

$lines += "=== stored_assets ==="
try {
    $lines += (Invoke-PsqlRo "SELECT status, COUNT(*) FROM stored_assets GROUP BY status ORDER BY 1;")
} catch {
    $lines += "(stored_assets absent or unreadable)"
}

$lines | Set-Content -Path $report -Encoding utf8
Write-Host "Report: $report"
if ($fail -ne 0) {
    Write-Host "PREFLIGHT FAIL"
    exit 1
}
Write-Host "PREFLIGHT PASS"
exit 0
