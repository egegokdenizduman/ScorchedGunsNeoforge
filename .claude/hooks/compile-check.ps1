# Stop hook: if Java sources changed since the last successful check, run compileJava.
# A failed compile blocks the stop once so Claude fixes it; the result is cached by diff hash.
$ErrorActionPreference = 'SilentlyContinue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$hookInput = [Console]::In.ReadToEnd() | ConvertFrom-Json
$root = git rev-parse --show-toplevel
if (-not $root) { exit 0 }
Set-Location $root

$changed = git status --porcelain -- src/main/java
if (-not $changed) { exit 0 }

# Fingerprint the current Java changes so an unchanged tree isn't recompiled every turn
$diff = (git diff HEAD -- src/main/java | Out-String) + ($changed | Out-String)
$sha = [System.Security.Cryptography.SHA1]::Create()
$hash = [BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes($diff))) -replace '-', ''
$stamp = Join-Path $root '.claude/.compile-ok'
if ((Test-Path $stamp) -and ((Get-Content $stamp) -eq $hash)) { exit 0 }

# Merge stderr inside cmd: PowerShell 5.1 drops native stderr under SilentlyContinue
$gradlew = Join-Path ($root -replace '/', '\') 'gradlew.bat'
$out = cmd /c "cd /d `"$(Split-Path $gradlew)`" && call `"$gradlew`" compileJava --offline -q 2>&1" | Out-String
if ($LASTEXITCODE -eq 0) {
    Set-Content -Path $stamp -Value $hash
    exit 0
}

$errors = ($out -split "`r?`n" | Where-Object { $_ -match 'error:|\.java:\d+' } | Select-Object -First 30) -join "`n"
if (-not $errors) { $errors = ($out -split "`r?`n" | Select-Object -Last 30) -join "`n" }
if ($hookInput.stop_hook_active) {
    # Already blocked once this turn; don't loop. Tell the owner instead.
    @{ systemMessage = "compileJava still fails after Claude's retry:`n$errors" } | ConvertTo-Json -Compress
    exit 0
}
@{ decision = 'block'; reason = "compileJava failed. Fix these errors before finishing:`n$errors" } | ConvertTo-Json -Compress
