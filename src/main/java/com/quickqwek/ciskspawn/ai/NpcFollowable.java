package com.quickqwek.ciskspawn.ai;

/**
 * Implemented by NPC entities that support player-follow mode.
 * NpcAnchorReturnGoal checks this interface to yield the MOVE flag
 * while the NPC is actively following a player.
 */
public interface NpcFollowable {
    boolean isFollowingPlayer();
}
