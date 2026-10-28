# 0.7.3

- App/launcher name is now IONIQ Companion; existing package identity and login storage are unchanged.
- Selected VIN’s model details choose the IONIQ 5 or IONIQ 6 heading, model year and transparent car photo. Dashboard widget and command notification titles follow the selected car.
- Existing installations fetch missing model details on ordinary app launch. Unknown models display generic branding, not an assumed IONIQ 6.
- Added the supplied silver IONIQ 5 image as a transparent cutout and model-identification tests. IONIQ 5 live connectivity is not verified by these display changes.

# 0.7.2

- Main-screen one-tap Walk-away lock control, with active status and one-tap Stop.
- Added 20/30/50 m settings; old 100 m default migrates to 30 m.
- Close settings use a five-second dwell and require GPS accuracy within 5 m; uncertainty margin still applies.
- Manual parking needs no Bluetooth permission and ignores Bluetooth connection changes. Bluetooth-triggered mode remains optional.
- Smaller return radius prevents departure from immediately producing an Unlock reminder at short distances.
- Added 15 short-distance decision checks. Real Samsung/car validation remains required.

# 0.7.1

- Read odometer from the selected VIN’s US enrollment vehicle details; numeric status odometer remains a fallback.
- Show the separate odometer-update timestamp. Preserve last known mileage with a label when details refresh fails.
- Normal status refresh remains usable if the mileage request fails. Account erasure clears cached mileage.
- Existing local HyundaiApi.java and credential configuration remain unchanged by the updater.

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
