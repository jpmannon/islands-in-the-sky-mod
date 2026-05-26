# CISKSPAWN 1.0.30 — Azerion Placeholder Model Pass

## What changed
- Replaced the old Azerion placeholder geometry with the uploaded `rook.geo.json`.
- This is still a placeholder pass. The model may need scale, pivot, texture, and animation tuning in Blockbench.

## Important
- Only the geometry file was provided in this pass.
- If Azerion appears untextured, wrongly scaled, rotated, or invisible, upload:
  - `azerion.png`
  - `azerion.animation.json`
  - any corrected `.geo.json`

## Test checklist
1. Build succeeds.
2. Game loads.
3. Run `/summon ciskspawn:azerion_rook`.
4. Check whether the model appears.
5. Check:
   - scale
   - rotation
   - texture mapping
   - whether he is on the ground
   - whether hitbox feels reasonable
6. Right-click Azerion and check the dialogue/CBC quest prototype.
7. Check latest.log for missing texture/model errors.

## Expected issues
- The model may still need cleanup because the uploaded geometry appears to be converted from the Sketchfab/Blockbench placeholder and has a complex bone hierarchy.
- If it looks weird, that does not mean the entity is broken. It means the asset needs another pass.
