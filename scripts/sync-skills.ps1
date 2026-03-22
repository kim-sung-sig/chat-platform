param(
  [switch]$DryRun,
  [switch]$Check
)

$ErrorActionPreference = "Stop"

function Get-RepoRoot {
  return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

function New-Sha256([string]$Text) {
  $sha = [System.Security.Cryptography.SHA256]::Create()
  try {
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Text)
    $hash = $sha.ComputeHash($bytes)
    return ([BitConverter]::ToString($hash)).Replace("-", "").ToLowerInvariant()
  } finally {
    $sha.Dispose()
  }
}

$repoRoot = Get-RepoRoot
$registryPath = Join-Path $repoRoot ".harness/registries/skills.json"
$manifestPath = Join-Path $repoRoot ".harness/state/generated-files.json"

if (-not (Test-Path $registryPath)) {
  throw "Missing registry: $registryPath"
}

$registry = Get-Content -Raw -Path $registryPath -Encoding UTF8 | ConvertFrom-Json
$changes = New-Object System.Collections.Generic.List[object]
$generated = New-Object System.Collections.Generic.List[object]

function Write-GeneratedSkill {
  param(
    [pscustomobject]$Skill,
    [string]$TargetRelativePath
  )

  $sourceRelativePath = $Skill.source
  $sourcePath = Join-Path $repoRoot $sourceRelativePath
  if (-not (Test-Path $sourcePath)) {
    throw "Missing skill source for $($Skill.id): $sourceRelativePath"
  }

  $sourceBody = Get-Content -Raw -Path $sourcePath -Encoding UTF8
  $header = @"
<!-- GENERATED FILE: DO NOT EDIT DIRECTLY
source: $sourceRelativePath
skill: $($Skill.id)
generated-by: scripts/sync-skills.ps1
-->

"@
  $generatedBody = ($header + $sourceBody.TrimEnd() + "`n")

  $targetPath = Join-Path $repoRoot $TargetRelativePath
  $targetDir = Split-Path $targetPath -Parent
  if (-not (Test-Path $targetDir)) {
    if (-not $Check -and -not $DryRun) {
      New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
    }
  }

  $existing = $null
  if (Test-Path $targetPath) {
    $existing = Get-Content -Raw -Path $targetPath -Encoding UTF8
  }

  if ($existing -ne $generatedBody) {
    $changes.Add([pscustomobject]@{
      skill = $Skill.id
      path = $TargetRelativePath
    }) | Out-Null

    if (-not $Check -and -not $DryRun) {
      [System.IO.File]::WriteAllText($targetPath, $generatedBody, [System.Text.UTF8Encoding]::new($false))
    }
  }

  $generated.Add([pscustomobject]@{
    skill = $Skill.id
    path = $TargetRelativePath
    source = $sourceRelativePath
    sha256 = New-Sha256 -Text $generatedBody
  }) | Out-Null
}

foreach ($skill in $registry.managedSkills) {
  $claudeDir = if ($skill.adapters.claude_command_dir) { $skill.adapters.claude_command_dir } else { $skill.id }
  $codexDir = if ($skill.adapters.codex_skill_dir) { $skill.adapters.codex_skill_dir } else { $skill.id }

  Write-GeneratedSkill -Skill $skill -TargetRelativePath ".claude/commands/$claudeDir/SKILL.md"
  Write-GeneratedSkill -Skill $skill -TargetRelativePath ".codex/skills/$codexDir/SKILL.md"
}

$manifestObject = [ordered]@{
  generatedAt = $null
  generator = "scripts/sync-skills.ps1"
  files = $generated
}
$manifestJson = $manifestObject | ConvertTo-Json -Depth 6

$manifestChanged = $true
if (Test-Path $manifestPath) {
  $currentManifest = Get-Content -Raw -Path $manifestPath -Encoding UTF8 | ConvertFrom-Json
  $currentMap = @{}
  foreach ($f in $currentManifest.files) {
    $currentMap[$f.path] = $f.sha256
  }

  $newMap = @{}
  foreach ($f in $generated) {
    $newMap[$f.path] = $f.sha256
  }

  $sameCount = ($currentMap.Count -eq $newMap.Count)
  $samePairs = $true
  if ($sameCount) {
    foreach ($key in $newMap.Keys) {
      if (-not $currentMap.ContainsKey($key) -or $currentMap[$key] -ne $newMap[$key]) {
        $samePairs = $false
        break
      }
    }
  } else {
    $samePairs = $false
  }

  $manifestChanged = -not ($sameCount -and $samePairs)
}

if ($manifestChanged) {
  $changes.Add([pscustomobject]@{
    skill = "_manifest"
    path = ".harness/state/generated-files.json"
  }) | Out-Null

  if (-not $Check -and -not $DryRun) {
    [System.IO.File]::WriteAllText($manifestPath, $manifestJson + "`n", [System.Text.UTF8Encoding]::new($false))
  }
}

if ($changes.Count -eq 0) {
  Write-Output "sync-skills: no changes"
  exit 0
}

if ($DryRun) {
  Write-Output "sync-skills (dry-run): pending changes"
  $changes | ForEach-Object { Write-Output " - $($_.path)" }
  exit 0
}

if ($Check) {
  Write-Output "sync-skills (check): drift detected"
  $changes | ForEach-Object { Write-Output " - $($_.path)" }
  exit 2
}

Write-Output "sync-skills: updated $($changes.Count) file(s)"
$changes | ForEach-Object { Write-Output " - $($_.path)" }
exit 0
