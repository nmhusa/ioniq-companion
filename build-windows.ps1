param([switch]$PrepareOnly)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$jdk17 = Get-ChildItem "$env:ProgramFiles\Eclipse Adoptium" -Directory -Filter 'jdk-17*' -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $jdk17) { throw 'Install Java 17 first: winget install --id EclipseAdoptium.Temurin.17.JDK --exact' }
$env:JAVA_HOME = $jdk17.FullName
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
if (-not (Test-Path "$env:ANDROID_HOME\platforms\android-35\android.jar")) { throw 'In Android Studio > SDK Manager, install Android 15 (API 35).' }
if (-not (Test-Path "$env:ANDROID_HOME\build-tools\34.0.0")) { throw 'In SDK Manager > SDK Tools > Show Package Details, install Android SDK Build-Tools 34.0.0.' }
$toolsDir = Join-Path $PSScriptRoot '.build-tools'
New-Item -ItemType Directory -Force $toolsDir | Out-Null
$gradleExe = Join-Path $toolsDir 'gradle-8.9\bin\gradle.bat'
if (-not (Test-Path $gradleExe)) {
 $zipPath = Join-Path $toolsDir 'gradle-8.9-bin.zip'
 Write-Host 'Downloading Gradle 8.9...'
 Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.9-bin.zip' -OutFile $zipPath
 $expected = (Invoke-RestMethod 'https://services.gradle.org/distributions/gradle-8.9-bin.zip.sha256').Trim()
 $actual = (Get-FileHash $zipPath -Algorithm SHA256).Hash
 if ($actual -ine $expected) { throw 'Gradle checksum mismatch. Download stopped.' }
 Expand-Archive $zipPath -DestinationPath $toolsDir -Force
 Remove-Item $zipPath
}
& $gradleExe wrapper --gradle-version 8.9 --distribution-type bin
if ($LASTEXITCODE -ne 0) { throw 'Gradle setup failed.' }
if ($PrepareOnly) { return }
& $gradleExe --no-daemon :app:assembleDebug :app:lintDebug
if ($LASTEXITCODE -ne 0) { throw 'Build or lint failed. Copy the error text back to ChatGPT.' }
Copy-Item 'app\build\outputs\apk\debug\app-debug.apk' 'app\build\outputs\apk\debug\ioniq-companion-v084.apk' -Force
Write-Host 'APK ready: app\build\outputs\apk\debug\ioniq-companion-v084.apk'
Start-Process explorer.exe -ArgumentList (Join-Path $PSScriptRoot 'app\build\outputs\apk\debug')
