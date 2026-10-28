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
