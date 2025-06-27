package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.DensityFunction;

public interface IVeinCondition {
    boolean test(DensityFunction.FunctionContext context);
    Codec<? extends IVeinCondition> codec();
    String type();

    /* Deprecated interface wrapper*/
//    interface Context {
//        BlockPos pos();
//        LevelHeightAccessor heightAccessor();
//        ChunkGenerator chunkGenerator();
//        double compute(DensityFunction function);
//    }
}