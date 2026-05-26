# Mortimer Implementation Notes 1.0.6

This build continues the companion-system work while the Create Aeronautics seat behavior is still being investigated.

## Added

- Discuss Work now actually advances/handles Mortimer's prototype quest chain.
- Added **Follow me** button in the dialogue UI.
- Added **Scan seats** button in the dialogue UI.
- Added **H** keybind for seat diagnostics while looking at Mortimer.
- Added persistent temporary follow behavior.
- Mortimer gains tiny trust over time while riding/traveling near the player.
- Seat scan prints nearby relevant entity and block registry IDs to chat.

## Why Seat Scan Exists

Create Aeronautics may represent moving ships using contraptions/sublevels rather than normal world blocks/entities. If Mortimer can board seats on the ground but not on moving ships, the scan output tells us what Minecraft can actually see near the player.

When testing on a ship:

1. Look at Mortimer.
2. Press **H** or click **Scan seats**.
3. Copy the chat output.
4. Send the entity/block registry IDs back for the next integration pass.

## Current Expected Limitations

- Ground seats should work better.
- Moving ship seats may still fail until we identify the Create Aeronautics runtime representation.
- Sitting animation support is wired through GeckoLib, but final posture may need model/animation adjustment.
