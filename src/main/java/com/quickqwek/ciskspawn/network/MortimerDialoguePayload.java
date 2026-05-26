package com.quickqwek.ciskspawn.network;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server -> client packet that opens Mortimer's lightweight dialogue UI. */
public record MortimerDialoguePayload(int entityId, String title, String body, String optionOne, String optionTwo, String footer) implements CustomPacketPayload {
    public static final Type<MortimerDialoguePayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "mortimer_dialogue")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MortimerDialoguePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MortimerDialoguePayload::entityId,
            ByteBufCodecs.STRING_UTF8, MortimerDialoguePayload::title,
            ByteBufCodecs.STRING_UTF8, MortimerDialoguePayload::body,
            ByteBufCodecs.STRING_UTF8, MortimerDialoguePayload::optionOne,
            ByteBufCodecs.STRING_UTF8, MortimerDialoguePayload::optionTwo,
            ByteBufCodecs.STRING_UTF8, MortimerDialoguePayload::footer,
            MortimerDialoguePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
