package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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

public class DensityFunctionThreshold implements IVeinCondition{
    public static final double DEFAULT_MIN_THRESHOLD = -1.0;
    public static final double DEFAULT_MAX_THRESHOLD = 1.0;
    protected final double minThreshold;
    protected final double maxThreshold;
    protected final DensityFunction function;
    protected DensityFunction wiredFunction = null;

    public DensityFunctionThreshold(@Nullable DensityFunction function, double minThreshold, double maxThreshold){
        if (minThreshold > maxThreshold){
            throw new IllegalArgumentException("Minimum threshold (" + minThreshold + ") cannot be greater than maximum threshold (" + maxThreshold + ").");
        }
        this.function = function;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    public static final Codec<DensityFunctionThreshold> DENSITY_FUNCTION_THRESHOLD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(config -> config.function),
            Codec.DOUBLE.fieldOf("min_threshold").orElse(Double.NEGATIVE_INFINITY).forGetter(config -> config.minThreshold),
            Codec.DOUBLE.fieldOf("max_threshold").orElse(Double.POSITIVE_INFINITY).forGetter(config -> config.maxThreshold)
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

    public double computeValue(DensityFunction densityFunction, Context context){
        int x = context.pos().getX();
        int y = context.pos().getY();
        int z = context.pos().getZ();
        return (densityFunction.compute(new DensityFunction.SinglePointContext(x, y, z)));
    }

    public DensityFunction function(){ return function; }
    public double minThreshold(){ return minThreshold; }
    public double maxThreshold(){ return maxThreshold; }

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

        if(wiredFunction == null){
            wiredFunction = this.function.mapAll(new NoiseWiringHelper(context.seed(), context.useLegacyRandomSource(), context.randomState(), context.randomFactory()));
        }

        double value = computeValue(wiredFunction, context);
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

        public NoiseWiringHelper(long seed, boolean useLegacySource, RandomState randomState, PositionalRandomFactory randomFactory){
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

//            System.out.println("Rebinding noise: " + data.unwrapKey().orElse(null));

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
            NormalNoise resolved = randomState.getOrCreateNoise(data.unwrapKey().orElseThrow());
//            System.out.println("[WIRING] For key " + data.unwrapKey().get().location() + ", resolved = " + resolved);
//            System.out.println("[WIRING] Instance noise: " + (resolved == null ? "NULL" : "OK"));

            return new DensityFunction.NoiseHolder(data, noise);
        }

        private DensityFunction wrapNew(DensityFunction function){
            if (function instanceof BlendedNoise blendedNoise){
                RandomSource source = this.useLegacySource ? this.newLegacyInstance(0L) : this.randomFactory.fromHashOf(new ResourceLocation("terrain"));
                return blendedNoise.withNewRandom(source);
            } else return function;
        }

        @Override
        public DensityFunction apply(DensityFunction function) {
            return this.wrapped.computeIfAbsent(function, this::wrapNew);
        }
    }
}