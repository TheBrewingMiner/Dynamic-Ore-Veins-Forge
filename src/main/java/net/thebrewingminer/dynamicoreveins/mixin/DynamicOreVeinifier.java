package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.thebrewingminer.dynamicoreveins.accessor.HeightRangeWrapper;
import net.thebrewingminer.dynamicoreveins.accessor.IDimensionAware;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import net.thebrewingminer.dynamicoreveins.accessor.WorldgenContextCache;
import net.thebrewingminer.dynamicoreveins.codec.OreRichnessSettings;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.DensityFunctionThreshold;
import net.thebrewingminer.dynamicoreveins.codec.condition.FlattenConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.HeightRangeCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(NoiseChunk.class)
public abstract class DynamicOreVeinifier {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/OreVeinifier;create(Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;)Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;"
        )
    )
    private NoiseChunk.BlockStateFiller createVein(DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, PositionalRandomFactory randomFactory){
        Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getRegistry();
        if(veinRegistry.size() == 0) { return ((functionContext) -> null); }                // If registry is empty or null by now, don't do anything else. Return like nothing happened.
        List<OreVeinConfig> veinList = new ArrayList<>(veinRegistry.stream().toList());
        List<OreVeinConfig> shufflingList = new ArrayList<>(veinList);                      // Copy just to be sure original list does not get mutated
                                                                                            // in case of future use.
        long PLACE_HOLDER_SEED = 1;
        Random random = new Random(PLACE_HOLDER_SEED);
        Collections.shuffle(shufflingList, random);

        return (functionContext) -> {
            NoiseChunk noiseChunk = (NoiseChunk)(Object)this;
            IWorldgenContext wgContext = (IWorldgenContext)noiseChunk;

            ChunkGenerator chunkGenerator = wgContext.getChunkGenerator();
            LevelHeightAccessor heightAccessor = wgContext.getHeightAccessor();
            ResourceKey<Level> currDimension = wgContext.getDimension();

            if (currDimension == null && chunkGenerator instanceof IDimensionAware dimAware) {
                currDimension = dimAware.getDimension();
            }

            // Final fallback (slow but safe): use the static WorldgenContextCache
            if ((currDimension == null || chunkGenerator == null || heightAccessor == null) && currDimension != null) {
                WorldgenContextCache.WGContext fallback = WorldgenContextCache.getContext(currDimension);
                if (fallback != null) {
                    if (chunkGenerator == null) chunkGenerator = fallback.generator();
                    if (heightAccessor == null) heightAccessor = fallback.heightAccessor();
                }
            }

            // If still missing required info, log once and return null
            if (currDimension == null || chunkGenerator == null || heightAccessor == null) {
                System.err.println("-------------------------------------------------------------------------------------------------------");
                System.err.println("[DOV] Warning: Worldgen context missing during BlockStateFiller evaluation. Skipping vein placement.");
                System.err.println("  -> NoiseChunk = " + noiseChunk);
                System.err.println("  -> generator = " + chunkGenerator);
                System.err.println("  -> dimensionKey = " + currDimension);
                System.err.println("  -> heightAccessor = " + heightAccessor);
                System.err.println("  -> Pos = " + new BlockPos(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ()));
                System.err.println("-------------------------------------------------------------------------------------------------------");
                return null;
            }
            return selectVein(functionContext, routerVeinToggle, routerVeinRidged, routerVeinGap, shufflingList, heightAccessor, chunkGenerator, currDimension, randomFactory);
        };
    }

    @Unique
    private static boolean inThreshold(DensityFunction function, double min, double max, IVeinCondition.Context veinContext) {
        DensityFunctionThreshold tempThreshold = new DensityFunctionThreshold(function, min, max);
        return tempThreshold.test(veinContext);
    }

    @Unique
    private static BlockState selectVein(DensityFunction.FunctionContext functionContext, DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, List<OreVeinConfig> veinList, LevelHeightAccessor heightAccessor, ChunkGenerator chunkGenerator, ResourceKey<Level> currDimension, PositionalRandomFactory randomFactory){
        BlockPos pos = new BlockPos(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());

        IVeinCondition.Context veinContext = new IVeinCondition.Context() {
            @Override public BlockPos pos() { return pos;}
            @Override public LevelHeightAccessor heightAccessor() { return heightAccessor; }
            @Override public ChunkGenerator chunkGenerator() { return chunkGenerator; }
            @Override public double compute(DensityFunction function) { return function.compute(functionContext); }
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
//            System.out.println("Testing config in dimension: " + currDimension.location());
//            System.out.println("Config's allowed dimensions: " + veinConfig.dimension);
            if (!veinConfig.dimension.contains(Level.OVERWORLD)) continue;
//            System.out.println("Checked dimension.");
            /* Use configured vein toggle and shaping DFs if specified */
            localVeinToggle = (veinConfig.veinToggle.function() != null ? veinConfig.veinToggle.function() : routerVeinToggle);
            localVeinRidged = (veinConfig.veinRidged.function() != null ? veinConfig.veinRidged.function() : routerVeinRidged);
            localVeinGap = (veinConfig.veinGap.function() != null ? veinConfig.veinGap.function() : routerVeinGap);

            /* Calculate if in toggle's and shaping DFs' threshold */
            if (!inThreshold(localVeinToggle, veinConfig.veinToggle.minThreshold(), veinConfig.veinToggle.maxThreshold(), veinContext)) continue;
//            System.out.println("Passed toggle");

            if (veinConfig.conditions.test(veinContext)){
                selectedConfig = veinConfig;
                veinToggle = localVeinToggle;
                veinRidged = localVeinRidged;
                veinGap = localVeinGap;
                break;
            }
        }

        if (selectedConfig == null) return null;
//        System.out.println("Passed config testing! It is: " + selectedConfig);
//        System.out.println("Calling flattenConditions!!");
        List<IVeinCondition> conditionsList = FlattenConditions.flattenConditions(selectedConfig.conditions);
//        System.out.println("Calling findMatchingHeightRange!!");
        HeightRangeWrapper heightRange = findMatchingHeightRange(conditionsList, veinContext);
//        System.out.println("Calling dynamicOreVeinifier!");
        return dynamicOreVeinifier(functionContext, veinToggle, veinRidged, veinGap, selectedConfig, veinContext, heightRange, randomFactory);
    }


    @Unique
    private static HeightRangeWrapper findMatchingHeightRange(List<IVeinCondition> conditions, IVeinCondition.Context veinContext) {
        WorldGenerationContext worldGenContext = new WorldGenerationContext(veinContext.chunkGenerator(), veinContext.heightAccessor());
        HeightRangeWrapper firstMatchingRange = null;
        int matchingRangeCounter = 0;
        int DEFAULT_MIN_Y = -64;
        int DEFAULT_MAX_Y = 320;
        int minOverlapY = Integer.MIN_VALUE;
        int maxOverlapY = Integer.MAX_VALUE;
        int y = veinContext.pos().getY();

        for (IVeinCondition condition : conditions) {
            if (condition instanceof HeightRangeCondition heightCondition) {
                int minY = heightCondition.minInclusive().resolveY(worldGenContext);
                int maxY = heightCondition.maxInclusive().resolveY(worldGenContext);
                if (y >= minY && y <= maxY) {
                    matchingRangeCounter++;
                    if (firstMatchingRange == null){
                        firstMatchingRange = new HeightRangeWrapper(minY, maxY);    // For the first range the current pos is, find the min and max of that range.
                    }
                    if (minY > minOverlapY) minOverlapY = minY;                     // Set those as the found mins and maxes. If another height range is found for
                    if (maxY < maxOverlapY) maxOverlapY = maxY;                     // this position, expand to find the min and max of all found ranges.
                }
            }
        }
        if ((matchingRangeCounter > 1) && (minOverlapY <= maxOverlapY)){
            return (new HeightRangeWrapper(minOverlapY, maxOverlapY));      // Return a new range for the expanded range if more than one valid range was found for this pos.
        } else if (firstMatchingRange != null){                             // If only one valid range was found, return that one's range.
            return firstMatchingRange;
        } else {
            return new HeightRangeWrapper(DEFAULT_MIN_Y, DEFAULT_MAX_Y);    // Otherwise, default if no valid range was found.
        }
    }

    @Unique
    private static BlockState dynamicOreVeinifier(DensityFunction.FunctionContext functionContext, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap, OreVeinConfig config, IVeinCondition.Context veinContext, HeightRangeWrapper heightRange, PositionalRandomFactory randomFactory){
        RandomSource seededRandom = randomFactory.at(veinContext.pos());

        BlockState ore = config.ore.getState(seededRandom, veinContext.pos());
        BlockState secondaryOre = config.secondary_ore.getState(seededRandom, veinContext.pos());
        BlockState fillerBlock = config.fillerBlock.getState(seededRandom, veinContext.pos());
        float secondaryOreChance = config.secondary_ore_chance;
        OreRichnessSettings settings = config.veinSettings;

        BlockState toReturn = null;

        double toggleValue = veinToggle.compute(functionContext);
        int currY = veinContext.pos().getY();
        double toggleStrength = Math.abs(toggleValue);
        int relativeToMinY = (currY - heightRange.min_y());
        int relativeToMaxY = (heightRange.max_y() - currY);
        if (relativeToMinY >= 0 && relativeToMaxY >= 0){
            int relativeToVeinBoundary = Math.min(relativeToMinY, relativeToMaxY);
            double boundClamped = Mth.clampedMap(relativeToVeinBoundary, 0.0, settings.edgeRoundOffBegin(), -settings.maxEdgeRoundOff(), 0.0);
            if((toggleStrength + boundClamped) < settings.minRichnessThreshold()){
                return toReturn;
            } else {
                if(seededRandom.nextFloat() > settings.veinSolidness()){
                    return toReturn;
                } else if (veinRidged.compute(functionContext) >= 0.0){
                    return toReturn;
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
        } else {
            return toReturn;
        }
    }
}