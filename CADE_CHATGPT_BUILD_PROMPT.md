# ChatGPT Build Prompt — CadeEntity.java

---

## PROMPT

I am building a NeoForge 1.21.1 Minecraft mod (`ciskspawn`, package `com.quickqwek.ciskspawn`). Write `CadeEntity.java` — a complete, compilable entity class. Follow the exact patterns from this mod.

---

### MOD CONTEXT

- NeoForge 1.21.1, Java 21, GeckoLib 4.7.1
- Package: `com.quickqwek.ciskspawn.entity`
- All NPCs extend `PathfinderMob implements GeoEntity`
- Network: `MortimerDialoguePayload` (server→client), `MortimerActionPayload` (client→server)
- `PacketDistributor.sendToPlayer(serverPlayer, payload)` sends the GUI open packet
- `setPersistenceRequired()` in every constructor
- `removeWhenFarAway()` returns false

---

### PLAYER PROGRESS

```java
private static class PlayerProgress {
    int trust = 0;
    boolean hasMet = false;
    int questStage = 0;      // 0=not started, 1=iron done, 2=blaze done, 3=skull done (complete)
    boolean trainingApplied = false;  // guard for AttributeModifier
}
```

Trust cap 100. `addTrust(progress, n)` uses `Math.min(100, progress.trust + n)`.

---

### ATTRIBUTES

```java
public static AttributeSupplier.Builder createAttributes() {
    return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 30.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.19D)
            .add(Attributes.FOLLOW_RANGE, 20.0D);
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
    "Cade - Security",
    body,
    "Combat training",
    "Combat tip",
    footer
));
```

---

### ACTION HANDLER

```java
public void handleCadeAction(Player player, String action) {
    switch (action) {
        case "cade_quest"   -> handleQuest(player);
        case "cade_tip"     -> combatTip(player);
        case "cade_himself" -> aboutHimself(player);
        case "cade_crew"    -> aboutCrew(player);
        default             -> openDialogue(player);
    }
}
```

---

### FIRST MEETING

On first `openDialogue()`, `hasMet = true`, trust +1. Send this payload:

```
Title:  "Cade - Security"
Body:   "Cade. I handle security here. You're new, so I'll say this once: don't go past 
         the east ridge without telling someone. The footing's bad and the wind comes 
         from the wrong direction.\n\nEverything else you'll figure out. Ask if you don't."
Btn1:   "Combat training"
Btn2:   "Combat tip"
Footer: "He registered you before you finished walking in."
```

---

### GREETINGS — pickCadeGreeting(PlayerProgress progress)

```java
private String pickCadeGreeting(PlayerProgress progress) {
    if (progress.trust >= 80) return randomOf(
        "If something goes wrong out there, you're who I'd want to know about it first. Just so you know where you stand.",
        "I had a partner in the navy. Good fighter. Better instincts. You remind me of her sometimes. That's a compliment.",
        "Still here. Good."
    );
    if (progress.trust >= 55) return randomOf(
        "Tell me something. The last island you came from — anyone follow you?",
        "I've been thinking about the west perimeter. Walk it with me if you have time.",
        "I've worked with worse crews. I've worked with better. This one's worth the work. Thought you should know that."
    );
    if (progress.trust >= 35) return randomOf(
        "Tarn says you've been keeping your health up. Good habit.",
        "Velho asked me something strange last week. I've been thinking about it since.",
        "You're consistent. I respect that more than most things."
    );
    if (progress.trust >= 15) return randomOf(
        "Back again. You didn't break anything last time. That's something.",
        "You move like you've had training. Who.",
        "The south approach had a problem this morning. Nothing now. Just so you know."
    );
    return randomOf(
        "Stay on the path. The drop-off on the west side isn't obvious until it is.",
        "You're new. Good. Keep that knife where I can see it's yours.",
        "Cade. I handle security. Don't make my job harder than it has to be."
    );
}
```

---

### QUEST — handleQuest(Player player)

Three stages. Each checks trust minimums and item requirements.

**Stage 0 — Iron swords evaluation:**

Trust minimum: none (Cade respects anyone willing to start).

If player has 2 iron swords:
- Consume 2 iron swords
- `questStage = 1`, trust +5
- Give enchanted book: Sharpness I (`minecraft:enchanted_book` — see note below)
- Say:
```
"These are fine. Here's what I'm going to show you. Pay attention. I don't repeat myself."
```

If player does not have 2 iron swords:
```
"You've been in a fight recently. You took more than you should have. 
Bring me two iron swords. I'll test them and I'll test you."
```

**Stage 1 — Blaze rod field test:**

Trust minimum: 15. If below: `"Not yet. Need to know you'll follow through first."`

If player has 1 blaze rod:
- Consume 1 blaze rod
- `questStage = 2`, trust +8
- Give 2 enchanted books: Fire Aspect I and Knockback I (one each, `minecraft:enchanted_book`)
- Say:
```
"Good. You went and came back. Some people don't."
```

If missing:
```
"Blaze rod. If you can reach a fortress, you can handle what's waiting for it. 
Come back when you have one."
```

**Stage 2 — Wither skeleton skull:**

Trust minimum: 35. If below: `"Not yet. This one takes someone I trust."`

If player has 1 wither skeleton skull:
- Consume skull
- Apply `AttributeModifier` — permanent +0.1 attack speed:

```java
ResourceLocation TRAINING_ID = ResourceLocation.fromNamespaceAndPath("ciskspawn", "cade_training");
var attr = player.getAttribute(Attributes.ATTACK_SPEED);
if (attr != null && !progress.trainingApplied) {
    boolean alreadyApplied = attr.getModifiers(AttributeModifier.Operation.ADD_VALUE)
        .stream().anyMatch(m -> m.id().equals(TRAINING_ID));
    if (!alreadyApplied) {
        attr.addPermanentModifier(new AttributeModifier(TRAINING_ID, 0.1, AttributeModifier.Operation.ADD_VALUE));
        progress.trainingApplied = true;
    }
}
```

- `questStage = 3`, trust +10
- Say:
```
"You earned this."
(pause)
"Don't waste it."
```

If missing:
```
"One wither skeleton skull. One is enough. 
Come back when you've been somewhere worth coming back from."
```

**Stage 3 (complete):**
```
"You've had everything I can give you. The rest is practice. You know where I am."
```

**Note on enchanted book giving:** Use `giveItemById` for `minecraft:enchanted_book` as a plain item (no enchant applied via code — this is a reward the player gets to apply themselves with an anvil). Alternatively if you want to give a properly enchanted book, use:
```java
ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
EnchantmentHelper.setEnchantments(book, Map.of(Enchantments.SHARPNESS, 1));
if (!player.getInventory().add(book)) player.drop(book, false);
```
Use whichever compiles cleanly in NeoForge 1.21.1.

---

### COMBAT TIPS — combatTip(Player player)

Trust +2.

```java
say(player, randomOf(
    "Hit first if you're going to hit. Hesitation costs more than initiative.",
    "Armor buys time. Time buys options. Don't confuse either one for safety.",
    "If you don't know what something does, don't stand in front of it.",
    "Retreat is a tactic. Running is a failure. Know the difference before you move.",
    "Fight what's in front of you. Think about what's behind you. Do both."
));
```

---

### ABOUT HIMSELF — aboutHimself(Player player)

Trust +1.

```java
if (progress.trust >= 80) {
    say(player, "My commanding officer gave an order I couldn't follow. I told him I couldn't. He accepted that — which surprised me. He was a good officer. I wasn't a good soldier by the end. Different things.");
} else if (progress.trust >= 55) {
    say(player, "The navy asks you to protect things that don't always deserve protecting. Eventually you run the numbers. I ran them.");
} else if (progress.trust >= 35) {
    say(player, "I served five years. Didn't renew the contract. That's the short version.");
} else {
    say(player, "I've been doing this long enough. That covers most of the questions.");
}
```

---

### ABOUT CREW — aboutCrew(Player player)

Trust +1. Single `randomOf` pool:

```java
say(player, randomOf(
    "Mortimer's honest about what he is. That matters more than most people think.",
    "Geera's sharp. Knows the value of everything she sells and everything she doesn't.",
    "Scoria's got good instincts. Rough edges. She'll be formidable.",
    "Never have to worry about Azerion's loyalty. Can't decide if that's reassuring or unsettling.",
    "Velho's brilliant and difficult. I trust him more than he'd expect.",
    "Joelle feeds people. That's not a small thing.",
    "Ramone's quiet, watches, knows more than he says. Good qualities.",
    "I don't ask questions about Tarn. She doesn't ask questions about me. Works well.",
    "Zii-ko does good work. Honest about what he can and can't do. Respectable.",
    "Agatha knows everything about everyone. I try to stay on her good side."
));
```

---

### SAY / SAY NEARBY

```java
private void say(Player player, String text) {
    this.setCustomName(Component.literal("§cCade§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    player.displayClientMessage(Component.literal("§cCade§7: " + text), false);
}

private void sayNearby(String text) {
    this.setCustomName(Component.literal("§cCade§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    Component chat = Component.literal("§cCade§7: " + text);
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
        "Check your footing near the north edge. Especially at night.",
        "Wind's shifted. Something's coming from the west.",
        "All clear. That's what I want to keep saying.",
        "Seen worse weather. Seen worse company. Currently prefer both.",
        "Still here."
    );
}
```

---

### AI STEP

Standard pattern:
```java
@Override
public void aiStep() {
    super.aiStep();
    if (this.level().isClientSide) return;
    if (speechTicksLeft > 0) {
        speechTicksLeft--;
        if (speechTicksLeft == 0) {
            this.setCustomNameVisible(false);
            this.setCustomName(Component.literal("Cade - Security"));
        }
    }
    if (ambientCooldown > 0) { ambientCooldown--; return; }
    Player nearby = this.level().getNearestPlayer(this, 8.0D);
    if (nearby != null) sayNearby(pickAmbientLine());
    ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
}
```

---

### NBT SAVE / LOAD

Tag: `"CadeProgress"` (ListTag). Per-player CompoundTag stores: `"Player"` (UUID), `"Trust"` (int), `"HasMet"` (boolean), `"QuestStage"` (int), `"TrainingApplied"` (boolean). Also save `"AmbientCooldown"` (int) directly on entity tag.

---

### WHAT TO GENERATE

Complete `CadeEntity.java` with all imports, all methods listed above, GeckoLib boilerplate (IDLE_ANIM, WALK_ANIM, cache, registerControllers, predicate, getAnimatableInstanceCache), item helpers (hasItem, countItem, consumeItem, giveItemById, stackMatchesId), randomOf, getProgress, getTrustForPlayer, addTrust, addAdditionalSaveData, readAdditionalSaveData, PlayerProgress inner class.

Do NOT generate ModEntities registration, ModPayloads changes, model, or renderer. Just CadeEntity.java.
