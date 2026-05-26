package com.quickqwek.ciskspawn.client;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Bridges the network packet to the on-screen cutscene overlay.
 * Reads assets/ciskspawn/textures/cutscene/<name>.json for cutscene config:
 *   {"fps": 10, "length": 100}
 * then opens a CutsceneScreen that blits frame_001.png .. frame_<length>.png.
 */
public final class CutsceneClient {
    private CutsceneClient() {}

    /** Called on the client thread when a cutscene packet arrives. */
    public static void start(String name) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        ResourceLocation manifestId =
                ResourceLocation.fromNamespaceAndPath(CiskSpawnMod.MODID, "textures/cutscene/" + name + ".json");
        Optional<Resource> res = mc.getResourceManager().getResource(manifestId);
        if (res.isEmpty()) {
            CiskSpawnMod.LOG.warn("[CISK] Cutscene manifest missing: {}", manifestId);
            return;
        }

        int fps = 10;
        int length = 0;
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                res.get().open(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            String json = sb.toString();
            fps = parseInt(json, "fps", 10);
            length = parseInt(json, "length", 0);
        } catch (Throwable t) {
            CiskSpawnMod.LOG.error("[CISK] Failed to read cutscene manifest {}: {}", manifestId, t.getMessage());
            return;
        }

        if (length <= 0) {
            CiskSpawnMod.LOG.warn("[CISK] Cutscene {} has length=0; ignoring.", name);
            return;
        }

        CiskSpawnMod.LOG.info("[CISK] Playing cutscene '{}' ({} frames @ {} fps)", name, length, fps);
        mc.setScreen(new CutsceneScreen(name, length, fps));
    }

    /** Tiny dependency-free JSON-int extractor for {"key": NN, ...}. */
    private static int parseInt(String json, String key, int fallback) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return fallback;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return fallback;
        StringBuilder num = new StringBuilder();
        for (int i = colon + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || (num.length() == 0 && c == '-')) num.append(c);
            else if (num.length() > 0) break;
        }
        try { return Integer.parseInt(num.toString()); } catch (Exception e) { return fallback; }
    }
}
