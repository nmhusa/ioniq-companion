# Park & return — preview 0.7

## Setup

1. Install a locally configured build and connect/select your Hyundai. The public CI APK has no Hyundai client credential and cannot connect.
2. Open **Park & return · Walk-away locking**. Grant precise location while using the app, Nearby devices and notifications. Both parking notification channels must remain enabled.
3. Select your paired car or Android Auto dongle. Choose 100, 150 or 200 metres (default 100). Start monitoring from this screen before driving.
4. After parking, check that the monitoring notification says parking was saved. An observed Bluetooth ACL disconnect requires a GPS fix no older than 30 seconds, accuracy at most 20 m and measured speed below 1.5 m/s. Otherwise tap **I’ve parked here** while beside the stationary car. No coordinates are retained after the service stops.
5. Walk away. Distance must exceed the configured radius plus both GPS error estimates, with measured speed at most 2.5 m/s and 20 seconds of continued separation. Poor/missing GPS or returning inside the radius resets the countdown. Use **Stop / cancel** in the notification to cancel pending work.
6. One lock request is made through the existing Hyundai API and shared command queue. A queued request checks the session and location again before sending. Errors and timeouts are not retried. Once a command is sent it cannot be recalled. Read the result notification; do not assume a request was successful.
7. After the departure attempt, returning within 60 m including accuracy margin produces an **Unlock** notification action. Tapping sends the command; approach alone never unlocks.

## Limits

This is not passive key entry. The dongle may use Wi-Fi instead of a persistent Bluetooth link; pairing alone does not prove disconnects correspond to parking. Phone coordinates are an estimate of the parked car’s location; if someone moves the car, stop and re-arm. Monitoring uses GPS and increases battery use. Android/Samsung may suspend or stop it. It is not restarted on reboot, process termination or force-stop, and needs a visible start from the app. Departure must occur within 15 minutes of parking, within 500 m and at walking speed. Monitoring expires after 24 hours. A reconnect cancels the old parking session and waits for the next disconnect. Changing account, vehicle, device, distance or erasing data cancels monitoring.

If the dongle emits misleading disconnects during a drive, use manual parking sessions instead. Always check the saved-parking status before relying on a session. Keep the key with you while testing.

## Device acceptance checks (not yet performed here)

- Start with **I’ve parked here**, walk beyond the radius outdoors, confirm exactly one lock attempt and verify the door physically or in the Hyundai app.
- Return during the countdown: no lock should be sent. Use Stop during the countdown: no lock should be sent.
- Disable location, revoke permissions, force-stop and reboot: no delayed command should run after restarting.
- Test Wi-Fi/cellular loss: one failed/unconfirmed attempt only. Verify in the official app before manually retrying.
- Reconnect the selected device during departure: parking session is cancelled. Unrelated Bluetooth devices do not arm/cancel it.
- Return after departure: one Unlock reminder; the car stays locked until you tap Unlock.
- Test with the screen off and Samsung battery settings at their defaults. If monitoring stops, check Android’s battery settings for this app.
- Tap each caution-area account action and Cancel: saved account and monitoring session remain unchanged. Confirm erase only when ready to sign in again.

## Automated checks

`tests/ParkingRulesTest.java` exercises accuracy margins, dwell reset, walking-speed gating, one-attempt behavior, return reminders and expiry without contacting a car. Android integration and actual vehicle behavior require the device checks above.
