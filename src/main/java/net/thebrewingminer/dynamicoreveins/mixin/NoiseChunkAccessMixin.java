package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.thebrewingminer.dynamicoreveins.accessor.NoiseChunkAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunk.class)
public class NoiseChunkAccessMixin implements NoiseChunkAccessor {

    @Unique
    private LevelHeightAccessor cachedHeightAccessor;

    @Override
    public LevelHeightAccessor getHeightAccessor() {
        return cachedHeightAccessor;
    }

    @Override
    public void setHeightAccessor(LevelHeightAccessor accessor) {
        this.cachedHeightAccessor = accessor;
    }

    @Inject(method = "forChunk", at = @At("RETURN"))
    private static void onForChunk(ChunkAccess chunk, RandomState state, DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings settings, Aquifer.FluidPicker fluidPicker, Blender blender, CallbackInfoReturnable<NoiseChunk> cir) {
        NoiseChunk chunkInstance = cir.getReturnValue();
        ((NoiseChunkAccessor)chunkInstance).setHeightAccessor(chunk);
    }
}