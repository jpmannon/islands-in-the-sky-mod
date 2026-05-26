# Mortimer Implementation Notes

This version starts the Mortimer companion systems.

## Added

### Updated Mortimer assets
- Replaced the GeckoLib model with the latest exported `storykeeper.geo.json`.
- Replaced the GeckoLib animation file with the latest exported `storykeeper.animation.json`.
- Replaced the texture with the latest 256x256 Mortimer texture.
- Updated the entity display name to `Mortimer - Aeromancer`.
- Adjusted the entity hitbox to better match the larger model.

### Ambient dialogue system
Mortimer now occasionally speaks nearby ambient lines as a floating nameplate above his head.

The ambient system currently reacts to:
- nearby players
- rain/weather
- hunger
- nearby Create blocks
- whether Mortimer is riding/traveling

This is intentionally lightweight and can later be replaced by a proper custom floating UI.

### Basic floating dialogue
Right-clicking Mortimer displays dialogue in chat and shows a temporary floating line above him.

This is the first version of the non-invasive floating UI idea. It does not yet use the custom graphic mockup.

### First quest chain prototype
Current quest stages:

1. **Tea and Torque**
   - Requires `create:builders_tea` x1
   - Requires `create:andesite_alloy` x2
   - Requires `create:copper_sheet` x1

2. **Hull Groans**
   - Requires `create:shaft` x4
   - Requires `create:cogwheel` x4
   - Requires `minecraft:copper_ingot` x6

3. **The Abalone**
   - Reveals the main ship-restoration campaign as a placeholder.

### Relationship progression prototype
Mortimer stores a hidden trust value per player on the entity.

Current relationship labels:
- Stranger
- Guild Associate
- Trusted Pilot
- Friend
- Crew

This data is saved inside Mortimer’s entity NBT.

### Travel prototype
Shift + right-click Mortimer while the player is mounted makes Mortimer try to ride the same vehicle.

This is a first-pass test for future Create Aeronautics seat/airship behavior.

## Important limitations

- The proper floating UI graphic is not implemented yet.
- Dialogue is still hardcoded in Java.
- Quest progression is stored on Mortimer’s entity, not globally.
- Mortimer’s airship travel behavior is generic and may not work with all Create Aeronautics seats yet.
- FTB Quests integration has not been added yet.

## Recommended next development step

Move dialogue and quests into JSON files:

- `data/ciskspawn/mortimer/dialogue.json`
- `data/ciskspawn/mortimer/quests.json`
- `data/ciskspawn/mortimer/relationship.json`

Then add a custom client-side floating UI renderer using the dialogue-box graphic.
