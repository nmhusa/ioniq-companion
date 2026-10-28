# 0.7.0

- Optional Park & return monitoring, selected Bluetooth disconnect cue, manual parking fallback, departure distance and 20-second dwell.
- Single lock attempt with a result notification; return reminder with a user-tapped Unlock action.
- Yellow/black account caution section with confirmations before account change or local-data erasure.
- Precise location and Nearby devices permissions only for the optional parking feature.
- Added parking decision tests. Vehicle/device acceptance testing remains required.

IONIQ Companion 0.6.0 — community preview for Hyundai US

- Vehicle status with report timestamp.
- Raised metallic controls; warm-red climate on and ice-blue climate off.
- Individual one-tap lock, unlock, climate on/off and charge on/off widgets.
- Command-result feedback and optional notifications.
- Screenshot capture enabled.

Android 8.0+. Requires a Hyundai US account with applicable active remote services. Version 0.2 connectivity was tested by the owner with a 2023 IONIQ 6. Maintainer: replace this sentence with actual 0.6.0 device-test results before publishing; do not imply broad compatibility.

One-tap widgets immediately send commands, including Unlock. Anyone using the unlocked home screen can operate them. “Confirmed” is Hyundai's response, not independent live vehicle verification. API availability may change.

First release-signed installation may require uninstalling the earlier debug build and signing in again. Future updates use the same release signing key.

Unofficial; not affiliated with Hyundai/Kia. US protocol adapted from Andy Fase's MIT-licensed egmp-bluelink-scriptable; attribution included.
