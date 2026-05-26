# ChatGPT Build Prompt — NPC Spawn Anchor System

Run this FIRST. The other two system prompts depend on knowing the anchor block exists.

---

## PROMPT

I am building a NeoForge 1.21.1 Minecraft mod (`ciskspawn`, package `com.quickqwek.ciskspawn`). I hand-build all towns and structures in-world and need a way to designate exactly where each NPC spawns, lives, and returns to. I need an NPC Spawn Anchor system — a placeable block that permanently tethers a specific NPC type to a location. Generate all files listed at the bottom.

---

### DESIGN OVERVIEW

The **NPC Anchor** is a special block (`ciskspawn:npc_anchor`) that a world-builder places in the world to mark where an NPC should exist. It stores which NPC type it controls. On server load and periodically during play, it checks whether its assigned NPC is alive and within range — if not, it respawns them at the anchor position. The NPC has a custom AI goal that makes them return to their anchor position if they wander too far.

---

### BLOCK: NpcAnchorBlock

**Registry name:** `ciskspawn:npc_anchor`  
**Location:** `com.quickqwek.ciskspawn.block.NpcAnchorBlock`

Properties:
- Extends `BaseEntityBlock`
- Solid, full-block shape
- Emits light level 4 (visible in dark areas so builders can find them)
- Does NOT drop as item when broken in survival (builder-only block) — or drops itself if broken in creative

**Right-click behavior (no sneaking):**  
Cycles the stored NPC type forward through the list. Shows the current selection to the player in chat: `"[CISK Anchor] → Mortimer"`. Returns `InteractionResult.sidedSuccess`.

**Sneak + right-click:**  
Cycles backward through the list.

**NPC Type List (cycle order):**
```
mortimer, geera, scoria, azerion_rook, joelle, ramone, velho, tarn, cade, ziiko, agatha
```
These correspond exactly to the entity registry names in `ModEntities`.

**Display:** When a player looks at the block, show the current assigned NPC type and status in the block's hover name or as a small overlay (use `player.sendSystemMessage` on interaction, not a tooltip).

---

### BLOCK ENTITY: NpcAnchorBlockEntity

**Registry name:** `ciskspawn:npc_anchor_be`  
**Location:** `com.quickqwek.ciskspawn.block.NpcAnchorBlockEntity`

Stored NBT fields:
- `"NpcType"` (String) — one of the 11 registry names above. Default: `"mortimer"`.
- `"BoundEntityUUID"` (UUID, optional) — UUID of the currently spawned entity. Null if none spawned yet.
- `"CheckCooldown"` (int) — ticks until next respawn check. Default 100.

**Implements `ServerLevelTickingBlockEntity`** (NeoForge 1.21.1 ticking block entity interface for server-side ticking).

**Tick logic (server side only, runs every tick, uses CheckCooldown to gate):**

```
CheckCooldown--
If CheckCooldown > 0: return

Reset CheckCooldown to 200 (10 seconds)

If BoundEntityUUID is not null:
    Look up entity in level by UUID
    If entity exists AND is alive AND within 64 blocks of anchor: return (all good)
    If entity exists but is dead or gone: clear BoundEntityUUID

// Need to (re)spawn
EntityType<?> type = resolve NpcType string to ModEntities entry
If type == null: return (unknown type)

// Check if any entity of this type already exists nearby (within 8 blocks) — don't double-spawn
List<Entity> nearby = level.getEntitiesOfClass(type.getBaseClass(), AABB(blockPos).inflate(8))
If any entity in nearby is the correct type: 
    Store its UUID as BoundEntityUUID, return

// Spawn new entity
Entity entity = type.create(level)
Set entity position to blockPos.getCenter() (center of block, +0.5 on each axis)
entity.setPersistenceRequired()
level.addFreshEntity(entity)
BoundEntityUUID = entity.getUUID()
setChanged()
```

**save/load:** `addAdditionalSaveData` / `readAdditionalSaveData` for NpcType, BoundEntityUUID (if present), CheckCooldown.

---

### CUSTOM AI GOAL: NpcAnchorReturnGoal

**Location:** `com.quickqwek.ciskspawn.ai.NpcAnchorReturnGoal`

A custom `Goal` subclass that makes an NPC return to its home position if it's too far away.

Constructor parameters: `PathfinderMob mob, BlockPos homePos, double speedModifier, float maxDistance`

```java
// canUse(): return mob.distanceToSqr(homePos center) > (maxDistance * maxDistance)
// canContinueToUse(): return mob.distanceToSqr(homePos center) > (2.0 * 2.0)
// start(): mob.getNavigation().moveTo(homePos.getX()+0.5, homePos.getY(), homePos.getZ()+0.5, speedModifier)
// stop(): mob.getNavigation().stop()
```

Set flags: `EnumSet.of(Goal.Flag.MOVE)`

---

### WIRING: NPC ENTITIES

Each NPC entity (`StorykeeperEntity`, `GeeraEntity`, etc.) needs two additions:

1. **A `homePos` field:** `@Nullable private BlockPos homePos = null;`

2. **A setter:** `public void setHomePos(BlockPos pos) { this.homePos = pos; }`

3. **In `registerGoals()`**, add at priority 1 (higher than wander, lower than float):
```java
if (homePos != null) {
    this.goalSelector.addGoal(1, new NpcAnchorReturnGoal(this, homePos, 0.6D, 12.0F));
}
```

The `NpcAnchorBlockEntity` sets `homePos` after spawning the entity:
```java
if (entity instanceof StorykeeperEntity sk) sk.setHomePos(blockPos);
else if (entity instanceof GeeraEntity g) g.setHomePos(blockPos);
// ... etc for all 11 NPC types
```

**Also save/load homePos in each entity's NBT** so it persists across chunk reloads:
- `addAdditionalSaveData`: `if (homePos != null) tag.put("HomePos", NbtUtils.writeBlockPos(homePos));`
- `readAdditionalSaveData`: `if (tag.contains("HomePos")) homePos = NbtUtils.readBlockPos(tag.getCompound("HomePos"));`

---

### REGISTRATION

**ModBlocks.java** (create this file if it doesn't exist):
```java
public static final DeferredRegister<Block> BLOCKS = 
    DeferredRegister.create(BuiltInRegistries.BLOCK, CiskSpawnMod.MODID);

public static final DeferredHolder<Block, NpcAnchorBlock> NPC_ANCHOR =
    BLOCKS.register("npc_anchor", () -> new NpcAnchorBlock(
        BlockBehaviour.Properties.of()
            .strength(3.5F)
            .lightLevel(state -> 4)
            .sound(SoundType.STONE)
    ));
```

**ModBlockEntities.java** (create this file):
```java
public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
    DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CiskSpawnMod.MODID);

public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NpcAnchorBlockEntity>> NPC_ANCHOR_BE =
    BLOCK_ENTITY_TYPES.register("npc_anchor_be",
        () -> BlockEntityType.Builder.of(NpcAnchorBlockEntity::new, ModBlocks.NPC_ANCHOR.get()).build(null));
```

Register both `DeferredRegister` instances in `CiskSpawnMod.java`'s constructor using `register(modBus)`.

Also register a block item for the anchor so it can be given via `/give`:
```java
// In ModItems or inline in ModBlocks:
public static final DeferredHolder<Item, BlockItem> NPC_ANCHOR_ITEM =
    ITEMS.register("npc_anchor", () -> new BlockItem(ModBlocks.NPC_ANCHOR.get(), new Item.Properties()));
```

---

### WHAT TO GENERATE

Generate these files in full:

1. `com.quickqwek.ciskspawn.block.NpcAnchorBlock` — full block class
2. `com.quickqwek.ciskspawn.block.NpcAnchorBlockEntity` — full block entity with ticking and respawn logic
3. `com.quickqwek.ciskspawn.ai.NpcAnchorReturnGoal` — full Goal subclass
4. `com.quickqwek.ciskspawn.block.ModBlocks` — registration class
5. `com.quickqwek.ciskspawn.block.ModBlockEntities` — registration class

Additionally provide:
- The exact lines to add to `CiskSpawnMod.java` constructor to register both DeferredRegisters
- The exact `homePos` field + setter + `registerGoals` addition + NBT save/load lines to add to each NPC entity (provide once as a template — I will apply it to all 11 entities myself)

Do NOT modify any existing entity files. Just provide the template additions separately.

Do NOT generate models, textures, or client renderers.
