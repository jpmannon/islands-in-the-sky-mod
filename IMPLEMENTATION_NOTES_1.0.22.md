# CISK Spawn 1.0.22 Notes

This build continues the lightweight RPG systems from 1.0.21 without touching the fragile Mortimer seating branch.

## Added
- Mortimer UI now has extra buttons for Guild status and Scoria clues.
- Geera UI/shop UI now has extra buttons for Shop status and Work station status.
- Mortimer Guild status page shows current day and whether he is on a possible Guild routine day.
- Scoria clue tracker now exists as a UI page.
- Geera shop status page shows day, open/closed status, bait sold, and rumors traded.
- Geera workstation status page shows her current routine target.

## Design Intent
The goal is to make the RPG layer feel discoverable and optional:
- players can ignore it and build/fly,
- or they can check in with NPCs for relationship, shop, logbook, and story progress.

## Still Not Final
- Geera shop is not a real container/trading GUI yet.
- Mortimer Guild visits are still routine/status hooks, not full physical schedules.
- Scoria is not an entity yet.
- Thought bubbles currently use floating name/emote text, not custom rendered icons.
