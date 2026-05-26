package com.quickqwek.ciskspawn.block;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, CiskSpawnMod.MODID);

    public static final DeferredHolder<Block, NpcAnchorBlock> NPC_ANCHOR =
            BLOCKS.register("npc_anchor", () -> new NpcAnchorBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5F)
                            .lightLevel(state -> 4)
                            .sound(SoundType.STONE)
            ));

    public static final DeferredHolder<Block, SettlementMarkerBlock> SETTLEMENT_MARKER =
            BLOCKS.register("settlement_marker", () -> new SettlementMarkerBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5F)
                            .lightLevel(state -> 8)
                            .sound(SoundType.AMETHYST)
            ));

    private ModBlocks() {}
}
