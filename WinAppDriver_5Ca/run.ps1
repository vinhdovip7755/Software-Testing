param(
    [ValidateSet('all','emptyEmail','invalidEmail','emptyPassword','wrongPassword','validLogin','bookSearch','addToCart','editCartQuantity','removeFromCart','bookFormValidation','logout')]
    [string]$Case = 'all',
    [string]$Email = 'vinhyt7755@gmail.com',
    [string]$Password = '',
    [ValidateSet('winappdriver','swing','robot')]
    [string]$Driver = 'winappdriver',
    [int]$StepDelayMs = 700,
    [switch]$Isolated
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root 'desktop-client\target\BookStoreApp-jar-with-dependencies.jar'
if (!(Test-Path $jar)) { throw 'Build desktop-client first. See HUONG-DAN.md.' }

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
        # Note: On Windows 11 24H2 (build 26100), WinAppDriver GET /status can return HTTP 500
        # even while POST /session works normally, so probe TCP port 4723 directly.
        $tcp = New-Object System.Net.Sockets.TcpClient
        $ar = $tcp.BeginConnect('127.0.0.1', 4723, $null, $null)
        if ($ar.AsyncWaitHandle.WaitOne(400, $false) -and $tcp.Connected) {
            $tcp.EndConnect($ar)
            return $true
        }
        return $false
    } catch {
        return $false
    } finally {
        if ($tcp) { $tcp.Dispose() }
    }
}

$bin = Resolve-JdkBin
$javac = Join-Path $bin 'javac.exe'
$java = Join-Path $bin 'java.exe'

$classes = Join-Path $PSScriptRoot 'results\.classes-cache'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$srcFiles = @((Join-Path $PSScriptRoot 'WinKeys.java'), (Join-Path $PSScriptRoot 'BookstoreUiTest.java'))
$classFile = Join-Path $classes 'BookstoreUiTest.class'
$needsCompile = !(Test-Path $classFile)
if (!$needsCompile) {
    $classTime = (Get-Item $classFile).LastWriteTimeUtc
    foreach ($f in ($srcFiles + @($jar))) {
        if ((Get-Item $f).LastWriteTimeUtc -gt $classTime) { $needsCompile = $true; break }
    }
}
if ($needsCompile) {
    & $javac --release 17 -encoding UTF-8 -cp $jar -d $classes $srcFiles[0] $srcFiles[1]
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
}

$runDir = Join-Path $PSScriptRoot ('results\' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
New-Item -ItemType Directory -Path $runDir -Force | Out-Null

$priorEmail = $env:BOOKSTORE_TEST_EMAIL
$priorPassword = $env:BOOKSTORE_TEST_PASSWORD
$startedWinAppDriver = $null
$wadOut = Join-Path $env:TEMP 'bookstore_wad.out.log'
$wadErr = Join-Path $env:TEMP 'bookstore_wad.err.log'
$rows = @()

$allCases = @(
    'emptyEmail', 'invalidEmail', 'emptyPassword', 'wrongPassword', 'validLogin',
    'bookSearch', 'addToCart', 'editCartQuantity', 'removeFromCart',
    'bookFormValidation', 'logout'
)
$noApiCases = @('emptyEmail', 'invalidEmail')

function Invoke-UiProcess([string]$ArgCase, [string]$LogFile, [int]$TimeoutMs) {
    $proc = Start-Process -FilePath $java `
        -ArgumentList @('-Dfile.encoding=UTF-8', "-Dui.driver=$Driver", "-Dui.delayMs=$StepDelayMs", '-cp', ('"' + $classes + ';' + $jar + '"'), 'BookstoreUiTest', $ArgCase) `
        -WorkingDirectory $root -NoNewWindow -PassThru `
        -RedirectStandardOutput $LogFile -RedirectStandardError "$LogFile.err"
    # Cache Handle so PowerShell 5.1 always populates ExitCode after WaitForExit.
    $null = $proc.Handle
    if (!$proc.WaitForExit($TimeoutMs)) {
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
            if ($msg) {
                throw "WinAppDriver failed to start: $msg`n-> Bat Developer Mode trong Windows Settings, hoac chay nhanh voi -Driver swing."
            }
            throw 'WinAppDriver failed to listen on port 4723. Bat Developer Mode trong Windows Settings, hoac chay nhanh voi -Driver swing.'
        }
    }

    if ($noApiCases -notcontains $Case) {
        if ($Case -ne 'emptyPassword') {
            $apiUp = $false
            try { Invoke-RestMethod 'http://127.0.0.1:8080/api/health' -TimeoutSec 2 | Out-Null; $apiUp = $true } catch {}
            if (!$apiUp) {
                $apiJar = Join-Path $root 'api-server\target\bookstore-api-1.0-SNAPSHOT.jar'
                if (Test-Path $apiJar) {
                    Write-Host 'Starting BookStore API server on port 8080...'
                    Start-Process -FilePath $java -ArgumentList @('-jar', $apiJar) -WorkingDirectory $root -WindowStyle Minimized | Out-Null
                    $apiDeadline = (Get-Date).AddSeconds(20)
                    while (!$apiUp -and (Get-Date) -lt $apiDeadline) {
                        Start-Sleep -Milliseconds 300
                        try { Invoke-RestMethod 'http://127.0.0.1:8080/api/health' -TimeoutSec 2 | Out-Null; $apiUp = $true } catch {}
                    }
                }
            }
            if (!$apiUp) { throw 'Start the BookStore API on port 8080 first (start-api.bat).' }
        }

        if ($Password) {
            $env:BOOKSTORE_TEST_EMAIL = $Email
            $env:BOOKSTORE_TEST_PASSWORD = $Password
        } elseif ($Case -eq 'emptyPassword') {
            $env:BOOKSTORE_TEST_EMAIL = $Email
            $env:BOOKSTORE_TEST_PASSWORD = 'unused'
        } elseif (!$env:BOOKSTORE_TEST_EMAIL -or !$env:BOOKSTORE_TEST_PASSWORD) {
            $cred = Get-Credential -UserName $Email -Message 'BookStore test account with sales & product permissions'
            if ($null -eq $cred) { throw 'Cancelled.' }
            $env:BOOKSTORE_TEST_EMAIL = $cred.UserName
            $env:BOOKSTORE_TEST_PASSWORD = $cred.GetNetworkCredential().Password
        }
    }

    if ($Case -eq 'all' -and !$Isolated) {
        Write-Host "Running all $($allCases.Count) cases in single JVM (driver=$Driver, delay=${StepDelayMs}ms)..."
        $log = Join-Path $runDir 'suite.log'
        $time = [Diagnostics.Stopwatch]::StartNew()
        $res = Invoke-UiProcess -ArgCase 'all' -LogFile $log -TimeoutMs 300000
        $elapsed = [math]::Round($time.Elapsed.TotalSeconds, 1)

        if (Test-Path $log) {
            foreach ($line in (Get-Content $log -Encoding UTF8)) {
                if ($line -match '^RESULT\s+(\S+)\s+(PASS|FAIL|TIMEOUT)\s+([0-9.]+)$') {
                    $rows += [pscustomobject]@{ Test = $Matches[1]; Result = $Matches[2]; Seconds = $Matches[3] }
                    Write-Host ("{0,-20} {1,-7} ({2}s)" -f $Matches[1], $Matches[2], $Matches[3])
                }
            }
        }
        if ($rows.Count -eq 0) {
            $rows += [pscustomobject]@{ Test = 'all'; Result = $res.Status; Seconds = $elapsed }
        }
        $rows | Export-Csv (Join-Path $runDir 'summary.csv') -NoTypeInformation -Encoding UTF8
        Write-Host "Total: ${elapsed}s"
        if ($res.Status -ne 'PASS' -and (Test-Path "$log.err")) {
            Get-Content "$log.err" -Encoding UTF8 | Select-Object -First 30
        }
    } else {
        $cases = if ($Case -eq 'all') { $allCases } else { @($Case) }
        foreach ($test in $cases) {
            Write-Host "Running $test (driver=$Driver, delay=${StepDelayMs}ms)"
            $log = Join-Path $runDir "$test.log"
            $time = [Diagnostics.Stopwatch]::StartNew()
            $res = Invoke-UiProcess -ArgCase $test -LogFile $log -TimeoutMs 180000
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
    if ($startedWinAppDriver -and !$startedWinAppDriver.HasExited) {
        Stop-Process -Id $startedWinAppDriver.Id -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "Report: $runDir\summary.csv"
if ($rows | Where-Object Result -ne 'PASS') { exit 1 }
exit 0
