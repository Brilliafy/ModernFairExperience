package com.brilliafy.fairexperience;

import net.minecraft.world.entity.player.Player;

/**
 * Pure maths for converting flat level costs into fair raw XP costs.
 *
 * <p>The level-up curve has been stable since 1.8 ({@code getXpNeededForNextLevel}), so the
 * same closed forms are used for every supported version. The raw XP required to reach a
 * level is the sum of every level-up requirement below it.</p>
 */
public final class XpHelper {

    private XpHelper() {
    }

    /**
     * Experience points needed to level up from {@code level} to {@code level + 1}.
     */
    public static int getXpBarCap(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else if (level >= 15) {
            return 37 + (level - 15) * 5;
        } else {
            return 7 + level * 2;
        }
    }

    /**
     * Total raw experience points required to reach {@code level} starting from level 0.
     */
    public static int getRawXpForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    /**
     * The player's exact current total raw XP (completed levels plus progress).
     */
    public static int getPlayerTotalXp(Player player) {
        int levelXp = getRawXpForLevel(player.experienceLevel);
        int cap = getXpBarCap(player.experienceLevel);
        int progressXp = Math.round(player.experienceProgress * cap);
        return levelXp + progressXp;
    }

    /**
     * Sets the player's total raw XP, updating level, progress bar and total. Server-side
     * syncing is handled by vanilla, which re-sends the experience packet whenever
     * {@code totalExperience} changes.
     */
    public static void setPlayerTotalXp(Player player, int newTotalXp) {
        if (newTotalXp <= 0) {
            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.totalExperience = 0;
            return;
        }

        int level = getLevelForRawXp(newTotalXp);
        int remainder = newTotalXp - getRawXpForLevel(level);

        player.experienceLevel = level;
        int cap = getXpBarCap(level);
        float progress = cap > 0 ? (float) remainder / (float) cap : 0.0F;
        if (progress >= 1.0F) {
            progress = 0.9999F;
        }
        player.experienceProgress = progress;
        player.totalExperience = newTotalXp;
    }

    /**
     * Highest level L such that {@code getRawXpForLevel(L) <= totalXp}.
     */
    public static int getLevelForRawXp(int totalXp) {
        if (totalXp <= 0) {
            return 0;
        }

        int low = 0;
        int high = 20000;
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (getRawXpForLevel(mid) <= totalXp) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    /**
     * Fair raw XP cost for an enchanting operation.
     *
     * @param reqLevel  the level requirement of the clicked slot
     * @param levelCost the flat level cost vanilla would charge
     */
    public static int getEnchantFairXpCost(int reqLevel, int levelCost) {
        if (levelCost <= 0) {
            return 0;
        }
        if (reqLevel < levelCost) {
            reqLevel = levelCost;
        }
        int startXp = getRawXpForLevel(reqLevel);
        int endXp = getRawXpForLevel(Math.max(0, reqLevel - levelCost));
        return Math.max(1, startXp - endXp);
    }

    /**
     * Fair raw XP cost for an anvil or other flat level deduction.
     */
    public static int getAnvilFairXpCost(int levelCost) {
        if (levelCost <= 0) {
            return 0;
        }
        return Math.max(1, getRawXpForLevel(levelCost));
    }
}
