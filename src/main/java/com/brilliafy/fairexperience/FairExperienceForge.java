package com.brilliafy.fairexperience;

//? if forge {
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FairExperience.MOD_ID)
public class FairExperienceForge {

    public FairExperienceForge() {
        FairExperience.VERSION = ModList.get()
            .getModContainerById(FairExperience.MOD_ID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("dev");

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FairExperienceForgeConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(FairExperienceForgeConfig::onConfigEvent);

        FairExperience.LOGGER.info(
            "Fair Experience {} initialized! Converting flat level costs into fair raw XP costs.",
            FairExperience.VERSION
        );
    }
}
//?}
