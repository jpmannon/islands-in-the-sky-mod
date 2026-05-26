package com.quickqwek.ciskspawn.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class NpcAnchorReturnGoal extends Goal {
    private final PathfinderMob mob;
    private final BlockPos homePos;
    private final double speedModifier;
    private final float maxDistance;

    public NpcAnchorReturnGoal(PathfinderMob mob, BlockPos homePos, double speedModifier, float maxDistance) {
        this.mob = mob;
        this.homePos = homePos;
        this.speedModifier = speedModifier;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.distanceToSqr(
                this.homePos.getX() + 0.5D,
                this.homePos.getY() + 0.5D,
                this.homePos.getZ() + 0.5D
        ) > this.maxDistance * this.maxDistance;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.distanceToSqr(
                this.homePos.getX() + 0.5D,
                this.homePos.getY() + 0.5D,
                this.homePos.getZ() + 0.5D
        ) > 4.0D;
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(
                this.homePos.getX() + 0.5D,
                this.homePos.getY(),
                this.homePos.getZ() + 0.5D,
                this.speedModifier
        );
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
