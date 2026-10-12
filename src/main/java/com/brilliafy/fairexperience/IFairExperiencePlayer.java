package com.brilliafy.fairexperience;

/**
 * Duck-interface injected into the vanilla player class by the platform mixins so the
 * per-player XP bookkeeping survives across game versions without extra fields where
 * possible.
 */
public interface IFairExperiencePlayer {

    int fairExperience$getLastLevel();

    void fairExperience$setLastLevel(int level);

    int fairExperience$getLastTotalXp();

    void fairExperience$setLastTotalXp(int totalXp);
}
