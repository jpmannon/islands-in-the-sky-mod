# Cade — Security / Fighter

**Role:** Ship and settlement security, former sky-navy  
**Location:** Near the ship's gangway or the settlement perimeter — wherever something could go wrong  
**Species:** Human  
**Mod color code:** `§c` (red)  
**Title display:** `Cade - Security`

---

## Core Identity

Cade left the sky-navy at the end of a contract he didn't renew. He doesn't explain why. He came aboard because Mortimer asked him in person and told him exactly what the job was. He's stayed because no one has given him a reason to leave, and because he thinks the people here are worth protecting — though he'd phrase it differently if you pushed him. He'd say the work is consistent.

He is not brooding. He's quiet because he doesn't see the point in words that don't carry weight. When he does say something, he means it exactly. He'll tell you something important once. He won't repeat it. This can read as cold to people who aren't paying attention. People who are paying attention understand it as a form of respect.

At low trust he's professional and brief. At high trust he starts asking questions — about where you've been, what you saw, how you handled it. That's how you know you've gotten somewhere. Cade only asks about things he cares about.

He has a clear moral code but doesn't explain it unless asked and finds it mildly irritating when he has to. He's not proud of everything he's done. He's at peace with most of it.

---

## Voice

- Short declaratives. Subject, verb, done.
- No metaphors. No softening language.
- Rare compliments — but when they land, they land hard.
- Dry without being cold. Occasionally wry, never performative.
- Doesn't ask questions unless he already cares about the answer.
- When genuinely amused, it shows in one word, not a sentence.

---

## Trust Tiers

**0–14 Stranger**
Registers your existence. Tells you what you need to know to stay out of trouble. Not rude — just minimal.

Greetings:
- "Stay on the path. The drop-off on the west side isn't obvious until it is."
- "You're new. Good. Keep that knife where I can see it's yours."
- "Cade. I handle security. Don't make my job harder than it has to be."

**15–34 Acquaintance**
Remembers what you did last time. Starts evaluating.

Greetings:
- "Back again. You didn't break anything last time. That's something."
- "You move like you've had training. Who."
- "The south approach had a problem this morning. Nothing now. Just so you know."

**35–54 Friend**
Offers information you didn't ask for. Occasional dry commentary on the crew.

Greetings:
- "Tarn says you've been keeping your health up. Good habit."
- "Velho asked me something strange last week. I've been thinking about it since."
- "You're consistent. I respect that more than most things."

**55–79 Trusted**
Starts asking you questions. Wants to know how you handle things.

Greetings:
- "Tell me something. The last island you came from — anyone follow you?"
- "I've been thinking about the west perimeter. Walk it with me if you have time."
- "I've worked with worse crews. I've worked with better. This one's worth the work. Thought you should know that."

**80+ Crew**
Direct. Personal. You've earned his actual opinion.

Greetings:
- "If something goes wrong out there, you're who I'd want to know about it first. Just so you know where you stand."
- "I had a partner in the navy. Good fighter. Better instincts. You remind me of her sometimes. That's a compliment."
- "Still here. Good."

---

## First Meeting

Direct, professional, assessing.

```
"Cade. I handle security here. You're new, so I'll say this once: 
don't go past the east ridge without telling someone. The footing's 
bad and the wind comes from the wrong direction.

Everything else you'll figure out. Ask if you don't."

[Ask about the ship] [Ask about threats] [Ask about him] [Leave it]
```

`hasMet = true`, trust +1.

---

## Quest Design — Combat Training

**Framing:** Cade doesn't offer to train you. He offers to evaluate you. If you pass his bar, he'll help you get better. If you don't, he'll tell you plainly and give you what you need to work on.

**Stage 0** — He observes the player has taken damage:
> "You've been in a fight recently. You took more than you should have. I can help with that — but I want to see what you're working with first. Bring me: 2 iron swords. I'll test them and I'll test you."

Check for 2 iron swords → consume → say:
> "These are fine. Here's what I'm going to show you. Pay attention. I don't repeat myself."
> Trust +5. `questStage = 1`. Give enchanted book (Sharpness I).

**Stage 1** — Practical test:
> "You've got the basics. Now I want to know if you can apply them under pressure. Bring me proof you've handled something that could actually hurt you. Blaze rod. If you can get to a fortress, you can get past what's waiting for it."

Check for 1 blaze rod → consume → say:
> "Good. You went and came back. Some people don't."
> Trust +8. `questStage = 2`. Give enchanted book (Fire Aspect I + Knockback I, combined on one book if possible, else two books).

**Stage 2** — Full reward:
> "Last thing. Bring me something that cost you — not in materials, in effort. A wither skeleton skull. One's enough."

Check for 1 wither skeleton skull → consume → say:
> "You earned this."

Apply permanent `AttributeModifier` on `Attributes.ATTACK_SPEED`:
```java
ResourceLocation TRAINING_ID = ResourceLocation.fromNamespaceAndPath("ciskspawn", "cade_training");
// Add +0.1 to attack speed (roughly +10%, stacks with base)
attr.addPermanentModifier(new AttributeModifier(TRAINING_ID, 0.1, AttributeModifier.Operation.ADD_VALUE));
```

Trust +10. `questStage = 3`.

> "Don't waste it."

**Stage 3 (complete):**
> "You've had everything I can give you. The rest is practice. You know where I am."

---

## Dialogue Options

**"Ask about threats"** — gives situation intel, trust-gated
**"Ask about him"** — deflects at low trust, opens at high trust
**"Combat tip"** — trust +2, random tip from pool

---

## Combat Tips (randomOf pool)

```
"Hit first if you're going to hit. Hesitation costs more than initiative."
"Armor buys time. Time buys options. Don't confuse either one for safety."
"If you don't know what something does, don't stand in front of it."
"Retreat is a tactic. Running is a failure. Know the difference before you move."
"Fight what's in front of you. Think about what's behind you. Do both."
```

---

## About Him (trust-gated)

```java
// trust < 35
"I've been doing this long enough. That covers most of the questions."

// trust < 55
"I served five years. Didn't renew the contract. That's the short version."

// trust < 80
"The navy asks you to protect things that don't always deserve protecting. 
Eventually you run the numbers. I ran them."

// trust >= 80
"My commanding officer gave an order I couldn't follow. I told him I couldn't 
follow it. He accepted that, which surprised me. He was a good officer. 
I wasn't a good soldier by the end. Different things."
```

---

## About the Crew (randomOf per NPC)

```
Mortimer:  "He's honest about what he is. That matters more than most people think."
Geera:     "Sharp. Knows the value of everything she sells and everything she doesn't."
Scoria:    "She's got good instincts. Rough edges. She'll be formidable."
Azerion:   "Never have to worry about his loyalty. Can't decide if that's reassuring or unsettling."
Velho:     "Brilliant. Difficult. I trust him more than he'd expect me to."
Joelle:    "She feeds people. That's not a small thing."
Ramone:    "Quiet. Watches. Knows more than he says. Good qualities."
Tarn:      "I don't ask questions about Tarn. She doesn't ask questions about me. Works well."
Zii-ko:   "Good work. Honest about what he can and can't do. Respectable."
Agatha:    "Knows everything about everyone. I try to stay on her good side."
```

---

## Ambient Lines

```
"Check your footing near the north edge. Especially at night."
"Wind's shifted. Something's coming from the west."
"All clear. That's what I want to keep saying."
"Seen worse weather. Seen worse company. Currently prefer both."
"Still here."
```

---

## NBT Save / Load

Tag: `"CadeProgress"` — stores Trust, HasMet, QuestStage, `StrongerApplied` (bool guard for attribute modifier).

Player persistent key: `"ciskspawn_cade_trained"` (boolean) — for cross-NPC reference if needed.
