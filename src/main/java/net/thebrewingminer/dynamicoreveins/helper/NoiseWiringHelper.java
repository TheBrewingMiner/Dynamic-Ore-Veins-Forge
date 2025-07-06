package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.HashMap;
import java.util.Map;

/*
 *   Thank you ApolloUnknownDev for pointing me to the correct
 *   method to implement this.
 */
public class NoiseWiringHelper implements DensityFunction.Visitor{
    private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();
    private final long seed;
    private final boolean useLegacyRandomSource;
    private final RandomState randomState;
    final PositionalRandomFactory randomFactory;

    public NoiseWiringHelper(IVeinCondition.Context context){
        this.seed = context.seed();
        this.useLegacyRandomSource = context.useLegacyRandomSource();
        this.randomState = context.randomState();
        this.randomFactory = context.randomFactory();
    }

    private RandomSource newLegacyInstance(long noiseSeed) {
        return new LegacyRandomSource(seed + noiseSeed);
    }

    public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder){
        Holder<NormalNoise.NoiseParameters> data = noiseHolder.noiseData();
        NormalNoise noise;

        if (this.useLegacyRandomSource){
            if (data.is(Noises.TEMPERATURE)){
                noise = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L),  new NormalNoise.NoiseParameters(-7, 1.0, new double[]{1.0}));
                return new DensityFunction.NoiseHolder(data, noise);
            }

            if (data.is(Noises.VEGETATION)){
                noise = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, 1.0, new double[]{1.0}));
                return new DensityFunction.NoiseHolder(data, noise);
            }

            if (data.is(Noises.SHIFT)){
                noise = NormalNoise.createLegacyNetherBiome(this.randomFactory.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0, new double[0]));
                return new DensityFunction.NoiseHolder(data, noise);
            }
        }

        noise = this.randomState.getOrCreateNoise(data.unwrapKey().orElseThrow());
        return new DensityFunction.NoiseHolder(data, noise);
    }

    private DensityFunction wrapNew(DensityFunction function){
        if (function instanceof BlendedNoise blendedNoise){
            RandomSource source = this.useLegacyRandomSource ? this.newLegacyInstance(0L) : this.randomFactory.fromHashOf(new ResourceLocation("terrain"));
            return blendedNoise.withNewRandom(source);
        } else return function;
    }

    @Override
    public DensityFunction apply(DensityFunction function) {
        return this.wrapped.computeIfAbsent(function, this::wrapNew);
    }
}
