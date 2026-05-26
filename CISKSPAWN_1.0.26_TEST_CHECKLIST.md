# CISK Spawn 1.0.26 Test Checklist

## Build + load
1. Build succeeds.
2. Game launches.
3. Existing world loads.
4. No new startup crash in `latest.log`.

## Regression check from 1.0.24/1.0.25
5. Summon Mortimer: `/summon ciskspawn:storykeeper`.
6. Summon Geera: `/summon ciskspawn:geera`.
7. Summon Scoria: `/summon ciskspawn:scoria`.
8. Mortimer UI opens and existing pages still work.
9. Geera UI opens and existing pages still work.
10. Scoria UI opens and existing pages still work.

## Geera shop economy
11. Right-click Geera and open her shop.
12. Confirm shop text lists prices/stock/ledger.
13. Try Buy Bait with no copper/emerald: she should refuse.
14. Give yourself copper: `/give @p minecraft:copper_ingot 4`.
15. Buy bait: should remove one copper and give a Starcatcher bait if available, otherwise fallback string.
16. Try Buy Rumor: should display one fishing/family rumor.
17. Give yourself a requested Starcatcher fish or quest fish if available.
18. Use Sell Catch: she should remove accepted Starcatcher quest fish and pay copper.
19. Reopen Geera Crew Log: ledger should mention bait sold, rumors, and fish bought.

## Geera physical routine
20. Open Geera Workstation page.
21. Use Set station here near a barrel/chest/table/water/dock build.
22. Use Call to station.
23. Wait and verify Geera pathfinds toward the station.
24. Watch for occasional shop/workstation ambient lines.

## Mortimer guild routine
25. Open Mortimer Guild Status.
26. Use Set guild point near a workbench/lectern/Create block.
27. Use Call to guild point.
28. Verify Mortimer pathfinds toward the guild point.
29. Check Guild Status again for counters/text.

## Scoria entity and questline
30. Right-click Scoria.
31. Test Engineering Lesson.
32. Test About Mortimer.
33. Test Reveal clue multiple times.
34. Open Scoria Crew Log.
35. Open Scoria Workshop.
36. Use Set workbench near Create/redstone blocks.
37. Use Advance project with no materials: should tell you the needed items.
38. Stage 0 materials: `/give @p create:andesite_alloy 4` and `/give @p minecraft:redstone 4`, then Advance project.
39. Stage 1: try `/give @p create:precision_mechanism 1` or `/give @p create:cogwheel 6`, then Advance project.
40. Stage 2: try `/give @p create:brass_ingot 2` or `/give @p minecraft:copper_ingot 8`, then Advance project.
41. Stage 3/final: Advance project and check final reveal/prototype reward text.
42. Reopen Scoria log to see counters updated.

## Persistence
43. Save and quit.
44. Reload world.
45. Check Geera shop ledger remains.
46. Check Scoria project/reveal progress remains.
47. Check Mortimer trust/logbook still works.

## Known not-yet-finished
- Mortimer seating/mounting is still separate and not the focus of this build.
- Scoria still uses placeholder model assets unless replaced with final exports.
- Geera shop is still dialogue-based, not a true container/trading menu.
- Guild crew NPCs are not fully separate entities yet; this build focuses on anchors, routines, and content hooks.
