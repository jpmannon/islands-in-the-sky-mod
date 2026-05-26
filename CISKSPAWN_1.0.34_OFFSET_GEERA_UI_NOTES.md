# CISKSPAWN 1.0.34 — Offset + Geera UI Cleanup

## Changes
- Azerion render offset raised by 2 blocks from 1.0.33.
- Scoria render offset raised by 1 block from 1.0.33.
- Removed Geera's “Who are you?” option from her UI.
- Geera's identity/about text is treated as part of her intro/main dialogue instead of a separate button.
- Kept Crew Logbook and relationship/clue details out of the NPC UI.

## Test checklist
1. Build succeeds.
2. Game loads.
3. `/summon ciskspawn:azerion_rook`
4. Azerion is no longer two blocks too low.
5. Azerion texture is the updated rook texture.
6. `/summon ciskspawn:scoria`
7. Scoria is no longer one block too low.
8. Scoria scale still feels okay.
9. `/summon ciskspawn:geera`
10. Right-click Geera.
11. Confirm there is no “Who are you?” button.
12. Confirm Geera still has:
   - Fishing work
   - Open shop
   - Fishing tips
   - Goodbye
13. Confirm Crew Logbook still has tabs/options.
