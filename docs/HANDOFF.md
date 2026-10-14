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
- Flechette: no longer craftable with anthralite (keep the Create saw recipe from cactus).
- Broken mold blasting: 8 nuggets + 1 at 50% (currently 9).
- Flash protection: also reduce (not remove) stun-grenade deafening; add Create's Netherite Diving Helmet to `#scguns:flash_protection`.
- Reflex sight: check it really uses the Aeronautics laser pointer with the non-Aeronautics fallback, like the laser sight.
- Empty tank: iron palette; recipe = iron sheets + bucket pressed together.

### Dev environment
- Add Colorwheel, Create Aeronautics, Farmer's Delight, Vanilla Backport to the dev run.
- Make CEE (Create Enchantment Industry), Vanilla Backport and Create hard dependencies; Aeronautics soft (optional).

### Rendering / effects
- Continuous beams (Flayed God style) start from the wrong position; may need a rework of how beams are positioned.
- Muzzle flashes: more emissive, especially sculk and the bullet-type flashes; some are not emissive at all. Plasma is the reference for "good".
- Microjet particles: more "meat/force". Laser Musket's molten particles are the owner's favourite reference.
- Multiplayer flash visibility is untested; the owner wants a way to test without a second player (e.g. a debug command that makes a mob or dummy fire a held gun).

### Done and on `test` (2026-10-14)
- Niami removed (item, renderer, models, textures, gun data, recipes, tags, lang).
- Ammo boxes as Create packages: owner approved textures, fill states and mechanics. Boxes are `AmmoBoxItem extends PackageItem` (ammo in Create's `PACKAGE_CONTENTS`, capacity config `ammoBoxStackCapacity`), conversion from plain packages, packager output, placed/chain boxes show fill state. Worn on the curios belt as a leather belt with a tinted pouch per hip; the belt shows while any box's render toggle is on. Alt + right-click (keybind `key.scguns.equip_ammo_boxes`) with a box in the main hand equips it: empty belt slot first, otherwise the two slots take turns and the worn box swaps back into the hand. The casing pouch is shown as "Empty Casing Box" (item id still `empty_casing_pouch`).

### Beam impact fix: on branch `alpha2/beam-impact` (2026-10-14, owner tested in game: "it worked"; on `test`)
- All beam guns: the tip lands on the crosshair point (eye ray at the server's hit distance), drawn from the gun, and snaps to the block surface (`BeamHandler.renderBeam`).
- Flayed God: impact rings and nether star lie flat on the hit block face, lifted off it (`BeamRenderer.renderWitherImpact`); on entities/air they sit across the beam as before. Impact particles start just off the face.
- Beam start position is unchanged (still its own to-do item above).

### Crusader: on `test` (2026-10-14, branch `alpha2/crusader`, owner tested in game: "PERFECT")
- Speed 13 (Niami's), damage 16: ~11 per hit vs a full bow's 6 (crit max 10).
- Arrows are created with the gun as their weapon, so Power, Punch, Flame, Piercing work; Multishot = 3 arrows for 1 ammo, extras not pickable. Infinity and Quick Charge are refused on all guns (`GunItem.supportsEnchantment`).
- Healing arrows from the Crusader cancel the impact (`CrusaderArrowEvents`), apply the potion directly with tiny knockback: no damage, no anger. Undead still harmed by the potion.
- Backtank boost (owner's addition): with backtank air, 2.5x fire rate and 6 air per shot (`energyUse` 25 like the Nailer); without air it fires normally. Item bar shows air. Logic in `compat/CreateBacktank`, item class `CrusaderItem`.
- HUD ammo icon for arrow guns is drawn as a real item and keeps the last fired arrow when empty (was purple/black for tipped arrows).

### Doc "Textures" items 1–14 except 9 and 13: on `test` (2026-10-14, from branch `alpha2/item-textures`)
The owner was away, so they chose "sample, then batch": Opus reviewed every sample before its batch, one commit per item. Review sheets (old | new | reference at 10x + 1x) are in `docs/texture-review/<item>/`. Revert any item with `git revert <commit>`. Not yet seen in game.
- 1 Blends: treated iron, treated brass (colours kept) and scorched now use Create's crushed-ore mask.
- 2 Sulfur dust: clear hue-tinted outline with a lighter top-left rim.
- 3 + 7 Ingots: the original SC2 ingots already had vanilla's exact silhouette, so they were restored byte for byte (anthralite, treated iron, treated brass, scorched + `_e`/`_s`).
- 4 Nuggets: anthralite is a tilted diagonal, treated iron a flat-bottomed chip. Plasma was already distinct, so it is unchanged.
- 5 Needle: 2D icon on the vanilla stick/rod silhouette with a green ramp. Open question: the old needle was near-black, so "same greenish palette" was read as green. `needle_3d.png` is untouched.
- 6 Empty tank: iron palette. Recipe is now Create compacting, 1 bucket + 1 iron sheet (`c:plates/iron`) → 1 tank. The old crafting recipe gave 2 tanks and was removed.
- 8 + 10 Diamond steel: the ingot is grey-teal steel with glassy cyan streaks; depleted is an anthralite/teal mix; the blend uses Create's crushed-ore shape with subtle blue. Casings and the gun frame are not touched.
- 11 Flechette: straw bundle (old silhouette) in the frog dart tip's olive/cream palette.
- 12 Lime powder: top/left border lighter than bottom/right.
- 14 Gun parts only (owner's choice): gun_parts, heavy_gun_parts and both unfinished ones were redrawn with dither. Firing units, barrels and magazine got only a light touch on the old art (small hue shift, a little dither, specular pixels), so they may need a stronger pass.
- **Owner review (2026-10-14): "This was horrible."** Reverted to SC2 originals (commit 1.4.0 `e531a1e`) for: anthralite ingot + nugget, diamond steel + depleted ingots, treated iron nugget, treated brass blend, scorched blend (its `_e`/`_s` rebuilt from SC2's lava pixels), flechette. Sulfur dust is back to the `test` version. Diamond steel and treated iron blends keep the new crushed-ore shape, recoloured with the SC2 colours. Everything else from this pass stays: needle, empty tank, lime powder, gun parts, and the other ingots (already SC2).
- Not done: 9 (treated brass palette) and 13 (grenades), as the owner excluded them.

### Done on branch `alpha2/storage-blocks-ores` (2026-10-14, merged into `working` and `test` with the item textures)
- Scorched block removed (block, item, recipes, tags, lang, textures, loot, Iris mapping).
- JEI added to the dev runtime (`localRuntime`).
- Approved textures: treated brass block (Create brass recoloured to the gun palette), treated iron block (Create industrial iron), vehement coal ore (new forked ember fissure) with `vehement_coal_ore_e.png` full-bright overlay (block model like the old scorched block). Second pass (2026-10-14, approved): stone anthralite ore (old cluster shapes, solid, dark top / light bottom, stone darkened above each cluster like vanilla iron ore); deepslate anthralite ore (same clusters, brighter ore, lighter deepslate lip like vanilla deepslate iron, almost no darkening); diamond steel block (old texture with its streaks sheared from 45° to horizontal; frame and inner ring untouched). Anthralite block: committed as "v1" (old texture + one-sided checker dither on the streak edges, a few speckles); the owner is still working on it themselves.
- **Owner review (2026-10-14): only the treated brass block and the diamond steel block are final.** The owner is not happy with treated iron block, vehement coal ore, both anthralite ores or the anthralite block, and will come back to them later. Leave them as committed until then; don't rework them unprompted.
- **Owner, later on 2026-10-14:** loves the new vehement coal ore and treated brass textures. Whole branch pushed to `test`; the owner will tackle the remaining blocks/ores later together with the item textures.

### Textures (ask for approval on one sample before batching)
- Anthralite ores: owner will revisit (see above). Glossiness is a shader-only thing; owner will explain later, don't do it yet.
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
