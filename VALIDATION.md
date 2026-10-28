# Version 0.3 validation

Passed: Java syntax parsing of all 14 sources; XML parsing; Java R resource references; manifest widget/provider mapping; action-specific PendingIntent identities; request guards and unlock endpoint/body inclusion.

Not run here: SDK compilation, Android lint, emulator/phone layout, widget taps, notification permission grant/denial, process kill during a command, or live unlock. Windows script runs compilation and lint. Test these device behaviors after building, including rapid repeat taps, disconnected-account handling, climate temperature persistence and notification results.

Prior evidence: user confirmed version 0.2 logs in and communicates with the vehicle. Version 0.3 retains that API flow and adds unlock as a separately selected command. Never interpret missing/failed completion as a confirmed vehicle action.

Version 0.4: all Java syntax and XML resources/references checked again after styling changes. Android rendering and build remain to be verified on the user PC/phone. Layered gradients and pressed selectors implement the raised appearance; no live 3D rendering.

0.6: adaptive icon XML/source syntax parsed, SVG preview rendered, website local links checked. Not published; no release-signed APK built yet.

## 0.7 verification

GitHub Actions run https://github.com/nmhusa/ioniq-companion/actions/runs/40609614335 passed `:app:assembleDebug`, `:app:lintDebug` and all 18 ParkingRules checks on source commit 66b6a1bbe23d7b06b1d7a64f3e3011cdefe27e14. Local Java compilation against android-35 also passed. The header PNG has a real alpha channel, with pixel alpha values spanning 0–255. CI uses no Hyundai client credential, contacts no car and produces a test APK that cannot authenticate with Hyundai.

Samsung device, screen-off behavior, dongle events, actual remote commands, visual layout and the Windows updater have not been exercised on hardware here. Follow PARKING.md for device acceptance checks. The updater preserves existing local HyundaiApi/Vault and signing configuration, so the final locally configured APK must be built/linted again by build-windows.ps1.
