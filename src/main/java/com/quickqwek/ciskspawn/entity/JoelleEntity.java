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

public class JoelleEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;
    private BlockPos homePos = null;

    public JoelleEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
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
            say(player, "Hello. Sit down. I'll bring you something.");
            say(player, "No, I'm not asking what you want yet. I'll read you first.");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Joelle - Chef",
                        "Hello. Sit down. I'll bring you something.\n\nNo, I'm not asking what you want yet. I'll read you first.",
                        "Cooking quest",
                        "Ask for a recipe tip",
                        "First time meeting Joelle."
                ));
            }
            return;
        }

        String greeting = pickJoelleGreeting(progress);
        String body = greeting + "\n\n" + buildDialogueBody(progress);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Joelle - Chef",
                    body,
                    "Cooking quest",
                    "Ask for a recipe tip",
                    "Restaurant trust: " + progress.trust + "/100. Quest stage: " + Math.min(progress.questStage + 1, 3) + "/3."
            ));
        }
    }

    public void handleJoelleAction(Player player, String action) {
        switch (action) {
            case "joelle_quest" -> handleQuest(player);
            case "joelle_recipe" -> recipeTip(player);
            case "joelle_about" -> aboutRestaurant(player);
            default -> openDialogue(player);
        }
    }

    private String pickJoelleGreeting(PlayerProgress progress) {
        if (progress.trust >= 80) return randomOf(
                "Oh good, it's you. I made too much of everything. Sit.",
                "Come in, come in. I've been waiting for someone with taste to try this.",
                "You. Good. I need a second opinion and you are the only one I trust to give an honest one."
        );
        if (progress.trust >= 55) return randomOf(
                "I've been saving something for you. Come in.",
                "Sit down. I have been thinking about the next dish and I want to talk it through.",
                "There you are. I set aside a portion. I was not going to, and then I did."
        );
        if (progress.trust >= 35) return randomOf(
                "There you are. I was wondering if you'd come back at the right time.",
                "The timing is right today. That matters more than people admit.",
                "Good. I need someone around who doesn't treat the kitchen like a hallway."
        );
        if (progress.trust >= 15) return randomOf(
                "Back again. The soup is better today than yesterday. I planned it that way.",
                "Come in. I was just starting something — you can watch if you are quiet about it.",
                "Oh. You're back. Good. The last batch needed a witness and you missed it."
        );
        return randomOf(
                "Take your time. Everyone gets a moment to breathe here.",
                "Sit wherever. I'll come to you when I have a second.",
                "Don't apologize for looking lost — everyone does the first time."
        );
    }

    private String buildDialogueBody(PlayerProgress progress) {
        return switch (progress.questStage) {
            case 0 -> "The first thing this place needs is brightness. Bring me five cloudberries. Wild berries will do if the sky refuses to be poetic.";
            case 1 -> "There's a plant that grows on island undersides. Silver leaves. If you find it, bring it to me before it wilts.";
            case 2 -> "I am making something from my grandmother's recipe. I have never made it before. Come back after I have watched it misbehave a little longer.";
            default -> "The restaurant is open, the fire is behaving, and no one has insulted the soup today. Excellent conditions.";
        };
    }

    private void handleQuest(Player player) {
        PlayerProgress progress = getProgress(player);
        switch (progress.questStage) {
            case 0 -> {
                if (hasItem(player, "farmersdelight:wild_berries", 5)) {
                    consumeItem(player, "farmersdelight:wild_berries", 5);
                    completeBerryQuest(player, progress);
                } else if (hasItem(player, "minecraft:sweet_berries", 5)) {
                    consumeItem(player, "minecraft:sweet_berries", 5);
                    completeBerryQuest(player, progress);
                } else {
                    say(player, "Five cloudberries. Or sweet berries, if the island is being difficult. I can work with stubborn ingredients.");
                }
            }
            case 1 -> {
                if (hasItem(player, "minecraft:glow_berries", 1)) {
                    consumeItem(player, "minecraft:glow_berries", 1);
                    progress.questStage = 2;
                    progress.recipeRevisits = 0;
                    addTrust(progress, 5);
                    say(player, "Silver enough. Fresh enough. Good. Now I need to find out if my memory is lying to me.");
                } else {
                    say(player, "Silver leaves from an island underside. Glow berries are close enough for now, if the sky has not given you the real thing yet.");
                }
            }
            case 2 -> {
                progress.recipeRevisits++;
                if (progress.recipeRevisits >= 3) {
                    progress.questStage = 3;
                    addTrust(progress, 5);
                    say(player, "There. Grandmother would say it needs one more impossible thing. I say it is finished. Eat while it is still brave.");
                    giveMeal(player);
                } else {
                    say(player, "Not yet. This recipe listens slowly. Come back once more, and then perhaps once more after that.");
                }
            }
            default -> say(player, "You've helped enough for one restaurant to feel less like a room and more like a promise.");
        }
    }

    private void completeBerryQuest(Player player, PlayerProgress progress) {
        progress.questStage = 1;
        addTrust(progress, 5);
        say(player, "Good. Bright, sharp, and a little rude. Perfect berries.");
        giveMeal(player);
    }

    private void recipeTip(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 2);
        say(player, randomOf(
                "Salt early if you want depth. Salt late if you want apology.",
                "Soup is patience with a spoon in it.",
                "Never crowd a pan unless you are trying to steam your own disappointment.",
                "Taste before serving. Taste again if you are nervous. Especially if you are nervous."
        ));
    }

    private void aboutRestaurant(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
        say(player, "Ramone handles the garden because he understands quiet things. I handle the kitchen because I understand hungry ones.");
    }

    private void giveMeal(Player player) {
        if (giveItemById(player, "farmersdelight:beef_stew", 1)) return;
        if (giveItemById(player, "farmersdelight:vegetable_soup", 1)) return;
        if (giveItemById(player, "minecraft:mushroom_stew", 1)) return;
        giveItemById(player, "minecraft:cooked_beef", 1);
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
        this.setCustomName(Component.literal("§dJoelle§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        player.displayClientMessage(Component.literal("§dJoelle§7: " + text), false);
    }

    private void sayNearby(String text) {
        this.setCustomName(Component.literal("§dJoelle§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        Component chat = Component.literal("§dJoelle§7: " + text);
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
                this.setCustomName(Component.literal("Joelle - Chef"));
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
                "The garden's early this season.",
                "Grandmother always said the secret was attention. She was right.",
                "The cloudberries are peaking. Come by before the end of the week.",
                "Ramone's been at the east beds since sunrise.",
                "A good meal is a conversation. The cook speaks first.",
                "There's always one dish that won't do what you want. Mine's been the same one for eleven years."
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
            playerTag.putInt("RecipeRevisits", entry.getValue().recipeRevisits);
            list.add(playerTag);
        }
        tag.put("JoelleProgress", list);
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
        if (tag.contains("JoelleProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("JoelleProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.questStage = playerTag.getInt("QuestStage");
                progress.recipeRevisits = playerTag.getInt("RecipeRevisits");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<JoelleEntity> state) {
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
        int recipeRevisits = 0;
    }
}
