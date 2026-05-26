# CISK Spawn 1.0.24 Test Checklist

Focus build: finishing the already-working lightweight RPG systems.

## Build and Load
1. `./gradlew build` succeeds.
2. Minecraft reaches main menu.
3. Existing test world loads.
4. No new startup crash.

## Geera Model
5. `/summon ciskspawn:geera`.
6. Confirm Geera uses the latest scaled model.
7. Confirm texture still maps correctly.
8. Confirm idle/walk animations still look correct.

## Mortimer + Geera Couple Dialogue
9. `/summon ciskspawn:storykeeper` near Geera.
10. Stand within 6 blocks and wait.
11. Confirm couple dialogue triggers from either NPC.
12. Confirm small emote/thought bubble appears with some couple lines.
13. Confirm dialogue is sweet/funny, not too spammy.

## Relationship / Trust UI
14. Right-click Mortimer.
15. Click Relationship.
16. Confirm it opens a full styled UI page, not just chat.
17. Confirm trust tier and unlock ladder display.
18. Advance Mortimer quests if possible and verify trust text changes eventually.

## Travel Dialogue
19. Mount/seat Mortimer using the known working 1.0.12-style seat path if possible.
20. Travel or wait near him while mounted.
21. Confirm occasional travel lines appear and no crash occurs.
22. If seating still fails, note that this build did not focus on seat fixes.

## Thought Bubbles / Emotes
23. Wait near Mortimer and Geera.
24. Click Mood bubble in each UI.
25. Confirm custom name temporarily changes to an icon.
26. Confirm normal names return after a few seconds.

## Logbook / Persistence
27. Open Mortimer Crew Log.
28. Open Geera Crew Log.
29. Confirm new counters appear: couple lines, emotes, work sessions, logbook opens, travel lines.
30. Save/quit/reopen world.
31. Re-open logbooks and confirm counters did not reset unexpectedly.

## Regression Checks
32. Mortimer UI still opens.
33. Geera UI still opens.
34. Geera shop page still opens.
35. Geera fishing quest still works.
36. Couple dialogue does not break normal ambient dialogue.
