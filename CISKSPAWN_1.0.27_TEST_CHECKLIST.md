# CISKSpawn 1.0.27 Test Checklist

## Build/load
1. Build with `./gradlew build`.
2. Launch the modpack.
3. Confirm the game reaches title/world load.

## Azerion
4. Run `/summon ciskspawn:azerion_rook`.
5. Confirm an entity appears. Current model is temporary placeholder until Sketchfab model conversion is complete.
6. Right-click Azerion.
7. Confirm the UI opens with title `Azerion Rook - AZ Mk 9`.
8. Press `Begin cannon drill`.
9. Confirm chat/dialogue mentions Create Big Cannons safety/training.
10. Press `Crew record`.
11. Confirm former Abalone crew lore appears.
12. Wait near Azerion and confirm ambient lines appear occasionally.

## Regression
13. Summon Mortimer and Geera.
14. Confirm their UI still opens.
15. Confirm Scoria still summons with `/summon ciskspawn:scoria`.
16. Confirm no startup crash.
