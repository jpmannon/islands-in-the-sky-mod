package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.entity.StorykeeperEntity;
import com.quickqwek.ciskspawn.network.MortimerActionPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class MortimerKeybinds {
    public static final KeyMapping TRAVEL_KEY = new KeyMapping(
            "key.ciskspawn.mortimer_travel",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.ciskspawn"
    );

    public static final KeyMapping SCAN_KEY = new KeyMapping(
            "key.ciskspawn.mortimer_scan_seats",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.ciskspawn"
    );

    private MortimerKeybinds() {}

    @EventBusSubscriber(modid = CiskSpawnMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(TRAVEL_KEY);
            event.register(SCAN_KEY);
        }
    }

    @EventBusSubscriber(modid = CiskSpawnMod.MODID, value = Dist.CLIENT)
    public static final class GameBus {
        @SubscribeEvent
        public static void onKey(InputEvent.Key event) {
            Minecraft mc = Minecraft.getInstance();
            while (TRAVEL_KEY.consumeClick()) {
                sendIfLookingAtMortimer(mc, "travel");
            }
            while (SCAN_KEY.consumeClick()) {
                sendIfLookingAtMortimer(mc, "scan_seats");
            }
        }

        private static void sendIfLookingAtMortimer(Minecraft mc, String action) {
            if (mc.player == null || mc.level == null) return;

            // Prefer the Mortimer under the crosshair, but fall back to the nearest Mortimer.
            // This is important for seating: the player must be able to look at the chair/seat
            // while pressing G, instead of always needing to look directly at Mortimer.
            StorykeeperEntity target = null;
            if (mc.hitResult instanceof EntityHitResult hit) {
                Entity entity = hit.getEntity();
                if (entity instanceof StorykeeperEntity storykeeper) {
                    target = storykeeper;
                }
            }

            if (target == null) {
                double bestDist = Double.MAX_VALUE;
                for (StorykeeperEntity storykeeper : mc.level.getEntitiesOfClass(StorykeeperEntity.class, mc.player.getBoundingBox().inflate(32.0D))) {
                    double dist = storykeeper.distanceToSqr(mc.player);
                    if (dist < bestDist) {
                        bestDist = dist;
                        target = storykeeper;
                    }
                }
            }

            if (target != null && target.distanceTo(mc.player) < 32.0F) {
                PacketDistributor.sendToServer(new MortimerActionPayload(target.getId(), action));
            }
        }
    }
}
