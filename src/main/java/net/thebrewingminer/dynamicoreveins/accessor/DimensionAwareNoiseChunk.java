package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface DimensionAwareNoiseChunk {
    void setDimension(ResourceKey<Level> dimension);
    ResourceKey<Level> getDimension();
}