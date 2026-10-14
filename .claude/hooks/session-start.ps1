# SessionStart hook: hand the new window the current status and the workflow gates.
$ErrorActionPreference = 'SilentlyContinue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$root = git rev-parse --show-toplevel
if (-not $root) { $root = (Get-Location).Path }

$parts = @("# Session start (automatic hook)",
    "Before any work: follow docs/WORKFLOW.md. Gate 1: post a checklist quoting the doc item and wait for approval. Gate 2: one sample before any batch.")
$branch = git branch --show-current
if ($branch) { $parts += "Current branch: $branch" }
$handoff = Join-Path $root 'docs/HANDOFF.md'
if (Test-Path $handoff) { $parts += Get-Content -Raw -Encoding UTF8 $handoff }
else { $parts += "docs/HANDOFF.md not found on this branch; the handoff lives on the GitHub branch 'test'." }

@{ hookSpecificOutput = @{ hookEventName = 'SessionStart'; additionalContext = ($parts -join "`n`n") } } |
    ConvertTo-Json -Compress -Depth 4
