package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.DensityFunctions;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record DensityFunctionThreshold(@Nullable DensityFunction function, double minThreshold, double maxThreshold) implements IVeinCondition{
    public static final double DEFAULT_MIN_THRESHOLD = -1.0;
    public static final double DEFAULT_MAX_THRESHOLD = 1.0;

    public DensityFunctionThreshold {
        if (minThreshold > maxThreshold){
            throw new IllegalArgumentException("Minimum threshold (" + minThreshold + ") cannot be greater than maximum threshold (" + maxThreshold + ").");
        }
    }

    public static final Codec<DensityFunctionThreshold> DENSITY_FUNCTION_THRESHOLD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctionThreshold::function),
            Codec.DOUBLE.fieldOf("min_threshold").orElse(Double.NEGATIVE_INFINITY).forGetter(DensityFunctionThreshold::minThreshold),
            Codec.DOUBLE.fieldOf("max_threshold").orElse(Double.POSITIVE_INFINITY).forGetter(DensityFunctionThreshold::maxThreshold)
    ).apply(instance, DensityFunctionThreshold::new));

    public static final Codec<DensityFunctionThreshold> CODEC = Codec.either(
            DensityFunction.HOLDER_HELPER_CODEC,
            DENSITY_FUNCTION_THRESHOLD_CODEC
    ).xmap(
            either -> either.map(
                    densityFunction -> new DensityFunctionThreshold(densityFunction, DEFAULT_MIN_THRESHOLD, DEFAULT_MAX_THRESHOLD),
                    densityFunctionThreshold -> densityFunctionThreshold
            ),
            Either::right
    );

    @Override
    public String type(){
        return "dynamic_veins:density_function";
    }

    @Override
    public boolean test(Context context){
        if (!(context.chunkGenerator() instanceof NoiseBasedChunkGenerator)) return false;

        if(this.function == null){
            throw new IllegalStateException("Density function in condition should not be null");
        }

        int posX = context.pos().getX();
        int posY = context.pos().getY();
        int posZ = context.pos().getZ();

        DensityFunction function = this.function.mapAll(new NoiseWiringHelper(context.seed(), context.useLegacyRandomSource(), context.randomState(), context.randomFactory()));

        double value = function.compute(new DensityFunction.SinglePointContext(posX, posY, posZ));
        return (value >= minThreshold && value <= maxThreshold);
    }

    @Override
    public Codec<? extends IVeinCondition> codec(){
        return CODEC;
    }

    /*
    *   Thank you ApolloUnknownDev for pointing me to the correct
    *   method to implement this.
    */
    public static class NoiseWiringHelper implements DensityFunction.Visitor{
        private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();
        private final long seed;
        private final boolean useLegacySource;
        final RandomState randomState;
        final PositionalRandomFactory randomFactory;

        NoiseWiringHelper(long seed, boolean useLegacySource, RandomState randomState, PositionalRandomFactory randomFactory){
            this.seed = seed;
            this.useLegacySource = useLegacySource;
            this.randomState = randomState;
            this.randomFactory = randomFactory;
        }

        private RandomSource newLegacyInstance(long noiseSeed) {
            return new LegacyRandomSource(seed + noiseSeed);
        }


        public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder){
            Holder<NormalNoise.NoiseParameters> data = noiseHolder.noiseData();
            NormalNoise noise;
            if (this.useLegacySource){
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
                RandomSource source = this.useLegacySource ? this.newLegacyInstance(0L) : this.randomFactory.fromHashOf(new ResourceLocation("terrain"));
                return blendedNoise.withNewRandom(source);
            } else {
//                return ((function instanceof DensityFunctions) ? DensityFunctions.endIslands(this.seed) : function);
                if (function.codec() == DensityFunctions.endIslands(0).codec()){
                    return DensityFunctions.endIslands(this.seed);
                } else return function;
            }
        }

        @Override
        public DensityFunction apply(DensityFunction function) {
            return this.wrapped.computeIfAbsent(function, this::wrapNew);
        }
    }
}