$ErrorActionPreference = 'Stop'
$source = [IO.File]::ReadAllText((Join-Path $PSScriptRoot '..\update-existing-windows.ps1'))
$start = $source.IndexOf('# Prepare credential masking locally')
$end = $source.IndexOf('# End credential masking.', $start)
$migration = $source.Substring($start,$end-$start)
$patchedApi = 'h.setRequestProperty("clientSecret","SYNTHETIC_TEST_credential");'
Invoke-Expression $migration
if ($patchedApi.Contains('"SYNTHETIC_TEST_credential"')) { throw 'Plaintext was retained' }
$m = [regex]::Match($patchedApi,'new int\[\]\{([^}]*)\},new int\[\]\{([^}]*)\}')
$a=$m.Groups[1].Value.Split(',');$b=$m.Groups[2].Value.Split(',')
$bytes = New-Object byte[] $a.Length
for($i=0;$i -lt $a.Length;$i++) { $bytes[$i]=([int]$a[$i] -bxor [int]$b[$i]) }
if([Text.Encoding]::UTF8.GetString($bytes) -cne 'SYNTHETIC_TEST_credential') { throw 'Credential changed' }
$first=$patchedApi;Invoke-Expression $migration
if($first -cne $patchedApi) { throw 'Not idempotent' }
$patchedApi='h.setRequestProperty("clientSecret",custom());'
$rejected=$false;try{Invoke-Expression $migration}catch{$rejected=$true}
if(-not $rejected){throw 'Unknown source accepted'}
$patchedApi='if(BuildConfig.HYUNDAI_CLIENT_SECRET.isEmpty())throw new Exception(); h.setRequestProperty("clientSecret",BuildConfig.HYUNDAI_CLIENT_SECRET);'
Invoke-Expression $migration
if($patchedApi.Contains('HYUNDAI_CLIENT_SECRET') -or -not $patchedApi.Contains('BuildConfig.CLIENT_PAD')){throw 'BuildConfig migration failed'}
Write-Output '5 credential migration checks passed'
