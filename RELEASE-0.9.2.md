# IONIQ Companion 0.9.2 — 12-volt battery badge

The car image panel now includes a compact 12V badge beside Range and Odometer. Tap it for report age, timestamp and explanation. It reads `battery.batSoc` from the existing US vehicle-status cache; the main EV battery is never used as a fallback. Missing/invalid data displays Unknown. No extra API requests or continuous monitoring are added.

Display guides: amber below 70%, red below 50%; these are app thresholds, not Hyundai fault limits. Readings over 24 hours are labeled old and shown neutrally. Unknown report age is neutral. This estimates charge, not battery health or starting reliability. No background low-battery notification is included in this version.

Run the latest update-existing-windows.ps1, then build-release-windows.ps1 in your existing project. Install release-output/0.9.2/ioniq-companion-0.9.2.apk using the same signing key. No watch update is required.

Local battery parsing/time/color tests passed; full Android CI and on-car reading availability still require verification.
