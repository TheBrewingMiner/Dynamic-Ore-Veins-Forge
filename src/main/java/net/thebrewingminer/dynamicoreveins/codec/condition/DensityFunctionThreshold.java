package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.DensityFunction;

public record DensityFunctionThreshold(DensityFunction function, double minThreshold, double maxThreshold) implements IVeinCondition {
    public static final double DEFAULT_MIN_THRESHOLD = -1.0;
    public static final double DEFAULT_MAX_THRESHOLD = 1.0;

    public DensityFunctionThreshold {
        if (minThreshold > maxThreshold) {
            throw new IllegalArgumentException("Minimum threshold (" + minThreshold + ") cannot be greater than maximum threshold (" + maxThreshold + ").");
        }
        if (function == null) {
            throw new NullPointerException("Density function should not be null.");
        }
    }

    public static final Codec<DensityFunctionThreshold> DENSITY_FUNCTION_THRESHOLD_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctionThreshold::function),
            Codec.DOUBLE.fieldOf("min_threshold").forGetter(DensityFunctionThreshold::minThreshold),
            Codec.DOUBLE.fieldOf("max_threshold").forGetter(DensityFunctionThreshold::maxThreshold)
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
    public boolean test(IVeinCondition.Context context){
        double value = context.compute(function);
        return (value >= minThreshold && value <= maxThreshold);
    }

    @Override
    public Codec<? extends IVeinCondition> codec() {
        return CODEC;
    }
}