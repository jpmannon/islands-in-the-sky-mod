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

// NOTE: Register a LivingDropsEvent listener in your EventBusSubscriber class:
// If the dying entity is a Player AND the player has "ciskspawn_has_soul_anchor" == true
// in getPersistentData(), call event.setCanceled(true) to suppress all drops.
// The soul_anchor item itself is already in the inventory and will be retained.
public class TarnEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;
    private BlockPos homePos = null;

    public TarnEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
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
            progress.hasMet = true;
            addTrust(progress, 1);
            String body = "Sit down. I'll take a look before you tell me what's wrong — you always give it "
                    + "away before you open your mouth anyway.\n\nTarn. Ship's physician. Don't be "
                    + "alarmed by the herbs. They smell strange but they work.";
            say(player, "Sit down. I'll take a look before you tell me what's wrong — you always give it away before you open your mouth anyway.");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Tarn - Healer",
                        body,
                        "Healing work",
                        "Ask about herself",
                        "She looked you over before you finished walking in."
                ));
            }
            return;
        }

        addTrust(progress, 1);
        String greeting = pickTarnGreeting(progress);
        String body = greeting + "\n\n" + buildDialogueBody(progress);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Tarn - Healer",
                    body,
                    "Healing work",
                    "Ask about herself",
                    "Medical notes are kept in the Crew Logbook."
            ));
        }
    }

    public void handleTarnAction(Player player, String action) {
        switch (action) {
            case "tarn_quest" -> handleQuest(player);
            case "tarn_herself" -> aboutHerself(player);
            case "tarn_tip" -> medicineTip(player);
            case "tarn_crew" -> aboutCrew(player);
            default -> openDialogue(player);
        }
    }

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

    private String buildDialogueBody(PlayerProgress progress) {
        return switch (progress.questStage) {
            case 0 -> "You've got the look. The 'I dropped everything' look. I can fix that.\n"
                    + "Bring me: 4 string, 2 echo shards, 1 amethyst shard. I'll make you something that holds.";
            case 1 -> "There's something I can do for people I trust with the knowledge. It's not magic — "
                    + "it's closer to medicine, but not the kind most people practice. It won't hurt. Much.\n"
                    + "Bring me: 1 golden apple, 2 ghast tears, 3 blaze powder.";
            default -> "You're welcome. Don't ask for more — that's the limit of what I can safely do "
                    + "without understanding your full history. You don't want me guessing.";
        };
    }

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

    private void handleQuest(Player player) {
        PlayerProgress progress = getProgress(player);
        switch (progress.questStage) {
            case 0 -> soulAnchorQuest(player, progress);
            case 1 -> strongerQuest(player, progress);
            default -> say(player, "You're welcome. Don't ask for more — that's the limit of what I can safely do without understanding your full history. You don't want me guessing.");
        }
    }

    private void soulAnchorQuest(Player player, PlayerProgress progress) {
        if (!hasItem(player, "minecraft:string", 4)
                || !hasItem(player, "minecraft:echo_shard", 2)
                || !hasItem(player, "minecraft:amethyst_shard", 1)) {
            say(player, "You've got the look. The 'I dropped everything' look. I can fix that. Bring me: 4 string, 2 echo shards, 1 amethyst shard. I'll make you something that holds.");
            return;
        }

        consumeItem(player, "minecraft:string", 4);
        consumeItem(player, "minecraft:echo_shard", 2);
        consumeItem(player, "minecraft:amethyst_shard", 1);
        progress.questStage = 1;
        addTrust(progress, 8);
        player.getPersistentData().putBoolean("ciskspawn_has_soul_anchor", true);
        if (!giveItemById(player, "ciskspawn:soul_anchor", 1)) giveItemById(player, "minecraft:amethyst_shard", 1);
        say(player, "There. Soul Anchor. Keep it in your inventory — don't vault it, don't drop it, don't hand it to someone else for safekeeping. It only works for the person it was made for.");
        say(player, "Don't ask me how I know so much about anchoring things. I have personal reasons.");
    }

    private void strongerQuest(Player player, PlayerProgress progress) {
        if (progress.trust < 35) {
            say(player, "Not yet. I need to trust you with the knowledge before I trust you with the treatment.");
            return;
        }
        if (!hasItem(player, "minecraft:golden_apple", 1)
                || !hasItem(player, "minecraft:ghast_tear", 2)
                || !hasItem(player, "minecraft:blaze_powder", 3)) {
            say(player, "There's something I can do for people I trust with the knowledge. It's not magic — it's closer to medicine, but not the kind most people practice. It won't hurt. Much. Bring me: 1 golden apple, 2 ghast tears, 3 blaze powder.");
            return;
        }

        consumeItem(player, "minecraft:golden_apple", 1);
        consumeItem(player, "minecraft:ghast_tear", 2);
        consumeItem(player, "minecraft:blaze_powder", 3);
        ResourceLocation strongerId = ResourceLocation.fromNamespaceAndPath("ciskspawn", "tarn_stronger");
        var attr = player.getAttribute(Attributes.MAX_HEALTH);
        if (attr != null) {
            if (attr.getModifier(strongerId) == null) {
                attr.addPermanentModifier(new AttributeModifier(strongerId, 4.0D, AttributeModifier.Operation.ADD_VALUE));
                player.setHealth(player.getMaxHealth());
            }
        }
        progress.questStage = 2;
        progress.strongerApplied = true;
        addTrust(progress, 10);
        say(player, "Hold still. This takes about thirty seconds and you're going to feel it settle. That's normal. Don't panic about the warmth — that's just your body deciding what to do with the extra room.");
        say(player, "There. Two extra hearts, give or take. You're welcome. Don't waste them on something stupid within the first hour — I say this from experience watching people.");
    }

    private void medicineTip(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 2);
        say(player, randomOf(
                "Hunger is the most underrated wound. Fix it first.",
                "If it's swollen and it shouldn't be, the answer is almost always time and elevation.",
                "Potions are quick. Quick isn't always good. Know the difference.",
                "I have never lost a patient to something I caught early enough. I've lost a few to overconfidence. Theirs, not mine.",
                "Pain is information. Stopping pain without knowing what caused it is just reading half the message."
        ));
    }

    private void aboutCrew(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
        say(player, randomOf(
                "Mortimer's been kind to me. People like that — you want to protect them without telling them you're doing it.",
                "Geera notices things almost as quickly as I do. We've never discussed what that means about either of us.",
                "Azerion finds things preferable. The list of entities with preferences is shorter than people think.",
                "Velho spent nineteen years watching what he made become something he didn't design. I think he finds it wonderful. He'd never say that.",
                "Scoria asks good questions. She doesn't always like the answers, but she asks anyway. That's worth something.",
                "Joelle feeds people and calls it cooking. Ramone grows things and calls it gardening. Both of them are doing something else entirely."
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

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;

        if (speechTicksLeft > 0) {
            speechTicksLeft--;
            if (speechTicksLeft == 0) {
                this.setCustomNameVisible(false);
                this.setCustomName(Component.literal("Tarn - Healer"));
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
                "The herbs are doing something they weren't doing yesterday. I'm watching it.",
                "If you've been putting off something that hurts, come back later. I'll be here.",
                "I have a good memory for injuries. Don't take that as a threat.",
                "Something on the eastern approach smells like rain coming. Prepare accordingly.",
                "I've been in worse places. This one has good light."
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
            playerTag.putBoolean("StrongerApplied", entry.getValue().strongerApplied);
            list.add(playerTag);
        }
        tag.put("TarnProgress", list);
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
        if (tag.contains("TarnProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("TarnProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.questStage = playerTag.getInt("QuestStage");
                if (playerTag.contains("StrongerApplied")) progress.strongerApplied = playerTag.getBoolean("StrongerApplied");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<TarnEntity> state) {
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
        boolean strongerApplied = false;
    }
}
