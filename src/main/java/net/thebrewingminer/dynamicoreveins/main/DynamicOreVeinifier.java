package net.thebrewingminer.dynamicoreveins.main;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.thebrewingminer.dynamicoreveins.codec.OreRichnessSettings;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.helper.HeightRangeWrapper;
import net.thebrewingminer.dynamicoreveins.helper.NoiseWiringHelper;

import java.util.List;

import static net.thebrewingminer.dynamicoreveins.helper.FindMatchingHeightRange.findMatchingHeightRange;
import static net.thebrewingminer.dynamicoreveins.helper.FlattenConditions.flattenConditions;
import static net.thebrewingminer.dynamicoreveins.helper.InThresholdHelper.inThreshold;

public class DynamicOreVeinifier {
    private DynamicOreVeinifier(){}

    public static BlockState selectVein(DensityFunction.FunctionContext functionContext, DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, List<OreVeinConfig> veinList, LevelHeightAccessor heightAccessor, ChunkGenerator chunkGenerator, ResourceKey<Level> currDimension, long seed, boolean useLegacyRandomSource, RandomState randomState, PositionalRandomFactory randomFactory){
        BlockPos pos = new BlockPos(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());

        IVeinCondition.Context veinContext = new IVeinCondition.Context() {
            @Override public BlockPos pos() { return pos;}
            @Override public LevelHeightAccessor heightAccessor() { return heightAccessor; }
            @Override public ChunkGenerator chunkGenerator() { return chunkGenerator; }
            @Override public long seed() { return seed; }
            @Override public boolean useLegacyRandomSource() { return useLegacyRandomSource; }
            @Override public RandomState randomState() { return randomState; }
            @Override public PositionalRandomFactory randomFactory() { return randomFactory; }
        };

        DensityFunction veinToggle = routerVeinToggle;
        DensityFunction veinRidged = routerVeinRidged;
        DensityFunction veinGap = routerVeinGap;

        OreVeinConfig selectedConfig = null;

        for (OreVeinConfig veinConfig : veinList) {
            DensityFunction localVeinToggle;
            DensityFunction localVeinRidged;
            DensityFunction localVeinGap;

            /* Check if in suitable dimension. */
            if (!veinConfig.dimension().contains(currDimension)) continue;

            /* Use configured vein toggle and shaping DFs if specified */
            localVeinToggle = (veinConfig.veinToggle().function() != null ? veinConfig.veinToggle().function().mapAll(new NoiseWiringHelper(veinContext)) : routerVeinToggle);
            localVeinRidged = (veinConfig.veinRidged().function() != null ? veinConfig.veinRidged().function().mapAll(new NoiseWiringHelper(veinContext)) : routerVeinRidged);
            localVeinGap = (veinConfig.veinGap().function() != null ? veinConfig.veinGap().function().mapAll(new NoiseWiringHelper(veinContext)) : routerVeinGap);

            /* Calculate if in toggle's and shaping DFs' threshold */
            if (!inThreshold(localVeinToggle, veinConfig.veinToggle().minThreshold(), veinConfig.veinToggle().maxThreshold(), veinContext)) continue;

            if (veinConfig.conditions().test(veinContext)){
                selectedConfig = veinConfig;
                veinToggle = localVeinToggle;
                veinRidged = localVeinRidged;
                veinGap = localVeinGap;
                break;
            }
        }

        if (selectedConfig == null) return null;
        List<IVeinCondition> conditionsList = flattenConditions(selectedConfig.conditions());
        HeightRangeWrapper heightRange = findMatchingHeightRange(conditionsList, veinContext);
        return dynamicOreVeinifier(functionContext, veinToggle, veinRidged, veinGap, selectedConfig, veinContext, heightRange);
    }

    public static BlockState dynamicOreVeinifier(DensityFunction.FunctionContext functionContext, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap, OreVeinConfig config, IVeinCondition.Context veinContext, HeightRangeWrapper heightRange){
        RandomSource seededRandom = veinContext.randomFactory().at(veinContext.pos());

        BlockState ore = config.ore().getState(seededRandom, veinContext.pos());
        BlockState secondaryOre = config.secondaryOre().getState(seededRandom, veinContext.pos());
        BlockState fillerBlock = config.fillerBlock().getState(seededRandom, veinContext.pos());
        float secondaryOreChance = config.secondaryOreChance();
        OreRichnessSettings settings = config.veinSettings();

        BlockState empty = null;

        double toggleValue = veinToggle.compute(functionContext);
        int currY = veinContext.pos().getY();
        double toggleStrength = Math.abs(toggleValue);
        int relativeToMinY = (currY - heightRange.min_y());
        int relativeToMaxY = (heightRange.max_y() - currY);
        if (relativeToMinY >= 0 && relativeToMaxY >= 0){
            int relativeToVeinBoundary = Math.min(relativeToMinY, relativeToMaxY);
            double boundClamped = Mth.clampedMap(relativeToVeinBoundary, 0.0, settings.edgeRoundOffBegin(), -settings.maxEdgeRoundOff(), 0.0);
            if((toggleStrength + boundClamped) < settings.minRichnessThreshold()){
                return empty;
            } else {
                if(seededRandom.nextFloat() > settings.veinSolidness()){
                    return empty;
                } else if (veinRidged.compute(functionContext) >= 0.0){
                    return empty;
                } else {
                    double richness = Mth.clampedMap(toggleStrength, settings.minRichnessThreshold(), settings.maxRichnessThreshold(), settings.minRichness(), settings.maxRichness());
                    if(((double)seededRandom.nextFloat() < richness) && (veinGap.compute(functionContext) > settings.skipOreThreshold())){
                        System.out.println("Success! " + veinContext.pos().toString());
                        return (seededRandom.nextFloat() < secondaryOreChance ? secondaryOre : ore);
                    } else {
                        System.out.println("Success! " + veinContext.pos().toString());
                        return fillerBlock;
                    }
                }
            }
        } else return empty;
    }
}