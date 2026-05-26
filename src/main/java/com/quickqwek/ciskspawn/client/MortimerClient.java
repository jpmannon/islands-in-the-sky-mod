package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.client.Minecraft;

public final class MortimerClient {
    private MortimerClient() {}

    public static void openDialogue(MortimerDialoguePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (payload.title().toLowerCase().contains("geera")) {
            minecraft.setScreen(new GeeraDialogueScreen(payload));
        } else {
            minecraft.setScreen(new MortimerDialogueScreen(payload));
        }
    }
}
