# 0.8.1 — Release obfuscation
Phone and watch release builds now use R8 code shrinking, optimization and name obfuscation. The phone client credential is stored as masked numeric data and decoded at runtime, rather than as a plain string constant. This is a deterrent to casual extraction, not a security boundary; a determined analyst can recover it.

Update locally with update-existing-windows.ps1, then build-release-windows.ps1 using your EXISTING key. The updater masks an existing quoted credential locally without uploading it. Private backups may contain the original credential; do not share them. Existing API behavior, account storage and signing identity are preserved. Watch 0.8.0 remains protocol-compatible; updating the watch is optional.

Only release builds are R8-obfuscated. Keep mapping.txt privately for crash diagnosis, outside shared release folders. No credentials or live car commands are used in CI.
