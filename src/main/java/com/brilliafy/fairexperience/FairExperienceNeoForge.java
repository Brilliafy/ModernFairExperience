package com.brilliafy.fairexperience;

//? if neoforge {
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(FairExperience.MOD_ID)
public class FairExperienceNeoForge {

    public FairExperienceNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        FairExperience.VERSION = modContainer.getModInfo().getVersion().toString();

        FairExperienceNeoForgeConfig.register(modContainer);
        modEventBus.addListener(FairExperienceNeoForgeConfig::onConfigEvent);

        FairExperience.LOGGER.info(
            "Fair Experience {} initialized! Converting flat level costs into fair raw XP costs.",
            FairExperience.VERSION
        );
    }
}
//?}
