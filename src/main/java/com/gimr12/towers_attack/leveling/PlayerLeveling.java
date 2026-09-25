package com.gimr12.towers_attack.leveling;

import com.gimr12.towers_attack.attachment.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Central API for reading/writing a player's Grand Points (GP) and the
 * level derived from GP. GP persists via a NeoForge data attachment.
 *
 * SP is NOT stored anywhere - it's always computed fresh as
 * (3 x current level) minus however many points are currently allocated
 * across the 5 stats. This means SP is always exactly correct for whatever
 * level a player is at right now, even after an admin resets/changes their
 * level - there's no separate "earned SP" value that can fall out of sync.
 */
public class PlayerLeveling {

    /** SP a player should have in total at a given level, before subtracting allocated points. */
    public static final int SP_PER_LEVEL = 3;

    // GP required to go from level N to level N+1, indexed from level 1.
    // LEVEL_UP_COSTS[0] = cost to go from level 1 -> 2, [1] = 2 -> 3, etc.
    private static final int[] LEVEL_UP_COSTS = {100, 150, 250, 500, 750, 1200, 1800};

    // Beyond the hand-tuned curve above, growth is LINEAR (not compounding) so very
    // high levels stay reachable instead of exploding.
    private static final int LINEAR_GROWTH_PER_LEVEL = 500;

    /** GP required to advance one level starting FROM the given level. */
    private static int gpNeededToLevelUp(int fromLevel) {
        int index = fromLevel - 1;
        if (index >= 0 && index < LEVEL_UP_COSTS.length) {
            return LEVEL_UP_COSTS[index];
        }
        int steps = index - (LEVEL_UP_COSTS.length - 1);
        return LEVEL_UP_COSTS[LEVEL_UP_COSTS.length - 1] + steps * LINEAR_GROWTH_PER_LEVEL;
    }

    /** Total cumulative GP needed to REACH the given level from level 1. */
    private static int totalGpRequiredForLevel(int level) {
        if (level <= 1) return 0;
        int total = 0;
        for (int lvl = 1; lvl < level; lvl++) {
            total += gpNeededToLevelUp(lvl);
        }
        return total;
    }

    public static int getGP(Player player) {
        return player.getData(ModAttachments.GRAND_POINTS.get());
    }

    public static void setGP(Player player, int amount) {
        player.setData(ModAttachments.GRAND_POINTS.get(), Math.max(0, amount));
    }

    public static void addGP(Player player, int amount) {
        int levelBefore = getLevel(player);
        setGP(player, getGP(player) + amount);
        int levelAfter = getLevel(player);

        if (levelAfter > levelBefore) {
            player.displayClientMessage(Component.literal("You leveled up! Now level " + levelAfter), true);
        }

        com.gimr12.towers_attack.titles.TitleManager.checkUnlocks(player);
        com.gimr12.towers_attack.network.PlayerDataSync.syncIfServer(player);
    }

    /** Removes GP directly (clamped at 0 by setGP). */
    public static void removeGP(Player player, int amount) {
        setGP(player, getGP(player) - amount);
        com.gimr12.towers_attack.network.PlayerDataSync.syncIfServer(player);
    }

    /**
     * Directly sets the player's level by moving their GP to that level's threshold.
     * SP is never stored, so this is always safe: dropping to a lower level and
     * climbing back up later always recomputes exactly (3 x level) - allocated,
     * no matter how many times you reset. If the player already has more points
     * allocated across their stats than the new (lower) level allows, the excess
     * is clawed back evenly across all 5 stats - see PlayerStats.clawBackExcess.
     */
    public static void setLevel(Player player, int targetLevel) {
        targetLevel = Math.max(1, targetLevel);
        int newGp = totalGpRequiredForLevel(targetLevel);
        setGP(player, newGp);
        PlayerStats.clawBackExcess(player, targetLevel);
        com.gimr12.towers_attack.titles.TitleManager.checkUnlocks(player);
        com.gimr12.towers_attack.network.PlayerDataSync.syncIfServer(player);
    }

    /** Returns the level derived from a raw GP amount. Pure math - usable client-side too. */
    public static int levelForGp(int gp) {
        int level = 1;
        while (totalGpRequiredForLevel(level + 1) <= gp) {
            level++;
        }
        return level;
    }

    /** Returns the player's current level, derived from their stored GP. */
    public static int getLevel(Player player) {
        return levelForGp(getGP(player));
    }

    /** GP required to reach the *next* level from where the player is now. */
    public static int getGpNeededForNextLevel(Player player) {
        int nextLevel = getLevel(player) + 1;
        return totalGpRequiredForLevel(nextLevel) - getGP(player);
    }

    /** How much GP the player has banked *within* their current level (for progress bars). */
    public static int getGpIntoCurrentLevel(int gp, int level) {
        return gp - totalGpRequiredForLevel(level);
    }

    /** Total GP required to go from the current level to the next (for progress bars). */
    public static int getGpRequiredForCurrentLevel(int level) {
        return gpNeededToLevelUp(level);
    }

    /**
     * SP available to spend right now: (3 x level) minus every point currently
     * allocated across all 5 stats. Nothing is stored for this - it's recomputed
     * from scratch every time, so it can never drift or double-count.
     */
    public static int getSP(Player player) {
        int totalAllocated = 0;
        for (StatType type : StatType.values()) {
            totalAllocated += PlayerStats.getStat(player, type);
        }
        return Math.max(0, SP_PER_LEVEL * getLevel(player) - totalAllocated);
    }
}
