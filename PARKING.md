# Bluetooth auto-lock — 0.8.2

This replaces GPS walk-away lock. There is no distance setting, parked-location arming, or return-unlock notification.

## Set up once
Open Bluetooth auto-lock on the phone dashboard. Select the car's own paired Bluetooth device, NOT an Android Auto dongle. Choose 30 (default), 60 or 120 seconds, then Enable auto-lock. Allow Nearby devices and notifications. Location permission is not required for this feature.

After setup, no button press is needed per trip. The app observes a car connection (including an existing headset/audio connection at startup), then waits for the link to disconnect. After the delay it attempts one lock through Hyundai. It will not retry automatically. Reconnection before dispatch cancels the request. An actual new connection/disconnection starts a new cycle.

The persistent notification shows monitoring/countdown and offers Turn off / cancel. Turning Bluetooth off disables monitoring. Account/vehicle changes also disable it. Delay/device changes require re-enabling once.

Monitoring can resume after reboot, app update or system restart when Android permits. Pending countdowns are never persisted or replayed. Android force-stop/battery restrictions may prevent monitoring; the dashboard reports when enabled monitoring is not running. Open settings and enable again if needed.

## Limits and checks
Bluetooth loss is not proof the door closed, the key left, or the car parked. Test with the car's own connection and check the result notification. A disconnect during driving can trigger a lock attempt. This mode measures time, not distance, and does not confirm door closure.

Android may delay execution; stale queued requests expire rather than locking much later. A short wake lock is held only during the countdown/dispatch window. Internet and Hyundai availability are required. A command already sent cannot be recalled.

Verify: enable while already connected; shut car off; see countdown; reconnect and verify cancellation; disconnect again and verify one result. Verify no duplicate attempts, Bluetooth-off cancellation, account-change cancellation, and no old countdown after reboot. Keep a key available and check actual door state. No automatic unlocking.
