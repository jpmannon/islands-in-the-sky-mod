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

    public static final DeferredHolder<EntityType<?>, EntityType<TarnEntity>> TARN =
            ENTITY_TYPES.register("tarn",
                    () -> EntityType.Builder.<TarnEntity>of(TarnEntity::new, MobCategory.MISC)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("tarn"));

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
        event.put(TARN.get(), TarnEntity.createAttributes().build());
    }

    private ModEntities() {}
}
