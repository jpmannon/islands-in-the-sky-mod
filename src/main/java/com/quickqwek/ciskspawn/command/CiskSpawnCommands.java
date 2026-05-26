package com.quickqwek.ciskspawn.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.quickqwek.ciskspawn.block.ModBlocks;
import com.quickqwek.ciskspawn.block.SettlementMarkerBlockEntity;
import com.quickqwek.ciskspawn.item.WaypointRecorderItem;
import com.quickqwek.ciskspawn.world.NpcWaypoint;
import com.quickqwek.ciskspawn.world.SettlementRegistry;
import com.quickqwek.ciskspawn.world.WaypointRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;
import java.util.UUID;

public final class CiskSpawnCommands {
    private CiskSpawnCommands() {}

    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ciskspawn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("settlement")
                        .then(Commands.literal("register")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .then(Commands.argument("displayName", StringArgumentType.string())
                                                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                                        .executes(CiskSpawnCommands::registerSettlement)))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(CiskSpawnCommands::removeSettlement)))
                        .then(Commands.literal("list")
                                .executes(CiskSpawnCommands::listSettlements))
                        .then(Commands.literal("info")
                                .executes(CiskSpawnCommands::settlementInfo)))
                .then(Commands.literal("waypoint")
                        .then(Commands.literal("list")
                                .then(Commands.argument("entityUUID", StringArgumentType.word())
                                        .executes(CiskSpawnCommands::listWaypoints)))
                        .then(Commands.literal("clear")
                                .then(Commands.argument("entityUUID", StringArgumentType.word())
                                        .executes(CiskSpawnCommands::clearWaypoints)))
                        .then(Commands.literal("settick")
                                .then(Commands.argument("tick", IntegerArgumentType.integer(0, 23999))
                                        .executes(CiskSpawnCommands::setWaypointTick)))
                        .then(Commands.literal("setwait")
                                .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                        .executes(CiskSpawnCommands::setWaypointWait)))
                        .then(Commands.literal("setlabel")
                                .then(Commands.argument("label", StringArgumentType.greedyString())
                                        .executes(CiskSpawnCommands::setWaypointLabel))));

        event.getDispatcher().register(root);
    }

    private static int registerSettlement(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.literal("[CISK] This command must be run by a player."));
            return 0;
        }

        String id = StringArgumentType.getString(context, "id").toLowerCase(Locale.ROOT);
        String displayName = StringArgumentType.getString(context, "displayName");
        int radius = IntegerArgumentType.getInteger(context, "radius");

        ServerLevel level = player.serverLevel();
        BlockPos center = getTargetBlock(player);
        BlockPos markerPos = findNearestMarker(level, player.blockPosition(), 5);
        if (center == null) {
            center = markerPos != null ? markerPos : player.blockPosition();
        }

        SettlementRegistry.get(level).register(id, displayName, center, radius);

        BlockPos markerToBind = isSettlementMarker(level, center) ? center : markerPos;
        if (markerToBind != null && level.getBlockEntity(markerToBind) instanceof SettlementMarkerBlockEntity marker) {
            marker.setSettlementId(id);
        }

        BlockPos registeredCenter = center;
        source.sendSuccess(() -> Component.literal("[CISK] Registered settlement '" + displayName
                + "' (id: " + id + ") at " + formatPos(registeredCenter) + " radius " + radius), true);
        return 1;
    }

    private static int removeSettlement(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = StringArgumentType.getString(context, "id");

        ServerLevel level = source.getLevel();
        SettlementRegistry.get(level).remove(id);
        source.sendSuccess(() -> Component.literal("[CISK] Removed settlement id: " + id.toLowerCase(Locale.ROOT)), true);
        return 1;
    }

    private static int listSettlements(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        SettlementRegistry registry = SettlementRegistry.get(source.getLevel());

        if (registry.getAll().isEmpty()) {
            source.sendSuccess(() -> Component.literal("[CISK] No registered settlements."), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("[CISK] Registered settlements:"), false);
        for (SettlementRegistry.Settlement settlement : registry.getAll()) {
            source.sendSuccess(() -> Component.literal("- " + settlement.displayName()
                    + " (id: " + settlement.id() + ") center "
                    + formatPos(settlement.center())
                    + " radius " + settlement.radius()), false);
        }
        return 1;
    }

    private static int settlementInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.literal("[CISK] This command must be run by a player."));
            return 0;
        }

        SettlementRegistry.get(player.serverLevel())
                .getSettlementAt(player.blockPosition())
                .ifPresentOrElse(
                        settlement -> source.sendSuccess(() -> Component.literal("[CISK] You are in "
                                + settlement.displayName()
                                + " (id: " + settlement.id() + ")"), false),
                        () -> source.sendSuccess(() -> Component.literal("[CISK] You are not in a registered settlement."), false)
                );
        return 1;
    }

    private static int listWaypoints(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        UUID uuid = parseUUID(source, StringArgumentType.getString(context, "entityUUID"));
        if (uuid == null) {
            return 0;
        }

        var waypoints = WaypointRegistry.get(source.getLevel()).getWaypoints(uuid);
        if (waypoints.isEmpty()) {
            source.sendSuccess(() -> Component.literal("[CISK Waypoints] No waypoints for " + uuid), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("[CISK Waypoints] Waypoints for " + uuid + ":"), false);
        for (NpcWaypoint waypoint : waypoints) {
            source.sendSuccess(() -> Component.literal("#" + waypoint.index()
                    + " " + formatPos(waypoint.pos())
                    + " start " + waypoint.startTick()
                    + " wait " + (waypoint.waitDuration() / 20)
                    + "s label '" + waypoint.label() + "'"), false);
        }
        return 1;
    }

    private static int clearWaypoints(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        UUID uuid = parseUUID(source, StringArgumentType.getString(context, "entityUUID"));
        if (uuid == null) {
            return 0;
        }

        WaypointRegistry.get(source.getLevel()).clearWaypoints(uuid);
        source.sendSuccess(() -> Component.literal("[CISK Waypoints] Cleared waypoints for " + uuid), true);
        return 1;
    }

    private static int setWaypointTick(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = getCommandPlayer(source);
        if (player == null) {
            return 0;
        }

        ItemStack stack = getHeldRecorder(player);
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("[CISK Waypoints] Hold a Waypoint Recorder first."));
            return 0;
        }

        int tick = IntegerArgumentType.getInteger(context, "tick");
        WaypointRecorderItem.setPendingStartTick(stack, tick);
        source.sendSuccess(() -> Component.literal("[CISK Waypoints] Next waypoint will activate at daytime tick "
                + tick + " (~" + tickToTime(tick) + ")"), false);
        return 1;
    }

    private static int setWaypointWait(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = getCommandPlayer(source);
        if (player == null) {
            return 0;
        }

        ItemStack stack = getHeldRecorder(player);
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("[CISK Waypoints] Hold a Waypoint Recorder first."));
            return 0;
        }

        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        WaypointRecorderItem.setPendingWaitSeconds(stack, seconds);
        source.sendSuccess(() -> Component.literal("[CISK Waypoints] Next waypoint wait duration set to " + seconds + "s"), false);
        return 1;
    }

    private static int setWaypointLabel(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = getCommandPlayer(source);
        if (player == null) {
            return 0;
        }

        ItemStack stack = getHeldRecorder(player);
        if (stack.isEmpty()) {
            source.sendFailure(Component.literal("[CISK Waypoints] Hold a Waypoint Recorder first."));
            return 0;
        }

        String label = StringArgumentType.getString(context, "label");
        WaypointRecorderItem.setPendingLabel(stack, label);
        source.sendSuccess(() -> Component.literal("[CISK Waypoints] Next waypoint label set to '" + label + "'"), false);
        return 1;
    }

    private static BlockPos getTargetBlock(ServerPlayer player) {
        HitResult hitResult = player.pick(8.0D, 0.0F, false);
        if (hitResult instanceof BlockHitResult blockHitResult && hitResult.getType() == HitResult.Type.BLOCK) {
            return blockHitResult.getBlockPos();
        }
        return null;
    }

    private static BlockPos findNearestMarker(ServerLevel level, BlockPos center, int radius) {
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            BlockPos immutablePos = pos.immutable();
            if (isSettlementMarker(level, immutablePos)) {
                double distance = immutablePos.distSqr(center);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = immutablePos;
                }
            }
        }

        return nearest;
    }

    private static boolean isSettlementMarker(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(ModBlocks.SETTLEMENT_MARKER.get())) {
            return false;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof SettlementMarkerBlockEntity;
    }

    private static String formatPos(BlockPos pos) {
        return "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
    }

    private static ServerPlayer getCommandPlayer(CommandSourceStack source) {
        try {
            return source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.literal("[CISK] This command must be run by a player."));
            return null;
        }
    }

    private static ItemStack getHeldRecorder(ServerPlayer player) {
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (WaypointRecorderItem.isRecorder(mainHand)) {
            return mainHand;
        }

        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        return WaypointRecorderItem.isRecorder(offHand) ? offHand : ItemStack.EMPTY;
    }

    private static UUID parseUUID(CommandSourceStack source, String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("[CISK Waypoints] Invalid UUID: " + value));
            return null;
        }
    }

    private static String tickToTime(int tick) {
        int minutesAfterSix = Math.floorMod(tick, 24000) * 1440 / 24000;
        int hour = (6 + minutesAfterSix / 60) % 24;
        int minute = minutesAfterSix % 60;
        return String.format(Locale.ROOT, "%02d:%02d", hour, minute);
    }
}
