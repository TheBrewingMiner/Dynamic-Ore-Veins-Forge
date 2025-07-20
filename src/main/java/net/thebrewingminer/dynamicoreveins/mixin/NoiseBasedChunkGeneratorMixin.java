package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.thebrewingminer.dynamicoreveins.accessor.IDimensionAware;
import net.thebrewingminer.dynamicoreveins.accessor.ISettingsAccessor;
import net.thebrewingminer.dynamicoreveins.accessor.IWorldgenContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin implements ISettingsAccessor {

    @Unique
    private NoiseGeneratorSettings settings;

    @Override
    public NoiseGeneratorSettings getNoiseGenSettings() { return this.settings; }

    @Override
    public RandomState getRandomState() { return null; }


    @Inject(
            method = "createNoiseChunk(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;)Lnet/minecraft/world/level/levelgen/NoiseChunk;",
            at = @At("RETURN")
    )
    private void attachWorldgenContext(ChunkAccess pChunk, StructureManager pStructureManager, Blender pBlender, RandomState pRandom, CallbackInfoReturnable<NoiseChunk> cir) {
        // Grab the noise chunk being created by NoiseBasedChunkGenerator.
        NoiseChunk noiseChunk = cir.getReturnValue();

        // Grab the instance of the chunk generator (This NoiseBasedChunkGenerator instance).
        ChunkGenerator chunkGenerator = (ChunkGenerator)(Object)this;

        // Grab the dimension cached into the chunk generator (stored by IDimensionAware in ChunkGeneratorMixin, set by ServerLevelMixin).
        ResourceKey<Level> dimension = ((IDimensionAware)chunkGenerator).getDimension();

        // Grab the level's height accessor from ChunkAccess parameter.
        LevelHeightAccessor heightAccessor = pChunk.getHeightAccessorForGeneration();

        // Grab the noise settings cached by the noise chunk (accessed via ISettingsAccessor in NoiseChunkMixin).
        NoiseGeneratorSettings settings = ((ISettingsAccessor)noiseChunk).getNoiseGenSettings();

        // Store the worldgen context into the noise settings (stored by IWorldgenContext in NoiseGeneratorSettingsMixin).
        IWorldgenContext wgContext = (IWorldgenContext)(Object)settings;

        wgContext.setDimension(dimension);
        wgContext.setChunkGenerator(chunkGenerator);
        wgContext.setHeightAccessor(heightAccessor);
    }
}