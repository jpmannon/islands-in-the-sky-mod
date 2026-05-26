package com.quickqwek.ciskspawn;

import com.quickqwek.ciskspawn.block.ModBlockEntities;
import com.quickqwek.ciskspawn.block.ModBlocks;
import com.quickqwek.ciskspawn.entity.ModEntities;
import com.quickqwek.ciskspawn.item.ModItems;
import com.quickqwek.ciskspawn.item.ModCreativeTabs;
import com.quickqwek.ciskspawn.network.ModPayloads;
import com.quickqwek.ciskspawn.server.PlayerStatsTracker;
import com.quickqwek.ciskspawn.server.SpawnHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CiskSpawnMod.MODID)
public class CiskSpawnMod {
    public static final String MODID = "ciskspawn";
    public static final Logger LOG = LoggerFactory.getLogger(MODID);

    public CiskSpawnMod(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        ModEntities.registerAttributesEvent(modBus);
        ModPayloads.register(modBus);

        NeoForge.EVENT_BUS.register(new SpawnHandler());
        NeoForge.EVENT_BUS.register(PlayerStatsTracker.class);

        LOG.info("[CISK] Mod constructor complete.");
    }
}
