package com.brilliafy.fairexperience;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Pure maths tests. These do not touch Minecraft classes so they run on every target.
 */
public class XpHelperTest {

    @Test
    public void rawXpForLevelMatchesVanillaTotals() {
        assertEquals(0, XpHelper.getRawXpForLevel(0));
        assertEquals(7, XpHelper.getRawXpForLevel(1));
        assertEquals(315, XpHelper.getRawXpForLevel(15));
        assertEquals(352, XpHelper.getRawXpForLevel(16));
        assertEquals(1395, XpHelper.getRawXpForLevel(30));
        assertEquals(1507, XpHelper.getRawXpForLevel(31));
        assertEquals(1628, XpHelper.getRawXpForLevel(32));

        for (int level = 0; level <= 500; level++) {
            assertEquals(level, XpHelper.getLevelForRawXp(XpHelper.getRawXpForLevel(level)));
        }
    }

    @Test
    public void barCapMatchesVanilla() {
        assertEquals(7, XpHelper.getXpBarCap(0));
        assertEquals(37, XpHelper.getXpBarCap(15));
        assertEquals(42, XpHelper.getXpBarCap(16));
        assertEquals(112, XpHelper.getXpBarCap(30));
    }

    @Test
    public void anvilCostIsRawXpOfTheFlatLevelCost() {
        assertEquals(0, XpHelper.getAnvilFairXpCost(0));
        assertEquals(7, XpHelper.getAnvilFairXpCost(1));
        assertEquals(55, XpHelper.getAnvilFairXpCost(5));
        assertEquals(1395, XpHelper.getAnvilFairXpCost(30));
    }

    @Test
    public void enchantingCostUsesTheRequirementWindow() {
        // Level 30 slot with a 3 level cost: XP(30) - XP(27)
        assertEquals(XpHelper.getRawXpForLevel(30) - XpHelper.getRawXpForLevel(27),
            XpHelper.getEnchantFairXpCost(30, 3));

        // Never zero, even for cheap slots.
        assertTrue(XpHelper.getEnchantFairXpCost(1, 1) >= 1);
        assertEquals(0, XpHelper.getEnchantFairXpCost(30, 0));

        // A cost larger than the requirement clamps to the requirement.
        assertEquals(XpHelper.getRawXpForLevel(5), XpHelper.getEnchantFairXpCost(5, 5));
    }

    @Test
    public void enchantingIsCheaperAtLowLevelsThanVanillaFlatLevels() {
        // The whole point of the mod: a level 30 slot at player level 46 must not cost
        // the raw XP of 3 levels at level 46 (which is ~900 XP) but the raw XP window of
        // the requirement itself.
        int fair = XpHelper.getEnchantFairXpCost(30, 3);
        int vanilla = XpHelper.getRawXpForLevel(46) - XpHelper.getRawXpForLevel(43);
        assertTrue(fair < vanilla / 2);
    }
}
