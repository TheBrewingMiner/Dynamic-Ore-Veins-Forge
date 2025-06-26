package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;

public class ResourceKeyOrBlockState {
    public static final Codec<BlockStateProvider> CODEC = Codec.either(
            ResourceLocation.CODEC,
            BlockStateProvider.CODEC
    ).xmap(
        either -> either.map(
                location -> BlockStateProvider.simple(
                        BuiltInRegistries.BLOCK.getOrThrow(ResourceKey.create(Registries.BLOCK, location)).defaultBlockState()
                ),
                provider -> provider
        ),
        stateProvider -> {
            if (stateProvider instanceof SimpleStateProvider simpleProvider) {
                Block block = simpleProvider.getState(RandomSource.create(), BlockPos.ZERO).getBlock();
                ResourceLocation location = BuiltInRegistries.BLOCK.getKey(block);
                return Either.left(location);
            } else {
                return Either.right(stateProvider);
            }
        }
    );
}