# Create: Scorched (mod id `scguns`)

Debloated NeoForge 1.21.1 fork of Scorched Guns 2 for Ege's Create modpack. Java in `src/main/java/top/ribs/scguns`, assets in `src/main/resources/assets/scguns`, data in `src/main/resources/data/scguns`.

## Source of truth
- The owner's Google Doc "Development Plan" (id `1VUfd3PI8qGkMaYaUAe1A3EUrnt_hSVU2EWEsJRGSniE`) holds the spec. Tab "THE MOD" = plan + changelog + the owner's review notes under each item; tab "Bible of Texture Design" = art rules. Read the relevant section before starting a task; the owner's indented notes under an item override anything else.
- `docs/HANDOFF.md` = current status and the open to-do list taken from the doc. Read it at the start of a session.
- Never edit the Google Doc or `CHANGELOG.md`. The owner writes changelogs and release notes themselves.

## Working rules
Follow `docs/WORKFLOW.md` for every task. The two gates in short:
- **Gate 1:** post a checklist (template in WORKFLOW.md) quoting the doc item and get the owner's approval before building anything. Afterwards, check each line against the doc wording.
- **Gate 2:** before applying a change to more than one file, show one sample (textures: upscaled old | new | vanilla/Create reference) and get approval.
- Do only what the item says. If you think something else should change, propose it; do not do it.
- Never call something tested unless it was run in game. Say "compiles" or "untested" otherwise.
- Push/merge/release only when asked. Work on a branch, never on `main`.

## Build and run
- `./gradlew compileJava --offline` (fast check), `./gradlew build --offline`, `./gradlew runClient --offline` (run dir `run/`, git-ignored; has Create, Iris + Complementary/Euphoria, JEI, Sodium).
- Crash reports: `run/crash-reports/`; log: `run/logs/latest.log`.
- Shell is Windows: prefer the PowerShell tool for git; Python 3.14 + Pillow at `C:/Users/Griskop/AppData/Local/Python/bin/python.exe` (run with `-I`).

## Texture rules (summary of the Bible tab)
16x16, no mixels, binary alpha, light from top-left, darker hue-tinted outline (lighter top-left, darker bottom-right), hue-shifted 3-5 shade ramps, no pillow/pancake shading, no banding, deliberate dithering where the material needs it. Compare against the real vanilla/Create textures, never programmer art. Items made from a material use that material's palette. Ingots/nuggets follow vanilla's angled 3/4 shape; every nugget a distinct silhouette (colour-blind readability). Ores: never touch the block edge, darkened "indent" stone around the ore, dithered.

## Rendering gotcha
Fetching a second `VertexConsumer` from a `MultiBufferSource` ends the previous one. Finish writing one render type before calling `getBuffer` for the next (crash: `IllegalStateException: Not building!`).
