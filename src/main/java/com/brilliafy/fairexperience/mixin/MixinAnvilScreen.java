package com.brilliafy.fairexperience.mixin;

import com.brilliafy.fairexperience.FairExperienceConfig;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Client-side half of the "Too Expensive!" limit removal: keeps the anvil screen from
 * replacing the real cost with the expensive message once costs reach 40 levels.
 */
@Mixin(AnvilScreen.class)
public abstract class MixinAnvilScreen {

    @ModifyConstant(
        method = "renderLabels",
        constant = @Constant(intValue = 40)
    )
    private int fairExperience$modifyMaxAnvilCost(int constant) {
        if (FairExperienceConfig.removeTooExpensiveLimit) {
            return Integer.MAX_VALUE;
        }
        return constant;
    }
}
