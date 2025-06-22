package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public class blockStateOrWeightedListCodec {
    public static final Codec<BlockStateProvider> BLOCK_OR_WEIGHTED_LIST = Codec.either(
            ResourceLocation.CODEC,
            WeightedStateProvider.CODEC
    ).xmap(
            either -> either.map(
                    location -> BlockStateProvider.simple(
                            Registry.BLOCK.getOrThrow(ResourceKey.create(Registry.BLOCK_REGISTRY, location)).defaultBlockState()
                    ),
                    weighted -> weighted
            ),
            provider -> {
                if (provider instanceof WeightedStateProvider weighted) {
                    return Either.right(weighted);
                } else if (provider instanceof SimpleStateProvider simple) {
                    Block block = simple.getState(RandomSource.create(), BlockPos.ZERO).getBlock();
                    ResourceLocation location = Registry.BLOCK.getKey(block);
                    return Either.left(location);
                }
                throw new IllegalArgumentException("Unsupported BlockStateProvider type: " + provider.getClass());
            }
    );
}
