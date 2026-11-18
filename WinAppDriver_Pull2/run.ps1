param([ValidateSet('all','emptyEmail','wrongPassword','validLogin','bookSearch')][string]$Case = 'all')
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root 'desktop-client\target\BookStoreApp-jar-with-dependencies.jar'
if (!(Test-Path $jar)) { throw 'Build desktop-client first. See HUONG-DAN.md.' }
try { Invoke-RestMethod 'http://127.0.0.1:4723/status' -TimeoutSec 4 | Out-Null }
catch { throw 'Start WinAppDriver.exe on port 4723 first.' }
$bin = Join-Path $env:JAVA_HOME 'bin'
$javac = Join-Path $bin 'javac.exe'
$java = Join-Path $bin 'java.exe'
$runDir = Join-Path $PSScriptRoot ('results\' + (Get-Date -Format 'yyyyMMdd-HHmmss-fff'))
$classes = Join-Path $runDir 'classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
& $javac --release 17 -encoding UTF-8 -cp $jar -d $classes "$PSScriptRoot\WinKeys.java" "$PSScriptRoot\BookstoreUiTest.java"
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
$cases = @($Case)
if ($Case -eq 'all') { $cases = @('emptyEmail','wrongPassword','validLogin','bookSearch') }
$priorEmail = $env:BOOKSTORE_TEST_EMAIL
$priorPassword = $env:BOOKSTORE_TEST_PASSWORD
$rows = @()
try {
    if ($Case -ne 'emptyEmail') {
        try { Invoke-RestMethod 'http://127.0.0.1:8080/api/health' -TimeoutSec 5 | Out-Null }
        catch { throw 'Start the BookStore API on port 8080 first.' }
        $cred = Get-Credential -UserName 'nhan15042006@gmail.com' -Message 'BookStore test account with sales permissions'
        if ($null -eq $cred) { throw 'Cancelled.' }
        $env:BOOKSTORE_TEST_EMAIL = $cred.UserName
        $env:BOOKSTORE_TEST_PASSWORD = $cred.GetNetworkCredential().Password
    }
    foreach ($test in $cases) {
        Write-Host "Running $test"
        $log = Join-Path $runDir "$test.log"
        $time = [Diagnostics.Stopwatch]::StartNew()
        $process = Start-Process -FilePath $java -ArgumentList @('-Dfile.encoding=UTF-8','-cp',('"' + $classes + ';' + $jar + '"'),'BookstoreUiTest',$test) -WorkingDirectory $root -NoNewWindow -PassThru -RedirectStandardOutput $log -RedirectStandardError "$log.err"
        if (!$process.WaitForExit(150000)) {
            $process.Kill()
            $process.WaitForExit()
            $status = 'TIMEOUT'
        } elseif ($process.ExitCode -eq 0) { $status = 'PASS' } else { $status = 'FAIL' }
        $rows += [pscustomobject]@{Test=$test; Result=$status; Seconds=[math]::Round($time.Elapsed.TotalSeconds,1)}
        $rows | Export-Csv (Join-Path $runDir 'summary.csv') -NoTypeInformation -Encoding UTF8
        Write-Host "$test $status"
        if ($status -ne 'PASS') { Get-Content "$log.err" -Encoding UTF8 | Select-Object -First 12 }
    }
} finally {
    $env:BOOKSTORE_TEST_EMAIL = $priorEmail
    $env:BOOKSTORE_TEST_PASSWORD = $priorPassword
}
Write-Host "Report: $runDir\summary.csv"
if ($rows | Where-Object Result -ne 'PASS') { exit 1 }
exit 0
