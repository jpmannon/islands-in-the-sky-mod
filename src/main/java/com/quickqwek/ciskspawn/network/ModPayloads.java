package com.quickqwek.ciskspawn.network;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.client.CutsceneClient;
import com.quickqwek.ciskspawn.client.MortimerClient;
import com.quickqwek.ciskspawn.entity.StorykeeperEntity;
import com.quickqwek.ciskspawn.entity.GeeraEntity;
import com.quickqwek.ciskspawn.entity.JoelleEntity;
import com.quickqwek.ciskspawn.entity.ScoriaEntity;
import com.quickqwek.ciskspawn.entity.AzerionEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModPayloads {
    private ModPayloads() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(ModPayloads::onRegister);
    }

    private static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                CutscenePayload.TYPE,
                CutscenePayload.CODEC,
                (payload, context) -> {
                    // Only client should ever receive this. The handler dispatches to
                    // the client cutscene system.
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        context.enqueueWork(() -> CutsceneClient.start(payload.name()));
                    }
                }
        );

        registrar.playToClient(
                MortimerDialoguePayload.TYPE,
                MortimerDialoguePayload.CODEC,
                (payload, context) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        context.enqueueWork(() -> MortimerClient.openDialogue(payload));
                    }
                }
        );

        registrar.playToServer(
                MortimerActionPayload.TYPE,
                MortimerActionPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) context.player();
                    Entity entity = player.level().getEntity(payload.entityId());
                    if (entity instanceof StorykeeperEntity storykeeper && storykeeper.distanceTo(player) < 32.0F) {
                        storykeeper.handleMortimerAction(player, payload.action());
                    } else if (entity instanceof GeeraEntity geera && geera.distanceTo(player) < 32.0F) {
                        geera.handleGeeraAction(player, payload.action());
                    } else if (entity instanceof ScoriaEntity scoria && scoria.distanceTo(player) < 32.0F) {
                        scoria.handleScoriaAction(player, payload.action());
                    } else if (entity instanceof AzerionEntity azerion && azerion.distanceTo(player) < 32.0F) {
                        azerion.handleAzerionAction(player, payload.action());
                    } else if (entity instanceof JoelleEntity joelle && joelle.distanceTo(player) < 32.0F) {
                        joelle.handleJoelleAction(player, payload.action());
                    }
                })
        );
        CiskSpawnMod.LOG.info("[CISK] Payloads registered.");
    }
}
