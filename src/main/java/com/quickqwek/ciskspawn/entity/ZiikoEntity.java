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

public class ZiikoEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, PlayerProgress> progressByPlayer = new HashMap<>();
    private int speechTicksLeft = 0;
    private int ambientCooldown = 20 * 154;
    private BlockPos homePos = null;

    public ZiikoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
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

        if (!progress.hasMet) {
            progress.hasMet = true;
            addTrust(player, progress, 1);
            String body = "I am Zii-ko, who you?\n\nYou want sword? I give you good price. "
                    + "Bring materials — only good materials. You come back then, I make. Go now.";
            say(player, "I am Zii-ko, who you?");
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                        this.getId(),
                        "Zii-ko - Smith",
                        body,
                        "Bring materials",
                        "Ask about materials",
                        "He looked you over quickly. Apparently satisfied."
                ));
            }
            return;
        }

        addTrust(player, progress, 1);
        String greeting = pickZiikoGreeting(progress);
        String body = greeting + "\n\n" + buildDialogueBody(progress);
        say(player, greeting);
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new MortimerDialoguePayload(
                    this.getId(),
                    "Zii-ko - Smith",
                    body,
                    "Bring materials",
                    "Ask about materials",
                    "Smithing notes are kept in the Crew Logbook."
            ));
        }
    }

    public void handleZiikoAction(Player player, String action) {
        switch (action) {
            case "ziiko_forge" -> handleForge(player);
            case "ziiko_materials" -> materialsTip(player);
            case "ziiko_himself" -> aboutHimself(player);
            case "ziiko_crew" -> aboutOthers(player);
            default -> openDialogue(player);
        }
    }

    private String pickZiikoGreeting(PlayerProgress progress) {
        if (progress.trust >= 80) return randomOf(
                "Oh good, it's you. Sit. I finish this and then we talk. I like when you visit.",
                "I made something for you. Not because you ask — because I wanted to. You are good customer and also good person. Both matter.",
                "You know, most humans — [click-hiss] — they come, they take, they go. You are different. You stay. I like that."
        );
        if (progress.trust >= 55) return randomOf(
                "My shop doing good today. I am happy. Happy Pogon makes better things — this is true fact.",
                "You know, in Pogon we say: 'The one who comes back is the one worth making for.' You keep coming back. This is good thing.",
                "I was thinking — you travel a lot, yes? You see strange metals anywhere? Bring me, I study."
        );
        if (progress.trust >= 35) return randomOf(
                "Good, good. You come at good time. I am testing new grip design. You want to see?",
                "I was thinking about you! I have idea for better sword. Come.",
                "You know Agatha? She bring me tea this morning. Good tea. I like her cats. They sit on my materials. I don't mind."
        );
        if (progress.trust >= 15) return randomOf(
                "Oi! Human! You back! Good. What you need.",
                "You again. Still alive? Good job. What you bring me.",
                "I remember you. You had — [click-hiss] — okay materials last time. You do better this time?"
        );
        return randomOf(
                "I am Zii-ko, who you?",
                "You new? Come. I show you what I make.",
                "Oi! New face! You want something made? Tell me."
        );
    }

    private String buildDialogueBody(PlayerProgress progress) {
        if (progress.forgeCount <= 0) {
            return "Bring iron ingots and flint. Or bring diamond and blaze rod. Or bring netherite if you have real good materials. I make useful thing.";
        }
        if (progress.trust >= 55) {
            return "You bring materials, I make. Simple. Good arrangement. Maybe best arrangement.";
        }
        return "Good materials make good things. Bad materials make lesson. Both useful, but good things better.";
    }

    private void handleForge(Player player) {
        PlayerProgress progress = getProgress(player);

        if (hasItem(player, "minecraft:netherite_ingot", 1)
                && hasItem(player, "minecraft:iron_ingot", 4)
                && hasItem(player, "minecraft:blaze_rod", 2)) {
            consumeItem(player, "minecraft:netherite_ingot", 1);
            consumeItem(player, "minecraft:iron_ingot", 4);
            consumeItem(player, "minecraft:blaze_rod", 2);
            progress.forgeCount++;
            addTrust(player, progress, 12);
            say(player, "Oh. Oh! This is good. This is very good. [click-hiss] You found netherite? Real netherite?");
            say(player, "...Yes. Okay. You sit. I work. This will be best thing I make this week.");
            giveItemById(player, "minecraft:netherite_sword", 1);
            say(player, "There. You see? Good materials, good result. Not my skill alone — your materials, my hands. We both did this. You understand?");
            return;
        }

        if (hasItem(player, "minecraft:diamond", 2)
                && hasItem(player, "minecraft:iron_ingot", 4)
                && hasItem(player, "minecraft:blaze_rod", 1)) {
            consumeItem(player, "minecraft:diamond", 2);
            consumeItem(player, "minecraft:iron_ingot", 4);
            consumeItem(player, "minecraft:blaze_rod", 1);
            progress.forgeCount++;
            addTrust(player, progress, 8);
            say(player, "Diamond! Good, good. I can work with diamond. Okay. Come back in — [click-hiss] — small time. I make you something worth carrying.");
            giveItemById(player, "minecraft:diamond_sword", 1);
            say(player, "Good sword. Not the best I ever make — I save that for netherite — but good. Don't drop it in lava. I will know. I always know.");
            return;
        }

        if (hasItem(player, "minecraft:iron_ingot", 6)
                && hasItem(player, "minecraft:flint", 1)) {
            consumeItem(player, "minecraft:iron_ingot", 6);
            consumeItem(player, "minecraft:flint", 1);
            progress.forgeCount++;
            addTrust(player, progress, 3);
            say(player, "Oh... [click-hiss] ...these are... okay materials. Not bad. Not good. Okay.");
            say(player, "I make something. It will work. It won't be beautiful. Not my fault — your fault. Only good materials make good things.");
            giveItemById(player, "minecraft:iron_sword", 1);
            say(player, "See? It works. I told you — it works but is not beautiful. Next time, bring better. I show you what I can really do.");
            return;
        }

        say(player, "You brought — [click-hiss] — no. No, this is not right.");
        say(player, "You need iron ingots, diamond, or netherite. And blaze rods — they help with heat process. Go find better, come back. I wait. I am always here.");
    }

    private void materialsTip(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(player, progress, 2);
        say(player, "Good materials? Okay. Listen.");
        say(player, "Iron is fine. Basic. I can work with iron — everyone can work with iron. Diamond is good. I like diamond. Holds edge longer.");
        say(player, "Netherite — [click-hiss, pause] — if you find netherite, bring all of it. I make you something you remember.");
        say(player, "Blaze rods help with forge temperature. Always bring. Better heat, better bond. Simple science.");
    }

    private void aboutHimself(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(player, progress, 1);
        if (progress.trust >= 80) {
            say(player, "My home island... I miss it sometimes. The sounds.");
            say(player, "You would not understand the sounds — human throat cannot make them. But I teach you to listen. That is different thing. Listening is possible for anyone.");
        } else if (progress.trust >= 55) {
            say(player, "In Pogon, we say only do what you want. Not lazy — honest. I want to make things. So I make things. Very simple life.");
        } else if (progress.trust >= 35) {
            say(player, "I came here because — [click-hiss] — I was bored. Then I was not bored. Then I stayed. This is how Pogon decide things. Simple.");
        } else {
            say(player, "I am Pogon. We are small, we make good things. That is all you need to know.");
        }
    }

    private void aboutOthers(Player player) {
        PlayerProgress progress = getProgress(player);
        addTrust(player, progress, 1);
        say(player, randomOf(
                "Mortimer is good man. Comes in sometimes, asks about armor. I give fair price. He pays fair price. Good relationship.",
                "Geera! Yes. She bring me good business. Also good conversation. Pogon like when humans are direct. Geera is direct.",
                "Agatha! [click-hiss — warm sound] She bring me tea. Her cats come also. One gray one — Smudge — he sleep on my best anvil once. I let him.",
                "Joelle make food that is also art. I understand this. My swords are also art. We have good understanding, me and Joelle.",
                "I don't get sick — Pogon immune system is [click-hiss] very robust. But I visit Tarn anyway. She is interesting. Very perceptive.",
                "Cade is good fighter. He know how to use good blade — this is rare. Most people carry weapon and don't understand it. Cade understands."
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
        this.setCustomName(Component.literal("§6Zii-ko§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        player.displayClientMessage(Component.literal("§6Zii-ko§7: " + text), false);
    }

    private void sayNearby(String text) {
        this.setCustomName(Component.literal("§6Zii-ko§f: " + text));
        this.setCustomNameVisible(true);
        this.speechTicksLeft = 20 * 7;
        Component chat = Component.literal("§6Zii-ko§7: " + text);
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
                this.setCustomName(Component.literal("Zii-ko - Smith"));
            }
        }

        if (ambientCooldown > 0) {
            ambientCooldown--;
            return;
        }

        Player nearby = this.level().getNearestPlayer(this, 8.0D);
        if (nearby != null) {
            boolean isNight = !this.level().isDay();
            boolean knowsAgatha = nearby.getPersistentData().getBoolean("ciskspawn_has_agatha_trust");
            boolean knowsZiiko = nearby.getPersistentData().getBoolean("ciskspawn_ziiko_trust_35");

            if (isNight && knowsAgatha && knowsZiiko && this.random.nextFloat() < 0.4F) {
                sayNearby(pickSmudgeLine());
            } else {
                sayNearby(pickAmbientLine());
            }
        }

        ambientCooldown = 20 * 154 + this.random.nextInt(20 * 242);
    }

    private String pickSmudgeLine() {
        return randomOf(
                "Smudge is back. He found the anvil again. I put blanket for him. [click-hiss] Don't tell Agatha. She worry.",
                "Gray cat is sleeping on anvil. This is third night this week. I named that spot his now. Official.",
                "Shh. Smudge is here. He always come at night. I think he like the warmth from forge. Smart cat."
        );
    }

    private String pickAmbientLine() {
        return randomOf(
                "[click-hiss] ...almost. Almost right...",
                "If you hear good clang, that is good sign. Bad clang means I am learning something.",
                "You know what I like about this island? Nobody bother me unless they want something. Very efficient.",
                "Oi! Don't touch that one. That one is cooling. Touch it after.",
                "Good day. Good materials today. Good day."
        );
    }

    private PlayerProgress getProgress(Player player) {
        return progressByPlayer.computeIfAbsent(player.getUUID(), uuid -> new PlayerProgress());
    }

    public int getTrustForPlayer(UUID playerUUID) {
        return progressByPlayer.getOrDefault(playerUUID, new PlayerProgress()).trust;
    }

    private void addTrust(Player player, PlayerProgress progress, int amount) {
        progress.trust = Math.min(100, progress.trust + amount);
        if (progress.trust >= 35 && !player.getPersistentData().getBoolean("ciskspawn_ziiko_trust_35")) {
            player.getPersistentData().putBoolean("ciskspawn_ziiko_trust_35", true);
        }
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
            playerTag.putInt("ForgeCount", entry.getValue().forgeCount);
            list.add(playerTag);
        }
        tag.put("ZiikoProgress", list);
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
        if (tag.contains("ZiikoProgress", Tag.TAG_LIST)) {
            ListTag list = tag.getList("ZiikoProgress", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag playerTag = list.getCompound(i);
                PlayerProgress progress = new PlayerProgress();
                progress.trust = playerTag.getInt("Trust");
                if (playerTag.contains("HasMet")) progress.hasMet = playerTag.getBoolean("HasMet");
                progress.forgeCount = playerTag.getInt("ForgeCount");
                progressByPlayer.put(playerTag.getUUID("Player"), progress);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }

    private PlayState predicate(AnimationState<ZiikoEntity> state) {
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
        int forgeCount = 0;
    }
}
