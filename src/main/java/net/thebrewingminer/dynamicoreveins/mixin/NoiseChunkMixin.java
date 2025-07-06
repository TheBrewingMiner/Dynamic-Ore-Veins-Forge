package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.thebrewingminer.dynamicoreveins.accessor.ISettingsAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements ISettingsAccessor {

    @Unique
    private NoiseGeneratorSettings cachedNoiseGenSettings;

    @Unique
    private RandomState cachedRandomState;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(int pCellCountXZ, RandomState pRandom, int p_224345_, int p_224346_, NoiseSettings pNoiseSettings, DensityFunctions.BeardifierOrMarker pBeardifier, NoiseGeneratorSettings pNoiseGeneratorSettings, Aquifer.FluidPicker pFluidPicker, Blender pBlendifier, CallbackInfo ci){
        this.cachedNoiseGenSettings = pNoiseGeneratorSettings;
        this.cachedRandomState = pRandom;
    }

    @Override
    public NoiseGeneratorSettings getNoiseGenSettings() {
        return cachedNoiseGenSettings;
    }

    @Override
    public RandomState getRandomState() {
        return cachedRandomState;
    }
}