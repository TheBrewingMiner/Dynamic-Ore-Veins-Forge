package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.thebrewingminer.dynamicoreveins.accessor.DimensionAwareNoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NoiseChunk.class)
public class NoiseChunkDimensionAwarenessMixin implements DimensionAwareNoiseChunk {

    @Unique
    private ResourceKey<Level> dimension;

    @Override
    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }
}
