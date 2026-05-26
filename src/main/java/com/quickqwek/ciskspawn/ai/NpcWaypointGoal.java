package com.quickqwek.ciskspawn.ai;

import com.quickqwek.ciskspawn.world.NpcWaypoint;
import com.quickqwek.ciskspawn.world.WaypointRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class NpcWaypointGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private int currentWaypointIndex = 0;
    private int waitTicksRemaining = 0;
    private boolean isWaiting = false;
    private List<NpcWaypoint> cachedWaypoints = null;
    private int cacheRefreshCooldown = 0;

    public NpcWaypointGoal(PathfinderMob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        refreshCacheIfNeeded(serverLevel);
        return this.cachedWaypoints != null && !this.cachedWaypoints.isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return this.cachedWaypoints != null && !this.cachedWaypoints.isEmpty();
    }

    @Override
    public void start() {
        selectWaypointForCurrentTime();
        moveToCurrentWaypoint();
    }

    @Override
    public void tick() {
        if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        refreshCacheIfNeeded(serverLevel);
        if (this.cachedWaypoints == null || this.cachedWaypoints.isEmpty()) {
            return;
        }

        if (this.currentWaypointIndex >= this.cachedWaypoints.size()) {
            this.currentWaypointIndex = 0;
        }

        if (this.isWaiting) {
            this.waitTicksRemaining--;
            if (this.waitTicksRemaining <= 0) {
                this.isWaiting = false;
                this.currentWaypointIndex = (this.currentWaypointIndex + 1) % this.cachedWaypoints.size();
                moveToCurrentWaypoint();
            }
            return;
        }

        NpcWaypoint waypoint = this.cachedWaypoints.get(this.currentWaypointIndex);
        if (this.mob.distanceToSqr(
                waypoint.pos().getX() + 0.5D,
                waypoint.pos().getY(),
                waypoint.pos().getZ() + 0.5D
        ) < 2.0D) {
            this.isWaiting = true;
            this.waitTicksRemaining = waypoint.waitDuration();
            return;
        }

        if (this.mob.getNavigation().isDone()) {
            moveToCurrentWaypoint();
        }
    }

    @Override
    public void stop() {
        this.mob.getNavigation().stop();
    }

    private void refreshCacheIfNeeded(ServerLevel level) {
        if (this.cacheRefreshCooldown > 0) {
            this.cacheRefreshCooldown--;
            return;
        }

        this.cacheRefreshCooldown = 200;
        List<NpcWaypoint> waypoints = WaypointRegistry.get(level).getWaypoints(this.mob.getUUID());
        if (waypoints.isEmpty()) {
            this.cachedWaypoints = Collections.emptyList();
        } else {
            this.cachedWaypoints = new ArrayList<>(waypoints);
            this.cachedWaypoints.sort(Comparator.comparingInt(NpcWaypoint::index));
        }
    }

    private void selectWaypointForCurrentTime() {
        if (this.cachedWaypoints == null || this.cachedWaypoints.isEmpty()) {
            return;
        }

        int dayTick = (int) (this.mob.level().getDayTime() % 24000L);
        int selected = 0;
        int selectedStartTick = -1;
        for (int i = 0; i < this.cachedWaypoints.size(); i++) {
            int startTick = this.cachedWaypoints.get(i).startTick();
            if (startTick <= dayTick && startTick >= selectedStartTick) {
                selected = i;
                selectedStartTick = startTick;
            }
        }

        this.currentWaypointIndex = selected;
        this.isWaiting = false;
        this.waitTicksRemaining = 0;
    }

    private void moveToCurrentWaypoint() {
        if (this.cachedWaypoints == null || this.cachedWaypoints.isEmpty()) {
            return;
        }

        NpcWaypoint waypoint = this.cachedWaypoints.get(this.currentWaypointIndex);
        this.mob.getNavigation().moveTo(
                waypoint.pos().getX() + 0.5D,
                waypoint.pos().getY(),
                waypoint.pos().getZ() + 0.5D,
                this.speedModifier
        );
    }
}
