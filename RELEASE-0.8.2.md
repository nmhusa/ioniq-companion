# 0.8.2 — Automatic lock after car Bluetooth disconnect

Replaces the cumbersome GPS walk-away/return method with one-time Bluetooth setup. Select the car itself (not the Android Auto dongle), choose 30/60/120 seconds, enable once. No arming button each trip. Reconnection, Bluetooth-off, disabling, account/vehicle change or expiry cancels unsent work. One lock attempt per observed disconnect; no automatic retries or unlock.

Monitoring uses a foreground notification and resumes after phone restart when Android permits. It never restores a pending lock countdown. Bluetooth loss does not prove parking or closed doors. Actual behavior must be tested on your Samsung phone and car.

Release obfuscation from 0.8.1 is retained. Use your existing signing key and update over the current app. Watch 0.8.0/0.8.1 remains compatible; no watch reinstall is required.
