# Restore a custom-format dump into an explicit non-production database.
# Usage:
#   $env:APP_ENV='local-prod-like'; ...
#   .\scripts\deployment\restore_postgres.ps1 -Dump path.dump -TargetDb exotic_stamp_restore_tmp -Confirm YES -CreateDb
param(
    [Parameter(Mandatory = $true)][string]$Dump,
    [Parameter(Mandatory = $true)][string]$TargetDb,
    [Parameter(Mandatory = $true)][string]$Confirm,
    [switch]$CreateDb
)

$ErrorActionPreference = "Stop"

function Require-Env([string]$Name) {
    $v = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($v)) { throw "Required: $Name" }
    return $v
}

$appEnv = Require-Env "APP_ENV"
$dbHost = Require-Env "DB_HOST"
$dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }
$dbUser = Require-Env "DB_USERNAME"
if (-not $env:PGPASSWORD -and $env:DB_PASSWORD) { $env:PGPASSWORD = $env:DB_PASSWORD }
if (-not $env:PGPASSWORD) { throw "Set PGPASSWORD or DB_PASSWORD" }

if ($Confirm -ne "YES") { throw "refuse restore without -Confirm YES" }

$prodLike = @("production", "prod", "exotic_stamp_prod", "exotic_stamp_production")
if (($appEnv -in @("production", "prod")) -or ($TargetDb -in $prodLike)) {
    if ($env:ALLOW_PRODUCTION_RESTORE -ne "YES_I_UNDERSTAND") {
        throw "production restore blocked. Set ALLOW_PRODUCTION_RESTORE=YES_I_UNDERSTAND only with authorization."
    }
}

if (-not (Test-Path $Dump)) { throw "dump not found: $Dump" }

$env:PGHOST = $dbHost
$env:PGPORT = $dbPort
$env:PGUSER = $dbUser

Write-Host "Restoring into target database '$TargetDb' (no DROP DATABASE)."
if ($CreateDb) {
    $exists = & psql -d postgres -v ON_ERROR_STOP=1 -t -A -c "SELECT 1 FROM pg_database WHERE datname='$TargetDb';"
    if (-not $exists) {
        & psql -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE `"$TargetDb`";"
    }
}

& pg_restore --no-owner --no-acl -d $TargetDb $Dump
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "RESTORE PASS → $TargetDb"
