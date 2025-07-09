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
import net.thebrewingminer.dynamicoreveins.main.DefaultVanillaVein;
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
        OreVeinConfig IRON_VEIN = DefaultVanillaVein.ironVein();
        OreVeinConfig COPPER_VEIN = DefaultVanillaVein.copperVein();
        List<OreVeinConfig> veinRegistryList;
        List<OreVeinConfig> veinList = new ArrayList<>();

        Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getRegistry();
        veinRegistryList = new ArrayList<>(veinRegistry.stream().toList());

        long PLACE_HOLDER_SEED = 1;
        Random random = new Random(PLACE_HOLDER_SEED);

        if (config.vanillaVeinsEnabled()){
            veinList.add(IRON_VEIN);
            veinList.add(COPPER_VEIN);
            if (config.vanillaVeinsPrioritized()){
                Collections.shuffle(veinRegistryList, random);
                veinList.addAll(veinRegistryList);
            } else {
                veinList.addAll(veinRegistryList);
                Collections.shuffle(veinList, random);
            }
        } else {
            Collections.shuffle(veinRegistryList, random);
            veinList.addAll(veinRegistryList);
        }

        if (veinList.isEmpty()) { return ((functionContext) -> null); }

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

            return selectVein(functionContext, routerVeinToggle, routerVeinRidged, routerVeinGap, veinList, heightAccessor, chunkGenerator, currDimension, seed, useLegacyRandomSource, randomState, randomFactory);
        };
    }
}