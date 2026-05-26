package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgathaEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;

    public AgathaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.40D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide) openDialogue(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void openDialogue(Player player) {
        PlayerProgress progress = getProgress(player);

        if (!progress.hasMet) {
            progress.hasMet = true;
            player.getPersistentData().putBoolean("ciskspawn_agatha_visited", true);
            addTrust(player, progress, 1);
            String body = "Oh! Don't mind Smudge — yes, he is just going to sit there, that's fine, he owns "
                    + "this doorway apparently — come in, come in!\n\nI'm Agatha. Brewer, herbalist, and "
                    + "keeper of these three terrible wonderful creatures you'll be tripping over. That's "
                    + "Ink — yes, the judgmental one. Biscuit is somewhere. And you've met Smudge.\n\n"
                    + "Now. What can I do for you? And what's your name? I like to know everyone.";
            say(player, "Oh! Don't mind Smudge — yes, he is just going to sit there, that's fine, he owns this doorway apparently — come in, come in!");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Agatha - Brewer",
                        body,
                        "Ask a favour",
                        "Ask about the cats",
                        "She was already talking before you finished walking in."
                ));
            }
            return;
        }

        player.getPersistentData().putBoolean("ciskspawn_agatha_visited", true);
        addTrust(player, progress, 1);
        String greeting = pickAgathaGreeting(progress);
        String body = greeting + "\n\n" + buildDialogueBody(progress);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Agatha - Brewer",
                    body,
                    "Ask a favour",
                    "Ask about the cats",
                    "Brewing notes are kept in the Crew Logbook."
            ));
        }
    }

    public void handleAgathaAction(Player player, String action) {
        switch (action) {
            case "agatha_quest" -> handleQuest(player);
            case "agatha_cats" -> aboutCats(player);
            case "agatha_aster" -> aboutAster(player);
            case "agatha_crew" -> aboutCrew(player);
            default -> openDialogue(player);
        }
    }

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

    private String buildDialogueBody(PlayerProgress progress) {
        return switch (progress.questStage) {
            case 0 -> "Oh, I've been meaning to ask someone! Ink has been restless lately — I think the air up here isn't quite right for him. "
                    + "I need: five sweet berries, two dried kelp, and a lily pad. Not medicinal, just — I want to make him a little treat.";
            case 1 -> "I'm low on something and it's driving me absolutely mad. I need four blaze powder, three nether wart, and a ghast tear if you can manage it.";
            case 2 -> "I need one more thing and then I can finish what I've been working on for months. One dragon's breath.";
            default -> "You've been wonderful. Truly. Now sit down, Ink wants attention and I want to tell you about what the harbormaster said last week.";
        };
    }

    private void handleQuest(Player player) {
        PlayerProgress progress = getProgress(player);
        switch (progress.questStage) {
            case 0 -> inkQuest(player, progress);
            case 1 -> shelfQuest(player, progress);
            case 2 -> goodBatchQuest(player, progress);
            default -> say(player, "You've been wonderful. Truly. Now sit down, Ink wants attention and I want to tell you about what the harbormaster said last week.");
        }
    }

    private void inkQuest(Player player, PlayerProgress progress) {
        if (!hasItem(player, "minecraft:sweet_berries", 5)
                || !hasItem(player, "minecraft:dried_kelp", 2)
                || !hasItem(player, "minecraft:lily_pad", 1)) {
            say(player, "Oh, I've been meaning to ask someone! Ink has been restless lately — I think the air up here isn't quite right for him. I need: five sweet berries, two dried kelp, and a lily pad. Not medicinal, just — I want to make him a little treat. He deserves one. He's been so patient with me.");
            return;
        }

        consumeItem(player, "minecraft:sweet_berries", 5);
        consumeItem(player, "minecraft:dried_kelp", 2);
        consumeItem(player, "minecraft:lily_pad", 1);
        progress.questStage = 1;
        addTrust(player, progress, 6);
        givePotion(player, Items.POTION, Potions.SWIFTNESS, 3, "minecraft:sugar", 4);
        givePotion(player, Items.POTION, Potions.REGENERATION, 2, "minecraft:glistering_melon_slice", 2);
        say(player, "Oh you absolute star. He's going to be thrilled. Or he's going to look at it and walk away and I'll eat it myself, but either way — here, I made you something while I was thinking about it.");
    }

    private void shelfQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 15) {
            say(player, "Oh, I couldn't ask that of someone I've only just met! Come back when we know each other a little better.");
            return;
        }
        if (!hasItem(player, "minecraft:blaze_powder", 4)
                || !hasItem(player, "minecraft:nether_wart", 3)
                || !hasItem(player, "minecraft:ghast_tear", 1)) {
            say(player, "I'm low on something and it's driving me absolutely mad. I need four blaze powder, three nether wart, and a ghast tear if you can manage it. The things I can make with a ghast tear, love — you have no idea.");
            return;
        }

        consumeItem(player, "minecraft:blaze_powder", 4);
        consumeItem(player, "minecraft:nether_wart", 3);
        consumeItem(player, "minecraft:ghast_tear", 1);
        progress.questStage = 2;
        addTrust(player, progress, 8);
        givePotion(player, Items.POTION, Potions.STRENGTH, 2, "minecraft:blaze_powder", 2);
        givePotion(player, Items.POTION, Potions.FIRE_RESISTANCE, 1, "minecraft:magma_cream", 1);
        givePotion(player, Items.POTION, Potions.INVISIBILITY, 1, "minecraft:fermented_spider_eye", 1);
        say(player, "Oh perfect, yes, exactly right — Biscuit! Get off the ingredient jars, we've had this conversation — sorry, where was I. Yes. Perfect. I'm going to make you something that takes a week to make and you're going to use it at exactly the right moment.");
    }

    private void goodBatchQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 35) {
            say(player, "Oh, that one I save for people I know well. Come back when we've had a few more conversations, love.");
            return;
        }
        if (!hasItem(player, "minecraft:dragon_breath", 1)) {
            say(player, "I need one more thing and then I can finish what I've been working on for months. One dragon's breath. Don't ask me how you get it — you'll figure it out — just bring it here when you have it and I will do something wonderful.");
            return;
        }

        consumeItem(player, "minecraft:dragon_breath", 1);
        progress.questStage = 3;
        addTrust(player, progress, 12);
        givePotion(player, Items.LINGERING_POTION, Potions.STRONG_HEALING, 3, "minecraft:glistering_melon_slice", 3);
        givePotion(player, Items.SPLASH_POTION, Potions.HARMING, 1, "minecraft:fermented_spider_eye", 1);
        say(player, "Oh. Oh yes. Yes, this is exactly it. Smudge, come look — don't actually come look, you'll knock it over — sit.");
        say(player, "...There. That's for you. I've been saving that recipe for someone who'd actually use it.");
    }

    private void aboutCats(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(player, progress, 2);
        say(player, randomOf(
                "Ink is the oldest — seven years, very dignified, deeply offended by everything. I love him.",
                "Biscuit came to me in the rain three winters ago and has never left. Excellent decision on her part.",
                "Smudge found his way in through a window I didn't know was open. He's been here since. I've stopped questioning it.",
                "Ink knocked over my best flask last week and looked me directly in the eyes while he did it. No remorse.",
                "Biscuit and Smudge have reached an agreement about the south windowsill. I wasn't consulted.",
                "Ink is a very serious cat. He watches me work. Sometimes I think he's judging my methods. He's probably right.",
                "You know, Smudge goes off some nights and I never know where. He always comes back smelling of woodsmoke. I've decided not to investigate."
        ));
    }

    private void aboutAster(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(player, progress, 1);
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
    }

    private void aboutCrew(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(player, progress, 1);
        say(player, randomOf(
                "Geera is my oldest friend and my worst influence — she'll tell you the same about me. Don't believe her, she started it.",
                "Mortimer came through Aster years ago before he was the Mortimer, you know. I remember when he was still figuring things out.",
                "Oh I adore Scoria. Very intense. I told her once that her father worries and she made a face like I'd told her the sky was blue. She knows.",
                "Tarn noticed Smudge has a trick knee before I did. I didn't ask how she knew.",
                "Velho brought me something once that I still haven't quite identified. It's on the high shelf.",
                "I bring Zii-ko tea. He lets my cats in the shop. Best relationship I have with anyone in the city.",
                "I don't know much about Cade. He doesn't tell me much about him. That's suspicious. I'm watching."
        ));
    }

    private boolean hasItem(Player player, String itemId, int amount) {
        return countItem(player, itemId) >= amount;
    }

    private int countItem(Player player, String itemId) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stackMatchesId(stack, itemId)) count += stack.getCount();
        }
        return count;
    }

    private void consumeItem(Player player, String itemId, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stackMatchesId(stack, itemId)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
            if (remaining <= 0) break;
        }
    }

    private boolean giveItemById(Player player, String itemId, int amount) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return false;
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id), amount);
        giveStack(player, stack);
        return true;
    }

    private void givePotion(Player player, Item item, Holder<net.minecraft.world.item.alchemy.Potion> potion, int amount, String fallbackItemId, int fallbackAmount) {
        try {
            for (int i = 0; i < amount; i++) {
                ItemStack stack = new ItemStack(item);
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                giveStack(player, stack);
            }
        } catch (RuntimeException exception) {
            giveItemById(player, fallbackItemId, fallbackAmount);
        }
    }

    private void giveStack(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private boolean stackMatchesId(ItemStack stack, String itemId) {
        if (stack.isEmpty()) return false;
        return stack.getItem().builtInRegistryHolder().key().location().toString().equals(itemId);
    }

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

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        if (speechTicksLeft > 0) {
            speechTicksLeft--;
            if (speechTicksLeft == 0) {
                this.setCustomNameVisible(false);
                this.setCustomName(Component.literal("Agatha - Brewer"));
            }
        }

        if (ambientCooldown > 0) {
            ambientCooldown--;
            return;
        }

        Player nearby = this.level().getNearestPlayer(this, 8.0D);
        if (nearby != null) sayNearby(pickAmbientLine());
        ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
    }

    private String pickAmbientLine() {
        return randomOf(
                "Biscuit! That is a potion ingredient, not a toy. Biscuit.",
                "Mm. This one needs another hour. I can feel it.",
                "Did I tell you what Ink did this morning? Of course I did. He did it again.",
                "Come in if you're coming in — the draft is bad for the fermentation.",
                "I love this city. Everyone's got a story. Nobody tells the whole one."
        );
    }

    private PlayerProgress getProgress(Player player) {
        return progressByPlayer.computeIfAbsent(player.getUUID(), uuid -> new PlayerProgress());
    }

    public int getTrustForPlayer(UUID playerUUID) {
        return progressByPlayer.getOrDefault(playerUUID, new PlayerProgress()).trust;
    }

    private void addTrust(Player player, PlayerProgress progress, int amount) {
        progress.trust = Math.min(100, progress.trust + amount);
        if (progress.trust >= 35 && !player.getPersistentData().getBoolean("ciskspawn_has_agatha_trust")) {
            player.getPersistentData().putBoolean("ciskspawn_has_agatha_trust", true);
        }
    }

    private String randomOf(String... lines) {
        return lines[this.random.nextInt(lines.length)];
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerProgress> entry : progressByPlayer.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            playerTag.putInt("Trust", entry.getValue().trust);
            playerTag.putBoolean("HasMet", entry.getValue().hasMet);
            playerTag.putInt("QuestStage", entry.getValue().questStage);
            list.add(playerTag);
        }
        tag.put("AgathaProgress", list);
        tag.putInt("AmbientCooldown", ambientCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        progressByPlayer.clear();
        if (tag.contains("AmbientCooldown")) ambientCooldown = tag.getInt("AmbientCooldown");
        if (tag.contains("AgathaProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("AgathaProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.questStage = playerTag.getInt("QuestStage");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<AgathaEntity> state) {
        if (state.isMoving()) state.setAnimation(WALK_ANIM);
        else state.setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static class PlayerProgress {
        int trust = 0;
        boolean hasMet = false;
        int questStage = 0;
    }
}
