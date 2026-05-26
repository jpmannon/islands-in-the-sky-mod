package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerActionPayload;
import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNpcDialogueScreen extends Screen {
    protected final MortimerDialoguePayload payload;
    protected final List<OptionRegion> options = new ArrayList<>();
    protected int guiX;
    protected int guiY;
    protected int guiW;
    protected int guiH;
    protected float guiScale = 1.0F;

    protected AbstractNpcDialogueScreen(MortimerDialoguePayload payload) {
        super(Component.literal(payload.title()));
        this.payload = payload;
    }

    /**
     * Native pixel width of the full dialogue artwork. All button and text zones
     * are authored in this same coordinate space so future NPC backgrounds can
     * swap in without rewriting the scaling math.
     */
    protected abstract int textureWidth();

    /** Native pixel height of the full dialogue artwork. */
    protected abstract int textureHeight();

    protected abstract ResourceLocation backgroundTexture();

    protected abstract TextZone textZone();

    protected abstract void buildOptions();

    @Override
    protected void init() {
        layoutGui();
        options.clear();
        buildOptions();
    }

    private void layoutGui() {
        float scaleX = (float) this.width / (float) textureWidth();
        float scaleY = (float) this.height / (float) textureHeight();
        guiScale = Math.min(scaleX, scaleY);
        guiW = Math.max(1, Math.round(textureWidth() * guiScale));
        guiH = Math.max(1, Math.round(textureHeight() * guiScale));
        guiX = (this.width - guiW) / 2;
        guiY = (this.height - guiH) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // The artwork is the complete UI frame, so no vanilla dim or gray panel is drawn.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderArtwork(graphics);
        renderDialogueText(graphics);
        for (OptionRegion option : options) {
            renderOption(graphics, option, mouseX, mouseY);
        }
    }

    /**
     * Draws the full PNG as one crisp UI texture. The source rectangle is the
     * full native image; the destination rectangle is scaled uniformly so the
     * art keeps its exact aspect ratio on any resolution.
     */
    protected void renderArtwork(GuiGraphics graphics) {
        graphics.blit(backgroundTexture(), guiX, guiY, guiW, guiH, 0.0F, 0.0F, textureWidth(), textureHeight(), textureWidth(), textureHeight());
    }

    /**
     * Renders wrapped dialogue inside the parchment area already painted into
     * the background art. No extra panel is drawn over the image.
     */
    protected void renderDialogueText(GuiGraphics graphics) {
        TextZone zone = textZone();
        int x = sx(zone.x());
        int y = sy(zone.y());
        int w = sw(zone.w());
        int maxY = sy(zone.y() + zone.h());
        int lineHeight = Math.max(8, Math.round(zone.lineHeight() * guiScale));
        int textColor = zone.color();
        List<FormattedCharSequence> lines = this.font.split(Component.literal(payload.body()), Math.max(24, w));
        int textY = y;
        for (FormattedCharSequence line : lines) {
            if (textY + lineHeight > maxY) {
                graphics.drawString(this.font, "...", x, textY, textColor, false);
                break;
            }
            graphics.drawString(this.font, line, x, textY, textColor, false);
            textY += lineHeight;
        }
    }

    /**
     * Button regions sit directly on top of the painted blue bars. Only text and
     * a subtle hover tint are drawn, leaving the background artwork intact.
     */
    protected void renderOption(GuiGraphics graphics, OptionRegion option, int mouseX, int mouseY) {
        int x = sx(option.x());
        int y = sy(option.y());
        int w = sw(option.w());
        int h = sh(option.h());
        boolean hovered = contains(option, mouseX, mouseY);
        if (hovered) {
            graphics.fill(x, y, x + w, y + h, 0x3300D8FF);
        }
        int textColor = hovered ? 0xFFFFE5A3 : 0xFFECC77D;
        int textX = x + Math.max(4, (w - this.font.width(option.label())) / 2);
        int textY = y + Math.max(2, (h - 8) / 2);
        graphics.drawString(this.font, option.label(), textX, textY, textColor, false);
    }

    protected void addOption(int x, int y, int w, int h, String label, String action) {
        options.add(new OptionRegion(x, y, w, h, label, action));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (OptionRegion option : options) {
                if (contains(option, mouseX, mouseY)) {
                    if (!"close".equals(option.action())) {
                        PacketDistributor.sendToServer(new MortimerActionPayload(payload.entityId(), option.action()));
                    }
                    this.onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean contains(OptionRegion option, double mouseX, double mouseY) {
        int x = sx(option.x());
        int y = sy(option.y());
        int w = sw(option.w());
        int h = sh(option.h());
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    protected int sx(int nativeX) {
        return guiX + Math.round(nativeX * guiScale);
    }

    protected int sy(int nativeY) {
        return guiY + Math.round(nativeY * guiScale);
    }

    protected int sw(int nativeW) {
        return Math.round(nativeW * guiScale);
    }

    protected int sh(int nativeH) {
        return Math.round(nativeH * guiScale);
    }

    protected record TextZone(int x, int y, int w, int h, int lineHeight, int color) {}

    protected record OptionRegion(int x, int y, int w, int h, String label, String action) {}
}
