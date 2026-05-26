package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.resources.ResourceLocation;

public class GeeraDialogueScreen extends AbstractNpcDialogueScreen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("ciskspawn", "textures/gui/dialogue/geera_dialogue.png");
    private static final int TEX_W = 1535;
    private static final int TEX_H = 1024;

    public GeeraDialogueScreen(MortimerDialoguePayload payload) {
        super(payload);
    }

    @Override
    protected int textureWidth() {
        return TEX_W;
    }

    @Override
    protected int textureHeight() {
        return TEX_H;
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return BACKGROUND;
    }

    @Override
    protected TextZone textZone() {
        // Parchment panel on the right side of the painted texture.
        return new TextZone(680, 205, 655, 350, 18, 0xFF3B2410);
    }

    @Override
    protected void buildOptions() {
        /*
         * These rectangles align to the five empty blue bars painted into
         * geera_dialogue.png. To skin another NPC, keep this class's coordinate
         * style and swap the background plus zones for that NPC's own art.
         */
        if (isGeeraShop()) {
            addButtonRow(0, payload.optionOne(), "geera_buy_bait");
            addButtonRow(1, payload.optionTwo(), "geera_buy_rumor");
            addButtonRow(2, "Fishing work", "geera_quest");
            addButtonRow(3, "Sell catch", "geera_sell_catch");
            addButtonRow(4, "Goodbye", "close");
        } else {
            addButtonRow(0, payload.optionOne(), "geera_quest");
            addButtonRow(1, "Open shop", "geera_shop");
            addButtonRow(2, "Fishing tips", "geera_tips");
            addButtonRow(3, "Ask about the docks", "geera_tips");
            addButtonRow(4, "Goodbye", "close");
        }
    }

    private void addButtonRow(int row, String label, String action) {
        addOption(260, 650 + row * 72, 1030, 54, label, action);
    }

    private boolean isGeeraShop() {
        return payload.title().toLowerCase().contains("bait & tackle");
    }
}
