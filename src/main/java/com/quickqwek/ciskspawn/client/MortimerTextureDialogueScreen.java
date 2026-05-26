package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.resources.ResourceLocation;

public class MortimerTextureDialogueScreen extends AbstractNpcDialogueScreen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("ciskspawn", "textures/gui/dialogue/mortimer_dialogue.png");
    private static final int TEX_W = 1549;
    private static final int TEX_H = 1015;

    public MortimerTextureDialogueScreen(MortimerDialoguePayload payload) {
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
        // Right parchment rectangle in MortimerUI.png.
        return new TextZone(615, 190, 725, 350, 18, 0xFF3B2410);
    }

    @Override
    protected void buildOptions() {
        /*
         * Five regions mapped to the painted blue bars. Swap this texture,
         * text zone, and row coordinates for another NPC skin.
         */
        addButtonRow(0, payload.optionOne(), "talk");
        addButtonRow(1, payload.optionTwo(), "travel");
        addButtonRow(2, "Follow me", "follow");
        addButtonRow(3, "Scan seats", "scan_seats");
        addButtonRow(4, "Goodbye", "close");
    }

    private void addButtonRow(int row, String label, String action) {
        addOption(225, 615 + row * 70, 1090, 52, label, action);
    }
}
