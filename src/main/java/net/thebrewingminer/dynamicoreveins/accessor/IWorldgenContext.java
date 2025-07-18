package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

public interface IWorldgenContext {
        // Implements storing and retrieving general worldgen context.

        void setChunkGenerator(ChunkGenerator generator);
        ChunkGenerator getChunkGenerator();

        void setHeightAccessor(LevelHeightAccessor accessor);
        LevelHeightAccessor getHeightAccessor();

        void setDimension(ResourceKey<Level> dimensionKey);
        ResourceKey<Level> getDimension();
}