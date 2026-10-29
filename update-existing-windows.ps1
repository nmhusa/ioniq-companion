param([string]$ProjectPath = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
# Pinned source. Known climate payload and charge-limit case are patched in HyundaiApi.java.
# Credentials and signing configuration are preserved; API source is never uploaded.
# Vault keeps the same credential key and format; only its cache cleanup changes.
$revision = '9f91753cf0a1a711a4f0c2cbe139583db177f77a'
$project = (Resolve-Path $ProjectPath).Path
$javaRelative = 'app\src\main\java\com\ioniq\widget'
if (-not (Test-Path (Join-Path $project "$javaRelative\HyundaiApi.java"))) { throw 'Open PowerShell in your existing working IONIQ app project folder, then run this script again.' }
$gradlePath = Join-Path $project 'app\build.gradle'
if (-not (Test-Path $gradlePath)) { throw 'Expected app\build.gradle in this project.' }
$gradle = [IO.File]::ReadAllText($gradlePath)
if ($gradle -notmatch 'versionCode\s+\d+' -or $gradle -notmatch 'versionName\s+[''"][^''"]+[''"]') { throw 'Unrecognized build.gradle format; no files were changed.' }
# Prepare watch Gradle integration without replacing local API/signing settings.
$settingsPath = Join-Path $project 'settings.gradle'
if (-not (Test-Path $settingsPath)) { throw 'Expected settings.gradle; no files changed.' }
$settings = [IO.File]::ReadAllText($settingsPath)
foreach ($module in @('shared','wear')) { if (-not $settings.Contains(":" + $module)) { $settings += "`ninclude ':$module'`n" } }
if (-not $gradle.Contains("implementation project(':shared')")) { $gradle += "`ndependencies { implementation project(':shared') }`n" }
if (-not $gradle.Contains('com.google.android.gms:play-services-wearable:')) { $gradle += "`ndependencies { implementation 'com.google.android.gms:play-services-wearable:20.0.1' }`n" }
$propertiesPath = Join-Path $project 'gradle.properties'
$properties = if (Test-Path $propertiesPath) { [IO.File]::ReadAllText($propertiesPath) } else { '' }
if ($properties -match '(?m)^\s*android.useAndroidX\s*=') { $properties = $properties -replace '(?m)^\s*android.useAndroidX\s*=.*$', 'android.useAndroidX=true' }
else { $properties += "`nandroid.useAndroidX=true`n" }
if (-not $gradle.Contains("apply from: rootProject.file('release-hardening.gradle')")) { $gradle += "`napply from: rootProject.file('release-hardening.gradle')`n" }
# End watch Gradle preparation.
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
# Route the two legacy status-cache accesses through encrypted storage; preserve API credentials.
$patchedApi = $patchedApi.Replace('context.getSharedPreferences("status",0)', 'SecurePrefs.get(context,"status")')
# Add explicit US light and horn/light commands, keeping the local API credential intact.
$signalCase = '   case "lights":case "horn_lights":path=VehicleSignal.path(action);body=new JSONObject().put("userName",auth.getString("email")).put("vin",auth.getString("vin"));break;'
if (-not $patchedApi.Contains($signalCase)) {
 if ($patchedApi.Contains('case "lights"') -or $patchedApi.Contains('case "horn_lights"') -or [regex]::Matches($patchedApi, [regex]::Escape($defaultCase)).Count -ne 1) { throw 'Unrecognized signal-command switch; no files changed.' }
 $patchedApi = $patchedApi.Replace($defaultCase, $signalCase + "`n" + $defaultCase)
}
$oldPoll = '"service_type","REMOTE_POLL"'
$newPoll = '"service_type",VehicleSignal.serviceType(action)'
if (-not $patchedApi.Contains($newPoll)) {
 if ([regex]::Matches($patchedApi, [regex]::Escape($oldPoll)).Count -ne 1) { throw 'Unrecognized command confirmation headers; no files changed.' }
 $patchedApi = $patchedApi.Replace($oldPoll,$newPoll)
}

# Prepare credential masking locally; never print the original value.
$credentialHeader = 'h.setRequestProperty("clientSecret",'
$literalPattern = 'h\.setRequestProperty\("clientSecret",\s*"([^"\\\r\n]*)"\s*\);'
$literalMatches = [regex]::Matches($patchedApi,$literalPattern)
if ($literalMatches.Count -eq 1) {
 $credentialBytes = [Text.Encoding]::UTF8.GetBytes($literalMatches[0].Groups[1].Value)
 $padBytes = New-Object byte[] $credentialBytes.Length
 $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
 try { $rng.GetBytes($padBytes) } finally { $rng.Dispose() }
 $maskedValues = @(); $padValues = @()
 for ($i=0; $i -lt $credentialBytes.Length; $i++) { $maskedValues += ([int]$credentialBytes[$i] -bxor [int]$padBytes[$i]); $padValues += [int]$padBytes[$i] }
 $replacement = $credentialHeader + 'ClientCredential.decode(new int[]{' + ($maskedValues -join ',') + '},new int[]{' + ($padValues -join ',') + '}));'
 $patchedApi = $patchedApi.Replace($literalMatches[0].Value,$replacement)
 [Array]::Clear($credentialBytes,0,$credentialBytes.Length)
} elseif ($patchedApi.Contains($credentialHeader + 'BuildConfig.HYUNDAI_CLIENT_SECRET);')) {
 # Repository-based projects can read ignored local-api.properties / environment at build time.
 $patchedApi = $patchedApi.Replace('BuildConfig.HYUNDAI_CLIENT_SECRET.isEmpty()','BuildConfig.CLIENT_MASKED.length==0')
 $patchedApi = $patchedApi.Replace($credentialHeader + 'BuildConfig.HYUNDAI_CLIENT_SECRET);',$credentialHeader + 'ClientCredential.decode(BuildConfig.CLIENT_MASKED,BuildConfig.CLIENT_PAD));')
} elseif (-not $patchedApi.Contains($credentialHeader + 'ClientCredential.decode(')) {
 throw 'Unrecognized credential declaration; no files changed. Keep HyundaiApi.java private.'
}
# End credential masking.
$work = Join-Path ([IO.Path]::GetTempPath()) ('ioniq-update-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory $work | Out-Null
$backup = Join-Path $project ('.update-backups\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
try {
 $archive = Join-Path $work 'source.zip'
 Invoke-WebRequest "https://github.com/nmhusa/ioniq-companion/archive/$revision.zip" -OutFile $archive
 Expand-Archive $archive -DestinationPath $work
 $source = Join-Path $work "ioniq-companion-$revision"
 $files = @('app\src\main\AndroidManifest.xml', 'build-windows.ps1', 'build-release-windows.ps1', 'build-watch-windows.ps1', 'WATCH.md', 'INSTALL.md', 'PUBLISH.md', 'RELEASE-0.8.1.md', 'PARKING.md', 'release-hardening.gradle', 'release-rules.pro')
 $files += Get-ChildItem (Join-Path $source $javaRelative) -Filter '*.java' | Where-Object { $_.Name -notin @('HyundaiApi.java') } | ForEach-Object { "$javaRelative\$($_.Name)" }
 $files += Get-ChildItem (Join-Path $source 'app\src\main\res') -File -Recurse | ForEach-Object { $_.FullName.Substring($source.Length + 1) }
 foreach ($module in @('shared','wear')) { $files += Get-ChildItem (Join-Path $source $module) -File -Recurse | ForEach-Object { $_.FullName.Substring($source.Length + 1) } }
 # Validate the complete download and take backups before replacing any project file.
 foreach ($relative in $files) { if (-not (Test-Path (Join-Path $source $relative))) { throw "Download is incomplete: $relative" } }
 New-Item -ItemType Directory -Force $backup | Out-Null
 foreach ($relative in ($files + @('app\build.gradle', 'settings.gradle', 'gradle.properties', "$javaRelative\HyundaiApi.java"))) {
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
 $gradle = $gradle -replace 'versionCode\s+\d+', 'versionCode 22'
 $gradle = $gradle -replace 'versionName\s+[''"][^''"]+[''"]', "versionName '0.8.1'"
 [IO.File]::WriteAllText($gradlePath, $gradle, [Text.UTF8Encoding]::new($false))
 [IO.File]::WriteAllText($settingsPath, $settings, [Text.UTF8Encoding]::new($false))
 [IO.File]::WriteAllText($propertiesPath, $properties, [Text.UTF8Encoding]::new($false))
 $ignore = Join-Path $project '.gitignore'
 if (-not (Test-Path $ignore) -or -not (Select-String -Path $ignore -SimpleMatch '.update-backups/' -Quiet)) { Add-Content $ignore "`n.update-backups/" }
 if (-not (Select-String -Path $ignore -SimpleMatch 'private-build-maps/' -Quiet)) { Add-Content $ignore "`nprivate-build-maps/" }
 Write-Host "Updated app source to 0.8.1. Backup: $backup"
 Write-Host 'Your existing API configuration and signing files were preserved.'
 Write-Host 'For the watch APK, also run: powershell -ExecutionPolicy Bypass -File .\build-watch-windows.ps1'
 Write-Host 'Next, run: powershell -ExecutionPolicy Bypass -File .\build-release-windows.ps1'
} finally { Remove-Item $work -Recurse -Force -ErrorAction SilentlyContinue }
