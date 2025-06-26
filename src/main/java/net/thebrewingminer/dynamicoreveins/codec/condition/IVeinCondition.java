package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;

public interface IVeinCondition {
    Codec<? extends IVeinCondition> codec();
    String type();
    boolean test(Context context);
    interface Context {
        BlockPos pos();
        LevelHeightAccessor heightAccessor();
        ChunkGenerator chunkGenerator();
        double compute(DensityFunction function);
    }
}