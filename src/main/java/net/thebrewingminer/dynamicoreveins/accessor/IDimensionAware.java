package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IDimensionAware {
    // Implement means to store and retrieve the dimension key.
    void setDimension(ResourceKey<Level> dimension);
    ResourceKey<Level> getDimension();
}