package com.brilliafy.fairexperience.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code AnvilMenu} inherits its input/result containers and player reference from
 * {@code ItemCombinerMenu}. Shadowing inherited members does not survive the SRG remapping
 * of the reference map, so the fields are exposed through accessors declared on the class
 * that actually owns them.
 */
@Mixin(ItemCombinerMenu.class)
public interface MixinItemCombinerMenuAccessor {

    @Accessor("inputSlots")
    Container fairExperience$inputSlots();

    @Accessor("resultSlots")
    ResultContainer fairExperience$resultSlots();

    @Accessor("player")
    Player fairExperience$player();
}
