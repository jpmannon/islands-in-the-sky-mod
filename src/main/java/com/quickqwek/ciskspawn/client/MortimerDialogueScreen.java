package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.network.MortimerActionPayload;
import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class MortimerDialogueScreen extends Screen {

    // ── Colours ───────────────────────────────────────────────────────────────
    private static final int FRAME        = 0xEE2B1B14;
    private static final int INNER        = 0xEE16343A;
    private static final int INNER_DARK   = 0xEE0D2026;
    private static final int PARCHMENT    = 0xEED4B483;   // dialogue text background
    private static final int PARCH_TEXT   = 0xFF2B1A08;   // dark ink on parchment
    private static final int BRASS        = 0xFFD7A84A;
    private static final int BRASS_DARK   = 0xFF7B5425;
    private static final int BRASS_FAINT  = 0xAA7B5425;
    private static final int TEAL         = 0xFF46C7C7;
    private static final int TEAL_DARK    = 0xDD0D4E58;
    private static final int TEAL_HOVER   = 0xEE146B78;
    private static final int MUTED        = 0xFFB8AA86;

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int PORTRAIT_SIZE  = 120;   // portrait texture must be 120×120 px
    private static final int TITLE_H        = 38;    // height of title + subtitle strip
    private static final int TOP_H          = PORTRAIT_SIZE + 8;
    private static final int BUTTON_H       = 22;
    private static final int BUTTON_GAP     = 5;
    private static final int BUTTON_ROWS    = 3;
    private static final int BUTTONS_H      = BUTTON_ROWS * (BUTTON_H + BUTTON_GAP) + 8;

    // ── State ─────────────────────────────────────────────────────────────────
    private final MortimerDialoguePayload payload;
    private final List<OptionRegion> options = new ArrayList<>();
    private int panelX, panelY, panelW, panelH;

    // ── NPC detection ─────────────────────────────────────────────────────────
    private boolean isGeera()      { String t = lo(); return t.contains("geera") && !isGeeraShop() && !isGeeraWorkstation(); }
    private boolean isScoria()     { return lo().contains("scoria"); }
    private boolean isAzerion()    { String t = lo(); return t.contains("azerion") || t.contains("az mk"); }
    private boolean isJoelle()     { return lo().contains("joelle"); }
    private boolean isRamone()     { return lo().contains("ramone"); }
    private boolean isVelho()      { return lo().contains("velho"); }
    private boolean isGeeraShop()  { return lo().contains("bait & tackle"); }
    private boolean isGeeraWorkstation()    { return lo().contains("geera workstation"); }
    private boolean isMortimerGuildStatus() { return lo().contains("mortimer guild status"); }
    private boolean isCrewLog()    { return lo().contains("crew logbook"); }
    private String lo()            { return payload.title().toLowerCase(); }

    // ── Portrait textures ─────────────────────────────────────────────────────
    // Drop a 120×120 PNG into:
    //   src/main/resources/assets/ciskspawn/textures/gui/portrait/<name>.png
    // The dark placeholder frame shows automatically when a file is missing.
    private ResourceLocation getPortraitTexture() {
        if (isAzerion())                      return portrait("azerion");
        if (isScoria())                       return portrait("scoria");
        if (isGeeraShop() || isGeera())       return portrait("geera");
        if (isJoelle())                       return portrait("joelle");
        if (isRamone())                       return portrait("ramone");
        if (isVelho())                        return portrait("velho");
        return portrait("mortimer"); // default / crew log
    }

    private static ResourceLocation portrait(String name) {
        return ResourceLocation.fromNamespaceAndPath("ciskspawn", "textures/gui/portrait/" + name + ".png");
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public MortimerDialogueScreen(MortimerDialoguePayload payload) {
        super(Component.literal(payload.title()));
        this.payload = payload;
    }

    // ── Init ──────────────────────────────────────────────────────────────────
    @Override
    protected void init() {
        panelW = Math.min(480, this.width - 32);
        panelH = TITLE_H + TOP_H + 10 + BUTTONS_H + 12;
        panelX = (this.width - panelW) / 2;            // centred
        panelY = Math.max(14, this.height / 2 - panelH / 2);

        options.clear();
        int pad   = 14;
        int bw    = (panelW - pad * 2 - 8) / 2;       // button width (2-column)
        int bax   = panelX + pad;                      // button area left
        int bay   = panelY + TITLE_H + TOP_H + 14;    // button area top

        if (isCrewLog()) {
            addBtn(bax,         bay,              bw,           "Crew",          "log_crew");
            addBtn(bax + bw+8,  bay,              bw,           "Clues",         "log_clues");
            addBtn(bax,         bay+row(1),       bw,           "Systems",       "log_systems");
            addBtn(bax + bw+8,  bay+row(1),       bw,           "Notes",         "log_notes");
            addBtn(bax,         bay+row(2),       panelW-pad*2, "Close logbook", "close");

        } else if (isGeeraShop()) {
            addBtn(bax,         bay,              bw,           payload.optionOne(),  "geera_buy_bait");
            addBtn(bax + bw+8,  bay,              bw,           payload.optionTwo(),  "geera_buy_rumor");
            addBtn(bax,         bay+row(1),       bw,           "Fishing work",       "geera_quest");
            addBtn(bax + bw+8,  bay+row(1),       bw,           "Sell catch",         "geera_sell_catch");
            addBtn(bax,         bay+row(2),       panelW-pad*2, "Goodbye",            "close");

        } else if (isGeera()) {
            addBtn(bax,         bay,              bw,           payload.optionOne(),  "geera_quest");
            addBtn(bax + bw+8,  bay,              bw,           "Open shop",          "geera_shop");
            addBtn(bax,         bay+row(1),       bw,           "Fishing tips",       "geera_tips");
            addBtn(bax + bw+8,  bay+row(1),       bw,           "Goodbye",            "close");

        } else if (isAzerion()) {
            addBtn(bax,         bay,              bw,           "Begin cannon drill", "azerion_drill");
            addBtn(bax + bw+8,  bay,              bw,           "Operational query",  "azerion_query");
            addBtn(bax,         bay+row(1),       bw,           "Relationship",       "azerion_relationship");
            addBtn(bax + bw+8,  bay+row(1),       bw,           "Goodbye",            "close");

        } else if (isJoelle()) {
            addBtn(bax,         bay,              bw,           "Cooking quest",      "joelle_quest");
            addBtn(bax + bw+8,  bay,              bw,           "Ask for a recipe",   "joelle_recipe");
            addBtn(bax,         bay+row(1),       panelW-pad*2, "Goodbye",            "close");

        } else if (isRamone()) {
            addBtn(bax,         bay,              bw,           "Garden work",        "ramone_quest");
            addBtn(bax + bw+8,  bay,              bw,           "Garden tip",         "ramone_garden");
            addBtn(bax,         bay+row(1),       bw,           "About the sky",      "ramone_sky");
            addBtn(bax + bw+8,  bay+row(1),       bw,           "Goodbye",            "close");

        } else if (isVelho()) {
            if ("Wait".equals(payload.optionOne()) || lo().contains("not looked up")) {
                addBtn(bax, bay, panelW - pad * 2, "Wait", "velho_wait");
            } else {
                addBtn(bax,         bay,          bw,           "Enchanting work",    "velho_quest");
                addBtn(bax + bw+8,  bay,          bw,           "Enchanting tip",     "velho_enchanting");
                addBtn(bax,         bay+row(1),   bw,           "About Azerion",      "velho_azerion");
                addBtn(bax + bw+8,  bay+row(1),   bw,           "Goodbye",            "close");
            }

        } else if (isScoria()) {
            addBtn(bax,         bay,              bw,           "Engineering lesson", "scoria_lesson");
            addBtn(bax + bw+8,  bay,              bw,           "About Mortimer",     "scoria_mortimer");
            addBtn(bax,         bay+row(1),       bw,           "Advance project",    "scoria_project");
            addBtn(bax + bw+8,  bay+row(1),       bw,           "Goodbye",            "close");

        } else {
            // Mortimer default
            addBtn(bax,         bay,              bw,           payload.optionOne(),  "talk");
            addBtn(bax + bw+8,  bay,              bw,           payload.optionTwo(),  "travel");
            addBtn(bax,         bay+row(1),       bw,           "Follow me",          "follow");
            addBtn(bax + bw+8,  bay+row(1),       bw,           "Goodbye",            "close");
        }
    }

    /** Y offset for button row n (0-indexed). */
    private int row(int n) { return n * (BUTTON_H + BUTTON_GAP); }

    private void addBtn(int x, int y, int w, String label, String action) {
        options.add(new OptionRegion(x, y, w, BUTTON_H, label, action));
    }

    // ── Screen overrides ──────────────────────────────────────────────────────
    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        // World stays visible — diegetic floating panel
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long gt = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        int bob  = (int)(Math.sin(gt / 12.0) * 2.0);
        int y    = panelY + bob;
        int yOff = bob;

        drawPanel(g, panelX, y, panelW, panelH);

        // ── Title (centred) ──────────────────────────────────────
        String title  = payload.title();
        int titleX = panelX + (panelW - this.font.width(title)) / 2;
        g.drawString(this.font, title, titleX, y + 10, BRASS, false);
        g.hLine(panelX + 14, panelX + panelW - 14, y + 26, BRASS_DARK);
        String sub = getSubtitle();
        int subX = panelX + (panelW - this.font.width(sub)) / 2;
        g.drawString(this.font, sub, subX, y + TITLE_H - 8, MUTED, false);

        // ── Portrait ─────────────────────────────────────────────
        int px = panelX + 14;
        int py = y + TITLE_H + 4;
        drawPortrait(g, px, py);

        // ── Dialogue text (parchment) ─────────────────────────────
        int tx  = px + PORTRAIT_SIZE + 12;
        int tw  = panelW - (PORTRAIT_SIZE + 14 + 12 + 14);
        int ty  = py;
        int th  = PORTRAIT_SIZE;
        g.fill(tx - 2, ty - 2, tx + tw + 2, ty + th + 2, BRASS_DARK); // thin brass border
        g.fill(tx, ty, tx + tw, ty + th, PARCHMENT);

        int textY   = ty + 5;
        int maxTextY = ty + th - 8;
        List<FormattedCharSequence> lines = this.font.split(Component.literal(getDisplayBody()), tw - 8);
        for (FormattedCharSequence line : lines) {
            if (textY + 9 > maxTextY) {
                g.drawString(this.font, "...", tx + 4, textY, PARCH_TEXT, false);
                break;
            }
            g.drawString(this.font, line, tx + 4, textY, PARCH_TEXT, false);
            textY += 10;
        }

        // ── Footer / trust hint (slim strip above buttons) ────────
        int footerY = y + TITLE_H + TOP_H + 4;
        List<FormattedCharSequence> footerLines = this.font.split(Component.literal(getDisplayFooter()), panelW - 32);
        for (int i = 0; i < Math.min(1, footerLines.size()); i++) {
            g.drawString(this.font, footerLines.get(i), panelX + 16, footerY + i * 10, TEAL, false);
        }

        // ── Buttons ───────────────────────────────────────────────
        for (OptionRegion opt : options) drawOption(g, opt, mouseX, mouseY, yOff);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            long gt = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
            int yOff = (int)(Math.sin(gt / 12.0) * 2.0);
            for (OptionRegion opt : options) {
                if (opt.contains(mx, my - yOff)) {
                    if (isCrewLog() && opt.action.startsWith("log_")) {
                        PacketDistributor.sendToServer(new MortimerActionPayload(-1, opt.action));
                        this.onClose();
                        return true;
                    }
                    if (!"close".equals(opt.action))
                        PacketDistributor.sendToServer(new MortimerActionPayload(payload.entityId(), opt.action));
                    this.onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────
    private String getSubtitle() {
        if (isCrewLog())    return "✦ Persistent crew notes";
        if (isGeeraShop())  return "✦ Dockside shop";
        if (isAzerion())    return "✦ CBC artillery training interface";
        if (isVelho())      return "✦ Workshop notes";
        if (isRamone())     return "✦ Garden ledger";
        if (isScoria())     return "✦ Apprentice engineering notes";
        if (isGeera())      return "✦ Dockside fishing ledger";
        return "✦ Aero Guild projection interface";
    }

    private String getDisplayBody()   { return payload.body(); }
    private String getDisplayFooter() { return payload.footer(); }

    private void drawPortrait(GuiGraphics g, int x, int y) {
        // Brass frame
        g.fill(x - 3, y - 3, x + PORTRAIT_SIZE + 3, y + PORTRAIT_SIZE + 3, BRASS);
        g.fill(x - 1, y - 1, x + PORTRAIT_SIZE + 1, y + PORTRAIT_SIZE + 1, BRASS_DARK);
        // Dark background visible if texture is missing
        g.fill(x, y, x + PORTRAIT_SIZE, y + PORTRAIT_SIZE, INNER_DARK);
        // Portrait texture — place a 120×120 PNG at:
        //   src/main/resources/assets/ciskspawn/textures/gui/portrait/<npc>.png
        g.blit(getPortraitTexture(), x, y, 0, 0, PORTRAIT_SIZE, PORTRAIT_SIZE, PORTRAIT_SIZE, PORTRAIT_SIZE);
    }

    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, FRAME);
        g.fill(x + 3, y + 3, x + w - 3, y + h - 3, BRASS_DARK);
        g.fill(x + 6, y + 6, x + w - 6, y + h - 6, INNER);

        // Brass corner rivets
        int s = 5;
        g.fill(x + 8,     y + 8,     x + 8 + s,     y + 8 + s,     BRASS);
        g.fill(x + w - 13, y + 8,    x + w - 8,     y + 13,        BRASS);
        g.fill(x + 8,     y + h - 13, x + 13,       y + h - 8,     BRASS);
        g.fill(x + w - 13, y + h - 13, x + w - 8,  y + h - 8,     BRASS);

        // Divider above buttons
        int divY = y + TITLE_H + TOP_H + 8;
        g.hLine(x + 14, x + w - 14, divY,     TEAL);
        g.hLine(x + 14, x + w - 14, divY + 2, BRASS_FAINT);
    }

    private void drawOption(GuiGraphics g, OptionRegion opt, int mouseX, int mouseY, int yOff) {
        int x = opt.x;
        int y = opt.y + yOff;
        boolean hov = opt.contains(mouseX, mouseY - yOff);
        g.fill(x, y, x + opt.w, y + opt.h, BRASS_DARK);
        g.fill(x + 2, y + 2, x + opt.w - 2, y + opt.h - 2, hov ? TEAL_HOVER : TEAL_DARK);
        g.hLine(x + 4, x + opt.w - 4, y + 4, hov ? TEAL : BRASS_FAINT);
        int tc  = hov ? 0xFFFFD56A : 0xFFE7C05A;
        int tx  = x + Math.max(8, (opt.w - this.font.width(opt.label)) / 2);
        g.drawString(this.font, opt.label, tx, y + 7, tc, false);
    }

    // ── OptionRegion ─────────────────────────────────────────────────────────
    private static class OptionRegion {
        final int x, y, w, h;
        final String label, action;

        OptionRegion(int x, int y, int w, int h, String label, String action) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            this.label = label; this.action = action;
        }

        boolean contains(double mx, double my) {
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }
    }
}
