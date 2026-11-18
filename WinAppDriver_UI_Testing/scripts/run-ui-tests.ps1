param(
    [Parameter(Mandatory = $true)][string]$AppJar,
    [Parameter(Mandatory = $true)][string]$TestEmail,
    [Parameter(Mandatory = $true)][string]$TestPassword,
    [Parameter(Mandatory = $true)][string]$SearchKeyword,
    [string]$WinAppDriverUrl = "http://127.0.0.1:4723"
)

$ErrorActionPreference = "Stop"
try {
    Invoke-WebRequest -Uri "$WinAppDriverUrl/status" -TimeoutSec 3 | Out-Null
} catch {
    throw "WinAppDriver chưa chạy tại $WinAppDriverUrl. Khởi chạy WinAppDriver.exe trước rồi chạy lại."
}

$env:BOOKSTORE_APP_JAR = (Resolve-Path $AppJar).Path
$env:BOOKSTORE_TEST_EMAIL = $TestEmail
$env:BOOKSTORE_TEST_PASSWORD = $TestPassword
$env:BOOKSTORE_SEARCH_KEYWORD = $SearchKeyword

$mavenProject = Join-Path $PSScriptRoot "..\pom.xml"
mvn -f $mavenProject test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
