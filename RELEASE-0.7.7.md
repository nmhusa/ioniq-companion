# IONIQ Companion 0.7.7 — community preview

- Compact Climate presets control replaces the main-screen temperature slider and heater-options button. Winter, Summer and Custom each save temperature, front/rear defrost and steering-wheel heat.
- Existing settings are preserved as Custom. Choosing or editing presets sends no vehicle command; Climate on (including its widget) uses the selected preset.
- Charge limits opens an AC/DC editor with last-reported values and report timestamp. Read latest retrieves Hyundai's cached report. Both limits must be chosen and confirmed before one request is sent; unknown values are not guessed. No automatic retries.
- AC/DC limits use the Hyundai US endpoint, with 50–100% choices in 10% steps. Actual vehicle support requires testing. Command completion is not independent readback verification; refresh and compare the reported limits afterward.

Includes compact resizable action widgets and protected account controls. Signed release updates must use the existing permanent key. See INSTALL.md. Public redistribution remains pending client-credential/artwork authorization. Do not attach keys or configuration files.
