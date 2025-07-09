package net.thebrewingminer.dynamicoreveins.main;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.thebrewingminer.dynamicoreveins.codec.OreRichnessSettings;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.DensityFunctionThreshold;
import net.thebrewingminer.dynamicoreveins.codec.condition.HeightRangeCondition;

import java.util.Collections;

public class DefaultVanillaVein {
    private DefaultVanillaVein(){}

    protected static OreVeinConfig IRON_VEIN = new OreVeinConfig(
        BlockStateProvider.simple(Blocks.DEEPSLATE_IRON_ORE),
        BlockStateProvider.simple(Blocks.RAW_IRON_BLOCK),
    0.02f,
        BlockStateProvider.simple(Blocks.TUFF),
        new DensityFunctionThreshold(null, Double.NEGATIVE_INFINITY, 0.0),
        new DensityFunctionThreshold(null, DensityFunctionThreshold.DEFAULT_MIN_THRESHOLD, DensityFunctionThreshold.DEFAULT_MAX_THRESHOLD),
        new DensityFunctionThreshold(null, DensityFunctionThreshold.DEFAULT_MIN_THRESHOLD, DensityFunctionThreshold.DEFAULT_MAX_THRESHOLD),
        OreRichnessSettings.createDefault(),
        Collections.singletonList(Level.OVERWORLD),
        new HeightRangeCondition(VerticalAnchor.absolute(-60), VerticalAnchor.absolute(-8))
    );

    protected static OreVeinConfig COPPER_VEIN = new OreVeinConfig(
            BlockStateProvider.simple(Blocks.COPPER_ORE),
            BlockStateProvider.simple(Blocks.RAW_COPPER_BLOCK),
            0.02f,
            BlockStateProvider.simple(Blocks.GRANITE),
            new DensityFunctionThreshold(null, 0.0, Double.POSITIVE_INFINITY),
            new DensityFunctionThreshold(null, DensityFunctionThreshold.DEFAULT_MIN_THRESHOLD, DensityFunctionThreshold.DEFAULT_MAX_THRESHOLD),
            new DensityFunctionThreshold(null, DensityFunctionThreshold.DEFAULT_MIN_THRESHOLD, DensityFunctionThreshold.DEFAULT_MAX_THRESHOLD),
            OreRichnessSettings.createDefault(),
            Collections.singletonList(Level.OVERWORLD),
            new HeightRangeCondition(VerticalAnchor.absolute(0), VerticalAnchor.absolute(50))
    );

    public static OreVeinConfig ironVein(){
        return IRON_VEIN;
    }

    public static OreVeinConfig copperVein(){
        return COPPER_VEIN;
    }
}
