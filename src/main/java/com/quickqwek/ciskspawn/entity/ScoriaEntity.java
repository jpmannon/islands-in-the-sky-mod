package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Scoria implementation.
 * Uses the exported Scoria Blockbench/GeckoLib assets.
 */
public class ScoriaEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation IDLE2_ANIM = RawAnimation.begin().thenLoop("idle2");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int clueStage = 0;
    private int techLessonsGiven = 0;
    private int revealProgress = 0;
    private int projectStage = 0;
    private int projectSessions = 0;
    private int ambientCooldown = 20 * 154;
    private int projectCooldown = 20 * 40;
    private BlockPos manualProjectAnchor = null;
    private BlockPos projectTarget = null;
    private int projectWorkTicks = 0;

    public ScoriaEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 20.0D);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.45D));
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
            String firstMeet = pickFirstMeeting(player);
            say(player, firstMeet);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Scoria - Apprentice",
                        firstMeet,
                        "Engineering lesson",
                        "About Mortimer",
                        "First time meeting Scoria."
                ));
            }
            return;
        }

        String greeting = pickScoriaGreeting(progress, player.getName().getString());
        String body = greeting + "\n\n"
                + "Scoria keeps his coat too clean for a dockhand and too practical for a banker. "
                + "He is trying very hard not to look proud of the pressure-valve diagrams tucked under his arm.\n\n"
                + "Secret status: apprentice aero-engineer. Mortimer still thinks 'banker clothes' explain everything.\n\n"
                + "Trust: " + progress.trust + "/100. Personal quest stage: " + progress.questStage + "/3.\n"
                + "Reveal progress: " + revealProgress + "/4. Tech lessons given: " + techLessonsGiven + ".\n"
                + "Project stage: " + projectStage + "/4. Project sessions: " + projectSessions + ".";
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Scoria - Apprentice",
                    body,
                    "Engineering lesson",
                    "About Mortimer",
                    "Scoria is now using his real exported model. Idle alternates between idle and idle2."
            ));
        }
    }

    public void handleScoriaAction(Player player, String action) {
        switch (action) {
            case "scoria_lesson" -> engineeringLesson(player);
            case "scoria_mortimer" -> aboutMortimer(player);
            case "scoria_clue" -> clueHint(player);
            case "scoria_reveal" -> revealStep(player);
            case "scoria_log" -> logPage(player);
            case "scoria_emote" -> showEmote("⚙");
            case "scoria_project" -> projectStatus(player);
            case "scoria_set_project" -> setProjectHere(player);
            case "scoria_advance_project" -> advanceProject(player);
            default -> openDialogue(player);
        }
    }

    private String pickFirstMeeting(Player player) {
        if (nearCraftingBlock()) {
            return "This isn't — I was just looking at something. Structural curiosity. Entirely unrelated. Scoria. What can I do for you?";
        }
        return "Oh — sorry, I didn't hear you. Scoria. I'm here visiting family. I have some... investments. Financial interests. What did you need?";
    }

    private String pickScoriaGreeting(PlayerProgress progress, String playerName) {
        if (progress.trust >= 55) {
            return randomOf(
                    "You know what I'm doing, don't you. I can tell you know.",
                    "The design is almost ready. I just need to test one component."
            );
        }

        if (progress.trust >= 35) {
            return randomOf(
                    "There you are. I had a — I need to show you something, actually. When you have a moment.",
                    "I've been working on something. I'd like a second opinion. Not from Mortimer."
            );
        }

        if (progress.trust >= 15) {
            return randomOf(
                    "Oh, " + playerName + ". Good. I was going to ask you something. Not financial.",
                    "Back again. Good. The guild can be a lot to take in the first time."
            );
        }

        return randomOf(
                "Scoria. Financial work, mostly. Not very interesting, I'm afraid. What can I do for you?",
                "Just passing through. Visiting family. The usual."
        );
    }

    private void setProjectHere(Player player) {
        manualProjectAnchor = player.blockPosition().immutable();
        projectTarget = manualProjectAnchor;
        projectWorkTicks = 20 * 90;
        say(player, "Good. This spot is officially not suspicious. Just a normal apprentice workstation. With stabilizer sketches. And no banking.");
    }

    private void projectStatus(Player player) {
        if (projectTarget == null) projectTarget = findProjectTarget();
        String target = projectTarget == null ? "No Create block/redstone station found nearby." : projectTarget.getX() + ", " + projectTarget.getY() + ", " + projectTarget.getZ();
        String body = "Scoria Project Routine\n\n"
                + "Target: " + target + "\n"
                + "Manual anchor: " + (manualProjectAnchor == null ? "not set" : manualProjectAnchor.getX()+", "+manualProjectAnchor.getY()+", "+manualProjectAnchor.getZ()) + "\n"
                + "Project sessions: " + projectSessions + ". Project stage: " + projectStage + "/4.\n\n"
                + "Current routine: Scoria looks for nearby Create machinery, redstone, crafting tables, or a manual anchor. When close, he idles as if tuning stabilizers and pressure valves.\n\n"
                + "Future polish: dedicated Scoria project block, advanced Create addon lessons, and a reveal scene with Mortimer.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Scoria Project",
                    body,
                    "Advance project",
                    "Engineering lesson",
                    "Set a manual project anchor if automatic scanning misses your build."
            ));
        }
    }

    private void advanceProject(Player player) {
        switch (projectStage) {
            case 0 -> {
                if (hasItem(player, "create:andesite_alloy", 4) && hasItem(player, "minecraft:redstone", 4)) {
                    consumeItem(player, "create:andesite_alloy", 4);
                    consumeItem(player, "minecraft:redstone", 4);
                    projectStage = 1; revealProgress = Math.max(revealProgress, 1);
                    say(player, "Good. Basic stabilizer frame. Not a bank ledger. Very important distinction.");
                    giveItemById(player, "create:shaft", 2);
                } else say(player, "For the stabilizer frame I need 4 andesite alloy and 4 redstone. Quietly. Dad has dramatic ears.");
            }
            case 1 -> {
                if (hasItem(player, "create:precision_mechanism", 1) || hasItem(player, "create:cogwheel", 6)) {
                    if (hasItem(player, "create:precision_mechanism", 1)) consumeItem(player, "create:precision_mechanism", 1); else consumeItem(player, "create:cogwheel", 6);
                    projectStage = 2; revealProgress = Math.max(revealProgress, 2);
                    say(player, "Timing assembly complete. If anyone asks, this is... accountancy. With gears.");
                    giveItemById(player, "create:electron_tube", 1);
                } else say(player, "I need a precision mechanism. Or six cogwheels if we're doing this the loud way.");
            }
            case 2 -> {
                if (hasItem(player, "create:brass_ingot", 2) || hasItem(player, "minecraft:copper_ingot", 8)) {
                    if (hasItem(player, "create:brass_ingot", 2)) consumeItem(player, "create:brass_ingot", 2); else consumeItem(player, "minecraft:copper_ingot", 8);
                    projectStage = 3; revealProgress = Math.max(revealProgress, 3);
                    say(player, "Pressure housing done. This is the part where Dad would pretend not to cry over good tolerances.");
                    giveItemById(player, "create:brass_sheet", 2);
                } else say(player, "I need 2 brass ingots, or 8 copper ingots for a cheaper prototype. Don't tell Mortimer I said cheaper.");
            }
            default -> {
                projectStage = 4; revealProgress = 4;
                say(player, "The prototype's ready. I just need the courage to tell Dad the banker thing was a ruse. Possibly also a helmet.");
                giveItemById(player, "create:precision_mechanism", 1);
            }
        }
        showEmote("⚙");
    }

    private void engineeringLesson(Player player) {
        PlayerProgress progress = getProgress(player);
        techLessonsGiven++;
        clueStage = Math.max(clueStage, 2);
        switch (progress.questStage) {
            case 0 -> {
                addTrust(progress, 3);
                say(player, "Engineering theory? I know a little. Purely academically. Levitite, for example — fascinating stabilization problem.");
            }
            case 1 -> {
                addTrust(progress, 5);
                revealProgress = Math.max(revealProgress, 1);
                say(player, "Alright. Between us — the banker thing is a simplification. I've been working on something. Levitite thruster stabilizers at altitude. It's a real problem.");
            }
            case 2 -> {
                addTrust(progress, 5);
                revealProgress = Math.max(revealProgress, 2);
                say(player, "Come back when the sun's lower. I'll show you the actual design. Don't tell my father.");
            }
            default -> {
                addTrust(progress, 10);
                revealProgress = Math.max(revealProgress, 4);
                say(player, "The test went well. You can tell him now. If the moment comes. I think I'm ready.");
            }
        }
        updateQuestStageFromTrust(progress);
    }

    private void aboutMortimer(Player player) {
        PlayerProgress progress = getProgress(player);
        clueStage = Math.max(clueStage, 1);
        say(player, switch (trustTier(progress)) {
            case 0 -> "My father. Freelance captain, airship work mostly. He's... well-respected.";
            case 1 -> "He thinks I'm a banker. I've tried to correct it and the moment always closes before I get there.";
            case 2 -> "I'm trying to build something he'd respect. I just need to do it right first. And by right I mean perfectly.";
            default -> "He's already proud of me. I know that. I just need him to know *why* to be proud.";
        });
    }

    private void clueHint(Player player) {
        PlayerProgress progress = getProgress(player);
        clueStage = Math.max(clueStage, 2);
        addTrust(progress, 1);
        updateQuestStageFromTrust(progress);
        say(player, randomOf(
                "Levitite drift gets worse when the hull is asymmetric. That's not a banker observation.",
                "I keep sketching stabilizer fins in ledger margins. So far no one has noticed. Except Mum, obviously.",
                "I'll tell him when I have something worth showing.",
                "The design has to hold at altitude. If it fails on the bench, fine. If it fails in front of Dad, no."
        ));
    }

    private void revealStep(Player player) {
        revealProgress = Math.min(4, revealProgress + 1);
        clueStage = Math.max(clueStage, 3);
        say(player, switch (revealProgress) {
            case 1 -> "Fine. No, I'm not learning banking. Please don't tell Dad yet.";
            case 2 -> "My apprenticeship is with the Aero Guild. I wanted to earn the badge first.";
            case 3 -> "The thruster notes? Mine. The neat handwriting gave me away, didn't it?";
            default -> "When I tell him, I want the Abalone flying overhead. Dramatic? Yes. Family tradition.";
        });
    }

    private void logPage(Player player) {
        String body = "Crew Log - Scoria\n\n"
                + "Public story: future banker.\n"
                + "Actual story: aero-engineering apprentice keeping the secret far too long.\n\n"
                + "Tech themes: stabilizers, levitite tuning, redstone safety, thrusters, pressure valves.\n\n"
                + "Clue stage: " + clueStage + ". Reveal progress: " + revealProgress + "/4. Lessons: " + techLessonsGiven + ".\n\n"
                + "Late-game hook: Scoria can teach advanced airship tech once his apprenticeship is revealed.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Crew Logbook - Scoria",
                    body,
                    "Engineering lesson",
                    "Close",
                    "Persistent prototype: saved on the Scoria entity."
            ));
        }
    }

    private void say(Player player, String text) {
        this.setCustomName(Component.literal("§bScoria§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        player.displayClientMessage(Component.literal("§bScoria§7: " + text), false);
    }

    private void showEmote(String icon) {
        this.setCustomName(Component.literal("§bScoria §f" + icon));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 3;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        if (speechTicksLeft > 0) {
            speechTicksLeft--;
            if (speechTicksLeft == 0) {
                this.setCustomNameVisible(false);
                this.setCustomName(Component.literal("Scoria - Apprentice"));
            }
        }
        tickProjectRoutine();
        if (ambientCooldown > 0) ambientCooldown--;
        else {
            Player nearby = this.level().getNearestPlayer(this, 8.0D);
            if (nearby != null) sayNearby(pickAmbientLine(nearby));
            ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
        }
    }

    private String pickAmbientLine(Player player) {
        PlayerProgress progress = getProgress(player);
        if (progress.trust >= 35) {
            return randomOf(
                    "Father used to say the sky rewards adaptation. I've been adapting.",
                    "Levitite needs a stabilizer at high altitude. Solved problem. Someone just hasn't written it down yet."
            );
        }

        if (progress.trust >= 15) {
            return "Levitite needs a stabilizer at high altitude. Solved problem. Someone just hasn't written it down yet.";
        }

        return randomOf(
                "The tolerances on high-altitude propulsion are — sorry. Never mind.",
                "The banker thing is fine. Entirely fine.",
                "Not here for engineering reasons. Obviously."
        );
    }

    private void tickProjectRoutine() {
        if (projectCooldown > 0) projectCooldown--;
        if (projectTarget == null && projectCooldown <= 0) {
            projectCooldown = 20 * 60;
            projectTarget = findProjectTarget();
        }
        if (projectTarget == null) return;
        double dist = this.distanceToSqr(projectTarget.getX() + 0.5D, projectTarget.getY(), projectTarget.getZ() + 0.5D);
        if (dist > 3.0D) {
            this.getNavigation().moveTo(projectTarget.getX() + 0.5D, projectTarget.getY(), projectTarget.getZ() + 0.5D, 0.5D);
        } else {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(projectTarget.getX()+0.5D, projectTarget.getY()+0.5D, projectTarget.getZ()+0.5D, 20.0F, 20.0F);
            if (projectWorkTicks <= 0) {
                projectSessions++;
                projectWorkTicks = 20 * 20;
            } else projectWorkTicks--;
            if (this.random.nextFloat() < 0.006F) sayNearby(randomOf("Redstone timing looks stable. Unlike family communication.", "Pressure tolerance acceptable. Emotional tolerance pending.", "Thrusters need respect. So do fathers, annoyingly.", "This is definitely not banking."));
        }
    }

    private BlockPos findProjectTarget() {
        if (manualProjectAnchor != null) return manualProjectAnchor;
        BlockPos center = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-10, -3, -10), center.offset(10, 3, 10))) {
            BlockState state = this.level().getBlockState(pos);
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String id = key == null ? "" : key.toString();
            if (id.contains("create:") || id.contains("redstone") || id.contains("crafting_table") || id.contains("smithing_table") || id.contains("station")) return pos.immutable();
        }
        return null;
    }

    private void sayNearby(String text) {
        this.setCustomName(Component.literal("§bScoria§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        Component chat = Component.literal("§bScoria§7: " + text);
        for (Player p : this.level().players()) if (p.distanceTo(this) <= 12.0F) p.sendSystemMessage(chat);
    }

    private String randomOf(String... values) { return values[this.random.nextInt(values.length)]; }

    private PlayerProgress getProgress(Player player) {
        return progressByPlayer.computeIfAbsent(player.getUUID(), uuid -> new PlayerProgress());
    }

    public int getTrustForPlayer(UUID playerUUID) {
        return progressByPlayer.getOrDefault(playerUUID, new PlayerProgress()).trust;
    }

    private void addTrust(PlayerProgress progress, int amount) {
        progress.trust = Math.min(100, progress.trust + amount);
    }

    private void updateQuestStageFromTrust(PlayerProgress progress) {
        if (progress.trust >= 55) {
            progress.questStage = Math.max(progress.questStage, 3);
        } else if (progress.trust >= 35) {
            progress.questStage = Math.max(progress.questStage, 2);
        } else if (progress.trust >= 15) {
            progress.questStage = Math.max(progress.questStage, 1);
        }
    }

    private int trustTier(PlayerProgress progress) {
        if (progress.trust >= 55) return 3;
        if (progress.trust >= 35) return 2;
        if (progress.trust >= 15) return 1;
        return 0;
    }

    private boolean nearCraftingBlock() {
        BlockPos center = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-5, -5, -5), center.offset(5, 5, 5))) {
            BlockState state = this.level().getBlockState(pos);
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String id = key == null ? "" : key.toString();
            if (id.contains("anvil") || id.contains("bench") || id.contains("table") || id.contains("depot")) {
                return true;
            }
        }
        return false;
    }

    private int countItem(Player player, String itemId) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) if (stackMatchesId(stack, itemId)) count += stack.getCount();
        return count;
    }
    private boolean hasItem(Player player, String itemId, int amount) { return countItem(player, itemId) >= amount; }
    private void consumeItem(Player player, String itemId, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stackMatchesId(stack, itemId)) continue;
            int take = Math.min(remaining, stack.getCount()); stack.shrink(take); remaining -= take;
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
        tag.put("ScoriaProgress", list);
        tag.putInt("ScoriaClueStage", clueStage);
        tag.putInt("ScoriaTechLessons", techLessonsGiven);
        tag.putInt("ScoriaRevealProgress", revealProgress);
        tag.putInt("ScoriaProjectStage", projectStage);
        tag.putInt("ScoriaProjectSessions", projectSessions);
        if (manualProjectAnchor != null) { tag.putInt("ManualProjectX", manualProjectAnchor.getX()); tag.putInt("ManualProjectY", manualProjectAnchor.getY()); tag.putInt("ManualProjectZ", manualProjectAnchor.getZ()); }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        progressByPlayer.clear();
        if (tag.contains("ScoriaProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("ScoriaProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.questStage = playerTag.getInt("QuestStage");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
        if (tag.contains("ScoriaClueStage")) clueStage = tag.getInt("ScoriaClueStage");
        if (tag.contains("ScoriaTechLessons")) techLessonsGiven = tag.getInt("ScoriaTechLessons");
        if (tag.contains("ScoriaRevealProgress")) revealProgress = tag.getInt("ScoriaRevealProgress");
        if (tag.contains("ScoriaProjectStage")) projectStage = tag.getInt("ScoriaProjectStage");
        if (tag.contains("ScoriaProjectSessions")) projectSessions = tag.getInt("ScoriaProjectSessions");
        if (tag.contains("ManualProjectX")) manualProjectAnchor = new BlockPos(tag.getInt("ManualProjectX"), tag.getInt("ManualProjectY"), tag.getInt("ManualProjectZ"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<ScoriaEntity> state) {
        if (state.isMoving()) {
            state.setAnimation(WALK_ANIM);
        } else {
            state.setAnimation(shouldUseIdle2() ? IDLE2_ANIM : IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    private boolean shouldUseIdle2() {
        // Deterministic pseudo-random idle variation so clients stay visually stable.
        // Changes roughly every 10 seconds, with entity id offset so multiple Scorias do not sync.
        long window = Math.max(0, this.tickCount / 200L);
        long mixed = window * 1103515245L + (long) this.getId() * 12345L + 67890L;
        return Math.floorMod(mixed, 4L) == 0L;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    private static class PlayerProgress {
        int trust = 0;
        boolean hasMet = false;
        int questStage = 0;
    }
}
