# CISK Starter Spawn — NeoForge 1.21.1 Mod

Server-side starter spawn + animated NPC + intro cutscene for the
*Create: Islands in the Sky* modpack.

**Features**

- **Per-player unique spawn**: on a player's first join, picks a
  deterministic location ~2000 blocks from origin (seeded by their UUID
  — same player always gets the same coords).
- **N starter island variants**: drop `starter_island_NN.nbt` files
  into resources, mod auto-discovers and picks one per player at spawn.
- **Starter kit**: iron tools, leather armor, food, building blocks,
  Create essentials, a compass pointing to 0,0.
- **Storykeeper NPC**: GeckoLib-animated villager-replacement that
  wanders and greets players. Drop your model/animation/texture in.
- **Intro cutscene**: full-screen PNG-frame overlay (~10 sec @ 10 fps =
  100 frames). Triggered automatically on first spawn.

---

## Build

Requires JDK 21.

```
./gradlew build
```

The mod jar lands at `build/libs/ciskspawn-1.0.0.jar`. Drop it into
both your server's `mods/` folder and your client's CurseForge
instance `mods/` folder.

First build downloads NeoForge, Parchment mappings, and GeckoLib —
takes a few minutes. Subsequent builds are seconds.

---

## What you need to author before it's useful

| Asset | Path | Notes |
|---|---|---|
| Storykeeper geometry | `src/main/resources/assets/ciskspawn/geo/storykeeper.geo.json` | BlockBench + Geckolib plugin |
| Storykeeper animations | `src/main/resources/assets/ciskspawn/animations/storykeeper.animation.json` | Must include `animation.storykeeper.idle` and `animation.storykeeper.walk` |
| Storykeeper texture | `src/main/resources/assets/ciskspawn/textures/entity/storykeeper.png` | Matches your model UVs |
| Starter island variants | `src/main/resources/data/ciskspawn/structure/starter_island_NN.nbt` | Vanilla structure-block saves, naming `_01`..`_50` |
| Cutscene frames | `src/main/resources/assets/ciskspawn/textures/cutscene/intro/frame_NNN.png` | 3-digit zero-padded, 16:9 aspect |
| Cutscene manifest | `src/main/resources/assets/ciskspawn/textures/cutscene/intro.json` | `{"fps": 10, "length": 100}` |

Each asset folder has a README.txt with detailed instructions for that
specific asset.

The mod boots fine without these — Storykeepers will be invisible
(GeckoLib renders nothing if model is missing), structures fall back
to a 7×7 dirt platform, and the cutscene silently no-ops if its
manifest isn't present. So you can build, install, test the spawn
flow, then incrementally drop in real assets.

---

## Test

In your dev environment:

```
./gradlew runServer    # launches a test server with mod loaded
./gradlew runClient    # launches a client connected to dev env
```

To verify spawn behavior:

```
/scoreboard players reset @s ciskspawnInitialized   # legacy syntax — see note
```

The mod uses player NBT, not a scoreboard. To re-roll your spawn,
either delete your `<world>/playerdata/<uuid>.dat` (offline server only)
or use NBT-edit tools. For development, the easiest approach is to
delete the world between tests.

---

## Customize

- **Spawn distance**: `SpawnHandler.MIN_DIST` / `MAX_DIST` / `LATERAL_SPREAD`
- **Starter kit**: edit `StarterKit.give()` — add/remove `add()` and
  `addByModId()` lines, rebuild
- **Number of island variants checked**: `IslandVariants.MAX_LOOKUP`
  (default 50)
- **Storykeeper stats**: `StorykeeperEntity.createAttributes()`
- **NPC interaction**: `StorykeeperEntity.mobInteract()` — replace
  placeholder greeting with quest/item/dialog code
- **Cutscene name on first join**: last line of `SpawnHandler.assignSpawn()`
  — change `"intro"` to any cutscene you've authored

---

## Architecture

```
com.quickqwek.ciskspawn
├── CiskSpawnMod              # entrypoint, wires registries + event subs
├── server/
│   ├── SpawnHandler          # PlayerLoggedInEvent listener
│   ├── IslandVariants        # auto-discovery of starter_island_*.nbt
│   └── StarterKit            # gives items
├── entity/
│   ├── ModEntities           # DeferredRegister
│   └── StorykeeperEntity     # GeckoLib mob, AI, interaction
├── client/                   # client-side classes (loaded only on Dist.CLIENT)
│   ├── ClientEvents          # registers entity renderer
│   ├── StorykeeperModel      # GeckoLib model resource paths
│   ├── StorykeeperRenderer   # GeckoLib renderer
│   ├── CutsceneClient        # reads cutscene manifest, opens overlay
│   └── CutsceneScreen        # full-screen PNG-frame blitting
└── network/
    ├── CutscenePayload       # server -> client packet
    └── ModPayloads           # registers payload handler
```

The server-only paths (`server/`) never reference client code.
Client-only paths (`client/`) are gated by `EventBusSubscriber` with
`value = Dist.CLIENT` and the `FMLEnvironment.dist` check in the
payload handler, so a dedicated server doesn't try to load client
classes.

---

## Dependencies

- **NeoForge** 21.1.228 (required, both sides)
- **Minecraft** 1.21.1 (exact)
- **GeckoLib** 4.7.1+ (required, both sides) — handles the NPC animations

Add the same GeckoLib version as a manifest entry in your CurseForge
modpack so everyone connecting has it client-side.


## 1.0.30
- Added uploaded Azerion/Rook placeholder geometry for testing.


## 1.0.32
- Swapped in updated Scoria and Azerion geometry files for testing.
