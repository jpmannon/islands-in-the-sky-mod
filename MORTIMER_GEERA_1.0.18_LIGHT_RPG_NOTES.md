# CISK Spawn Mod 1.0.18 - Mortimer + Geera Lightweight RPG Pass

Built from 1.0.17 Geera UI source, keeping the working 1.0.12 Sable/Create seat logic direction rather than the custom Mortimer chair branch.

## Added / changed

### 1. Updated Geera model pass
- Replaced Geera assets with the latest uploaded Geera model, texture, and animation files.
- Added a small renderer scale bump for Geera so she appears less tiny in-game.

### 2. Mortimer + Geera couple dialogue
- When Mortimer and Geera are within a few blocks, they can now trigger occasional ambient couple dialogue.
- Tone target: sweet, teasing, sarcastic, domestic, lightly funny.
- Geera can tease Mortimer about banks, rope, lunch, and him being dramatic.
- Mortimer can respond to Geera occasionally.

### 3. Basic NPC routine hooks
- Geera has a simple bait-and-tackle shop day system.
- Mortimer has simple Guild meeting / Guild board ambient routine lines.
- These are intentionally lightweight and mostly dialogue-driven for now.

### 4. Geera bait and tackle stall prototype
- Geera has a UI button for shop status.
- Every 12 Minecraft days she considers the stall open.
- This is currently dialogue/status only, not a full trade GUI yet.

### 5. Mortimer Guild visit prototype
- Mortimer occasionally mentions Guild meeting days and old crew business.
- This is a foundation for later Guild Hall / old crew NPC routines.

### 6. Relationship / trust menu
- Mortimer now has a Relationship UI button.
- It prints the current trust tier and a small characterful line.
- Existing tiers remain: Stranger, Guild Associate, Trusted Pilot, Friend, Crew.

### 7. Travel dialogue foundations
- Mortimer has more aboard/ship-related ambient lines.
- Scoria-related advanced engineering hints can appear while traveling.

### 8. Scoria storyline groundwork
- Scoria is now referenced in Mortimer and Geera dialogue/logbook entries.
- Canon in this build: Scoria is not becoming a banker. He is secretly apprenticing as an aero-engineer.
- Geera knows and finds the misunderstanding funny.
- Future direction: Scoria teaches advanced Create/airship tech after the reveal.

### 9. Thought bubble / emote placeholder
- Still uses floating name text and chat for visibility.
- Future: replace some ambient lines with small icons like fish, tea, wrench, storm cloud, heart.

### 10. Crew log / journal prototype
- Mortimer and Geera both now expose a simple Crew Log through their UI.
- It prints current lore/progression hints in chat.
- Future: convert to a real book/menu screen.

## Known limitations
- Workstation routines are not physical pathfinding schedules yet. They are dialogue/status hooks.
- Geera's shop does not yet open an inventory/trade GUI.
- Couple dialogue may need cooldown tuning after testing.
- Geera scale may still need hitbox/eye-height tuning.
- Scoria is not implemented as an entity yet.

## Suggested test checklist
1. Summon Mortimer and Geera near each other.
2. Wait for couple dialogue to trigger.
3. Right-click Mortimer and test Relationship and Crew Log buttons.
4. Right-click Geera and test Shop Status and Crew Log buttons.
5. Confirm Geera scale looks better.
6. Confirm Mortimer seat logic still behaves like 1.0.12/1.0.17 branch.
