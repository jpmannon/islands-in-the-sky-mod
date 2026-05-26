package com.quickqwek.ciskspawn.entity;

import com.quickqwek.ciskspawn.ai.NpcAnchorReturnGoal;
import com.quickqwek.ciskspawn.ai.NpcShipStabilityGoal;
import com.quickqwek.ciskspawn.ai.NpcWaypointGoal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializers;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import com.quickqwek.ciskspawn.network.MortimerDialoguePayload;
import com.quickqwek.ciskspawn.server.PlayerStatsTracker;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Geera - fisherwoman, goblin, Mortimer's wife.
 *
 * First-pass implementation:
 * - GeckoLib idle/walk animations.
 * - Right-click fishing quest prototype using Starcatcher item ids.
 * - No hard Starcatcher class dependency; item lookup is registry-based.
 */
public class GeeraEntity extends PathfinderMob implements GeoEntity {
    // Geera's latest Blockbench export had the visible idle/walk names effectively swapped in-game.
    // This intentionally swaps the controller mapping so she idles with "walk" and moves with "animation".
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("animation");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int ambientCooldown = 20 * 154;
    private BlockPos homePos = null;
    private int coupleCooldown = 20 * 25;
    private int routineCooldown = 20 * 60;
    private int speechTicksLeft = 0;
    private int emoteCooldown = 20 * 55;
    private BlockPos shopWorkTarget = null;
    private BlockPos manualShopAnchor = null;
    private int stationCooldown = 20 * 20;
    private int shopOperateTicks = 0;
    private int baitBundlesSold = 0;
    private int rumorsSold = 0;
    private int fishBought = 0;
    private int coupleLinesSpoken = 0;
    private int emotesShown = 0;
    private int workSessionsCompleted = 0;
    private int logbookOpens = 0;

    private static final int SPEECH_TICKS = 20 * 7;
    private static final int ROUTINE_CHECK_TICKS = 20 * 60;
    private static final int SHOP_DAY_INTERVAL = 3; // Geera opens the bait-and-tackle stall every third Minecraft day

    private static final FishQuest[] QUESTS = new FishQuest[] {
            new FishQuest("starcatcher:driftfin", "Driftfin", 1,
                    "Driftfin swim lazy until they don't. Bit like Mortimer before tea."),
            new FishQuest("starcatcher:blue_herring", "Blue Herring", 2,
                    "Blue Herring travel in schools. Sensible creatures. No banker among them."),
            new FishQuest("starcatcher:bigeye_tuna", "Bigeye Tuna", 1,
                    "Bigeye Tuna. Ugly little genius of the water. I respect that."),
            new FishQuest("starcatcher:cinder_squid", "Cinder Squid", 1,
                    "Cinder Squid for the brave, or the very hungry. Try not to cook yourself."),
            new FishQuest("starcatcher:chorus_crab", "Chorus Crab", 1,
                    "Chorus Crab. If it vanishes, don't blame me. Crabs are unionless chaos.")
    };

    public GeeraEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
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
        this.goalSelector.addGoal(0, new NpcShipStabilityGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new NpcAnchorReturnGoal(this, () -> this.homePos, 0.55D, 12.0F));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.45D));
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
        PlayerStatsTracker.recordVisit(player, "geera");
        PlayerProgress progress = getProgress(player);
        if (!progress.hasMet) {
            progress.hasMet = true;
            addTrust(progress, 1);
            String firstMeet = pickFirstMeeting(player);
            say(player, firstMeet);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Geera - Fisherwoman",
                        firstMeet,
                        "Fishing work",
                        "Fishing tips",
                        "First time meeting Geera."
                ));
            }
            return;
        }

        int stage = progress.fishingStage;
        FishQuest quest = QUESTS[Math.min(stage, QUESTS.length - 1)];
        String greeting = pickGeeraGreeting(progress, player.getName().getString());
        String body = greeting + "\n\n" + buildDialogueBody(player, quest, stage);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Geera - Fisherwoman",
                    body,
                    "Fishing work",
                    "Fishing tips",
                    "Fishing Progress: " + (stage + 1) + "/" + QUESTS.length + "  •  Starcatcher catches accepted."
            ));
        }
    }

    public void handleGeeraAction(Player player, String action) {
        switch (action) {
            case "geera_quest" -> handleQuest(player);
            case "geera_tips" -> fishingTips(player);
            case "geera_shop" -> openShopGui(player);
            case "geera_buy_bait" -> buyBait(player);
            case "geera_buy_rumor" -> buyRumor(player);
            case "geera_sell_catch" -> sellCatch(player);
            case "geera_emote" -> showEmote(randomOf("🐟", "🪝", "☕", "💚", "🌧"));
            case "geera_log" -> say(player, "Logbook notes moved to the Crew Logbook. Cleaner that way.");
            case "geera_status" -> shopStatus(player);
            case "geera_station" -> stationStatus(player);
            case "geera_set_station" -> stationStatus(player);
            case "geera_call_station" -> callToStation(player);
            default -> openDialogue(player);
        }
    }


    private void setStationHere(Player player) {
        // Deprecated: Geera now treats her summon location as the bait-and-tackle station.
        stationStatus(player);
    }

    private void callToStation(Player player) {
        ensureSummonShopAnchor();
        if (manualShopAnchor == null) return;
        shopWorkTarget = manualShopAnchor;
        shopOperateTicks = Math.max(shopOperateTicks, 20 * 30);
        say(player, "On my way to the stall. If the fish union asks, you didn't see me hurry.");
    }


    private void ensureSummonShopAnchor() {
        if (manualShopAnchor == null) {
            manualShopAnchor = this.blockPosition().immutable();
            shopWorkTarget = manualShopAnchor;
        }
    }

    private void shopStatus(Player player) {
        long day = this.level().getDayTime() / 24000L;
        long next = SHOP_DAY_INTERVAL - (day % SHOP_DAY_INTERVAL);
        boolean open = isShopDay();
        String body = "Geera's Bait & Tackle - Stall Status\n\n"
                + "Current day: " + day + ". "
                + (open ? "The stall is open today. Geera is willing to trade bait bundles, rumors, and fishing work." : "The stall is closed today. Next scheduled shop day in about " + next + " day(s).")
                + "\n\nBait bundles sold: " + baitBundlesSold + ". Rumors traded: " + rumorsSold + ". Work sessions completed: " + workSessionsCompleted + "."
                + "\n\nCurrent prototype: bait/rumor ledger is saved on Geera. Future version: real inventory slots, prices, Starcatcher fish records, and rotating stock.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Geera's Bait & Tackle",
                    body,
                    "Buy bait",
                    "Buy rumor",
                    open ? "Shop is open today." : "Shop closed, but prototype buttons still give testing feedback."
            ));
        }
    }

    private void stationStatus(Player player) {
        ensureSummonShopAnchor();
        if (shopWorkTarget == null) shopWorkTarget = findShopWorkTarget();
        String target = shopWorkTarget == null ? "No summon anchor found yet." : (shopWorkTarget.getX() + ", " + shopWorkTarget.getY() + ", " + shopWorkTarget.getZ());
        String body = "Geera Station Routine\n\n"
                + "Shop location: " + target + "\n"
                + "This is set automatically from the location where /summon ciskspawn:geera was run.\n"
                + "Operating ticks remaining: " + shopOperateTicks + ".\n\n"
                + "The manual Set Station button was removed so her routine is predictable for testing and easier to build around later.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Geera Station",
                    body,
                    "Fishing work",
                    "Open shop",
                    "Summon-location station anchor. Move Geera with a fresh summon to change it."
            ));
        }
    }

    private String buildDialogueBody(Player player, FishQuest quest, int stage) {
        long day = this.level().getDayTime() / 24000L;
        boolean shopDay = isShopDay();
        return "Morning. If Mortimer sent you, no, I am not lending him more rope until he returns the last three coils. "
                + "If you're here to work, the waters are honest enough. Current catch request: "
                + quest.amount + "x " + quest.displayName + ". "
                + (shopDay ? "The bait stall is open today, if the sky doesn't throw a tantrum." : "Shop's shut today. Nets need mending and Mortimer needs supervising.")
                + " Fishing progress: " + (stage + 1) + "/" + QUESTS.length + ".";
    }

    private String pickFirstMeeting(Player player) {
        if (this.level().isRaining()) {
            return "Come in, come in. Rain up here is more personal than ground rain. Geera.";
        }
        return "Hello. You've got that look — you haven't eaten since you got here, have you? Don't answer that. Just sit.";
    }

    private String pickGeeraGreeting(PlayerProgress progress, String playerName) {
        if (progress.trust >= 80) {
            return randomOf(
                    "Oh, it's you. Good. Come in.",
                    "You look terrible. What happened? Sit down.",
                    "This is your home too now. You know that, right?"
            );
        }

        if (progress.trust >= 55) {
            return randomOf(
                    "I'm going to tell you something about Mortimer. You're going to have to act surprised when it comes up.",
                    "You know — I came from somewhere before this. I don't talk about it much. But you're the kind of person I'd tell.",
                    "You've been good for us. I hope you know that."
            );
        }

        if (progress.trust >= 35) {
            return randomOf(
                    "There you are. Mortimer was asking about you. I wasn't, obviously.",
                    "Something happened. I can see it. Do you want to talk about it or just fish for a while?",
                    "You know you can just say if something's wrong? I won't fix it. But I'll listen."
            );
        }

        if (progress.trust >= 15) {
            return randomOf(
                    "Back again. Good. You've got the look of someone learning to like it up here.",
                    "How are you finding the eastern islands? I've got opinions if you want them.",
                    "You're doing alright, " + playerName + ". Better than most newcomers."
            );
        }

        return randomOf(
                "Hello there. New to the sky, or just new to this island?",
                "Geera. I sell bait, mostly. I also know things. The bait is cheaper.",
                "Take your time. The sky doesn't rush anyone who knows how to read it."
        );
    }

    private boolean isShopDay() {
        long day = this.level().getDayTime() / 24000L;
        return day % SHOP_DAY_INTERVAL == 0;
    }

    private void openShopGui(Player player) {
        addTrust(player, 1);
        long day = this.level().getDayTime() / 24000L;
        long next = SHOP_DAY_INTERVAL - (day % SHOP_DAY_INTERVAL);
        boolean open = isShopDay();
        String body = open
                ? "Bait and tackle stall's open today. Current stock: Starcatcher bait bundles, fishing rumors, repair-rope gossip, and Scoria-adjacent secrets.\n\n"
                + "Prices:\n- Bait bundle: 1 copper ingot or 1 emerald.\n- Rumor: free for testing, later likely fish/copper.\n- Sell catch: Geera buys Starcatcher quest fish for copper.\n\n"
                + "Ledger: bait bundles sold " + baitBundlesSold + ", rumors traded " + rumorsSold + ", fish bought " + fishBought + "."
                : "Shop's shut today. Nets need mending and Mortimer needs supervising. Come back in about " + next + " day(s). You can still ask for fishing work or rumors.";
        shopOperateTicks = Math.max(shopOperateTicks, 20 * 20);
        shopWorkTarget = findShopWorkTarget();
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Geera's Bait & Tackle",
                    body,
                    "Buy bait",
                    "Buy rumor",
                    open ? "Shop is open today. Bait gives real items if Starcatcher ids are present." : "Shop closed. Prototype buttons still give dialogue/testing feedback."
            ));
        }
    }

    private void buyBait(Player player) {
        addTrust(player, 1);
        if (isShopDay()) {
            if (!hasPayment(player, "minecraft:copper_ingot", 1) && !hasPayment(player, "minecraft:emerald", 1)) {
                say(player, "Bait costs one copper ingot. Or an emerald if you're feeling theatrical.");
                showEmote("🪙");
                return;
            }
            takePayment(player, "minecraft:copper_ingot", 1, "minecraft:emerald", 1);
            baitBundlesSold++;
            boolean gave = giveItemById(player, "starcatcher:almighty_worm", 3)
                    || giveItemById(player, "starcatcher:seeking_worm", 3)
                    || giveItemById(player, "starcatcher:cherry_bait", 3)
                    || giveItemById(player, "minecraft:string", 6);
            say(player, gave
                    ? "There. Bait bundle. If it wriggles in your pocket, that's between you and the sky."
                    : "I tried to hand you bait, but the registry's being slippery. Very fish-like. Very annoying.");
            showEmote("🪝");
        } else {
            say(player, "Shop's shut. I can sell you advice: never trust dry boots on a fisherman.");
            showEmote("🌧");
        }
    }

    private void buyRumor(Player player) {
        addTrust(player, 1);
        rumorsSold++;
        String[] rumors = new String[] {
                "Stormglass fish have been circling under broken islands. That usually means strange currents.",
                "Saw a Guild apprentice buying pressure valves in fancy clothes. Mortimer would call it banker behavior. I call it comedy.",
                "Starcatcher fish bite better when the sky looks wrong. Don't ask me why. Fish are little prophets with fins.",
                "If you hear bells under the clouds, pack extra line. And maybe don't tell Mortimer until after tea.",
                "Scoria has been asking about stabilizers, redstone timing, and levitite. Very banker questions, obviously.",
                "Mortimer pretends he hates fancy clothes. He wore polished brass cuffs to his first Guild interview. Do not tell him I told you."
        };
        say(player, rumors[this.random.nextInt(rumors.length)]);
        showEmote("💬");
    }

    private void sellCatch(Player player) {
        addTrust(player, 1);
        int sold = 0;
        for (FishQuest quest : QUESTS) {
            int count = Math.min(4, countItem(player, quest.itemId));
            if (count > 0) {
                removeItems(player, quest.itemId, count);
                sold += count;
            }
        }
        if (sold <= 0) {
            say(player, "I buy Starcatcher catches, not pocket lint. Bring me something with fins, claws, or suspicious glow.");
            showEmote("🐟");
            return;
        }
        fishBought += sold;
        giveItemById(player, "minecraft:copper_ingot", Math.max(1, sold));
        say(player, "Fair trade. " + sold + " catch(es) for copper. If Mortimer asks, yes, commerce can be ethical when I'm doing it.");
        showEmote("🪙");
    }

    private void fishingTips(Player player) {
        String[] tips = new String[] {
                "Fish don't care how heroic you look. Bring patience, bait, and dry socks. Mostly patience.",
                "Starcatcher fish like odd weather and odder places. If the sky looks wrong, cast anyway.",
                "If a fish glows, bites, or insults your family, bring it to me before Mortimer tries to classify it as engine fuel.",
                "Scoria used to sort my hooks by alloy content. Should've known that boy was headed for engineering, not banking."
        };
        say(player, tips[this.random.nextInt(tips.length)]);
    }

    private void crewLog(Player player) {
        PlayerProgress progress = getProgress(player);
        int stage = progress.fishingStage;
        logbookOpens++;
        String body = "Crew Log - Dockside Notes\n\n"
                + "Geera: fisherwoman, bait-and-tackle operator, dock rumor collector, and the person most likely to make Mortimer admit he needs lunch.\n\n"
                + "Trust: " + progress.trust + "/100. Fishing Progress: " + Math.min(stage + 1, QUESTS.length) + "/" + QUESTS.length + ".\n\n"
                + "Mood: " + currentMood() + ".\n"
                + "World counters: couple lines " + coupleLinesSpoken + ", emotes " + emotesShown + ", work sessions " + workSessionsCompleted + ", logbook opens " + logbookOpens + ".\n\n"
                + "Shop Ledger: bait bundles sold " + baitBundlesSold + ", rumors traded " + rumorsSold + ", fish bought " + fishBought + ". Shop days happen every third Minecraft day.\n\n"
                + "Scoria: allegedly a banker. Actually apprenticing as an aero-engineer, which Geera finds too funny to correct yet.\n\n"
                + "Persistent Pages:\n"
                + "- Fish Records: current quest target is " + QUESTS[Math.min(stage, QUESTS.length - 1)].displayName + ".\n"
                + "- Shop Ledger: bait bundles sold " + baitBundlesSold + ", rumors traded " + rumorsSold + ", fish bought " + fishBought + ".\n"
                + "- Family Notes: Scoria is not becoming a banker. Geera is waiting to see how long Mortimer takes to notice.\n\n"
                + "Future notes: fishing contracts, cooking requests, shop inventory, Starcatcher fish records, and family truth-bombs.";
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Crew Logbook - Geera",
                    body,
                    "Fishing work",
                    "Close",
                    "Persistent prototype: this log is rebuilt from saved Geera quest progress."
            ));
        }
    }

    private void handleQuest(Player player) {
        PlayerProgress progress = getProgress(player);
        int stage = progress.fishingStage;
        FishQuest quest = QUESTS[Math.min(stage, QUESTS.length - 1)];
        int count = countItem(player, quest.itemId);
        if (count >= quest.amount) {
            removeItems(player, quest.itemId, quest.amount);
            addTrust(progress, 5);
            say(player, "Good catch. " + quest.completionLine);
            if (stage < QUESTS.length - 1) {
                progress.fishingStage = stage + 1;
                FishQuest next = QUESTS[stage + 1];
                say(player, "Next, bring me " + next.amount + "x " + next.displayName + ". Waters don't work themselves.");
            } else {
                say(player, "You've got decent hands for fishing. Mortimer better not steal you for engine work.");
            }
        } else {
            say(player, "Bring me " + quest.amount + "x " + quest.displayName + ". And don't let Mortimer call it a fetch quest. This is honest work.");
        }
    }

    private static int countItem(Player player, String itemId) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stackMatchesId(stack, itemId)) count += stack.getCount();
        }
        return count;
    }

    private static void removeItems(Player player, String itemId, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (!stackMatchesId(stack, itemId)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
            if (remaining <= 0) break;
        }
    }

    private boolean hasPayment(Player player, String primary, int primaryAmount) {
        return countItem(player, primary) >= primaryAmount;
    }

    private void takePayment(Player player, String primary, int primaryAmount, String fallback, int fallbackAmount) {
        if (countItem(player, primary) >= primaryAmount) removeItems(player, primary, primaryAmount);
        else if (countItem(player, fallback) >= fallbackAmount) removeItems(player, fallback, fallbackAmount);
    }

    private boolean giveItemById(Player player, String itemId, int amount) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) return false;
        if (!BuiltInRegistries.ITEM.containsKey(id)) return false;
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(id), amount);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        return true;
    }

    private static boolean stackMatchesId(ItemStack stack, String itemId) {
        if (stack.isEmpty()) return false;
        String key = stack.getItem().builtInRegistryHolder().key().location().toString();
        return key.equals(itemId);
    }

    private void say(Player player, String text) {
        this.setCustomName(Component.literal("§2Geera§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = SPEECH_TICKS;
        player.displayClientMessage(Component.literal("§2Geera§7: " + text), false);
    }

    private void sayNearby(String text) {
        this.setCustomName(Component.literal("§2Geera§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = SPEECH_TICKS;
        Component chat = Component.literal("§2Geera§7: " + text);
        for (Player p : this.level().players()) {
            if (p.distanceTo(this) <= 12.0F) p.sendSystemMessage(chat);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) return;
        ensureSummonShopAnchor();

        if (speechTicksLeft > 0) {
            speechTicksLeft--;
            if (speechTicksLeft == 0) {
                this.setCustomNameVisible(false);
                this.setCustomName(Component.literal("Geera - Fisherwoman"));
            }
        }

        if (coupleCooldown > 0) coupleCooldown--;
        else if (tryCoupleDialogue()) coupleCooldown = 20 * 90 + this.random.nextInt(20 * 90);
        else coupleCooldown = 20 * 40;

        if (routineCooldown > 0) routineCooldown--;
        else {
            routineCooldown = ROUTINE_CHECK_TICKS;
            if (isShopDay() && this.random.nextFloat() < 0.45F) {
                sayNearby("Bait stall's open if you've got coin, patience, or a convincing sob story.");
                shopWorkTarget = findShopWorkTarget();
            }
        }

        tickShopWorkTarget();

        if (emoteCooldown > 0) emoteCooldown--;
        else {
            Player nearbyForEmote = this.level().getNearestPlayer(this, 7.0D);
            if (nearbyForEmote != null && this.random.nextFloat() < 0.55F) {
                showEmote(randomOf("🐟", "🪝", "☕", "💚", "🌧"));
            }
            emoteCooldown = 20 * 80 + this.random.nextInt(20 * 120);
        }

        if (ambientCooldown > 0) {
            ambientCooldown--;
            return;
        }
        Player nearby = this.level().getNearestPlayer(this, 8.0D);
        if (nearby != null) {
            String statLine = pickStatLine(nearby);
            if (statLine != null && this.random.nextFloat() < 0.25f) {
                sayNearby(statLine);
                ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
                return;
            }
            sayNearby(pickAmbientLine(nearby));
        }
        ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
    }

    private void tickShopWorkTarget() {
        if (!isShopDay()) {
            shopWorkTarget = null;
            return;
        }
        if (stationCooldown > 0) stationCooldown--;
        if (shopWorkTarget == null && stationCooldown <= 0) {
            stationCooldown = 20 * 45;
            shopWorkTarget = findShopWorkTarget();
        }
        if (shopWorkTarget == null) return;
        double dist = this.distanceToSqr(shopWorkTarget.getX() + 0.5D, shopWorkTarget.getY(), shopWorkTarget.getZ() + 0.5D);
        if (dist > 3.0D) {
            this.getNavigation().moveTo(shopWorkTarget.getX() + 0.5D, shopWorkTarget.getY(), shopWorkTarget.getZ() + 0.5D, 0.55D);
        } else {
            this.getNavigation().stop();
            if (shopOperateTicks <= 0) workSessionsCompleted++;
            shopOperateTicks = Math.max(shopOperateTicks, 20 * 10);
            this.getLookControl().setLookAt(shopWorkTarget.getX() + 0.5D, shopWorkTarget.getY() + 0.5D, shopWorkTarget.getZ() + 0.5D, 20.0F, 20.0F);
            if (this.random.nextFloat() < 0.0075F) {
                sayNearby(randomOf("Hooks counted. Nets sulking. Business as usual.", "If Mortimer asks, this is not his rope.", "Good shop day. Bad day to be a worm.", "Shop's tidy. Suspiciously tidy. I blame customers."));
            }
        }
    }

    private BlockPos findShopWorkTarget() {
        ensureSummonShopAnchor();
        if (manualShopAnchor != null) return manualShopAnchor;
        BlockPos center = this.blockPosition();
        BlockPos fallbackWater = null;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-10, -3, -10), center.offset(10, 3, 10))) {
            BlockState state = this.level().getBlockState(pos);
            ResourceLocation key = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            String id = key == null ? "" : key.toString();
            if (id.contains("barrel") || id.contains("chest") || id.contains("crate") || id.contains("table")) {
                return pos;
            }
            if (fallbackWater == null && id.equals("minecraft:water")) fallbackWater = pos;
        }
        return fallbackWater;
    }

    private boolean tryCoupleDialogue() {
        AABB box = this.getBoundingBox().inflate(6.0D);
        java.util.List<StorykeeperEntity> mortimers = this.level().getEntitiesOfClass(StorykeeperEntity.class, box);
        if (mortimers.isEmpty()) return false;
        StorykeeperEntity mortimer = mortimers.get(0);
        String[] lines = new String[] {
                "Mortimer, stop teaching travelers that banks are weather events.",
                "Mortimer, if you borrowed my rope again, blink twice and run.",
                "You told a child the banks eat souls. He believed you.",
                "If you're taking another sky trip, you are packing lunch this time.",
                "Don't let him fool you. He likes being fussed over more than any goblin I've met.",
                "Mortimer, the player does not need a lecture about bolt ethics before breakfast.",
                "Love, if you mention Scoria's banker clothes again I'm charging you dock fees.",
                "That old ship loves you. So do I. One of us has better judgment.",
                "You polish brass like you are apologizing to it, Mortimer.",
                "If Scoria is a banker, I am a cloud whale. Keep guessing, love."
        };
        coupleLinesSpoken++;
        showEmote(randomOf("💚", "🐟", "🪢", "☕"));
        sayNearby(lines[this.random.nextInt(lines.length)]);
        mortimer.hearGeeraNearby();
        return true;
    }

    private String pickAmbientLine(Player player) {
        if (isShopDay()) {
            return randomOf("Tackle stall's open today. Bring patience. The fish can smell desperation.", "Good tide for odd catches. Bad tide for excuses.");
        }
        return randomOf(
                "Fish first. Philosophy later.",
                "Mortimer says the sky rewards adaptation. Water rewards quiet.",
                "Scoria polished his boots again. Mortimer nearly fainted. Good morning, honestly.",
                "If you catch anything with too many eyes, bring it here before it starts a union.",
                "A calm dock is a lie. Check the ropes.",
                "Scoria asked for brass washers again. Very banker behavior, obviously.",
                "One day Mortimer will ask a question before panicking. Not today, but one day."
        );
    }

    private String pickStatLine(Player player) {
        int deaths = PlayerStatsTracker.getDeaths(player);
        int islands = PlayerStatsTracker.getIslandsDiscovered(player);
        String shipName = PlayerStatsTracker.getShipName(player);
        long daysSince = PlayerStatsTracker.getDaysSinceLastVisit(player, "geera");

        if (daysSince >= 5) return "Wondered where you'd got to. The sky treating you alright?";
        if (deaths >= 10) return "Heard you've been having a rough time up there. " + deaths + " times? Mortimer would call that experience. I'd say eat something first.";
        if (islands >= 10) return "You've been going further than most. What are you looking for out there?";
        if (shipName != null && this.random.nextFloat() < 0.3f) return "A ship named " + shipName + ". I like that. She sounds reliable.";
        return null;
    }

    private String currentMood() {
        if (isShopDay()) return "open, practical, and pretending she is not enjoying the rush";
        if (shopOperateTicks > 0) return "focused on nets, hooks, and making the dock behave";
        if (coupleLinesSpoken > 0) return "fondly exasperated by Mortimer";
        return "calm, watchful, and only mildly suspicious of the sky";
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

    private void addTrust(Player player, int amount) {
        addTrust(getProgress(player), amount);
    }

    private void addTrust(PlayerProgress progress, int amount) {
        progress.trust = Math.min(100, progress.trust + amount);
    }

    private void showEmote(String icon) {
        emotesShown++;
        this.setCustomName(Component.literal("§2Geera §f" + icon));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 3;
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
            playerTag.putInt("FishingStage", entry.getValue().fishingStage);
            list.add(playerTag);
        }
        tag.put("GeeraProgress", list);
        tag.putInt("AmbientCooldown", ambientCooldown);
        tag.putInt("CoupleCooldown", coupleCooldown);
        tag.putInt("EmoteCooldown", emoteCooldown);
        tag.putInt("BaitBundlesSold", baitBundlesSold);
        tag.putInt("RumorsSold", rumorsSold);
        tag.putInt("FishBought", fishBought);
        tag.putInt("CoupleLinesSpoken", coupleLinesSpoken);
        tag.putInt("EmotesShown", emotesShown);
        tag.putInt("WorkSessionsCompleted", workSessionsCompleted);
        tag.putInt("LogbookOpens", logbookOpens);
        if (manualShopAnchor != null) { tag.putInt("ManualShopX", manualShopAnchor.getX()); tag.putInt("ManualShopY", manualShopAnchor.getY()); tag.putInt("ManualShopZ", manualShopAnchor.getZ()); }
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
        if (tag.contains("CoupleCooldown")) coupleCooldown = tag.getInt("CoupleCooldown");
        if (tag.contains("EmoteCooldown")) emoteCooldown = tag.getInt("EmoteCooldown");
        if (tag.contains("BaitBundlesSold")) baitBundlesSold = tag.getInt("BaitBundlesSold");
        if (tag.contains("RumorsSold")) rumorsSold = tag.getInt("RumorsSold");
        if (tag.contains("FishBought")) fishBought = tag.getInt("FishBought");
        if (tag.contains("CoupleLinesSpoken")) coupleLinesSpoken = tag.getInt("CoupleLinesSpoken");
        if (tag.contains("EmotesShown")) emotesShown = tag.getInt("EmotesShown");
        if (tag.contains("WorkSessionsCompleted")) workSessionsCompleted = tag.getInt("WorkSessionsCompleted");
        if (tag.contains("LogbookOpens")) logbookOpens = tag.getInt("LogbookOpens");
        if (tag.contains("ManualShopX")) manualShopAnchor = new BlockPos(tag.getInt("ManualShopX"), tag.getInt("ManualShopY"), tag.getInt("ManualShopZ"));
        if (tag.contains("GeeraProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("GeeraProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.fishingStage = playerTag.getInt("FishingStage");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        } else if (tag.contains("GeeraFishingProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("GeeraFishingProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.fishingStage = playerTag.getInt("QuestStage");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<GeeraEntity> state) {
        if (state.isMoving()) state.setAnimation(WALK_ANIM);
        else state.setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private record FishQuest(String itemId, String displayName, int amount, String completionLine) {}

    private static class PlayerProgress {
        int trust = 0;
        boolean hasMet = false;
        int fishingStage = 0;
    }
}
