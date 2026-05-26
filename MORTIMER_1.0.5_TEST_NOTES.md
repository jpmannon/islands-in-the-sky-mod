# Mortimer 1.0.5 Test Notes

Changes in this build:

- Added sit animation support. Mortimer should play `sit` while mounted.
- Updated Mortimer model, animation, and texture files from the latest exports.
- Removed/disabled normal Screen background rendering so the UI should no longer blur/darken the world.
- Lowered Mortimer render offset slightly from 0.85 to 0.78 because he was hovering a little.
- Changed G-key boarding logic:
  - Mortimer now searches for nearby seat/chair/stool-like entity IDs.
  - He avoids the player’s current mounted vehicle.
  - He walks to the open seat instead of teleporting.
  - When close enough, he attempts to mount it.

Important Create Aeronautics note:

- If seats on moving ships are inside a contraption/sublevel, normal nearby entity search may not see them.
- This build adds diagnostic chat when Mortimer cannot find or mount a ship seat.
- If ground seats work but moving ship seats do not, we probably need a Create Aeronautics-specific integration layer.
