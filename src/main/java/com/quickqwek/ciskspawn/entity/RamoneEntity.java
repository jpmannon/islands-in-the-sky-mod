package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.ai.NpcAnchorReturnGoal;
import com.quickqwek.ciskspawn.ai.NpcWaypointGoal;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.core.BlockPos;
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

public class RamoneEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;
    private BlockPos homePos = null;

    public RamoneEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.17D)
                .add(Attributes.FOLLOW_RANGE, 18.0D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    public void setHomePos(BlockPos pos) {
        this.homePos = pos;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new NpcAnchorReturnGoal(this, () -> this.homePos, 0.55D, 12.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.40D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new NpcWaypointGoal(this, 0.45D));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide) openDialogue(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void openDialogue(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);

        if (!progress.hasMet) {
            progress.hasMet = true;
            say(player, "Didn't hear you come in. Ramone. I'm usually in the back — Joelle handles most of the front.");
            say(player, "You here to eat, or something else?");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Ramone - Gardener",
                        "Didn't hear you come in. Ramone. I'm usually in the back — Joelle handles most of the front.\n\nYou here to eat, or something else?",
                        "Garden work",
                        "Garden tip",
                        "First time meeting Ramone."
                ));
            }
            return;
        }

        String greeting = pickRamoneGreeting(progress);
        String body = greeting + "\n\n" + buildDialogueBody(progress);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Ramone - Gardener",
                    body,
                    "Garden work",
                    "Garden tip",
                    "Garden notes are kept in the Crew Logbook."
            ));
        }
    }

    public void handleRamoneAction(Player player, String action) {
        switch (action) {
            case "ramone_quest" -> handleQuest(player);
            case "ramone_garden" -> gardenTip(player);
            case "ramone_sky" -> skyDialogue(player);
            default -> openDialogue(player);
        }
    }

    private String pickRamoneGreeting(PlayerProgress progress) {
        if (progress.trust >= 80) return randomOf(
                "You're here. Good. I've got something to show you.",
                "Come round. I've been watching something in the east bed and I want a second pair of eyes.",
                "I was just thinking about you. The garden has been doing something unusual."
        );
        if (progress.trust >= 55) return randomOf(
                "Good timing. I was going to find you, actually.",
                "Come in from the path. I've got a few minutes and something to say.",
                "You're back. Good. There's something I've been saving for the right moment."
        );
        if (progress.trust >= 35) return randomOf(
                "Oh, you again. Come round the back — easier to talk out there.",
                "Good. I was getting tired of the plants being the only company.",
                "Glad you came. There's a question I have been turning over."
        );
        if (progress.trust >= 15) return randomOf(
                "Back. The south beds are looking good today.",
                "You came back. The compost is working. Two good signs.",
                "Morning. Or close enough. The east side's already busy."
        );
        return randomOf(
                "Ramone. Garden's in back. Joelle's inside if you need food.",
                "If you're here for the restaurant, Joelle's inside. If not, I'm out here.",
                "Don't touch the south row. Everything else, ask first."
        );
    }

    private String buildDialogueBody(PlayerProgress progress) {
        return switch (progress.questStage) {
            case 0 -> "The soil needs work. Five dirt or coarse dirt. Three bonemeal. Nothing fancy. Just useful.";
            case 1 -> "There's a plant that needs specific light. Island with no overhead cover, morning sun. You going anywhere like that?";
            case 2 -> "Southeast. Low approach. There's a marker. Take us there, then bring me a compass after. I will know what it means.";
            default -> "Garden's settled. Joelle's happy with the yield. That is enough for one season.";
        };
    }

    private void handleQuest(Player player) {
        PlayerProgress progress = getProgress(player);
        switch (progress.questStage) {
            case 0 -> soilQuest(player, progress);
            case 1 -> skyPlantQuest(player, progress);
            case 2 -> skyEscortQuest(player, progress);
            default -> say(player, "Nothing urgent. That is not the same thing as nothing to do.");
        }
    }

    private void soilQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 15) {
            say(player, "Not yet. Need to know you will finish what you start.");
            return;
        }
        int dirt = countItem(player, "minecraft:dirt");
        int coarse = countItem(player, "minecraft:coarse_dirt");
        if (dirt + coarse < 5 || !hasItem(player, "minecraft:bone_meal", 3)) {
            say(player, "Five dirt or coarse dirt. Three bonemeal. Soil first. Always soil first.");
            return;
        }
        consumeFlexibleDirt(player, 5);
        consumeItem(player, "minecraft:bone_meal", 3);
        progress.questStage = 1;
        addTrust(progress, 5);
        say(player, "Good. This will hold roots. Most things start there.");
        giveSeedReward(player);
    }

    private void skyPlantQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 35) {
            say(player, "Later. This needs someone I know a little better.");
            return;
        }
        if (!hasItem(player, "minecraft:spore_blossom", 1)) {
            say(player, "High island. No overhead cover. Morning sun. Bring the plant back alive if you can.");
            return;
        }
        consumeItem(player, "minecraft:spore_blossom", 1);
        progress.questStage = 2;
        addTrust(progress, 5);
        say(player, "That is the one. Light-starved, stubborn. Useful plant.");
        giveRareSeedReward(player);
    }

    private void skyEscortQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 55) {
            say(player, "Not yet. Need to trust the hands on the wheel first.");
            return;
        }
        if (progress.hasCompletedSkyQuest) {
            say(player, "She cried a little on the way back. She didn't want me to notice. I noticed.");
            return;
        }
        if (!hasItem(player, "minecraft:compass", 1)) {
            say(player, "Southeast. Low approach. There's a marker. Bring the compass back when you've been there.");
            return;
        }
        progress.hasCompletedSkyQuest = true;
        progress.questStage = 3;
        addTrust(progress, 10);
        say(player, "She cried a little on the way back. She didn't want me to notice. I noticed.");
    }

    private void gardenTip(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 2);
        say(player, randomOf(
                "Turn soil when it is damp, not wet. Wet soil remembers the insult.",
                "Compost needs air. So do people, usually.",
                "If a plant is failing, look at the light before you blame the plant.",
                "Seeds do not care about your schedule. That is their best quality."
        ));
    }

    private void skyDialogue(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
        say(player, switch (trustTier(progress)) {
            case 0 -> "The sky? It's there. It does its job.";
            case 1 -> "I used to know it well. Different now. Not worse. Just different.";
            case 2 -> "I don't regret coming down. I want to be clear about that.";
            default -> "There was a voyage. The last one. We almost didn't come back. Not because of anyone's fault — the sky just asked for more than we had. Joelle cried when she saw what it cost. I couldn't see her like that again. So I came down. I'd make the same choice.";
        });
    }

    private int trustTier(PlayerProgress progress) {
        if (progress.trust >= 55) return 3;
        if (progress.trust >= 35) return 2;
        if (progress.trust >= 15) return 1;
        return 0;
    }

    private void giveSeedReward(Player player) {
        if (giveItemById(player, "farmersdelight:cabbage_seeds", 3)) return;
        if (giveItemById(player, "farmersdelight:tomato_seeds", 3)) return;
        giveItemById(player, "minecraft:wheat_seeds", 4);
    }

    private void giveRareSeedReward(Player player) {
        if (giveItemById(player, "farmersdelight:tomato_seeds", 2)) return;
        if (giveItemById(player, "farmersdelight:cabbage_seeds", 2)) return;
        giveItemById(player, "minecraft:pumpkin_seeds", 2);
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

    private void consumeFlexibleDirt(Player player, int amount) {
        int remaining = consumeItemCount(player, "minecraft:dirt", amount);
        if (remaining > 0) consumeItemCount(player, "minecraft:coarse_dirt", remaining);
    }

    private void consumeItem(Player player, String itemId, int amount) {
        consumeItemCount(player, itemId, amount);
    }

    private int consumeItemCount(Player player, String itemId, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stackMatchesId(stack, itemId)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
            if (remaining <= 0) break;
        }
        return remaining;
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
        this.setCustomName(Component.literal("§2Ramone§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        player.displayClientMessage(Component.literal("§2Ramone§7: " + text), false);
    }

    private void sayNearby(String text) {
        this.setCustomName(Component.literal("§2Ramone§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        Component chat = Component.literal("§2Ramone§7: " + text);
        for (Player player : this.level().players()) {
            if (player.distanceTo(this) <= 12.0F) player.sendSystemMessage(chat);
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
                this.setCustomName(Component.literal("Ramone - Gardener"));
            }
        }

        if (ambientCooldown > 0) {
            ambientCooldown--;
            return;
        }

        Player nearby = this.level().getNearestPlayer(this, 8.0D);
        if (nearby != null) sayNearby(pickAmbientLine(getProgress(nearby)));
        ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
    }

    private String pickAmbientLine(PlayerProgress progress) {
        if (progress.trust >= 15 && this.random.nextFloat() < 0.2F) {
            return "I miss the cold up high sometimes. Just the cold.";
        }
        return randomOf(
                "East beds are coming up. Better than I hoped.",
                "There's a proper way to turn soil. Most people don't know it.",
                "The cloudroot took this season. First time I've gotten it right.",
                "Joelle says this batch is different. She's right. I changed the compost ratio.",
                "Ship came through this morning. Interesting hull configuration.",
                "Joelle's making something new. I can tell by the sound the kitchen makes."
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
        if (homePos != null) {
            tag.putInt("HomePosX", homePos.getX());
            tag.putInt("HomePosY", homePos.getY());
            tag.putInt("HomePosZ", homePos.getZ());
        }
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerProgress> entry : progressByPlayer.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            playerTag.putInt("Trust", entry.getValue().trust);
            playerTag.putBoolean("HasMet", entry.getValue().hasMet);
            playerTag.putInt("QuestStage", entry.getValue().questStage);
            playerTag.putBoolean("HasCompletedSkyQuest", entry.getValue().hasCompletedSkyQuest);
            list.add(playerTag);
        }
        tag.put("RamoneProgress", list);
        tag.putInt("AmbientCooldown", ambientCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomePosX")) {
            homePos = new BlockPos(
                    tag.getInt("HomePosX"),
                    tag.getInt("HomePosY"),
                    tag.getInt("HomePosZ")
            );
        }
        progressByPlayer.clear();
        if (tag.contains("AmbientCooldown")) ambientCooldown = tag.getInt("AmbientCooldown");
        if (tag.contains("RamoneProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("RamoneProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.questStage = playerTag.getInt("QuestStage");
                progress.hasCompletedSkyQuest = playerTag.getBoolean("HasCompletedSkyQuest");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<RamoneEntity> state) {
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
        boolean hasCompletedSkyQuest = false;
    }
}
