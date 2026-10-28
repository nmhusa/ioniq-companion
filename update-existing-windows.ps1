param([string]$ProjectPath = (Get-Location).Path)
$ErrorActionPreference = 'Stop'
# Pinned, reviewed source snapshot. This script never reads or replaces HyundaiApi.java,
# Vault.java, signing keys, local-api.properties, local.properties or the debug keystore.
$revision = 'c362ae0c5bbafc9dad982b8586e6ca01d4015da3'
$project = (Resolve-Path $ProjectPath).Path
$javaRelative = 'app\src\main\java\com\ioniq\widget'
if (-not (Test-Path (Join-Path $project "$javaRelative\HyundaiApi.java"))) { throw 'Open PowerShell in your existing working IONIQ app project folder, then run this script again.' }
$gradlePath = Join-Path $project 'app\build.gradle'
if (-not (Test-Path $gradlePath)) { throw 'Expected app\build.gradle in this project.' }
$gradle = [IO.File]::ReadAllText($gradlePath)
if ($gradle -notmatch 'versionCode\s+\d+' -or $gradle -notmatch 'versionName\s+[''"][^''"]+[''"]') { throw 'Unrecognized build.gradle format; no files were changed.' }
$work = Join-Path ([IO.Path]::GetTempPath()) ('ioniq-update-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory $work | Out-Null
$backup = Join-Path $project ('.update-backups\' + (Get-Date -Format 'yyyyMMdd-HHmmss'))
try {
 $archive = Join-Path $work 'source.zip'
 Invoke-WebRequest "https://github.com/nmhusa/ioniq-companion/archive/$revision.zip" -OutFile $archive
 Expand-Archive $archive -DestinationPath $work
 $source = Join-Path $work "ioniq-companion-$revision"
 $files = @('app\src\main\AndroidManifest.xml', 'build-windows.ps1', 'build-release-windows.ps1', 'INSTALL.md', 'PUBLISH.md', 'RELEASE-0.7.5.md', 'PARKING.md')
 $files += Get-ChildItem (Join-Path $source $javaRelative) -Filter '*.java' | Where-Object { $_.Name -notin @('HyundaiApi.java','Vault.java') } | ForEach-Object { "$javaRelative\$($_.Name)" }
 $files += Get-ChildItem (Join-Path $source 'app\src\main\res') -File -Recurse | ForEach-Object { $_.FullName.Substring($source.Length + 1) }
 # Validate the complete download and take backups before replacing any project file.
 foreach ($relative in $files) { if (-not (Test-Path (Join-Path $source $relative))) { throw "Download is incomplete: $relative" } }
 New-Item -ItemType Directory -Force $backup | Out-Null
 foreach ($relative in ($files + 'app\build.gradle')) {
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
 $gradle = $gradle -replace 'versionCode\s+\d+', 'versionCode 12'
 $gradle = $gradle -replace 'versionName\s+[''"][^''"]+[''"]', "versionName '0.7.5'"
 [IO.File]::WriteAllText($gradlePath, $gradle, [Text.UTF8Encoding]::new($false))
 $ignore = Join-Path $project '.gitignore'
 if (-not (Test-Path $ignore) -or -not (Select-String -Path $ignore -SimpleMatch '.update-backups/' -Quiet)) { Add-Content $ignore "`n.update-backups/" }
 Write-Host "Updated app source to 0.7.5. Backup: $backup"
 Write-Host 'Your existing API configuration and signing files were preserved.'
 Write-Host 'Next, run: powershell -ExecutionPolicy Bypass -File .\build-windows.ps1'
} finally { Remove-Item $work -Recurse -Force -ErrorAction SilentlyContinue }
