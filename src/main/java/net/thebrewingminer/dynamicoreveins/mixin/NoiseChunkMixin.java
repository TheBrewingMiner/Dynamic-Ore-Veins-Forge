package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements IWorldgenContext {

    @Unique
    private ChunkGenerator dynamicVeins_generator;

    @Unique
    private LevelHeightAccessor dynamicVeins_heightAccessor;

    @Unique
    private ResourceKey<Level> dynamicVeins_dimension;

    @Override
    public void setChunkGenerator(ChunkGenerator generator) {
        this.dynamicVeins_generator = generator;
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return this.dynamicVeins_generator;
    }

    @Override
    public void setHeightAccessor(LevelHeightAccessor accessor) {
        this.dynamicVeins_heightAccessor = accessor;
    }

    @Override
    public LevelHeightAccessor getHeightAccessor() {
        return this.dynamicVeins_heightAccessor;
    }

    @Override
    public void setDimension(ResourceKey<Level> dimensionKey) {
        this.dynamicVeins_dimension = dimensionKey;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return this.dynamicVeins_dimension;
    }
}
