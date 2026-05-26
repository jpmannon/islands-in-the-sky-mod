# CISK Spawn 1.0.26 Implementation Notes

This build focuses on finishing the lightweight RPG systems enough to test them as real gameplay loops, while leaving heavy polish for later.

## Added / expanded

### Geera shop economy
- Shop text now lists basic stock and prices.
- Bait bundle costs 1 copper ingot or 1 emerald.
- Bait purchase gives Starcatcher bait where possible, with a vanilla fallback.
- Added `Sell catch` action for accepted Starcatcher quest fish.
- Shop ledger now tracks bait bundles sold, rumors sold, and fish bought.

### Geera physical routine
- Existing bait-and-tackle station anchor is now more useful.
- Geera can be assigned a station, called to it, and will path toward it.
- Workstation sessions and ledger values persist on Geera.

### Mortimer Guild routine
- Existing Guild anchor/call behavior remains.
- Intended use: set a Guild point near a workbench, lectern, bell, smithing table, or Create machinery.
- Mortimer pathfinds to the Guild point and uses it as a placeholder for future guild hall/crew scenes.

### Scoria questline
- Scoria placeholder entity is expanded into a real prototype questline.
- Added workshop anchor and physical routine.
- Added `Advance project` tech chain:
  1. Stabilizer frame: andesite alloy + redstone.
  2. Timing assembly: precision mechanism or cogwheels.
  3. Pressure housing: brass or copper fallback.
  4. Reveal-ready prototype.
- Added saved project stage, reveal progress, and workshop session counters.
- Scoria ambient/workshop dialogue now reinforces the banker-ruse twist.

## Still intentionally lightweight
- Geera shop is not a true Minecraft merchant/container screen yet.
- Scoria has placeholder visual assets until the real model is exported.
- Mortimer's old crew are not separate entities yet.
- Mortimer seating/mounting remains separate from this RPG pass.
