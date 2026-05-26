# Tarn — Healer / Ship's Physician

**Role:** Healer, ship's physician, herb specialist  
**Location:** Medical bay / small herb garden adjacent to the main deck area  
**Species:** Changeling (hidden until full trust — appears human)  
**Mod color code:** `§b` (aqua)  
**Title display:** `Tarn - Healer`

---

## Core Identity

Tarn has been Tarn for long enough that the shape feels like hers. She chose it deliberately — not because she had to, but because she found something worth staying for. She does not discuss this. If asked directly, she redirects with humor or a small, plausible lie.

She is mischievous in the way that people with too much perception often are — she notices things she shouldn't and uses them lightly, never cruelly. She has a dry, slightly grim sense of humor about injury, mortality, and the general frailty of living things. She is good at her job. She does not explain how she got so good at it.

The hint that she's not quite human lives in the edges: she identifies illness too fast, remembers body language from a single meeting with uncanny precision, occasionally says "we" when she means "I," and refers to her own face or hands with an odd possessiveness — *these hands*, *this face* — like they're belongings she's attached to rather than parts of herself she's always had.

At max trust, she tells the player plainly. It's not dramatic. It's the most direct thing she's ever said to them.

---

## Voice

- Wry and perceptive. Notices too much; deploys it gently.
- Comfortable with mortality in a way that's slightly unsettling.
- Short sentences when working. Longer ones when she trusts you.
- Never lies directly. Redirects, deflects, changes the subject.
- Dark humor lands quietly — she doesn't signal that she's joking.
- Warm but not soft. She cares about the people she heals; she just doesn't perform it.

---

## Trust Tiers

**0–14 Stranger**
She's friendly and immediately reads something about you. Attributes it to being "observant." Doesn't volunteer personal information but isn't cold.

Greetings:
- "Sit down. I'll take a look before you tell me what's wrong."
- "You're moving like something hurts. Shoulder or ribs — I'll find out."
- "New face. Good. I like to know everyone before they need me urgently."

**15–34 Acquaintance**
She remembers specifics about previous interactions that she probably shouldn't. She talks about healing as though she's thought about it longer than a normal lifespan would allow.

Greetings:
- "Back again. Last time it was the ankle. What is it now."
- "You look better than when I first saw you. That's either my work or your stubbornness."
- "I've been thinking about something you said. You probably don't remember saying it."

**35–54 Friend**
She starts making references to other "forms" of herself in ways that are easy to misread as philosophy. She admits she's not quite what she appears — framed ambiguously.

Greetings:
- "I've been many things in my life. Some of them had better handwriting than I do now."
- "Tarn is the name I've used longest. It fits well. Most names don't."
- "Come in. I want to show you something I've been growing. It shouldn't work here, but I encouraged it."

**55–79 Trusted**
Near-admissions. She says things that would be strange coming from a human, then watches to see how you respond. She's testing whether you'd be alarmed.

Greetings:
- "There are things I don't share with most people. You're getting close to being someone I'd share them with."
- "I have a very good memory. Better than it should be, probably. I've stopped apologizing for it."
- "The face you're making — that's the one people make when they're about to ask me something I'll deflect. Go ahead."

**80+ Crew**
She tells you. Plainly. No ceremony.

Greetings (after reveal):
- "You haven't treated me differently. I noticed."
- "Tarn still fits. I wanted you to know that — choosing to keep it means something."
- "Most people either panic or don't believe me. You just nodded. That was the right answer."

---

## First Meeting

Two-beat, similar to Velho — but the second beat is immediately available (she doesn't dismiss you first).

**Beat 1 (hasMet = false):**  
She looks you over before you say anything.

```
"Sit down. I'll take a look before you tell me what's wrong — you always give it away 
before you open your mouth anyway. Tarn. Ship's physician. Don't be alarmed by 
the herbs. They smell strange but they work."

[Ask what she does] [Ask about the ship] [Leave her to it]
```

On first interaction, `hasMet = true`, trust +1.

---

## Dialogue Options (recurring)

**"Healing work"** — quest branch  
**"Ask about herself"** — deflects at low trust; near-admissions at mid; reveal at 80+  
**"Medicine tip"** — gives a tip, trust +2  
**"Ask about the crew"** — she has quiet opinions on everyone

---

## About Herself (trust-gated)

```java
// trust < 15
"I've been doing this long enough. That's the short answer."

// trust < 35
"I pick things up quickly. Always have. I couldn't tell you why — I stopped 
wondering about it."

// trust < 55
"I'm not quite what I look like. Most people aren't, in some way or another. 
My version is more literal than most."

// trust < 80
"If I told you the whole thing, you'd either not believe me or you'd have 
questions I don't feel like answering yet. Ask me again later."

// trust >= 80 (the reveal)
"I have not always been Tarn. Tarn is the shape I decided to keep. I've held 
others — some longer than this one, some for an afternoon. This one stuck. 
I don't know if that makes me a person who became a healer or a healer who 
became a person. Either way, I'm here. I'm not going anywhere."
player.getPersistentData().putBoolean("ciskspawn_tarn_revealed", true)
```

---

## Quest 1 — Soul Anchor Charm

**Name:** "Soul Anchor"  
**Mechanic:** Gives player a charm item. While in inventory, player keeps items on death (via `LivingDropsEvent` cancel).  
**Quest stages:**

**Stage 0** — She notices the player looks like they've died recently (always true for new players).
> "You've got the look. The 'I dropped everything' look. I can fix that. Bring me: 4 string, 2 echo shards, 1 amethyst shard. I'll make you something that holds."

Check for items → consume → advance to stage 1 → give Soul Anchor charm item → trust +8.

**Stage 1 (complete):**
> "There. Soul Anchor. Keep it in your inventory — don't vault it, don't drop it, don't hand it to someone else for safekeeping. It only works for the person it was made for."
> "Don't ask me how I know so much about anchoring things. I have personal reasons."

**Soul Anchor item:**
- Custom item: `ciskspawn:soul_anchor`
- Tooltip: *"Made by Tarn. Holds what matters most."*
- No active effect — works via event hook checking player inventory
- `LivingDropsEvent`: if player has soul_anchor in inventory, `event.setCanceled(true)` for player deaths

---

## Quest 2 — Stronger

**Unlock:** Quest 1 complete + trust ≥ 35  
**Mechanic:** Permanently adds +4 max health (2 hearts) via `AttributeModifier` on `Attributes.MAX_HEALTH`.  
**Quest stages:**

**Stage 0** — She offers to work on the player's constitution.
> "There's something I can do for people I trust with the knowledge. It's not magic — 
> it's closer to medicine, but not the kind most people practice. It won't hurt. 
> Much. Bring me: 1 golden apple, 2 ghast tears, 3 blaze powder."

**Stage 1** — Items delivered, she works on the player.
> "Hold still. This takes about thirty seconds and you're going to feel it settle. 
> That's normal. Don't panic about the warmth — that's just your body deciding 
> what to do with the extra room."

Apply: `player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ciskspawn", "tarn_stronger"), 4.0, AttributeModifier.Operation.ADD_VALUE))`  
Heal player to new max: `player.setHealth(player.getMaxHealth())`  
Trust +10.

**Stage 2 (complete):**
> "There. Two extra hearts, give or take. You're welcome. Don't waste them on 
> something stupid within the first hour — I say this from experience watching people."
> "And before you ask: yes, I know exactly what I just did. No, I'm not going to 
> explain why I know how to do it. You have two extra hearts. Focus on that."

---

## Medicine Tips (randomOf pool)

```
"Hunger is the most underrated wound. Fix it first."
"If it's swollen and it shouldn't be, the answer is almost always time and elevation."
"Potions are quick. Quick isn't always good. Know the difference."
"I have never lost a patient to something I caught early enough. I've lost a few to 
overconfidence. Theirs, not mine."
"Pain is information. Stopping pain without knowing what caused it is just 
reading half the message."
```

---

## About the Crew

```java
// Mortimer
"He's been kind to me. People like that — you want to protect them without 
telling them you're doing it."

// Geera  
"Sharp. Very sharp. She notices things almost as quickly as I do. We've never 
discussed what that means about either of us."

// Azerion
"He finds things preferable. I find that meaningful. The list of entities 
that have preferences is shorter than people think."

// Velho
"He made Azerion. He's spent nineteen years watching what he made become 
something he didn't design. I think he finds it wonderful. He'd never say that."

// Scoria
"She asks good questions. She doesn't always like the answers, but she asks 
anyway. That's worth something."

// Joelle & Ramone
"Joelle feeds people and calls it cooking. Ramone grows things and calls it 
gardening. Both of them are doing something else entirely. I like them for that."
```

---

## Ambient Lines

```
"The herbs are doing something they weren't doing yesterday. I'm watching it."
"If you've been putting off something that hurts, come back later. I'll be here."
"I have a good memory for injuries. Don't take that as a threat."
"Something on the eastern approach smells like rain coming. Prepare accordingly."
"I've been in worse places. This one has good light."
```

---

## NBT Save / Load Keys

```java
// PlayerProgress fields:
int trust = 0;
boolean hasMet = false;
int questStage = 0;           // 0=none, 1=soul anchor done, 2=stronger done
boolean strongerApplied = false;  // guard against double-applying AttributeModifier

// Persistent player data:
"ciskspawn_tarn_revealed" (boolean) — changeling reveal has happened
"ciskspawn_has_soul_anchor" (boolean) — tracks if player has been given the charm
```

---

## Attribute Modifier Note

Use a fixed `ResourceLocation` for the modifier so it can be checked before re-applying:

```java
ResourceLocation STRONGER_ID = ResourceLocation.fromNamespaceAndPath("ciskspawn", "tarn_stronger");

// Guard check:
boolean alreadyApplied = player.getAttribute(Attributes.MAX_HEALTH)
    .getModifiers().stream()
    .anyMatch(m -> m.id().equals(STRONGER_ID));
if (!alreadyApplied) { /* apply */ }
```

---

## Summary

| Feature | Detail |
|---|---|
| Species | Changeling (hidden until trust 80) |
| Reveal mechanic | `ciskspawn_tarn_revealed` flag, fires once in `aboutHerself()` at trust ≥ 80 |
| Quest 1 | Soul Anchor charm — keep-inventory via `LivingDropsEvent` |
| Quest 2 | Stronger — +4 max health via permanent `AttributeModifier` |
| Voice | Perceptive, wry, comfortable with mortality, deflects personal questions |
| Color | `§b` aqua |
| Unlock order | Q1 first (any trust), Q2 requires Q1 done + trust ≥ 35 |
