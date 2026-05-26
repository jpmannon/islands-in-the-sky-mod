# Mortimer 1.0.8 Notes

Changes in this pass:

- Ambient dialogue frequency reduced by about half.
- Sitting animation JSON converted to explicit `0.0` keyframes so GeckoLib should read it more reliably.
- Visual chair sitting now lasts longer, stops navigation while sitting, and snaps Mortimer to the chair block center for easier testing.
- Added reflection-based Create contraption seat detection.
  - Looks for nearby entities with `getContraption()` and `addSittingPassenger(Entity, int)`.
  - Reads `getSeats()` and `getSeatMapping()` from the contraption to find a free seat index.
  - Attempts Create's own `addSittingPassenger` method instead of plain Minecraft `startRiding` when possible.
- This is still experimental for Create Aeronautics, but it should tell us whether the moving contraption passenger system is reachable from our mod.

Design note:

A custom Mortimer-only chair or heirloom compass is probably the clean long-term route. The best version may be:

- Mortimer gives the player a Guilded Compass.
- The compass can mark a ship as Mortimer-compatible.
- Mortimer only boards if the ship has either a valid empty seat or his own placed chair/marker.
- This avoids guessing every mod's seat implementation.

