package com.quickqwek.ciskspawn.world;

import net.minecraft.core.BlockPos;

public record NpcWaypoint(
        int index,
        BlockPos pos,
        int startTick,
        int waitDuration,
        String label
) {}
