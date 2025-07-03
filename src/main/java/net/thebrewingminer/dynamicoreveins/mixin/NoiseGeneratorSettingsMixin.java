package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements IWorldgenContext {

    @Unique
    private ChunkGenerator chunkGenerator;

    @Unique
    private LevelHeightAccessor heightAccessor;

    @Unique
    private ResourceKey<Level> dimension;

    @Override
    public void setChunkGenerator(ChunkGenerator generator) {
        this.chunkGenerator = generator;
    }

    @Override
    public ChunkGenerator getChunkGenerator() {
        return chunkGenerator;
    }

    @Override
    public void setHeightAccessor(LevelHeightAccessor heightAccessor) {
        this.heightAccessor = heightAccessor;
    }

    @Override
    public LevelHeightAccessor getHeightAccessor() {
        return heightAccessor;
    }

    @Override
    public void setDimension(ResourceKey<Level> dimensionKey) {
        this.dimension = dimensionKey;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return dimension;
    }
}