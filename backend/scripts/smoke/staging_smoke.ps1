# Staging / local-prod-like smoke tests. Never prints JWTs or voucher codes.
# Uses curl.exe + temp JSON files (PowerShell strips quotes in --data-binary strings).
param()

$ErrorActionPreference = "Stop"
if (-not $env:BASE_URL) { throw "BASE_URL required" }
$BaseUrl = $env:BASE_URL.TrimEnd("/")
$Mode = if ($env:SMOKE_MODE) { $env:SMOKE_MODE } else { "staging" }
$Fail = 0
$script:LastCode = 0
$script:LastBody = ""
$script:LastHeadersFile = Join-Path $env:TEMP "exotic-smoke-headers.txt"
$script:LastBodyFile = Join-Path $env:TEMP "exotic-smoke-body.txt"

function Redact([string]$text) {
    if (-not $text) { return "" }
    $t = $text
    $t = [regex]::Replace($t, '(?i)Authorization:\s*Bearer\s+\S+', 'Authorization: Bearer ***')
    $t = [regex]::Replace($t, '(?i)"accessToken"\s*:\s*"[^"]+"', '"accessToken":"***"')
    $t = [regex]::Replace($t, '(?i)"refreshToken"\s*:\s*"[^"]+"', '"refreshToken":"***"')
    $t = [regex]::Replace($t, '(?i)"voucherCode"\s*:\s*"[^"]+"', '"voucherCode":"***"')
    return $t
}

function Invoke-Smoke([string]$Method, [string]$Path, [string]$Body = $null, [string]$AuthBearer = $null) {
    $uri = "$BaseUrl$Path"
    $args = @("-sS", "-D", $script:LastHeadersFile, "-o", $script:LastBodyFile, "-w", "%{http_code}", "-X", $Method, $uri, "-H", "Accept: application/json")
    if ($Body) {
        $bodyFile = Join-Path $env:TEMP ("exotic-smoke-req-" + [guid]::NewGuid().ToString() + ".json")
        [System.IO.File]::WriteAllText($bodyFile, $Body, [System.Text.UTF8Encoding]::new($false))
        $args += @("-H", "Content-Type: application/json", "--data-binary", "@$bodyFile")
    }
    if ($AuthBearer) {
        $args += @("-H", "Authorization: Bearer $AuthBearer")
    }
    $code = & curl.exe @args
    $script:LastCode = [int]$code
    $script:LastBody = [System.IO.File]::ReadAllText($script:LastBodyFile)
    $snippet = $script:LastBody
    if ($snippet.Length -gt 400) { $snippet = $snippet.Substring(0, 400) }
    Write-Host ("HTTP $LastCode $Method $Path :: " + (Redact $snippet))
}

function Pass([string]$m) { Write-Host "PASS: $m" }
function Fail([string]$m) { Write-Host "FAIL: $m"; $script:Fail = 1 }

Write-Host "=== staging smoke mode=$Mode base=$BaseUrl ==="

Invoke-Smoke GET /actuator/health/liveness
if ($LastCode -eq 200) { Pass "liveness" } else { Fail "liveness ($LastCode)" }

Invoke-Smoke GET /actuator/health/readiness
if ($LastCode -eq 200) { Pass "readiness" } else { Fail "readiness ($LastCode)" }

Invoke-Smoke GET /swagger-ui/index.html
if ($LastCode -in 404, 401, 403) { Pass "swagger blocked ($LastCode)" } else { Fail "swagger exposed ($LastCode)" }

Invoke-Smoke POST /api/v1/auth/login '{"identifier":"invalid@example.com","password":"wrong-password-not-real"}'
if ($LastCode -in 401, 400, 403) { Pass "invalid login ($LastCode)" } else { Fail "invalid login unexpected ($LastCode)" }

$rl = $false
for ($i = 1; $i -le 25; $i++) {
    Invoke-Smoke POST /api/v1/auth/login '{"identifier":"ratelimit@example.com","password":"wrong"}' | Out-Null
    if ($LastCode -eq 429) {
        $rl = $true
        $hdrs = [System.IO.File]::ReadAllText($script:LastHeadersFile)
        if ($hdrs -match '(?i)Retry-After') { Pass "rate-limit 429 + Retry-After" } else { Fail "429 without Retry-After" }
        break
    }
}
if (-not $rl) { Write-Host "WARN: rate-limit 429 not observed in burst" }

if ($env:SMOKE_ADMIN_USER -and $env:SMOKE_ADMIN_PASSWORD) {
    $bodyObj = @{ identifier = $env:SMOKE_ADMIN_USER; password = $env:SMOKE_ADMIN_PASSWORD } | ConvertTo-Json -Compress
    Invoke-Smoke POST /api/v1/auth/login $bodyObj
    if ($LastCode -eq 200) { Pass "admin login" } else { Fail "admin login ($LastCode)" }
} else {
    Write-Host "SKIP admin authenticated checks"
}

if ($env:SMOKE_MOBILE_USER -and $env:SMOKE_MOBILE_PASSWORD) {
    $bodyObj = @{ identifier = $env:SMOKE_MOBILE_USER; password = $env:SMOKE_MOBILE_PASSWORD } | ConvertTo-Json -Compress
    Invoke-Smoke POST /api/v1/auth/login $bodyObj
    if ($LastCode -eq 200) { Pass "mobile login" } else { Fail "mobile login ($LastCode)" }
} else {
    Write-Host "SKIP mobile fixture flows"
}

Write-Host "INFO: Redis-down / S3-down / SIGTERM scenarios in STAGING_SMOKE_TEST_PLAN.md"

if ($Fail -ne 0) {
    Write-Host "SMOKE FAIL"
    exit 1
}
Write-Host "SMOKE PASS (mandatory checks)"
exit 0
