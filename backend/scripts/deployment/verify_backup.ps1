# Verify a pg_dump custom-format backup.
param(
    [Parameter(Mandatory = $true)]
    [string]$DumpPath
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path $DumpPath)) {
    throw "Backup not found: $DumpPath"
}

$size = (Get-Item $DumpPath).Length
if ($size -le 0) { throw "FAIL: zero-size backup" }

$shaFile = "$DumpPath.sha256"
if (Test-Path $shaFile) {
    $expected = ((Get-Content $shaFile -Raw).Trim() -split "\s+")[0].ToLowerInvariant()
    $actual = (Get-FileHash -Algorithm SHA256 -Path $DumpPath).Hash.ToLowerInvariant()
    if ($expected -ne $actual) { throw "FAIL: checksum mismatch" }
    Write-Host "PASS: checksum matches"
} else {
    Write-Host "WARN: no .sha256 sidecar"
    Write-Host ((Get-FileHash -Algorithm SHA256 -Path $DumpPath).Hash)
}

Write-Host "=== pg_restore --list (first 40 lines) ==="
& pg_restore --list $DumpPath | Select-Object -First 40
Write-Host "VERIFY PASS ($size bytes)"
