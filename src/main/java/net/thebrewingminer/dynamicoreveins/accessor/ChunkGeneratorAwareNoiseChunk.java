package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.world.level.chunk.ChunkGenerator;

public interface ChunkGeneratorAwareNoiseChunk {
    void setGenerator(ChunkGenerator generator);
    ChunkGenerator getGenerator();
}