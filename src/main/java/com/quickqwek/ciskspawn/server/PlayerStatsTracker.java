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
        // Placeholder - Island registry (Task 13) will fill this in.
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
