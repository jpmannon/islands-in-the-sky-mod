package com.quickqwek.ciskspawn.block;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CiskSpawnMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NpcAnchorBlockEntity>> NPC_ANCHOR_BE =
            BLOCK_ENTITY_TYPES.register("npc_anchor_be",
                    () -> BlockEntityType.Builder.of(NpcAnchorBlockEntity::new, ModBlocks.NPC_ANCHOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SettlementMarkerBlockEntity>> SETTLEMENT_MARKER_BE =
            BLOCK_ENTITY_TYPES.register("settlement_marker_be",
                    () -> BlockEntityType.Builder.of(SettlementMarkerBlockEntity::new, ModBlocks.SETTLEMENT_MARKER.get()).build(null));

    private ModBlockEntities() {}
}
