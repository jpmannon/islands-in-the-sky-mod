package com.quickqwek.ciskspawn.network;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client -> server packet for Mortimer UI buttons and keybind actions. */
public record MortimerActionPayload(int entityId, String action) implements CustomPacketPayload {
    public static final Type<MortimerActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "mortimer_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MortimerActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MortimerActionPayload::entityId,
            ByteBufCodecs.STRING_UTF8, MortimerActionPayload::action,
            MortimerActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
