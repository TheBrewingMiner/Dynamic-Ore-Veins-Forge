package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.DensityFunction;

public interface IVeinCondition {
    Codec<? extends IVeinCondition> codec();
    boolean test(Context context);
    interface Context {
        BlockPos pos();
        LevelHeightAccessor heightAccessor();
        double compute(DensityFunction function);
    }
}