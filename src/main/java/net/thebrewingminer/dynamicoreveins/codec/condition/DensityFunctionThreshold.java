package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.thebrewingminer.dynamicoreveins.helper.NoiseWiringHelper;
import org.jetbrains.annotations.Nullable;

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

    public static DensityFunctionThreshold createDefault(){
        return new DensityFunctionThreshold(null, DEFAULT_MIN_THRESHOLD, DEFAULT_MAX_THRESHOLD);
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
            throw new IllegalStateException("Density function in condition should not be null.");
        }

        if(wiredFunction == null){
            wiredFunction = this.function.mapAll(new NoiseWiringHelper(context));
        }

        double value = computeValue(wiredFunction, context);
        return (value >= minThreshold && value <= maxThreshold);
    }

//    @Override
//    public Codec<? extends IVeinCondition> codec(){
//        return CODEC;
//    }
}