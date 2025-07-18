package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.thebrewingminer.dynamicoreveins.accessor.IDimensionAware;
import net.thebrewingminer.dynamicoreveins.accessor.WorldSeedHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(MinecraftServer pServer, Executor pDispatcher, LevelStorageSource.LevelStorageAccess pLevelStorageAccess, ServerLevelData pServerLevelData, ResourceKey pDimensionKey, LevelStem pLevelStem, ChunkProgressListener pProgressListener, boolean pIsDebug, long pSeed, List pCustomSpawners, boolean pTickTime, CallbackInfo ci) {
        // Grab information of the dimension for each level on the Server.
        ChunkGenerator chunkGenerator = pLevelStem.generator();             // LevelStem provides the chunk generator.
        ((IDimensionAware)chunkGenerator).setDimension(pDimensionKey);      // Pass the provided dimension key into the chunk generator via IDimensionAware.
        WorldSeedHolder.setSeed(pSeed);                                     // Hold onto the world seed to retrieve later.
    }
}