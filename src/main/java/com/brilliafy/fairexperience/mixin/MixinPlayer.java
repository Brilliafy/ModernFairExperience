package com.brilliafy.fairexperience.mixin;

import com.brilliafy.fairexperience.FairExperienceConfig;
import com.brilliafy.fairexperience.FairExperienceContext;
import com.brilliafy.fairexperience.IFairExperiencePlayer;
import com.brilliafy.fairexperience.XpHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Converts flat level deductions performed by vanilla into fair raw XP deductions, and
 * detects direct {@code experienceLevel} field modifications done by other mods.
 */
@Mixin(Player.class)
public abstract class MixinPlayer implements IFairExperiencePlayer {

    @Shadow
    protected int enchantmentSeed;

    @Unique
    private int fairExperience$lastLevel = -1;

    @Unique
    private int fairExperience$lastTotalXp = -1;

    @Override
    public int fairExperience$getLastLevel() {
        return this.fairExperience$lastLevel;
    }

    @Override
    public void fairExperience$setLastLevel(int level) {
        this.fairExperience$lastLevel = level;
    }

    @Override
    public int fairExperience$getLastTotalXp() {
        return this.fairExperience$lastTotalXp;
    }

    @Override
    public void fairExperience$setLastTotalXp(int totalXp) {
        this.fairExperience$lastTotalXp = totalXp;
    }

    /**
     * Enchanting table deduction. {@code EnchantmentMenu.clickMenuButton} calls this with
     * the flat level cost; only the server side touches XP to avoid client desyncs.
     */
    @Inject(method = "onEnchantmentPerformed", at = @At("HEAD"), cancellable = true)
    private void fairExperience$onEnchantmentPerformed(ItemStack enchantedItem, int levelCost, CallbackInfo ci) {
        if (!FairExperienceConfig.enableFairEnchanting) {
            return;
        }

        Player self = (Player) (Object) this;
        if (self.level().isClientSide() || self.getAbilities().instabuild) {
            return;
        }

        ci.cancel();

        int reqLevel = FairExperienceContext.getEnchantReqLevel();
        if (reqLevel <= 0 && self.containerMenu instanceof EnchantmentMenu menu) {
            int slotId = levelCost - 1;
            if (menu.costs != null && slotId >= 0 && slotId < menu.costs.length) {
                reqLevel = menu.costs[slotId];
            }
        }
        if (reqLevel <= 0) {
            if (levelCost == 1) {
                reqLevel = 8;
            } else if (levelCost == 2) {
                reqLevel = 15;
            } else if (levelCost == 3) {
                reqLevel = 30;
            } else {
                reqLevel = Math.max(30, levelCost * 10);
            }
        }

        int rawXpCost = XpHelper.getEnchantFairXpCost(reqLevel, levelCost);
        int newTotalXp = Math.max(0, XpHelper.getPlayerTotalXp(self) - rawXpCost);

        XpHelper.setPlayerTotalXp(self, newTotalXp);
        this.enchantmentSeed = self.getRandom().nextInt();

        this.fairExperience$lastLevel = self.experienceLevel;
        this.fairExperience$lastTotalXp = XpHelper.getPlayerTotalXp(self);
    }

    /**
     * Flat level deductions (e.g. the anvil cost) are converted to fair raw XP costs.
     * Positive values (XP grants) are left to vanilla.
     */
    @Inject(method = "giveExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void fairExperience$giveExperienceLevels(int levels, CallbackInfo ci) {
        if (levels >= 0) {
            return;
        }

        Player self = (Player) (Object) this;
        if (self.level().isClientSide() || self.getAbilities().instabuild) {
            return;
        }

        int costLevels = -levels;
        int reqLevel = FairExperienceContext.getEnchantReqLevel();

        if (reqLevel > 0) {
            if (!FairExperienceConfig.enableFairEnchanting) {
                return;
            }
        } else if (!FairExperienceConfig.enableFairAnvils) {
            return;
        }

        ci.cancel();

        int rawXpCost = reqLevel > 0
            ? XpHelper.getEnchantFairXpCost(reqLevel, costLevels)
            : XpHelper.getAnvilFairXpCost(costLevels);

        int newTotalXp = Math.max(0, XpHelper.getPlayerTotalXp(self) - rawXpCost);

        XpHelper.setPlayerTotalXp(self, newTotalXp);

        this.fairExperience$lastLevel = self.experienceLevel;
        this.fairExperience$lastTotalXp = XpHelper.getPlayerTotalXp(self);
    }

    /**
     * Detects direct {@code experienceLevel} modifications from other mods and applies the
     * fair raw XP cost, while respecting mods that drain raw XP properly (they keep
     * {@code totalExperience} in sync).
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void fairExperience$onTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        if (self.level().isClientSide()) {
            this.fairExperience$lastLevel = self.experienceLevel;
            this.fairExperience$lastTotalXp = XpHelper.getPlayerTotalXp(self);
            return;
        }

        if (!FairExperienceConfig.enableFairDirectModifications || self.isDeadOrDying()) {
            this.fairExperience$lastLevel = self.experienceLevel;
            this.fairExperience$lastTotalXp = XpHelper.getPlayerTotalXp(self);
            return;
        }

        if (FairExperienceContext.isProcessing()) {
            return;
        }

        int currentLevel = self.experienceLevel;
        int currentTotalXp = XpHelper.getPlayerTotalXp(self);

        if (this.fairExperience$lastLevel < 0) {
            this.fairExperience$lastLevel = currentLevel;
            this.fairExperience$lastTotalXp = currentTotalXp;
            return;
        }

        if (currentLevel < this.fairExperience$lastLevel) {
            boolean isRawXpModification = Math.abs(self.totalExperience - currentTotalXp) <= 2
                && Math.abs(self.totalExperience - this.fairExperience$lastTotalXp) > 2;

            if (isRawXpModification) {
                this.fairExperience$lastLevel = currentLevel;
                this.fairExperience$lastTotalXp = currentTotalXp;
                return;
            }

            FairExperienceContext.setProcessing(true);
            try {
                int levelDrop = this.fairExperience$lastLevel - currentLevel;
                int reqLevel = FairExperienceContext.getEnchantReqLevel();
                int rawXpCost;

                if (reqLevel > 0) {
                    rawXpCost = XpHelper.getEnchantFairXpCost(reqLevel, levelDrop);
                } else if (self.containerMenu instanceof EnchantmentMenu menu) {
                    int slotId = levelDrop - 1;
                    if (menu.costs != null && slotId >= 0 && slotId < menu.costs.length && menu.costs[slotId] > 0) {
                        rawXpCost = XpHelper.getEnchantFairXpCost(menu.costs[slotId], levelDrop);
                    } else {
                        rawXpCost = XpHelper.getAnvilFairXpCost(levelDrop);
                    }
                } else {
                    rawXpCost = XpHelper.getAnvilFairXpCost(levelDrop);
                }

                int baseTotalXp = this.fairExperience$lastTotalXp > 0
                    ? this.fairExperience$lastTotalXp
                    : XpHelper.getRawXpForLevel(this.fairExperience$lastLevel);
                int newTotalXp = Math.max(0, baseTotalXp - rawXpCost);

                XpHelper.setPlayerTotalXp(self, newTotalXp);

                this.fairExperience$lastLevel = self.experienceLevel;
                this.fairExperience$lastTotalXp = XpHelper.getPlayerTotalXp(self);
            } finally {
                FairExperienceContext.setProcessing(false);
            }
        } else {
            this.fairExperience$lastLevel = currentLevel;
            this.fairExperience$lastTotalXp = currentTotalXp;
        }
    }
}
