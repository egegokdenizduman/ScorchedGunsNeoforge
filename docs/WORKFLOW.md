# Workflow: how every task is done

Two gates. Nothing gets built before gate 1 is passed, and nothing gets batch-applied before gate 2 is passed. "Approved" means the owner (Ege) said yes in chat. Silence, a dismissed question, or my own judgement is not approval.

## Gate 1: Approve a checklist before building anything

Applies to every doc item, bug fix or feature, however small.

1. Read the item in the Google Doc, including every indented owner note under it. The notes override the item text and any earlier decision.
2. Write a checklist and post it in chat. Use the template below and stop there.
3. The owner corrects it. Repeat until they say it's approved.
4. Build only what's on the approved list. If something new comes up while working, stop and ask; don't add it.
5. When done, go through the list line by line against the doc wording. Mark each line done / not done / untested, and say why for anything not done.

### Checklist template

```
Item: <doc item name, quoted from the doc>
Doc says (quoted): "<the item text and owner notes, word for word>"

Will do:
- [ ] <one concrete, checkable change per line>
Will NOT do (out of scope or a later update):
- <anything nearby I'm deliberately leaving alone>
Questions:
- <anything ambiguous; no guessing>
How you can check it in game:
- <what to look at or try>
```

### Rules
- One line per change, written so the owner can verify it ("flechette recipe no longer uses anthralite"), not vague ("improve flechette").
- Quote the doc; don't paraphrase it into something else.
- Items the doc marks for a later update (e.g. "lets leave it for now", "future update") go under "Will NOT do".
- Several small items can share one checklist message, but each keeps its own list.

## Gate 2: One sample before any batch

Applies whenever the same kind of change is going to hit more than one file: textures (always), models, particles, recipes following a new pattern, lang changes, and so on.

1. Make exactly one sample, picking the most representative file of the batch.
2. Show it for review:
   - **Textures:** an upscaled (nearest-neighbour, 8× or more) image with three panels side by side: the old texture, the new one, and the real vanilla/Create reference it should match (never programmer art). Also show it at 1× / inventory size.
   - **Code or data patterns:** the one file or diff, plus a list of every other file the pattern would be applied to.
3. Wait for approval. If the owner asks for changes, redo the sample and show it again.
4. Apply the approved style to the rest of the batch.
5. Show a contact sheet of the whole batch (old vs new) so the owner can spot any outliers. Fix those before committing.

### Rules
- Never change a texture's shape or silhouette unless the doc asks for it. Recolouring and cleanup are not permission to redesign.
- A sample counts as approved for its own batch only, not for other batches that happen to look similar.
- If a batch turns out to need something the sample didn't show (a different shape, a new palette), stop and get a new sample approved.

## Texture rules (checked at Gate 2)
Summary of the doc's "Bible of Texture Design" tab:
- 16×16, no mixels, binary alpha, light from the top-left.
- Darker, hue-tinted outline, lighter top-left and darker bottom-right.
- Hue-shifted ramps of 3–5 shades.
- No pillow or pancake shading, no banding.
- Deliberate dithering where the material needs it.

Further rules:
- Compare against the real vanilla/Create textures, never programmer art.
- Things made from a material use that material's palette.
- Ingots and nuggets follow vanilla's angled 3/4 shape, and every nugget has a distinct silhouette (for colour-blind readability).
- Ores never touch the block edge, sit in darkened "indent" stone, and are dithered.

## What counts as done
- Compiles: `./gradlew compileJava --offline` passes. Say "compiles", not "works".
- Tested: only after the owner, or a run of the dev client, has shown it working in game.
- Committed on a branch; pushed or merged only when the owner asks.
