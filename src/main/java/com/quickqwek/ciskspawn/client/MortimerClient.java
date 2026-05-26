package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.client.Minecraft;

public final class MortimerClient {
    private MortimerClient() {}

    public static void openDialogue(MortimerDialoguePayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new MortimerDialogueScreen(payload));
    }
}
