# CISK Spawn Mod 1.0.28 Test Checklist

## Build + Load
1. Run `./gradlew build` successfully.
2. Put the built jar into the modpack `mods` folder.
3. Confirm the game reaches the main menu.
4. Confirm an existing world loads.

## NPC Persistence
5. Summon Mortimer: `/summon ciskspawn:storykeeper`.
6. Summon Geera: `/summon ciskspawn:geera`.
7. Move far away / reload chunks / relog if convenient.
8. Confirm they do not naturally despawn.

## Mortimer UI + Crew Log
9. Right-click Mortimer.
10. Confirm there is no **Mood bubble** button in the main UI.
11. Open **Crew log**.
12. Confirm the logbook includes a **Mood:** line.
13. Confirm the page still includes trust, Abalone, Geera, Scoria, Guild, and travel notes.
14. Wait near Mortimer and check that ambient dialogue feels slightly less frequent.

## Geera UI + Crew Log
15. Right-click Geera.
16. Confirm there is no **Mood bubble** button in her UI.
17. Open **Crew log**.
18. Confirm the logbook includes a **Mood:** line.
19. Confirm shop ledger and Scoria notes still appear.

## Geera Shop Schedule
20. Open Geera shop/status page.
21. Confirm text says her shop opens every 3rd Minecraft day.
22. Test day 0, 3, 6, etc. if possible with `/time set`.
23. Confirm non-shop days show closed status.
24. Confirm shop days show open status.

## Geera Workstation Anchor
25. Summon Geera near the intended bait-and-tackle location.
26. Open **Work station**.
27. Confirm the station location is based on her summon position.
28. Confirm there is no option to manually set the workstation.
29. Use **Call to station** and confirm she tries to return to that summon-location anchor.

## Regression Checks
30. Mortimer + Geera couple dialogue still triggers when they are close.
31. Geera fishing quest still opens.
32. Geera shop buttons still give testing feedback/items.
33. Scoria and Azerion entities still summon if included in the build.
34. Check latest.log for new `ERROR` lines from ciskspawn.

## Known Notes
- Azerion still uses placeholder/scaffolded assets until a clean Blockbench model is supplied.
- Mortimer seat/mounting polish is intentionally not the focus of this build.
