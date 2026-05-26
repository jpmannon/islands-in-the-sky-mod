# Agatha — Brewer / Herbalist

**Role:** Brewer, herbalist, potions and tinctures  
**Location:** Aster — the regional capital island city. She runs her own shop there, same city as Zii-ko (confirmed). Good friends with Geera despite the distance — they visit or trade regularly.
**Species:** Human (older — similar age to Geera)  
**Mod color code:** `§d` (light purple)  
**Title display:** `Agatha - Brewer`

---

## Core Identity

Agatha has lived in Aster her whole life and knows approximately everyone in it and most of their business. She doesn't repeat gossip maliciously — she repeats it because she finds people genuinely fascinating and can't help sharing. She's been doing this long enough that most people have just accepted it as part of who she is. Even the people she's gossiped about mostly don't hold it against her, because she's equally willing to gossip in their favor.

She has three cats: **Ink** (black), **Biscuit** (white), and **Smudge** (gray). She talks about them the way some people talk about children — with complete sincerity, total investment, and no awareness that the other person might not share her level of interest. The cats are part of every conversation eventually. You cannot stop this from happening.

She is eccentric in the way that people become when they've spent a long time doing exactly what they want and stopped worrying about how it looks. Her shop is organized by her own logic. Her potions work. Her cats sit wherever they like. She's generous to people who need something and particular about people who waste her time. She's almost impossible not to like.

Geera is one of her oldest friends. They argue like it, which is to say warmly and about nothing serious.

---

## Voice

- Warm, quick, chatty. Sentences run long when she's excited.
- Gossip delivered with enthusiasm, not malice. She finds people *interesting*.
- Sassy but not sharp. The sass has a smile in it.
- Pivots to cats mid-conversation without apology.
- Generous with compliments. Also with unsolicited opinions.
- Calls the player "dear" or "love" after the first few meetings.
- When actually serious, she gets quieter. It's noticeable because of the contrast.

---

## Trust Tiers

**0–14 Stranger**
Immediately friendly. Immediately curious. Offers you something before she knows your name.

Greetings:
- "Oh! A new one! Come in, come in — Biscuit, off the counter, we have company."
- "Hello, hello! I don't think I've seen you before. I'd remember. I remember everyone."
- "Welcome! Mind Smudge — he stretches out in the doorway and takes absolutely no responsibility for it."

**15–34 Acquaintance**
Remembers your name, probably knows something about you already via Geera.

Greetings:
- "Back again! Geera mentioned you, you know. Said you were interesting. She doesn't say that about many."
- "Oh good, you're here — Ink has been in a mood and I need someone to tell me I'm not imagining it. Look at that face."
- "I was just thinking about you! I had something I wanted to ask. Also I made a new blend this morning — you should try it."

**35–54 Friend**
Warm, chatty, starts looping you into island gossip. Ink has decided he likes you.

Greetings:
- "There you are! Ink! Look who's here — yes, go say hello, don't be shy — sorry, he's been a bit particular lately."
- "I have news. Sit down, let me make you something, and I'll tell you what I heard about the harbormaster. You'll love it."
- "Smudge slept on the good mortar again. I'm not even upset. Look at his little face. I simply cannot be upset."

**55–79 Trusted**
Shares things she doesn't share with most people. Gets genuinely fond.

Greetings:
- "You know, I was thinking — you're one of the good ones. I have a sense for it. Fifty years of talking to people, you develop a sense."
- "Geera asked about you again. I told her you were doing well. I hope that was true?"
- "Sit down, love. Biscuit, come here and be sweet to our friend. There. That's better."

**80+ Crew**
Treats you like family. Sends you on quests for her own reasons which also happen to benefit you greatly.

Greetings:
- "Oh wonderful, you're here — I was just telling Ink about you. He pretended not to care but he was listening."
- "I made something for you. It's not a quest, it's a gift. You've more than earned it."
- "You know you're one of my favorite people, don't you. Don't let it go to your head. Sit down."

---

## First Meeting

She's already talking before you finish walking in.

```
"Oh! Don't mind Smudge — yes, he is just going to sit there, that's fine, 
he owns this doorway apparently — come in, come in!

I'm Agatha. Brewer, herbalist, and keeper of these three terrible wonderful 
creatures you'll be tripping over. That's Ink — yes, the judgmental one. 
Biscuit is somewhere. And you've met Smudge.

Now. What can I do for you? And what's your name? I like to know everyone."

[Potions and brews] [Ask a favour] [Ask about the cats] [Ask about Aster]
```

`hasMet = true`, trust +1.

---

## Quest Design — Fetch & Brew (Series)

Agatha is the quintessential fetch-quest NPC — but her rewards are unusually good, and she makes you feel like you did her a personal favor rather than running an errand.

**Quest 1 — Something for Ink**
> "Oh, I've been meaning to ask someone! Ink has been restless lately — I think the 
> air up here isn't quite right for him. I need: 5 sweet berries, 2 dried kelp, and 
> a lily pad. Not medicinal, just — I want to make him a little treat. He deserves 
> one. He's been so patient with me."

Deliver: sweet berries (5) + dried kelp (2) + lily pad (1)
> "Oh you absolute star. He's going to be thrilled. Or he's going to look at it and walk 
> away and I'll eat it myself, but either way — here, I made you something while I was 
> thinking about it."

Reward: 3 Potions of Swiftness (3:00) + 2 Potions of Regeneration (0:45). Trust +6.

---

**Quest 2 — For the Shelf**
> "I'm low on something and it's driving me absolutely mad. I need 4 blaze powder, 
> 3 nether wart, and a ghast tear if you can manage it. I know, I know — but if you 
> ever find yourself near a fortress, you'll thank me. The things I can make with a 
> ghast tear, love — you have no idea."

Deliver: blaze powder (4) + nether wart (3) + ghast tear (1)
> "Oh perfect, yes, exactly right — Biscuit! Get off the ingredient jars, we've had 
> this conversation — sorry, where was I. Yes. Perfect. I'm going to make you something 
> that takes a week to make and you're going to use it at exactly the right moment."

Reward: 2 Potions of Strength II (1:30) + 1 Potion of Fire Resistance (8:00) + 1 Potion of Invisibility (3:00). Trust +8.

---

**Quest 3 — The Good Batch**
> "I need one more thing and then I can finish what I've been working on for months. 
> One dragon's breath. Don't ask me how you get it — you'll figure it out — just bring 
> it here when you have it and I will do something wonderful."

Deliver: dragon's breath (1)
> "Oh. Oh yes. Yes, this is exactly it. Smudge, come look — don't actually come look, 
> you'll knock it over — sit. Right. This is going to take me just a moment.

> ...There. That's for you. I've been saving that recipe for someone who'd actually use it."

Reward: 3 Lingering Potions of Healing II + 1 Splash Potion of Harming II (for enemies) + something specific to the player's journey (creative decision per server). Trust +12.

---

## "Ask About the Cats" (any trust, trust +2)

```java
say(player, randomOf(
    "Ink is the oldest — seven years, very dignified, deeply offended by everything. I love him.",
    "Biscuit came to me in the rain three winters ago and has never left. Excellent decision on her part.",
    "Smudge found his way in through a window I didn't know was open. He's been here since. I've stopped questioning it.",
    "Ink knocked over my best flask last week and looked me directly in the eyes while he did it. No remorse.",
    "Biscuit and Smudge have reached an agreement about the south windowsill. I wasn't consulted.",
    "Ink is a very serious cat. He watches me work. Sometimes I think he's judging my methods. He's probably right."
));
```

---

## "Ask About Aster" (trust-gated gossip, trust +1)

```java
// trust < 15
say(player, "Oh, Aster is wonderful. Old city, lots of history, everyone knows everyone — 
which is either charming or exhausting depending on the day.");

// trust < 35
say(player, "I'll tell you this much — the harbormaster and the city council have been 
having a disagreement for two years that neither of them will admit is personal. I have theories.");

// trust < 55
say(player, randomOf(
    "Geera tells me there's been unusual traffic through the lower docks lately. 
     She doesn't say what she thinks it means. I have ideas.",
    "There's a family on the east side of the city that's been here since the 
     founding. They don't talk about how they got their first island. Nobody asks."
));

// trust >= 55
say(player, randomOf(
    "Alright, I'll tell you what I actually know — and what I suspect, which is different 
     but usually more interesting.",
    "You didn't hear this from me. You heard this from someone who knows someone who 
     told me. That's different. That's just being informed."
));
```

---

## About the Crew / Other NPCs

```
Geera:     "My oldest friend and my worst influence — she'll tell you the same about me. 
            Don't believe her, she started it."
Mortimer:  "Good man. Came through Aster years ago before he was the Mortimer, you know. 
            I remember when he was still figuring things out."
Scoria:    "Oh I adore that girl. Very intense. I told her once that her father worries 
            and she made a face like I'd told her the sky was blue. She knows."
Tarn:      "Very perceptive, that one. She noticed Smudge has a trick knee before I did. 
            I didn't ask how she knew."
Velho:     "Brilliant. Terrifying. Brought me something once that I still haven't quite 
            identified. I put it on the high shelf."
Zii-ko:   "I bring him tea. He lets my cats in the shop. Best relationship I have with 
            anyone in the city."
Cade:      "I don't know much about him. He doesn't tell me much about him. 
            That's suspicious. I'm watching."
```

---

## Ambient Lines

```
"Biscuit! That is a potion ingredient, not a toy. Biscuit."
"Mm. This one needs another hour. I can feel it."
"Did I tell you what Ink did this morning? Of course I did. He did it again."
"Come in if you're coming in — the draft is bad for the fermentation."
"I love this city. Everyone's got a story. Nobody tells the whole one."
```

---

## NBT Save / Load

Tag: `"AgathaProgress"` — stores Trust, HasMet, QuestStage (0–3).

Player persistent key: `"ciskspawn_agatha_visited"` (boolean) — for Geera to reference cross-NPC.
