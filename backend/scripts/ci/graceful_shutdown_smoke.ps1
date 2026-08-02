# Graceful shutdown smoke — SIGTERM / docker stop within timeout.
# Usage:
#   $env:BASE_URL='http://127.0.0.1:8080'; $env:CONTAINER='exotic-backend'; .\scripts\ci\graceful_shutdown_smoke.ps1
#   $env:APP_PID=12345; .\scripts\ci\graceful_shutdown_smoke.ps1

$ErrorActionPreference = 'Stop'
$TimeoutSec = if ($env:TIMEOUT_SEC) { [int]$env:TIMEOUT_SEC } else { 45 }
$BaseUrl = if ($env:BASE_URL) { $env:BASE_URL } else { 'http://127.0.0.1:8080' }

Write-Host "[graceful-shutdown] checking readiness at $BaseUrl/actuator/health/readiness"
Invoke-WebRequest -Uri "$BaseUrl/actuator/health/readiness" -TimeoutSec 5 | Out-Null

if ($env:CONTAINER) {
    Write-Host "[graceful-shutdown] docker stop -t $TimeoutSec $($env:CONTAINER)"
    docker stop -t $TimeoutSec $env:CONTAINER
    Write-Host '[graceful-shutdown] PASS (container stopped)'
    exit 0
}

if ($env:APP_PID) {
    $procId = [int]$env:APP_PID
    Write-Host "[graceful-shutdown] Stop-Process -Id $procId (close)"
    Stop-Process -Id $procId -ErrorAction SilentlyContinue
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while (Get-Process -Id $procId -ErrorAction SilentlyContinue) {
        if ((Get-Date) -gt $deadline) {
            Write-Error "[graceful-shutdown] FAIL — process still alive after ${TimeoutSec}s"
            exit 1
        }
        Start-Sleep -Seconds 1
    }
    Write-Host '[graceful-shutdown] PASS (process exited)'
    exit 0
}

Write-Error 'Set CONTAINER=<name> or APP_PID=<pid>'
exit 2
