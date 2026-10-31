# 0.9.5 — security defaults

Unlocking now requires phone authentication by default. Upgrading enables protection once, including on installations that previously disabled it. You may explicitly disable it after phone authentication under Unlock security. Protected unlock requires a configured phone screen lock; watch unlock remains blocked while protection is on. Locking, climate, and Bluetooth auto-lock keep their existing behavior.

Sign-in and unlock authentication windows prevent screenshots and recent-app previews. Dashboard screenshots remain available. Sign-in fields are excluded from saved view state and cleared when the dialog closes. The Connect button rejects touches through an obscuring overlay.

The Hyundai shared client credential remains recoverable from an APK despite obfuscation. This release does not claim to make embedded credentials secret. Existing Android Keystore encryption, HTTPS validation, command guards and signed-update requirements remain in place.

Build with your existing signing key using build-release-windows.ps1. Output: release-output/0.9.5/ioniq-companion-0.9.5.apk. No watch APK update is required.
