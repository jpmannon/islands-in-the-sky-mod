# CISK Spawn Mod 1.0.25 Test Checklist

## Build + launch
1. Run `./gradlew build`.
2. Replace the old jar in the modpack `mods` folder.
3. Launch the pack and confirm it reaches the main menu.
4. Load a test world.
5. Watch `latest.log` for new errors mentioning `ciskspawn`.

## Regression checks from 1.0.24
6. `/summon ciskspawn:storykeeper` works.
7. `/summon ciskspawn:geera` works.
8. Mortimer right-click UI opens.
9. Geera right-click UI opens.
10. Geera's idle/walk animations still look correct.
11. Mortimer + Geera couple dialogue still triggers when they are within about 6 blocks.
12. Thought bubbles still appear for Mortimer and Geera.
13. Crew log pages still open.
14. Relationship/trust pages still open.

## New Scoria placeholder entity
15. Run `/summon ciskspawn:scoria`.
16. Confirm Scoria appears. He currently uses placeholder Geera-derived assets until the real model is ready.
17. Right-click Scoria.
18. Confirm Scoria UI opens.
19. Test **Engineering lesson**.
20. Test **About Mortimer**.
21. Test **Reveal clue** several times.
22. Test **Crew log**.
23. Confirm Scoria dialogue appears in chat and above his head.
24. Save and reload world, then check Scoria reveal/log state still behaves reasonably.

## Geera physical routine / station anchor
25. Spawn/place Geera near barrels, chests, tables, or water.
26. Open Geera UI > **Work station**.
27. Test **Set station here**.
28. Move Geera away a little.
29. Open Work station again and test **Call to station**.
30. Confirm she pathfinds/walks toward the station anchor.
31. Confirm Work Station page displays the manual anchor coordinates.
32. Wait on a shop day if possible, or use the Work Station page to confirm state updates.

## Geera shop economy
33. Open Geera shop.
34. Try **Buy bait** with no copper ingot or emerald: she should ask for payment.
35. Give yourself one copper ingot: `/give @p minecraft:copper_ingot 1`.
36. Buy bait again.
37. Confirm payment is removed and bait/string is added if registry IDs are present.
38. Try **Buy rumor**.
39. Confirm shop ledger/logbook counters update.

## Mortimer Guild routine / anchor
40. Open Mortimer UI > **Guild status**.
41. Test **Set guild point** near a workbench/lectern/bell/Create machine.
42. Move Mortimer away.
43. Open Guild status and test **Call to guild point**.
44. Confirm Mortimer pathfinds toward the guild point.
45. Confirm Guild Status page displays the manual anchor coordinates.
46. Confirm Mortimer still opens Relationship, Crew Log, and Scoria Clues.

## Scoria questline integration
47. Open Mortimer > Scoria clues.
48. Open Geera > Crew log and check Scoria notes.
49. Talk to Scoria and use Reveal clue.
50. Confirm the three NPCs feel narratively connected, even if Scoria is still placeholder visually.

## Seating regression
51. Test Mortimer chair/seat behavior from the previous working branch if needed.
52. Expect seating to remain imperfect; this build focuses on RPG systems and routines, not seat polish.

## Notes to report back
- Did Scoria summon/load?
- Did the Scoria placeholder model look acceptable for now?
- Did any UI button do nothing?
- Did Geera accept payment correctly?
- Did Geera pathfind to her manual station?
- Did Mortimer pathfind to his Guild point?
- Any new crash/error in latest.log?
