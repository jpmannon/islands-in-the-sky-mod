package com.quickqwek.ciskspawn.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public class CrewLogbookItem extends Item {
    private static final ResourceLocation BOOK_ID = ResourceLocation.fromNamespaceAndPath("ciskspawn", "crew_logbook");

    public CrewLogbookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openPatchouliBook(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void openPatchouliBook(ServerPlayer player) {
        if (!ModList.get().isLoaded("patchouli")) {
            player.displayClientMessage(Component.literal("The Crew Logbook needs Patchouli to open."), false);
            return;
        }

        try {
            Class<?> patchouliApi = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Class<?> patchouliApiInterface = Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI");
            Object api = patchouliApi.getMethod("get").invoke(null);
            Method openBook = patchouliApiInterface.getMethod("openBookGUI", ServerPlayer.class, ResourceLocation.class);
            openBook.invoke(api, player, BOOK_ID);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            player.displayClientMessage(Component.literal("The Crew Logbook could not open its Patchouli pages."), false);
        }
    }
}
