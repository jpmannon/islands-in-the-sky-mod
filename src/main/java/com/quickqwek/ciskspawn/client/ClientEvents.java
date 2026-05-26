package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.entity.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = CiskSpawnMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientEvents {
    private ClientEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(ModEntities.STORYKEEPER.get(), StorykeeperRenderer::new);
        e.registerEntityRenderer(ModEntities.GEERA.get(), GeeraRenderer::new);
        e.registerEntityRenderer(ModEntities.JOELLE.get(), JoelleRenderer::new);
        e.registerEntityRenderer(ModEntities.RAMONE.get(), RamoneRenderer::new);
        e.registerEntityRenderer(ModEntities.SCORIA.get(), ScoriaRenderer::new);
        e.registerEntityRenderer(ModEntities.AZERION_ROOK.get(), AzerionRenderer::new);
        CiskSpawnMod.LOG.info("[CISK] Client renderers registered.");
    }
}
