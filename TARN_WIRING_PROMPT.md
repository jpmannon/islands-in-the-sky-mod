# ChatGPT Prompt — Wire Tarn into the Mod (3 surgical edits)

Paste this prompt to ChatGPT. Do NOT paste TarnEntity.java with it — ChatGPT only needs to see the three files below.

---

## PROMPT

I have a NeoForge 1.21.1 Minecraft mod (`ciskspawn`). I just added `TarnEntity.java`. I need three surgical edits to wire her in. Do not rewrite any file from scratch — make only the specific additions described below.

---

## EDIT 1 — PlayerStatsTracker.java

**File:** `src/main/java/com/quickqwek/ciskspawn/server/PlayerStatsTracker.java`

**Current file:**

```java
package com.quickqwek.ciskspawn.server;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class PlayerStatsTracker {
    private PlayerStatsTracker() {}

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            CompoundTag data = player.getPersistentData();
            int deaths = data.getInt("ciskspawn_deaths");
            data.putInt("ciskspawn_deaths", deaths + 1);
        }
    }

    public static int getDeaths(Player player) {
        return player.getPersistentData().getInt("ciskspawn_deaths");
    }

    public static int getIslandsDiscovered(Player player) {
        return player.getPersistentData().getInt("ciskspawn_islands");
    }

    public static void incrementIslands(Player player) {
        CompoundTag data = player.getPersistentData();
        data.putInt("ciskspawn_islands", data.getInt("ciskspawn_islands") + 1);
    }

    public static String getShipName(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains("ciskspawn_ship_name")) return data.getString("ciskspawn_ship_name");
        return null;
    }

    public static void setShipName(Player player, String name) {
        player.getPersistentData().putString("ciskspawn_ship_name", name);
    }

    public static long getDaysSinceLastVisit(Player player, String npcId) {
        CompoundTag data = player.getPersistentData();
        String key = "ciskspawn_last_visit_" + npcId;
        long currentDay = player.level().getDayTime() / 24000L;
        if (!data.contains(key)) return 0;
        return currentDay - data.getLong(key);
    }

    public static void recordVisit(Player player, String npcId) {
        CompoundTag data = player.getPersistentData();
        data.putLong("ciskspawn_last_visit_" + npcId, player.level().getDayTime() / 24000L);
    }
}
```

**What to add:**

Add one new `@SubscribeEvent` method — `onPlayerClone` — that implements the Soul Anchor keep-inventory mechanic. When a player dies, if their persistent data has `"ciskspawn_has_soul_anchor" = true`, their full inventory should be copied from the pre-death player to the respawned player, and the persistent data should be merged so the flag survives.

Use `PlayerEvent.Clone` (NeoForge). Check `event.isWasDeath()` — only trigger on death respawn, not dimension change. Copy inventory via `original.getInventory().getContainerSize()` loop. Merge persistent data with `newPlayer.getPersistentData().merge(original.getPersistentData())`.

Add only the necessary import(s) and the new method. Do not modify anything else in this file.

---

## EDIT 2 — ModEntities.java

**File:** `src/main/java/com/quickqwek/ciskspawn/entity/ModEntities.java`

**Current file:**

```java
package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, CiskSpawnMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<StorykeeperEntity>> STORYKEEPER =
            ENTITY_TYPES.register("storykeeper",
                    () -> EntityType.Builder.<StorykeeperEntity>of(StorykeeperEntity::new, MobCategory.CREATURE)
                            .sized(0.9F, 2.35F)
                            .clientTrackingRange(10)
                            .build("storykeeper"));

    public static final DeferredHolder<EntityType<?>, EntityType<GeeraEntity>> GEERA =
            ENTITY_TYPES.register("geera",
                    () -> EntityType.Builder.<GeeraEntity>of(GeeraEntity::new, MobCategory.CREATURE)
                            .sized(0.65F, 1.95F)
                            .clientTrackingRange(10)
                            .build("geera"));

    public static final DeferredHolder<EntityType<?>, EntityType<JoelleEntity>> JOELLE =
            ENTITY_TYPES.register("joelle",
                    () -> EntityType.Builder.<JoelleEntity>of(JoelleEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("joelle"));

    public static final DeferredHolder<EntityType<?>, EntityType<RamoneEntity>> RAMONE =
            ENTITY_TYPES.register("ramone",
                    () -> EntityType.Builder.<RamoneEntity>of(RamoneEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("ramone"));

    public static final DeferredHolder<EntityType<?>, EntityType<VelhoEntity>> VELHO =
            ENTITY_TYPES.register("velho",
                    () -> EntityType.Builder.<VelhoEntity>of(VelhoEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("velho"));

    public static final DeferredHolder<EntityType<?>, EntityType<ScoriaEntity>> SCORIA =
            ENTITY_TYPES.register("scoria",
                    () -> EntityType.Builder.<ScoriaEntity>of(ScoriaEntity::new, MobCategory.CREATURE)
                            .sized(0.62F, 1.85F)
                            .clientTrackingRange(10)
                            .build("scoria"));

    public static final DeferredHolder<EntityType<?>, EntityType<AzerionEntity>> AZERION_ROOK =
            ENTITY_TYPES.register("azerion_rook",
                    () -> EntityType.Builder.<AzerionEntity>of(AzerionEntity::new, MobCategory.CREATURE)
                            .sized(1.05F, 2.45F)
                            .clientTrackingRange(10)
                            .build("azerion_rook"));

    public static void registerAttributesEvent(IEventBus modBus) {
        modBus.addListener(ModEntities::onAttributeCreation);
    }

    private static void onAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(STORYKEEPER.get(), StorykeeperEntity.createAttributes().build());
        event.put(GEERA.get(), GeeraEntity.createAttributes().build());
        event.put(JOELLE.get(), JoelleEntity.createAttributes().build());
        event.put(RAMONE.get(), RamoneEntity.createAttributes().build());
        event.put(VELHO.get(), VelhoEntity.createAttributes().build());
        event.put(SCORIA.get(), ScoriaEntity.createAttributes().build());
        event.put(AZERION_ROOK.get(), AzerionEntity.createAttributes().build());
    }

    private ModEntities() {}
}
```

**What to add:**

Add `TarnEntity` registration. Insert after the `AZERION_ROOK` field and before `registerAttributesEvent`:

```java
public static final DeferredHolder<EntityType<?>, EntityType<TarnEntity>> TARN =
        ENTITY_TYPES.register("tarn",
                () -> EntityType.Builder.<TarnEntity>of(TarnEntity::new, MobCategory.MISC)
                        .sized(0.6F, 1.8F)
                        .clientTrackingRange(10)
                        .build("tarn"));
```

Also add to `onAttributeCreation`:
```java
event.put(TARN.get(), TarnEntity.createAttributes().build());
```

No other changes.

---

## EDIT 3 — ModPayloads.java

**File:** `src/main/java/com/quickqwek/ciskspawn/network/ModPayloads.java`

**Current file:**

```java
package com.quickqwek.ciskspawn.network;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.client.CutsceneClient;
import com.quickqwek.ciskspawn.client.MortimerClient;
import com.quickqwek.ciskspawn.item.CrewLogbookItem;
import com.quickqwek.ciskspawn.entity.StorykeeperEntity;
import com.quickqwek.ciskspawn.entity.GeeraEntity;
import com.quickqwek.ciskspawn.entity.JoelleEntity;
import com.quickqwek.ciskspawn.entity.RamoneEntity;
import com.quickqwek.ciskspawn.entity.VelhoEntity;
import com.quickqwek.ciskspawn.entity.ScoriaEntity;
import com.quickqwek.ciskspawn.entity.AzerionEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModPayloads {
    private ModPayloads() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(ModPayloads::onRegister);
    }

    private static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient( /* CutscenePayload handler — do not touch */ );

        registrar.playToClient( /* MortimerDialoguePayload handler — do not touch */ );

        registrar.playToServer(
                MortimerActionPayload.TYPE,
                MortimerActionPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    if (payload.entityId() == -1) {
                        CrewLogbookItem.handleAction(player, payload.action());
                        return;
                    }
                    Entity entity = player.level().getEntity(payload.entityId());
                    if (entity instanceof StorykeeperEntity storykeeper && storykeeper.distanceTo(player) < 32.0F) {
                        storykeeper.handleMortimerAction(player, payload.action());
                    } else if (entity instanceof GeeraEntity geera && geera.distanceTo(player) < 32.0F) {
                        geera.handleGeeraAction(player, payload.action());
                    } else if (entity instanceof ScoriaEntity scoria && scoria.distanceTo(player) < 32.0F) {
                        scoria.handleScoriaAction(player, payload.action());
                    } else if (entity instanceof AzerionEntity azerion && azerion.distanceTo(player) < 32.0F) {
                        azerion.handleAzerionAction(player, payload.action());
                    } else if (entity instanceof JoelleEntity joelle && joelle.distanceTo(player) < 32.0F) {
                        joelle.handleJoelleAction(player, payload.action());
                    } else if (entity instanceof RamoneEntity ramone && ramone.distanceTo(player) < 32.0F) {
                        ramone.handleRamoneAction(player, payload.action());
                    } else if (entity instanceof VelhoEntity velho && velho.distanceTo(player) < 32.0F) {
                        velho.handleVelhoAction(player, payload.action());
                    }
                })
        );
        CiskSpawnMod.LOG.info("[CISK] Payloads registered.");
    }
}
```

**What to add:**

Add `TarnEntity` import alongside the other entity imports:
```java
import com.quickqwek.ciskspawn.entity.TarnEntity;
```

Add this `else if` branch at the end of the entity routing chain (after the `VelhoEntity` branch, before the closing `}` of `enqueueWork`):
```java
} else if (entity instanceof TarnEntity tarn && tarn.distanceTo(player) < 32.0F) {
    tarn.handleTarnAction(player, payload.action());
}
```

No other changes.

---

## OUTPUT FORMAT

Return three code blocks in order, one per file, each containing the complete updated file. Label them clearly:
- `// FILE: PlayerStatsTracker.java`
- `// FILE: ModEntities.java`
- `// FILE: ModPayloads.java`
