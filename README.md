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

This produces a debug APK for testing, not a public release-signed APK. Use PUBLISH.md to sign a release. GitHub Actions also builds/lints a credential-free debug artifact that cannot connect to Hyundai; its signing key is ephemeral and it does not automatically publish a release.

## Attribution

US API protocol adapted from [andyfase/egmp-bluelink-scriptable](https://github.com/andyfase/egmp-bluelink-scriptable). Original MIT license and copyright preserved in LICENSE. Unaffiliated with Hyundai/Kia; trademarks belong to their owners.

## Preview 0.7: Park & return

Opt-in, user-started parking monitoring for Android. Select the paired car/dongle and start before driving, or tap **I’ve parked here** beside the car. A usable Bluetooth disconnect and a recent slow-moving GPS fix can mark parking. After departure beyond the selected radius plus GPS uncertainty and a 20-second dwell, the app attempts locking once. Return proximity offers a one-tap Unlock notification; it never automatically unlocks.

Phone-only detection is approximate and needs on-device testing. A dongle that uses Wi-Fi or drops Bluetooth during normal Android Auto operation may not supply a usable cue; use manual parking in that case. Force-stop, reboot, revoked permissions and battery restrictions can stop monitoring. Start a new session after that. See [parking setup and tests](PARKING.md).

Account-change and erase actions sit in a yellow/black caution section, with explicit confirmation. Cancelling either warning changes nothing.

## Updating an existing working Windows project

Download [update-existing-windows.ps1](update-existing-windows.ps1). Open PowerShell in the root of your existing working IONIQ project and run the downloaded script by its full path. It backs up changed UI/resource files, preserves your local HyundaiApi.java, Vault.java, API configuration and signing files, and updates the app version. Then run `powershell -ExecutionPolicy Bypass -File .\build-windows.ps1`. Install the resulting `ioniq6-v07.apk` over the existing app using the same signing key; do not uninstall if you want to preserve login data.

The new top-of-page car photo has a transparent background and native status overlays; unknown odometer/range values are explicitly unavailable.
