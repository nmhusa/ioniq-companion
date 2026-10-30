# IONIQ Companion 0.8.9

- IONIQ 9 model detection and a transparent generated vehicle illustration, alongside the existing IONIQ 5 and 6 images. Illustration is representative, not trim-accurate.
- Top-right switch button appears for multi-vehicle accounts (and while the list is not yet known). Refresh the list from Settings & help → Switch / refresh vehicles.
- Tap the vehicle name to give that specific car a nickname, stored encrypted on this phone. Blank restores the model name. Names are scoped by account and VIN and cleared on disconnect/account replacement.
- Switching updates the active VIN/registration and clears old status, mileage, locations and command results. A failed status refresh leaves the new vehicle selected with unavailable readings, not the previous car's data.
- Phone controls and all existing widgets follow the selected car. Switching pauses Bluetooth auto-lock and watch controls; enable them for the newly selected vehicle. Queued commands cannot silently cross a vehicle/account change, and pending unlock authentication is cancelled if the selected car changes.

Use the existing signing key and install over the current app. No watch APK update is required. This adds IONIQ 9 display support, not verified remote-command compatibility for every model/year. Multi-car operation and IONIQ 9 API behavior still require owner testing.

Asset: `app/src/main/res/drawable-nodpi/car_profile_9.webp`, generated with the built-in image tool. Prompt: photorealistic dark gray 2026 Hyundai IONIQ 9 production SUV, front three-quarter view facing left, complete car and wheels, soft studio light, transparent background, no scenery or captions. Stored as WebP with transparency for APK size.
