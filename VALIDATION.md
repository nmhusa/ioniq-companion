# Version 0.3 validation

Passed: Java syntax parsing of all 14 sources; XML parsing; Java R resource references; manifest widget/provider mapping; action-specific PendingIntent identities; request guards and unlock endpoint/body inclusion.

Not run here: SDK compilation, Android lint, emulator/phone layout, widget taps, notification permission grant/denial, process kill during a command, or live unlock. Windows script runs compilation and lint. Test these device behaviors after building, including rapid repeat taps, disconnected-account handling, climate temperature persistence and notification results.

Prior evidence: user confirmed version 0.2 logs in and communicates with the vehicle. Version 0.3 retains that API flow and adds unlock as a separately selected command. Never interpret missing/failed completion as a confirmed vehicle action.

Version 0.4: all Java syntax and XML resources/references checked again after styling changes. Android rendering and build remain to be verified on the user PC/phone. Layered gradients and pressed selectors implement the raised appearance; no live 3D rendering.

0.6: adaptive icon XML/source syntax parsed, SVG preview rendered, website local links checked. Not published; no release-signed APK built yet.

## 0.7 verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40609614335 passed `:app:assembleDebug`, `:app:lintDebug` and all 18 ParkingRules checks on source commit 66b6a1bbe23d7b06b1d7a64f3e3011cdefe27e14. Local Java compilation against android-35 also passed. The header PNG has a real alpha channel, with pixel alpha values spanning 0–255. CI uses no Hyundai client credential, contacts no car and produces a test APK that cannot authenticate with Hyundai.

Samsung device, screen-off behavior, dongle events, actual remote commands, visual layout and the Windows updater have not been exercised on hardware here. Follow PARKING.md for device acceptance checks. The updater preserves existing local HyundaiApi/Vault and signing configuration, so the final locally configured APK must be built/linted again by build-windows.ps1.

## 0.7.1 verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40610964444 passed assembleDebug, lintDebug, 18 parking-rule checks and 14 mileage-parsing checks for commit 2d393e12df5eb7f270b4a0a3752b46935a628171. Local Android-35 Java compilation also passed. No real account/vehicle was contacted during these checks. The selected vehicle’s actual odometer display still needs confirmation after installation and a saved-status refresh.

## 0.7.2 verification

Owner confirmed 0.7.1 odometer working after installing the correct APK. GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40681169326 passed assembleDebug, lintDebug, 33 parking-rule checks and 14 mileage checks for commit ba1a9a41e67cb8a27b1e66f8b804be46df436470. The close-distance decision tests include GPS uncertainty, the five-second dwell, countdown cancellation, and preventing an immediate return reminder while walking away. On-phone 20/30 m behavior and Bluetooth sessions remain unverified; thresholds are estimates plus uncertainty and network delays.

## 0.7.3 verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40684163161 passed assembleDebug, lintDebug, 33 parking-rule checks, 14 mileage checks and 15 model-identification checks (62 total) for commit 0f949ae628b12c4be10313e373208cfc67805714. The IONIQ 5 PNG is 1448×1086 RGBA, matching the IONIQ 6 asset’s dimensions. Model tests cover spaced/compact/hyphenated names, 5 N, fallback modelName and unknown models. Real IONIQ 5 API connectivity and on-device vehicle switching have not been tested here. Application ID and credential key alias remain unchanged.


## 0.7.4 verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40690710968 passed assembleDebug, lintDebug and the existing 62 parking, mileage and model checks for commit ac40fbd451dc0784e016f5c947b34729eb8de725. Dashboard explanations, widgets and protected account controls are grouped under expandable Settings & help. Report timestamps and full command results are available by tapping their labels; routine successful results are shortened while errors and unconfirmed outcomes retain their full text. Relative report ages are shown only for timestamps with an explicit offset; ambiguous timestamps remain available in details without guessing a timezone. Parking readiness and failure text remain visible, with routine monitoring text shortened.

On-phone layout and interaction have not been exercised here. After installing, check Settings expand/collapse, report/result dialogs, both refresh options, walk-away readiness/cancellation, and account confirmation cancellation. Existing API credentials, application ID and signing configuration are preserved by the Windows updater. No vehicle commands were sent during validation.


## 0.7.5 and release tooling

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40710253457 passed debug and unsigned release assembly/lint, all existing 62 decision/parser checks, and Windows PowerShell syntax parsing for commit c362ae0c5bbafc9dad982b8586e6ca01d4015da3. Resource XML also parses locally. Single-cell action widgets use responsive compact/expanded RemoteViews on Android 12+ and an options-size fallback earlier. Commands remain unchanged. Samsung launcher resizing/folding and TalkBack have not been tested on-device here.

Local release-key generation/signing is prepared but has not been executed on the owner's PC. No permanent key, working signed release APK or public GitHub release was created here. Credential/artwork redistribution and signed-build device validation remain outstanding; CI has no Hyundai credential and sent no commands.


## 0.7.6 climate preset verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40711797691 passed debug/release assembly and lint, Windows PowerShell parsing, and 66 checks including all four rear-defrost/steering-heat enum combinations. The updater's narrow payload substitution was checked against the original local adapter and round-tripped without changing other source content. All options default off. No vehicle commands were issued; heater behavior, actual device UI and signed-release installation remain to be verified on the owner's phone/car.


## 0.7.7 verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40728765351 passed debug/release assembly and lint, 116 Java checks and four Windows updater-migration checks, plus PowerShell parsing. AC/DC validation covers all 36 supported pairs, swapped-channel protection and invalid values. Migration checks cover the original adapter, the 0.7.6 adapter, repeat updates and rejecting unknown formats without writes. No personal credentials or live vehicle were used. Source commit: 5f565f1401c7cd9a27d78bcb69e55dd9507acd85.

Winter/Summer/Custom selection and editing stay in dialogs; existing values migrate to Custom without changing the active climate settings. Charge limits use the US targetsoc/set endpoint and parse last-reported targets from evStatus.reservChargeInfos.targetSOClist. The reported timestamp remains visible inside the editor; missing targets require explicit selection. Live charge-limit support, on-phone dialog appearance and preset/widget operation still need owner testing. The owner previously confirmed the 0.7.6 signed APK installs and connects successfully; preserve that same release key for this update.


## 0.7.8 Find My Car verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40749147511 passed debug/release assembly and lint, 131 Java checks (including 15 coordinate/map-link cases), four updater migration checks and Windows script parsing. Commit: 192067f5545c492706809b1b55bb6403b4efa17e. Coordinates reject missing/non-finite/out-of-range values; zero coordinates are accepted as real values, not filled in by default. Cached location is associated with the selected VIN, and bad/missing reports retain existing caches. Maps links specify the selected destination and walking mode where requested.

No real account, vehicle location or map navigation was exercised. Test Hyundai lookup success/failure, timestamp presentation, marking/retaining/clearing parking, account/vehicle changes, browser/map fallback and signed update on the phone. The retained parking fallback is new in this release; a new parking mark is required after installation. Privacy documentation now covers persistent local coordinates and user-triggered sharing with Google Maps.


## 0.7.9 direct map navigation

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40750654283 passed debug/release assembly and lint, all 135 existing Java/migration checks and script parsing. Source e3defc31c390a50b31c72f90d58383fc05595bdf. Find My Car opens cached Hyundai coordinates directly, or requests them once if absent and then opens the map. Timestamps/refresh and phone-parking fallback remain in the adjacent details menu. Phone/browser handoff and actual address rendering have not been tested on-device; Google Maps controls available address information. No live vehicle requests were sent during validation.


## 0.7.10 — compact widget feedback

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40776216811 passed debug/release assembly and lint, Java regression tests (including 16 widget feedback assertions), Windows script parsing, and updater migration tests. Source: `c589e997a890506a72ae86204e54605f0ba6e7be`.

Widgets display queued/sending/last-confirmed/unknown/error states and open full timestamped details from their badge. Repeated widget taps during an active command do not submit another command. Interrupted pending commands become unknown on redraw; a local-only scheduled job requests a recovery redraw, subject to Android scheduling. Last OK describes the previous command acknowledgement, not live vehicle state.

No real vehicle commands were issued during validation. Samsung launcher rendering, background scheduling, and on-device command transitions still require device testing.


## 0.7.11 — local security hardening

Source commit `8487a3d36291f06860568b14d5bf181fc9c7eba1`; GitHub Actions https://github.com/nmhusa/ioniq-companion/actions/runs/40861557269 passed debug/release assembly and lint, Windows syntax/migration checks, existing regression tests, and 17 new cryptographic/permit/error-redaction assertions.

An API 30 Android emulator passed 27 instrumentation checks: legacy plaintext migration and deletion, typed encrypted preference round-trips using Android Keystore and a newly constructed reader, non-clobbering editor updates, deletion, rejection of corrupted encrypted data without plaintext fallback, manifest component export restrictions, and rejection of a stale unlock intent lacking an authentication permit. Test payloads were synthetic. No Hyundai credentials or real vehicle commands were used.

Remaining device checks: Samsung biometric/PIN prompt success and cancellation, launcher/widget behavior after upgrading, actual vehicle operations, and background scheduling. This is not a penetration test or a claim that the embedded Hyundai client credential is secret. Existing password storage remains encrypted and unchanged for protocol compatibility. Existing vault key alias and ciphertext format are preserved.


## 0.7.12 — Find my car lights and horn

Source `1a13fa0245cddf720ee07a27301980ee35aaf9f9`. GitHub Actions https://github.com/nmhusa/ioniq-companion/actions/runs/40887206259 passed debug/release builds and lint, Windows updater syntax/migration tests, 13 new command-route/polling checks, existing regressions and Android security instrumentation. Requests and payload names were checked against Hyundai-Kia-Connect/hyundai_kia_connect_api's US implementation. Flash lights uses rcs/rhl/light and LIGHTS_ONLY; horn and lights uses rcs/rhl/hnl and HORN_AND_LIGHTS. Existing commands retain REMOTE_POLL.

The Find my car main button retains direct-map behavior. Its adjacent menu contains the two signal actions, each with Send/Cancel confirmation. The app does not implement horn-only, signal duration configuration or a stop-horn action. No live requests were made to a vehicle; real-car behavior and Samsung UI verification remain user device checks.


## 0.7.13 — signal menu styling

Source `cc135d9b73a43f8086a42d2fa7bd141d2ed35a3a`; workflow https://github.com/nmhusa/ioniq-companion/actions/runs/40891299898 passed debug/release builds and lint, Windows updater checks, existing regression tests and Android security instrumentation. Flash lights is bold amber; Horn & lights is bold red, using native styled menu text. No command or authentication behavior changed. Samsung visual appearance remains an on-device check.


## 0.8.0 — Galaxy Watch companion preview

Source `9f91753cf0a1a711a4f0c2cbe139583db177f77a`; workflow https://github.com/nmhusa/ioniq-companion/actions/runs/40908137758 passed phone and watch debug/release assembly and lint, Windows script syntax and updater migration checks, shared watch protocol tests, and existing regression tests.

An API 30 emulator passed 51 phone storage/component/watch-receipt checks and 15 compact watch UI checks at 450×450 pixels, density 320. Tests cover opt-in/session scope, duplicate and rejected request receipts, process interruption, protected unlock, disabled controls, touch target bounds, swipe navigation without command activation, and recreation. Synthetic payloads only; no Hyundai credentials or real vehicle commands were used.

The watch sends transient requests through the paired phone and contains no Hyundai credentials. Both APKs require the same package name and signing key. This is a preview: actual Samsung round-screen appearance, Wear OS pairing/message delivery, phone background service behavior, and live vehicle command results still require paired-device testing. The emulator is not a substitute for that testing. No standalone LTE commands, map view, tiles, complications, or bezel navigation are implemented.


## 0.8.1 — release obfuscation and credential masking

Source `e45b55dad7318b589fe23951ad7966fc1c7ed4fe`; https://github.com/nmhusa/ioniq-companion/actions/runs/40918496276 passed phone/watch debug and release builds and lint, Windows migration tests, existing regressions, and Android instrumentation (51 phone checks and 15 compact watch checks). The obfuscated release APKs were signed with a disposable CI key and both launched successfully on an API 30 emulator, with live processes after startup.

New checks verify UTF-8 credential decoding, empty/mismatched data handling, local quoted-credential migration, repeat-update stability and rejection of unknown declarations. Release inspection verifies that HyundaiApi is renamed in R8 mapping and that a synthetic credential does not appear in any decompressed APK entry. Actual credentials are never supplied to CI. These checks demonstrate an extraction deterrent, not secrecy: masked bytes and the decoding data both ship in the APK. API endpoints remain discoverable and decoded values exist at runtime.

The updater preserves signing identity and locally masks the existing quoted credential; private backups can still contain the original value. Build maps are stored outside distribution folders under ignored private-build-maps/. Watch 0.8.0 is protocol-compatible with phone 0.8.1. Live login, widgets, car commands and phone/watch interaction after the release update remain owner-device checks; no real vehicle commands were issued during validation.


## 0.8.2 — replace GPS walk-away with automatic Bluetooth lock

Source `b91012fd82aaef63dc5fdea1505f0e3f3b66e4be`; workflow https://github.com/nmhusa/ioniq-companion/actions/runs/40947042105 passed phone/watch debug and release builds, lint, Windows updater syntax/migrations, credential-obfuscation inspection, pure Bluetooth countdown regression tests, and existing regressions. API 30 instrumentation passed 60 phone storage/component checks and 15 watch UI checks. Both obfuscated release apps launched successfully.

The new in-memory state machine requires an observed car connection before disconnect; rejects duplicate disconnects and duplicate consumption; cancels queued work on reconnection/stop; expires delayed execution; and cannot restore a countdown after restart. Instrumentation checks private service/receiver/activity components, connectedDevice foreground type, rejection of a stale command without API/action-state changes, and settings-screen startup. The stale-command test found and fixed an Android foreground-service rejection-path crash. Test data is synthetic; no vehicle commands or Hyundai credentials were used.

Automatic monitoring is opt-in once, can resume after boot/package replacement/system restart when Android permits, and never persists pending commands. Old GPS services/rules are removed, and the updater backs up and deletes obsolete source files. The car must be selected anew, so an old dongle selection is not silently reused. GPS permissions are removed; existing historical parking-location caches may remain until cleared. Account/vehicle changes cancel unsent work. Phone 0.8.2 remains compatible with watch 0.8.0/0.8.1.

Physical-device testing remains necessary for Samsung Bluetooth event delivery, connection detection when already in the car, background/boot behavior and actual car locking. Bluetooth loss does not establish that the vehicle is parked, doors are closed or a key is outside. A command already sent cannot be recalled.


## 0.8.3 compact phone layout

Source commit `ba389cb11fd71771afe6249d07145d2e9b688ee8`; [CI run 40963391676](https://github.com/nmhusa/ioniq-companion/actions/runs/40963391676) passed all jobs: phone/watch debug and release builds and lint, release obfuscation checks, Windows script parsing and migration checks, pure regression checks, Android instrumentation, and obfuscated release launch smoke tests. Changes affect phone layout and version metadata, not command dispatch or Bluetooth locking rules. No live vehicle commands were sent. Physical Samsung layout and enlarged-text review remain owner checks.


## 0.8.9 vehicle identity and switching

Source `a4e238c0ad5df8ed887a2b2220d065bd1588c1c1`; [CI run 41095663899](https://github.com/nmhusa/ioniq-companion/actions/runs/41095663899) passed all three jobs. Phone/watch builds and lint, release obfuscation, PowerShell parsing/migration, model identification (including 9 and rejection of 90), Android encrypted nickname/account isolation and vehicle-selection checks, queued old-vehicle command rejection, existing storage and component checks, watch tests and release launch smoke checks passed. Synthetic accounts only; no live car commands sent. Real multi-car accounts and IONIQ 9 API compatibility remain owner validation.
