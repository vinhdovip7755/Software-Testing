param(
    [ValidateSet('all','offline','emptyEmail','whitespaceEmail','invalidEmailNoAt','invalidEmailBadDomain','emptyPassword','togglePasswordVisibility','forgotPasswordValidation','forgotPasswordResetFlow','forgotPasswordSuccess','nonExistentAccount','wrongPassword','validLoginWithEnter','logout')]
    [string]$Case = 'all',
    [string]$Email = 'vinhyt7755@gmail.com',
    [string]$Password = '',
    [string]$ForgotEmail = 'lowres14@gmail.com',
    [string]$ForgotPassword = '01082006',
    [ValidateSet('winappdriver','swing','robot')]
    [string]$Driver = 'winappdriver',
    [int]$StepDelayMs = 700,
    [switch]$ManualOtp,
    [int]$ManualOtpWaitSec = 120,
    [switch]$OfflineOnly,
    [switch]$Isolated
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root 'desktop-client\target\BookStoreApp-jar-with-dependencies.jar'
if (!(Test-Path $jar)) { throw 'Build desktop-client first: mvn -pl desktop-client -am clean package -DskipTests' }

$dotEnvPath = Join-Path $root '.env'
if (Test-Path $dotEnvPath) {
    foreach ($rawLine in (Get-Content $dotEnvPath -Encoding UTF8)) {
        $line = $rawLine.Trim()
        if (!$line -or $line.StartsWith('#')) { continue }
        $idx = $line.IndexOf('=')
        if ($idx -le 0) { continue }
        $k = $line.Substring(0, $idx).Trim()
        $v = $line.Substring($idx + 1).Trim().Trim('"').Trim("'")
        if ($k -eq 'BOOKSTORE_SMTP_PASSWORD') { $v = $v -replace '\s+', '' }
        if ($k -and $v -and ![Environment]::GetEnvironmentVariable($k, 'Process')) {
            [Environment]::SetEnvironmentVariable($k, $v, 'Process')
        }
    }
}

if ($OfflineOnly -and $Case -eq 'all') { $Case = 'offline' }

function Resolve-JdkBin {
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += (Join-Path $env:JAVA_HOME 'bin') }
    $candidates += 'C:\Program Files\Java\jdk-17\bin'
    $jdkDirs = Get-ChildItem 'C:\Program Files\Java' -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue |
        Sort-Object Name
    foreach ($d in $jdkDirs) { $candidates += (Join-Path $d.FullName 'bin') }
    foreach ($dir in $candidates) {
        if ((Test-Path (Join-Path $dir 'javac.exe')) -and (Test-Path (Join-Path $dir 'java.exe'))) {
            return $dir
        }
    }
    $javacCmd = Get-Command javac.exe -ErrorAction SilentlyContinue
    $javaCmd = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javacCmd -and $javaCmd) { return Split-Path -Parent $javacCmd.Source }
    throw 'JDK 17+ not found. Set JAVA_HOME or install JDK.'
}

function Test-WinAppDriver {
    $tcp = $null
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $ar = $tcp.BeginConnect('127.0.0.1', 4723, $null, $null)
        if ($ar.AsyncWaitHandle.WaitOne(400, $false) -and $tcp.Connected) {
            $tcp.EndConnect($ar)
            return $true
        }
        return $false
    } catch { return $false } finally {
        if ($tcp) { $tcp.Dispose() }
    }
}

$bin = Resolve-JdkBin
$javac = Join-Path $bin 'javac.exe'
$java = Join-Path $bin 'java.exe'

$classes = Join-Path $PSScriptRoot 'results\.classes-cache'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$srcFiles = @((Join-Path $PSScriptRoot 'WinKeys.java'), (Join-Path $PSScriptRoot 'LoginUiTest.java'))
$classFile = Join-Path $classes 'LoginUiTest.class'
$needsCompile = !(Test-Path $classFile)
if (!$needsCompile) {
    $classTime = (Get-Item $classFile).LastWriteTimeUtc
    foreach ($f in ($srcFiles + @($jar))) {
        if ((Get-Item $f).LastWriteTimeUtc -gt $classTime) { $needsCompile = $true; break }
    }
}
if ($needsCompile) {
    & $javac --release 17 -encoding UTF-8 -cp $jar -d $classes $srcFiles[0] $srcFiles[1]
    if ($LASTEXITCODE -ne 0) { throw 'LoginUiTest compilation failed.' }
}

$runDir = Join-Path $PSScriptRoot ('results\login-' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
New-Item -ItemType Directory -Path $runDir -Force | Out-Null

$offlineCases = @('emptyEmail','whitespaceEmail','invalidEmailNoAt','invalidEmailBadDomain','emptyPassword','togglePasswordVisibility','forgotPasswordValidation')
$onlineCases = @('forgotPasswordResetFlow','forgotPasswordSuccess','nonExistentAccount','wrongPassword','validLoginWithEnter','logout')
$allCases = $offlineCases + $onlineCases
$needsApi = ($Case -eq 'all') -or ($onlineCases -contains $Case)

$priorEmail = $env:BOOKSTORE_TEST_EMAIL
$priorPassword = $env:BOOKSTORE_TEST_PASSWORD
$priorForgotEmail = $env:BOOKSTORE_FORGOT_EMAIL
$priorForgotPassword = $env:BOOKSTORE_FORGOT_PASSWORD
$startedWinAppDriver = $null
$startedApiServer = $null
$apiBaseUrl = 'http://127.0.0.1:8080'
$otpFile = Join-Path $runDir 'test-otp.txt'
$wadOut = Join-Path $env:TEMP 'bookstore_wad.out.log'
$wadErr = Join-Path $env:TEMP 'bookstore_wad.err.log'
$rows = @()

function Get-FreePort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    $listener.Start()
    $port = ([System.Net.IPEndPoint]$listener.LocalEndpoint).Port
    $listener.Stop()
    return $port
}

$otpMode = if ($ManualOtp) { 'manual' } else { 'auto' }
$extraTimeoutMs = if ($ManualOtp) { ($ManualOtpWaitSec + 30) * 1000 } else { 0 }

function Invoke-LoginProcess([string]$ArgCase, [string]$LogFile, [int]$TimeoutMs) {
    $proc = Start-Process -FilePath $java `
        -ArgumentList @(
            '-Dfile.encoding=UTF-8',
            "-Dui.driver=$Driver",
            "-Dui.delayMs=$StepDelayMs",
            "-Dbookstore.api.base-url=$apiBaseUrl",
            "-Dbookstore.otp.mode=$otpMode",
            "-Dbookstore.otp.waitSec=$ManualOtpWaitSec",
            "-Dbookstore.otp.file=$otpFile",
            '-cp', ('"' + $classes + ';' + $jar + '"'),
            'LoginUiTest', $ArgCase
        ) `
        -WorkingDirectory $root -NoNewWindow -PassThru `
        -RedirectStandardOutput $LogFile -RedirectStandardError "$LogFile.err"
    $null = $proc.Handle
    if (!$proc.WaitForExit($TimeoutMs + $extraTimeoutMs)) {
        $proc.Kill()
        $proc.WaitForExit()
        return @{ Status = 'TIMEOUT'; ExitCode = -1 }
    }
    $proc.WaitForExit()
    $code = $proc.ExitCode
    return @{ Status = $(if ($code -eq 0) { 'PASS' } else { 'FAIL' }); ExitCode = $code }
}

try {
    if ($Driver -eq 'winappdriver' -and !(Test-WinAppDriver)) {
        $wadPaths = @(
            (Join-Path $env:ProgramFiles 'Windows Application Driver\WinAppDriver.exe'),
            (Join-Path ${env:ProgramFiles(x86)} 'Windows Application Driver\WinAppDriver.exe')
        )
        $wadExe = $wadPaths | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
        if (!$wadExe) { throw 'WinAppDriver.exe not found. Install WinAppDriver or run with -Driver swing.' }
        Write-Host "Starting WinAppDriver: $wadExe"
        $startedWinAppDriver = Start-Process -FilePath $wadExe -WindowStyle Hidden -PassThru `
            -RedirectStandardOutput $wadOut -RedirectStandardError $wadErr
        $deadline = (Get-Date).AddSeconds(6)
        while (!(Test-WinAppDriver) -and !$startedWinAppDriver.HasExited -and (Get-Date) -lt $deadline) {
            Start-Sleep -Milliseconds 200
        }
        if (!(Test-WinAppDriver)) {
            $msg = ''
            if (Test-Path $wadOut) { $msg = ((Get-Content $wadOut -Raw -ErrorAction SilentlyContinue) -replace "`0", '').Trim() }
            if (!$msg -and (Test-Path $wadErr)) { $msg = ((Get-Content $wadErr -Raw -ErrorAction SilentlyContinue) -replace "`0", '').Trim() }
            throw "WinAppDriver failed to start: $msg`n-> Bat Developer Mode trong Windows Settings, hoac chay voi -Driver swing."
        }
    }

    if ($needsApi) {
        $apiJar = Join-Path $root 'api-server\target\bookstore-api-1.0-SNAPSHOT.jar'
        if (!(Test-Path $apiJar)) {
            throw 'Build api-server first (mvn -f api-server/pom.xml package -DskipTests), hoac chay 7 ca UI offline voi -OfflineOnly.'
        }
        if ($ManualOtp -and (!$env:BOOKSTORE_SMTP_USERNAME -or !$env:BOOKSTORE_SMTP_PASSWORD)) {
            throw 'Che do -ManualOtp can dien BOOKSTORE_SMTP_USERNAME va BOOKSTORE_SMTP_PASSWORD trong file .env (hoac bien moi truong Windows).'
        }
        $apiPort = Get-FreePort
        $apiBaseUrl = "http://127.0.0.1:$apiPort"
        if ($ManualOtp) {
            Write-Host "Starting dedicated test BookStore API server on port $apiPort (REAL Gmail SMTP: $($env:BOOKSTORE_SMTP_USERNAME))..."
            $apiArgs = @("-Dserver.port=$apiPort", '-jar', $apiJar)
        } else {
            Write-Host "Starting dedicated test BookStore API server on port $apiPort (OTP capture enabled)..."
            $apiArgs = @("-Dserver.port=$apiPort", "-Dbookstore.otp.test-out=$otpFile", '-jar', $apiJar)
        }
        $startedApiServer = Start-Process -FilePath $java `
            -ArgumentList $apiArgs `
            -WorkingDirectory $root -WindowStyle Hidden -PassThru `
            -RedirectStandardOutput (Join-Path $runDir 'api-server.out.log') `
            -RedirectStandardError (Join-Path $runDir 'api-server.err.log')
        $apiUp = $false
        $apiDeadline = (Get-Date).AddSeconds(25)
        while (!$apiUp -and !$startedApiServer.HasExited -and (Get-Date) -lt $apiDeadline) {
            Start-Sleep -Milliseconds 300
            try { Invoke-RestMethod "$apiBaseUrl/api/health" -TimeoutSec 2 | Out-Null; $apiUp = $true } catch {}
        }
        if (!$apiUp) {
            throw "Test API server failed to start on port $apiPort. See $runDir\api-server.err.log"
        }

        $env:BOOKSTORE_FORGOT_EMAIL = $ForgotEmail
        $env:BOOKSTORE_FORGOT_PASSWORD = $ForgotPassword
        if ($Password) {
            $env:BOOKSTORE_TEST_EMAIL = $Email
            $env:BOOKSTORE_TEST_PASSWORD = $Password
        } elseif ($Case -eq 'forgotPasswordResetFlow' -or $Case -eq 'forgotPasswordSuccess' -or $Case -eq 'nonExistentAccount') {
            $env:BOOKSTORE_TEST_EMAIL = $Email
        } elseif (!$env:BOOKSTORE_TEST_EMAIL -or !$env:BOOKSTORE_TEST_PASSWORD) {
            $cred = Get-Credential -UserName $Email -Message 'BookStore account for Login UI tests'
            if ($null -eq $cred) { throw 'Cancelled.' }
            $env:BOOKSTORE_TEST_EMAIL = $cred.UserName
            $env:BOOKSTORE_TEST_PASSWORD = $cred.GetNetworkCredential().Password
        }
    } else {
        $env:BOOKSTORE_TEST_EMAIL = $Email
    }

    if (($Case -eq 'all' -or $Case -eq 'offline') -and !$Isolated) {
        $count = if ($Case -eq 'offline') { $offlineCases.Count } else { $allCases.Count }
        Write-Host "Running $count Login UI test cases in single JVM (driver=$Driver, delay=${StepDelayMs}ms)..."
        $log = Join-Path $runDir 'login-suite.log'
        $time = [Diagnostics.Stopwatch]::StartNew()
        $res = Invoke-LoginProcess -ArgCase $Case -LogFile $log -TimeoutMs 240000
        $elapsed = [math]::Round($time.Elapsed.TotalSeconds, 1)

        if (Test-Path $log) {
            foreach ($line in (Get-Content $log -Encoding UTF8)) {
                if ($line -match '^RESULT\s+(\S+)\s+(PASS|FAIL|TIMEOUT)\s+([0-9.]+)$') {
                    $rows += [pscustomobject]@{ Test = $Matches[1]; Result = $Matches[2]; Seconds = $Matches[3] }
                    Write-Host ("{0,-26} {1,-7} ({2}s)" -f $Matches[1], $Matches[2], $Matches[3])
                }
            }
        }
        if ($rows.Count -eq 0) {
            $rows += [pscustomobject]@{ Test = $Case; Result = $res.Status; Seconds = $elapsed }
        }
        $rows | Export-Csv (Join-Path $runDir 'summary.csv') -NoTypeInformation -Encoding UTF8
        Write-Host "Total: ${elapsed}s"
        if ($res.Status -ne 'PASS' -and (Test-Path "$log.err")) {
            Get-Content "$log.err" -Encoding UTF8 | Select-Object -First 30
        }
    } else {
        $cases = if ($Case -eq 'all') { $allCases } elseif ($Case -eq 'offline') { $offlineCases } else { @($Case) }
        foreach ($test in $cases) {
            Write-Host "Running $test (driver=$Driver, delay=${StepDelayMs}ms)"
            $log = Join-Path $runDir "$test.log"
            $time = [Diagnostics.Stopwatch]::StartNew()
            $res = Invoke-LoginProcess -ArgCase $test -LogFile $log -TimeoutMs 120000
            $rows += [pscustomobject]@{ Test = $test; Result = $res.Status; Seconds = [math]::Round($time.Elapsed.TotalSeconds, 1) }
            $rows | Export-Csv (Join-Path $runDir 'summary.csv') -NoTypeInformation -Encoding UTF8
            Write-Host "$test $($res.Status)"
            if ($res.Status -ne 'PASS' -and (Test-Path "$log.err")) {
                Get-Content "$log.err" -Encoding UTF8 | Select-Object -First 25
            }
        }
    }
} finally {
    $env:BOOKSTORE_TEST_EMAIL = $priorEmail
    $env:BOOKSTORE_TEST_PASSWORD = $priorPassword
    $env:BOOKSTORE_FORGOT_EMAIL = $priorForgotEmail
    $env:BOOKSTORE_FORGOT_PASSWORD = $priorForgotPassword
    if ($startedApiServer -and !$startedApiServer.HasExited) {
        Stop-Process -Id $startedApiServer.Id -Force -ErrorAction SilentlyContinue
    }
    if ($startedWinAppDriver -and !$startedWinAppDriver.HasExited) {
        Stop-Process -Id $startedWinAppDriver.Id -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "Report: $runDir\summary.csv"
if ($rows | Where-Object Result -ne 'PASS') { exit 1 }
exit 0
