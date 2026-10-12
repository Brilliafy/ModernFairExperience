package com.brilliafy.fairexperience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader-agnostic entry point constants. The actual mod initialisers live in the
 * platform classes ({@code FairExperienceFabric}, {@code FairExperienceNeoForge},
 * {@code FairExperienceForge}) and are swapped in by Stonecutter.
 */
public final class FairExperience {

    public static final String MOD_ID = "fairexperience";
    public static final String NAME = "Fair Experience";

    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    /** Filled in by the platform entry point from the loader's mod metadata. */
    public static String VERSION = "dev";

    private FairExperience() {
    }
}
