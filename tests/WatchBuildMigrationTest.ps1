$ErrorActionPreference = 'Stop'
$source = [IO.File]::ReadAllText((Join-Path $PSScriptRoot '..\update-existing-windows.ps1'))
$start = $source.IndexOf('# Prepare watch Gradle integration')
$end = $source.IndexOf('# End watch Gradle preparation.', $start)
if ($start -lt 0 -or $end -lt 0) { throw 'Watch Gradle migration missing' }
$migration = $source.Substring($start,$end-$start)
$project = Join-Path ([IO.Path]::GetTempPath()) ('watch-gradle-test-' + [guid]::NewGuid())
New-Item -ItemType Directory $project | Out-Null
try {
 $file = Join-Path $project 'settings.gradle'
 [IO.File]::WriteAllText($file,"include ':app'")
 $gradle = "android { /* LOCAL_CONFIG_SENTINEL */ }"
 Invoke-Expression $migration
 if (-not $gradle.Contains('LOCAL_CONFIG_SENTINEL') -or -not $gradle.Contains("project(':shared')") -or -not $gradle.Contains('play-services-wearable:20.0.1') -or -not $settings.Contains(":wear") -or -not $settings.Contains(":shared")) { throw 'Watch Gradle additions failed' }
 $firstGradle=$gradle; $firstSettings=$settings
 [IO.File]::WriteAllText($file,$settings)
 Invoke-Expression $migration
 if ($gradle -cne $firstGradle -or $settings -cne $firstSettings) { throw 'Watch Gradle migration is not idempotent' }
 Write-Output '2 watch Gradle migration checks passed'
} finally { Remove-Item $project -Recurse -Force }
