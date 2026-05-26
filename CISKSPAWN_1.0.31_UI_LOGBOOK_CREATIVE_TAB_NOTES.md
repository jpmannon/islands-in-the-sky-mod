# CISKSPAWN 1.0.31 — UI / Logbook / Creative Tab Pass

## Implemented
- Added a dedicated creative inventory tab: **Create: Islands in the Sky**.
- Added `ciskspawn:crew_logbook`.
- Added `ciskspawn:guilded_compass`.
- Crew Logbook item opens a guidebook-style UI page for mood, relationship, clues, and crew notes.
- Removed Crew Log / Relationship / Clue options from NPC dialogue menus.
- Added a **Goodbye** button to NPC dialogue menus.
- Removed visible workbench/workstation-style options from NPC UIs.
- Dialogue frequency for Mortimer, Geera, Scoria, and Azerion was reduced to roughly half of Mortimer’s previous frequency.

## Asset note
I did not receive separate new Scoria/Azerion texture or animation files in this request beyond the already-present Scoria assets and the previously uploaded `rook.geo.json`. This build keeps the current bundled assets and the Azerion placeholder geometry.

## Test checklist
1. Build succeeds.
2. Game loads.
3. Open Creative Mode inventory and find the **Create: Islands in the Sky** tab.
4. Confirm `Crew Logbook` appears.
5. Confirm `Guilded Compass` appears.
6. Right-click Crew Logbook and confirm the guidebook UI opens.
7. Summon Mortimer, Geera, Scoria, and Azerion.
8. Right-click each NPC.
9. Confirm no NPC menu has Crew Log / Relationship / Clues buttons.
10. Confirm every NPC menu has a Goodbye option.
11. Confirm Geera shop still opens and has shop actions plus Goodbye.
12. Confirm ambient dialogue feels much less frequent.
13. Confirm Scoria and Azerion still render with their current assets.
