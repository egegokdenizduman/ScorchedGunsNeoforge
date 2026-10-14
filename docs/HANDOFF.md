# Handoff: Alpha 0.0.2 (written 2026-10-14)

## State
- All Alpha 0.0.2 work so far is on GitHub branch `test` (fast-forward of the previous `test`). Built from local branch `claude/minecraft-texture-style-bc5dc1`, which merged nine `alpha2/*` feature branches.
- `./gradlew build --offline` passes. The owner played it in the dev client; the one crash found (muzzle flash `Not building!`) is fixed in `db91fb2`.
- `mod_version` is still `0.0.1-alpha`. Nothing is released.
- The changelog for what was done lives in the Google Doc under "Create: Scorched: Alpha 0.0.2 {Planned Features & Changelog}". The owner's indented notes there are the review. The list below turns them into tasks; re-read the doc before each task in case it changed.

## What went wrong last session (avoid repeating)
- Textures were batch-changed (~110 files) without the owner approving a sample first; many came back as "needs dithering / wrong shape / wrong palette".
- Ingots, nuggets, blends and flechette got new shapes the owner did not ask for; the doc wants vanilla/Create shapes.
- Ammo boxes were rebuilt as bundle-style items; the owner's idea was Create packages (now marked obsolete).
- Scope creep: items were added (scorched block) that the owner now wants removed.
- Several plan items were never started: dev-environment mods, Niami removal, empty tank recipe.

## To-do from the owner's review (grouped, easiest first)

### Quick data/code changes
- Remove the Scorched storage block (block, item, recipes, tags, lang, textures, loot).
- Flechette: no longer craftable with anthralite (keep the Create saw recipe from cactus).
- Broken mold blasting: 8 nuggets + 1 at 50% (currently 9).
- Flash protection: also reduce (not remove) stun-grenade deafening; add Create's Netherite Diving Helmet to `#scguns:flash_protection`.
- Reflex sight: check it really uses the Aeronautics laser pointer with the non-Aeronautics fallback, like the laser sight.
- Empty tank: iron palette; recipe = iron sheets + bucket pressed together.
- Crusader: much higher arrow velocity than a bow (like Niami's power); healing arrows deal no damage from the Crusader only; enchantable with all vanilla bow/crossbow enchantments except Infinity.

### Dev environment
- Add Colorwheel, Create Aeronautics, Farmer's Delight, Vanilla Backport to the dev run.
- Make CEE (Create Enchantment Industry), Vanilla Backport and Create hard dependencies; Aeronautics soft (optional).

### Rendering / effects
- Flayed God: impact diamond effects sometimes end up inside the block; keep them above the hit surface.
- Continuous beams (Flayed God style) start from the wrong position; may need a rework of how beams are positioned.
- Muzzle flashes: more emissive, especially sculk and the bullet-type flashes; some are not emissive at all. Plasma is the reference for "good".
- Microjet particles: more "meat/force". Laser Musket's molten particles are the owner's favourite reference.
- Multiplayer flash visibility is untested; the owner wants a way to test without a second player (e.g. a debug command that makes a mob or dummy fire a held gun).

### Done and on `test` (2026-10-14)
- Niami removed (item, renderer, models, textures, gun data, recipes, tags, lang).
- Ammo boxes as Create packages: owner approved textures, fill states and mechanics. Boxes are `AmmoBoxItem extends PackageItem` (ammo in Create's `PACKAGE_CONTENTS`, capacity config `ammoBoxStackCapacity`), conversion from plain packages, packager output, placed/chain boxes show fill state. Worn on the curios belt as a leather belt with a tinted pouch per hip; the belt shows while any box's render toggle is on. Alt + right-click (keybind `key.scguns.equip_ammo_boxes`) with a box in the main hand equips it: empty belt slot first, otherwise the two slots take turns and the worn box swaps back into the hand. The casing pouch is shown as "Empty Casing Box" (item id still `empty_casing_pouch`).

### Textures (ask for approval on one sample before batching)
- Storage blocks: dithering, shading, anti-aliasing. Treated brass = Create brass block recoloured; treated iron referencing Create's industrial iron block; anthralite keeps the metal/polished-stone idea; diamond steel = diamond glassiness + steel, no centre bar, look at vanilla "Block of X".
- Ores: ore must not touch the block edge (deepslate anthralite does); darker "indent" stone around the ore like vanilla; dithering; emissivity only on the ore (currently one edge glows). Vehement coal needs its own fiery/sparkly shape (quartz and nether gold ore as inspiration, not centred, not a nether gold copy) and must be emissive. Stone anthralite shape is good. Complementary uses its own normals/material qualities: ores glossy, stone matte.
- Ingots: vanilla shape and angled look. Nuggets: vanilla angled look, each a distinct silhouette (anthralite and treated iron are currently identical). The old ore/ingot textures were mostly fine; don't redesign drastically, except diamond steel.
- Diamond steel: palette mismatch; must not look like plain diamond; depleted = closer to an anthralite/treated-diamond mix; the blend keeps subtle blue hues (see Aeronautics crushed end stone).
- Treated brass: use the gun palette.
- Blends: Create crushed-ore shape.
- Sulfur dust: clearer border.
- Needle: blaze/breeze rod / stick shape, greenish palette.
- Flechette: like the old straw bundle with the frog dart tip palette, inspired by Farmer's Delight straw; anti-aliased.
- Lime powder: lighter top-left border (look at redstone dust).
- Changed gun components need dithering.
- Later updates (don't start now): stun grenade, all grenades, all gun frames, soul molotov retexture, Crusader remodel.

## Open decisions for the owner
- Ship-impulse strength for the shotball (default 60 is a guess) — needs testing.
- Raw anthralite block: not decided.

## Useful facts
- Rendering: fetching a second `VertexConsumer` from a `MultiBufferSource` ends the previous one. Finish one render type before calling `getBuffer` for the next, or the game crashes with `IllegalStateException: Not building!`.
- Hooks (`.claude/settings.json`): SessionStart injects this file; Stop runs compileJava when `src/main/java` changed and blocks once on failure. The cache file is `.claude/.compile-ok` (git-ignored).
- Crash reports: `run/crash-reports/`; log: `run/logs/latest.log`. The dev run has Create, Iris + Complementary/Euphoria, JEI, Sodium.
- Scratch Python texture helpers from last session were in a temp dir and are gone; the extracted reference textures are not in the repo. Re-extract from jars in `run/mods` and the Gradle cache when needed.
- Emissive items use `<name>_e.png` (`neoforge:item_layers`); animated guns use `textures/animated/gun/<gun>_glowmask.png` (GunGlowLayer).
- Optional Iris mixin gives our ores block ids 10024 (stone ores) / 10026 (netherrack ores) for Complementary's glowing modded ores.
- Aeronautics ships (Sable sub-levels) are accessed by reflection in `SableBlockInteraction`.
