# GitHub release procedure

Repository: https://github.com/nmhusa/ioniq-companion

## Current readiness

Release tooling is prepared. No working public APK is approved or published by this preparation. Public CI uses no Hyundai credential and cannot authenticate. The owner's local working API source/configuration is preserved by the updater.

The public API adapter reads a client credential from BuildConfig, configured through ignored local-api.properties or an environment variable. The existing local adapter may embed that credential directly. Either way, a working APK contains an extractable client credential. Keeping it out of GitHub source does not make it confidential inside the APK. MIT attribution for protocol code does not establish authorization to redistribute a Hyundai-issued credential or artwork. Establish provenance and permission before public release; do not upload credentials to issues/chat. If authorization cannot be established, keep distribution on hold or change to an authorized integration.

Login email/password/PIN and vehicle data are runtime app data, not normally part of a build. Nevertheless review local source/assets for personal test data before building. Never attach project folders, local configuration, backups or signing keys.

## 1. Update the existing project

Download the latest update-existing-windows.ps1 into Downloads, replacing the old copy. From PowerShell in the existing working project:

```powershell
powershell -ExecutionPolicy Bypass -File "$env:USERPROFILE\Downloads\update-existing-windows.ps1"
```

The updater preserves API credentials, Vault source, signing files and configuration. It backs up the API source and patches only the recognized climate payload to use saved preset choices, and adds release tooling/documentation.

## 2. First release key and build

```powershell
powershell -ExecutionPolicy Bypass -File .\build-release-windows.ps1 -CreateKey
```

Requires the same Java 17 and Android SDK setup as the existing debug build. The helper creates a permanent PKCS12 key at Documents\IONIQ-Signing\ioniq-release.p12 (outside the project), alias `ioniq-release`. Java prompts locally for a password. Store it in your password manager and securely back up the key separately. If Documents is cloud-synced, choose an appropriate private location using `-KeyStore 'D:\PrivateSigning\ioniq-release.p12'`.

For later builds, omit -CreateKey. If the key is missing, restore the original backup rather than generate a replacement. A different key prevents existing users from installing an update. Do not use your debug key for public distribution.

The helper builds/lints release, rejects a debuggable APK, aligns/signs it, verifies its signature and creates release-output\0.7.12 containing the APK, SHA-256 checksum, public signing-certificate information and installation/release notes. It never uploads files. Existing output directories are not overwritten. Passwords are not stored in source or script arguments.

## 3. Test the signed APK

A new release key cannot update the current debug-signed app. Prefer a spare Android device for first testing. On the existing phone, switching requires uninstall/re-login and loses local settings; stop parking monitoring first. Future release updates keep the same key/package identity.

Test login/vehicle selection, cached/fresh refresh, lock/unlock, climate, charging, widgets, permission denial, offline errors, Settings collapse/expand and account-warning cancellation. Confirm Bluetooth connection detection, countdown, reconnection cancellation, reboot behavior and result on the phone; never infer success from a build. Check certificate fingerprint against your retained known-good copy before each publication. Record actual device results in release notes.

## 4. Upload a draft, then publish

Go to https://github.com/nmhusa/ioniq-companion/releases/new . Tag `v0.7.12`, title `IONIQ Companion 0.7.12 — community preview`. Use RELEASE-0.7.12.md after removing the maintainer checklist and adding actual testing results. Save as a draft while preparing. Attach the signed APK, SHA256SUMS.txt, SIGNING-CERTIFICATE.txt and INSTALL.md only. After readiness checks, publish as a prerelease for early testers.

Share https://github.com/nmhusa/ioniq-companion/releases for prereleases. GitHub's /releases/latest link excludes prereleases; use it only after publishing a full release. Each later app update needs a greater versionCode and corresponding versionName, tag and release notes.

References: https://developer.android.com/tools/apksigner and https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository
