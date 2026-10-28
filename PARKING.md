# Park & return — preview 0.7.2

## Setup

1. Install a locally configured build and connect/select your Hyundai. The public CI APK has no Hyundai client credential and cannot connect.
2. On the main screen, tap **Walk-away lock · 30 m** beside the parked car. On first use, allow precise location and notifications, then tap again. Wait for **Parking saved** before walking away. Subsequent sessions need one tap; tap again to stop. Manual parking does not need Nearby devices permission. Both notification channels must remain enabled.
3. **Walk-away distance & automatic Bluetooth settings** offers 20, 30, 50, 100, 150 and 200 metres. The old 100 m default migrates to 30 m once; other existing distances remain. For Bluetooth-triggered parking, select your paired car/dongle, allow Nearby devices, then start monitoring before driving. Door closure and key-fob movement are not directly detected.
4. After parking, check that the monitoring notification says parking was saved. An observed Bluetooth ACL disconnect requires a GPS fix no older than 30 seconds, accuracy at most 5 m for 20/30 m distances (20 m for longer distances) and measured speed below 1.5 m/s. Otherwise tap **I’ve parked here** while beside the stationary car. No coordinates are retained after the service stops.
5. Walk away. Distance must exceed the configured radius plus both GPS error estimates, with measured speed at most 2.5 m/s and 5 seconds of continued separation for 20/30 m distances (20 seconds for longer distances). Poor/missing GPS or returning inside the radius resets the countdown. Use **Stop / cancel** in the notification to cancel pending work.
6. One lock request is made through the existing Hyundai API and shared command queue. A queued request checks the session and location again before sending. Errors and timeouts are not retried. Once a command is sent it cannot be recalled. Read the result notification; do not assume a request was successful.
7. After the departure attempt, returning within half the departure radius for distances under 100 m, or within 60 m for longer distances, including accuracy margin produces an **Unlock** notification action. Tapping sends the command; approach alone never unlocks.

## Limits

This is not passive key entry. The dongle may use Wi-Fi instead of a persistent Bluetooth link; pairing alone does not prove disconnects correspond to parking. Phone coordinates are an estimate of the parked car’s location; if someone moves the car, stop and re-arm. Monitoring uses GPS and increases battery use. Android/Samsung may suspend or stop it. It is not restarted on reboot, process termination or force-stop, and needs a visible start from the app. Departure must occur within 15 minutes of parking, within 500 m and at walking speed. Monitoring expires after 24 hours. In Bluetooth monitoring mode, a reconnect cancels the old parking session and waits for the next disconnect. In manual one-tap mode Bluetooth changes do not cancel the session. Changing account, vehicle, device, distance or erasing data cancels monitoring.

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

## Short-distance testing

20/30 m are minimum separation estimates, not guaranteed physical lock points. GPS uncertainty is added to the threshold and walking during the five-second dwell, scheduling delays and Hyundai response time add distance. Keep the phone beside the car until Parking saved is shown. If a 5 m accuracy fix is unavailable, the short-distance session waits instead of guessing. Test outdoors with your key available. Confirm one lock attempt at departure, no return reminder while continuing away, and one reminder after actually approaching the car again.


Find My Car (0.7.8) retains the last successfully marked parking spot, accuracy and save time on this phone, associated with the selected vehicle. Stopping monitoring does not clear that fallback. Use Find My Car → Clear saved to delete it. Disconnect, account change or vehicle selection also clears saved locations. A map opens only after your tap and shares the selected coordinates with Google.
