package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.thebrewingminer.dynamicoreveins.codec.condition.predicate.AllConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.predicate.AnyConditions;

import java.util.Map;

public class VeinConditionRegistry {
    public static final Map<String, Codec<? extends IVeinCondition>> REGISTRY = Map.of(
            "minecraft:height_range", HeightRangeCondition.CODEC,
            "dynamic_veins:density_threshold", DensityFunctionThreshold.CODEC,
            "dynamic_veins:any_of", AllConditions.CODEC,
            "dynamic_veins:all_of", AnyConditions.CODEC
    );

    public static final Codec<IVeinCondition> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<IVeinCondition, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).flatMap(map -> {
                T typeKey = ops.createString("type");
                T typeElement = map.get(typeKey);

                if (typeElement == null) {
                    return DataResult.error("Missing 'type' field in vein condition");
                }

                return ops.getStringValue(typeElement).flatMap(typeName -> {
                    Codec<? extends IVeinCondition> conditionCodec = VeinConditionRegistry.REGISTRY.get(typeName);
                    if (conditionCodec == null) {
                        return DataResult.error("Unknown vein condition type: " + typeName);
                    }

                    return conditionCodec.decode(ops, input).map(pair -> Pair.of(pair.getFirst(), pair.getSecond()));
                });
            });
        }

        @Override
        public <T> DataResult<T> encode(IVeinCondition input, DynamicOps<T> ops, T prefix) {
            String typeName = input.type();
            @SuppressWarnings("unchecked")
            Codec<IVeinCondition> codec = (Codec<IVeinCondition>) VeinConditionRegistry.REGISTRY.get(typeName);
            if (codec == null) {
                return DataResult.error("Unknown vein condition type for encoding: " + typeName);
            }

            return codec.encode(input, ops, prefix);
        }
    };
}