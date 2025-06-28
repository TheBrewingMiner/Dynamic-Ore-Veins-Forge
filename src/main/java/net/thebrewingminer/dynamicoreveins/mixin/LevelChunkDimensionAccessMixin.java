package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.thebrewingminer.dynamicoreveins.accessor.DimensionAwareNoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class LevelChunkDimensionAccessMixin implements DimensionAwareNoiseChunk {

        @Unique
        private ResourceKey<Level> dimension;

        @Inject(method = "<init>", at = @At("TAIL"))
        private void injectDimension(Level level, ChunkPos pos, CallbackInfo ci) {
            this.dimension = level.dimension(); // Store it
        }

        @Override
        public void setDimension(ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }

        @Override
        public ResourceKey<Level> getDimension() {
            return dimension;
        }
}