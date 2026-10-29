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

Download [update-existing-windows.ps1](update-existing-windows.ps1). Open PowerShell in the root of your existing working IONIQ project and run the downloaded script by its full path. It backs up replaced files, preserves local API credentials and signing files, and migrates the app/build configuration. Then run `powershell -ExecutionPolicy Bypass -File .\build-release-windows.ps1` with your existing signing key. Install `release-output\0.8.0\ioniq-companion-0.8.0.apk` over your signed phone app. Do not uninstall your working app. For the separate watch APK, follow [WATCH.md](WATCH.md).

The new top-of-page car photo has a transparent background and native status overlays; unknown odometer/range values are explicitly unavailable.

## Odometer fix (0.7.1)

The odometer uses the selected VIN’s enrollment vehicle details, with a numeric status-odometer fallback. Mileage is refreshed with normal status refresh and the hourly background job; its own Hyundai update timestamp appears below the header. A failed/missing mileage response retains the previous reading, marked last known. The public updater keeps existing local Hyundai API and credential files unchanged. Download the updated updater again, rebuild, install over your existing app, then tap **Refresh saved Hyundai status**.

## One-tap walk-away locking (0.7.2)

Tap **Walk-away lock** on the main screen beside your parked car, wait for **Parking saved**, then walk away. Tap the same button to cancel. Android permission prompts are first-use setup only. Choose 20 or 30 m in the distance settings; the old 100 m default becomes 30 m once. Close settings use a five-second dwell and a 5 m GPS-accuracy requirement. Uncertainty and network delay mean the actual locking distance can be larger. Manual arming does not require Bluetooth. Optional Bluetooth-triggered sessions still need monitoring started from the app; key-fob movement and door closure cannot directly arm this implementation.

## Vehicle-aware header (0.7.3)

The app is named IONIQ Companion. Selected vehicle details determine the IONIQ 5/6 heading, model year, car photo and dashboard widget title. No VIN decoding or nickname guessing is used. Unknown models use generic branding. Existing logins stay intact; on ordinary launch the app refreshes missing model details once per launch until a matched vehicle record is available. Display support for IONIQ 5 does not establish live API compatibility for every trim/year.

## Signed GitHub releases

See [PUBLISH.md](PUBLISH.md) for the local release-signing helper and [INSTALL.md](INSTALL.md) for recipient instructions. The release key stays on the maintainer PC. Public distribution remains pending credential/artwork authorization and signed-build device testing; CI artifacts are not working public releases.

## Compact action widgets (0.7.5)

Each action widget defaults to 1×1 and can resize horizontally/vertically. Small sizes show only the raised icon; larger tiles restore labels/results. Android 12+ responsive layouts adapt to launcher-provided sizes, including foldables. Actual grid sizes depend on the launcher. Commands and the larger vehicle dashboard are unchanged.

## Climate preset (0.7.6)

Tap the defrost/steering-heat preset button below the temperature slider. Save front defrost, rear defrost and steering-wheel heater choices; all default off. Choices apply with Climate on from the app or widget, not immediately when saved. They describe requests, not verified live heater state. No separate seat heating is implemented. The updater now backs up and narrowly patches the climate payload in the local HyundaiApi.java, preserving credential/configuration content. Unknown payload formats stop the updater before changes. Mapping source: https://github.com/Hacksore/bluelinky/pull/294 . Real car behavior remains to be verified.

## Clean presets and charge limits (0.7.7)

One Climate presets control opens Winter/Summer/Custom selection and editing. One Charge limits control opens reported AC/DC targets and a confirmation-protected editor. No new main-screen sliders or explanatory paragraphs. Existing settings migrate to Custom. The updater backs up the local API and inserts only the known payload/case changes; credentials and signing configuration stay intact.

US charge-limit protocol verified against Hyundai-Kia-Connect/hyundai_kia_connect_api, HyundaiBlueLinkApiUSA.py: `evc/charge/targetsoc/set`, `targetSOClist`, DC plug type 0 and AC plug type 1. No account was contacted during development. Endpoint/vehicle behavior still requires owner testing.

## Find My Car (0.7.8)

One compact control opens on-demand Hyundai location retrieval, map links and walking directions. A separate saved phone parking fix is retained when parking is marked. Both are tied to the selected vehicle and show source/time. No automatic location requests. See INSTALL.md and the updated privacy page. US endpoint reference: https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api/blob/master/hyundai_kia_connect_api/HyundaiBlueLinkApiUSA.py .

## Compact widget feedback (0.7.10)

Compact widgets now retain a status badge, with amber pending, green last-confirmed and amber attention feedback. Tap the icon to send, or the badge to inspect the full result/timestamp. Other widgets indicate Busy while a widget command is active. A local-only job refreshes interrupted feedback without a network request or command retry. Status is per command, not live vehicle state.


## Galaxy Watch companion (0.8.0 preview)

A Wear OS companion provides swipe-up/down pages and large one-tap controls through your paired phone. Enable Watch controls on the phone first. Phone unlock protection is respected. Both apps must be signed with the same existing key. See [WATCH.md](WATCH.md) for building and installing the separate watch APK.
