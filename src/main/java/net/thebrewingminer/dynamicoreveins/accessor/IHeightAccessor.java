package net.thebrewingminer.dynamicoreveins.accessor;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

public interface IHeightAccessor {
    void setHeightAccessor(LevelHeightAccessor heightAccessor);
    LevelHeightAccessor getHeightAccessor();
}
