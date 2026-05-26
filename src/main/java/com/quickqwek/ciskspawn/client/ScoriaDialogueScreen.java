package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.resources.ResourceLocation;

public class ScoriaDialogueScreen extends AbstractNpcDialogueScreen {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("ciskspawn", "textures/gui/dialogue/scoria_dialogue.png");
    private static final int TEX_W = 1549;
    private static final int TEX_H = 1015;

    public ScoriaDialogueScreen(MortimerDialoguePayload payload) {
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
        // Right parchment rectangle in ScoriaUI.png.
        return new TextZone(660, 190, 700, 350, 18, 0xFF3B2410);
    }

    @Override
    protected void buildOptions() {
        // These options sit over Scoria's purple painted choice bars.
        addButtonRow(0, "Engineering lesson", "scoria_lesson");
        addButtonRow(1, "About Mortimer", "scoria_mortimer");
        addButtonRow(2, "Advance project", "scoria_project");
        addButtonRow(3, "Project status", "scoria_project");
        addButtonRow(4, "Goodbye", "close");
    }

    private void addButtonRow(int row, String label, String action) {
        addOption(225, 630 + row * 70, 1090, 52, label, action);
    }
}
