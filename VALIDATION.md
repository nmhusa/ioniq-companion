# Version 0.3 validation

Passed: Java syntax parsing of all 14 sources; XML parsing; Java R resource references; manifest widget/provider mapping; action-specific PendingIntent identities; request guards and unlock endpoint/body inclusion.

Not run here: SDK compilation, Android lint, emulator/phone layout, widget taps, notification permission grant/denial, process kill during a command, or live unlock. Windows script runs compilation and lint. Test these device behaviors after building, including rapid repeat taps, disconnected-account handling, climate temperature persistence and notification results.

Prior evidence: user confirmed version 0.2 logs in and communicates with the vehicle. Version 0.3 retains that API flow and adds unlock as a separately selected command. Never interpret missing/failed completion as a confirmed vehicle action.

Version 0.4: all Java syntax and XML resources/references checked again after styling changes. Android rendering and build remain to be verified on the user PC/phone. Layered gradients and pressed selectors implement the raised appearance; no live 3D rendering.

0.6: adaptive icon XML/source syntax parsed, SVG preview rendered, website local links checked. Not published; no release-signed APK built yet.
