package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.thebrewingminer.dynamicoreveins.accessor.IDimensionAware;
import net.thebrewingminer.dynamicoreveins.accessor.ISettingsAccessor;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import net.thebrewingminer.dynamicoreveins.accessor.WorldgenContextCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    @Shadow @Final private ServerChunkCache chunkSource;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(MinecraftServer pServer, Executor pDispatcher, LevelStorageSource.LevelStorageAccess pLevelStorageAccess, ServerLevelData pServerLevelData, ResourceKey pDimensionKey, LevelStem pLevelStem, ChunkProgressListener pProgressListener, boolean pIsDebug, long pSeed, List pCustomSpawners, boolean pTickTime, CallbackInfo ci) {
        ChunkGenerator chunkGenerator = pLevelStem.generator();
        ((IDimensionAware) chunkGenerator).setDimension(pDimensionKey);
        LevelHeightAccessor heightAccessor = (LevelHeightAccessor) this;
        RandomState randomState = this.chunkSource.randomState();
        PositionalRandomFactory randomFactory = ((IRandomStateAccessor)(Object)randomState).randomFactory();

        WorldgenContextCache.setContext(pDimensionKey, chunkGenerator, heightAccessor);
        System.out.println("Cached generator and height accessor for dimension: " + pDimensionKey.location());

        NoiseGeneratorSettings settings = ((ISettingsAccessor) chunkGenerator).getNoiseGenSettings();

        if ((Object) settings instanceof IWorldgenContext wgContext) {
            if (chunkGenerator != null && heightAccessor != null) {
                wgContext.setChunkGenerator(chunkGenerator);
                wgContext.setDimension(pDimensionKey);
                wgContext.setHeightAccessor(heightAccessor);
                wgContext.setSeed(pSeed);
                wgContext.setRandomState(randomState);
                System.out.println("[DOV] Injected RandomState into NoiseGeneratorSettings: " + randomState);

                System.out.println("[DynamicOreVeins] Injected context into NoiseGeneratorSettings for " + pDimensionKey.location());
            } else {
                System.err.println("[DynamicOreVeins] Missing generator/heightAccessor for dimension: " + pDimensionKey.location());
            }
        } else {
            System.err.println("[DynamicOreVeins] NoiseGeneratorSettings is not an instance of IWorldgenContext");
        }
    }
}