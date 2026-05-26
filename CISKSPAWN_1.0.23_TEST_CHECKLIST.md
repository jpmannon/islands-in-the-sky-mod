# CISK Spawn 1.0.23 Test Checklist

## Build + Load
1. Build succeeds.
2. Game reaches main menu.
3. Existing world loads.
4. No new crash or broken mod state.

## Geera
5. `/summon ciskspawn:geera`.
6. Right-click opens Geera UI.
7. Shop page still opens.
8. Shop status page text mentions saved bait/rumor ledger.
9. Buy bait on shop day or test feedback without crash.
10. Buy rumor gives one of the newer Scoria/Guild hints sometimes.
11. Crew Logbook includes Fish Records, Shop Ledger, and Family Notes sections.
12. Geera workstation status still works near barrels/chests/tables/water.

## Mortimer
13. `/summon ciskspawn:storykeeper`.
14. Right-click opens Mortimer UI.
15. Guild Status page explains current routine anchor behavior.
16. Scoria Clue Tracker includes the new Guild Recognition stage.
17. Crew Logbook includes persistent pages for Abalone, Guild, Scoria, and Travel.
18. Relationship/trust still works.

## Couple / World Feel
19. Spawn Mortimer and Geera within 6 blocks.
20. Wait for couple banter.
21. Confirm ambient/emote frequency is not too spammy.

## Seating Regression
22. Check that this build has not intentionally changed seat logic from the current working branch.
23. If Mortimer mounting is still broken, do not treat that as a 1.0.23 regression unless it was working in 1.0.22.

## Log Check
24. After testing, check latest.log for new ciskspawn-related errors.
