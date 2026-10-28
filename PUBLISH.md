# Publish IONIQ Companion

Prepared for GitHub account nmhusa and repository ioniq-companion. Repository created at https://github.com/nmhusa/ioniq-companion. Website publishing and the public APK release still require the steps below.

## 1. Create the repository

Go to https://github.com/new. Owner: nmhusa. Name: ioniq-companion. Select Public and create it. This package includes the README and MIT license, so do not initialize extra files.

Upload the CONTENTS of this package's ioniq-companion folder into the repository root, not the enclosing folder. Prefer GitHub Desktop: clone the empty repository, copy the files into it (including .github and .gitignore), review the changes, commit and push. Do not copy build output, credentials or signing keys from your working project.

## 2. Enable the website

Repository Settings → Pages → Source: Deploy from a branch → main → /docs → Save.
Expected URL after successful deployment: https://nmhusa.github.io/ioniq-companion/
The page links to Releases without claiming a binary is already available.

## 3. Build a release-signed APK

First run build-windows.ps1 to generate the Gradle wrapper and confirm the debug build/lint pass. Open the repository folder in Android Studio. Set its Gradle JDK to Java 17.

Build → Generate Signed App Bundle or APK → APK → Create new keystore. Store the keystore OUTSIDE the repository. Choose a strong password and retain the key alias. Back up the keystore and credentials securely; future APK updates must use the same signing key. Do not send the key/password in chat or upload them to GitHub. Select release and finish the wizard.

The release build type is supported by Android's application plugin without extra signing configuration; the wizard supplies it. This project has no hardcoded signing credentials. Keep the Android applicationId com.ioniq.widget and increase versionCode for every later release.

A release-signed APK will not update an earlier debug-signed install. For the first switch, uninstall the debug app and sign in again after installing the release. Thereafter keep using the same release key.

## 4. Verify before publishing

Install the release APK yourself. Verify login, vehicle selection, reported timestamp, lock/unlock, climate on/off, charging on/off, each widget, duplicate-tap handling, offline behavior, notification permission and screenshots. Confirm no personal data is packaged. Version 0.2 connection was owner-tested; later changes must be validated separately.

## 5. Create a prerelease

GitHub → Releases → Draft a new release. Tag: v0.6.0. Title: IONIQ Companion 0.6.0 — Preview. Use RELEASE_NOTES.md. Check “Set as a pre-release”. Attach only the tested release-signed APK and its SHA-256 text file. Never attach a keystore or local.properties.

PowerShell checksum command (replace the path with your release APK):

```powershell
Get-FileHash .\ioniq-companion-0.6.0.apk -Algorithm SHA256 | Format-List | Out-File .\SHA256.txt
```

The website’s Releases button will show this prerelease. After device validation, add your own redacted screenshots under docs/images and update index.html. The current illustration is explicitly labeled, not represented as a screenshot.
