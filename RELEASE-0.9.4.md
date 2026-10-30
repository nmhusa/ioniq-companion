# IONIQ Companion 0.9.4 — optional seat heating and ventilation

Climate presets → Edit presets → choose a preset. On a supported selected vehicle, enable Include seat heating / cooling and choose each seat's reported levels. Save & use, then Climate on applies those settings. This is not a standalone seat command. Seat-inclusive climate runs use the upstream default five-minute duration.

Controls require reported EV status E, Bluelink generation 3, and seatConfigurations.seatConfigs with heatingCapable / ventCapable and supportedLevels. Model year alone never enables controls. A normal Refresh status retrieves enrollment metadata; unsupported or unknown capability means hidden controls and unchanged ordinary climate payloads. No probing commands are sent. Settings are encrypted and scoped by account, VIN and preset, and removed by disconnect/erase. Ventilation means the seat fan, not necessarily refrigerated cooling.

Mapping follows the US library: seat state 2 Off, 3–5 low/medium/high cooling, 6–8 low/medium/high heating; seatHeaterVentInfo driver/passenger/rear-left/rear-right. Unavailable positions use the upstream zero placeholder. Vehicle-reported generation replaces the year-based header guess when available.

Sources: [US climate implementation](https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api/blob/master/hyundai_kia_connect_api/HyundaiBlueLinkApiUSA.py), [seat status constants](https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api/blob/master/hyundai_kia_connect_api/const.py), [US enrollment fixture](https://github.com/Hyundai-Kia-Connect/hyundai_kia_connect_api/blob/master/tests/fixtures/updates/responses/usa_hyundai_enrollment_1040.json).

Local capability and payload tests passed, and seat/editor classes compile against Android API classes with integration stubs. Full Android CI, actual equipment reports, and live seat operation are not yet verified. No vehicle commands were issued during development.

Update with the latest update-existing-windows.ps1 and build-release-windows.ps1 in your existing project. Install release-output/0.9.4/ioniq-companion-0.9.4.apk with the same signing key. No watch update required; seat settings accompany the active phone preset when the existing climate command is used.
