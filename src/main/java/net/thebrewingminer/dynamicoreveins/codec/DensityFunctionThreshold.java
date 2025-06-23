package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.DensityFunction;

public record DensityFunctionThreshold(DensityFunction function, double minThreshold, double maxThreshold){
    public static final Codec<DensityFunctionThreshold> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("input").forGetter(DensityFunctionThreshold::function),
            Codec.DOUBLE.fieldOf("min_threshold").forGetter(DensityFunctionThreshold::minThreshold),
            Codec.DOUBLE.fieldOf("max_threshold").forGetter(DensityFunctionThreshold::maxThreshold)
    ).apply(instance, DensityFunctionThreshold::new));
}
