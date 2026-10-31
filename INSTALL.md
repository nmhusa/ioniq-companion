# Install IONIQ Companion

For version 0.9.5, run the latest `update-existing-windows.ps1`, then `build-release-windows.ps1` with your existing signing key. Install `release-output/0.9.5/ioniq-companion-0.9.5.apk` over the current app. Tap the vehicle name to rename it; use the top-right switch button for another enrolled vehicle. If needed, refresh the list from Settings & help → Switch / refresh vehicles. Switching pauses auto-lock and watch controls until re-enabled for that car. No watch APK update is required.

Unofficial community preview for Hyundai US. Android 8.0 or later. Use your own Bluelink account and remote-service PIN with applicable active services. US 2023 IONIQ 6 connectivity has been owner-tested; IONIQ 5 display support is not proof of API compatibility with every model. This is not Hyundai Digital Key/NFC.

1. Download the APK attached to a release at https://github.com/nmhusa/ioniq-companion/releases — not the source ZIP or Actions debug artifact.
2. Open it on your Android phone. If prompted, allow installation from the browser/file manager you used. Restore that permission afterward if desired. Do not disable Play Protect to force installation past a security warning.
3. Open **Settings & help → Connect / change Bluelink account** and sign in with your own email, password and remote-service PIN. Select your vehicle and refresh status.
4. Add optional widgets from Settings & help. Individual action widgets send immediately, including Unlock; in-app tiles ask for confirmation.
5. Optional Bluetooth auto-lock: select the car’s own Bluetooth device in Bluetooth auto-lock, choose the delay and enable once. It runs on subsequent connection/disconnection cycles without trip-by-trip arming. See PARKING.md for cancellation and restart behavior.

## Updates

Install newer releases from the same publisher over the existing app. Matching package identity and signing key preserve app data. If Android reports an incompatible signature, do not immediately uninstall: ask the maintainer which build you have.

The first switch from a debug-signed preview to the permanent release key requires uninstalling the debug app, which erases its login/settings. Turn off Bluetooth auto-lock and record your preferences first; then sign in again. Do not uninstall a working app just to test a download of unknown provenance.

## Verify a download on Windows

Run `Get-FileHash .\ioniq-companion-0.7.12.apk -Algorithm SHA256` and compare it with SHA256SUMS.txt attached to the same release. The certificate fingerprint is in SIGNING-CERTIFICATE.txt. A checksum detects a changed download; it does not independently establish publisher trust.

## Help and privacy

Report problems at https://github.com/nmhusa/ioniq-companion/issues. Include app version, phone/Android version and a redacted error. Never post passwords, PINs, tokens, VINs or your location. Read docs/privacy.html in the repository. Personal credentials are entered on each user's device and encrypted locally; requests go to Hyundai. No developer account/server login is needed. Hyundai's service availability and API behavior can change.

## Resize action widgets

The six action widgets default to one home-screen cell with just the colorful raised icon. Touch and hold a widget, then drag its resize handles to enlarge it. Larger tiles show the command label and latest result. The vehicle dashboard remains a larger information widget. Launcher grids control actual sizes; Samsung may need an existing widget removed and added again to pick up the new minimum size. Test folded and unfolded layouts. Compact widgets send the same immediate commands; read completion in the notification or app.

## Presets and charging limits

Tap Climate presets to choose Winter, Summer or Custom. Edit presets opens temperature and heater controls. Save & use changes only your saved preset; press Climate on to send it. The climate widget uses the same selection. Your previous settings are retained as Custom.

Charge limits shows last-reported AC/DC targets and their timestamp. Read latest reads Hyundai's cached report; it may be old. Choose both targets, review and confirm to send once. Refresh and check the reported targets afterward; a command-completed response is not an independent reading. Changing a limit during charging can affect when that session ends.

## Find My Car

Tap Find my car below the car image to open Google Maps directly at the saved Hyundai coordinates. If no Hyundai location is saved, the app requests it once and then opens the map. Use the adjacent ⋯ button for timestamps, Update & open map, walking directions, the separately labeled phone parking spot, and Clear saved. There is no background location polling. Maps links share the chosen coordinates with Google. Your map app handles walking directions and any phone-location permission.

Hyundai-reported location may be stale even after an update; retrieval time is not the vehicle timestamp. A saved parking spot from an older version may remain available, but Bluetooth auto-lock does not capture phone GPS locations. Clear saved removes local location caches.

## Widget feedback (0.7.10)

The icon sends the command; the small badge beneath it opens details without sending a command. Amber Queued/Sending means the app is processing it. Green Last OK is Hyundai's acknowledgement of the last request, not a live locked/climate/charge state. Unknown or Check app means read the full result and check the official app before retrying. The remaining widgets show Busy and dim while one widget command runs; repeated taps during that time send nothing else.

Android may delay a widget repaint or the local recovery job after process termination. Opening IONIQ Companion refreshes these badges; no command is retried by this repaint. If an old widget does not pick up the new layout, remove and re-add that widget only. Do not uninstall the app.


## Unlock protection (0.7.11)

Open Settings & help → Unlock security to enable fingerprint or phone screen-lock authentication for unlocking. It starts off to preserve your current behavior. Lock and climate widgets remain one tap. Both enabling and disabling require phone authentication. If your home-screen app shortcut disappears after the launcher security change, add IONIQ Companion again from the app drawer; do not uninstall it.


## Find my car signals (0.7.12)

Tap ⋯ next to Find my car, then Flash lights or Horn & lights. Confirm with Send, or Cancel to send nothing. The main Find my car button still opens the map directly.


Vehicle colors: tap the car image, choose a palette and paint, then Save. See [color catalog](COLOR-CATALOG.md) for verified years and limitations.

Version 0.9.5 labels the top-right vehicle picker **Change vehicle**, opposite the vehicle name. It stays visible while connected, including when only one car is cached; use Refresh list in the picker to discover another registered car.

0.9.5: tap the 12V badge beside Range and Odometer for the auxiliary battery reading and report age.

Seat heating/ventilation: after Refresh status, open Climate presets → Edit presets. Controls appear only for compatible reported equipment; see [0.9.5](RELEASE-0.9.5.md).
