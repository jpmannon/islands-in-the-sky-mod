# 1.0.15 Notes - Revert to 1.0.12 Seat Logic + Geera Prototype

## Base

This build is based on `1.0.12 Sable/Create seat source`, because that was the last build where Mortimer successfully interacted with seats from other mods / physics ships.

The custom Mortimer chair and Guilded Compass systems from later builds are intentionally not included in this branch.

## Mortimer

Kept:
- 1.0.12 Sable/Create seat logic
- G key seat targeting behavior
- Mortimer dialogue UI
- Mortimer ambient dialogue
- Mortimer follow/boarding prototype

Known status from testing:
- Mortimer can mount at least some Bits/Create-style chair blocks on physics ships.
- Seating can still look buggy, but this branch is the best integration base so far.

## Geera

Added a first-pass Geera entity:

```mcfunction
/summon ciskspawn:geera
```

Assets added:
- `assets/ciskspawn/geo/geera.geo.json`
- `assets/ciskspawn/textures/entity/geera.png`
- `assets/ciskspawn/animations/geera.animation.json`

Geera is registered as:

```text
ciskspawn:geera
```

Display name:

```text
Geera - Fisherwoman
```

## Geera Prototype Fishing Quest

Right-clicking Geera starts a simple chat-based Starcatcher fishing quest chain.

Current requested fish:
1. `starcatcher:driftfin`
2. `starcatcher:blue_herring`
3. `starcatcher:bigeye_tuna`
4. `starcatcher:cinder_squid`
5. `starcatcher:chorus_crab`

The system checks the player's inventory and removes the fish when turned in.

This is intentionally simple for now so we can test:
- entity loading
- model/texture/animation
- Starcatcher item id compatibility
- future fishing quest direction

## Starcatcher Integration

This build does not hard-code Starcatcher Java classes. It only uses registry ids such as:

```text
starcatcher:driftfin
```

That means the mod can still load without directly depending on Starcatcher classes, but Geera's fishing quests require Starcatcher items to be present in-game.
