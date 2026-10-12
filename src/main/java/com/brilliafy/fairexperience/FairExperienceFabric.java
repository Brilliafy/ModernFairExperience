package com.brilliafy.fairexperience;

//? if fabric {
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class FairExperienceFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FairExperience.VERSION = FabricLoader.getInstance()
            .getModContainer(FairExperience.MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("dev");

        FairExperienceFabricConfig.load();

        FairExperience.LOGGER.info(
            "Fair Experience {} initialized! Converting flat level costs into fair raw XP costs.",
            FairExperience.VERSION
        );
    }
}
//?}
