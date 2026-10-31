# 0.9.8 — Compact vehicle check and controls

Adds an expandable cached vehicle check below the car report: doors, windows, hood, trunk, tire-warning lamps and washer-fluid warning. Unknown values remain unavailable. Reports older than 15 minutes or with an unrecognized timestamp are marked older; this is a display freshness threshold, not a guarantee of current state. No new vehicle requests or automated actions.

Primary action buttons now use 30 dp icons beside their labels and a 68 dp minimum height (previously 104 dp), with expanding height for wrapped text. Existing colors, raised surfaces and authentication behavior remain.

No numeric tire pressures are displayed in this release. Model-specific readings require vehicle testing. Build using your existing signing key and API configuration.
