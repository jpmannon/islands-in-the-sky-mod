package com.quickqwek.ciskspawn.network;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client packet that triggers a named cutscene (e.g. "intro"). */
public record CutscenePayload(String name) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CutscenePayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "cutscene"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CutscenePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, CutscenePayload::name,
                    CutscenePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
