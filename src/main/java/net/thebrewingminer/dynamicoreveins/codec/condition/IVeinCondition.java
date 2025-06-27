package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.DensityFunction;

public interface IVeinCondition {
    Codec<? extends IVeinCondition> codec();
    String type();
    boolean test(DensityFunction densityFunction);
    /* Deprecated interface wrapper*/
//    interface Context {
//        BlockPos pos();
//        LevelHeightAccessor heightAccessor();
//        ChunkGenerator chunkGenerator();
//        double compute(DensityFunction function);
//    }
}