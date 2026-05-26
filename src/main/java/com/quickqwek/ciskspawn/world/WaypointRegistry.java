package com.quickqwek.ciskspawn.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WaypointRegistry extends SavedData {
    private static final String DATA_NAME = "ciskspawn_waypoints";

    private final Map<UUID, List<NpcWaypoint>> waypointsByEntity = new LinkedHashMap<>();

    public static WaypointRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WaypointRegistry::new, WaypointRegistry::load),
                DATA_NAME
        );
    }

    public static WaypointRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        WaypointRegistry registry = new WaypointRegistry();
        ListTag entityList = tag.getList("Entities", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < entityList.size(); i++) {
            CompoundTag entityTag = entityList.getCompound(i);
            if (!entityTag.hasUUID("UUID")) {
                continue;
            }

            UUID uuid = entityTag.getUUID("UUID");
            ListTag waypointTags = entityTag.getList("Waypoints", CompoundTag.TAG_COMPOUND);
            List<NpcWaypoint> waypoints = new ArrayList<>();
            for (int j = 0; j < waypointTags.size(); j++) {
                CompoundTag waypointTag = waypointTags.getCompound(j);
                waypoints.add(new NpcWaypoint(
                        waypointTag.getInt("Index"),
                        new BlockPos(waypointTag.getInt("PosX"), waypointTag.getInt("PosY"), waypointTag.getInt("PosZ")),
                        waypointTag.getInt("StartTick"),
                        waypointTag.getInt("WaitDuration"),
                        waypointTag.getString("Label")
                ));
            }
            waypoints.sort(Comparator.comparingInt(NpcWaypoint::index));
            registry.waypointsByEntity.put(uuid, waypoints);
        }
        return registry;
    }

    public void addWaypoint(UUID entityUUID, BlockPos pos, int startTick, int waitDuration, String label) {
        List<NpcWaypoint> list = this.waypointsByEntity.computeIfAbsent(entityUUID, key -> new ArrayList<>());
        int index = list.size();
        list.add(new NpcWaypoint(index, pos, normalizeDayTick(startTick), Math.max(0, waitDuration), label == null ? "" : label));
        this.setDirty();
    }

    public void clearWaypoints(UUID entityUUID) {
        this.waypointsByEntity.remove(entityUUID);
        this.setDirty();
    }

    public void removeLastWaypoint(UUID entityUUID) {
        List<NpcWaypoint> list = this.waypointsByEntity.get(entityUUID);
        if (list != null && !list.isEmpty()) {
            list.remove(list.size() - 1);
            this.setDirty();
        }
    }

    public List<NpcWaypoint> getWaypoints(UUID entityUUID) {
        List<NpcWaypoint> list = this.waypointsByEntity.get(entityUUID);
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    public Set<UUID> getAllEntityUUIDs() {
        return Collections.unmodifiableSet(this.waypointsByEntity.keySet());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entityList = new ListTag();
        for (Map.Entry<UUID, List<NpcWaypoint>> entry : this.waypointsByEntity.entrySet()) {
            CompoundTag entityTag = new CompoundTag();
            entityTag.putUUID("UUID", entry.getKey());

            ListTag waypointTags = new ListTag();
            for (NpcWaypoint waypoint : entry.getValue()) {
                CompoundTag waypointTag = new CompoundTag();
                waypointTag.putInt("Index", waypoint.index());
                waypointTag.putInt("PosX", waypoint.pos().getX());
                waypointTag.putInt("PosY", waypoint.pos().getY());
                waypointTag.putInt("PosZ", waypoint.pos().getZ());
                waypointTag.putInt("StartTick", waypoint.startTick());
                waypointTag.putInt("WaitDuration", waypoint.waitDuration());
                waypointTag.putString("Label", waypoint.label());
                waypointTags.add(waypointTag);
            }

            entityTag.put("Waypoints", waypointTags);
            entityList.add(entityTag);
        }
        tag.put("Entities", entityList);
        return tag;
    }

    private static int normalizeDayTick(int tick) {
        return Math.floorMod(tick, 24000);
    }
}
