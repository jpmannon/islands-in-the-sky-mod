package com.quickqwek.ciskspawn.server;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.network.CutscenePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Listens for PlayerLoggedInEvent. On a player's first join:
 *   1. Builds a UUID-seeded RNG (deterministic per player).
 *   2. Picks spawn coords roughly 2000 blocks from origin.
 *   3. Picks an island variant from data/ciskspawn/structure/starter_island_NN.nbt.
 *   4. Places the structure, teleports the player, gives starter kit.
 *   5. Sends a "play cutscene" packet to the client.
 *
 * Deterministic: same UUID -> same coords + same variant, always.
 */
public class SpawnHandler {

    private static final String NBT_FLAG = "ciskspawnInitialized";
    private static final int MIN_DIST = 1800;
    private static final int MAX_DIST = 2200;
    private static final int LATERAL_SPREAD = 1500;
    private static final int FALLBACK_Y = 100;

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound("PlayerPersisted");
        if (persisted.getBoolean(NBT_FLAG)) return;

        try {
            assignSpawn(player);
        } catch (Throwable t) {
            CiskSpawnMod.LOG.error("[CISK] Failed to assign spawn for {}: {}",
                    player.getName().getString(), t.getMessage(), t);
        }

        persisted.putBoolean(NBT_FLAG, true);
        root.put("PlayerPersisted", persisted);
    }

    private void assignSpawn(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();

        // Deterministic per-player RNG from UUID
        UUID id = player.getUUID();
        Random rng = new Random(id.getMostSignificantBits() ^ id.getLeastSignificantBits());

        // Pick coords (random ring around origin)
        int[] coords = randomCoords(rng);
        BlockPos baseTarget = new BlockPos(coords[0], FALLBACK_Y, coords[1]);

        // Discover and pick island variant
        IslandVariants variants = IslandVariants.discover(overworld.getStructureManager());
        Optional<ResourceLocation> chosen = variants.pickFor(rng);

        BlockPos spawnAt;
        if (chosen.isPresent()) {
            spawnAt = placeStructure(overworld, baseTarget, chosen.get());
            CiskSpawnMod.LOG.info("[CISK] {} placed variant {} at {}",
                    player.getName().getString(), chosen.get(), spawnAt);
        } else {
            spawnAt = buildFallbackPlatform(overworld, coords[0], coords[1], FALLBACK_Y);
            CiskSpawnMod.LOG.info("[CISK] No island variants found; built fallback for {} at {}",
                    player.getName().getString(), spawnAt);
        }

        player.teleportTo(overworld,
                spawnAt.getX() + 0.5, spawnAt.getY(), spawnAt.getZ() + 0.5,
                Set.of(), 0f, 0f);

        player.setRespawnPosition(overworld.dimension(), spawnAt, 0f, true, false);

        StarterKit.give(player);
        sendWelcome(player);

        // Trigger client-side cutscene overlay
        PacketDistributor.sendToPlayer(player, new CutscenePayload("intro"));
    }

    private int[] randomCoords(Random rng) {
        int dir = rng.nextInt(4);
        int dist = MIN_DIST + rng.nextInt(MAX_DIST - MIN_DIST + 1);
        int spread = -LATERAL_SPREAD + rng.nextInt(2 * LATERAL_SPREAD + 1);
        return switch (dir) {
            case 0 -> new int[]{spread, -dist};
            case 1 -> new int[]{dist, spread};
            case 2 -> new int[]{spread, dist};
            default -> new int[]{-dist, spread};
        };
    }

    /** Places the picked structure at the target X/Z, centered, returns the
     *  position to teleport the player to (one block above structure top). */
    private BlockPos placeStructure(ServerLevel level, BlockPos target, ResourceLocation id) {
        ChunkPos cp = new ChunkPos(target);
        level.getChunkSource().updateChunkForced(cp, true);
        try {
            StructureTemplateManager mgr = level.getStructureManager();
            StructureTemplate template = mgr.getOrCreate(id);
            // Center the placement on target by offsetting by half the template size
            Vec3i size = template.getSize();
            BlockPos origin = target.offset(-size.getX() / 2, 0, -size.getZ() / 2);
            template.placeInWorld(level, origin, origin,
                    new StructurePlaceSettings().setRotation(Rotation.NONE).setMirror(Mirror.NONE),
                    level.random, 3);
            // Player lands at top-center of the placed structure
            return new BlockPos(target.getX(), target.getY() + size.getY(), target.getZ());
        } finally {
            level.getChunkSource().updateChunkForced(cp, false);
        }
    }

    /** Fallback: 7x7 grass/dirt platform when no variants are loaded. */
    private BlockPos buildFallbackPlatform(ServerLevel level, int cx, int cz, int y) {
        ChunkPos cp = new ChunkPos(cx >> 4, cz >> 4);
        level.getChunkSource().updateChunkForced(cp, true);
        try {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    level.setBlock(new BlockPos(cx + dx, y - 1, cz + dz), Blocks.DIRT.defaultBlockState(), 3);
                    level.setBlock(new BlockPos(cx + dx, y, cz + dz), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                    for (int dy = 1; dy <= 4; dy++) {
                        BlockPos p = new BlockPos(cx + dx, y + dy, cz + dz);
                        if (!level.getBlockState(p).isAir()) {
                            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        } finally {
            level.getChunkSource().updateChunkForced(cp, false);
        }
        return new BlockPos(cx, y + 1, cz);
    }

    private void sendWelcome(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("\n=== Welcome to Create: Islands in the Sky ==="));
        player.sendSystemMessage(Component.literal(
                "You've spawned roughly 2000 blocks from world center."));
        player.sendSystemMessage(Component.literal(
                "Build an airship and fly to 0,0 to find the heart of the archipelago."));
        player.sendSystemMessage(Component.literal(
                "A compass points the way. Good luck, captain.\n"));
    }
}
