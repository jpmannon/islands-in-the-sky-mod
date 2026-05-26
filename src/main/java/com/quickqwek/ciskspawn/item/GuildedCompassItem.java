package com.quickqwek.ciskspawn.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuildedCompassItem extends Item {
    public GuildedCompassItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.literal("§6[Guilded Compass] §7The needle twitches toward safe routes, old mistakes, and unfinished voyages."));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
