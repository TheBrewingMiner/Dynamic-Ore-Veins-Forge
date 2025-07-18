package net.thebrewingminer.dynamicoreveins.codec.condition;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;

public interface IVeinCondition {
    // Conditions must implement a test method and a "type" (string identifier).
    boolean test(Context context);
    String type();

    // Defines the "worldgen context" that carries the necessary information
    // to test at a position in the world.
    interface Context {
        BlockPos pos();
        LevelHeightAccessor heightAccessor();
        ChunkGenerator chunkGenerator();
        long seed();
        boolean useLegacyRandomSource();
        RandomState randomState();
        PositionalRandomFactory randomFactory();
        ResourceKey<Level> dimension();
    }
}