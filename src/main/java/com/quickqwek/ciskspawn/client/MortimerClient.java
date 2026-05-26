package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.client.Minecraft;

public final class MortimerClient {
    private MortimerClient() {}

    public static void openDialogue(MortimerDialoguePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        String title = payload.title().toLowerCase();
        if (title.contains("geera")) {
            minecraft.setScreen(new GeeraDialogueScreen(payload));
        } else if (title.contains("scoria")) {
            minecraft.setScreen(new ScoriaDialogueScreen(payload));
        } else if (title.contains("mortimer")) {
            minecraft.setScreen(new MortimerTextureDialogueScreen(payload));
        } else {
            minecraft.setScreen(new MortimerDialogueScreen(payload));
        }
    }
}
