$ErrorActionPreference = 'Stop'
$source = [IO.File]::ReadAllText((Join-Path $PSScriptRoot '..\update-existing-windows.ps1'))
$start = $source.IndexOf('# Validate a narrow local climate-payload migration')
$end = $source.IndexOf('$work = Join-Path', $start)
if ($start -lt 0 -or $end -lt 0) { throw 'Migration section missing' }
$migration = $source.Substring($start, $end-$start)
$project = Join-Path ([IO.Path]::GetTempPath()) ('ioniq-migration-test-' + [guid]::NewGuid())
$javaRelative = 'api'
New-Item -ItemType Directory (Join-Path $project $javaRelative) | Out-Null
try {
 $file = Join-Path $project 'api\HyundaiApi.java'
 $original = ' String command(String action,int temp) throws Exception { CREDENTIAL_SENTINEL; headers("service_type","REMOTE_POLL"); context.getSharedPreferences("status",0); body=new JSONObject().put("airCtrl",1).put("defrost",false).put("airTemp",new JSONObject().put("value",String.valueOf(temp)).put("unit",1)).put("heating1",0);' + "`n" + '   default:throw new Exception("Unsupported command");'
 [IO.File]::WriteAllText($file,$original)
 Invoke-Expression $migration
 if (-not $patchedApi.Contains('case "horn_lights"') -or -not $patchedApi.Contains('VehicleSignal.serviceType(action)') -or -not $patchedApi.Contains('CREDENTIAL_SENTINEL') -or -not $patchedApi.Contains('ClimatePreset.body(context,temp)') -or -not $patchedApi.Contains('case "limits"') -or -not $patchedApi.Contains('JSONObject findCar()') -or -not $patchedApi.Contains('SecurePrefs.get(context,"status")') -or $patchedApi.Contains('context.getSharedPreferences("status",0)')) { throw 'Migration lost existing source or missed additions' }
 $first = $patchedApi
 [IO.File]::WriteAllText($file,$first)
 Invoke-Expression $migration
 if ($patchedApi -cne $first) { throw 'Migration not idempotent' }
 $priorVersion = $original.Replace($oldClimate,$newClimate)
 [IO.File]::WriteAllText($file,$priorVersion)
 Invoke-Expression $migration
 if ($patchedApi -cne $first) { throw '0.7.6 upgrade differs from original upgrade' }
 [IO.File]::WriteAllText($file,'unknown customized source')
 $rejected = $false
 try { Invoke-Expression $migration } catch { $rejected=$true }
 if (-not $rejected -or [IO.File]::ReadAllText($file) -cne 'unknown customized source') { throw 'Unknown adapter was not rejected without writes' }
 Write-Output '4 updater migration checks passed'
} finally { Remove-Item $project -Recurse -Force }
