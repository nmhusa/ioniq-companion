# IONIQ Companion 0.7.11 — local security improvements

- Encrypts vehicle reports, mileage, identity, saved locations, parking settings and command history using AES-256-GCM and Android Keystore. Existing plaintext cache preferences migrate after successful encrypted storage; the credential vault key and format stay unchanged.
- Settings & help → Unlock security optionally requires strong biometrics or the phone screen-lock credential before unlocking. Both enabling and disabling require authentication. Older Android versions use the system credential confirmation screen. Protection starts OFF to preserve existing behavior.
- Protected unlock widgets open authentication; stale direct-service widget intents cannot bypass protection. Authentication issues a short-lived, single-use in-memory permit. Cancelling never sends a command.
- Only a minimal launcher activity is exported. It discards external extras and data. Main UI and authentication activities are private.
- Error displays/history use sanitized messages rather than arbitrary exception contents.
- Existing one-tap locking/climate, screenshot support, account login and release signing remain supported.

## Limits

The Hyundai client credential remains recoverable from the APK. No backend or Hyundai authorization is added. The password remains encrypted in the existing credential vault for compatibility; this release does not change the undocumented sign-in protocol. Cache encryption and unlock authentication do not protect a compromised app process or rooted device. Screenshots, widget contents, and map coordinates intentionally handed to a map app remain visible outside the encrypted cache.

## Device checks before sharing

Upgrade using the existing signing key. Confirm the account, odometer, locations and widgets survive migration. Enable unlock protection, cancel a prompt and verify no command is sent; test one authorized unlock while beside your car. Confirm lock/climate widgets remain one tap. Test background/rotation behavior and a phone restart. No live vehicle operations are executed by automated checks.
