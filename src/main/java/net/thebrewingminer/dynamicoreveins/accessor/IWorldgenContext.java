package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

public interface IWorldgenContext {
        void setChunkGenerator(ChunkGenerator generator);
        ChunkGenerator getChunkGenerator();

        void setHeightAccessor(LevelHeightAccessor accessor);
        LevelHeightAccessor getHeightAccessor();

        void setDimension(ResourceKey<Level> dimensionKey);
        ResourceKey<Level> getDimension();

        void setSeed(long seed);
        long getSeed();

        void setRandomState(RandomState randomState);
        RandomState getRandomState();
}