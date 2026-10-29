param(
 [string]$KeyStore = (Join-Path ([Environment]::GetFolderPath('MyDocuments')) 'IONIQ-Signing\ioniq-release.p12'),
 [switch]$CreateKey
)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
# Never accept/store passwords as script arguments. keytool/apksigner prompt locally.
$KeyStore = [IO.Path]::GetFullPath($KeyStore)
$projectRoot = [IO.Path]::GetFullPath($PSScriptRoot).TrimEnd('\') + '\'
if ($KeyStore.StartsWith($projectRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Choose a keystore location outside the project.' }
if (-not (Test-Path $KeyStore) -and -not $CreateKey) { throw 'Signing key missing. Restore your existing key. For your FIRST release only, run with -CreateKey.' }
& (Join-Path $PSScriptRoot 'build-windows.ps1') -PrepareOnly
if (-not (Test-Path $KeyStore)) {
 New-Item -ItemType Directory -Force (Split-Path $KeyStore) | Out-Null
 Write-Host 'Creating your permanent release key. Save its password in your password manager.'
 & "$env:JAVA_HOME\bin\keytool.exe" -genkeypair -keystore $KeyStore -storetype PKCS12 -alias ioniq-release -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=IONIQ Companion'
 if ($LASTEXITCODE -ne 0) { throw 'Key creation failed. Do not delete an existing key.' }
}
$gradle = Join-Path $PSScriptRoot '.build-tools\gradle-8.9\bin\gradle.bat'
& $gradle --no-daemon :app:assembleRelease :app:lintRelease
if ($LASTEXITCODE -ne 0) { throw 'Release build/lint failed; no package prepared.' }
$unsigned = Join-Path $PSScriptRoot 'app\build\outputs\apk\release\app-release-unsigned.apk'
if (-not (Test-Path $unsigned)) { throw 'Expected unsigned release APK. Check for custom signing/build configuration.' }
$buildTools = Join-Path $env:ANDROID_HOME 'build-tools\34.0.0'
$badging = & "$buildTools\aapt.exe" dump badging $unsigned
if ($LASTEXITCODE -ne 0) { throw 'Could not inspect release APK.' }
$package = ($badging | Select-String "^package: name='com.ioniq.widget'" | Select-Object -First 1).ToString()
if ($package -notmatch "versionName='([0-9]+\.[0-9]+\.[0-9]+)'") { throw 'Unexpected package identity/version.' }
$version = $Matches[1]
if ($badging -match '^application-debuggable') { throw 'Refusing to package a debuggable APK.' }
$out = Join-Path $PSScriptRoot "release-output\$version"
if (Test-Path $out) { throw 'Output already exists. Archive it outside release-output before rebuilding; do not overwrite a published release.' }
New-Item -ItemType Directory -Force $out | Out-Null
try {
 $aligned = Join-Path $out 'aligned-unsigned.apk'
 & "$buildTools\zipalign.exe" -f -p 4 $unsigned $aligned
 if ($LASTEXITCODE -ne 0) { throw 'APK alignment failed.' }
 $apkName = "ioniq-companion-$version.apk"
 $apk = Join-Path $out $apkName
 & "$buildTools\apksigner.bat" sign --ks $KeyStore --ks-key-alias ioniq-release --out $apk $aligned
 if ($LASTEXITCODE -ne 0) { throw 'Signing failed.' }
 $certificate = & "$buildTools\apksigner.bat" verify --verbose --print-certs $apk
 if ($LASTEXITCODE -ne 0) { throw 'Signature verification failed.' }
 $certificate | Set-Content (Join-Path $out 'SIGNING-CERTIFICATE.txt') -Encoding UTF8
 $digest = (Get-FileHash $apk -Algorithm SHA256).Hash.ToLowerInvariant()
 "$digest  $apkName" | Set-Content (Join-Path $out 'SHA256SUMS.txt') -Encoding ASCII
 Copy-Item (Join-Path $PSScriptRoot 'INSTALL.md') $out
 Copy-Item (Join-Path $PSScriptRoot "RELEASE-$version.md") (Join-Path $out 'RELEASE-NOTES.md')
 $mappingDir = Join-Path $PSScriptRoot "private-build-maps\$version\app"
 New-Item -ItemType Directory -Force $mappingDir | Out-Null
 Copy-Item (Join-Path $PSScriptRoot 'app\build\outputs\mapping\release\mapping.txt') (Join-Path $mappingDir ($digest + '-mapping.txt'))
 Remove-Item $aligned
 Write-Host "Signed package ready for LOCAL TESTING: $out"
 Write-Host 'Back up your keystore and password separately. Never upload either.'
 Write-Host 'Before publication: resolve API credential redistribution, test this signed APK, and complete PUBLISH.md.'
 Start-Process explorer.exe -ArgumentList $out
} catch {
 Remove-Item $out -Recurse -Force -ErrorAction SilentlyContinue
 throw
}
