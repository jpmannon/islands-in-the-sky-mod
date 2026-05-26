# ChatGPT Build Prompt — TarnEntity.java

Paste this prompt to ChatGPT. It contains everything needed to build TarnEntity.java from scratch.

---

## PROMPT

I am building a NeoForge 1.21.1 Minecraft mod (mod_id: `ciskspawn`, package: `com.quickqwek.ciskspawn`). I need you to write `TarnEntity.java` — a full, compilable entity class using the existing patterns from my mod. Here is everything you need.

---

### MOD CONTEXT

- NeoForge 1.21.1, Java 21, GeckoLib 4.7.1
- Package: `com.quickqwek.ciskspawn.entity`
- Network: uses `MortimerDialoguePayload` (server→client) to open the GUI
- All NPCs extend `PathfinderMob implements GeoEntity`
- All NPCs use an inner `PlayerProgress` class and `Map<UUID, PlayerProgress> progressByPlayer`
- `PlayerStatsTracker` is a static utility in `com.quickqwek.ciskspawn` (not needed for Tarn)
- `setPersistenceRequired()` is called in every constructor

---

### ENTITY REGISTRATION (already exists — do NOT generate this)

Tarn is already registered in `ModEntities.java` as `TARN`. You will reference it as:
```java
import com.quickqwek.ciskspawn.entity.ModEntities; // for context only
```
Her model/renderer are separate files — do not generate those. Just the entity.

---

### PLAYER PROGRESS FIELDS

```java
private static class PlayerProgress {
    int trust = 0;
    boolean hasMet = false;
    int questStage = 0;          // 0 = not started, 1 = soul anchor done, 2 = stronger done
    boolean strongerApplied = false;
}
```

Trust cap: 100. `addTrust(progress, amount)` uses `Math.min(100, progress.trust + amount)`.

---

### TRUST TIERS

```
0–14   Stranger
15–34  Acquaintance
35–54  Friend
55–79  Trusted
80+    Crew
```

---

### ATTRIBUTES

```java
public static AttributeSupplier.Builder createAttributes() {
    return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 26.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.16D)
            .add(Attributes.FOLLOW_RANGE, 18.0D);
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

### DIALOGUE GUI PAYLOAD

Send this to open the GUI on the client side:

```java
PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
    this.getId(),       // entity ID
    "Tarn - Healer",   // NPC name/title
    body,              // main dialogue text (String)
    "Healing work",    // button 1 label
    "Ask about herself", // button 2 label
    footerHint         // small hint text shown at bottom
));
```

---

### ACTION HANDLER

```java
public void handleTarnAction(Player player, String action) {
    switch (action) {
        case "tarn_quest"    -> handleQuest(player);
        case "tarn_herself"  -> aboutHerself(player);
        case "tarn_tip"      -> medicineTip(player);
        case "tarn_crew"     -> aboutCrew(player);
        default              -> openDialogue(player);
    }
}
```

Action strings are sent from the client via `MortimerActionPayload` and routed in `ModPayloads`. You do not need to modify `ModPayloads` — just write the handler method above.

---

### FIRST MEETING

`hasMet` starts false. On first `openDialogue()` call:
- Set `hasMet = true`
- Add trust +1
- Send this specific payload:

```
Title: "Tarn - Healer"
Body:  "Sit down. I'll take a look before you tell me what's wrong — you always give it 
        away before you open your mouth anyway.\n\nTarn. Ship's physician. Don't be 
        alarmed by the herbs. They smell strange but they work."
Btn1:  "Healing work"
Btn2:  "Ask about herself"
Footer: "She looked you over before you finished walking in."
```

---

### GREETINGS — pickTarnGreeting(PlayerProgress progress)

Use `randomOf(String... lines)` backed by `this.random.nextInt(lines.length)`.

```java
private String pickTarnGreeting(PlayerProgress progress) {
    if (progress.trust >= 80) return randomOf(
        "You haven't treated me differently. I noticed.",
        "Tarn still fits. I wanted you to know that — choosing to keep it means something.",
        "Most people either panic or don't believe me. You just nodded. That was the right answer."
    );
    if (progress.trust >= 55) return randomOf(
        "There are things I don't share with most people. You're getting close to being someone I'd share them with.",
        "I have a very good memory. Better than it should be, probably. I've stopped apologizing for it.",
        "The face you're making — that's the one people make when they're about to ask me something I'll deflect. Go ahead."
    );
    if (progress.trust >= 35) return randomOf(
        "I've been many things in my life. Some of them had better handwriting than I do now.",
        "Tarn is the name I've used longest. It fits well. Most names don't.",
        "Come in. I want to show you something I've been growing. It shouldn't work here, but I encouraged it."
    );
    if (progress.trust >= 15) return randomOf(
        "Back again. Last time it was the ankle. What is it now.",
        "You look better than when I first saw you. That's either my work or your stubbornness.",
        "I've been thinking about something you said. You probably don't remember saying it."
    );
    return randomOf(
        "Sit down. I'll take a look before you tell me what's wrong.",
        "You're moving like something hurts. Shoulder or ribs — I'll find out.",
        "New face. Good. I like to know everyone before they need me urgently."
    );
}
```

---

### ABOUT HERSELF — aboutHerself(Player player)

Trust +1 each call. The reveal at trust ≥ 80 fires once and sets a persistent player NBT flag.

```java
private void aboutHerself(Player player) {
    PlayerProgress progress = getProgress(player);
    addTrust(progress, 1);

    if (progress.trust >= 80 && !player.getPersistentData().getBoolean("ciskspawn_tarn_revealed")) {
        player.getPersistentData().putBoolean("ciskspawn_tarn_revealed", true);
        say(player, "I have not always been Tarn.");
        say(player, "Tarn is the shape I decided to keep. I've held others — some longer than this one, some for an afternoon. This one stuck.");
        say(player, "I don't know if that makes me a person who became a healer or a healer who became a person. Either way, I'm here. I'm not going anywhere.");
        return;
    }
    if (progress.trust >= 55) {
        say(player, randomOf(
            "If I told you the whole thing, you'd either not believe me or you'd have questions I don't feel like answering yet. Ask me again later.",
            "There are things I don't share with people who haven't earned the right to know them.",
            "I have a very good memory. Better than it should be. I've stopped wondering why."
        ));
    } else if (progress.trust >= 35) {
        say(player, randomOf(
            "I'm not quite what I look like. Most people aren't, in some way or another. My version is more literal than most.",
            "I've been doing this longer than most people do anything. Don't read into that.",
            "Tarn is the name I've used longest. It fits. Most names don't."
        ));
    } else if (progress.trust >= 15) {
        say(player, randomOf(
            "I pick things up quickly. Always have. I couldn't tell you why — I stopped wondering about it.",
            "I've been doing this long enough. That's the short answer.",
            "Observant. That's the word people use. It's close enough."
        ));
    } else {
        say(player, "I've been doing this long enough. That's the short answer.");
    }
}
```

---

### QUEST 1 — Soul Anchor Charm

**Items required:** 4 string, 2 echo shards, 1 amethyst shard

Quest stage 0 — prompt:
```
"You've got the look. The 'I dropped everything' look. I can fix that.
Bring me: 4 string, 2 echo shards, 1 amethyst shard. I'll make you something that holds."
```

On delivery:
- Consume all items
- `questStage = 1`
- Trust +8
- `player.getPersistentData().putBoolean("ciskspawn_has_soul_anchor", true)`
- Give item: `ciskspawn:soul_anchor` — if not registered, fall back to giving `minecraft:amethyst_shard` as placeholder
- Say:
```
"There. Soul Anchor. Keep it in your inventory — don't vault it, don't drop it, 
don't hand it to someone else for safekeeping. It only works for the person it was made for."

"Don't ask me how I know so much about anchoring things. I have personal reasons."
```

**Soul Anchor keep-inventory hook (add as a separate `@SubscribeEvent` in a handler class, or directly in TarnEntity using `IForgeEntityDataSerializer` — note below):**

Because the event must be on the bus, write this note in a comment rather than inside the entity class:
```java
// NOTE: Register a LivingDropsEvent listener in your EventBusSubscriber class:
// If the dying entity is a Player AND the player has "ciskspawn_has_soul_anchor" == true
// in getPersistentData(), call event.setCanceled(true) to suppress all drops.
// The soul_anchor item itself is already in the inventory and will be retained.
```

---

### QUEST 2 — Stronger

**Unlock requirement:** `questStage >= 1` AND `progress.trust >= 35`

**Items required:** 1 golden apple, 2 ghast tears, 3 blaze powder

Quest stage 1 — prompt:
```
"There's something I can do for people I trust with the knowledge. It's not magic — 
it's closer to medicine, but not the kind most people practice. It won't hurt. Much.
Bring me: 1 golden apple, 2 ghast tears, 3 blaze powder."
```

On delivery:
- Consume all items
- Apply the AttributeModifier — guard against double-apply:

```java
ResourceLocation STRONGER_ID = ResourceLocation.fromNamespaceAndPath("ciskspawn", "tarn_stronger");
var attr = player.getAttribute(Attributes.MAX_HEALTH);
if (attr != null) {
    boolean alreadyApplied = attr.getModifiers(AttributeModifier.Operation.ADD_VALUE)
        .stream().anyMatch(m -> m.id().equals(STRONGER_ID));
    if (!alreadyApplied) {
        attr.addPermanentModifier(new AttributeModifier(STRONGER_ID, 4.0, AttributeModifier.Operation.ADD_VALUE));
        player.setHealth(player.getMaxHealth());
    }
}
```

- `questStage = 2`, `strongerApplied = true`, trust +10
- Say:
```
"Hold still. This takes about thirty seconds and you're going to feel it settle. 
That's normal. Don't panic about the warmth — that's just your body deciding 
what to do with the extra room."

"There. Two extra hearts, give or take. You're welcome. Don't waste them on 
something stupid within the first hour — I say this from experience watching people."
```

Stage 2 (complete) — if player revisits quest option:
```
"You're welcome. Don't ask for more — that's the limit of what I can safely do 
without understanding your full history. You don't want me guessing."
```

---

### MEDICINE TIPS — medicineTip(Player player)

Trust +2.

```java
say(player, randomOf(
    "Hunger is the most underrated wound. Fix it first.",
    "If it's swollen and it shouldn't be, the answer is almost always time and elevation.",
    "Potions are quick. Quick isn't always good. Know the difference.",
    "I have never lost a patient to something I caught early enough. I've lost a few to overconfidence. Theirs, not mine.",
    "Pain is information. Stopping pain without knowing what caused it is just reading half the message."
));
```

---

### ABOUT CREW — aboutCrew(Player player)

Trust +1. Use a sub-switch on `progress.trust` or keep flat:

```java
say(player, randomOf(
    "Mortimer's been kind to me. People like that — you want to protect them without telling them you're doing it.",
    "Geera notices things almost as quickly as I do. We've never discussed what that means about either of us.",
    "Azerion finds things preferable. The list of entities with preferences is shorter than people think.",
    "Velho spent nineteen years watching what he made become something he didn't design. I think he finds it wonderful. He'd never say that.",
    "Scoria asks good questions. She doesn't always like the answers, but she asks anyway. That's worth something.",
    "Joelle feeds people and calls it cooking. Ramone grows things and calls it gardening. Both of them are doing something else entirely."
));
```

---

### AMBIENT LINES — pickAmbientLine()

```java
private String pickAmbientLine() {
    return randomOf(
        "The herbs are doing something they weren't doing yesterday. I'm watching it.",
        "If you've been putting off something that hurts, come back later. I'll be here.",
        "I have a good memory for injuries. Don't take that as a threat.",
        "Something on the eastern approach smells like rain coming. Prepare accordingly.",
        "I've been in worse places. This one has good light."
    );
}
```

Ambient cooldown: `20 * 154` ticks baseline, jitter by up to `20 * 242`.

---

### SAY / SAY NEARBY

```java
private void say(Player player, String text) {
    this.setCustomName(Component.literal("§bTarn§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    player.displayClientMessage(Component.literal("§bTarn§7: " + text), false);
}

private void sayNearby(String text) {
    this.setCustomName(Component.literal("§bTarn§f: " + text));
    this.setCustomNameVisible(true);
    this.speechTicksLeft = 20 * 7;
    Component chat = Component.literal("§bTarn§7: " + text);
    for (Player player : this.level().players()) {
        if (player.distanceTo(this) <= 12.0F) player.sendSystemMessage(chat);
    }
}
```

---

### NBT SAVE / LOAD

Tag name: `"TarnProgress"` (ListTag of CompoundTags)

Each entry stores:
- `"Player"` (UUID)
- `"Trust"` (int)
- `"HasMet"` (boolean)
- `"QuestStage"` (int)
- `"StrongerApplied"` (boolean)

Also save/load `"AmbientCooldown"` (int) on the entity tag directly.

---

### WHAT TO GENERATE

Please write the complete, compilable `TarnEntity.java` file with:

1. All imports
2. Class declaration extending `PathfinderMob implements GeoEntity`
3. GeckoLib boilerplate (IDLE_ANIM, WALK_ANIM, cache, `registerControllers`, `predicate`, `getAnimatableInstanceCache`)
4. `speechTicksLeft` and `ambientCooldown` fields
5. Constructor with `setPersistenceRequired()`
6. `createAttributes()` static method
7. `removeWhenFarAway()` returning false
8. `registerGoals()`
9. `mobInteract()` routing to `openDialogue()`
10. `openDialogue()` — first meeting beat + normal routing
11. `handleTarnAction()` switch
12. `pickTarnGreeting()`
13. `aboutHerself()` — with changeling reveal
14. `handleQuest()` — routing by questStage
15. Quest 1: `soulAnchorQuest()` — item check/consume/give/advance
16. Quest 2: `strongerQuest()` — item check/consume/attribute apply/advance
17. `medicineTip()`
18. `aboutCrew()`
19. Item helper methods: `hasItem()`, `countItem()`, `consumeItem()`, `giveItemById()`, `stackMatchesId()`
20. `say()`, `sayNearby()`
21. `aiStep()` — speech ticks + ambient
22. `pickAmbientLine()`
23. `getProgress()`, `getTrustForPlayer()`, `addTrust()`, `randomOf()`
24. `addAdditionalSaveData()` and `readAdditionalSaveData()`
25. `PlayerProgress` inner class

Do not generate ModEntities registration, the model class, the renderer class, or ModPayloads changes. Just TarnEntity.java.
