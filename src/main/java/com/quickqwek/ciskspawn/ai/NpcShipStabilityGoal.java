package com.quickqwek.ciskspawn.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;

public class NpcShipStabilityGoal extends Goal {
    private final PathfinderMob mob;
    private int checkCooldown = 0;
    private boolean onShip = false;

    private static boolean reflectionAttempted = false;
    public static Method getTrackingSubLevel = null;
    public static Object sableHelper = null;

    public NpcShipStabilityGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().isClientSide) {
            return false;
        }
        if (this.checkCooldown-- > 0) {
            return this.onShip;
        }
        this.checkCooldown = 10;
        this.onShip = isOnSableShip();
        return this.onShip;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.checkCooldown-- > 0) {
            return this.onShip;
        }
        this.checkCooldown = 10;
        this.onShip = isOnSableShip();
        return this.onShip;
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.mob.getNavigation().stop();
        this.mob.setJumping(false);
    }

    @Override
    public void stop() {
        try {
            Method setHome = this.mob.getClass().getMethod("setHomePos", BlockPos.class);
            setHome.invoke(this.mob, this.mob.blockPosition());
        } catch (Exception ignored) {
        }
    }

    private boolean isOnSableShip() {
        initReflection();
        if (sableHelper == null || getTrackingSubLevel == null) {
            return false;
        }
        try {
            Object subLevel = getTrackingSubLevel.invoke(sableHelper, this.mob);
            return subLevel != null;
        } catch (Exception exception) {
            return false;
        }
    }

    public static synchronized void initReflection() {
        if (reflectionAttempted) {
            return;
        }
        reflectionAttempted = true;
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Field helperField = sableClass.getDeclaredField("HELPER");
            helperField.setAccessible(true);
            sableHelper = helperField.get(null);
            if (sableHelper == null) {
                return;
            }
            getTrackingSubLevel = sableHelper.getClass().getMethod("getTrackingSubLevel", Entity.class);
        } catch (Exception exception) {
            sableHelper = null;
            getTrackingSubLevel = null;
        }
    }
}
