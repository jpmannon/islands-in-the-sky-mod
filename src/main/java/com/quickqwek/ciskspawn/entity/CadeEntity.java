package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class CadeEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;

    public CadeEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.19D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
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
            addTrust(progress, 1);
            String body = "Cade. I handle security here. You're new, so I'll say this once: don't go past "
                    + "the east ridge without telling someone. The footing's bad and the wind comes "
                    + "from the wrong direction.\n\nEverything else you'll figure out. Ask if you don't.";
            say(player, "Cade. I handle security here. You're new, so I'll say this once: don't go past the east ridge without telling someone.");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Cade - Security",
                        body,
                        "Combat training",
                        "Combat tip",
                        "He registered you before you finished walking in."
                ));
            }
            return;
        }

        addTrust(progress, 1);
        String greeting = pickCadeGreeting(progress);
        String body = greeting + "\n\n" + buildDialogueBody(progress);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Cade - Security",
                    body,
                    "Combat training",
                    "Combat tip",
                    "Training notes are kept in the Crew Logbook."
            ));
        }
    }

    public void handleCadeAction(Player player, String action) {
        switch (action) {
            case "cade_quest" -> handleQuest(player);
            case "cade_tip" -> combatTip(player);
            case "cade_himself" -> aboutHimself(player);
            case "cade_crew" -> aboutCrew(player);
            default -> openDialogue(player);
        }
    }

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

    private String buildDialogueBody(PlayerProgress progress) {
        return switch (progress.questStage) {
            case 0 -> "You've been in a fight recently. You took more than you should have.\n"
                    + "Bring me two iron swords. I'll test them and I'll test you.";
            case 1 -> "Blaze rod. If you can reach a fortress, you can handle what's waiting for it.\n"
                    + "Come back when you have one.";
            case 2 -> "One wither skeleton skull. One is enough.\n"
                    + "Come back when you've been somewhere worth coming back from.";
            default -> "You've had everything I can give you. The rest is practice. You know where I am.";
        };
    }

    private void handleQuest(Player player) {
        PlayerProgress progress = getProgress(player);
        switch (progress.questStage) {
            case 0 -> ironSwordQuest(player, progress);
            case 1 -> blazeRodQuest(player, progress);
            case 2 -> skullQuest(player, progress);
            default -> say(player, "You've had everything I can give you. The rest is practice. You know where I am.");
        }
    }

    private void ironSwordQuest(Player player, PlayerProgress progress) {
        if (!hasItem(player, "minecraft:iron_sword", 2)) {
            say(player, "You've been in a fight recently. You took more than you should have. Bring me two iron swords. I'll test them and I'll test you.");
            return;
        }

        consumeItem(player, "minecraft:iron_sword", 2);
        progress.questStage = 1;
        addTrust(progress, 5);
        giveItemById(player, "minecraft:enchanted_book", 1);
        say(player, "These are fine. Here's what I'm going to show you. Pay attention. I don't repeat myself.");
    }

    private void blazeRodQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 15) {
            say(player, "Not yet. Need to know you'll follow through first.");
            return;
        }
        if (!hasItem(player, "minecraft:blaze_rod", 1)) {
            say(player, "Blaze rod. If you can reach a fortress, you can handle what's waiting for it. Come back when you have one.");
            return;
        }

        consumeItem(player, "minecraft:blaze_rod", 1);
        progress.questStage = 2;
        addTrust(progress, 8);
        giveItemById(player, "minecraft:enchanted_book", 1);
        giveItemById(player, "minecraft:enchanted_book", 1);
        say(player, "Good. You went and came back. Some people don't.");
    }

    private void skullQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 35) {
            say(player, "Not yet. This one takes someone I trust.");
            return;
        }
        if (!hasItem(player, "minecraft:wither_skeleton_skull", 1)) {
            say(player, "One wither skeleton skull. One is enough. Come back when you've been somewhere worth coming back from.");
            return;
        }

        consumeItem(player, "minecraft:wither_skeleton_skull", 1);
        ResourceLocation trainingId = ResourceLocation.fromNamespaceAndPath("ciskspawn", "cade_training");
        var attr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attr != null && !progress.trainingApplied) {
            if (attr.getModifier(trainingId) == null) {
                attr.addPermanentModifier(new AttributeModifier(trainingId, 0.1D, AttributeModifier.Operation.ADD_VALUE));
                progress.trainingApplied = true;
            }
        }
        progress.questStage = 3;
        addTrust(progress, 10);
        say(player, "You earned this.");
        say(player, "Don't waste it.");
    }

    private void combatTip(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 2);
        say(player, randomOf(
                "Hit first if you're going to hit. Hesitation costs more than initiative.",
                "Armor buys time. Time buys options. Don't confuse either one for safety.",
                "If you don't know what something does, don't stand in front of it.",
                "Retreat is a tactic. Running is a failure. Know the difference before you move.",
                "Fight what's in front of you. Think about what's behind you. Do both."
        ));
    }

    private void aboutHimself(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
        if (progress.trust >= 80) {
            say(player, "My commanding officer gave an order I couldn't follow. I told him I couldn't. He accepted that — which surprised me. He was a good officer. I wasn't a good soldier by the end. Different things.");
        } else if (progress.trust >= 55) {
            say(player, "The navy asks you to protect things that don't always deserve protecting. Eventually you run the numbers. I ran them.");
        } else if (progress.trust >= 35) {
            say(player, "I served five years. Didn't renew the contract. That's the short version.");
        } else {
            say(player, "I've been doing this long enough. That covers most of the questions.");
        }
    }

    private void aboutCrew(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
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
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        return true;
    }

    private boolean stackMatchesId(ItemStack stack, String itemId) {
        if (stack.isEmpty()) return false;
        return stack.getItem().builtInRegistryHolder().key().location().toString().equals(itemId);
    }

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
                "Check your footing near the north edge. Especially at night.",
                "Wind's shifted. Something's coming from the west.",
                "All clear. That's what I want to keep saying.",
                "Seen worse weather. Seen worse company. Currently prefer both.",
                "Still here."
        );
    }

    private PlayerProgress getProgress(Player player) {
        return progressByPlayer.computeIfAbsent(player.getUUID(), uuid -> new PlayerProgress());
    }

    public int getTrustForPlayer(UUID playerUUID) {
        return progressByPlayer.getOrDefault(playerUUID, new PlayerProgress()).trust;
    }

    private void addTrust(PlayerProgress progress, int amount) {
        progress.trust = Math.min(100, progress.trust + amount);
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
            playerTag.putBoolean("TrainingApplied", entry.getValue().trainingApplied);
            list.add(playerTag);
        }
        tag.put("CadeProgress", list);
        tag.putInt("AmbientCooldown", ambientCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        progressByPlayer.clear();
        if (tag.contains("AmbientCooldown")) ambientCooldown = tag.getInt("AmbientCooldown");
        if (tag.contains("CadeProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("CadeProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.questStage = playerTag.getInt("QuestStage");
                if (playerTag.contains("TrainingApplied")) progress.trainingApplied = playerTag.getBoolean("TrainingApplied");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<CadeEntity> state) {
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
        boolean trainingApplied = false;
    }
}
