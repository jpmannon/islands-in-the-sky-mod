# 1.0.21 Notes

Focus: continue the lightweight RPG systems without touching the currently best broad seat-integration branch too aggressively.

Changes:
- Geera's shop now attempts to hand out real items when buying bait.
  - Priority ids: `starcatcher:almighty_worm`, `starcatcher:seeking_worm`, `starcatcher:cherry_bait`, fallback `minecraft:string`.
- Geera shop ledger now tracks bait bundles sold and rumors traded.
- Geera crew log includes shop ledger values.
- Geera shop interaction now activates a short workstation/operation state and tries to find a nearby barrel/chest/crate/table/water edge.
- Mortimer relationship screen now lists simple unlock tiers.
- Mortimer crew log now tracks guild-routine mentions.
- Mortimer now has a lightweight physical guild-routine hook: on some routine days he may path toward nearby lecterns, bells, smithing tables, crafting tables, Create depots, gearboxes, or workbench-like blocks.

Known:
- This does not yet implement a full Scoria entity.
- This does not yet implement full physical schedules between named destinations.
- This does not solve Mortimer's seat issue; seating should be compared against 1.0.20/1.0.12 behavior.
