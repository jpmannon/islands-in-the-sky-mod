# CISKSPAWN 1.0.32 — Scoria + Azerion Model Swap

## What changed
- Replaced Azerion geometry with the updated `rook.geo(1).json`.
- Replaced Scoria geometry with the updated `Scoria.geo.json`.
- Kept the current bundled textures and animation files from 1.0.31.

## Important honesty note
Only `.geo.json` files were uploaded for this pass. If textures or animations were also changed in Blockbench, upload those separately and they should be added in the next build.

## Expected test focus
- Azerion may still need texture, scale, pivot, and animation tuning.
- Scoria should be closer to final, but may still need hitbox and eye-height tuning.
- If either entity appears invisible, tiny, huge, rotated, or untextured, the entity code may be fine and the asset export needs another pass.

## Test checklist
1. Build succeeds.
2. Game loads.
3. Creative tab still appears.
4. Crew Logbook appears in the creative tab.
5. Guilded Compass appears in the creative tab.
6. `/summon ciskspawn:scoria`
7. Scoria appears with the updated model.
8. Scoria texture maps correctly.
9. Scoria idle animation plays.
10. Scoria idle2 randomly appears over time.
11. Scoria walk animation plays when moving.
12. Scoria hitbox feels reasonable.
13. `/summon ciskspawn:azerion_rook`
14. Azerion appears with the updated model.
15. Azerion texture maps at least acceptably.
16. Azerion scale is checked.
17. Azerion is not buried underground or floating badly.
18. Azerion right-click UI works.
19. Azerion artillery/CBC dialogue still appears.
20. latest.log has no new missing model/texture errors.
