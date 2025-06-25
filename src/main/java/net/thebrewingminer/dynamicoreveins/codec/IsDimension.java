package net.thebrewingminer.dynamicoreveins.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;

public class IsDimension {
    public static final Codec<List<ResourceKey<Level>>> CODEC = Codec.either(
            ResourceKey.codec(Registry.DIMENSION_REGISTRY),
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).listOf()
    ).xmap(
            either -> either.map(
                    Collections::singletonList,
                    (List<ResourceKey<Level>> list) -> list
            ),
            list -> {
                if(list.size() == 1) {
                    return Either.left(list.get(0));
                } else {
                    return Either.right(list);
                }
            }
    );
}