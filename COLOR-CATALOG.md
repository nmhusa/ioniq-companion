# US vehicle appearance catalog

Verified from Hyundai USA model configurators and archived Hyundai brochures on 2026-10-30. The catalog covers the IONIQ 5, 6 and 9 families, not the earlier IONIQ hybrid/PHEV/electric hatchback or worldwide market palettes. Model year is used, not registration year. It is not a VIN paint decoder or a claim of exhaustive trim availability.

| Model / body | Model years | Source |
| --- | --- | --- |
| IONIQ 5 original | 2022 | [Hyundai brochure](https://secure.viewer.zmags.com/publication/7ae4faa8) |
| IONIQ 5 original | 2023 | [Hyundai brochure](https://secure.viewer.zmags.com/publication/ed5bb72b) |
| IONIQ 5 original | 2024 | [Hyundai brochure](https://secure.viewer.zmags.com/publication/5f3be1ec) |
| IONIQ 5 refreshed, XRT, N | 2025 | [Hyundai brochure](https://secure.viewer.zmags.com/publication/5f26c1e9) |
| IONIQ 5 refreshed, XRT | 2026 | [Hyundai configurator](https://www.hyundaiusa.com/us/en/vehicles/2026-ioniq-5) |
| IONIQ 5 N | 2026 | [Hyundai configurator](https://www.hyundaiusa.com/us/en/vehicles/ioniq-5-n) |
| IONIQ 6 | 2023 | [Hyundai brochure](https://secure.viewer.zmags.com/publication/f402942b) |
| IONIQ 6 | 2024 | [Hyundai brochure](https://secure.viewer.zmags.com/publication/85fa8deb) |
| IONIQ 6 | 2025 | [Hyundai configurator](https://www.hyundaiusa.com/us/en/vehicles/ioniq-6) |
| IONIQ 9 | 2026 | [Hyundai configurator](https://www.hyundaiusa.com/us/en/vehicles/ioniq-9) |

Archived brochure links came from [Hyundai USA brochures](https://www.hyundaiusa.com/us/en/brochures). Unversioned configurator pages can change; the bundled JSON records the reviewed year and exact palette.

## Limitations

- No verified US 2026 IONIQ 6 palette is bundled. No silent assumption that 2025 colors apply. Unknown years offer manual reference palettes.
- Standard palettes combine colors across trims; selecting a paint does not assert it was sold on that exact trim. The 2025 brochure mentions Celadon Gray Matte without excluding XRT, but no matching XRT render was obtained; it is available under the standard 2025 reference palette only. XRT trim coverage is therefore not exhaustive.
- Paint codes are stored only for reviewed configurators: 2026 IONIQ 5/N/9 and 2025 IONIQ 6. Historical palettes match names only. Conflicting recognized hints do not auto-select a paint. API exterior fields are optional, not confirmed present for every US account.
- Paint render is illustrative: monitor color, gloss, lighting, wheels and trim details differ from the real vehicle. Early IONIQ 5 assets may use a representative 2023 body; IONIQ 6 uses a representative 2025 body and 2024 Digital Green render.
- Current US IONIQ 5 web page serves model year 2027; it was intentionally excluded.

## Assets and provenance

`app/src/main/assets/paint_catalog.json` maps model/year/body to named paints and local assets. `paint-image-sources.json` records the source URL for every image. Images are bundled offline; no runtime third-party image requests.

49 images are Hyundai transparent product renders, downloaded from its Scene7 image host and encoded to WebP without recoloring. These Hyundai images are third-party material, not relicensed as project code. Attribution is not a grant of redistribution rights; verify permitted image use before public distribution.

The glossy Performance Blue IONIQ 5 N image is an AI-assisted representative illustration, made with the built-in image tool. Prompt: preserve the official matte N image’s shape, angle, wheels, glass and red trim; change only body finish to glossy Performance Blue using Hyundai’s glossy image as color reference, with a transparent background. Asset: `app/src/main/assets/paint_icons/i5_n_sfb.webp`. It is not an official factory render. All other images use their original factory paint.

## Behavior

Tap the dashboard car, preview a palette/color, then Save. Cancel discards changes. Automatic/original removes the manual override. Overrides are encrypted under account + VIN. A stale editor cannot write a selection after a vehicle switch. Disconnect/erase clears saved appearances. The editor sends no commands and requests no Hyundai data.
