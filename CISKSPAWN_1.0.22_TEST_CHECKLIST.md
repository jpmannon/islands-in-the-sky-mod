# CISK Spawn 1.0.22 Test Checklist

## Build + Load
1. `./gradlew build` succeeds.
2. Game reaches main menu.
3. Existing world loads.
4. No new startup crash.

## Mortimer Core
5. `/summon ciskspawn:storykeeper` works.
6. Right-click Mortimer opens UI.
7. UI is taller and shows extra buttons.
8. Text wraps without major cut-off.
9. Mood bubble button works.
10. Relationship button still works.
11. Crew Log button still works.

## Mortimer New Buttons
12. Click **Guild status**.
13. Confirm it opens a new status page.
14. Confirm it shows day/guild routine information.
15. Click **Scoria clues**.
16. Confirm it opens Scoria tracker page.
17. Confirm it includes the banker misunderstanding, secret workshop, and aero-engineer reveal setup.

## Geera Core
18. `/summon ciskspawn:geera` works.
19. Right-click Geera opens UI.
20. Geera idle/walk still look correct.
21. Fishing work still works.
22. Shop button still opens shop UI.
23. Crew Log still opens.
24. Mood bubble still works.

## Geera New Buttons
25. Click **Shop status**.
26. Confirm it shows current day, open/closed status, bait bundles sold, and rumors traded.
27. Click **Work station**.
28. Confirm it shows a target position or says no station found.
29. Place barrel/chest/table/water near Geera and test Work station again.
30. On shop days, watch whether Geera wanders toward the nearby workstation.

## Mortimer + Geera Together
31. Spawn both within 6 blocks.
32. Confirm couple dialogue still triggers.
33. Confirm thought bubbles/emotes still appear.
34. Confirm neither UI breaks the other.

## Persistent-ish State
35. Buy bait or rumor from Geera.
36. Reopen Shop status and see counts update.
37. Advance Mortimer quest/trust if possible.
38. Reopen Scoria tracker and crew log.
39. Save/reload world.
40. Confirm counts/progress are still reasonable.

## Known Issues to Keep an Eye On
- Mortimer seat mounting is still the main unstable system.
- Geera shop is still a prototype, not a full container shop.
- Routine movement is anchor-based, not a full schedule system.
- Scoria tracker is narrative groundwork; Scoria entity/model is not implemented yet.
