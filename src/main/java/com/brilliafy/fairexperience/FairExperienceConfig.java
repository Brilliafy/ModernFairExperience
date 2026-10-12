package com.brilliafy.fairexperience;

/**
 * Loader-agnostic config facade. The actual config backends (Fabric JSON, NeoForge
 * {@code ModConfigSpec}, Forge 1.20.1 {@code ForgeConfigSpec}) write into these fields
 * whenever the config is loaded or reloaded, so the mixins can read them cheaply.
 */
public final class FairExperienceConfig {

    public static boolean enableFairEnchanting = true;
    public static boolean enableFairAnvils = true;
    public static boolean enableFairAnvilCombining = true;
    public static boolean removeTooExpensiveLimit = true;
    public static boolean enableFairDirectModifications = true;
    public static boolean debugLogging = false;

    private FairExperienceConfig() {
    }
}
