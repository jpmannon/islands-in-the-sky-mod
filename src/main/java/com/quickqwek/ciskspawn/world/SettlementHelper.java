package com.quickqwek.ciskspawn.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class SettlementHelper {
    private SettlementHelper() {}

    public static String getPlayerSettlement(ServerPlayer player) {
        return SettlementRegistry.get(player.serverLevel())
                .getSettlementAt(player.blockPosition())
                .map(SettlementRegistry.Settlement::displayName)
                .orElse(null);
    }

    public static boolean isPlayerInSettlement(ServerPlayer player, String settlementId) {
        return SettlementRegistry.get(player.serverLevel())
                .getSettlementAt(player.blockPosition())
                .map(settlement -> settlement.id().equalsIgnoreCase(settlementId))
                .orElse(false);
    }

    public static BlockPos getSettlementCenter(ServerLevel level, String settlementId) {
        return SettlementRegistry.get(level)
                .get(settlementId)
                .map(SettlementRegistry.Settlement::center)
                .orElse(null);
    }
}
