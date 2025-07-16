package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.thebrewingminer.dynamicoreveins.helper.NoiseWiringHelper;

public record VeinSettingsConfig(boolean vanillaVeinsEnabled, boolean vanillaVeinsPrioritized, DensityFunction shuffleSource, boolean mapped){
    public static final Codec<VeinSettingsConfig> CODEC = RecordCodecBuilder.create(configInstance -> configInstance.group(
            Codec.BOOL.fieldOf("vanilla_veins_enabled").orElse(true).forGetter(VeinSettingsConfig::vanillaVeinsEnabled),
            Codec.BOOL.fieldOf("vanilla_priority").orElse(true).forGetter(VeinSettingsConfig::vanillaVeinsPrioritized),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shuffle_source").forGetter(VeinSettingsConfig::shuffleSource)
    ).apply(configInstance, (vanillaVeinsEnabled, vanillaVeinsPrioritized, shuffleSource) -> new VeinSettingsConfig(vanillaVeinsEnabled, vanillaVeinsPrioritized, shuffleSource, false)));

    public VeinSettingsConfig mapAll(long seed, boolean useLegacyRandomSource, RandomState randomState, PositionalRandomFactory randomFactory){
        if (this.mapped) return this;
        DensityFunction mappedShuffle = this.shuffleSource.mapAll(new NoiseWiringHelper(seed, useLegacyRandomSource, randomState, randomFactory));
        return new VeinSettingsConfig(this.vanillaVeinsEnabled, this.vanillaVeinsPrioritized, mappedShuffle, true);
    }
}