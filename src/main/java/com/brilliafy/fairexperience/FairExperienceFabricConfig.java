package com.brilliafy.fairexperience;

//? if fabric {
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Fabric config backend. Fabric has no config API, so this uses a small JSON file at
 * {@code config/fairexperience.json} and writes the values into {@link FairExperienceConfig}.
 */
public final class FairExperienceFabricConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path;

    private FairExperienceFabricConfig() {
    }

    public static void load() {
        path = FabricLoader.getInstance().getConfigDir().resolve(FairExperience.MOD_ID + ".json");

        Data data = null;
        if (Files.exists(path)) {
            try {
                data = GSON.fromJson(Files.readString(path), Data.class);
            } catch (Exception exception) {
                FairExperience.LOGGER.warn("Could not read {}, using defaults", path, exception);
            }
        }
        if (data == null) {
            data = new Data();
        }

        FairExperienceConfig.enableFairEnchanting = data.enableFairEnchanting;
        FairExperienceConfig.enableFairAnvils = data.enableFairAnvils;
        FairExperienceConfig.enableFairAnvilCombining = data.enableFairAnvilCombining;
        FairExperienceConfig.removeTooExpensiveLimit = data.removeTooExpensiveLimit;
        FairExperienceConfig.enableFairDirectModifications = data.enableFairDirectModifications;
        FairExperienceConfig.debugLogging = data.debugLogging;

        save();
    }

    public static void save() {
        Data data = new Data();
        data.enableFairEnchanting = FairExperienceConfig.enableFairEnchanting;
        data.enableFairAnvils = FairExperienceConfig.enableFairAnvils;
        data.enableFairAnvilCombining = FairExperienceConfig.enableFairAnvilCombining;
        data.removeTooExpensiveLimit = FairExperienceConfig.removeTooExpensiveLimit;
        data.enableFairDirectModifications = FairExperienceConfig.enableFairDirectModifications;
        data.debugLogging = FairExperienceConfig.debugLogging;

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(data));
        } catch (IOException exception) {
            FairExperience.LOGGER.warn("Could not write " + path, exception);
        }
    }

    @SuppressWarnings("unused")
    private static final class Data {
        boolean enableFairEnchanting = true;
        boolean enableFairAnvils = true;
        boolean enableFairAnvilCombining = true;
        boolean removeTooExpensiveLimit = true;
        boolean enableFairDirectModifications = true;
        boolean debugLogging = false;
    }
}
//?}
