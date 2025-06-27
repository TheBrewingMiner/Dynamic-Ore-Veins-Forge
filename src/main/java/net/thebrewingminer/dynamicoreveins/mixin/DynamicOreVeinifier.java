package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.thebrewingminer.dynamicoreveins.accessor.ChunkGeneratorAwareNoiseChunk;
import net.thebrewingminer.dynamicoreveins.accessor.NoiseChunkAccessor;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
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
        List<OreVeinConfig> shufflingList = new ArrayList<>(veinList);

        long PLACE_HOLDER_SEED = 1;
        Random random = new Random(PLACE_HOLDER_SEED);
        Collections.shuffle(shufflingList, random);

        return (DensityFunction.FunctionContext context) -> computeBlockState(context, routerVeinToggle, routerVeinRidged, routerVeinGap, shufflingList);
    }

    @Unique
    private BlockState computeBlockState(DensityFunction.FunctionContext context, DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, List<OreVeinConfig> veinList){
        BlockPos pos = new BlockPos(context.blockX(), context.blockY(), context.blockZ());
        LevelHeightAccessor levelHeightAccessor = ((NoiseChunkAccessor)this).getHeightAccessor();
        ChunkGenerator chunkGenerator = ((ChunkGeneratorAwareNoiseChunk)this).getGenerator();

        IVeinCondition.Context veinContext = new IVeinCondition.Context() {
            @Override public BlockPos pos() { return pos;}
            @Override public LevelHeightAccessor heightAccessor() { return levelHeightAccessor; }
            @Override public ChunkGenerator chunkGenerator() { return chunkGenerator; }
            @Override public double compute(DensityFunction function) { return function.compute(context); }
        };

        // Your vein decision logic here
        for (OreVeinConfig veinConfig : veinList) {
            if (veinConfig.conditions.test(veinContext)){
                return Blocks.STONE.defaultBlockState();
            }
        }

        return null;
    }
}