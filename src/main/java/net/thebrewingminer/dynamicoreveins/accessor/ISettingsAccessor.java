package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

public interface ISettingsAccessor {
        NoiseGeneratorSettings getNoiseGenSettings();
        RandomState getRandomState();
}