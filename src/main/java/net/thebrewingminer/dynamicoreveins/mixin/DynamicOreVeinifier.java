package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.thebrewingminer.dynamicoreveins.accessor.ChunkGeneratorAwareNoiseChunk;
import net.thebrewingminer.dynamicoreveins.accessor.DimensionAwareNoiseChunk;
import net.thebrewingminer.dynamicoreveins.accessor.NoiseChunkAccessor;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.DensityFunctionThreshold;
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
public class DynamicOreVeinifier {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/OreVeinifier;create(Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;)Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;"
        )
    )
    protected NoiseChunk.BlockStateFiller dynamicOreVeinifier(DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, PositionalRandomFactory randomFactory){
        Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getRegistry();
        List<OreVeinConfig> veinList = new ArrayList<>(veinRegistry.stream().toList());
        List<OreVeinConfig> shufflingList = new ArrayList<>(veinList);                      // Copy just to be sure original list does not get mutated
                                                                                            // in case of future use.
        long PLACE_HOLDER_SEED = 1;
        Random random = new Random(PLACE_HOLDER_SEED);
        Collections.shuffle(shufflingList, random);

        return (DensityFunction.FunctionContext context) -> computeBlockState(context, routerVeinToggle, routerVeinRidged, routerVeinGap, shufflingList);
    }

    @Unique
    private static boolean inThreshold(DensityFunction function, double min, double max, IVeinCondition.Context context) {
        return new DensityFunctionThreshold(function, min, max).test(context);
    }

    @Unique
    private BlockState computeBlockState(DensityFunction.FunctionContext functionContext, DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, List<OreVeinConfig> veinList){
        BlockPos pos = new BlockPos(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());
        LevelHeightAccessor levelHeightAccessor = ((NoiseChunkAccessor)this).getHeightAccessor();
        ChunkGenerator chunkGenerator = ((ChunkGeneratorAwareNoiseChunk)this).getGenerator();
        ResourceKey<Level> currDimension = ((DimensionAwareNoiseChunk)this).getDimension();

        IVeinCondition.Context veinContext = new IVeinCondition.Context() {
            @Override public BlockPos pos() { return pos;}
            @Override public LevelHeightAccessor heightAccessor() { return levelHeightAccessor; }
            @Override public ChunkGenerator chunkGenerator() { return chunkGenerator; }
            @Override public double compute(DensityFunction function) { return function.compute(functionContext); }
        };

        DensityFunction veinToggle = routerVeinToggle;
        DensityFunction veinRidged = routerVeinRidged;
        DensityFunction veinGap = routerVeinGap;

        OreVeinConfig selectedConfig = null;

        for (OreVeinConfig veinConfig : veinList) {

            /* Check if in suitable dimension. */
            if (!veinConfig.dimension.contains(currDimension)) continue;

            /* Use configured vein toggle if specified */
            if(veinConfig.veinToggle.function() != null) veinToggle = veinConfig.veinToggle.function();

            /* Calculate if in toggle threshold */
            if (!inThreshold(veinToggle, veinConfig.veinToggle.minThreshold(), veinConfig.veinToggle.maxThreshold(), veinContext)) continue;

            if (veinConfig.conditions.test(veinContext)){
                selectedConfig = veinConfig;
                break;
            }
        }

        // Vanilla Veinifier

        return null;
    }
}