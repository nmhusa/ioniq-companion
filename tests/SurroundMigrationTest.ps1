$ErrorActionPreference = 'Stop'
$script = [IO.File]::ReadAllText((Join-Path $PSScriptRoot '..\update-existing-windows.ps1'))
$start = $script.IndexOf('# Add surround-view methods')
$end = $script.IndexOf(' $obsolete = @', $start)
$migration = $script.Substring($start,$end-$start)
$source = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$commandSignature = ' String command(String action,int temp) throws Exception {'
$patchedApi = 'PRIVATE_CONFIG_SENTINEL; out.size()>2000000; ' + $commandSignature
Invoke-Expression $migration
if (-not $patchedApi.Contains('surroundDetails()') -or -not $patchedApi.Contains('surroundCapture()') -or -not $patchedApi.Contains('PRIVATE_CONFIG_SENTINEL') -or -not $patchedApi.Contains('?16000000:2000000')) { throw 'Surround migration incomplete' }
$first = $patchedApi
Invoke-Expression $migration
if ($first -cne $patchedApi) { throw 'Surround migration not idempotent' }
$patchedApi = 'unknown adapter'
$rejected = $false
try { Invoke-Expression $migration } catch { $rejected=$true }
if (-not $rejected) { throw 'Unknown adapter accepted' }
Write-Output 'Surround migration checks passed'
