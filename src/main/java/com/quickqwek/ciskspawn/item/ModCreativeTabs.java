package com.quickqwek.ciskspawn.item;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CiskSpawnMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ciskspawn.main"))
                    .icon(() -> new ItemStack(ModItems.CREW_LOGBOOK.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CREW_LOGBOOK.get());
                        output.accept(ModItems.GUILDED_COMPASS.get());
                        output.accept(ModItems.NPC_ANCHOR_ITEM.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
