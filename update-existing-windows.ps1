param([string]$ProjectPath = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
# Pinned source. Known climate payload and charge-limit case are patched in HyundaiApi.java.
# Credentials, Vault.java and signing configuration are preserved; API source is never uploaded.
$revision = 'c589e997a890506a72ae86204e54605f0ba6e7be'
$project = (Resolve-Path $ProjectPath).Path
$javaRelative = 'app\src\main\java\com\ioniq\widget'
if (-not (Test-Path (Join-Path $project "$javaRelative\HyundaiApi.java"))) { throw 'Open PowerShell in your existing working IONIQ app project folder, then run this script again.' }
$gradlePath = Join-Path $project 'app\build.gradle'
if (-not (Test-Path $gradlePath)) { throw 'Expected app\build.gradle in this project.' }
$gradle = [IO.File]::ReadAllText($gradlePath)
if ($gradle -notmatch 'versionCode\s+\d+' -or $gradle -notmatch 'versionName\s+[''"][^''"]+[''"]') { throw 'Unrecognized build.gradle format; no files were changed.' }
# Validate a narrow local climate-payload migration before changing any files.
$apiPath = Join-Path $project "$javaRelative\HyundaiApi.java"
$apiCode = [IO.File]::ReadAllText($apiPath)
$oldClimate = 'new JSONObject().put("airCtrl",1).put("defrost",false).put("airTemp",new JSONObject().put("value",String.valueOf(temp)).put("unit",1)).put("heating1",0)'
$newClimate = 'ClimatePreset.body(context,temp)'
if ($apiCode.Contains($newClimate)) { $patchedApi = $apiCode }
elseif ([regex]::Matches($apiCode, [regex]::Escape($oldClimate)).Count -eq 1) { $patchedApi = $apiCode.Replace($oldClimate, $newClimate) }
else { throw 'Unrecognized climate payload; no files changed. Keep your API file private and ask for help updating it.' }
# Add the charge-limit command without replacing the locally configured API adapter.
$limitCase = '   case "limits":path="ac/v2/evc/charge/targetsoc/set";body=ChargeLimits.body(temp);break;'
$defaultCase = '   default:throw new Exception("Unsupported command");'
if (-not $patchedApi.Contains($limitCase)) {
 if ($patchedApi.Contains('case "limits"') -or [regex]::Matches($patchedApi, [regex]::Escape($defaultCase)).Count -ne 1) { throw 'Unrecognized command switch; no files changed.' }
 $patchedApi = $patchedApi.Replace($defaultCase, $limitCase + "`n" + $defaultCase)
}
# Add an on-demand location read, preserving all existing connection configuration.
$locationMethod = ' JSONObject findCar() throws Exception { requireVehicle();ensureToken();return request("ac/v2/rcs/rfc/findMyCar","GET",null,true,true,headers()).json; }'
$commandSignature = ' String command(String action,int temp) throws Exception {'
if (-not $patchedApi.Contains($locationMethod)) {
 if ($patchedApi.Contains('JSONObject findCar(') -or [regex]::Matches($patchedApi, [regex]::Escape($commandSignature)).Count -ne 1) { throw 'Unrecognized API method layout; no files changed.' }
 $patchedApi = $patchedApi.Replace($commandSignature, $locationMethod + "`n" + $commandSignature)
}
$work = Join-Path ([IO.Path]::GetTempPath()) ('ioniq-update-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory $work | Out-Null
$backup = Join-Path $project ('.update-backups\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
try {
 $archive = Join-Path $work 'source.zip'
 Invoke-WebRequest "https://github.com/nmhusa/ioniq-companion/archive/$revision.zip" -OutFile $archive
 Expand-Archive $archive -DestinationPath $work
 $source = Join-Path $work "ioniq-companion-$revision"
 $files = @('app\src\main\AndroidManifest.xml', 'build-windows.ps1', 'build-release-windows.ps1', 'INSTALL.md', 'PUBLISH.md', 'RELEASE-0.7.10.md', 'PARKING.md')
 $files += Get-ChildItem (Join-Path $source $javaRelative) -Filter '*.java' | Where-Object { $_.Name -notin @('HyundaiApi.java','Vault.java') } | ForEach-Object { "$javaRelative\$($_.Name)" }
 $files += Get-ChildItem (Join-Path $source 'app\src\main\res') -File -Recurse | ForEach-Object { $_.FullName.Substring($source.Length + 1) }
 # Validate the complete download and take backups before replacing any project file.
 foreach ($relative in $files) { if (-not (Test-Path (Join-Path $source $relative))) { throw "Download is incomplete: $relative" } }
 New-Item -ItemType Directory -Force $backup | Out-Null
 foreach ($relative in ($files + @('app\build.gradle', "$javaRelative\HyundaiApi.java"))) {
  $target = Join-Path $project $relative
  if (Test-Path $target) {
   $saved = Join-Path $backup $relative
   New-Item -ItemType Directory -Force (Split-Path $saved) | Out-Null
   Copy-Item $target $saved
  }
 }
 foreach ($relative in $files) {
  $target = Join-Path $project $relative
  New-Item -ItemType Directory -Force (Split-Path $target) | Out-Null
  Copy-Item (Join-Path $source $relative) $target -Force
 }
 [IO.File]::WriteAllText($apiPath, $patchedApi, [Text.UTF8Encoding]::new($false))
 $gradle = $gradle -replace 'versionCode\s+\d+', 'versionCode 17'
 $gradle = $gradle -replace 'versionName\s+[''"][^''"]+[''"]', "versionName '0.7.10'"
 [IO.File]::WriteAllText($gradlePath, $gradle, [Text.UTF8Encoding]::new($false))
 $ignore = Join-Path $project '.gitignore'
 if (-not (Test-Path $ignore) -or -not (Select-String -Path $ignore -SimpleMatch '.update-backups/' -Quiet)) { Add-Content $ignore "`n.update-backups/" }
 Write-Host "Updated app source to 0.7.10. Backup: $backup"
 Write-Host 'Your existing API configuration and signing files were preserved.'
 Write-Host 'Next, run: powershell -ExecutionPolicy Bypass -File .\build-release-windows.ps1'
} finally { Remove-Item $work -Recurse -Force -ErrorAction SilentlyContinue }
