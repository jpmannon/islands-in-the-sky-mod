# ChatGPT Build Prompt — AgathaEntity.java

---

## PROMPT

I am building a NeoForge 1.21.1 Minecraft mod (`ciskspawn`, package `com.quickqwek.ciskspawn`). Write `AgathaEntity.java` — a complete, compilable entity class. Follow the exact patterns from this mod.

---

### CHARACTER OVERVIEW

Agatha is an older human woman who runs a brewing/herbalist shop in Aster, the regional capital city. She is a warm, sassy, eccentric gossip who is generous to a fault. She has three cats: **Ink** (black), **Biscuit** (white), and **Smudge** (gray). She cannot stop talking about them. She is good friends with Geera. She gives fetch quests and rewards them exceptionally well.

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
    int questStage = 0;    // 0=none started, 1=quest1 done, 2=quest2 done, 3=quest3 done
}
```

Trust cap 100.

---

### ATTRIBUTES

```java
public static AttributeSupplier.Builder createAttributes() {
    return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 24.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.16D)
            .add(Attributes.FOLLOW_RANGE, 16.0D);
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
    "Agatha - Brewer",
    body,
    "Ask a favour",
    "Ask about the cats",
    footer
));
```

---

### ACTION HANDLER

```java
public void handleAgathaAction(Player player, String action) {
    switch (action) {
        case "agatha_quest"  -> handleQuest(player);
        case "agatha_cats"   -> aboutCats(player);
        case "agatha_aster"  -> aboutAster(player);
        case "agatha_crew"   -> aboutCrew(player);
        default              -> openDialogue(player);
    }
}
```

---

### FIRST MEETING

`hasMet = true`, trust +1. Set persistent flag `"ciskspawn_agatha_visited" = true` on player. Send:

```
Title:  "Agatha - Brewer"
Body:   "Oh! Don't mind Smudge — yes, he is just going to sit there, that's fine, he owns 
         this doorway apparently — come in, come in!\n\nI'm Agatha. Brewer, herbalist, and 
         keeper of these three terrible wonderful creatures you'll be tripping over. That's 
         Ink — yes, the judgmental one. Biscuit is somewhere. And you've met Smudge.\n\n
         Now. What can I do for you? And what's your name? I like to know everyone."
Btn1:   "Ask a favour"
Btn2:   "Ask about the cats"
Footer: "She was already talking before you finished walking in."
```

Also set: `player.getPersistentData().putBoolean("ciskspawn_agatha_visited", true)`

---

### GREETINGS — pickAgathaGreeting(PlayerProgress progress)

```java
private String pickAgathaGreeting(PlayerProgress progress) {
    if (progress.trust >= 80) return randomOf(
        "Oh wonderful, you're here — I was just telling Ink about you. He pretended not to care but he was listening.",
        "I made something for you. It's not a quest, it's a gift. You've more than earned it.",
        "You know you're one of my favorite people, don't you. Don't let it go to your head. Sit down."
    );
    if (progress.trust >= 55) return randomOf(
        "You know, I was thinking — you're one of the good ones. I have a sense for it. Fifty years of talking to people, you develop a sense.",
        "Geera asked about you again. I told her you were doing well. I hope that was true?",
        "Sit down, love. Biscuit, come here and be sweet to our friend. There. That's better."
    );
    if (progress.trust >= 35) return randomOf(
        "There you are! Ink! Look who's here — yes, go say hello, don't be shy — sorry, he's been a bit particular lately.",
        "I have news. Sit down, let me make you something, and I'll tell you what I heard about the harbormaster. You'll love it.",
        "Smudge slept on the good mortar again. I'm not even upset. Look at his little face. I simply cannot be upset."
    );
    if (progress.trust >= 15) return randomOf(
        "Back again! Geera mentioned you, you know. Said you were interesting. She doesn't say that about many.",
        "Oh good, you're here — Ink has been in a mood and I need someone to tell me I'm not imagining it. Look at that face.",
        "I was just thinking about you! I had something I wanted to ask. Also I made a new blend this morning — you should try it."
    );
    return randomOf(
        "Oh! A new one! Come in, come in — Biscuit, off the counter, we have company.",
        "Hello, hello! I don't think I've seen you before. I'd remember. I remember everyone.",
        "Welcome! Mind Smudge — he stretches out in the doorway and takes absolutely no responsibility for it."
    );
}
```

---

### QUEST CHAIN — handleQuest(Player player)

**Stage 0 — Quest 1: Something for Ink**

Items needed: sweet berries ×5, dried kelp ×2, lily pad ×1.

If player has all items:
- Consume all
- `questStage = 1`, trust +6
- Give: 3× Potion of Swiftness (use `minecraft:potion` via giveItemById, or give `minecraft:sugar` ×4 as fallback if potions can't be given directly)
- Give: 2× Potion of Regeneration (or `minecraft:glistering_melon_slice` ×2 as fallback)
- Say:
```
"Oh you absolute star. He's going to be thrilled. Or he's going to look at it and walk away 
and I'll eat it myself, but either way — here, I made you something while I was thinking about it."
```

If missing items:
```
"Oh, I've been meaning to ask someone! Ink has been restless lately — I think the air up here 
isn't quite right for him. I need: five sweet berries, two dried kelp, and a lily pad. Not 
medicinal, just — I want to make him a little treat. He deserves one. He's been so patient with me."
```

**Note on potions:** In NeoForge 1.21.1, to give a properly typed potion:
```java
ItemStack potion = new ItemStack(Items.POTION);
potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.SWIFTNESS));
if (!player.getInventory().add(potion)) player.drop(potion, false);
```
Use this pattern for all potion rewards below. If `DataComponents` or `PotionContents` cause compile issues, fall back to giving the base ingredient items instead (noted per quest).

---

**Stage 1 — Quest 2: For the Shelf**

Items needed: blaze powder ×4, nether wart ×3, ghast tear ×1.

If player has all items:
- Consume all
- `questStage = 2`, trust +8
- Give: 2× Potion of Strength II (`Potions.STRENGTH` with `PotionContents`)
- Give: 1× Potion of Fire Resistance (`Potions.FIRE_RESISTANCE`)
- Give: 1× Potion of Invisibility (`Potions.INVISIBILITY`)
- Say:
```
"Oh perfect, yes, exactly right — Biscuit! Get off the ingredient jars, we've had this 
conversation — sorry, where was I. Yes. Perfect. I'm going to make you something that takes 
a week to make and you're going to use it at exactly the right moment."
```

If missing items:
```
"I'm low on something and it's driving me absolutely mad. I need four blaze powder, three 
nether wart, and a ghast tear if you can manage it. The things I can make with a ghast tear, 
love — you have no idea."
```

If trust < 15:
```
"Oh, I couldn't ask that of someone I've only just met! Come back when we know each other 
a little better."
```

---

**Stage 2 — Quest 3: The Good Batch**

Items needed: dragon's breath ×1.

If player has it:
- Consume 1 dragon's breath
- `questStage = 3`, trust +12
- Give: 3× Lingering Potion of Healing II (`Potions.STRONG_HEALING`, `Items.LINGERING_POTION`)
- Give: 1× Splash Potion of Harming (`Potions.HARMING`, `Items.SPLASH_POTION`)
- Say:
```
"Oh. Oh yes. Yes, this is exactly it. Smudge, come look — don't actually come look, you'll 
knock it over — sit."
(pause)
"...There. That's for you. I've been saving that recipe for someone who'd actually use it."
```

If missing:
```
"I need one more thing and then I can finish what I've been working on for months. One dragon's 
breath. Don't ask me how you get it — you'll figure it out — just bring it here when you have 
it and I will do something wonderful."
```

If trust < 35:
```
"Oh, that one I save for people I know well. Come back when we've had a few more conversations, love."
```

**Stage 3 (complete):**
```
"You've been wonderful. Truly. Now sit down, Ink wants attention and I want to tell you 
about what the harbormaster said last week."
```

---

### ABOUT CATS — aboutCats(Player player)

Trust +2. Always available.

```java
say(player, randomOf(
    "Ink is the oldest — seven years, very dignified, deeply offended by everything. I love him.",
    "Biscuit came to me in the rain three winters ago and has never left. Excellent decision on her part.",
    "Smudge found his way in through a window I didn't know was open. He's been here since. I've stopped questioning it.",
    "Ink knocked over my best flask last week and looked me directly in the eyes while he did it. No remorse.",
    "Biscuit and Smudge have reached an agreement about the south windowsill. I wasn't consulted.",
    "Ink is a very serious cat. He watches me work. Sometimes I think he's judging my methods. He's probably right.",
    "You know, Smudge goes off some nights and I never know where. He always comes back smelling of woodsmoke. I've decided not to investigate."
));
```

---

### ABOUT ASTER — aboutAster(Player player)

Trust +1. Trust-gated gossip.

```java
if (progress.trust >= 55) {
    say(player, randomOf(
        "Alright, I'll tell you what I actually know — and what I suspect, which is different but usually more interesting.",
        "You didn't hear this from me. You heard this from someone who knows someone who told me. That's different. That's just being informed."
    ));
} else if (progress.trust >= 35) {
    say(player, randomOf(
        "Geera tells me there's been unusual traffic through the lower docks lately. She doesn't say what she thinks it means. I have ideas.",
        "There's a family on the east side of the city that's been here since the founding. They don't talk about how they got their first island. Nobody asks."
    ));
} else if (progress.trust >= 15) {
    say(player, "I'll tell you this much — the harbormaster and the city council have been having a disagreement for two years that neither of them will admit is personal. I have theories.");
} else {
    say(player, "Oh, Aster is wonderful. Old city, lots of history, everyone knows everyone — which is either charming or exhausting depending on the day.");
}
```

---

### ABOUT CREW — aboutCrew(Player player)

Trust +1.

```java
say(player, randomOf(
    "Geera is my oldest friend and my worst influence — she'll tell you the same about me. Don't believe her, she started it.",
    "Mortimer came through Aster years ago before he was the Mortimer, you know. I remember when he was still figuring things out.",
    "Oh I adore Scoria. Very intense. I told her once that her father worries and she made a face like I'd told her the sky was blue. She knows.",
    "Tarn noticed Smudge has a trick knee before I did. I didn't ask how she knew.",
    "Velho brought me something once that I still haven't quite identified. It's on the high shelf.",
    "I bring Zii-ko tea. He lets my cats in the shop. Best relationship I have with anyone in the city.",
    "I don't know much about Cade. He doesn't tell me much about him. That's suspicious. I'm watching."
));
```

---

### SAY / SAY NEARBY

```java
private void say(Player player, String text) {
    this.setCustomName(Component.literal("§dAgatha§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    player.displayClientMessage(Component.literal("§dAgatha§7: " + text), false);
}

private void sayNearby(String text) {
    this.setCustomName(Component.literal("§dAgatha§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    Component chat = Component.literal("§dAgatha§7: " + text);
    for (Player p : this.level().players()) {
        if (p.distanceTo(this) <= 12.0F) p.sendSystemMessage(chat);
    }
}
```

---

### AMBIENT LINES — pickAmbientLine()

```java
private String pickAmbientLine() {
    return randomOf(
        "Biscuit! That is a potion ingredient, not a toy. Biscuit.",
        "Mm. This one needs another hour. I can feel it.",
        "Did I tell you what Ink did this morning? Of course I did. He did it again.",
        "Come in if you're coming in — the draft is bad for the fermentation.",
        "I love this city. Everyone's got a story. Nobody tells the whole one."
    );
}
```

---

### AI STEP

Standard pattern. Ambient cooldown 20*154 ticks + random up to 20*242.  
When trust hits 35 for the first time (check after every `addTrust` call), set:
```java
player.getPersistentData().putBoolean("ciskspawn_has_agatha_trust", true);
```
This flag is read by ZiikoEntity to unlock the Smudge-on-the-anvil ambient lines at night.

---

### NBT SAVE / LOAD

Tag: `"AgathaProgress"` (ListTag). Per-player CompoundTag stores: `"Player"` (UUID), `"Trust"` (int), `"HasMet"` (boolean), `"QuestStage"` (int). Also save `"AmbientCooldown"` (int) on entity tag.

---

### WHAT TO GENERATE

Complete `AgathaEntity.java` with all imports, all methods above, GeckoLib boilerplate (IDLE_ANIM, WALK_ANIM, cache, registerControllers, predicate, getAnimatableInstanceCache), item helpers (hasItem, countItem, consumeItem, giveItemById, stackMatchesId), randomOf, getProgress, getTrustForPlayer, addTrust, addAdditionalSaveData, readAdditionalSaveData, PlayerProgress inner class.

Do NOT generate ModEntities registration, ModPayloads changes, model, or renderer.
