# Zii-ko — Smith / Pogon Craftsman

**Role:** Blacksmith, weaponsmith, custom forge work  
**Location:** His own shop on solid ground in the island town (NOT on any ship or mobile structure)  
**Species:** Pogon (kobold-like race) — approximately 3 feet tall, big and pretty by Pogon standards  
**Mod color code:** `§6` (gold)  
**Title display:** `Zii-ko - Smith`

---

## Pogon Species Notes

Pogons are a small, kobold-adjacent people with a native language full of guttural clicks and hissing sounds that are physically impossible for humans to reproduce due to vocal cord differences. Zii-ko speaks broken English — short clauses, dropped articles, direct address, no hedging.

Culturally, Pogons:
- Always assume the best of others
- Take setbacks and bad news completely in stride — nothing much rattles them
- Only do things they genuinely want to do (not laziness — authenticity)
- Are easily bored, which means when they commit to something, it means something
- Are unbothered by nature; they don't perform reaction

Zii-ko being in that town means he chose to be there. That's a compliment the player earns the context for over time.

He is considered pretty and large by Pogon standards. He carries himself like someone completely comfortable with both facts.

**Inspiration:** Heimerdinger (Arcane) — brilliant, enthusiastic about craft, genuinely kind, honest to a fault about quality, mildly impatient with people who waste his time but patient with people willing to learn.

---

## Voice — Speech Pattern Rules

- Drops articles ("bring me good materials" not "bring me **the** good materials")
- Short, complete clauses separated by comma or pause
- Direct address: "Oi! Human!" or "You. Come here."
- States situation first, then condition, then result: "You bring good steel? I make good sword. Simple."
- Repetition for emphasis: "only good materials, only good things"
- Never softens a criticism but doesn't deliver it with malice
- Occasional untranslated Pogon sounds for emphasis: *"[click-hiss]"* as equivalent of a sigh or "obviously"
- "Not my fault. Your fault." is a signature construction — he separates responsibility cleanly

---

## Trust Tiers

**0–14 Stranger**
Registers you, tells you what he does, offers the forge immediately. Pogons assume the best.

Greetings:
- "I am Zii-ko, who you?"
- "You new? Come. I show you what I make."
- "Oi! New face! You want something made? Tell me."

**15–34 Acquaintance**
Remembers you. Brief warmth. Still direct.

Greetings:
- "Oi! Human! You back! Good. What you need."
- "You again. Still alive? Good job. What you bring me."
- "I remember you. You had... [click-hiss] ... bad materials. You do better this time?"

**35–54 Friend**
More relaxed. Volunteers information. Starts commenting on other people.

Greetings:
- "Good, good. You come at good time. I am testing new grip design. You want to see?"
- "I was thinking about you! I have idea for better sword. Come."
- "You know Agatha? She bring me tea this morning. Good tea. I like her cats. They sit on my materials. I don't mind."

**55–79 Trusted**
Personal. Talks about Pogon things. Asks genuine questions.

Greetings:
- "My shop doing good today. I am happy. Happy Pogon makes better things — this is true fact."
- "You know, in Pogon, we say: 'The one who comes back is the one worth making for.' You keep coming back. This is good thing."
- "I was thinking — you travel a lot, yes? You see strange metals anywhere? Bring me, I study."

**80+ Crew**
Openly affectionate in the Pogon way — which looks like casual honesty.

Greetings:
- "Oh good, it's you. Sit. I finish this and then we talk. I like when you visit."
- "I made something for you. Not because you ask — because I wanted to. You are good customer and also good person. Both matter."
- "You know, most humans... [click-hiss] ... they come, they take, they go. You are different. You stay. I like that."

---

## First Meeting

```
"I am Zii-ko, who you?

...You want sword? I give you good price. Bring materials — only good 
materials. You come back then, I make. Go now."

[Ask what counts as good materials] [Ask about him] [Leave]
```

`hasMet = true`, trust +1.

---

## Core Quest — The Forge (Tiered Material System)

This is not a linear quest chain. It's a single forge interaction that evaluates what the player brings and responds proportionally. The player can return multiple times.

**Calling the quest: "Bring materials"**

Zii-ko checks for materials in a tiered priority order. Best tier checked first.

---

### Tier 3 — Exceptional (Netherite)
Materials: 1 netherite ingot + 4 iron ingots + 2 blaze rods

> "Oh. Oh! This is good. This is very good. [click-hiss] You found netherite? 
> Real netherite? ...Yes. Okay. You sit. I work. This will be best thing I make this week."

Give: Custom-enchanted netherite sword (Sharpness III, Fire Aspect I, Unbreaking II) or netherite pickaxe depending on player preference dialog.
Trust +12. Can repeat for different tool types.

> "There. You see? Good materials, good result. Not my skill alone — 
> your materials, my hands. We both did this. You understand?"

---

### Tier 2 — Good (Diamond)
Materials: 2 diamonds + 4 iron ingots + 1 blaze rod

> "Diamond! Good, good. I can work with diamond. Okay. Come back in — 
> [click-hiss] — small time. I make you something worth carrying."

Give: Diamond sword (Sharpness II, Unbreaking I) or diamond pickaxe.
Trust +8.

> "Good sword. Not the best I ever make — I save that for netherite — 
> but good. Don't drop it in lava. I will know. I always know."

---

### Tier 1 — Basic (Iron)
Materials: 6 iron ingots + 1 flint

> "Oh... [click-hiss] ... these are... okay materials. Not bad. Not good. 
> Okay. I make something. It will work. It won't be beautiful. 
> Not my fault — your fault. Only good materials make good things."

Give: Iron sword (Sharpness I) or iron pickaxe.
Trust +3.

> "See? It works. I told you — it works but is not beautiful. 
> Next time, bring better. I show you what I can really do."

---

### No matching materials
> "You brought... [click-hiss] ... no. No, this is not right. 
> You need iron, diamond, or netherite. And blaze rods — they help 
> with the heat process. Go find better, come back. I wait. I am always here."

---

## "Ask About Good Materials" Tip

```
"Good materials? Okay. Listen.

Iron is fine. Basic. I can work with iron — everyone can work with iron.
Diamond is good. I like diamond. Harder, holds edge longer.
Netherite... [click-hiss, pause] ...if you can find netherite, bring all of it. 
I make you something you remember.

Blaze rods help with forge temperature. Always bring. 
Better heat, better bond. Simple science."
```

---

## About Himself (trust-gated)

```java
// trust < 15
"I am Pogon. We are small, we make good things. That is all you need to know."

// trust < 35
"I came here because — [click-hiss] — I was bored. Then I was not bored. 
Then I stayed. This is how Pogon decide things. Simple."

// trust < 55
"In Pogon, we say only do what you want. Not lazy — honest. 
I want to make things. So I make things. Very simple life."

// trust >= 80
"My home island... I miss it sometimes. The sounds. 
You would not understand the sounds — human throat cannot make them. 
But I teach you to listen. That is different thing. Listening is possible for anyone."
```

---

## About the Town / Other NPCs

```
Mortimer:  "Good man. Flies his ship. Comes in sometimes, asks about armor. I give fair price. He pays fair price. Good relationship."
Geera:     "Geera! Yes. She bring me good business. Also good conversation. Pogon like when humans are direct. Geera is direct."
Agatha:    "Agatha! [click-hiss — warm tone] She bring me tea. Her cats come also. One gray one — Smudge — he sleep on my best anvil once. I let him."
Joelle:    "She make food that is also art. I understand this. My swords are also art. We have good understanding, me and Joelle."
Tarn:      "I don't get sick — Pogon immune system is [click-hiss] very robust. But I visit Tarn anyway. She is interesting. Very perceptive."
Cade:      "Good fighter. He know how to use good blade — this is rare. Most people carry weapon and don't understand it. Cade understands."
```

---

## Ambient Lines

```
"[click-hiss] ...almost. Almost right..."
"If you hear good clang, that is good sign. Bad clang means I am learning something."
"You know what I like about this island? Nobody bother me unless they want something. Very efficient."
"Oi! Don't touch that one. That one is cooling. Touch it after."
"Good day. Good materials today. Good day."
```

---

## NBT Save / Load

Tag: `"ZiikoProgress"` — stores Trust, HasMet, ForgeCount (int, tracks how many times player has used the forge).

No complex quest stages — just trust + forge interaction history.
