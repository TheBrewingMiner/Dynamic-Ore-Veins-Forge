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

//    OreVeinifier.VeinType veinType = veinToggle.compute(functionContext) > 0.0 ? OreVeinifier.VeinType.COPPER : OreVeinifier.VeinType.IRON;

    // Default for the vanilla iron vein.
    protected static OreVeinConfig IRON_VEIN = new OreVeinConfig(
        BlockStateProvider.simple(Blocks.DEEPSLATE_IRON_ORE),   // Primary ore.
        BlockStateProvider.simple(Blocks.RAW_IRON_BLOCK),       // Secondary ore.
        0.02f,                                                  // Vanilla secondary ore chance
        BlockStateProvider.simple(Blocks.TUFF),                 // Filler block
        new DensityFunctionThreshold(null, Double.NEGATIVE_INFINITY, 0.0),  // Will use the vein toggle provided by the router; succeeds if its value is <= 0.0.
        DensityFunctionThreshold.createDefault(),       // Uses the vein ridged function provided by the noise router.
        DensityFunctionThreshold.createDefault(),       // Uses the vein gap function provided by the noise router.
        OreRichnessSettings.createDefault(),            // Uses vanilla constants.
        Collections.singletonList(Level.OVERWORLD),     // Only in the overworld.
        new HeightRangeCondition(VerticalAnchor.absolute(-60), VerticalAnchor.absolute(-8))    // The min and max Y range in Vanilla.
    );

    protected static OreVeinConfig COPPER_VEIN = new OreVeinConfig(
            BlockStateProvider.simple(Blocks.COPPER_ORE),       // Primary ore.
            BlockStateProvider.simple(Blocks.RAW_COPPER_BLOCK), // Secondary ore.
            0.02f,                                              // Vanilla secondary ore chance
            BlockStateProvider.simple(Blocks.GRANITE),          // Filler block
            new DensityFunctionThreshold(null, Double.MIN_VALUE, Double.POSITIVE_INFINITY), // Will use the vein toggle provided by the router; succeeds if its value is > 0.0.
            DensityFunctionThreshold.createDefault(),       // Uses the vein ridged function provided by the noise router.
            DensityFunctionThreshold.createDefault(),       // Uses the vein gap function provided by the noise router.
            OreRichnessSettings.createDefault(),            // Uses vanilla constants.
            Collections.singletonList(Level.OVERWORLD),     // Only in the overworld.
            new HeightRangeCondition(VerticalAnchor.absolute(0), VerticalAnchor.absolute(50))    // The min and max Y range in Vanilla.
    );

    // Getters.
    public static OreVeinConfig ironVein(){
        return IRON_VEIN;
    }

    public static OreVeinConfig copperVein(){
        return COPPER_VEIN;
    }
}
