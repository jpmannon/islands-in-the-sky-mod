package com.quickqwek.ciskspawn.item;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, CiskSpawnMod.MODID);

    public static final DeferredHolder<Item, Item> CREW_LOGBOOK = ITEMS.register("crew_logbook",
            () -> new CrewLogbookItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> GUILDED_COMPASS = ITEMS.register("guilded_compass",
            () -> new GuildedCompassItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, BlockItem> NPC_ANCHOR_ITEM = ITEMS.register("npc_anchor",
            () -> new BlockItem(ModBlocks.NPC_ANCHOR.get(), new Item.Properties()));

    private ModItems() {}
}
