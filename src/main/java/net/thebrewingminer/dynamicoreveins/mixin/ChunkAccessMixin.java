package net.thebrewingminer.dynamicoreveins.mixin;


import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.thebrewingminer.dynamicoreveins.accessor.DimensionAwareChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkAccess.class)
public class ChunkAccessMixin implements DimensionAwareChunk {

    @Unique
    private ResourceKey<Level> dimension;

    @Override
    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    @Override
    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

}