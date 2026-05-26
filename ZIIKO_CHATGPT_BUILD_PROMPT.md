# ChatGPT Build Prompt — ZiikoEntity.java

---

## PROMPT

I am building a NeoForge 1.21.1 Minecraft mod (`ciskspawn`, package `com.quickqwek.ciskspawn`). Write `ZiikoEntity.java` — a complete, compilable entity class. Follow the exact patterns from this mod.

---

### CHARACTER OVERVIEW

Zii-ko is a Pogon — a small kobold-like race, approximately 3 feet tall, about the size of a child. He is large and considered attractive by Pogon standards. He runs his own smithing shop on solid ground (NOT on a ship). He speaks in broken English: short clauses, dropped articles, direct address, no hedging. His native language includes guttural clicks and hissing sounds represented in text as `[click-hiss]`. Pogon assume the best of everyone and take bad news in stride. They only do things they genuinely want to do.

---

### MOD CONTEXT

- NeoForge 1.21.1, Java 21, GeckoLib 4.7.1
- Package: `com.quickqwek.ciskspawn.entity`
- All NPCs extend `PathfinderMob implements GeoEntity`
- Network: `MortimerDialoguePayload` (server→client), `MortimerActionPayload` (client→server)
- `setPersistenceRequired()` in every constructor
- `removeWhenFarAway()` returns false

---

### PLAYER PROGRESS

```java
private static class PlayerProgress {
    int trust = 0;
    boolean hasMet = false;
    int forgeCount = 0;   // tracks total forge interactions (not a quest stage — repeatable)
}
```

Trust cap 100. No linear quest stages — Zii-ko has a repeatable forge interaction.

---

### ATTRIBUTES

```java
public static AttributeSupplier.Builder createAttributes() {
    return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.15D)
            .add(Attributes.FOLLOW_RANGE, 12.0D);
}
```

---

### GOALS

```java
this.goalSelector.addGoal(0, new FloatGoal(this));
this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.40D));
this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
```

---

### GUI PAYLOAD

```java
PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
    this.getId(),
    "Zii-ko - Smith",
    body,
    "Bring materials",
    "Ask about materials",
    footer
));
```

---

### ACTION HANDLER

```java
public void handleZiikoAction(Player player, String action) {
    switch (action) {
        case "ziiko_forge"     -> handleForge(player);
        case "ziiko_materials" -> materialsTip(player);
        case "ziiko_himself"   -> aboutHimself(player);
        case "ziiko_crew"      -> aboutOthers(player);
        default                -> openDialogue(player);
    }
}
```

---

### FIRST MEETING

On first `openDialogue()`, `hasMet = true`, trust +1. Send:

```
Title:  "Zii-ko - Smith"
Body:   "I am Zii-ko, who you?\n\nYou want sword? I give you good price. 
         Bring materials — only good materials. You come back then, I make. Go now."
Btn1:   "Bring materials"
Btn2:   "Ask about materials"
Footer: "He looked you over quickly. Apparently satisfied."
```

---

### GREETINGS — pickZiikoGreeting(PlayerProgress progress)

```java
private String pickZiikoGreeting(PlayerProgress progress) {
    if (progress.trust >= 80) return randomOf(
        "Oh good, it's you. Sit. I finish this and then we talk. I like when you visit.",
        "I made something for you. Not because you ask — because I wanted to. You are good customer and also good person. Both matter.",
        "You know, most humans — [click-hiss] — they come, they take, they go. You are different. You stay. I like that."
    );
    if (progress.trust >= 55) return randomOf(
        "My shop doing good today. I am happy. Happy Pogon makes better things — this is true fact.",
        "You know, in Pogon we say: 'The one who comes back is the one worth making for.' You keep coming back. This is good thing.",
        "I was thinking — you travel a lot, yes? You see strange metals anywhere? Bring me, I study."
    );
    if (progress.trust >= 35) return randomOf(
        "Good, good. You come at good time. I am testing new grip design. You want to see?",
        "I was thinking about you! I have idea for better sword. Come.",
        "You know Agatha? She bring me tea this morning. Good tea. I like her cats. They sit on my materials. I don't mind."
    );
    if (progress.trust >= 15) return randomOf(
        "Oi! Human! You back! Good. What you need.",
        "You again. Still alive? Good job. What you bring me.",
        "I remember you. You had — [click-hiss] — okay materials last time. You do better this time?"
    );
    return randomOf(
        "I am Zii-ko, who you?",
        "You new? Come. I show you what I make.",
        "Oi! New face! You want something made? Tell me."
    );
}
```

---

### FORGE SYSTEM — handleForge(Player player)

This is the core mechanic. It is **repeatable** — the player can use the forge multiple times, each time bringing different materials. Check tiers from best to worst. Each interaction increments `forgeCount` and adds trust.

**Check order: Netherite → Diamond → Iron → Nothing**

```java
private void handleForge(Player player) {
    PlayerProgress progress = getProgress(player);

    // Tier 3 — Netherite
    if (hasItem(player, "minecraft:netherite_ingot", 1)
            && hasItem(player, "minecraft:iron_ingot", 4)
            && hasItem(player, "minecraft:blaze_rod", 2)) {
        consumeItem(player, "minecraft:netherite_ingot", 1);
        consumeItem(player, "minecraft:iron_ingot", 4);
        consumeItem(player, "minecraft:blaze_rod", 2);
        progress.forgeCount++;
        addTrust(progress, 12);
        say(player, "Oh. Oh! This is good. This is very good. [click-hiss] You found netherite? Real netherite?");
        say(player, "...Yes. Okay. You sit. I work. This will be best thing I make this week.");
        giveItemById(player, "minecraft:netherite_sword", 1);
        say(player, "There. You see? Good materials, good result. Not my skill alone — your materials, my hands. We both did this. You understand?");
        return;
    }

    // Tier 2 — Diamond
    if (hasItem(player, "minecraft:diamond", 2)
            && hasItem(player, "minecraft:iron_ingot", 4)
            && hasItem(player, "minecraft:blaze_rod", 1)) {
        consumeItem(player, "minecraft:diamond", 2);
        consumeItem(player, "minecraft:iron_ingot", 4);
        consumeItem(player, "minecraft:blaze_rod", 1);
        progress.forgeCount++;
        addTrust(progress, 8);
        say(player, "Diamond! Good, good. I can work with diamond. Okay. Come back in — [click-hiss] — small time. I make you something worth carrying.");
        giveItemById(player, "minecraft:diamond_sword", 1);
        say(player, "Good sword. Not the best I ever make — I save that for netherite — but good. Don't drop it in lava. I will know. I always know.");
        return;
    }

    // Tier 1 — Iron
    if (hasItem(player, "minecraft:iron_ingot", 6)
            && hasItem(player, "minecraft:flint", 1)) {
        consumeItem(player, "minecraft:iron_ingot", 6);
        consumeItem(player, "minecraft:flint", 1);
        progress.forgeCount++;
        addTrust(progress, 3);
        say(player, "Oh... [click-hiss] ...these are... okay materials. Not bad. Not good. Okay.");
        say(player, "I make something. It will work. It won't be beautiful. Not my fault — your fault. Only good materials make good things.");
        giveItemById(player, "minecraft:iron_sword", 1);
        say(player, "See? It works. I told you — it works but is not beautiful. Next time, bring better. I show you what I can really do.");
        return;
    }

    // Nothing matching
    say(player, "You brought — [click-hiss] — no. No, this is not right.");
    say(player, "You need iron ingots, diamond, or netherite. And blaze rods — they help with heat process. Go find better, come back. I wait. I am always here.");
}
```

---

### MATERIALS TIP — materialsTip(Player player)

Trust +2.

```java
say(player, "Good materials? Okay. Listen.");
say(player, "Iron is fine. Basic. I can work with iron — everyone can work with iron. Diamond is good. I like diamond. Holds edge longer.");
say(player, "Netherite — [click-hiss, pause] — if you find netherite, bring all of it. I make you something you remember.");
say(player, "Blaze rods help with forge temperature. Always bring. Better heat, better bond. Simple science.");
```

---

### ABOUT HIMSELF — aboutHimself(Player player)

Trust +1.

```java
if (progress.trust >= 80) {
    say(player, "My home island... I miss it sometimes. The sounds.");
    say(player, "You would not understand the sounds — human throat cannot make them. But I teach you to listen. That is different thing. Listening is possible for anyone.");
} else if (progress.trust >= 55) {
    say(player, "In Pogon, we say only do what you want. Not lazy — honest. I want to make things. So I make things. Very simple life.");
} else if (progress.trust >= 35) {
    say(player, "I came here because — [click-hiss] — I was bored. Then I was not bored. Then I stayed. This is how Pogon decide things. Simple.");
} else {
    say(player, "I am Pogon. We are small, we make good things. That is all you need to know.");
}
```

---

### ABOUT OTHERS — aboutOthers(Player player)

Trust +1. Single `randomOf` pool:

```java
say(player, randomOf(
    "Mortimer is good man. Comes in sometimes, asks about armor. I give fair price. He pays fair price. Good relationship.",
    "Geera! Yes. She bring me good business. Also good conversation. Pogon like when humans are direct. Geera is direct.",
    "Agatha! [click-hiss — warm sound] She bring me tea. Her cats come also. One gray one — Smudge — he sleep on my best anvil once. I let him.",
    "Joelle make food that is also art. I understand this. My swords are also art. We have good understanding, me and Joelle.",
    "I don't get sick — Pogon immune system is [click-hiss] very robust. But I visit Tarn anyway. She is interesting. Very perceptive.",
    "Cade is good fighter. He know how to use good blade — this is rare. Most people carry weapon and don't understand it. Cade understands."
));
```

---

### SAY / SAY NEARBY

```java
private void say(Player player, String text) {
    this.setCustomName(Component.literal("§6Zii-ko§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    player.displayClientMessage(Component.literal("§6Zii-ko§7: " + text), false);
}

private void sayNearby(String text) {
    this.setCustomName(Component.literal("§6Zii-ko§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    Component chat = Component.literal("§6Zii-ko§7: " + text);
    for (Player p : this.level().players()) {
        if (p.distanceTo(this) <= 12.0F) p.sendSystemMessage(chat);
    }
}
```

---

### AMBIENT LINES — AI STEP WITH SPECIAL NIGHT/CROSS-NPC CHECK

The ambient system has a special case: if it is nighttime AND the nearby player has both `"ciskspawn_has_agatha_trust"` AND `"ciskspawn_ziiko_trust_35"` in their persistent data (both booleans, set by their respective entities when trust hits 35), fire the Smudge line instead of a random ambient.

```java
@Override
public void aiStep() {
    super.aiStep();
    if (this.level().isClientSide) return;

    if (speechTicksLeft > 0) {
        speechTicksLeft--;
        if (speechTicksLeft == 0) {
            this.setCustomNameVisible(false);
            this.setCustomName(Component.literal("Zii-ko - Smith"));
        }
    }

    if (ambientCooldown > 0) { ambientCooldown--; return; }

    Player nearby = this.level().getNearestPlayer(this, 8.0D);
    if (nearby != null) {
        boolean isNight = !this.level().isDay();
        boolean knowsAgatha = nearby.getPersistentData().getBoolean("ciskspawn_has_agatha_trust");
        boolean knowsZiiko = nearby.getPersistentData().getBoolean("ciskspawn_ziiko_trust_35");

        if (isNight && knowsAgatha && knowsZiiko && this.random.nextFloat() < 0.4F) {
            sayNearby(pickSmudgeLine());
        } else {
            sayNearby(pickAmbientLine());
        }
    }

    ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
}

private String pickSmudgeLine() {
    return randomOf(
        "Smudge is back. He found the anvil again. I put blanket for him. [click-hiss] Don't tell Agatha. She worry.",
        "Gray cat is sleeping on anvil. This is third night this week. I named that spot his now. Official.",
        "Shh. Smudge is here. He always come at night. I think he like the warmth from forge. Smart cat."
    );
}

private String pickAmbientLine() {
    return randomOf(
        "[click-hiss] ...almost. Almost right...",
        "If you hear good clang, that is good sign. Bad clang means I am learning something.",
        "You know what I like about this island? Nobody bother me unless they want something. Very efficient.",
        "Oi! Don't touch that one. That one is cooling. Touch it after.",
        "Good day. Good materials today. Good day."
    );
}
```

**Also:** In the `openDialogue()` or `addTrust()` method, when trust reaches 35 for the first time, set the persistent flag:
```java
if (progress.trust >= 35 && !player.getPersistentData().getBoolean("ciskspawn_ziiko_trust_35")) {
    player.getPersistentData().putBoolean("ciskspawn_ziiko_trust_35", true);
}
```

---

### NBT SAVE / LOAD

Tag: `"ZiikoProgress"` (ListTag). Per-player CompoundTag stores: `"Player"` (UUID), `"Trust"` (int), `"HasMet"` (boolean), `"ForgeCount"` (int). Also save `"AmbientCooldown"` (int) on the entity tag directly.

---

### WHAT TO GENERATE

Complete `ZiikoEntity.java` with all imports, all methods above, GeckoLib boilerplate, item helpers (hasItem, countItem, consumeItem, giveItemById, stackMatchesId), randomOf, getProgress, getTrustForPlayer, addTrust, addAdditionalSaveData, readAdditionalSaveData, PlayerProgress inner class.

Do NOT generate ModEntities registration, ModPayloads changes, model, or renderer.
