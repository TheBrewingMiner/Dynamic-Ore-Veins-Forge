package net.thebrewingminer.dynamicoreveins.registry;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.thebrewingminer.dynamicoreveins.codec.condition.*;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.AllConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.AnyConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.NotCondition;

import java.util.Map;

public class VeinConditionRegistry {
    // Maps the provided string identifier to the matching codec.
    public static final Map<String, Codec<? extends IVeinCondition>> REGISTRY = Map.of(
            "dynamic_veins:height_range", HeightRangeCondition.CODEC,
            "dynamic_veins:density_threshold", DensityFunctionThreshold.CODEC,
            "dynamic_veins:biome", IsBiomeCondition.CODEC,
            "dynamic_veins:any_of", AnyConditions.CODEC,
            "dynamic_veins:all_of", AllConditions.CODEC,
            "dynamic_veins:always_true", AlwaysTrueCondition.CODEC,
            "dynamic_veins:not", NotCondition.CODEC
    );

    private VeinConditionRegistry(){}

    private static class LazyCodecHolder{
        private static final Codec<IVeinCondition> PREDICATE_CODEC = new Codec<>() {

            // Process the string identifier and encodes/decodes according to the condition map REGISTRY.
            public <T> DataResult<Pair<IVeinCondition, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getMap(input).flatMap(map -> {
                    T typeKey = ops.createString("type");
                    T typeElement = map.get(typeKey);

                    if (typeElement == null) {
                        return DataResult.error(() -> "Missing 'type' field in vein condition");
                    }

                    return ops.getStringValue(typeElement).flatMap(typeName -> {
                        Codec<? extends IVeinCondition> conditionCodec = VeinConditionRegistry.REGISTRY.get(typeName);
                        if (conditionCodec == null) {
                            return DataResult.error(() -> "Unknown vein condition type: " + typeName);
                        }

                        return conditionCodec.decode(ops, input).map(pair -> Pair.of(pair.getFirst(), pair.getSecond()));
                    });
                });
            }


            public <T> DataResult<T> encode(IVeinCondition input, DynamicOps<T> ops, T prefix) {
                String typeName = input.type();

                @SuppressWarnings("unchecked")
                Codec<IVeinCondition> codec = (Codec<IVeinCondition>) VeinConditionRegistry.REGISTRY.get(typeName);
                if (codec == null) {
                    return DataResult.error(() -> "Unknown vein condition type for encoding: " + typeName);
                }

                return codec.encode(input, ops, prefix);
            }
        };

        // Builds the condition codec that accepts a direct predicate object or a list of predicates.
        // Lists of predicates are implicitly AND (All) conditions.
        private static final Codec<IVeinCondition> CODEC = Codec.either(
                PREDICATE_CODEC,            // Encode single object
                PREDICATE_CODEC.listOf()    // Encode list of conditions
        ).xmap(
            either -> either.map(
                    condition -> condition,
                    list -> {
                        if (list.size() == 1) {
                            return list.getFirst();
                        } else {
                            return new AllConditions(list);
                        }
                    }
            ),
            condition -> {
                if (condition instanceof AllConditions all) {
                    return Either.right(all.conditions());
                } else {
                    return Either.left(condition);
                }
            }
        );
    }

    // Getters.
    public static Codec<IVeinCondition> predicateCodec() {
        return LazyCodecHolder.PREDICATE_CODEC;
    }

    public static Codec<IVeinCondition> codec() {
        return LazyCodecHolder.CODEC;
    }
}