# Mortimer Systems 1.0.4 Test Notes

## Added / changed

- Replaced the old shift-right-click travel test with a default keybind:
  - **G** while looking at Mortimer = ask Mortimer to board/travel.
- Right-clicking Mortimer now opens a first-pass custom UI instead of only printing chat.
- The UI is intentionally non-fullscreen and styled as a floating Aero Guild / Create-inspired projection panel.
- Dialogue options currently include:
  - Discuss work
  - Travel / Board
- Ambient lines now also print to chat for testing visibility.
- Mortimer's renderer has a temporary upward visual offset because the current model export appears about one block too low in-game.

## Known temporary hacks

- The model is visually translated upward in `StorykeeperRenderer` by `0.85D`.
  - Remove this later if the Blockbench origin/pivot is fixed.
- The UI is not yet truly rendered in-world. It is a lightweight screen positioned to feel like a floating side-panel.
- Travel/boarding is still a simple prototype. It tries to mount Mortimer to the player’s current vehicle.
- Ambient chat printing is intentionally noisy for testing and should later become configurable.

## Next likely steps

- Replace the temporary UI with a texture-backed panel using the final dialogue box art.
- Add real option branching / JSON dialogue trees.
- Make Mortimer detect Create Aeronautics seats properly.
- Add a debug/config toggle for ambient chat printing.
- Fix model origin in Blockbench and remove the renderer translate hack.
