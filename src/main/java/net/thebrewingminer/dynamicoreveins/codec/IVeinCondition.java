package net.thebrewingminer.dynamicoreveins.codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;

public interface IVeinCondition {
    boolean test(Context context);
    interface Context {
        BlockPos pos();
        LevelHeightAccessor heightAccessor();
    }
}