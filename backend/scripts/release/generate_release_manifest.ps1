# Wrapper: generate_release_manifest.py
# Usage:
#   .\scripts\release\generate_release_manifest.ps1 -AllowDirty -Version 0.1.0-rc.1
param(
    [switch]$AllowDirty,
    [string]$Version = "",
    [string]$ImageRepo = "",
    [string]$Digest = "",
    [string]$Out = "",
    [string]$Profile = "",
    [string]$Notes = ""
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $Root) { $Root = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path }

$argsList = @()
if ($AllowDirty) { $argsList += "--allow-dirty" }
if ($Version) { $argsList += @("--version", $Version) }
if ($ImageRepo) { $argsList += @("--image-repo", $ImageRepo) }
if ($Digest) { $argsList += @("--digest", $Digest) }
if ($Out) { $argsList += @("--out", $Out) }
if ($Profile) { $argsList += @("--profile", $Profile) }
if ($Notes) { $argsList += @("--notes", $Notes) }

python (Join-Path $Root "scripts\release\generate_release_manifest.py") @argsList
exit $LASTEXITCODE
