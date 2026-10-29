# IONIQ 6 Companion — version 0.5

Native Android app for your US 2023 IONIQ 6. Existing vehicle API login/status behavior retained from version 0.2, which you confirmed works with your car.

## New in 0.4

- Dark dashboard with a highlighted status card and paired colorful vector-icon controls.
- Raised metallic icon medallions and beveled tiles: mint lock, champagne unlock, red/copper flame for climate-on, ice-blue snowflake for climate-off, lime charging, violet stop charging. Each uses multi-tone gradients, rim highlights, glyph relief and a drop shadow. Pressed tiles appear depressed. These are native vector/drawable assets, not a 3D engine.
- Six independent home-screen widgets. Each sends its named command in one tap without launching an activity or asking for confirmation. Place only the actions you want. The original larger dashboard widget is also available.
- Climate-on uses the temperature saved with the in-app slider.
- Widget feedback shows Sending, Confirmed, Unconfirmed, or Check app with time. Confirmed is Hyundai's command response, not live proof of door or climate state.
- A foreground notification keeps the request active while waiting for Hyundai. Allow notifications to see the result; widget results remain available if permission is denied.
- Further widget taps are ignored while one widget command is active. Commands are never automatically replayed after a failure or process restart.
- Unlock sends the upstream US unlock request; this new action needs verification on your car.

## Build on your home Windows PC

Use the same home PC as your working 0.2 build so Android can retain the existing app login on update.

1. Extract this ZIP into a new folder, e.g. C:\AI-Test\IONIQ6-v04.
2. Open the enclosed ioniq6-android folder (contains build-windows.ps1).
3. Type powershell in File Explorer's address bar and press Enter.
4. Run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build-windows.ps1
```

Prerequisites already used for 0.2: Temurin Java 17, Android SDK Platform 35 and Build-Tools 34.0.0 at default locations. Script installs checksum-verified Gradle 8.9 if needed and runs assembleDebug plus lintDebug.

5. On BUILD SUCCESSFUL the output directory opens. Transfer **ioniq6-v06.apk** to your phone and install as an update. Keep the existing app installed when updating from the same signing key.
6. Confirm the title says PREVIEW 0.6. Allow notifications when asked.
7. Tap **Add a home-screen widget**, choose an action, and approve placement in your launcher. Repeat for each desired action. Alternatively long-press the home screen → Widgets → find IONIQ 6 entries. Resize tiles if text is clipped by your launcher.
8. Test one command at a time with the car parked and observable. Check the official app if completion is unconfirmed before retrying.

In-app control tiles still ask for confirmation; individual home-screen action widgets execute immediately. The dashboard widget retains its previous behavior. Anyone able to use your unlocked home screen can tap your widgets, including Unlock.

## Privacy and implementation

Credentials remain AES-GCM encrypted with an Android Keystore key; no analytics or third-party proxy. API traffic goes directly to api.telematics.hyundaiusa.com. Backups and cleartext HTTP are disabled. Widget command service and receivers are not exported. Its explicit immutable PendingIntents authorize only the defined six actions. Status and command results remain in app-private storage and may be displayed by your launcher or notification panel.

Foreground service uses the dataSync type for phone-to-cloud requests; it starts only after a user tap, does not run continuously and does not restart commands automatically. Android battery policies and Hyundai network latency still apply. Park & return uses a separate user-started location foreground service; see PARKING.md. Automatic unlock is not implemented.

## Provenance

Protocol adapted from Andy Fase's MIT-licensed project:
https://github.com/andyfase/egmp-bluelink-scriptable
US source reviewed: src/lib/bluelink-regions/usa.ts, blob e4a5f9569d6966e235dc133de245d68cda5c0517.
MIT copyright/license included in LICENSE.

## Validation limits

Java parsing, XML parsing and resource/component references checked locally. Version 0.2 has user-confirmed live connectivity. Version 0.3 has not yet been compiled with the Android SDK or exercised on a phone here. Build script runs Android lint and compilation on your PC. New widget layout, background execution and unlock must be verified there; a successful build does not prove a vehicle command worked.

## 0.4 styling update
Only visuals, tile sizing and version labels changed from 0.3; widget command behavior is unchanged. Native gradient vectors require no external image downloads. Allow room for larger icons by resizing widgets if your launcher clips labels.

## 0.5 screenshot support
Removed FLAG_SECURE from the activity at user request. Screenshots and screen recording are no longer blocked by this app; Android can also show the app in its recent-app preview. Password and PIN fields remain masked. Device or work-profile policy may independently restrict capture.

## Local API credential required for public source builds

The public repository does not embed the API client secret. Before building, create `local-api.properties` in the repository root with `hyundaiClientSecret=YOUR_VALUE`, or set the HYUNDAI_CLIENT_SECRET environment variable. Use the client credential from your existing working local source configuration. Do not commit this file. Builds without it compile but cannot connect. GitHub Actions only builds a credential-free test APK; that APK cannot connect to Hyundai. This is an API application credential, separate from the user's Hyundai password/PIN. Compiled apps necessarily contain the configured client credential; it cannot be treated as confidential once a binary is distributed. Review the upstream API integration's terms before distributing a binary.

## Preview 0.7
Park & return setup and limitations are in PARKING.md. Account actions now have a caution strip and confirmation dialogs. Build with the same signing key as your installed app to preserve its local credentials.


## 0.7.11 security changes

Personal caches migrate to AES-256-GCM storage backed by Android Keystore. The credential vault keeps its existing key alias and format. Optional unlock authentication uses a single-use process-local permit valid for 15 seconds. Only the launcher trampoline is exported, and it discards extras. Authentication gates car unlock, not the whole app. Client credentials remain extractable from compiled binaries. A corrupt/unavailable cache key fails closed; it never falls back to plaintext. Device migration and biometric cancellation need testing before wider distribution.
