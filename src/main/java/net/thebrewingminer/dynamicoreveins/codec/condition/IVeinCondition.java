package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;

public interface IVeinCondition {
    boolean test(Context context);
    Codec<? extends IVeinCondition> codec();
    String type();
    interface Context {
        BlockPos pos();
        LevelHeightAccessor heightAccessor();
        ChunkGenerator chunkGenerator();
        long seed();
        boolean useLegacyRandomSource();
        RandomState randomState();
        PositionalRandomFactory randomFactory();
    }
}