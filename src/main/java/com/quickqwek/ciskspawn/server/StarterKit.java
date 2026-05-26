package com.quickqwek.ciskspawn.server;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class StarterKit {

    private StarterKit() {}

    public static void give(ServerPlayer player) {
        // Tools
        add(player, Items.IRON_PICKAXE, 1);
        add(player, Items.IRON_AXE, 1);
        add(player, Items.IRON_SHOVEL, 1);
        add(player, Items.IRON_SWORD, 1);

        // Armor (leather — they should upgrade)
        add(player, Items.LEATHER_HELMET, 1);
        add(player, Items.LEATHER_CHESTPLATE, 1);
        add(player, Items.LEATHER_LEGGINGS, 1);
        add(player, Items.LEATHER_BOOTS, 1);

        // Survival basics
        add(player, Items.OAK_LOG, 64);
        add(player, Items.OAK_PLANKS, 64);
        add(player, Items.COBBLESTONE, 64);
        add(player, Items.DIRT, 32);
        add(player, Items.WHITE_WOOL, 32);
        add(player, Items.TORCH, 32);
        add(player, Items.WATER_BUCKET, 1);
        add(player, Items.BUCKET, 2);
        add(player, Items.BREAD, 32);
        add(player, Items.COOKED_BEEF, 16);

        // Ranged + defense
        add(player, Items.BOW, 1);
        add(player, Items.ARROW, 32);
        add(player, Items.SHIELD, 1);

        // Power / progression
        add(player, Items.COAL, 64);
        add(player, Items.IRON_INGOT, 32);
        add(player, Items.COPPER_INGOT, 16);

        // Compass points to (0,0) world spawn — the goal
        add(player, Items.COMPASS, 1);

        // Create essentials (skipped silently if mod missing)
        addByModId(player, "create:wrench", 1);
        addByModId(player, "create:goggles", 1);
        addByModId(player, "create:andesite_alloy", 32);
        addByModId(player, "create:cogwheel", 16);
        addByModId(player, "create:large_cogwheel", 8);
        addByModId(player, "create:shaft", 32);
        addByModId(player, "create:fluid_pipe", 16);
        addByModId(player, "create:hand_crank", 2);

        // Aeronautics — uncomment after confirming IDs on your build
        // addByModId(player, "aeronautics:helm", 1);
        // addByModId(player, "aeronautics:engine_block", 4);
        // addByModId(player, "aeronautics:propeller", 4);
    }

    private static void add(ServerPlayer p, Item item, int count) {
        p.getInventory().add(new ItemStack(item, count));
    }

    private static void addByModId(ServerPlayer p, String id, int count) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) { CiskSpawnMod.LOG.warn("[CISK] bad item id {}", id); return; }
        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item == null || item == Items.AIR) {
            CiskSpawnMod.LOG.info("[CISK] starter kit item not present (mod not loaded?): {}", id);
            return;
        }
        p.getInventory().add(new ItemStack(item, count));
    }
}
