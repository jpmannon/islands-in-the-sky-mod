package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

/**
 * Full-screen, modal cutscene overlay. Blits a sequence of PNG frames from
 *   assets/ciskspawn/textures/cutscene/<name>/frame_NNN.png
 * for `length` frames at `fps` frames per second. Closes itself when done.
 *
 * Frame files must be exactly named frame_001.png .. frame_<length>.png
 * (3-digit zero-padded). The aspect of each PNG should match your intended
 * 16:9 display; the screen scales each frame to fill the window.
 *
 * Skippable with SPACE or ESCAPE (you can disable this by removing the
 * keyPressed override below if you want the player locked in).
 */
public class CutsceneScreen extends Screen {

    private final String name;
    private final int length;
    private final int fps;
    private final long startNanos;

    public CutsceneScreen(String name, int length, int fps) {
        super(Component.literal("Cutscene"));
        this.name = name;
        this.length = Math.max(1, length);
        this.fps = Math.max(1, fps);
        this.startNanos = System.nanoTime();
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Plain black background, no vanilla overlay fade
        g.fill(0, 0, this.width, this.height, 0xFF000000);

        long elapsedNanos = System.nanoTime() - startNanos;
        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        int frame = (int) Math.floor(elapsedSeconds * fps) + 1;

        if (frame > length) {
            this.onClose();
            return;
        }

        String frameName = String.format("%s/frame_%03d", name, frame);
        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
                CiskSpawnMod.MODID, "textures/cutscene/" + frameName + ".png");

        // Blit the texture stretched to fill the screen (preserving texture's own resolution)
        g.blit(tex, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) mc.setScreen(null);
    }
}
