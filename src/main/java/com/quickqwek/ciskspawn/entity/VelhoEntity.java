package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.ai.NpcAnchorReturnGoal;
import com.quickqwek.ciskspawn.ai.NpcShipStabilityGoal;
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

public class VelhoEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;
    private BlockPos homePos = null;

    public VelhoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
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
        this.goalSelector.addGoal(0, new NpcShipStabilityGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new NpcAnchorReturnGoal(this, () -> this.homePos, 0.55D, 12.0F));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.40D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new NpcWaypointGoal(this, 0.45D));
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
            progress.beatOneShown = true;
            say(player, "Not now.");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Velho - Enchanter",
                        "Not now.",
                        "Wait",
                        "Wait",
                        "He has not looked up."
                ));
            }
            return;
        }

        addTrust(progress, 1);
        String greeting = pickVelhoGreeting(progress);
        String body = greeting + "\n\n" + buildDialogueBody(progress);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Velho - Enchanter",
                    body,
                    "Enchanting work",
                    "Enchanting tip",
                    "Workshop notes are kept in the Crew Logbook."
            ));
        }
    }

    public void handleVelhoAction(Player player, String action) {
        switch (action) {
            case "velho_wait" -> finishFirstMeeting(player);
            case "velho_quest" -> handleQuest(player);
            case "velho_enchanting" -> enchantingTip(player);
            case "velho_azerion" -> azerionDialogue(player);
            default -> openDialogue(player);
        }
    }

    private void finishFirstMeeting(Player player) {
        PlayerProgress progress = getProgress(player);
        if (!progress.hasMet) {
            progress.beatOneShown = true;
            progress.hasMet = true;
            addTrust(progress, 1);
            String body = "...You're still here.\n\nHm. Persistent. That's either the best thing you can be or the worst, depending on what you're being persistent about. Velho.";
            say(player, "...You're still here.");
            say(player, "Hm. Persistent. That's either the best thing you can be or the worst, depending on what you're being persistent about. Velho.");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Velho - Enchanter",
                        body,
                        "Enchanting work",
                        "Enchanting tip",
                        "First conversation completed."
                ));
            }
            return;
        }
        openDialogue(player);
    }

    private String pickVelhoGreeting(PlayerProgress progress) {
        if (progress.trust >= 80) return randomOf(
                "Oh. You. Yes. I need eyes on something. Come look at this.",
                "Good. I was going to come find you and decided against it. This is better.",
                "You. Sit. I have something finished and I want to describe it to someone who will actually follow along."
        );
        if (progress.trust >= 55) return randomOf(
                "Sit down. I need to tell you something about what I've been working on.",
                "Come in. I've been wrong about something for three weeks and I've only just noticed.",
                "Good timing. I need to say something out loud so I can hear whether it's right."
        );
        if (progress.trust >= 35) return randomOf(
                "Good. I was just thinking about something you should hear.",
                "You're here. That saves me the effort of finding you.",
                "Come in. I have one question for you and I need a direct answer."
        );
        if (progress.trust >= 15) return randomOf(
                "Come in. Mind the left table — that's organized.",
                "You came back. People often don't. Sit down if you want.",
                "The right table. You can touch anything on the right table."
        );
        return randomOf(
                "You again. Good. I have a question for you, actually.",
                "Don't move anything. What do you need.",
                "I'm busy. So are you, probably. Say what you came to say."
        );
    }

    private String buildDialogueBody(PlayerProgress progress) {
        return switch (progress.questStage) {
            case 0 -> "Lapis first. No lapis, no enchanting. This is not a suggestion, this is how the magic works.";
            case 1 -> "The ninth one I made was the most careful work I've done. You should go talk to Azerion. Don't ask him about his maintenance logs. Ask him what he finds preferable.";
            case 2 -> "Bring Azerion here. Within eight blocks, ideally. This is not dramatic. It is overdue.";
            default -> "The workbench is quiet. That usually means either success or a terrible delay before consequences.";
        };
    }

    private void handleQuest(Player player) {
        PlayerProgress progress = getProgress(player);
        switch (progress.questStage) {
            case 0 -> firstRulesQuest(player, progress);
            case 1 -> whatIMadeQuest(player, progress);
            case 2 -> conversationQuest(player, progress);
            default -> say(player, "No. Nothing else. Well, dozens of things, obviously. Nothing for you yet.");
        }
    }

    private void firstRulesQuest(Player player, PlayerProgress progress) {
        if (!hasItem(player, "minecraft:lapis_lazuli", 5)) {
            say(player, "Five lapis lazuli. Lapis first. No lapis, no enchanting. This is not a suggestion, this is how the magic works.");
            return;
        }
        consumeItem(player, "minecraft:lapis_lazuli", 5);
        progress.questStage = 1;
        addTrust(progress, 5);
        say(player, "Good. Now the table has something to answer with. It answers badly, often. That is why we learn rules.");
        giveItemById(player, "minecraft:book", 1);
    }

    private void whatIMadeQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 15) {
            say(player, "Not yet. You need context before I give you the part that matters.");
            return;
        }
        if (!player.getPersistentData().getBoolean("ciskspawn_spoke_to_azerion")) {
            say(player, "Go talk to Azerion. Don't ask about his maintenance logs. Ask him what he finds preferable.");
            return;
        }
        progress.questStage = 2;
        addTrust(progress, 5);
        say(player, "Yes. That was the answer, then. He has preferences. Good. Of course he does.");
    }

    private void conversationQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 55) {
            say(player, "No. This needs trust. Not the sentimental kind. The useful kind.");
            return;
        }
        AzerionEntity azerion = findNearbyAzerion();
        if (azerion == null) {
            say(player, "Bring Azerion here. Eight blocks is enough. Closer if he will tolerate it.");
            return;
        }
        progress.questStage = 3;
        addTrust(progress, 15);
        azerion.addTrustFromVelho(player, 10);
        sayToAzerion(player, "I've been meaning to tell you something.");
        sayToAzerion(player, "...I know your tolerances. Better than you do. I've never said.");
        sayToAzerion(player, "I should have said.");
    }

    private AzerionEntity findNearbyAzerion() {
        for (AzerionEntity azerion : this.level().getEntitiesOfClass(AzerionEntity.class, this.getBoundingBox().inflate(8.0D))) {
            if (azerion.distanceTo(this) <= 8.0F) return azerion;
        }
        return null;
    }

    private void enchantingTip(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 2);
        say(player, randomOf(
                "Enchanting is negotiation. The table wants shape. Lapis gives it grammar.",
                "Do not enchant your only tool unless you already know what mistake you are willing to live with.",
                "Books are useful because a failed idea can sit on a shelf instead of ruining your boots.",
                "Experience is not fuel. It is memory with edges. Spend it carefully."
        ));
    }

    private void azerionDialogue(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
        if (progress.trust >= 55) {
            say(player, "He has maintenance logs going back nineteen years. He's never deleted an entry. I know what that means. I haven't told him I know.");
        } else if (progress.trust >= 15) {
            say(player, "I made him. He knows this. We've never discussed what that means.");
        } else {
            say(player, "He works here. He's been here a long time.");
        }
    }

    private void sayToAzerion(Player player, String text) {
        this.setCustomName(Component.literal("§5Velho§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        Component chat = Component.literal("§5Velho§7 to Azerion: " + text);
        player.displayClientMessage(chat, false);
        for (Player nearby : this.level().players()) {
            if (nearby != player && nearby.distanceTo(this) <= 12.0F) nearby.sendSystemMessage(chat);
        }
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
        this.setCustomName(Component.literal("§5Velho§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        player.displayClientMessage(Component.literal("§5Velho§7: " + text), false);
    }

    private void sayNearby(String text) {
        this.setCustomName(Component.literal("§5Velho§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        Component chat = Component.literal("§5Velho§7: " + text);
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
                this.setCustomName(Component.literal("Velho - Enchanter"));
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
        if (progress.trust >= 35 && this.random.nextFloat() < 0.25F) {
            return "Azerion has been here nineteen years. I was there for the first hour. The rest was him.";
        }
        if (progress.trust >= 15 && this.random.nextFloat() < 0.25F) {
            return "Cap'n's ship was named after a food. This is not as strange as it sounds.";
        }
        return randomOf(
                "Give me a moment.",
                "Something almost worked just now. I need to understand why it almost worked.",
                "The difference between a mechanism and a living thing is smaller than people assume.",
                "Nine complete. Each one different. Each one—"
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
            playerTag.putBoolean("BeatOneShown", entry.getValue().beatOneShown);
            list.add(playerTag);
        }
        tag.put("VelhoProgress", list);
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
        if (tag.contains("VelhoProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("VelhoProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.questStage = playerTag.getInt("QuestStage");
                if (playerTag.contains("BeatOneShown")) progress.beatOneShown = playerTag.getBoolean("BeatOneShown");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<VelhoEntity> state) {
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
        boolean beatOneShown = false;
    }
}
