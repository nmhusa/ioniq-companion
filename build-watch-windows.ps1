param(
 [string]$KeyStore = (Join-Path ([Environment]::GetFolderPath('MyDocuments')) 'IONIQ-Signing\ioniq-release.p12')
)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
# Never accept/store passwords as script arguments. keytool/apksigner prompt locally.
$KeyStore = [IO.Path]::GetFullPath($KeyStore)
$projectRoot = [IO.Path]::GetFullPath($PSScriptRoot).TrimEnd('\') + '\'
if ($KeyStore.StartsWith($projectRoot, [StringComparison]::OrdinalIgnoreCase)) { throw 'Choose a keystore location outside the project.' }
if (-not (Test-Path $KeyStore)) { throw 'Signing key missing. Restore your existing key. Use the same key that signs your installed phone app.' }
& (Join-Path $PSScriptRoot 'build-windows.ps1') -PrepareOnly
$gradle = Join-Path $PSScriptRoot '.build-tools\gradle-8.9\bin\gradle.bat'
& $gradle --no-daemon :wear:assembleRelease :wear:lintRelease
if ($LASTEXITCODE -ne 0) { throw 'Release build/lint failed; no package prepared.' }
$unsigned = Join-Path $PSScriptRoot 'wear\build\outputs\apk\release\wear-release-unsigned.apk'
if (-not (Test-Path $unsigned)) { throw 'Expected unsigned release APK. Check for custom signing/build configuration.' }
$buildTools = Join-Path $env:ANDROID_HOME 'build-tools\34.0.0'
$badging = & "$buildTools\aapt.exe" dump badging $unsigned
if ($LASTEXITCODE -ne 0) { throw 'Could not inspect release APK.' }
$package = ($badging | Select-String "^package: name='com.ioniq.widget'" | Select-Object -First 1).ToString()
if ($package -notmatch "versionName='([0-9]+\.[0-9]+\.[0-9]+)'") { throw 'Unexpected package identity/version.' }
$version = $Matches[1]
if ($badging -match '^application-debuggable') { throw 'Refusing to package a debuggable APK.' }
$out = Join-Path $PSScriptRoot "watch-release-output\$version"
if (Test-Path $out) { throw 'Output already exists. Archive it outside release-output before rebuilding; do not overwrite a published release.' }
New-Item -ItemType Directory -Force $out | Out-Null
try {
 $aligned = Join-Path $out 'aligned-unsigned.apk'
 & "$buildTools\zipalign.exe" -f -p 4 $unsigned $aligned
 if ($LASTEXITCODE -ne 0) { throw 'APK alignment failed.' }
 $apkName = "ioniq-companion-watch-$version.apk"
 $apk = Join-Path $out $apkName
 & "$buildTools\apksigner.bat" sign --ks $KeyStore --ks-key-alias ioniq-release --out $apk $aligned
 if ($LASTEXITCODE -ne 0) { throw 'Signing failed.' }
 $certificate = & "$buildTools\apksigner.bat" verify --verbose --print-certs $apk
 if ($LASTEXITCODE -ne 0) { throw 'Signature verification failed.' }
 $certificate | Set-Content (Join-Path $out 'SIGNING-CERTIFICATE.txt') -Encoding UTF8
 $digest = (Get-FileHash $apk -Algorithm SHA256).Hash.ToLowerInvariant()
 "$digest  $apkName" | Set-Content (Join-Path $out 'SHA256SUMS.txt') -Encoding ASCII
 Copy-Item (Join-Path $PSScriptRoot 'WATCH.md') $out
 Copy-Item (Join-Path $PSScriptRoot "RELEASE-$version.md") (Join-Path $out 'RELEASE-NOTES.md')
 $mappingDir = Join-Path $PSScriptRoot "private-build-maps\$version\wear"
 New-Item -ItemType Directory -Force $mappingDir | Out-Null
 Copy-Item (Join-Path $PSScriptRoot 'wear\build\outputs\mapping\release\mapping.txt') (Join-Path $mappingDir ($digest + '-mapping.txt'))
 Remove-Item $aligned
 Write-Host "Signed package ready for LOCAL TESTING: $out"
 Write-Host 'Back up your keystore and password separately. Never upload either.'
 Write-Host 'Install this APK on the WATCH, not the phone. See WATCH.md for pairing/install steps.'
 Start-Process explorer.exe -ArgumentList $out
} catch {
 Remove-Item $out -Recurse -Force -ErrorAction SilentlyContinue
 throw
}
