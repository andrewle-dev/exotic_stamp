# Inspect a release image for non-root user, ports, labels, forbidden paths.
# Usage: .\scripts\release\inspect_image.ps1 -Image exotic-stamp-backend:git-abc1234
param(
    [Parameter(Mandatory = $true)]
    [string]$Image
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$OutDir = Join-Path $Root "artifacts\release"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$Short = ($Image -replace '[/:]', '_')
$Report = Join-Path $OutDir "image-inspect-$Short.txt"
$Fail = 0

$lines = @()
$lines += "=== image: $Image ==="
$lines += "=== inspect (user/entrypoint/ports/labels/env names) ==="
$inspect = docker image inspect $Image --format 'Id={{.Id}}`nUser={{.Config.User}}`nEntrypoint={{json .Config.Entrypoint}}`nCmd={{json .Config.Cmd}}`nExposedPorts={{json .Config.ExposedPorts}}`nLabels={{json .Config.Labels}}'
$lines += $inspect

# Env names only (strip values)
$envJson = docker image inspect $Image --format '{{json .Config.Env}}'
$envNames = @()
if ($envJson -and $envJson -ne "null") {
    $arr = $envJson | ConvertFrom-Json
    foreach ($e in $arr) {
        $envNames += ($e -split "=", 2)[0]
    }
}
$lines += ("EnvNames=" + ($envNames -join " "))
$lines += ""
$lines += "=== forbidden path scan ==="

$UserVal = (docker image inspect $Image --format "{{.Config.User}}").Trim()
if (-not $UserVal -or $UserVal -eq "root" -or $UserVal -eq "0") {
    $lines += "FAIL: image user is root or empty ($UserVal)"
    $Fail = 1
} else {
    $lines += "PASS: non-root user=$UserVal"
}

$findOut = docker run --rm --entrypoint /bin/sh $Image -c 'find / -xdev \( -name .env -o -name .git -o -name .m2 -o -path "*/uploads/*" -o -name "*.dump" \) 2>/dev/null | head -n 50'
$lines += $findOut
if ($findOut -match '(/\.env$|/\.git$|/\.m2$|/uploads/|\.dump$)') {
    $lines += "FAIL: forbidden filesystem entries detected"
    $Fail = 1
} else {
    $lines += "PASS: no forbidden .env/.git/.m2/uploads/dump hits in shallow scan"
}

$jarCheck = docker run --rm --entrypoint /bin/sh $Image -c 'test -f /app/app.jar && id'
if ($LASTEXITCODE -eq 0) {
    $lines += "PASS: /app/app.jar present"
    $lines += $jarCheck
} else {
    $lines += "FAIL: /app/app.jar missing"
    $Fail = 1
}

$lines | Set-Content -Path $Report -Encoding utf8
Write-Host "Report: $Report"
if ($Fail -ne 0) {
    Write-Host "INSPECT FAIL"
    exit 1
}
Write-Host "INSPECT PASS"
exit 0
