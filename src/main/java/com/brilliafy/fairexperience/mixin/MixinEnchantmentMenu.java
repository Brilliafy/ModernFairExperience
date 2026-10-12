package com.brilliafy.fairexperience.mixin;

import com.brilliafy.fairexperience.FairExperienceContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Captures the required level of the clicked enchanting-table slot so the fair raw XP
 * cost can be computed when the player's experience is deducted.
 */
@Mixin(EnchantmentMenu.class)
public abstract class MixinEnchantmentMenu {

    @Shadow
    public int[] costs;

    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void fairExperience$beforeClickMenuButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (this.costs != null && id >= 0 && id < this.costs.length) {
            FairExperienceContext.setEnchantReqLevel(this.costs[id]);
        }
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void fairExperience$afterClickMenuButton(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        FairExperienceContext.clearEnchantReqLevel();
    }
}
