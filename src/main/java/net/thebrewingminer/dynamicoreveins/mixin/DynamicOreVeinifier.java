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
import net.minecraft.world.level.levelgen.*;
import net.thebrewingminer.dynamicoreveins.accessor.*;
import net.thebrewingminer.dynamicoreveins.codec.OreRichnessSettings;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.helper.HeightRangeWrapper;
import net.thebrewingminer.dynamicoreveins.helper.FlattenConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.helper.NoiseWiringHelper;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.thebrewingminer.dynamicoreveins.helper.FindMatchingHeightRange.findMatchingHeightRange;
import static net.thebrewingminer.dynamicoreveins.helper.inThresholdHelper.inThreshold;

@Mixin(NoiseChunk.class)
public class DynamicOreVeinifier {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/OreVeinifier;create(Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;)Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;"
        )
    )
    private NoiseChunk.BlockStateFiller createVein(DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, PositionalRandomFactory randomFactory){
        Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getRegistry();
        if(veinRegistry.size() == 0) { return ((functionContext) -> null); }                // If registry is empty by now, don't do anything else. Return like nothing happened.
        List<OreVeinConfig> veinList = new ArrayList<>(veinRegistry.stream().toList());
        List<OreVeinConfig> shufflingList = new ArrayList<>(veinList);                      // Copy just to be sure original list does not get mutated
                                                                                            // in case of future use.
        long PLACE_HOLDER_SEED = 1;
        Random random = new Random(PLACE_HOLDER_SEED);
        Collections.shuffle(shufflingList, random);

        return (functionContext) -> {
            NoiseChunk noiseChunk = (NoiseChunk)(Object)this;
            NoiseGeneratorSettings noiseGeneratorSettings = ((ISettingsAccessor)noiseChunk).getNoiseGenSettings();
            IWorldgenContext wgContext = (IWorldgenContext)(Object)noiseGeneratorSettings;
            ChunkGenerator chunkGenerator = wgContext.getChunkGenerator();
            LevelHeightAccessor heightAccessor = wgContext.getHeightAccessor();
            ResourceKey<Level> currDimension = wgContext.getDimension();
            long seed = wgContext.getSeed();
            boolean useLegacyRandomSource = noiseGeneratorSettings.useLegacyRandomSource();
            RandomState randomState = ((ISettingsAccessor) noiseChunk).getRandomState();
//            System.out.println("[DOV] Retrieved RandomState from wgContext: " + randomState);

            // Final fallback: use the static WorldgenContextCache
            if ((chunkGenerator == null || heightAccessor == null) && currDimension != null) {
                WorldgenContextCache.WGContext fallback = WorldgenContextCache.getContext(currDimension);
                if (fallback != null) {
                    if (chunkGenerator == null) chunkGenerator = fallback.generator();
                    if (heightAccessor == null) heightAccessor = fallback.heightAccessor();
                }
            }

//          If still missing required info, log once and return null
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

            return selectVein(functionContext, routerVeinToggle, routerVeinRidged, routerVeinGap, shufflingList, heightAccessor, chunkGenerator, currDimension, seed, useLegacyRandomSource, randomState, randomFactory);
        };
    }

    @Unique
    private static BlockState selectVein(DensityFunction.FunctionContext functionContext, DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, List<OreVeinConfig> veinList, LevelHeightAccessor heightAccessor, ChunkGenerator chunkGenerator, ResourceKey<Level> currDimension, long seed, boolean useLegacyRandomSource, RandomState randomState, PositionalRandomFactory randomFactory){
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
//            System.out.println("Testing config in dimension: " + currDimension.location());
//            System.out.println("Config's allowed dimensions: " + veinConfig.dimension);
            if (!veinConfig.dimension.contains(currDimension)) continue;
//            System.out.println("Checked dimension.");
            /* Use configured vein toggle and shaping DFs if specified */
            localVeinToggle = (veinConfig.veinToggle.function() != null ? veinConfig.veinToggle.function().mapAll(new NoiseWiringHelper(veinContext)) : routerVeinToggle);
            localVeinRidged = (veinConfig.veinRidged.function() != null ? veinConfig.veinRidged.function().mapAll(new NoiseWiringHelper(veinContext)) : routerVeinRidged);
            localVeinGap = (veinConfig.veinGap.function() != null ? veinConfig.veinGap.function().mapAll(new NoiseWiringHelper(veinContext)) : routerVeinGap);

            /* Calculate if in toggle's and shaping DFs' threshold */
            if (!inThreshold(localVeinToggle, veinConfig.veinToggle.minThreshold(), veinConfig.veinToggle.maxThreshold(), veinContext)) continue;
//            System.out.println("Passed toggle");

            if (veinConfig.conditions.test(veinContext)){
//                System.out.println("Testing conditions");
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
//        System.out.println("Found: " + heightRange.min_y() + " and " + heightRange.max_y());
//        System.out.println("Calling dynamicOreVeinifier!");
        return dynamicOreVeinifier(functionContext, veinToggle, veinRidged, veinGap, selectedConfig, veinContext, heightRange, randomFactory);
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
//            System.out.println("Toggle value = " + toggleValue);
//            System.out.printf("Toggle strength: %.4f | Bound clamped: %.4f | Sum: %.4f | Min threshold: %.4f\n", toggleStrength, boundClamped, (toggleStrength + boundClamped), settings.minRichnessThreshold());
//            System.out.println("Settings from config: " + config.veinSettings);
            if((toggleStrength + boundClamped) < settings.minRichnessThreshold()){
//                System.out.println("Lower than minRichnessThreshold.");
                return toReturn;
            } else {
                if(seededRandom.nextFloat() > settings.veinSolidness()){
//                    System.out.println("Random float is larger than vein_solidness.");
                    return toReturn;
                } else if (veinRidged.compute(functionContext) >= 0.0){
//                    System.out.println("Vein ridged is negative!");
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