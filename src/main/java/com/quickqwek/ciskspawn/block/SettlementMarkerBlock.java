package com.quickqwek.ciskspawn.block;

import com.mojang.serialization.MapCodec;
import com.quickqwek.ciskspawn.world.SettlementRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SettlementMarkerBlock extends BaseEntityBlock {
    public static final MapCodec<SettlementMarkerBlock> CODEC = simpleCodec(SettlementMarkerBlock::new);

    public SettlementMarkerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SettlementMarkerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            if (level.getBlockEntity(pos) instanceof SettlementMarkerBlockEntity marker) {
                String settlementId = marker.getSettlementId();
                if (settlementId.isBlank()) {
                    player.sendSystemMessage(Component.literal("[CISK Settlement] Unregistered"));
                } else {
                    SettlementRegistry.get(serverLevel).get(settlementId)
                            .ifPresentOrElse(
                                    settlement -> player.sendSystemMessage(Component.literal("[CISK Settlement] "
                                            + settlement.displayName()
                                            + " (id: " + settlement.id() + ") center "
                                            + formatPos(settlement.center())
                                            + " radius " + settlement.radius())),
                                    () -> player.sendSystemMessage(Component.literal("[CISK Settlement] Unregistered id: " + settlementId))
                            );
                }
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 8;
    }

    private static String formatPos(BlockPos pos) {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
    }
}
