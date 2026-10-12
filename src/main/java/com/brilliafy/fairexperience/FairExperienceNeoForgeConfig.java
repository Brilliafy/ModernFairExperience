package com.brilliafy.fairexperience;

//? if neoforge {
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * NeoForge config backend (TOML, {@code config/fairexperience-common.toml}).
 */
public final class FairExperienceNeoForgeConfig {

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_FAIR_ENCHANTING;
    private static final ModConfigSpec.BooleanValue ENABLE_FAIR_ANVILS;
    private static final ModConfigSpec.BooleanValue ENABLE_FAIR_ANVIL_COMBINING;
    private static final ModConfigSpec.BooleanValue REMOVE_TOO_EXPENSIVE_LIMIT;
    private static final ModConfigSpec.BooleanValue ENABLE_FAIR_DIRECT_MODIFICATIONS;
    private static final ModConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Configuration for Fair Experience").push("general");

        ENABLE_FAIR_ENCHANTING = builder
            .comment("If true, enchanting table level costs will be converted to fair raw XP costs.")
            .define("enableFairEnchanting", true);

        ENABLE_FAIR_ANVILS = builder
            .comment("If true, anvil level costs will be converted to fair raw XP costs.")
            .define("enableFairAnvils", true);

        ENABLE_FAIR_ANVIL_COMBINING = builder
            .comment("If true, anvil enchanting calculations are fair and order-independent: combining books or applying them in any order costs the same, prior work penalties do not penalize combines, and combining books to reach higher levels will not yield higher costs to combine with items.")
            .define("enableFairAnvilCombining", true);

        REMOVE_TOO_EXPENSIVE_LIMIT = builder
            .comment("If true, removes the vanilla 40-level 'Too Expensive!' anvil limit, allowing operations that cost 40 or more levels. If false, operations costing 40+ levels are blocked like in vanilla.")
            .define("removeTooExpensiveLimit", true);

        ENABLE_FAIR_DIRECT_MODIFICATIONS = builder
            .comment("If true, direct experienceLevel subtractions (e.g. by other mods) will be converted to fair raw XP costs.")
            .define("enableFairDirectModifications", true);

        DEBUG_LOGGING = builder
            .comment("Enable detailed logging for experience calculations.")
            .define("debugLogging", false);

        builder.pop();
        SPEC = builder.build();
    }

    private FairExperienceNeoForgeConfig() {
    }

    public static void register(ModContainer modContainer) {
        //? if <1.21.2 {
        modContainer.registerConfig(ModConfig.Type.COMMON, SPEC);
        //?} else {
        /*modContainer.registerConfig(ModConfig.Type.STARTUP, SPEC);
        *///?}
    }

    public static void onConfigEvent(ModConfigEvent event) {
        if (FairExperience.MOD_ID.equals(event.getConfig().getModId())) {
            bake();
        }
    }

    public static void bake() {
        FairExperienceConfig.enableFairEnchanting = ENABLE_FAIR_ENCHANTING.get();
        FairExperienceConfig.enableFairAnvils = ENABLE_FAIR_ANVILS.get();
        FairExperienceConfig.enableFairAnvilCombining = ENABLE_FAIR_ANVIL_COMBINING.get();
        FairExperienceConfig.removeTooExpensiveLimit = REMOVE_TOO_EXPENSIVE_LIMIT.get();
        FairExperienceConfig.enableFairDirectModifications = ENABLE_FAIR_DIRECT_MODIFICATIONS.get();
        FairExperienceConfig.debugLogging = DEBUG_LOGGING.get();
    }
}
//?}
