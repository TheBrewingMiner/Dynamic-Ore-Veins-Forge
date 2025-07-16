package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.thebrewingminer.dynamicoreveins.accessor.ISettingsAccessor;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import static net.thebrewingminer.dynamicoreveins.helper.PrepareList.prepareList;
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
            NoiseChunk noiseChunk = (NoiseChunk)(Object)this;
            NoiseGeneratorSettings noiseGeneratorSettings = ((ISettingsAccessor)noiseChunk).getNoiseGenSettings();

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

            long seed = wgContext.getSeed();
            boolean useLegacyRandomSource = noiseGeneratorSettings.useLegacyRandomSource();
            RandomState randomState = ((ISettingsAccessor) noiseChunk).getRandomState();

            VeinSettingsConfig config = OreVeinRegistryHolder.getActiveConfig();
            VeinSettingsConfig mappedConfig = config.mapAll(seed, useLegacyRandomSource, randomState, randomFactory);

            List<OreVeinConfig> veinList = prepareList(functionContext, mappedConfig, seed);

            BlockPos pos = new BlockPos(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ());

            IVeinCondition.Context veinContext = new IVeinCondition.Context() {
                @Override public BlockPos pos() { return pos;}
                @Override public LevelHeightAccessor heightAccessor() { return heightAccessor; }
                @Override public ChunkGenerator chunkGenerator() { return chunkGenerator; }
                @Override public long seed() { return seed; }
                @Override public boolean useLegacyRandomSource() { return useLegacyRandomSource; }
                @Override public RandomState randomState() { return randomState; }
                @Override public PositionalRandomFactory randomFactory() { return randomFactory; }
                @Override public ResourceKey<Level> dimension() { return currDimension; }
            };

            return selectVein(functionContext, routerVeinToggle, routerVeinRidged, routerVeinGap, veinList, veinContext);
        };
    }
}