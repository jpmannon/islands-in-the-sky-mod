package com.quickqwek.ciskspawn.entity;

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
import net.minecraft.world.level.block.state.BlockState;
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

/** Azerion Rook AZ Mk 9 - former Abalone crew warframe.
 *  Placeholder runtime implementation using temporary GeckoLib assets until the Sketchfab model is converted.
 *  Source model attribution note is included in AZERION_ATTRIBUTION.md.
 */
public class AzerionEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int artilleryStage = 0;
    private int drillsCompleted = 0;
    private int ambientCooldown = 20 * 154;
    private int speechTicksLeft = 0;

    public AzerionEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.35D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 9.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide) openDialogue(player);
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void openDialogue(Player player) {
        player.getPersistentData().putBoolean("ciskspawn_spoke_to_azerion", true);
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);

        if (!progress.hasMet) {
            progress.hasMet = true;
            String firstMeet = pickFirstMeeting(player);
            say(player, firstMeet);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Azerion Rook - AZ Mk 9",
                        firstMeet,
                        "Begin cannon drill",
                        "Operational query",
                        "First contact logged."
                ));
            }
            return;
        }

        String greeting = pickAzerionGreeting(progress, player.getName().getString());
        String body = "AZERION ROOK MODEL AZ MK 9\nHigh Caliber Munitions Autonomous Warframe Unit\n\n"
                + greeting + "\n\n"
                + "Former Abalone crew. Current status: retired, technically. Azerion disagrees with the definition.\n\n"
                + "Training focus: Create Big Cannons basics, safe loading, recoil discipline, targeting, and shipboard artillery etiquette.\n\n"
                + "Trust: " + progress.trust + "/100. Personal drill stage: " + progress.drillStage + "/4.\n"
                + "Global artillery stage: " + artilleryStage + "/4. Drills completed: " + drillsCompleted + ".";
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Azerion Rook - AZ Mk 9",
                    body,
                    "Begin cannon drill",
                    "Operational query",
                    "Create Big Cannons discipline interface."
            ));
        }
    }

    public void handleAzerionAction(Player player, String action) {
        switch (action) {
            case "azerion_drill" -> cannonDrill(player);
            case "azerion_query" -> operationalQuery(player);
            case "azerion_relationship" -> relationshipStatus(player);
            case "azerion_record" -> crewRecord(player);
            case "azerion_log" -> crewRecord(player);
            default -> openDialogue(player);
        }
    }

    private String pickFirstMeeting(Player player) {
        if (nearArtillery()) {
            return "You are standing near active maintenance equipment. Step back approximately two metres. Then we can speak. Designation — Azerion Rook, Model A-Z, Mk. 9 — High Caliber Munitions Autonomous Guardian Unit.";
        }
        return "You are new here. I do not have prior record of you. Designation — Azerion Rook, Model A-Z, Mk. 9 — High Caliber Munitions Autonomous Guardian Unit. I am stationed at this facility. What is your purpose here?";
    }

    private String pickAzerionGreeting(PlayerProgress progress, String playerName) {
        if (progress.trust >= 80) {
            return "You are here. Good. Come to the range when you are ready.";
        }
        if (progress.trust >= 55) {
            return "I have been anticipating your return. I have something to discuss.";
        }
        if (progress.trust >= 35) {
            return "You are here again. I find this... expected. In a positive sense. What do you need?";
        }
        if (progress.trust >= 15) {
            return "I recognize you. Your pattern of return is consistent. This is noted.";
        }
        return "You have returned. I have catalogued your prior interaction. State your purpose.";
    }

    private void cannonDrill(Player player) {
        PlayerProgress progress = getProgress(player);
        switch (progress.drillStage) {
            case 0 -> {
                say(player, "A cannon is a system. You must understand the system before you operate it. Nomenclature first. The barrel is the tube. The breech is the rear. The bore is the interior channel. The fuse is not decoration. Questions?");
                giveItemById(player, "minecraft:written_book", 1);
                addTrust(progress, 5);
                progress.drillStage = 1;
                artilleryStage = Math.max(artilleryStage, 1);
            }
            case 1 -> {
                if (progress.trust < 10) {
                    say(player, "Loading protocol requires additional observed discipline. Return after further interaction.");
                    return;
                }
                say(player, "Loading protocol. Powder is loaded before shot. This sequence is not negotiable. Reversing it is a notable error. Follow the sequence. Always.");
                addTrust(progress, 5);
                progress.drillStage = 2;
                artilleryStage = Math.max(artilleryStage, 2);
            }
            case 2 -> {
                if (progress.trust < 20) {
                    say(player, "Trajectory instruction requires additional trust calibration. Return later.");
                    return;
                }
                say(player, "Trajectory is mathematics. Elevation angle and barrel length determine range. Wind affects horizontal displacement. Your target does not care what you intended. Adjust for what is, not what you meant.");
                addTrust(progress, 5);
                progress.drillStage = 3;
                artilleryStage = Math.max(artilleryStage, 3);
            }
            case 3 -> {
                if (progress.trust < 30) {
                    say(player, "Single-round discharge clearance requires additional reliability data. Continue standard interaction.");
                    return;
                }
                say(player, "You are cleared for single-round discharge. Shipboard artillery etiquette: clear the zone, announce intent, discharge, report. Do not skip steps. You are now a cleared operator.");
                addTrust(progress, 10);
                drillsCompleted++;
                progress.drillStage = 4;
                artilleryStage = Math.max(artilleryStage, 4);
                player.displayClientMessage(Component.literal("§6[Logbook] §7Azerion has certified you for standard single-round cannon operation."), false);
            }
            default -> say(player, "You have completed the standard drill sequence. Live fire parameters are documented. Return if you have operational questions.");
        }
    }

    private void crewRecord(Player player) {
        say(player, "Crew record: Abalone defensive officer. Incident classification: Cliff Kite swarm. Regret index: unresolved. Loyalty: intact.");
    }

    private void operationalQuery(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
        say(player, randomOf(
                "Operational answer: powder before shot. This is not preference. This is sequence integrity.",
                "Recoil management begins before discharge. If you notice recoil after discharge, you have noticed it late.",
                "Range estimation is acceptable when documented. Guessing is estimation without discipline.",
                "The safest cannon is maintained, respected, and not used to settle arguments."
        ));
    }

    private void relationshipStatus(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(progress, 1);
        say(player, switch (trustTier(progress)) {
            case 4 -> "Crew classification accepted. I will not describe the emotional result. It is operationally significant.";
            case 3 -> "Trusted classification accepted. I have begun preparing records for your review.";
            case 2 -> "Friend classification is imprecise. It is also not incorrect.";
            case 1 -> "Acquaintance classification accepted. Your return pattern has been stable.";
            default -> "Stranger classification active. Continue observation.";
        });
    }

    private void say(Player player, String text) {
        this.setCustomName(Component.literal("§7Azerion§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        player.displayClientMessage(Component.literal("§7Azerion§7: " + text), false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (speechTicksLeft > 0) {
                speechTicksLeft--;
                if (speechTicksLeft == 0) {
                    this.setCustomNameVisible(false);
                    this.setCustomName(Component.literal("Azerion Rook - AZ Mk 9"));
                }
            }
            if (--ambientCooldown <= 0) {
                ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
                Player p = this.level().getNearestPlayer(this, 10.0D);
                if (p != null) {
                    say(p, pickAmbientLine(p));
                }
            }
        }
    }

    private String pickAmbientLine(Player player) {
        PlayerProgress progress = getProgress(player);
        if (progress.trust >= 55) {
            return randomOf(
                    "There are things in the logs I have not reviewed in some time. I am not certain why I have not reviewed them.",
                    "The maintenance logs go back nineteen years. I have not deleted a single entry."
            );
        }
        if (progress.trust >= 35) {
            return "The maintenance logs go back nineteen years. I have not deleted a single entry.";
        }
        if (progress.trust >= 15) {
            return randomOf(
                    "I have been operational for a considerable duration. I find I prefer mornings.",
                    "Cannon bore is clean. This is not a small thing."
            );
        }
        return randomOf(
                "Maintenance complete. All systems within acceptable tolerances.",
                "Wind from the northeast. Trajectory adjustment of approximately 1.4 degrees recommended."
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

    public void addTrustFromVelho(Player player, int amount) {
        addTrust(getProgress(player), amount);
    }

    private int trustTier(PlayerProgress progress) {
        if (progress.trust >= 80) return 4;
        if (progress.trust >= 55) return 3;
        if (progress.trust >= 35) return 2;
        if (progress.trust >= 15) return 1;
        return 0;
    }

    private boolean nearArtillery() {
        BlockPos center = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -8, -8), center.offset(8, 8, 8))) {
            BlockState state = this.level().getBlockState(pos);
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String id = key == null ? "" : key.toString();
            if (id.contains("cannon")) {
                return true;
            }
        }
        return false;
    }

    private boolean giveItemById(Player player, String itemId, int amount) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return false;
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id), amount);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        return true;
    }

    private String randomOf(String... lines) {
        return lines[this.random.nextInt(lines.length)];
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private <E extends GeoEntity> PlayState predicate(AnimationState<E> state) {
        if (state.isMoving()) state.getController().setAnimation(WALK_ANIM);
        else state.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, PlayerProgress> entry : progressByPlayer.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("Player", entry.getKey());
            playerTag.putInt("Trust", entry.getValue().trust);
            playerTag.putBoolean("HasMet", entry.getValue().hasMet);
            playerTag.putInt("DrillStage", entry.getValue().drillStage);
            list.add(playerTag);
        }
        tag.put("AzerionProgress", list);
        tag.putInt("ArtilleryStage", artilleryStage);
        tag.putInt("DrillsCompleted", drillsCompleted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        progressByPlayer.clear();
        if (tag.contains("AzerionProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("AzerionProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.drillStage = playerTag.getInt("DrillStage");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
        artilleryStage = tag.getInt("ArtilleryStage");
        drillsCompleted = tag.getInt("DrillsCompleted");
    }

    private static class PlayerProgress {
        int trust = 0;
        boolean hasMet = false;
        int drillStage = 0;
    }
}
