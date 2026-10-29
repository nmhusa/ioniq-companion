# IONIQ Companion for Galaxy Watch — 0.8.0 preview

For Galaxy Watch4 and newer Wear OS watches, including the Galaxy Watch Ultra family. This is a phone-connected companion, not a standalone LTE Bluelink client. A reachable paired Android phone with IONIQ Companion 0.8.0, Google Play services, Internet access and a working Bluelink login is required.

## Screens

Swipe up/down through Doors, Climate, Charging, Find Car and Status. Swipe from the left edge or use Back to leave. Large colored buttons send lock, unlock, climate start/stop and charging start/stop with one tap. Climate uses the phone's saved preset and temperature. Flash lights is one tap; Horn & lights requires confirmation. Status sync reads the phone's saved Hyundai report and does not wake the car. Tap the result text for full result/report details and its timestamp.

Watch commands show waiting/queued/sending/confirmed/unknown states. No command is automatically resent. Reopening the app can read the last saved result from the phone. A successful message delivery is not presented as vehicle completion. Unknown results require checking the phone/official app before another attempt.

## Build both signed APKs

Run the latest update-existing-windows.ps1 in your existing phone project first. Then, from that project in PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-release-windows.ps1
powershell -ExecutionPolicy Bypass -File .\build-watch-windows.ps1
```

Both helpers prompt locally for the existing signing-key password. Use the SAME existing keystore and alias for phone and watch. If your key is elsewhere, pass `-KeyStore 'C:\path\ioniq-release.p12'` to both helpers. Do not create a new key.

- Phone: `release-output\0.8.0\ioniq-companion-0.8.0.apk`
- Watch: `watch-release-output\0.8.0\ioniq-companion-watch-0.8.0.apk`

Install the phone APK over the current phone app. Never install the watch APK on the phone: both deliberately use the same package name so Google Play services can verify matching signatures.

## Install on the watch using wireless debugging

1. Pair the watch to the phone with Galaxy Wearable as usual. Put the watch and Windows PC on the same Wi-Fi network.
2. On the watch, open Settings → About watch → Software information. Tap Software version repeatedly to enable Developer options. Under Developer options, turn on ADB debugging and Wireless debugging (labels vary by Wear OS version).
3. Choose Pair new device / Pair using pairing code. Note the pairing IP/port and code. The main Wireless debugging screen shows a DIFFERENT connection/debugging port.
4. In the project PowerShell window, use Android SDK platform-tools. Replace the placeholders with the watch's actual addresses:

```powershell
$adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
& $adb pair 'WATCH_IP:PAIRING_PORT'
& $adb connect 'WATCH_IP:DEBUGGING_PORT'
& $adb -s 'WATCH_IP:DEBUGGING_PORT' install -r '.\watch-release-output\0.8.0\ioniq-companion-watch-0.8.0.apk'
```

The pair command asks for the code. Always include `-s` for installation so the APK goes to the watch. Turn off wireless debugging after installation; normal watch-phone controls use the Wear OS Data Layer, not ADB.

5. On the PHONE: Settings & help → Watch controls → Enable. Then open IONIQ Companion from the watch app drawer. This opt-in is reset when changing the Bluelink account/selected car or disconnecting.

## Security and limits

The watch contains no Hyundai client credential, Bluelink password, PIN, access token or refresh token. The phone retains credentials and handles Hyundai calls. Google Play services requires the same app package/signature on both devices. Data Layer communication can use Bluetooth, Wi-Fi or Google's cloud relay; this is not a direct connection to the car.

The phone validates allowed actions, an account-specific session, request IDs and a short freshness window. It persists duplicate-request receipts and will not resend a command after process death or reconnection. Do not turn off automatic date/time on either device. Paired devices and the installed signing key remain part of the trust boundary.

If phone Unlock security is enabled, watch unlocking is blocked with instructions to authenticate on the phone. Other essential controls remain one tap. The watch uses the system watch lock; this version adds no separate watch PIN prompt.

Some Android/Samsung background restrictions may block starting the phone command service. The watch then asks you to open the phone app and try again; it does not silently queue a future action. If the phone is unreachable, commands cannot run. This needs paired-device testing on your Samsung hardware.

This preview has no standalone Internet commands, phone-free LTE control, map view, tiles, complications or bezel navigation. Existing phone features are retained.
