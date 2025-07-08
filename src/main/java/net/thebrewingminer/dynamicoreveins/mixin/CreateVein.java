package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.thebrewingminer.dynamicoreveins.accessor.*;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.codec.VeinSettingsConfig;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;
import net.thebrewingminer.dynamicoreveins.registry.VeinSettingsConfigLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static net.thebrewingminer.dynamicoreveins.main.DynamicOreVeinifier.selectVein;

@Mixin(NoiseChunk.class)
public class CreateVein {

    @SuppressWarnings("DataFlowIssue")
    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/OreVeinifier;create(Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;)Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;"
        )
    )
    private NoiseChunk.BlockStateFiller createVein(DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, PositionalRandomFactory randomFactory){
        VeinSettingsConfig config = VeinSettingsConfigLoader.get();
        if (config.vanillaVeinsEnabled()) {
            // enable logic
        }

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

            return selectVein(functionContext, routerVeinToggle, routerVeinRidged, routerVeinGap, shufflingList, heightAccessor, chunkGenerator, currDimension, seed, useLegacyRandomSource, randomState, randomFactory);
        };
    }
}