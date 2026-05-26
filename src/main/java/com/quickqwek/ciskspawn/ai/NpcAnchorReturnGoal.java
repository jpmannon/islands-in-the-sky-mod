package com.quickqwek.ciskspawn.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.function.Supplier;

public class NpcAnchorReturnGoal extends Goal {
    private final PathfinderMob mob;
    private final Supplier<BlockPos> homePosSupplier;
    private final double speedModifier;
    private final float maxDistance;

    public NpcAnchorReturnGoal(PathfinderMob mob, BlockPos homePos, double speedModifier, float maxDistance) {
        this(mob, () -> homePos, speedModifier, maxDistance);
    }

    public NpcAnchorReturnGoal(PathfinderMob mob, Supplier<BlockPos> homePosSupplier, double speedModifier, float maxDistance) {
        this.mob = mob;
        this.homePosSupplier = homePosSupplier;
        this.speedModifier = speedModifier;
        this.maxDistance = maxDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos homePos = this.homePosSupplier.get();
        if (homePos == null) {
            return false;
        }
        return this.mob.distanceToSqr(
                homePos.getX() + 0.5D,
                homePos.getY() + 0.5D,
                homePos.getZ() + 0.5D
        ) > this.maxDistance * this.maxDistance;
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos homePos = this.homePosSupplier.get();
        if (homePos == null) {
            return false;
        }
        return this.mob.distanceToSqr(
                homePos.getX() + 0.5D,
                homePos.getY() + 0.5D,
                homePos.getZ() + 0.5D
        ) > 4.0D;
    }

    @Override
    public void start() {
        BlockPos homePos = this.homePosSupplier.get();
        if (homePos == null) {
            return;
        }
        this.mob.getNavigation().moveTo(
                homePos.getX() + 0.5D,
                homePos.getY(),
                homePos.getZ() + 0.5D,
                this.speedModifier
        );
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }
}
