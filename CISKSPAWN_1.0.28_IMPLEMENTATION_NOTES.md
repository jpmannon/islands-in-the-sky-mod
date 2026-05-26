# CISK Spawn Mod 1.0.28 Notes

Implemented requested UI/routine tweaks:

- Moved NPC mood information into the crew logbook pages.
- Removed visible Mood Bubble buttons from the NPC dialogue UI.
- Geera's bait-and-tackle shop now opens every 3rd Minecraft day.
- Geera's workstation is now based on the position where `/summon ciskspawn:geera` is run.
- Removed the player-facing Set Station workflow for Geera.
- Added persistence overrides to Mortimer, Geera, Scoria, and Azerion so they should not naturally despawn.
- Reduced Mortimer ambient dialogue frequency by roughly 10%.

Notes:
- The crew log is still implemented as a styled UI page, not a physical Patchouli-style book item yet.
- This keeps the system lightweight while moving it toward a typical modded guidebook structure.
- Azerion remains scaffolded/placeholder until a clean custom model is provided.
