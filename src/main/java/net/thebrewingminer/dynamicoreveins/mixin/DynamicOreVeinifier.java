package net.thebrewingminer.dynamicoreveins.mixin;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.thebrewingminer.dynamicoreveins.codec.OreVeinConfig;
import net.thebrewingminer.dynamicoreveins.registry.OreVeinRegistryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(NoiseChunk.class)
public class DynamicOreVeinifier {

    @Redirect(
        method = "<init>",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/levelgen/OreVeinifier;create(Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/PositionalRandomFactory;)Lnet/minecraft/world/level/levelgen/NoiseChunk$BlockStateFiller;"
        )
    )
    protected NoiseChunk.BlockStateFiller dynamicOreVeinifier(DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, PositionalRandomFactory randomFactory){
        BlockState defaultState = null;
        return (blockStateRule) -> {
            DensityFunction veinToggle = routerVeinToggle;

            Registry<OreVeinConfig> veinRegistry = OreVeinRegistryHolder.getRegistry();
            List<OreVeinConfig> veinList = new ArrayList<>(veinRegistry.stream().toList());
            List<OreVeinConfig> shufflingList = new ArrayList<>(veinList);

            long PLACE_HOLDER_SEED = 1;
            Random random = new Random(PLACE_HOLDER_SEED);
            Collections.shuffle(shufflingList, random);

            return (DensityFunction.FunctionContext context) -> computeBlockState(context, routerVeinToggle, routerVeinRidged, routerVeinGap, shufflingList);
        };
    }

    @Unique
    private static BlockState computeBlockState(DensityFunction.FunctionContext context, DensityFunction routerVeinToggle, DensityFunction routerVeinRidged, DensityFunction routerVeinGap, List<OreVeinConfig> veinList){
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();

        // Your vein decision logic here
        for (OreVeinConfig config : veinList) {
            //
        }

        return null;
    }
}