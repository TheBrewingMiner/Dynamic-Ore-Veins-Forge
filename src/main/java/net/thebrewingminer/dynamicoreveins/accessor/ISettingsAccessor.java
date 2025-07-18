package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

public interface ISettingsAccessor {
        // Implement means to store and retrieve the noise generator settings.
        NoiseGeneratorSettings getNoiseGenSettings();

        // Allows retrieving the random state from the object. This was made despite IWorldgenContext,
        // and it was the simpler choice of using both interfaces.
        RandomState getRandomState();
}