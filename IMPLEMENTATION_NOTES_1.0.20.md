# CISK Spawn Mod 1.0.20 Notes

Based on the 1.0.19 light RPG branch.

## Focus
- Keep the 1.0.12-derived seat logic branch rather than the experimental custom chair branch.
- Fix Geera's idle/walk animation mapping.
- Add first-pass thought bubble/emote system.
- Add a proper styled Geera shop UI pass.
- Add persistent-style crew logbook UI screens.

## Changes

### Geera
- Replaced Geera geo, animation, and texture assets with the latest uploaded files.
- Swapped Geera animation mapping because the tested idle/walk appeared reversed.
  - Idle now plays the exported `walk` animation.
  - Movement now plays the exported `animation` animation.
- Added Geera thought bubbles/emotes.
  - Automatic occasional icons: fish, hook, tea, heart, rain.
  - Manual `Mood bubble` button in her UI.
- Added `Geera's Bait & Tackle` styled UI screen.
  - `Open shop` button opens a shop-style menu.
  - Buttons: Buy bait, Buy rumor, Fishing work, Crew log, Mood bubble, Goodbye.
  - Current shop is still prototype feedback, not real item trades yet.
- Geera crew log now opens the custom UI instead of only printing to chat.
- Geera shop state remains tied to the 12-day prototype schedule.

### Mortimer
- Added Mortimer thought bubbles/emotes.
  - Automatic occasional icons: wrench, tea, gear, thought, compass.
  - Manual `Mood bubble` button in Mortimer UI.
- Mortimer crew log now opens a custom styled logbook UI instead of only chat messages.
- Logbook page is rebuilt from Mortimer's saved trust and quest-stage data.

### UI
- Added special handling for:
  - Crew Logbook screens
  - Geera's Bait & Tackle shop screen
- Crew logbook screens use the same brass/teal Create-style interface and a single close button.
- Geera's shop screen uses the same visual language as Mortimer and Geera dialogue.

## Known Limitations
- Shop GUI is a visual/prototype interaction layer, not a container/trading screen yet.
- Thought bubbles use temporary custom names, not custom rendered icons yet.
- Persistent logbook is currently derived from saved NPC progress, not a separate world save file.
- Geera's animation mapping may need reversing again if the new uploaded animation export behaves differently in-game.

## Test Checklist
1. `/summon ciskspawn:geera`
2. Confirm Geera model scale looks right.
3. Confirm Geera idles correctly and walks correctly.
4. Right-click Geera.
5. Open Geera's shop.
6. Test Buy bait / Buy rumor / Mood bubble.
7. Open Geera crew log.
8. Right-click Mortimer.
9. Open Mortimer crew log.
10. Test Mortimer Mood bubble.
11. Let both NPCs idle near player and check occasional emote bubbles.
