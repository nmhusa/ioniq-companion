# IONIQ Companion 0.9.0 — vehicle appearance

Tap the car image to preview a factory paint color and save it. Choices are encrypted locally, separately for each account and VIN. Switch cars normally; each keeps its own color and nickname. Disconnect/erase removes those choices.

- 13 US model-year/body-style palettes and 50 transparent car images: IONIQ 5 (2022–2026), IONIQ 5 N/XRT (2025–2026), IONIQ 6 (2023–2025), IONIQ 9 (2026).
- Palette selector allows a manual body-style/year reference when Bluelink supplies incomplete identification.
- Automatic appearance uses only recognized exterior paint names or verified codes if supplied by Bluelink. No VIN paint decoding. Unknown or conflicting values keep the original image.
- These are representative illustrations. Wheels, paint finish and trim-specific availability vary. US 2026 IONIQ 6 availability is unverified; select a reference palette manually.
- No extra dashboard controls, location access or car requests. No change to lock, climate or Bluetooth auto-lock behavior.

See [palette coverage and image provenance](COLOR-CATALOG.md).

## Update your existing Windows project

Run from the existing project folder, keeping your API configuration and signing key private:

```powershell
Invoke-WebRequest 'https://raw.githubusercontent.com/nmhusa/ioniq-companion/main/update-existing-windows.ps1' -OutFile '.\update-existing-windows.ps1'
powershell -ExecutionPolicy Bypass -File .\update-existing-windows.ps1
powershell -ExecutionPolicy Bypass -File .\build-release-windows.ps1
```

Install `release-output\0.9.0\ioniq-companion-0.9.0.apk` over the existing app using the same signing key. This phone-only feature does not require a watch update.
