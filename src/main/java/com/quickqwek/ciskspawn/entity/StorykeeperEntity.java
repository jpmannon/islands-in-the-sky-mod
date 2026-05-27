package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.ai.NpcAnchorReturnGoal;
import com.quickqwek.ciskspawn.ai.NpcFollowable;
import com.quickqwek.ciskspawn.ai.NpcShipStabilityGoal;
import com.quickqwek.ciskspawn.ai.NpcWaypointGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.neoforged.neoforge.network.PacketDistributor;
import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import com.quickqwek.ciskspawn.server.PlayerStatsTracker;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;

import java.util.HashMap;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Mortimer - Aeromancer.
 *
 * Current systems:
 * - GeckoLib idle/walk animation controller.
 * - Anchored wandering and player watching.
 * - Floating dialogue through temporary custom-name speech bubbles.
 * - Contextual ambient lines.
 * - Basic expandable quest chain.
 * - Basic hidden trust/relationship progression saved on the entity.
 * - First-pass travel behavior: if the player is riding a vehicle/seat, Mortimer
 *   will try to ride the same vehicle.
 *
 * Later upgrade targets:
 * - True custom floating UI screen.
 * - JSON-driven dialogue trees.
 * - FTB Quests integration.
 * - Create Aeronautics seat/contraption-specific travel behavior.
 */
public class StorykeeperEntity extends PathfinderMob implements GeoEntity, NpcFollowable {

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("Walk");
    private static final RawAnimation SIT_ANIM = RawAnimation.begin().thenLoop("sit");
    private static final EntityDataAccessor<Boolean> DATA_VISUALLY_SITTING = SynchedEntityData.defineId(StorykeeperEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int SPEECH_TICKS = 20 * 7;
    private static final int MIN_AMBIENT_COOLDOWN = 20 * 154;
    private static final int AMBIENT_RANDOM_EXTRA = 20 * 242;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;
    private BlockPos homePos = null;
    private int coupleCooldown = 20 * 35;
    private int emoteCooldown = 20 * 70;
    private int routineCooldown = 20 * 80;
    private int targetSeatId = -1;
    private UUID requestedBoardingPlayer = null;
    private UUID followingPlayer = null;
    private int followTicksLeft = 0;
    private int mountedTrustCooldown = 20 * 30;
    private BlockPos targetSeatBlockPos = null;
    private int targetContraptionSeatEntityId = -1;
    private int targetContraptionSeatIndex = -1;
    private int pretendSittingTicks = 0;
    private float seatedYaw = 0.0F;
    private String lastSeatFailure = "";
    private BlockPos guildWorkTarget = null;
    private BlockPos manualGuildAnchor = null;
    private BlockPos morningAnchor = null;
    private BlockPos afternoonAnchor = null;
    private BlockPos eveningAnchor = null;
    private BlockPos scheduledTarget = null;
    private int guildWorkTicks = 0;
    private int scheduleCooldown = 20 * 20;
    private int nextScheduleAnchorSlot = 0;
    private int guildMeetingsMentioned = 0;
    private int coupleLinesSpoken = 0;
    private int emotesShown = 0;
    private int travelLinesSpoken = 0;
    private int relationshipChecks = 0;
    private int logbookOpens = 0;

    private static final int FOLLOW_DURATION_TICKS = 20 * 60 * 5;

    public boolean isVisuallySitting() {
        return this.isPassenger() || this.pretendSittingTicks > 0 || this.entityData.get(DATA_VISUALLY_SITTING);
    }



    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VISUALLY_SITTING, false);
    }

    public StorykeeperEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
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
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.55D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 9.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new NpcWaypointGoal(this, 0.45D));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

        if (!this.level().isClientSide) {
            ItemStack held = player.getItemInHand(hand);
            if (player.isShiftKeyDown() && held.is(Items.STICK)) {
                setNextScheduleAnchor(player);
                return InteractionResult.SUCCESS;
            }
            openDialogue(player);
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void openDialogue(Player player) {
        PlayerStatsTracker.recordVisit(player, "mortimer");
        PlayerProgress progress = getProgress(player);

        if (!progress.hasMet) {
            progress.hasMet = true;
            progress.trust = Math.min(100, progress.trust + 1);
            String firstMeet = pickFirstMeeting(player);
            String firstMeetBody = withWeatherNote(firstMeet, getWeather());
            sayTagOnly("Mortimer - Aeromancer", firstMeet);

            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Mortimer - Aeromancer",
                        firstMeetBody,
                        "Tell me about this place",
                        "What do you do here?",
                        "First time meeting Mortimer."
                ));
            }
            return;
        }

        progress.trust = Math.min(100, progress.trust + 1);

        String playerName = player.getName().getString();
        String greeting = pickGreeting(playerName, progress);
        String body = withWeatherNote(buildDialogueBody(playerName, progress), getWeather());
        sayTagOnly("Mortimer - Aeromancer", greeting);

        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Mortimer - Aeromancer",
                    body,
                    "Discuss work",
                    "Travel / Board",
                    "G = board/travel, H = scan seats for debugging."
            ));
        }
    }

    public void handleMortimerAction(Player player, String action) {
        switch (action) {
            case "travel" -> tryTravelWith(player);
            case "scan_seats" -> scanSeatEnvironment(player);
            case "follow" -> askToFollow(player);
            case "talk" -> sendQuestDialogue(player, getProgress(player));
            case "relationship", "logbook" -> sendMortimerDialogue(player, "Use the Crew Logbook for that now. Patchouli finally gave the notes somewhere proper to live.");
            case "guild_status" -> guildStatus(player);
            case "scoria_tracker" -> sendMortimerDialogue(player, "Scoria's notes are in the Crew Logbook now. Which is safer than asking me to organize them.");
            case "guild_set_anchor" -> setGuildAnchorHere(player);
            case "guild_call_anchor" -> callToGuildAnchor(player);
            case "emote" -> showEmote(randomOf("🔧", "☕", "⚙", "💭", "🧭"));
            default -> openDialogue(player);
        }
    }

    private String pickFirstMeeting(Player player) {
        if (this.level().isRaining()) {
            return "You came in during this? Either you're brave or you didn't check the sky before you left. Mortimer.";
        }
        return "Haven't seen you before. That means you just arrived or you've been avoiding the guild. Either way — you're here now. Name's Mortimer.";
    }

    private String withWeatherNote(String body, String weather) {
        return switch (weather) {
            case "STORM" -> body + "\n\nGet inside once we're done here.";
            case "RAIN" -> body + "\n\nMiserable day for flying.";
            case "CLEAR" -> body + "\n\nGood day for it.";
            default -> body;
        };
    }

    private void setNextScheduleAnchor(Player player) {
        BlockPos anchor = player.blockPosition().immutable();
        String label;
        switch (nextScheduleAnchorSlot) {
            case 0 -> {
                morningAnchor = anchor;
                label = "morning";
            }
            case 1 -> {
                afternoonAnchor = anchor;
                label = "afternoon";
            }
            default -> {
                eveningAnchor = anchor;
                label = "evening";
            }
        }
        nextScheduleAnchorSlot = (nextScheduleAnchorSlot + 1) % 3;
        scheduledTarget = null;
        scheduleCooldown = 0;
        player.sendSystemMessage(Component.literal("§bMortimer " + label + " schedule anchor set at " + anchor.getX() + ", " + anchor.getY() + ", " + anchor.getZ() + "."));
        say("Mortimer - Aeromancer", "Schedule noted. If I end up in the pantry, that is your fault.", player);
    }

    private BlockPos getScheduledAnchor() {
        long t = this.level().getDayTime() % 24000L;
        if (t < 6000) return morningAnchor;
        if (t < 12000) return afternoonAnchor;
        if (t < 18000) return eveningAnchor;
        return null;
    }

    private String buildDialogueBody(String playerName, PlayerProgress progress) {
        return switch (progress.questStage) {
            case 0 -> "Ah, " + playerName + ". Still in one piece. Good start. Bring Builders Tea, two Andesite Alloy, and a Copper Sheet. Can't teach on an empty boiler.";
            case 1 -> "The dock's been groaning like a banker asked to share. I need four Shafts, four Cogwheels, and six Copper Ingots.";
            case 2 -> "There's a ship in my hangar. Old girl named the Abalone. Don't laugh. Fried abalone is a noble food. Ask me about her when you're ready.";
            default -> randomOf(
                    "The Abalone will need hull plates, balloon cloth, navigation work, and more patience than either of us own.",
                    "Things worth loving require maintenance. Ships, islands, people. Especially people.",
                    "No island survives alone. Remember that when your machines start looking cleverer than your friends."
            );
        };
    }

    private String pickGreeting(String playerName, PlayerProgress progress) {
        if (progress.questStage >= 3) {
            return "Back already, " + playerName + "? Good. Old ships like attention. Old aeromancers too, though we complain louder.";
        }

        if (progress.trust >= 80) {
            return randomOf(
                    "There you are. Geera was starting to worry. I told her you were fine. I was also worrying.",
                    "You know you don't have to knock. This is just what home is now.",
                    "I'm not going to say it out loud, so just... you know. Alright?"
            );
        }

        if (progress.trust >= 55) {
            return randomOf(
                    "You remind me of someone I used to crew with. Stubborn in the same useful direction.",
                    "I'm going to tell you something and I need you to not make it a thing. Alright?",
                    "There's something about the western arches I've never told the full version of...",
                    "There was a voyage. Years ago. The kind they still tell about in quiet corners of taverns."
            );
        }

        if (progress.trust >= 35) {
            return randomOf(
                    playerName + ". Was wondering when you'd show up.",
                    "Don't tell me. Let me guess. Something went sideways and you want to talk about it.",
                    "You've been busy. I can tell. Sit down. What happened?"
            );
        }

        if (progress.trust >= 15) {
            return randomOf(
                    "Back again. Good. The sky suits you, I think.",
                    "You handling yourself out there? No shame in asking questions.",
                    "You've got that look. Someone thinking about the next island before they've finished the last."
            );
        }

        return randomOf(
                "You're new. The sky bring you here or something else?",
                "I'll give you the same advice I give everyone: don't fly blind and keep your rope dry.",
                "Mortimer. Been here long enough that the question doesn't matter much."
        );
    }


    private void setGuildAnchorHere(Player player) {
        manualGuildAnchor = player.blockPosition().immutable();
        guildWorkTarget = manualGuildAnchor;
        guildWorkTicks = 20 * 120;
        sendMortimerDialogue(player, "Right. This is a Guild point now. Inform the paperwork goblins. Actually no, don't. They'll make it official.");
        showEmote("⚙");
    }

    private void callToGuildAnchor(Player player) {
        if (manualGuildAnchor == null) {
            setGuildAnchorHere(player);
            return;
        }
        guildWorkTarget = manualGuildAnchor;
        guildWorkTicks = 20 * 120;
        sendMortimerDialogue(player, "On my way to the Guild point. If anyone says 'meeting minutes', I am leaving.");
    }


    private void guildStatus(Player player) {
        long day = this.level().getDayTime() / 24000L;
        boolean guildDay = day % 9 == 3 || day % 13 == 4;
        String body = "Aero Guild Routine Status\n\n"
                + "Today is day " + day + ". "
                + (guildDay ? "Mortimer is feeling the pull of Guild business today. If a lectern, bell, smithing table, or Create block is nearby, he may wander over and pretend he is only inspecting it." : "No formal Guild visit today. Mortimer is available for travel, lectures, tea, and irresponsible opinions about banks.")
                + "\n\nGuild meetings mentioned: " + guildMeetingsMentioned + ". Couple lines spoken: " + coupleLinesSpoken + ". Travel lines logged: " + travelLinesSpoken + "."
                + "\n\nCurrent prototype: Mortimer can choose a nearby lectern, bell, smithing table, crafting table, depot, or gearbox as a temporary Guild anchor. Future version: physical routes to the Guild hall, old crew NPCs, mission board jobs, and apprenticeship scenes for Scoria.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Mortimer Guild Status",
                    body,
                    "Discuss work",
                    "Travel / Board",
                    "Prototype routine status. Physical guild pathing will come after Guild hall layout exists."
            ));
        }
    }

    private void scoriaTracker(Player player) {
        PlayerProgress progress = getProgress(player);
        String body = "Scoria's Journey - Clues\n\n"
                + "Current clue stage: " + Math.min(5, Math.max(1, progress.questStage + 1)) + "/5.\n\n"
                + "1. The Banker? Mortimer thinks Scoria is dressing too finely for honest engineering.\n"
                + "2. Apprenticeship Interview: polished boots, careful hair, suspiciously clean cuffs.\n"
                + "3. The Secret Workshop: Geera keeps laughing whenever Mortimer says 'banker'.\n"
                + "4. The Truth: Scoria is training as an aero-engineer.\n"
                + "5. A New Kind of Engineer: late-game Create tech, thrusters, stabilizers, and airship systems.\n"
                + "6. Guild Recognition: Scoria joins the Aero Guild and Mortimer becomes the happiest man in the stratosphere.\n\n"
                + "Mortimer is not ready to know yet. Geera absolutely is enjoying this.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Scoria Clue Tracker",
                    body,
                    "Discuss work",
                    "Close",
                    "This tracker is narrative groundwork until Scoria has his own model/entity."
            ));
        }
    }

    private void describeRelationship(Player player) {
        PlayerProgress progress = getProgress(player);
        relationshipChecks++;
        String tier = relationshipName(progress.trust);
        String body = "Mortimer Relationship / Trust\n\n"
                + "Current tier: " + tier + " (" + progress.trust + "/100).\n\n"
                + "Unlock ladder:\n"
                + "0  - Stranger: jokes, warnings, basic tasks.\n"
                + "15 - Guild Associate: practical repair advice and Guild board hints.\n"
                + "35 - Trusted Pilot: Abalone references and travel trust.\n"
                + "55 - Friend: Geera, Scoria, and family hints.\n"
                + "80 - Crew: final-voyage planning and deeper Abalone restoration hooks.\n\n"
                + "Checks opened: " + relationshipChecks + ". Travel lines heard: " + travelLinesSpoken + ".\n\n"
                + switch (tier) {
                    case "Crew" -> "Mortimer worries about you like crew now. That means more sarcasm, not less.";
                    case "Friend" -> "He is starting to say things he would normally hide behind engine noise.";
                    case "Trusted Pilot" -> "He trusts your hands near machinery and your judgment near the sky.";
                    case "Guild Associate" -> "You have stopped being merely suspicious and become usefully suspicious.";
                    default -> "Every good crew starts suspicious.";
                };
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Mortimer Relationship",
                    body,
                    "Discuss work",
                    "Crew log",
                    "Relationship info is now a real UI page backed by saved trust."
            ));
        }
    }

    private String currentMood(PlayerProgress progress) {
        if (this.isPassenger()) return "trying to look relaxed while judging the seating arrangement";
        if (progress.trust >= 80) return "quietly proud, though he would rather fight a gearbox than say so";
        if (travelLinesSpoken > 0) return "restless for the sky";
        if (guildMeetingsMentioned > 0) return "thinking about the Guild and pretending it is casual";
        return "tea-seeking, bolt-counting, and suspicious of banks";
    }

    private void crewLog(Player player) {
        PlayerProgress progress = getProgress(player);
        logbookOpens++;
        String body = "Crew Log - Aeromancer Notes\n\n"
                + "Mortimer: retired adventurer, youngest-ever licensed Aeromancer, tea loyalist, bank skeptic, and current menace with a wrench.\n\n"
                + "Trust: " + relationshipName(progress.trust) + " (" + progress.trust + "/100). Current Mortimer quest stage: " + progress.questStage + ".\n\n"
                + "Session/world counters: couple lines " + coupleLinesSpoken + ", emotes " + emotesShown + ", travel lines " + travelLinesSpoken + ", logbook opens " + logbookOpens + ".\n\n"
                + "The Abalone: old ship, old grief, one final voyage waiting somewhere under the rust.\n\n"
                + "Geera: fisherwoman, bait-and-tackle operator, wife, and possibly the only person Mortimer fears respectfully.\n\n"
                + "Scoria: supposedly becoming a banker. Actually apprenticing as an aero-engineer to surprise Mortimer.\n\n"
                + "Guild routine notes: Mortimer has mentioned guild business " + guildMeetingsMentioned + " time(s). If there is a lectern, bell, smithing table, or Create block nearby on a guild day, he may wander over like he's pretending not to care.\n\n"
                + "Persistent Pages:\n"
                + "- Abalone: restoration thread active after quest stage 2.\n"
                + "- Guild: meetings mentioned " + guildMeetingsMentioned + " time(s).\n"
                + "- Scoria: clues unlocked through dialogue and Geera rumors.\n"
                + "- Travel: ship dialogue should remain optional flavor, not mandatory progression.\n\n"
                + "Design note: optional, lightweight RPG flavor that supports building and flying, never blocks it.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Crew Logbook - Mortimer",
                    body,
                    "Discuss work",
                    "Close",
                    "Persistent prototype: this page is rebuilt from saved Mortimer trust and quest progress."
            ));
        }
    }

    private void sendQuestDialogue(Player player, PlayerProgress progress) {
        switch (progress.questStage) {
            case 0 -> teaAndTorqueQuest(player, progress);
            case 1 -> hullGroansQuest(player, progress);
            case 2 -> abaloneRevealQuest(player, progress);
            default -> afterCurrentQuestChain(player, progress);
        }
    }

    private void teaAndTorqueQuest(Player player, PlayerProgress progress) {
        String prompt = "Tea and Torque\n\nBring me Builders Tea, two Andesite Alloy, and a Copper Sheet. Can't teach on an empty boiler.";

        if (hasItem(player, "create:builders_tea", 1) && hasItem(player, "create:andesite_alloy", 2) && hasItem(player, "create:copper_sheet", 1)) {
            consumeItem(player, "create:builders_tea", 1);
            consumeItem(player, "create:andesite_alloy", 2);
            consumeItem(player, "create:copper_sheet", 1);
            progress.questStage = 1;
            progress.trust = Math.min(100, progress.trust + 8);
            sendMortimerDialogue(player, "Good. Tea, alloy, copper. Civilization in three ingredients.\n\nQuest complete: Tea and Torque.\n\nNew quest unlocked. Right-click Mortimer again.");
        } else {
            sendMortimerDialogue(player, prompt + "\n\nNeeds: Builders Tea x1, Andesite Alloy x2, Copper Sheet x1.");
        }
    }

    private void hullGroansQuest(Player player, PlayerProgress progress) {
        String prompt = "Hull Groans\n\nThe dock's been groaning like a banker asked to share. Bring Shafts, Cogwheels, and Copper Ingots.";

        if (hasItem(player, "create:shaft", 4) && hasItem(player, "create:cogwheel", 4) && hasItem(player, "minecraft:copper_ingot", 6)) {
            consumeItem(player, "create:shaft", 4);
            consumeItem(player, "create:cogwheel", 4);
            consumeItem(player, "minecraft:copper_ingot", 6);
            progress.questStage = 2;
            progress.trust = Math.min(100, progress.trust + 10);
            sendMortimerDialogue(player, "Proper parts. Proper hands. That's how islands stay up.\n\nQuest complete: Hull Groans.\n\nNew quest unlocked. Right-click Mortimer again.");
        } else {
            sendMortimerDialogue(player, prompt + "\n\nNeeds: Shaft x4, Cogwheel x4, Copper Ingot x6.");
        }
    }

    private void abaloneRevealQuest(Player player, PlayerProgress progress) {
        progress.questStage = 3;
        progress.trust = Math.min(100, progress.trust + 12);
        sendMortimerDialogue(player, "There's a ship in my hangar. Old girl named the Abalone. Don't laugh. Fried abalone is a noble food.\n\nSomeday, if you've the stomach for it, we'll patch her for one last voyage. Not today. Today we start with trust.\n\nThe Abalone still has one voyage in her. So do I, if Geera doesn't bolt me to a chair first.\n\nQuest discovered: Restore the Abalone.");
    }

    private void afterCurrentQuestChain(Player player, PlayerProgress progress) {
        String[] lines = new String[] {
                "The Abalone will need hull plates, balloon cloth, navigation work, and more patience than either of us own.",
                "If you see Cliff Kites nesting, steer wide. They're harmless until they aren't.",
                "Things worth loving require maintenance. Ships, islands, people. Especially people.",
                "No island survives alone. Remember that when your machines start looking cleverer than your friends.",
                "Scoria in banker clothes. Still makes my teeth itch. Geera laughs every time I mention it.",
                "If my son joins a bank, I am hauntin' every ledger in Spawn City. Politely, at first.",
                "Geera says Scoria's got more engine sense than I had at his age. Which is terrifying, because I was unbearable.",
                "Advanced thrusters, levitite tuning, redstone safety cutoffs. Scoria reads that stuff for fun. Banker behavior, clearly.",
                "The Guild teaches craft. Banks teach fear with nicer shoes."
        };
        String line = lines[this.random.nextInt(lines.length)];
        sendMortimerDialogue(player, line);
    }

    public void askToFollow(Player player) {
        this.followingPlayer = player.getUUID();
        this.followTicksLeft = FOLLOW_DURATION_TICKS;
        PlayerProgress progress = getProgress(player);
        progress.trust = Math.min(100, progress.trust + 2);
        sendMortimerDialogue(player, "Fine, I'll keep close. But if you sprint off a ledge, that's a you problem.\n\nMortimer will follow you for a while. Press G near a seat to ask him to board.");
    }

    public void tryTravelWith(Player player) {
        Entity playerVehicle = player.getVehicle();

        if (playerVehicle != null) {
            ContraptionSeatTarget seat = findNearbyContraptionSeat(player, playerVehicle);
            if (seat != null) {
                this.targetContraptionSeatEntityId = seat.entity.getId();
                this.targetContraptionSeatIndex = seat.seatIndex;
                this.requestedBoardingPlayer = player.getUUID();
                this.getNavigation().moveTo(seat.entity.getX(), seat.entity.getY(), seat.entity.getZ(), 0.9D);
                sendMortimerDialogue(player, "I see the train. Give me a moment.");
                return;
            }
        }

        BlockPos lookedSeat = findLookedAtSeatBlock(player);
        if (lookedSeat != null) {
            this.targetSeatBlockPos = lookedSeat.immutable();
            this.seatedYaw = player.getYRot();
            this.requestedBoardingPlayer = player.getUUID();
            this.getNavigation().moveTo(lookedSeat.getX() + 0.5D, lookedSeat.getY() + 1.0D, lookedSeat.getZ() + 0.5D, 0.85D);
            sendMortimerDialogue(player, "I see the seat. Walking over.");
            return;
        }

        NpcShipStabilityGoal.initReflection();
        boolean playerOnShip = false;
        if (NpcShipStabilityGoal.sableHelper != null && NpcShipStabilityGoal.getTrackingSubLevel != null) {
            try {
                playerOnShip = NpcShipStabilityGoal.getTrackingSubLevel.invoke(
                        NpcShipStabilityGoal.sableHelper, player) != null;
            } catch (Exception ignored) {
            }
        }
        if (playerOnShip) {
            this.followingPlayer = player.getUUID();
            this.followTicksLeft = FOLLOW_DURATION_TICKS;
            this.getNavigation().moveTo(player.getX(), player.getY(), player.getZ(), 1.1D);
            sendMortimerDialogue(player, "I'll find my footing. Don't launch us until I'm aboard.");
            return;
        }

        sendMortimerDialogue(player, "Look directly at a seat and try again, or board first if this is an airship.");
    }

    private Entity findNearbyOpenSeat(Player player, Entity excludedVehicle) {
        AABB searchBox = this.getBoundingBox().inflate(24.0D);
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : this.level().getEntities(this, searchBox, this::looksLikeSeatEntity)) {
            if (entity == excludedVehicle) continue;
            if (entity.isVehicle()) continue;
            double distance = entity.distanceToSqr(this);
            if (distance < closestDist) {
                closestDist = distance;
                closest = entity;
            }
        }

        if (closest == null && excludedVehicle != null) {
            // Some seat implementations only expose vehicle entities near the rider, not near the NPC.
            AABB playerBox = player.getBoundingBox().inflate(24.0D);
            for (Entity entity : this.level().getEntities(player, playerBox, this::looksLikeSeatEntity)) {
                if (entity == excludedVehicle) continue;
                if (entity.isVehicle()) continue;
                double distance = entity.distanceToSqr(player);
                if (distance < closestDist) {
                    closestDist = distance;
                    closest = entity;
                }
            }
        }

        return closest;
    }


    private ContraptionSeatTarget findNearbyContraptionSeat(Player player, Entity playerVehicle) {
        Entity preferred = playerVehicle != null && looksLikeContraptionEntity(playerVehicle) ? playerVehicle : null;
        ContraptionSeatTarget target = preferred == null ? null : openContraptionSeat(preferred);
        if (target != null) return target;

        AABB box = player.getBoundingBox().inflate(24.0D);
        ContraptionSeatTarget closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Entity entity : this.level().getEntities(player, box, this::looksLikeContraptionEntity)) {
            ContraptionSeatTarget candidate = openContraptionSeat(entity);
            if (candidate == null) continue;
            double dist = entity.distanceToSqr(player);
            if (dist < closestDist) {
                closestDist = dist;
                closest = candidate;
            }
        }
        return closest;
    }

    private boolean looksLikeContraptionEntity(Entity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String id = key == null ? "" : key.toString().toLowerCase();
        String cls = entity.getClass().getName().toLowerCase();
        return id.contains("contraption") || id.contains("airship") || id.contains("ship") || cls.contains("contraption") || hasMethod(entity, "addSittingPassenger");
    }

    private boolean hasMethod(Entity entity, String methodName) {
        for (Class<?> c = entity.getClass(); c != null; c = c.getSuperclass()) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(methodName)) return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private ContraptionSeatTarget openContraptionSeat(Entity entity) {
        try {
            java.lang.reflect.Method getContraption = findMethod(entity.getClass(), "getContraption");
            if (getContraption == null) return null;
            Object contraption = getContraption.invoke(entity);
            if (contraption == null) return null;

            java.lang.reflect.Method getSeats = findMethod(contraption.getClass(), "getSeats");
            java.lang.reflect.Method getSeatMapping = findMethod(contraption.getClass(), "getSeatMapping");
            if (getSeats == null || getSeatMapping == null) return null;

            java.util.List<?> seats = (java.util.List<?>) getSeats.invoke(contraption);
            java.util.Map<?, ?> seatMapping = (java.util.Map<?, ?>) getSeatMapping.invoke(contraption);
            if (seats == null || seats.isEmpty()) return null;

            java.util.HashSet<Integer> occupied = new java.util.HashSet<>();
            if (seatMapping != null) {
                for (Object value : seatMapping.values()) {
                    if (value instanceof Integer i) occupied.add(i);
                }
            }
            for (int i = 0; i < seats.size(); i++) {
                if (!occupied.contains(i)) return new ContraptionSeatTarget(entity, i);
            }
        } catch (Exception ignored) {
            // Reflection-based Create compatibility: fail safely if the contraption implementation differs.
        }
        return null;
    }

    private java.lang.reflect.Method findMethod(Class<?> cls, String name, Class<?>... params) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                java.lang.reflect.Method m = c.getDeclaredMethod(name, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    private boolean mountContraptionSeat(Entity contraptionEntity, int seatIndex) {
        String[] methodNames = {"addSittingPassenger", "addPassengerWithSeat", "setSeat"};
        for (String name : methodNames) {
            try {
                Method method = findMethod(contraptionEntity.getClass(), name, Entity.class, int.class);
                if (method == null) continue;
                method.invoke(contraptionEntity, this, seatIndex);
                if (this.isPassenger()) return true;
            } catch (Exception ignored) {
            }
        }
        try {
            return this.startRiding(contraptionEntity, true);
        } catch (Exception ignored) {
        }
        return false;
    }


    private BlockPos findLookedAtSeatBlock(Player player) {
        try {
            Vec3 start = player.getEyePosition(1.0F);
            Vec3 look = player.getLookAngle();
            Vec3 end = start.add(look.scale(8.0D));
            BlockHitResult hit = this.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            if (hit.getType() != HitResult.Type.BLOCK) return null;
            BlockPos pos = hit.getBlockPos();
            BlockState state = this.level().getBlockState(pos);
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (key == null) return null;
            String id = key.toString().toLowerCase();
            if (isSeatLikeBlockId(id)) {
                return pos.immutable();
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private BlockPos findNearbySeatBlock(Player player) {
        BlockPos closest = null;
        double closestDist = Double.MAX_VALUE;

        BlockPos[] centers = new BlockPos[] { player.blockPosition(), this.blockPosition() };
        for (BlockPos center : centers) {
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-14, -6, -14), center.offset(14, 8, 14))) {
                BlockState state = this.level().getBlockState(pos);
                ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                if (key == null) continue;
                String id = key.toString().toLowerCase();
                if (!isSeatLikeBlockId(id)) continue;
                if (player.getVehicle() != null && distanceToBlockCenterSqr(pos, player.getVehicle()) <= 1.6D) continue;
                double dist = Math.min(distanceToBlockCenterSqr(pos, this), distanceToBlockCenterSqr(pos, player));
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = pos.immutable();
                }
            }
        }
        return closest;
    }

    private boolean isSeatLikeBlockId(String id) {
        return id.contains("seat") || id.contains("chair") || id.contains("stool") || id.contains("bench");
    }

    private boolean tryCreateSeatSitDown(BlockPos pos) {
        try {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(this.level().getBlockState(pos).getBlock());
            if (blockId == null || !"create".equals(blockId.getNamespace())) return false;
            Class<?> seatBlockClass = Class.forName("com.simibubi.create.content.contraptions.actors.seat.SeatBlock");
            Method sitDown;
            try {
                sitDown = seatBlockClass.getMethod("sitDown",
                        net.minecraft.world.level.Level.class, BlockPos.class,
                        net.minecraft.world.entity.LivingEntity.class);
            } catch (NoSuchMethodException exception) {
                sitDown = seatBlockClass.getMethod("sitDown",
                        net.minecraft.world.level.Level.class, BlockPos.class,
                        net.minecraft.world.entity.Entity.class);
            }
            sitDown.invoke(null, this.level(), pos, this);
            boolean mounted = this.isPassenger();
            if (mounted) {
                this.pretendSittingTicks = 20 * 60;
                this.entityData.set(DATA_VISUALLY_SITTING, true);
            } else {
                this.lastSeatFailure = "Create SeatBlock.sitDown ran, but Mortimer did not become a passenger.";
            }
            lockSeatedRotation();
            return mounted;
        } catch (Throwable t) {
            this.lastSeatFailure = "Create SeatBlock.sitDown failed: " + t.getClass().getSimpleName();
            return false;
        }
    }

    private boolean spawnGenericCreateSeatEntity(BlockPos pos) {
        try {
            Class<?> seatEntityClass = Class.forName("com.simibubi.create.content.contraptions.actors.seat.SeatEntity");
            Object seatEntityObject = seatEntityClass.getConstructor(Level.class).newInstance(this.level());
            if (!(seatEntityObject instanceof Entity seatEntity)) return false;
            seatEntity.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            this.level().addFreshEntity(seatEntity);
            boolean mounted = this.startRiding(seatEntity, true);
            if (mounted) {
                this.pretendSittingTicks = 20 * 60;
                this.entityData.set(DATA_VISUALLY_SITTING, true);
            } else {
                seatEntity.discard();
                this.lastSeatFailure = "Spawned a Create SeatEntity, but Mortimer could not ride it.";
            }
            lockSeatedRotation();
            return mounted;
        } catch (Throwable t) {
            this.lastSeatFailure = "Generic Create SeatEntity fallback failed: " + t.getClass().getSimpleName();
            return false;
        }
    }

    private boolean tryAnySeatMount(BlockPos pos) {
        this.lastSeatFailure = "";
        if (tryCreateSeatSitDown(pos)) return true;
        // Use Create's invisible SeatEntity as a generic ride point for other chair blocks too.
        // This lets Mortimer sit on Bits n Bobs / decorative chairs without needing their APIs.
        return spawnGenericCreateSeatEntity(pos);
    }

    private void lockSeatedRotation() {
        this.setYRot(seatedYaw);
        this.setYHeadRot(seatedYaw);
        this.yBodyRot = seatedYaw;
        this.yHeadRot = seatedYaw;
        this.yRotO = seatedYaw;
        this.yHeadRotO = seatedYaw;
        this.yBodyRotO = seatedYaw;
    }

    private void tickThoughtBubble() {
        if (this.level().isClientSide) return;
        if (this.isPassenger() || this.isVisuallySitting()) return;
        if (emoteCooldown > 0) {
            emoteCooldown--;
            return;
        }
        Player nearby = this.level().getNearestPlayer(this, 8.0D);
        if (nearby != null && this.random.nextFloat() < 0.45F) {
            showEmote(randomOf("🔧", "☕", "⚙", "💭", "🧭"));
        }
        emoteCooldown = 20 * 100 + this.random.nextInt(20 * 140);
    }

    private void showEmote(String icon) {
        emotesShown++;
        this.setCustomName(Component.literal("§6Mortimer §f" + icon));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 3;
    }

    private double distanceToBlockCenterSqr(BlockPos pos, Entity entity) {
        double dx = (pos.getX() + 0.5D) - entity.getX();
        double dy = (pos.getY() + 0.5D) - entity.getY();
        double dz = (pos.getZ() + 0.5D) - entity.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private boolean looksLikeSeatEntity(Entity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        String id = key.toString().toLowerCase();
        return id.contains("seat") || id.contains("chair") || id.contains("stool")
                || id.contains("contraption") || id.contains("carriage") || id.contains("airship");
    }

    private void scanSeatEnvironment(Player player) {
        player.sendSystemMessage(Component.literal("§6[Mortimer Seat Scan] §7Nearby entity/block ids that look relevant:"));

        int count = 0;
        AABB box = player.getBoundingBox().inflate(20.0D);
        for (Entity entity : this.level().getEntities(player, box, e -> true)) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            String id = key == null ? "unknown" : key.toString();
            String low = id.toLowerCase();
            if (low.contains("seat") || low.contains("chair") || low.contains("stool") || low.contains("contraption") || low.contains("aero") || low.contains("ship") || low.contains("carriage")) {
                count++;
                player.sendSystemMessage(Component.literal("§8Entity §b" + id + "§8 dist=" + Math.round(Math.sqrt(entity.distanceToSqr(player)) * 10.0) / 10.0 + " vehicle=" + entity.isVehicle() + " passengers=" + entity.getPassengers().size()));
            }
        }

        BlockPos center = player.blockPosition();
        int blockCount = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -4, -8), center.offset(8, 6, 8))) {
            BlockState state = this.level().getBlockState(pos);
            Block block = state.getBlock();
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
            if (key == null) continue;
            String id = key.toString().toLowerCase();
            if (id.contains("seat") || id.contains("chair") || id.contains("stool") || id.contains("aero") || id.contains("contraption") || id.contains("ship")) {
                blockCount++;
                if (blockCount <= 12) {
                    player.sendSystemMessage(Component.literal("§8Block §b" + key + "§8 at " + pos.toShortString()));
                }
            }
        }

        if (count == 0 && blockCount == 0) {
            player.sendSystemMessage(Component.literal("§eNo obvious seat/ship entities or blocks found nearby. For Sable airships, board first and ask Mortimer to follow aboard."));
        } else {
            player.sendSystemMessage(Component.literal("§7Found " + count + " relevant entities and " + blockCount + " relevant blocks."));
        }
    }

    private void tickBoardingTarget() {
        if (this.isPassenger()) return;

        if (this.targetContraptionSeatEntityId >= 0) {
            Entity contraption = this.level().getEntity(this.targetContraptionSeatEntityId);
            if (contraption == null || !contraption.isAlive()) {
                this.targetContraptionSeatEntityId = -1;
                this.targetContraptionSeatIndex = -1;
                this.requestedBoardingPlayer = null;
                return;
            }
            this.getNavigation().moveTo(contraption.getX(), contraption.getY(), contraption.getZ(), 0.9D);
            if (this.distanceTo(contraption) <= 8.0F) {
                boolean mounted = mountContraptionSeat(contraption, this.targetContraptionSeatIndex);
                Player player = this.requestedBoardingPlayer == null ? null : this.level().getPlayerByUUID(this.requestedBoardingPlayer);
                if (mounted) {
                    if (player != null) say("Mortimer - Aeromancer", "Hah. Contraption seat accepted me. Tell no one I looked surprised.", player);
                } else if (player != null) {
                    say("Mortimer - Aeromancer", "I found the contraption seat, but it refused boarding. We'll need the airship mod's own passenger rules next.", player);
                }
                this.targetContraptionSeatEntityId = -1;
                this.targetContraptionSeatIndex = -1;
                this.requestedBoardingPlayer = null;
            }
            return;
        }

        if (this.targetSeatId >= 0) {
            Entity seat = this.level().getEntity(this.targetSeatId);
            if (seat == null || !seat.isAlive()) {
                this.targetSeatId = -1;
                this.requestedBoardingPlayer = null;
                return;
            }

            if (seat.isVehicle()) {
                this.targetSeatId = -1;
                this.requestedBoardingPlayer = null;
                return;
            }

            this.getNavigation().moveTo(seat.getX(), seat.getY(), seat.getZ(), 0.85D);

            if (this.distanceTo(seat) <= 1.85F) {
                boolean mounted = this.startRiding(seat, true);
                Player player = null;
                if (this.requestedBoardingPlayer != null) {
                    player = this.level().getPlayerByUUID(this.requestedBoardingPlayer);
                }
                if (mounted) {
                    this.seatedYaw = player == null ? this.getYRot() : player.getYRot();
                    lockSeatedRotation();
                    if (player != null) {
                        PlayerProgress progress = getProgress(player);
                        progress.trust = Math.min(100, progress.trust + 3);
                        say("Mortimer - Aeromancer", "There. Seated. Now try not to fly like a banker budgets.", player);
                    }
                } else {
                    // If a seat entity exists but rejects riding, still show the sitting pose for testing.
                    this.pretendSittingTicks = 20 * 60;
                    this.setPos(seat.getX(), seat.getY() + 0.05D, seat.getZ());
                    lockSeatedRotation();
                    if (player != null) {
                        say("Mortimer - Aeromancer", "Seat's there, but it won't take me properly. I'll sit the old-fashioned way while we study the chair's manners.", player);
                        player.sendSystemMessage(Component.literal("§eMortimer reached the seat entity but could not mount it. Forced visual sit pose enabled for testing."));
                    }
                }
                this.targetSeatId = -1;
                this.requestedBoardingPlayer = null;
            }
            return;
        }

        if (this.targetSeatBlockPos != null) {
            BlockPos pos = this.targetSeatBlockPos;
            this.getNavigation().moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 0.85D);
            if (this.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D) <= 2.6D) {
                this.getNavigation().stop();
                Player player = null;
                if (this.requestedBoardingPlayer != null) {
                    player = this.level().getPlayerByUUID(this.requestedBoardingPlayer);
                }

                this.seatedYaw = player == null ? this.getYRot() : player.getYRot();
                boolean seatMounted = tryAnySeatMount(pos);

                if (!seatMounted) {
                    // Final fallback for any chair-like block: visual sit only.
                    this.pretendSittingTicks = 20 * 60;
                    this.setPos(pos.getX() + 0.5D, pos.getY() + 0.15D, pos.getZ() + 0.5D);
                    lockSeatedRotation();
                }

                if (player != null) {
                    if (seatMounted) {
                        PlayerProgress progress = getProgress(player);
                        progress.trust = Math.min(100, progress.trust + 3);
                        say("Mortimer - Aeromancer", "There. Seated properly. Try not to make the old man regret trusting your upholstery.", player);
                        player.sendSystemMessage(Component.literal("§aMortimer mounted a Create-style seat entity at the target chair/seat position."));
                    } else {
                        say("Mortimer - Aeromancer", "This chair still has no proper riding point I can grab, but the sitting pose works. We'll keep studying the upholstery.", player);
                        if (!lastSeatFailure.isEmpty()) player.sendSystemMessage(Component.literal("§eSeat debug: " + lastSeatFailure));
                        player.sendSystemMessage(Component.literal("§eMortimer is visually sitting near the chair block. True moving-ship seating may need Sable/Create Aeronautics-specific handling."));
                    }
                }
                this.targetSeatBlockPos = null;
                this.requestedBoardingPlayer = null;
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            this.entityData.set(DATA_VISUALLY_SITTING, this.isPassenger() || this.pretendSittingTicks > 0);
        }

        if (this.level().isClientSide) return;

        if (pretendSittingTicks > 0) {
            pretendSittingTicks--;
            this.getNavigation().stop();
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            lockSeatedRotation();
        }
        if (this.isPassenger()) {
            this.getNavigation().stop();
            lockSeatedRotation();
        }

        tickBoardingTarget();
        tickFollowTarget();
        tickMountedTrust();
        tickCoupleDialogue();
        tickRoutineDialogue();
        tickThoughtBubble();
        tickScheduledAnchor();

        if (speechTicksLeft > 0) {
            speechTicksLeft--;
            if (speechTicksLeft == 0) {
                this.setCustomNameVisible(false);
                this.setCustomName(Component.literal("Mortimer - Aeromancer"));
            }
        }

        if (ambientCooldown > 0) {
            ambientCooldown--;
            return;
        }

        Player nearby = this.level().getNearestPlayer(this, 9.0D);
        if (nearby != null && !this.isVehicle()) {
            String statLine = pickStatLine(nearby);
            if (statLine != null && this.random.nextFloat() < 0.25f) {
                say("Mortimer - Aeromancer", statLine, nearby);
                ambientCooldown = MIN_AMBIENT_COOLDOWN + this.random.nextInt(AMBIENT_RANDOM_EXTRA);
                return;
            }
            say("Mortimer - Aeromancer", pickAmbientLine(nearby), nearby);
        }
        resetAmbientCooldown();
    }


    private void tickCoupleDialogue() {
        if (coupleCooldown > 0) {
            coupleCooldown--;
            return;
        }
        AABB box = this.getBoundingBox().inflate(6.0D);
        java.util.List<GeeraEntity> geeras = this.level().getEntitiesOfClass(GeeraEntity.class, box);
        if (geeras.isEmpty()) {
            coupleCooldown = 20 * 40;
            return;
        }
        String[] lines = new String[] {
                "Geera, I have never once taught travelers that banks are weather events. I said natural disasters.",
                "Fine, yes, I borrowed the rope. But only because your knots are prettier than mine.",
                "Geera has never been wrong about fish, ropes, or me being an idiot. Annoying habit.",
                "If anyone asks, I was not flirting. I was appreciating goblin engineering. Entirely different craft.",
                "Scoria's fine clothes still worry me. Geera says I'm being dramatic, which is rude because she's right."
        };
        coupleLinesSpoken++;
        showEmote(randomOf("💚", "☕", "⚙", "🪢"));
        say("Mortimer - Aeromancer", lines[this.random.nextInt(lines.length)]);
        coupleCooldown = 20 * 100 + this.random.nextInt(20 * 120);
    }

    public void hearGeeraNearby() {
        if (this.level().isClientSide) return;
        if (this.random.nextFloat() < 0.55F) return;
        String[] lines = new String[] {
                "I did not say banks eat souls. I said they nibble.",
                "That rope was for safety, love. Mostly mine.",
                "Geera, you're making me sound charming. Dangerous precedent.",
                "I packed lunch once. It became engine insulation. Long story."
        };
        say("Mortimer - Aeromancer", lines[this.random.nextInt(lines.length)]);
    }

    private void tickRoutineDialogue() {
        tickGuildWorkTarget();
        if (routineCooldown > 0) {
            routineCooldown--;
            return;
        }
        routineCooldown = 20 * 180 + this.random.nextInt(20 * 180);
        long day = this.level().getDayTime() / 24000L;
        if (day % 15 == 0) {
            guildMeetingsMentioned++;
            guildWorkTarget = findGuildWorkTarget();
            guildWorkTicks = 20 * 90;
            say("Mortimer - Aeromancer", "Guild meeting day soon. Old crew, older arguments, same awful chairs.");
        } else if (day % 9 == 0) {
            guildMeetingsMentioned++;
            guildWorkTarget = findGuildWorkTarget();
            guildWorkTicks = 20 * 70;
            say("Mortimer - Aeromancer", "I should check the guild board. If anyone posted cheap engine work again, I'm confiscating their wrench.");
        }
    }

    private void tickGuildWorkTarget() {
        if (guildWorkTicks <= 0 || guildWorkTarget == null || this.isPassenger() || pretendSittingTicks > 0) return;
        guildWorkTicks--;
        double dist = this.distanceToBlockCenterSqr(guildWorkTarget, this);
        if (dist > 5.0D) {
            this.getNavigation().moveTo(guildWorkTarget.getX() + 0.5D, guildWorkTarget.getY(), guildWorkTarget.getZ() + 0.5D, 0.65D);
        } else {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(guildWorkTarget.getX() + 0.5D, guildWorkTarget.getY() + 0.5D, guildWorkTarget.getZ() + 0.5D, 20.0F, 20.0F);
            if (this.random.nextFloat() < 0.006F) {
                say("Mortimer - Aeromancer", randomOf("Guild board still smells like wet paper and ambition.", "Cheap engine work. I knew it. Confiscation remains on the table.", "Old crew would laugh at this posting. Then complain anyway."));
            }
        }
        if (guildWorkTicks <= 0) guildWorkTarget = null;
    }

    private void tickScheduledAnchor() {
        if (this.isPassenger()
                || pretendSittingTicks > 0
                || targetSeatId >= 0
                || targetSeatBlockPos != null
                || targetContraptionSeatEntityId >= 0
                || followingPlayer != null
                || guildWorkTicks > 0) {
            return;
        }

        if (scheduleCooldown > 0) {
            scheduleCooldown--;
        }

        if (scheduledTarget == null && scheduleCooldown <= 0) {
            scheduledTarget = getScheduledAnchor();
            scheduleCooldown = 20 * 30 + this.random.nextInt(20 * 30);
        }

        if (scheduledTarget == null) return;

        BlockPos activeAnchor = getScheduledAnchor();
        if (activeAnchor == null || !activeAnchor.equals(scheduledTarget)) {
            scheduledTarget = null;
            return;
        }

        double dist = this.distanceToBlockCenterSqr(scheduledTarget, this);
        if (dist > 6.25D) {
            this.getNavigation().moveTo(scheduledTarget.getX() + 0.5D, scheduledTarget.getY(), scheduledTarget.getZ() + 0.5D, 0.62D);
        } else {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(scheduledTarget.getX() + 0.5D, scheduledTarget.getY() + 0.5D, scheduledTarget.getZ() + 0.5D, 20.0F, 20.0F);
        }
    }

    private BlockPos findGuildWorkTarget() {
        if (manualGuildAnchor != null) return manualGuildAnchor;
        BlockPos center = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-14, -4, -14), center.offset(14, 4, 14))) {
            BlockState state = this.level().getBlockState(pos);
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String id = key == null ? "" : key.toString();
            if (id.contains("lectern") || id.contains("bell") || id.contains("smithing_table") || id.contains("crafting_table") || id.contains("depot") || id.contains("gearbox")) {
                return pos.immutable();
            }
        }
        return null;
    }

    private void tickFollowTarget() {
        if (followingPlayer == null || followTicksLeft <= 0 || this.isPassenger() || pretendSittingTicks > 0 || targetSeatId >= 0 || targetSeatBlockPos != null) {
            return;
        }
        followTicksLeft--;
        Player player = this.level().getPlayerByUUID(followingPlayer);
        if (player == null || player.isRemoved()) {
            followingPlayer = null;
            followTicksLeft = 0;
            return;
        }

        boolean playerOnShipEarly = isTrackedBySable(player);
        if (!playerOnShipEarly && player.distanceTo(this) > 80.0F) {
            followingPlayer = null;
            followTicksLeft = 0;
            return;
        }

        double dist = this.distanceToSqr(player);
        boolean playerOnShip = playerOnShipEarly;
        boolean mortimerOnShip = isTrackedBySable(this);

        if (mortimerOnShip) {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(player, 20.0F, 20.0F);
        } else if (dist > 6.25D || playerOnShip) {
            double speed = dist > 64.0D ? 1.1D : (playerOnShip ? 1.0D : 0.8D);
            this.getNavigation().moveTo(player, speed);
        } else {
            this.getNavigation().stop();
            this.getLookControl().setLookAt(player, 20.0F, 20.0F);
        }
    }

    @Override
    public boolean isFollowingPlayer() {
        return followingPlayer != null && followTicksLeft > 0;
    }

    private boolean isTrackedBySable(Entity entity) {
        NpcShipStabilityGoal.initReflection();
        if (NpcShipStabilityGoal.sableHelper == null || NpcShipStabilityGoal.getTrackingSubLevel == null) {
            return false;
        }
        try {
            return NpcShipStabilityGoal.getTrackingSubLevel.invoke(NpcShipStabilityGoal.sableHelper, entity) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void tickMountedTrust() {
        if (!this.isPassenger()) return;
        if (mountedTrustCooldown > 0) {
            mountedTrustCooldown--;
            return;
        }
        mountedTrustCooldown = 20 * 45;
        Entity vehicle = this.getVehicle();
        if (vehicle == null) return;
        for (Player player : this.level().players()) {
            if (player.distanceTo(this) <= 24.0F || player.getVehicle() == vehicle) {
                PlayerProgress progress = getProgress(player);
                progress.trust = Math.min(100, progress.trust + 1);
                    if (this.random.nextFloat() < 0.35F) {
                        travelLinesSpoken++;
                        say("Mortimer - Aeromancer", pickTravelLine(player), player);
                    }
            }
        }
    }

    private String pickTravelLine(Player player) {
        return randomOf(
                "Keep her steady, " + player.getName().getString() + ". The sky likes confidence, not arrogance.",
                "If the compass twitches, we listen. I ignored drift once. Once was enough.",
                "Good lift. Bad upholstery. We'll survive both.",
                "Scoria would have an equation for this wind. I have tea and suspicion.",
                "Geera says I relax on flights. Geera lies beautifully.",
                "Altitude is honest. It tells you exactly when you have been careless."
        );
    }

    private String pickStatLine(Player player) {
        int deaths = PlayerStatsTracker.getDeaths(player);
        int islands = PlayerStatsTracker.getIslandsDiscovered(player);
        String shipName = PlayerStatsTracker.getShipName(player);
        long daysSince = PlayerStatsTracker.getDaysSinceLastVisit(player, "mortimer");

        if (daysSince >= 5) return "Haven't seen you in " + daysSince + " days. Skies treat you well, I hope.";
        if (deaths >= 20) return "I've buried count of how many times I nearly didn't make it. You learn to stop counting.";
        if (deaths >= 10 && this.random.nextFloat() < 0.5f) return "You're still here. That says something. Not sure what yet, but something.";
        if (islands >= 15 && this.random.nextFloat() < 0.4f) return "You've been further than I have in the last few years. Don't tell Geera.";
        if (islands >= 5 && this.random.nextFloat() < 0.3f) return "You've got that look. Like someone who's seen more sky than they expected to.";
        if (shipName != null && this.random.nextFloat() < 0.3f) return "A ship named " + shipName + ". Good name. A ship needs a name before she'll trust you.";
        return null;
    }

    private TimeOfDay getTimeOfDay() {
        long t = this.level().getDayTime() % 24000L;
        if (t < 6000) return TimeOfDay.MORNING;
        if (t < 12000) return TimeOfDay.AFTERNOON;
        if (t < 18000) return TimeOfDay.EVENING;
        return TimeOfDay.NIGHT;
    }

    private String getWeather() {
        if (this.level().isThundering()) return "STORM";
        if (this.level().isRaining()) return "RAIN";
        if (this.random.nextFloat() < 0.20f) return "OVERCAST";
        return "CLEAR";
    }

    private String pickAmbientLine(Player player) {
        if (this.isPassenger()) {
            return randomOf("Hear that wobble? Left side's carrying guilt.", "Keep her steady. The sky forgives, but not often.", "Good altitude. Bad confidence. We'll work on both.", "Scoria would call this propulsion inefficiency. Clever lad. Banker nonsense aside.", "If the compass twitches, we listen. I ignored a drift once. Once was enough.", "Nice lift. Don't get smug. The sky hates smug.");
        }

        if (nearCreateBlock()) {
            return randomOf("Belts solve more problems than politicians.", "That machine's talking. You listening?", "Create kit nearby. Good. Civilization hasn't fully collapsed.", "Hear that? Someone skipped maintenance.");
        }

        if (player.getFoodData().getFoodLevel() <= 8) {
            return "You eat yet, " + player.getName().getString() + "? Can't engineer on an empty stomach.";
        }

        if (this.random.nextFloat() < 0.40f) {
            String weather = getWeather();
            switch (weather) {
                case "STORM" -> {
                    return randomOf(
                            "Get inside.",
                            "I've made the mistake of flying in weather like this exactly once.",
                            "Storm like this — the guild used to call them three-anchor nights. You stayed put and counted your blessings.",
                            "Listen to it. That's not wind. That's the sky telling you something."
                    );
                }
                case "RAIN" -> {
                    return randomOf(
                            "Don't fly in this unless you have to. I mean it.",
                            "Geera loves the rain. I tolerate it.",
                            "Rain sounds different at altitude. I still miss that."
                    );
                }
                case "OVERCAST" -> {
                    return randomOf(
                            "Grey sky means grey thinking. I've learned to wait it out.",
                            "Couldn't read these clouds when I was young. Took me years.",
                            "Overcast is the sky being honest. It's not always a threat."
                    );
                }
                case "CLEAR" -> {
                    return randomOf(
                            "Good flying weather. Don't waste it.",
                            "This kind of clear doesn't last. Enjoy it.",
                            "Take the long route. On a day like this, you earn the view."
                    );
                }
            }
        }

        if (this.random.nextFloat() < 0.3F) {
            return randomOf(
                    "Builders Tea fixes most things. The rest require torque.",
                    "No island survives alone.",
                    "The skies reward those who adapt.",
                    "Geera says I track grease into the house. Slander, mostly.",
                    "Never trust a banker with clean cuffs.",
                    "Goblins have excellent mechanical intuition. Fantastic ears too. For engines. Mostly.",
                    "Old ships don't creak. They reminisce.",
                    "Tighten bolts twice. Sky only gives second chances to liars."
            );
        }

        return switch (getTimeOfDay()) {
            case MORNING -> randomOf(
                    "Up early? Good. The sky makes more sense in the morning.",
                    "Morning clouds lie. They look soft. They're not.",
                    "The light this time of day — used to be my favourite part of a voyage.",
                    "Sleep well? The gusts were strange last night."
            );
            case AFTERNOON -> randomOf(
                    "This is the dead hour. Too late to start, too early to stop.",
                    "Seen Geera? She had that look this morning. I may have said something.",
                    "Afternoon shifts everything west. Keep that in mind if you're flying.",
                    "Not sleeping. Thinking. There's a difference."
            );
            case EVENING -> randomOf(
                    "That's the light I used to navigate by. The gold hour, we called it.",
                    "Day's ending. Never quite the same feeling twice.",
                    "You know what I miss? Watching the sunset from altitude. Changes everything.",
                    "Almost time. Come back tomorrow if you've got questions."
            );
            case NIGHT -> randomOf(
                    "Thought I saw a ship light out past the eastern ridge. Probably nothing.",
                    "Old habit — I still check the anchor lines before bed. No ship. Still check.",
                    "You're up late. So am I. Doesn't mean I want to talk about it.",
                    "The sky at night is a different sky. Takes getting used to."
            );
        };
    }

    private boolean nearCreateBlock() {
        BlockPos center = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -2, -4), center.offset(4, 3, 4))) {
            BlockState state = this.level().getBlockState(pos);
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            if (key != null && "create".equals(key.getNamespace())) {
                return true;
            }
        }
        return false;
    }

    private void say(String speaker, String text) {
        say(speaker, text, null);
    }

    private void sayTagOnly(String speaker, String text) {
        this.setCustomName(Component.literal("§6" + speaker + "§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = SPEECH_TICKS;
    }

    private void sendMortimerDialogue(Player player, String body) {
        sayTagOnly("Mortimer - Aeromancer", body);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Mortimer - Aeromancer",
                    body,
                    "Discuss work",
                    "Travel / Board",
                    "Mortimer waits for your answer."
            ));
        }
    }

    private void say(String speaker, String text, Player listener) {
        this.setCustomName(Component.literal("§6" + speaker + "§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = SPEECH_TICKS;

        // Debug-friendly for now: ambient and dialogue lines also print to chat.
        // Later this can be config-gated once the floating UI is easier to notice.
        Component chatLine = Component.literal("§6" + speaker + "§7: " + text);
        if (listener != null) {
            listener.sendSystemMessage(chatLine);
        } else {
            for (Player p : this.level().players()) {
                if (p.distanceTo(this) <= 12.0F) {
                    p.sendSystemMessage(chatLine);
                }
            }
        }
    }

    private void resetAmbientCooldown() {
        this.ambientCooldown = MIN_AMBIENT_COOLDOWN + this.random.nextInt(AMBIENT_RANDOM_EXTRA);
    }

    private String randomOf(String... lines) {
        return lines[this.random.nextInt(lines.length)];
    }

    private PlayerProgress getProgress(Player player) {
        return progressByPlayer.computeIfAbsent(player.getUUID(), uuid -> new PlayerProgress());
    }

    public int getTrustForPlayer(UUID playerUUID) {
        return progressByPlayer.getOrDefault(playerUUID, new PlayerProgress()).trust;
    }

    private String relationshipName(int trust) {
        if (trust >= 80) return "Crew";
        if (trust >= 55) return "Friend";
        if (trust >= 35) return "Trusted Pilot";
        if (trust >= 15) return "Guild Associate";
        return "Stranger";
    }

    private boolean hasItem(Player player, String itemId, int count) {
        return countItem(player, itemId) >= count;
    }

    private int countItem(Player player, String itemId) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (itemId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void consumeItem(Player player, String itemId, int count) {
        int remaining = count;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (itemId.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                int remove = Math.min(remaining, stack.getCount());
                stack.shrink(remove);
                remaining -= remove;
            }
        }
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
            playerTag.putInt("QuestStage", entry.getValue().questStage);
            playerTag.putBoolean("HasMet", entry.getValue().hasMet);
            list.add(playerTag);
        }
        tag.put("MortimerProgress", list);
        tag.putInt("CoupleCooldown", coupleCooldown);
        tag.putInt("RoutineCooldown", routineCooldown);
        tag.putInt("EmoteCooldown", emoteCooldown);
        tag.putInt("GuildMeetingsMentioned", guildMeetingsMentioned);
        tag.putInt("CoupleLinesSpoken", coupleLinesSpoken);
        tag.putInt("EmotesShown", emotesShown);
        tag.putInt("TravelLinesSpoken", travelLinesSpoken);
        tag.putInt("RelationshipChecks", relationshipChecks);
        tag.putInt("LogbookOpens", logbookOpens);
        if (manualGuildAnchor != null) { tag.putInt("ManualGuildX", manualGuildAnchor.getX()); tag.putInt("ManualGuildY", manualGuildAnchor.getY()); tag.putInt("ManualGuildZ", manualGuildAnchor.getZ()); }
        if (morningAnchor != null) { tag.putInt("MorningAnchorX", morningAnchor.getX()); tag.putInt("MorningAnchorY", morningAnchor.getY()); tag.putInt("MorningAnchorZ", morningAnchor.getZ()); }
        if (afternoonAnchor != null) { tag.putInt("AfternoonAnchorX", afternoonAnchor.getX()); tag.putInt("AfternoonAnchorY", afternoonAnchor.getY()); tag.putInt("AfternoonAnchorZ", afternoonAnchor.getZ()); }
        if (eveningAnchor != null) { tag.putInt("EveningAnchorX", eveningAnchor.getX()); tag.putInt("EveningAnchorY", eveningAnchor.getY()); tag.putInt("EveningAnchorZ", eveningAnchor.getZ()); }
        tag.putInt("NextScheduleAnchorSlot", nextScheduleAnchorSlot);
        if (followingPlayer != null) {
            tag.putUUID("FollowingPlayer", followingPlayer);
            tag.putInt("FollowTicksLeft", followTicksLeft);
        }
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
        if (tag.contains("CoupleCooldown")) coupleCooldown = tag.getInt("CoupleCooldown");
        if (tag.contains("RoutineCooldown")) routineCooldown = tag.getInt("RoutineCooldown");
        if (tag.contains("EmoteCooldown")) emoteCooldown = tag.getInt("EmoteCooldown");
        if (tag.contains("GuildMeetingsMentioned")) guildMeetingsMentioned = tag.getInt("GuildMeetingsMentioned");
        if (tag.contains("CoupleLinesSpoken")) coupleLinesSpoken = tag.getInt("CoupleLinesSpoken");
        if (tag.contains("EmotesShown")) emotesShown = tag.getInt("EmotesShown");
        if (tag.contains("TravelLinesSpoken")) travelLinesSpoken = tag.getInt("TravelLinesSpoken");
        if (tag.contains("RelationshipChecks")) relationshipChecks = tag.getInt("RelationshipChecks");
        if (tag.contains("LogbookOpens")) logbookOpens = tag.getInt("LogbookOpens");
        if (tag.contains("ManualGuildX")) manualGuildAnchor = new BlockPos(tag.getInt("ManualGuildX"), tag.getInt("ManualGuildY"), tag.getInt("ManualGuildZ"));
        if (tag.contains("MorningAnchorX")) morningAnchor = new BlockPos(tag.getInt("MorningAnchorX"), tag.getInt("MorningAnchorY"), tag.getInt("MorningAnchorZ"));
        if (tag.contains("AfternoonAnchorX")) afternoonAnchor = new BlockPos(tag.getInt("AfternoonAnchorX"), tag.getInt("AfternoonAnchorY"), tag.getInt("AfternoonAnchorZ"));
        if (tag.contains("EveningAnchorX")) eveningAnchor = new BlockPos(tag.getInt("EveningAnchorX"), tag.getInt("EveningAnchorY"), tag.getInt("EveningAnchorZ"));
        if (tag.contains("NextScheduleAnchorSlot")) nextScheduleAnchorSlot = tag.getInt("NextScheduleAnchorSlot");
        if (tag.hasUUID("FollowingPlayer")) {
            followingPlayer = tag.getUUID("FollowingPlayer");
            followTicksLeft = tag.getInt("FollowTicksLeft");
        }
        if (tag.contains("MortimerProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("MortimerProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                progress.questStage = playerTag.getInt("QuestStage");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<StorykeeperEntity> state) {
        if (this.isPassenger() || this.pretendSittingTicks > 0) {
            state.setAnimation(SIT_ANIM);
        } else if (state.isMoving()) {
            state.setAnimation(WALK_ANIM);
        } else {
            state.setAnimation(IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static class ContraptionSeatTarget {
        final Entity entity;
        final int seatIndex;
        ContraptionSeatTarget(Entity entity, int seatIndex) {
            this.entity = entity;
            this.seatIndex = seatIndex;
        }
    }

    private enum TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

    private static class PlayerProgress {
        int trust = 0;
        int questStage = 0;
        boolean hasMet = false;
    }
}
