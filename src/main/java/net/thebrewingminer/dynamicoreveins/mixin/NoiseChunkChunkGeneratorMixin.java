package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.thebrewingminer.dynamicoreveins.accessor.ChunkGeneratorAwareNoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NoiseChunk.class)
public class NoiseChunkChunkGeneratorMixin implements ChunkGeneratorAwareNoiseChunk {

    @Unique
    private ChunkGenerator generator;

    @Override
    public void setGenerator(ChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public ChunkGenerator getGenerator() {
        return this.generator;
    }
}