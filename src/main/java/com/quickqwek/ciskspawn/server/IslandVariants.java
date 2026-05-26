package com.quickqwek.ciskspawn.server;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Auto-discovers starter island variants. Looks for any structure named
 *   data/ciskspawn/structure/starter_island_NN.nbt
 * where NN is a zero-padded 2-digit number from 01 to MAX_LOOKUP (default 50).
 *
 * Add more variants by dropping additional starter_island_03.nbt, _04.nbt,
 * etc. files into src/main/resources/data/ciskspawn/structure/ and rebuilding.
 * The mod picks them up on next server start.
 */
public final class IslandVariants {

    private static final int MAX_LOOKUP = 50;
    private static final String PREFIX = "starter_island_";

    private final List<ResourceLocation> variants;

    private IslandVariants(List<ResourceLocation> variants) {
        this.variants = variants;
    }

    public static IslandVariants discover(StructureTemplateManager mgr) {
        List<ResourceLocation> found = new ArrayList<>();
        for (int i = 1; i <= MAX_LOOKUP; i++) {
            String name = PREFIX + String.format("%02d", i);
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, name);
            try {
                mgr.get(id).ifPresent(t -> found.add(id));
            } catch (Throwable t) {
                // skip missing
            }
        }
        if (found.isEmpty()) {
            CiskSpawnMod.LOG.warn("[CISK] No starter_island_NN.nbt variants found in resources. " +
                    "Players will get a fallback platform instead.");
        } else {
            CiskSpawnMod.LOG.info("[CISK] Loaded {} island variant(s): {}", found.size(), found);
        }
        return new IslandVariants(found);
    }

    public Optional<ResourceLocation> pickFor(Random rng) {
        if (variants.isEmpty()) return Optional.empty();
        return Optional.of(variants.get(rng.nextInt(variants.size())));
    }
}
