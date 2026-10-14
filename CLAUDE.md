# Create: Scorched (mod id `scguns`)

Debloated NeoForge 1.21.1 fork of Scorched Guns 2 for Ege's Create modpack.

- **Spec:** the owner's Google Doc `1VUfd3PI8qGkMaYaUAe1A3EUrnt_hSVU2EWEsJRGSniE`. Tab "THE MOD" holds the plan and the owner's review notes, which override everything else. Tab "Bible of Texture Design" holds the art rules. Read only the section you need.
- **Status and to-do:** `docs/HANDOFF.md` (a hook injects it at session start).
- **Every task:** follow `docs/WORKFLOW.md`.
  - Gate 1: a checklist quoting the doc, approved before building.
  - Gate 2: one approved sample before any batch.
  - Do only what the item says.
- **Never** edit the Google Doc or `CHANGELOG.md`. Never push or merge unless asked. Say "compiles", not "works", until it's been tested in game.
- **Build:** `./gradlew compileJava --offline`; dev client `./gradlew runClient --offline` (run dir `run/`). A Stop hook runs compileJava automatically when Java files have changed.
- **Shell:** Windows. Use PowerShell for git. Python: `C:/Users/Griskop/AppData/Local/Python/bin/python.exe -I`.
