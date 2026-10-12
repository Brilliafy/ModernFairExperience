package com.brilliafy.fairexperience.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code broadcastChanges} is declared on {@code AbstractContainerMenu}, so it is invoked
 * through an invoker on the owning class instead of a shadow on {@code AnvilMenu}.
 */
@Mixin(AbstractContainerMenu.class)
public interface MixinAbstractContainerMenuAccessor {

    @Invoker("broadcastChanges")
    void fairExperience$broadcastChanges();
}
