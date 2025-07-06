package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NoiseGeneratorSettings.class)
public class NoiseGeneratorSettingsMixin implements IWorldgenContext {

    @Unique private ChunkGenerator chunkGenerator;
    @Unique private LevelHeightAccessor heightAccessor;
    @Unique private ResourceKey<Level> dimension;
    @Unique private long seed;
    @Unique private RandomState randomState;
    @Unique private PositionalRandomFactory randomFactory;

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

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public void setRandomState(RandomState randomState) {
        this.randomState = randomState;
    }

    @Override
    public RandomState getRandomState() {
        return this.randomState;
    }
}