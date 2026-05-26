package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerActionPayload;
import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class MortimerDialogueScreen extends Screen {
    private static final int FRAME = 0xEE2B1B14;
    private static final int INNER = 0xEE16343A;
    private static final int INNER_DARK = 0xEE0D2026;
    private static final int BRASS = 0xFFD7A84A;
    private static final int BRASS_DARK = 0xFF7B5425;
    private static final int BRASS_FAINT = 0xAA7B5425;
    private static final int TEAL = 0xFF46C7C7;
    private static final int TEAL_DARK = 0xDD0D4E58;
    private static final int TEAL_HOVER = 0xEE146B78;
    private static final int TEXT = 0xFFEFE2C2;
    private static final int MUTED = 0xFFB8AA86;

    private final MortimerDialoguePayload payload;
    private final List<OptionRegion> options = new ArrayList<>();
    private int crewLogPage = 0;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    private boolean isGeera() {
        return payload.title().toLowerCase().contains("geera");
    }

    private boolean isScoria() {
        return payload.title().toLowerCase().contains("scoria");
    }

    private boolean isAzerion() {
        String title = payload.title().toLowerCase();
        return title.contains("azerion") || title.contains("az mk");
    }

    private boolean isGeeraShop() {
        return payload.title().toLowerCase().contains("bait & tackle");
    }

    private boolean isGeeraWorkstation() { return payload.title().toLowerCase().contains("geera workstation"); }
    private boolean isMortimerGuildStatus() { return payload.title().toLowerCase().contains("mortimer guild status"); }

    private boolean isCrewLog() {
        return payload.title().toLowerCase().contains("crew logbook");
    }

    public MortimerDialogueScreen(MortimerDialoguePayload payload) {
        super(Component.literal(payload.title()));
        this.payload = payload;
    }

    @Override
    protected void init() {
        this.panelW = Math.min(430, this.width - 32);
        this.panelH = 288;
        this.panelX = this.width - this.panelW - 24;
        this.panelY = Math.max(14, this.height / 2 - 138);

        options.clear();
        int buttonW = (panelW - 42) / 2;
        int buttonH = 22;
        int buttonY = panelY + panelH - 108;

        if (isCrewLog()) {
            options.add(new OptionRegion(panelX + 14, buttonY, buttonW, buttonH, "Crew", "log_crew"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY, buttonW, buttonH, "Relationships", "log_relationships"));
            options.add(new OptionRegion(panelX + 14, buttonY + 27, buttonW, buttonH, "Clues", "log_clues"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY + 27, buttonW, buttonH, "Systems", "log_systems"));
            options.add(new OptionRegion(panelX + 14, buttonY + 54, panelW - 28, buttonH, "Close logbook", "close"));
        } else if (isGeeraShop()) {
            options.add(new OptionRegion(panelX + 14, buttonY, buttonW, buttonH, payload.optionOne(), "geera_buy_bait"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY, buttonW, buttonH, payload.optionTwo(), "geera_buy_rumor"));
            options.add(new OptionRegion(panelX + 14, buttonY + 27, buttonW, buttonH, "Fishing work", "geera_quest"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY + 27, buttonW, buttonH, "Sell catch", "geera_sell_catch"));
            options.add(new OptionRegion(panelX + 14, buttonY + 54, panelW - 28, buttonH, "Goodbye", "close"));
        } else if (isGeera()) {
            options.add(new OptionRegion(panelX + 14, buttonY, buttonW, buttonH, payload.optionOne(), "geera_quest"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY, buttonW, buttonH, "Open shop", "geera_shop"));
            options.add(new OptionRegion(panelX + 14, buttonY + 27, buttonW, buttonH, "Fishing tips", "geera_tips"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY + 27, buttonW, buttonH, "Goodbye", "close"));
        } else if (isAzerion()) {
            options.add(new OptionRegion(panelX + 14, buttonY, buttonW, buttonH, "Begin cannon drill", "azerion_drill"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY, buttonW, buttonH, "Operational query", "azerion_query"));
            options.add(new OptionRegion(panelX + 14, buttonY + 27, buttonW, buttonH, "Relationship", "azerion_relationship"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY + 27, buttonW, buttonH, "Goodbye", "close"));
        } else if (isScoria()) {
            options.add(new OptionRegion(panelX + 14, buttonY, buttonW, buttonH, "Engineering lesson", "scoria_lesson"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY, buttonW, buttonH, "About Mortimer", "scoria_mortimer"));
            options.add(new OptionRegion(panelX + 14, buttonY + 27, buttonW, buttonH, "Advance project", "scoria_project"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY + 27, buttonW, buttonH, "Goodbye", "close"));
        } else {
            options.add(new OptionRegion(panelX + 14, buttonY, buttonW, buttonH, payload.optionOne(), "talk"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY, buttonW, buttonH, payload.optionTwo(), "travel"));
            options.add(new OptionRegion(panelX + 14, buttonY + 27, buttonW, buttonH, "Follow me", "follow"));
            options.add(new OptionRegion(panelX + 28 + buttonW, buttonY + 27, buttonW, buttonH, "Goodbye", "close"));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Keep the world visible. Mortimer's dialogue is a diegetic floating Guild panel, not a pause screen.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int bob = (int)(Math.sin((Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime()) / 12.0) * 2.0);
        int y = panelY + bob;
        int yOffset = y - panelY;

        drawPanel(graphics, panelX, y, panelW, panelH);

        graphics.drawString(this.font, payload.title(), panelX + 16, y + 12, BRASS, false);
        graphics.hLine(panelX + 14, panelX + panelW - 14, y + 27, BRASS_DARK);
        String subtitle = isCrewLog() ? "✦ Persistent crew notes" : (isGeeraShop() ? "✦ Dockside shop" : (isAzerion() ? "✦ CBC artillery training interface" : (isScoria() ? "✦ Apprentice engineering notes" : (isGeera() ? "✦ Dockside fishing ledger" : "✦ Aero Guild projection interface"))));
        graphics.drawString(this.font, subtitle, panelX + 16, y + 32, MUTED, false);

        int textY = y + 50;
        int maxTextY = y + panelH - 132;
        List<FormattedCharSequence> bodyLines = this.font.split(Component.literal(getDisplayBody()), panelW - 32);
        for (int i = 0; i < bodyLines.size(); i++) {
            if (textY + 10 > maxTextY) {
                graphics.drawString(this.font, "...", panelX + 16, textY, MUTED, false);
                break;
            }
            graphics.drawString(this.font, bodyLines.get(i), panelX + 16, textY, TEXT, false);
            textY += 10;
        }

        int footerY = y + panelH - 108;
        List<FormattedCharSequence> footerLines = this.font.split(Component.literal(getDisplayFooter()), panelW - 32);
        for (int i = 0; i < Math.min(2, footerLines.size()); i++) {
            graphics.drawString(this.font, footerLines.get(i), panelX + 16, footerY + i * 10, TEAL, false);
        }

        for (OptionRegion option : options) {
            drawOption(graphics, option, mouseX, mouseY, yOffset);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int yOffset = (int)(Math.sin((Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime()) / 12.0) * 2.0);
            for (OptionRegion option : options) {
                if (option.contains(mouseX, mouseY - yOffset)) {
                    if (isCrewLog() && option.action.startsWith("log_")) {
                        if ("log_crew".equals(option.action)) crewLogPage = 0;
                        if ("log_relationships".equals(option.action)) crewLogPage = 1;
                        if ("log_clues".equals(option.action)) crewLogPage = 2;
                        if ("log_systems".equals(option.action)) crewLogPage = 3;
                        return true;
                    }
                    if (!"close".equals(option.action)) {
                        PacketDistributor.sendToServer(new MortimerActionPayload(payload.entityId(), option.action));
                    }
                    this.onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String getDisplayBody() {
        if (!isCrewLog()) return payload.body();
        return switch (crewLogPage) {
            case 1 -> "RELATIONSHIPS\n\nMortimer: protective, restless, and slowly learning to trust the player as crew. Trust grows through travel, repairs, and guild work.\n\nGeera: amused, practical, and warmer than she lets on. Trust grows through fishing work, shop visits, and dockside help.\n\nScoria: nervous, proud, and hiding the truth of his apprenticeship. Trust grows through engineering lessons and project stages.\n\nAzerion: loyal, formal, and difficult to read. Trust grows through artillery drills and safe cannon handling.";
            case 2 -> "CLUES\n\nScoria's so-called banker clothes are actually interview clothes. Geera knows the truth and has been enjoying the misunderstanding.\n\nThe clues point toward Aero Guild apprenticeship, advanced propulsion, levitite stabilizers, and thruster work.\n\nMortimer is afraid his son chose safety over skyfaring, but the real reveal should make him proud.";
            case 3 -> "SYSTEMS\n\nGeera's shop opens every 3rd Minecraft day. Her summon location acts as her dock/shop anchor.\n\nNPC moods and relationship notes live here instead of cluttering dialogue panels.\n\nThe Guilded Compass and Crew Logbook live in the Islands in the Sky creative tab.\n\nAzerion's questline introduces Create Big Cannons through drills, safety checks, and artillery discipline.";
            default -> "CREW\n\nMortimer: veteran aeromancer, mentor, former skyfarer of the Abalone. Mood: watchful, sentimental, restless.\n\nGeera: fisherwoman and bait seller. Mood: amused, grounded, observant.\n\nScoria: half human, half goblin apprentice engineer. Mood: nervous, hopeful, secretive.\n\nAzerion Rook: AZ Mk 9 autonomous warframe and former crew member. Mood: operational, loyal, emotionally encrypted.";
        };
    }

    private String getDisplayFooter() {
        if (!isCrewLog()) return payload.footer();
        return switch (crewLogPage) {
            case 1 -> "Relationship details are tracked here instead of NPC menus.";
            case 2 -> "Clues collect here as the Scoria reveal progresses.";
            case 3 -> "Guidebook systems page for lightweight RPG mechanics.";
            default -> "Crew status, moods, and role summaries.";
        };
    }

    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, FRAME);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, BRASS_DARK);
        g.fill(x + 6, y + 6, x + w - 6, y + h - 6, INNER);
        g.fill(x + 10, y + 40, x + w - 10, y + h - 86, INNER_DARK);

        int s = 5;
        g.fill(x + 8, y + 8, x + 8 + s, y + 8 + s, BRASS);
        g.fill(x + w - 13, y + 8, x + w - 8, y + 13, BRASS);
        g.fill(x + 8, y + h - 13, x + 13, y + h - 8, BRASS);
        g.fill(x + w - 13, y + h - 13, x + w - 8, y + h - 8, BRASS);

        g.hLine(x + 16, x + w - 16, y + h - 114, TEAL);
        g.hLine(x + 16, x + w - 16, y + h - 95, BRASS_FAINT);
    }

    private void drawOption(GuiGraphics g, OptionRegion option, int mouseX, int mouseY, int yOffset) {
        int x = option.x;
        int y = option.y + yOffset;
        boolean hovered = option.contains(mouseX, mouseY - yOffset);
        g.fill(x, y, x + option.w, y + option.h, BRASS_DARK);
        g.fill(x + 2, y + 2, x + option.w - 2, y + option.h - 2, hovered ? TEAL_HOVER : TEAL_DARK);
        g.hLine(x + 4, x + option.w - 4, y + 4, hovered ? TEAL : BRASS_FAINT);
        String label = option.label;
        int textColor = hovered ? 0xFFFFD56A : 0xFFE7C05A;
        int textX = x + Math.max(8, (option.w - this.font.width(label)) / 2);
        g.drawString(this.font, label, textX, y + 7, textColor, false);
    }

    private static class OptionRegion {
        final int x;
        final int y;
        final int w;
        final int h;
        final String label;
        final String action;

        OptionRegion(int x, int y, int w, int h, String label, String action) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.action = action;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        }
    }
}
