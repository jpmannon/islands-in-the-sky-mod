package com.quickqwek.ciskspawn.item;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class CrewLogbookItem extends Item {
    public CrewLogbookItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    0,
                    "Crew Logbook",
                    "Open the tabs below to review crew status, relationships, clues, and systems.",
                    "Crew",
                    "Relationships",
                    "Use the tabs inside the logbook. NPC dialogue stays conversational."
            ));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private String buildGuideText() {
        return "Islands in the Sky Crew Logbook\n\n"
                + "Moods: Mortimer is often restless but protective. Geera is amused, practical, and currently watching everyone make poor decisions. Scoria is nervous, proud, and hiding his real apprenticeship. Azerion is operationally loyal and emotionally encrypted.\n\n"
                + "Relationships: trust grows through quests, travel, useful repairs, fishing work, artillery drills, and repeated visits. NPC UI stays conversational; deeper relationship details live here.\n\n"
                + "Scoria clues: clean clothes were mistaken for banker clothes. The truth points toward Aero Guild apprenticeship, advanced propulsion, and levitite/thruster work. Geera knows more than she admits.\n\n"
                + "Crew notes: Mortimer teaches skyfaring and maintenance. Geera teaches fishing and dock economy. Scoria will teach advanced Create tech. Azerion introduces Create Big Cannons discipline.\n\n"
                + "Design note: this book is the guidebook-style hub for systems, status, clues, and crew records.";
    }
}
