# CISK Spawn 1.0.21 Test Checklist

## Build + Load
1. `./gradlew build` succeeds.
2. Copy the jar from `build/libs/` into the modpack `mods` folder.
3. Game reaches main menu.
4. Existing test world loads.
5. Check `latest.log` for new `ERROR` lines from `ciskspawn`.

## Geera
6. Run `/summon ciskspawn:geera`.
7. Confirm Geera model size and texture look correct.
8. Confirm idle animation is idle and walk animation is walking.
9. Right-click Geera and confirm her UI opens.
10. Click **Fishing work** and confirm quest dialogue works.
11. Click **Open shop** and confirm Geera's Bait & Tackle opens.
12. On a shop day, click **Buy bait** and check if items are added to inventory.
13. If not shop day, confirm she gives a closed-shop line instead of crashing.
14. Click **Buy rumor** and confirm rumor text appears.
15. Open Geera's crew log and confirm shop ledger numbers update after buying bait/rumors.
16. Place a barrel/chest/table/crate/water nearby and wait near Geera on a shop day; confirm she tries to move toward it.

## Mortimer
17. Run `/summon ciskspawn:storykeeper`.
18. Confirm model/texture/animations still work.
19. Right-click Mortimer and confirm UI opens.
20. Click **Relationship** and confirm trust tier + unlock text appears.
21. Click **Crew log** and confirm Scoria, Geera, Abalone, and guild routine notes appear.
22. Put a lectern/bell/smithing table/crafting table/Create depot nearby and wait for guild routine dialogue; confirm he may path toward it on routine triggers.
23. Check that ambient lines still appear but are not too spammy.
24. Check thought bubbles/emotes still appear.

## Mortimer + Geera Together
25. Spawn both within 6 blocks.
26. Wait for couple dialogue.
27. Confirm it does not spam too often.
28. Confirm neither UI breaks when both are nearby.

## Seating / Ships
29. Confirm previous 1.0.12-style seat behavior has not regressed too badly.
30. Test normal chair/seat blocks.
31. Test chair/seat blocks on physics ships.
32. Report whether Mortimer can still detect/mount the seats.

## Persistence
33. Buy bait or rumors from Geera.
34. Advance any quest/dialogue state.
35. Save and quit.
36. Reload the world.
37. Confirm Geera's log still shows saved shop ledger/progress.
38. Confirm Mortimer's trust/logbook still looks reasonable.

## Notes to Report Back
- Any build errors.
- Whether bait item was actually given.
- Whether Geera physically moved to a workstation.
- Whether Mortimer guild-routine movement triggered.
- Whether seat behavior changed compared to 1.0.20.
