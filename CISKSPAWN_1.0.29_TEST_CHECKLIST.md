# CISK Spawn 1.0.29 Test Checklist - Scoria Asset Pass

## Build / Load
1. Run `./gradlew build` successfully.
2. Put the built jar into the modpack `mods` folder.
3. Launch the game and load a test world.

## Scoria Model
4. Run `/summon ciskspawn:scoria`.
5. Confirm Scoria uses the new model, not the old placeholder/Geera-like model.
6. Confirm the Scoria texture loads correctly.
7. Confirm his size feels reasonable beside Mortimer and Geera.
8. Confirm his hitbox/selection box feels usable.

## Scoria Animations
9. Watch Scoria while idle for at least 30 seconds.
10. Confirm idle animation plays.
11. Confirm idle2 sometimes appears as a variation.
12. Nudge/path Scoria or let him wander.
13. Confirm walk animation plays while moving.
14. Confirm idle and walk are not swapped.

## Scoria Dialogue / Quest
15. Right-click Scoria.
16. Confirm his UI opens.
17. Click Engineering lesson.
18. Confirm dialogue appears in chat and above him.
19. Click About Mortimer.
20. Confirm Scoria ruse/engineer twist dialogue still works.
21. Check Crew Log / Scoria tracker if available from the UI.

## Regression Check
22. Summon Mortimer and Geera.
23. Confirm their models still load.
24. Confirm Geera UI still opens.
25. Confirm Mortimer UI still opens.
26. Confirm no crash appears in latest.log.

## Known Notes
- Azerion remains scaffolded/placeholder until a proper custom Blockbench/GeckoLib model is supplied.
- Mortimer seating is still later polish.
