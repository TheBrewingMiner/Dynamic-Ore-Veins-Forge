package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.thebrewingminer.dynamicoreveins.accessor.IDimensionAware;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChunkGenerator.class)
abstract class ChunkGeneratorMixin implements IDimensionAware {

    @Unique
    private ResourceKey<Level> dimension;

    @Override
    public void setDimension(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    @Override
    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }
}

