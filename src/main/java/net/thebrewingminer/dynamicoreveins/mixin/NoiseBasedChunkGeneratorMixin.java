package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.thebrewingminer.dynamicoreveins.accessor.ChunkGeneratorAwareNoiseChunk;
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
}