package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.thebrewingminer.dynamicoreveins.accessor.ChunkGeneratorAwareNoiseChunk;
import net.thebrewingminer.dynamicoreveins.accessor.DimensionAwareNoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseBasedChunkGeneratorMixin {

    @Inject(
        method = "createNoiseChunk(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;)Lnet/minecraft/world/level/levelgen/NoiseChunk;",
        at = @At("RETURN")
    )
    private void injectChunkGenerator(ChunkAccess pChunk, StructureManager pStructureManager, Blender pBlender, RandomState pRandom, CallbackInfoReturnable<NoiseChunk> cir) {
        NoiseChunk chunk = cir.getReturnValue();
        ((ChunkGeneratorAwareNoiseChunk)chunk).setGenerator((ChunkGenerator)(Object)this);
    }

    @Inject(
            method = "createNoiseChunk(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/levelgen/RandomState;)Lnet/minecraft/world/level/levelgen/NoiseChunk;",
            at = @At("RETURN")
    )
    private void injectDimension(ChunkAccess chunk, StructureManager structureManager, Blender blender, RandomState random, CallbackInfoReturnable<NoiseChunk> cir) {
        ResourceKey<Level> dimension = ((DimensionAwareNoiseChunk) chunk).getDimension();
        ((DimensionAwareNoiseChunk) cir.getReturnValue()).setDimension(dimension);
    }
}