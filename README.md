# IONIQ Companion

Unofficial Android companion for Hyundai US, with vehicle status and six colorful home-screen controls.

**Preview software.** Owner-tested connection with a US 2023 Hyundai IONIQ 6 on version 0.2. Later widget/UI changes still require release-device validation. Android 8.0+. Other vehicles/regions are not verified.

- Lock/unlock doors; start/stop climate and charging.
- Six separate one-tap widgets and a larger status widget.
- Saved climate temperature, timestamps and command confirmation feedback.
- Raised metallic icon theme and screenshot support.
- Direct HTTPS requests to Hyundai; encrypted local credential storage; no analytics.

Individual widgets send immediately, including Unlock. In-app tiles ask for confirmation. A successful build does not prove vehicle commands work.

[Build instructions](BUILD_GUIDE.md) · [Publishing checklist](PUBLISH.md) · [Release notes](RELEASE_NOTES.md) · [Privacy](docs/privacy.html)

## Build

On Windows with Temurin JDK 17, SDK Platform 35 and Build-Tools 34.0.0 installed:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build-windows.ps1
```

This produces a debug APK for testing, not a public release-signed APK. Use PUBLISH.md to sign a release. GitHub Actions also builds/lints a debug artifact; its signing key is ephemeral and it does not automatically publish a release.

## Attribution

US API protocol adapted from [andyfase/egmp-bluelink-scriptable](https://github.com/andyfase/egmp-bluelink-scriptable). Original MIT license and copyright preserved in LICENSE. Unaffiliated with Hyundai/Kia; trademarks belong to their owners.
