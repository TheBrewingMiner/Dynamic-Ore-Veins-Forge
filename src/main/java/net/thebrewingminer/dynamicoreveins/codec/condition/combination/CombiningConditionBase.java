package net.thebrewingminer.dynamicoreveins.codec.condition.combination;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.registry.VeinConditionRegistry;

import java.util.List;
import java.util.function.Function;

public abstract class CombiningConditionBase implements IVeinCondition {
    protected final List<IVeinCondition> conditions;

    protected CombiningConditionBase(List<IVeinCondition> conditions){
        this.conditions = conditions;
    }

    public List<IVeinCondition> conditions(){
        return conditions;
    }

    public static <T extends CombiningConditionBase> Codec<T> codec(Function<List<IVeinCondition>, T> factory){
        // Encodes JSON objects as a list of vein conditions, or takes a direct vein condition object
        // and treats it as a list.
        Codec<List<IVeinCondition>> listOrObject = Codec.either(
            VeinConditionRegistry.predicateCodec(),
            VeinConditionRegistry.predicateCodec().listOf()
        ).xmap(
            either -> either.map(List::of, Function.identity()),
            list -> {
                if(list.size() == 1){
                    return Either.left(list.getFirst());
                } else {
                    return Either.right(list);
                }
            }
        );

        // Build the codec for use by the object passed in.
        return RecordCodecBuilder.create(instance -> instance.group(
            listOrObject.fieldOf("conditions").forGetter(CombiningConditionBase::conditions)
        ).apply(instance, factory));
    }
}