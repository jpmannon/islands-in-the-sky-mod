package com.quickqwek.ciskspawn.item;

import com.quickqwek.ciskspawn.CiskSpawnMod;
import com.quickqwek.ciskspawn.world.NpcWaypoint;
import com.quickqwek.ciskspawn.world.WaypointRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WaypointRecorderItem extends Item {
    private static final String TARGET_UUID = "TargetEntityUUID";
    private static final String TARGET_NAME = "TargetEntityName";
    private static final String PENDING_START_TICK = "PendingStartTick";
    private static final String PENDING_WAIT_DURATION = "PendingWaitDuration";
    private static final String PENDING_LABEL = "PendingLabel";

    public WaypointRecorderItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof PathfinderMob) || !isCiskSpawnEntity(target)) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide) {
            writeData(stack, tag -> {
                tag.putUUID(TARGET_UUID, target.getUUID());
                tag.putString(TARGET_NAME, target.getDisplayName().getString());
            });
            player.sendSystemMessage(Component.literal("[CISK Waypoints] Bound to: "
                    + target.getDisplayName().getString()
                    + " (UUID: " + target.getUUID() + "). Right-click ground to add waypoints."));
        }

        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        ItemStack stack = context.getItemInHand();
        Optional<UUID> targetUUID = getTargetUUID(stack);
        if (targetUUID.isEmpty()) {
            player.sendSystemMessage(Component.literal("[CISK Waypoints] Right-click an NPC first to bind this recorder."));
            return InteractionResult.SUCCESS;
        }

        WaypointRegistry registry = WaypointRegistry.get(serverLevel);
        UUID uuid = targetUUID.get();
        if (player.isShiftKeyDown()) {
            registry.removeLastWaypoint(uuid);
            player.sendSystemMessage(Component.literal("[CISK Waypoints] Last waypoint removed."));
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos().above();
        int startTick = getPendingStartTick(stack);
        int waitDuration = getPendingWaitDuration(stack);
        String label = getPendingLabel(stack);
        registry.addWaypoint(uuid, pos, startTick, waitDuration, label);
        int index = registry.getWaypoints(uuid).size() - 1;

        player.sendSystemMessage(Component.literal("[CISK Waypoints] Waypoint #" + index
                + " added at " + formatPos(pos)
                + " - start: " + startTick
                + ", wait: " + (waitDuration / 20)
                + "s, label: '" + label + "'"));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player.isShiftKeyDown()) {
            Optional<UUID> targetUUID = getTargetUUID(stack);
            if (targetUUID.isEmpty()) {
                player.sendSystemMessage(Component.literal("[CISK Waypoints] Right-click an NPC first to bind this recorder."));
            } else {
                WaypointRegistry.get(serverLevel).clearWaypoints(targetUUID.get());
                player.sendSystemMessage(Component.literal("[CISK Waypoints] All waypoints cleared."));
            }
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = readData(stack);
        tooltip.add(Component.literal("Bound: " + tag.getString(TARGET_NAME)));
        tooltip.add(Component.literal("Start tick: " + getPendingStartTick(stack)));
        tooltip.add(Component.literal("Wait: " + (getPendingWaitDuration(stack) / 20) + "s"));
        tooltip.add(Component.literal("Label: " + getPendingLabel(stack)));
    }

    public static void setPendingStartTick(ItemStack stack, int tick) {
        writeData(stack, tag -> tag.putInt(PENDING_START_TICK, Math.floorMod(tick, 24000)));
    }

    public static void setPendingWaitSeconds(ItemStack stack, int seconds) {
        writeData(stack, tag -> tag.putInt(PENDING_WAIT_DURATION, Math.max(0, seconds) * 20));
    }

    public static void setPendingLabel(ItemStack stack, String label) {
        writeData(stack, tag -> tag.putString(PENDING_LABEL, label == null ? "" : label));
    }

    public static Optional<UUID> getTargetUUID(ItemStack stack) {
        CompoundTag tag = readData(stack);
        return tag.hasUUID(TARGET_UUID) ? Optional.of(tag.getUUID(TARGET_UUID)) : Optional.empty();
    }

    public static int getPendingStartTick(ItemStack stack) {
        CompoundTag tag = readData(stack);
        return tag.contains(PENDING_START_TICK) ? tag.getInt(PENDING_START_TICK) : 0;
    }

    public static int getPendingWaitDuration(ItemStack stack) {
        CompoundTag tag = readData(stack);
        return tag.contains(PENDING_WAIT_DURATION) ? tag.getInt(PENDING_WAIT_DURATION) : 600;
    }

    public static String getPendingLabel(ItemStack stack) {
        CompoundTag tag = readData(stack);
        return tag.contains(PENDING_LABEL) ? tag.getString(PENDING_LABEL) : "";
    }

    public static boolean isRecorder(ItemStack stack) {
        return stack.getItem() instanceof WaypointRecorderItem;
    }

    private static CompoundTag readData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void writeData(ItemStack stack, DataWriter writer) {
        CompoundTag tag = readData(stack);
        writer.write(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static boolean isCiskSpawnEntity(LivingEntity target) {
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        return CiskSpawnMod.MODID.equals(id.getNamespace());
    }

    private static String formatPos(BlockPos pos) {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
    }

    @FunctionalInterface
    private interface DataWriter {
        void write(CompoundTag tag);
    }
}
