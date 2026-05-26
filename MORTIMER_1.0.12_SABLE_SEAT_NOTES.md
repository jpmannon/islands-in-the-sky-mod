# Mortimer 1.0.12 – Sable / Create Seat Integration Test

## Purpose
This build is focused on the root cause of Mortimer not finding or mounting seats, especially seats placed on Create Aeronautics / Sable physics ships.

## Main discovery
The previous G/H keybind behavior required the player to look directly at Mortimer. That meant Mortimer could not also use the player's crosshair to detect the chair or seat block. This likely explains why looked-at seat targeting failed during testing.

## Changes in this build

### 1. G/H keybind target behavior changed
- If the player is looking directly at Mortimer, the keybind targets that Mortimer.
- If the player is looking at a chair/seat/block instead, the keybind now finds the nearest Mortimer within range and sends the action to him.

This allows the intended workflow:
1. Stand near Mortimer.
2. Look directly at a chair/seat.
3. Press G.
4. Mortimer should use the player's crosshair target as the preferred seat.

### 2. Broader seat block scanning
Mortimer now scans around both:
- the player
- Mortimer himself

This is meant to help with cases where Sable/Create ship coordinate spaces make visually nearby blocks technically offset.

### 3. Direct Create seat logic preserved
For normal Create seats, Mortimer still attempts Create's own `SeatBlock.sitDown(level, pos, entity)` via reflection.

### 4. Generic Create SeatEntity fallback
For non-Create chairs or decorative seats, Mortimer now attempts to spawn Create's invisible `SeatEntity` manually at the chair position, then ride it.

This should help with:
- Create Bits n Bobs chairs
- decorative chairs
- seat-like blocks that do not expose their own ride entity

### 5. Experimental Sable sublevel scan
Mortimer now attempts to reflectively inspect loaded Sable sublevels and scan their loaded chunks for seat-like block IDs.

This is experimental. It is mostly for diagnostics right now, but if it finds ship seats in Sable plot coordinates, it may also allow Mortimer to mount them using a generated Create SeatEntity.

### 6. H scan improved
The H key now also prints Sable sublevel debug info:
- number of loaded Sable sublevels scanned
- any seat-like blocks found inside sublevel chunks
- first few block IDs and positions

## Important test cases

### Test A – normal ground Create seat
1. Place a Create seat on normal ground.
2. Stand near Mortimer.
3. Look directly at the seat.
4. Press G.

Expected:
- Mortimer walks to it or mounts it.
- If mount succeeds, he should play sit animation.

### Test B – decorative chair on ground
1. Place Create Bits n Bobs chair or other chair.
2. Look at the chair.
3. Press G.

Expected:
- Mortimer should use the generic Create SeatEntity fallback.

### Test C – chair on Sable / Create Aeronautics ship
1. Stand on the ship near Mortimer.
2. Look directly at the chair/seat.
3. Press H first and save the output.
4. Press G.

Expected:
- If the block raycast reaches Sable ship blocks, Mortimer should target that block.
- If not, H should reveal whether Sable sublevel scanning sees any seats.

## Known limitations
- True Sable passenger integration is still experimental.
- Sable sublevel coordinate transforms are not fully solved yet.
- If Sable stores the visible chair in transformed local coordinates and the player crosshair does not return the sublevel block position, we may need a dedicated Mortimer chair or Guilded Compass system.

## Long-term fallback design
If Sable/Create Aeronautics passenger mounting remains fragile, the clean gameplay solution is:

### Mortimer's Guild Chair
A special chair/block placed by the player on the airship.
Mortimer recognizes it as his assigned seat.

### Guilded Compass
An heirloom item from Mortimer.
Functionally, it could:
- call Mortimer to the player
- assign his seat
- mark the Abalone restoration objective
- track Spawn City / home island

Narratively, it fits his trauma around navigation drift and his fear of repeating the Abalone's final mistake.
