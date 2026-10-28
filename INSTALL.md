# Install IONIQ Companion

Unofficial community preview for Hyundai US. Android 8.0 or later. Use your own Bluelink account and remote-service PIN with applicable active services. US 2023 IONIQ 6 connectivity has been owner-tested; IONIQ 5 display support is not proof of API compatibility with every model. This is not Hyundai Digital Key/NFC.

1. Download the APK attached to a release at https://github.com/nmhusa/ioniq-companion/releases — not the source ZIP or Actions debug artifact.
2. Open it on your Android phone. If prompted, allow installation from the browser/file manager you used. Restore that permission afterward if desired. Do not disable Play Protect to force installation past a security warning.
3. Open **Settings & help → Connect / change Bluelink account** and sign in with your own email, password and remote-service PIN. Select your vehicle and refresh status.
4. Add optional widgets from Settings & help. Individual action widgets send immediately, including Unlock; in-app tiles ask for confirmation.
5. Optional walk-away locking: arm beside the parked car, grant requested permissions, and wait for **Parking saved**. GPS and network delays affect lock distance. Confirm the result; this is not a guaranteed replacement for locking the car yourself. Returning offers a notification to unlock, never automatic unlock.

## Updates

Install newer releases from the same publisher over the existing app. Matching package identity and signing key preserve app data. If Android reports an incompatible signature, do not immediately uninstall: ask the maintainer which build you have.

The first switch from a debug-signed preview to the permanent release key requires uninstalling the debug app, which erases its login/settings. Stop parking monitoring and record your preferences first; then sign in again. Do not uninstall a working app just to test a download of unknown provenance.

## Verify a download on Windows

Run `Get-FileHash .\ioniq-companion-0.7.5.apk -Algorithm SHA256` and compare it with SHA256SUMS.txt attached to the same release. The certificate fingerprint is in SIGNING-CERTIFICATE.txt. A checksum detects a changed download; it does not independently establish publisher trust.

## Help and privacy

Report problems at https://github.com/nmhusa/ioniq-companion/issues. Include app version, phone/Android version and a redacted error. Never post passwords, PINs, tokens, VINs or your location. Read docs/privacy.html in the repository. Personal credentials are entered on each user's device and encrypted locally; requests go to Hyundai. No developer account/server login is needed. Hyundai's service availability and API behavior can change.

## Resize action widgets

The six action widgets default to one home-screen cell with just the colorful raised icon. Touch and hold a widget, then drag its resize handles to enlarge it. Larger tiles show the command label and latest result. The vehicle dashboard remains a larger information widget. Launcher grids control actual sizes; Samsung may need an existing widget removed and added again to pick up the new minimum size. Test folded and unfolded layouts. Compact widgets send the same immediate commands; read completion in the notification or app.
