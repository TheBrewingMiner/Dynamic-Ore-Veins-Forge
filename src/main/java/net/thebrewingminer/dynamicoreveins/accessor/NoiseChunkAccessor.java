package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.world.level.LevelHeightAccessor;

public interface NoiseChunkAccessor {
    LevelHeightAccessor getHeightAccessor();
    void setHeightAccessor(LevelHeightAccessor accessor);
}
