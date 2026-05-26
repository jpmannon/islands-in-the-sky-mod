package com.quickqwek.ciskspawn.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class SettlementRegistry extends SavedData {
    private static final String DATA_NAME = "ciskspawn_settlements";

    private final Map<String, Settlement> settlements = new LinkedHashMap<>();

    public record Settlement(String id, String displayName, BlockPos center, int radius) {}

    public static SettlementRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SettlementRegistry::new, SettlementRegistry::load),
                DATA_NAME
        );
    }

    public static SettlementRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        SettlementRegistry registry = new SettlementRegistry();
        ListTag list = tag.getList("Settlements", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag settlementTag = list.getCompound(i);
            String id = settlementTag.getString("Id").toLowerCase(Locale.ROOT);
            String displayName = settlementTag.getString("DisplayName");
            BlockPos center = new BlockPos(
                    settlementTag.getInt("CenterX"),
                    settlementTag.getInt("CenterY"),
                    settlementTag.getInt("CenterZ")
            );
            int radius = settlementTag.getInt("Radius");
            registry.settlements.put(id, new Settlement(id, displayName, center, radius));
        }
        return registry;
    }

    public void register(String id, String displayName, BlockPos center, int radius) {
        String normalizedId = normalizeId(id);
        this.settlements.put(normalizedId, new Settlement(normalizedId, displayName, center, radius));
        this.setDirty();
    }

    public void remove(String id) {
        this.settlements.remove(normalizeId(id));
        this.setDirty();
    }

    public Optional<Settlement> get(String id) {
        return Optional.ofNullable(this.settlements.get(normalizeId(id)));
    }

    public Collection<Settlement> getAll() {
        return Collections.unmodifiableCollection(this.settlements.values());
    }

    public Optional<Settlement> getSettlementAt(BlockPos pos) {
        for (Settlement settlement : this.settlements.values()) {
            double distance = Math.sqrt(settlement.center().distSqr(pos));
            if (distance <= settlement.radius()) {
                return Optional.of(settlement);
            }
        }
        return Optional.empty();
    }

    public Optional<Settlement> getNearestSettlement(BlockPos pos) {
        return this.settlements.values().stream()
                .min(Comparator.comparingDouble(settlement -> settlement.center().distSqr(pos)));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Settlement settlement : this.settlements.values()) {
            CompoundTag settlementTag = new CompoundTag();
            settlementTag.putString("Id", settlement.id());
            settlementTag.putString("DisplayName", settlement.displayName());
            settlementTag.putInt("CenterX", settlement.center().getX());
            settlementTag.putInt("CenterY", settlement.center().getY());
            settlementTag.putInt("CenterZ", settlement.center().getZ());
            settlementTag.putInt("Radius", settlement.radius());
            list.add(settlementTag);
        }
        tag.put("Settlements", list);
        return tag;
    }

    private static String normalizeId(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
