package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.helper.NoiseWiringHelper;

import java.util.concurrent.ConcurrentHashMap;

public class VeinSettingsConfig{
    protected boolean vanillaVeinsEnabled;
    protected boolean vanillaVeinsPrioritized;
    protected DensityFunction shuffleSource;

    private final ConcurrentHashMap<ResourceKey<Level>, DensityFunction> mappedDensityFunctions = new ConcurrentHashMap<>();    // Caches the mapped functions per dimension.

    // Encodes simple boolean flags and a required density function.
    public static final Codec<VeinSettingsConfig> CODEC = RecordCodecBuilder.create(configInstance -> configInstance.group(
            Codec.BOOL.fieldOf("vanilla_veins_enabled").orElse(true).forGetter(config -> config.vanillaVeinsEnabled),
            Codec.BOOL.fieldOf("vanilla_priority").orElse(true).forGetter(config -> config.vanillaVeinsPrioritized),
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("shuffle_source").forGetter(config -> config.shuffleSource)
    ).apply(configInstance, VeinSettingsConfig::new));

    public VeinSettingsConfig(boolean vanillaVeinsEnabled, boolean vanillaVeinsPrioritized, DensityFunction shuffleSource){
        this.vanillaVeinsEnabled = vanillaVeinsEnabled;
        this.vanillaVeinsPrioritized = vanillaVeinsPrioritized;
        this.shuffleSource = shuffleSource;
    }

    // Getters.
    public boolean vanillaVeinsEnabled(){
        return this.vanillaVeinsEnabled;
    }

    public boolean vanillaVeinsPrioritized(){
        return this.vanillaVeinsPrioritized;
    }

    public DensityFunction shuffleSource(){
        return this.shuffleSource;
    }

    // Getter for the mapped density function.
    public DensityFunction getOrMapFunction(IVeinCondition.Context context){
        return mappedDensityFunctions.computeIfAbsent(context.dimension(), __ -> this.shuffleSource.mapAll(new NoiseWiringHelper(context)));
    }

    // Clears the density function cache.
    public void clearCache(){
        mappedDensityFunctions.clear();
    }
}