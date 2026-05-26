package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

/** Azerion Rook AZ Mk 9 - former Abalone crew warframe.
 *  Placeholder runtime implementation using temporary GeckoLib assets until the Sketchfab model is converted.
 *  Source model attribution note is included in AZERION_ATTRIBUTION.md.
 */
public class AzerionEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("animation");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int artilleryStage = 0;
    private int drillsCompleted = 0;
    private int ambientCooldown = 20 * 154;

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
        String body = "AZERION ROOK MODEL AZ MK 9\nHigh Caliber Munitions Autonomous Warframe Unit\n\n"
                + "Former Abalone crew. Current status: retired, technically. Azerion disagrees with the definition.\n\n"
                + "Training focus: Create Big Cannons basics, safe loading, recoil discipline, targeting, and shipboard artillery etiquette.\n\n"
                + "CBC drill stage: " + artilleryStage + "/4. Drills completed: " + drillsCompleted + ".";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Azerion Rook - AZ Mk 9",
                    body,
                    "Begin cannon drill",
                    "Crew record",
                    "Prototype Azerion implementation. Model conversion pending."
            ));
        }
    }

    public void handleAzerionAction(Player player, String action) {
        switch (action) {
            case "azerion_drill" -> cannonDrill(player);
            case "azerion_record" -> crewRecord(player);
            case "azerion_log" -> crewRecord(player);
            default -> openDialogue(player);
        }
    }

    private void cannonDrill(Player player) {
        drillsCompleted++;
        switch (artilleryStage) {
            case 0 -> say(player, "DRILL ONE: A cannon is not a decoration. Identify barrel, breech, loader, and safe rear clearance before touching powder.");
            case 1 -> say(player, "DRILL TWO: Recoil is the cannon's opinion of your preparation. Secure the mount before pride becomes shrapnel.");
            case 2 -> say(player, "DRILL THREE: Ammunition selection defines intent. Munition discipline is crew discipline.");
            case 3 -> say(player, "DRILL FOUR: Shipboard artillery is mathematics wearing thunder. Account for motion, wind, and panic.");
            default -> say(player, "CBC proficiency acceptable. Mortimer would call that 'less likely to remove your eyebrows.'");
        }
        artilleryStage = Math.min(4, artilleryStage + 1);
    }

    private void crewRecord(Player player) {
        say(player, "Crew record: Abalone defensive officer. Incident classification: Cliff Kite swarm. Regret index: unresolved. Loyalty: intact.");
    }

    private void say(Player player, String text) {
        player.displayClientMessage(Component.literal("§8Azerion§7: " + text), false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (--ambientCooldown <= 0) {
                ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
                Player p = this.level().getNearestPlayer(this, 10.0D);
                if (p != null) {
                    String[] lines = new String[] {
                            "Safety interlock thought for the day: do not stand behind the cannon.",
                            "Mortimer called this retirement. I detected no reduction in combat readiness.",
                            "Create Big Cannons lesson pending: respect recoil, respect crew, respect reload order.",
                            "Abalone crew status: remembered. Operational loyalty: continuing."
                    };
                    say(p, lines[this.random.nextInt(lines.length)]);
                }
            }
        }
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
        tag.putInt("ArtilleryStage", artilleryStage);
        tag.putInt("DrillsCompleted", drillsCompleted);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        artilleryStage = tag.getInt("ArtilleryStage");
        drillsCompleted = tag.getInt("DrillsCompleted");
    }
}
