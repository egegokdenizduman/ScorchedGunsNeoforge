# Create: Scorched (mod id `scguns`)

Debloated NeoForge 1.21.1 fork of Scorched Guns 2 for Ege's Create modpack.

- **Spec:** the owner's Google Doc `1VUfd3PI8qGkMaYaUAe1A3EUrnt_hSVU2EWEsJRGSniE`. For the current update, the tab "Claude Current" is the source of truth (it replaces "THE MOD"): a changelog of the owner's work, with notes under each entry that are the actual work items. Read only the section you need.
  - The tab "Bible of Texture Design" is read **only by texture-design subagents**. The main session never reads it.
- **Status and to-do:** `docs/HANDOFF.md` (a hook injects it at session start).
- **Every task:** follow `docs/WORKFLOW.md`.
  - Gate 1: a checklist quoting the doc, approved before building.
  - Gate 2: one approved sample before any batch.
  - Do only what the item says.
- **Never** edit the Google Doc or `CHANGELOG.md`. Say "compiles", not "works", until it's been tested in game.
- **Build:** `./gradlew compileJava --offline`; dev client `./gradlew runClient --offline` (run dir `run/`). A Stop hook runs compileJava automatically when Java files have changed.
- **Shell:** Windows. Use PowerShell for git. Python: `C:/Users/Griskop/AppData/Local/Python/bin/python.exe -I`.

## Branches
- `test` on GitHub is the approved baseline. `main` is release only.
- All work happens on `working` (branched from `origin/test`). Fetch first.
- "push test": fast-forward `test` to `working`, push `test`, keep working on `working`.
- "release": merge `test` into `main` and push. Never touch `main` otherwise.
- Never force-push. If `test` can't fast-forward to `working`, stop and tell the owner. No other pushes or merges unless asked.

## Agents and sessions
- Two task kinds: (a) textures/models, (b) gameplay features.
- Subagents: texture and data work runs on Sonnet. Opus does planning and review only.
- Use low effort for mechanical work (recipes, tags, renames).
- One task per window. Remind the owner to open a fresh window or `/clear` between unrelated tasks.
