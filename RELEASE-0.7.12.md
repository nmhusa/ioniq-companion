# IONIQ Companion 0.7.12 — Find my car signals

Find my car still opens the map directly. Its adjacent ⋯ menu now includes **Flash lights** and **Horn & lights**, alongside saved-location details and refresh. Both commands require confirmation; the horn prompt explicitly explains that the horn will sound. No horn-only or stop-horn command is claimed.

Requests go through Hyundai's US servers. Each command is submitted once and confirmation is polled with the corresponding LIGHTS_ONLY or HORN_AND_LIGHTS service type. Unknown outcomes are not automatically retried. Existing lock/climate/charge polling remains REMOTE_POLL. The endpoint and request-body reference is Hyundai-Kia-Connect/hyundai_kia_connect_api, HyundaiBlueLinkApiUSA.py (start_hazard_lights and start_hazard_lights_and_horn).

Includes 0.7.11 encryption and optional unlock-authentication protections. Update using your existing signing key. No live vehicle commands were issued during development; verify lights and horn behavior beside the parked car before sharing. Hyundai controls signal duration; this app does not set it.
