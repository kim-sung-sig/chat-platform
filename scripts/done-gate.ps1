param(
  [switch]$NoSyncCheck
)

$ErrorActionPreference = "Stop"

function Get-RepoRoot {
  return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

$repoRoot = Get-RepoRoot
$policyPath = Join-Path $repoRoot ".harness/registries/done-gate.json"

if (-not (Test-Path $policyPath)) {
  throw "Missing done-gate policy: $policyPath"
}

$policy = Get-Content -Raw -Path $policyPath -Encoding UTF8 | ConvertFrom-Json

$blockers = New-Object System.Collections.Generic.List[string]
$warnings = New-Object System.Collections.Generic.List[string]

function Add-Issue {
  param(
    [string]$Severity,
    [string]$Message
  )
  if ($Severity -eq "block") {
    $blockers.Add($Message) | Out-Null
  } else {
    $warnings.Add($Message) | Out-Null
  }
}

if (-not $NoSyncCheck -and $policy.checks.skill_sync_drift.enabled) {
  & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repoRoot "scripts/sync-skills.ps1") -Check
  if ($LASTEXITCODE -ne 0) {
    Add-Issue -Severity $policy.checks.skill_sync_drift.severity -Message "Skill adapter drift detected. Run scripts/sync-skills.ps1 and commit generated outputs."
  }
}

$changedFiles = @()
try {
  $diffOutput = git -c core.quotepath=false diff --name-only HEAD --
  if ($LASTEXITCODE -eq 0 -and $diffOutput) {
    $changedFiles = @($diffOutput -split "`r?`n" | Where-Object { $_ -and $_.Trim() -ne "" })
  }
} catch {
  $warnings.Add("Git diff unavailable; skipped file-based done checks.") | Out-Null
}

if ($policy.checks.protected_paths.enabled -and $changedFiles.Count -gt 0) {
  $allowProtectedEnv = $policy.override_env.allow_protected_path_edits
  $allowProtected = [System.Environment]::GetEnvironmentVariable($allowProtectedEnv)
  if ($allowProtected -ne "1") {
    foreach ($path in $policy.protected_paths) {
      if ($changedFiles -contains $path) {
        Add-Issue -Severity $policy.checks.protected_paths.severity -Message "Protected path changed: $path (override with $allowProtectedEnv=1 only for approved maintenance)."
      }
    }
  }
}

if ($policy.checks.test_evidence.enabled -and $changedFiles.Count -gt 0) {
  $mainChanged = $changedFiles | Where-Object { $_ -match "src/main/" }
  $testChanged = $changedFiles | Where-Object { $_ -match "src/test/" }
  if ($mainChanged.Count -gt 0 -and $testChanged.Count -eq 0) {
    Add-Issue -Severity $policy.checks.test_evidence.severity -Message "Main code changed without test changes. Add/adjust tests or document why not needed."
  }
}

if ($policy.checks.review_score.enabled) {
  $envVar = $policy.checks.review_score.env_var
  $minScore = [int]$policy.checks.review_score.minimum
  $scoreRaw = [System.Environment]::GetEnvironmentVariable($envVar)

  if ([string]::IsNullOrWhiteSpace($scoreRaw)) {
    Add-Issue -Severity $policy.checks.review_score.severity -Message "Review score missing. Set $envVar=$minScore+ or adjust policy."
  } else {
    $score = 0
    if (-not [int]::TryParse($scoreRaw, [ref]$score)) {
      Add-Issue -Severity $policy.checks.review_score.severity -Message "Review score is not an integer: $scoreRaw"
    } elseif ($score -lt $minScore) {
      Add-Issue -Severity $policy.checks.review_score.severity -Message "Review score below threshold: $score < $minScore"
    }
  }
}

if ($blockers.Count -gt 0) {
  Write-Output "DONE-GATE: FAIL"
  $blockers | ForEach-Object { Write-Output "BLOCK: $_" }
  $warnings | ForEach-Object { Write-Output "WARN: $_" }
  exit 2
}

Write-Output "DONE-GATE: PASS"
$warnings | ForEach-Object { Write-Output "WARN: $_" }
exit 0
