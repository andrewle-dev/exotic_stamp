# Logical PostgreSQL backup (custom format). Passwords via PGPASSWORD only.
param(
    [string]$BackupDir = ""
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
Set-Location $Root

function Require-Env([string]$Name) {
    $v = [Environment]::GetEnvironmentVariable($Name)
    if ([string]::IsNullOrWhiteSpace($v)) { throw "Required: $Name" }
    return $v
}

$appEnv = Require-Env "APP_ENV"
$dbHost = Require-Env "DB_HOST"
$dbPort = if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }
$dbName = Require-Env "DB_NAME"
$dbUser = Require-Env "DB_USERNAME"
if (-not $env:PGPASSWORD -and $env:DB_PASSWORD) { $env:PGPASSWORD = $env:DB_PASSWORD }
if (-not $env:PGPASSWORD) { throw "Set PGPASSWORD or DB_PASSWORD" }

if ($appEnv -in @("production", "prod") -and $env:ALLOW_PRODUCTION_BACKUP -ne "YES") {
    throw "production backup requires ALLOW_PRODUCTION_BACKUP=YES"
}

$env:PGHOST = $dbHost
$env:PGPORT = $dbPort
$env:PGDATABASE = $dbName
$env:PGUSER = $dbUser

$schemaVer = "unknown"
try {
    $schemaVer = (& psql -X -q -t -A -c "SELECT COALESCE(MAX(version::int)::text,'unknown') FROM flyway_schema_history WHERE success AND version ~ '^[0-9]+$';").Trim()
} catch { }

$ts = (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssZ")
$outDir = if ($BackupDir) { $BackupDir } elseif ($env:BACKUP_DIR) { $env:BACKUP_DIR } else { Join-Path $Root "artifacts\backups" }
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$base = "${appEnv}_${dbName}_fw${schemaVer}_${ts}"
$dump = Join-Path $outDir "$base.dump"

Write-Host "Creating custom-format backup (credentials not on argv)..."
& pg_dump -Fc -f $dump
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$sha = (Get-FileHash -Algorithm SHA256 -Path $dump).Hash.ToLowerInvariant()
Set-Content -Path "$dump.sha256" -Value "$sha  $(Split-Path -Leaf $dump)" -Encoding utf8
$size = (Get-Item $dump).Length

Write-Host "BACKUP_PATH=$dump"
Write-Host "BACKUP_SHA256=$sha"
Write-Host "BACKUP_BYTES=$size"
Write-Host "FLYWAY_SCHEMA=$schemaVer"
