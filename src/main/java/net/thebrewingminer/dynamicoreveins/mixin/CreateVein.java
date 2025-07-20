package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.thebrewingminer.dynamicoreveins.accessor.ISettingsAccessor;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import net.thebrewingminer.dynamicoreveins.accessor.WorldSeedHolder;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import static net.thebrewingminer.dynamicoreveins.helper.PrepareList.getOrShuffleList;
import static net.thebrewingminer.dynamicoreveins.main.DynamicOreVeinifier.selectVein;

@Mixin(NoiseChunk.class)
public abstract class CreateVein {
    @SuppressWarnings("DataFlowIssue")
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/OreVeinifier;create(Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;)Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;"
        )
    )
    private NoiseChunk.BlockStateFiller createVein(DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, PositionalRandomFactory randomFactory){
        return (functionContext) -> {
            // Grab the cached noise generator settings from the current noise chunk.
            NoiseChunk noiseChunk = (NoiseChunk)(Object)this;
            NoiseGeneratorSettings noiseGeneratorSettings = ((ISettingsAccessor)noiseChunk).getNoiseGenSettings();

            // Grab the cached worldgen info from noise generator settings.
            IWorldgenContext wgContext = (IWorldgenContext)(Object)noiseGeneratorSettings;
            ChunkGenerator chunkGenerator = wgContext.getChunkGenerator();
            LevelHeightAccessor heightAccessor = wgContext.getHeightAccessor();
            ResourceKey<Level> currDimension = wgContext.getDimension();

//          If missing required info, log once and return null
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

            // Grab the rest of the information from the noise settings.
            boolean useLegacyRandomSource = noiseGeneratorSettings.useLegacyRandomSource();
            RandomState randomState = ((ISettingsAccessor)noiseChunk).getRandomState();

            long worldSeed = WorldSeedHolder.getSeed();     // Grab the cached world seed.
            BlockPos pos = new BlockPos(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());  // Construct a new block pos from known data.

            // Create a context object with all this info to process the JSON objects and test conditions.
            IVeinCondition.Context veinContext = new IVeinCondition.Context() {
                @Override public BlockPos pos() { return pos;}
                @Override public LevelHeightAccessor heightAccessor() { return heightAccessor; }
                @Override public ChunkGenerator chunkGenerator() { return chunkGenerator; }
                @Override public long seed() { return worldSeed; }
                @Override public boolean useLegacyRandomSource() { return useLegacyRandomSource; }
                @Override public RandomState randomState() { return randomState; }
                @Override public PositionalRandomFactory randomFactory() { return randomFactory; }
                @Override public ResourceKey<Level> dimension() { return currDimension; }
            };

            // Prepare the order in which to test veins.
            List<OreVeinConfig> veinList = getOrShuffleList(functionContext, veinContext);

            return selectVein(functionContext, routerVeinToggle, routerVeinRidged, routerVeinGap, veinList, veinContext);
        };
    }
}