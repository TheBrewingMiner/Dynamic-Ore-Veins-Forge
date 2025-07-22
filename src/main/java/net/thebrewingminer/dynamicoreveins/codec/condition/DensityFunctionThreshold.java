package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.thebrewingminer.dynamicoreveins.helper.NoiseWiringHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DensityFunctionThreshold implements IVeinCondition{
    private static final Set<DensityFunctionThreshold> INSTANCES = ConcurrentHashMap.newKeySet();                   // Tracks all instances of this condition.
    private final Map<ResourceKey<Level>, DensityFunction> mappedDensityFunctions = new ConcurrentHashMap<>();      // Caches mapped function per dimension per condition.

    public static final double DEFAULT_MIN_THRESHOLD = -1.0;
    public static final double DEFAULT_MAX_THRESHOLD = 1.0;
    protected final double minThreshold;
    protected final double maxThreshold;
    protected final DensityFunction function;

    public DensityFunctionThreshold(@Nullable DensityFunction function, double minThreshold, double maxThreshold){
        if (minThreshold > maxThreshold){
            throw new IllegalArgumentException("Minimum threshold (" + minThreshold + ") cannot be greater than maximum threshold (" + maxThreshold + ").");
        }
        this.function = function;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
        INSTANCES.add(this);                // Track this new object in the list.
    }

    protected static DensityFunctionThreshold fromCodec(Optional<DensityFunction> function, double minThreshold, double maxThreshold) {
        return new DensityFunctionThreshold(function.orElse(null), minThreshold, maxThreshold);
    }

    // Reads objects with an input function, and optionally min and max thresholds. If no function is specified, the function will be assigned null.
    // However, this leads to NPEs when accessed unless explicitly guarded against.
    public static final Codec<DensityFunctionThreshold> DENSITY_FUNCTION_THRESHOLD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DensityFunction.HOLDER_HELPER_CODEC.optionalFieldOf("input").forGetter(config -> Optional.ofNullable(config.function)),
        Codec.DOUBLE.fieldOf("min_threshold").orElse(Double.NEGATIVE_INFINITY).forGetter(config -> config.minThreshold),
        Codec.DOUBLE.fieldOf("max_threshold").orElse(Double.POSITIVE_INFINITY).forGetter(config -> config.maxThreshold)
    ).apply(instance, DensityFunctionThreshold::fromCodec));

    // Reads a direct density function object or reference, or the object described above.
    // If a direct density function, encode with default threshold [-1.0, 1.0].
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

    public static DensityFunctionThreshold createDefault(){
        // Default to a null-function-condition object with threshold [-1.0, 1.0].
        return new DensityFunctionThreshold(null, DEFAULT_MIN_THRESHOLD, DEFAULT_MAX_THRESHOLD);
    }

    protected double computeValue(DensityFunction densityFunction, Context context){
        // Computes a function (must already be wired to noises) at a point.
        int x = context.pos().getX();
        int y = context.pos().getY();
        int z = context.pos().getZ();
        return (densityFunction.compute(new DensityFunction.SinglePointContext(x, y, z)));
    }

    // Getters.
    public DensityFunction rawFunction(){ return function; }
    public double minThreshold(){ return minThreshold; }
    public double maxThreshold(){ return maxThreshold; }

    @Override
    public String type(){
        // String identifier.
        return "dynamic_veins:density_function";
    }

    @Override
    public boolean test(Context context){
        if (!(context.chunkGenerator() instanceof NoiseBasedChunkGenerator)) return false;  // Guard clause. Chunk generator type should be implicit but just to be sure.

        if(this.function == null){
            throw new IllegalStateException("Density function in conditions should not be null.");  // Lazily check valid state, since this condition is
        }                                                                                           // allowed to be null in three cases in DynamicOreVeinifier.

        // Wire and cache the provided function.
        DensityFunction wiredFunction = mappedDensityFunctions.computeIfAbsent(context.dimension(), __ -> this.function.mapAll(new NoiseWiringHelper(context)));

        double value = computeValue(wiredFunction, context);        // Delegate to helper method.
        return (value >= minThreshold && value <= maxThreshold);    // Test if within [minThreshold, maxThreshold].
    }

    public DensityFunction getOrMapFunction(Context context) {
        // A getter used to grab the mapped function of this condition instance.
        return mappedDensityFunctions.computeIfAbsent(context.dimension(), __ -> {
            if (this.function == null) throw new IllegalStateException("Incorrectly called; Cannot use method getOrMapFunction() with nulled function DensityFunctionThreshold object.");
            return this.function.mapAll(new NoiseWiringHelper(context));
        });
    }

    public void clearInstanceCache(){
        // Clear the cache of this condition instance.
        mappedDensityFunctions.clear();
    }

    public static void clearCache(){
        // Clear the cache of all DensityFunctionThreshold instances.
        for (DensityFunctionThreshold instance : INSTANCES){
            instance.clearInstanceCache();
        }
    }
}