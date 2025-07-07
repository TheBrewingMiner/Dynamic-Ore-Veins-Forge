package net.thebrewingminer.dynamicoreveins.codec.condition.combination;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.registry.VeinConditionRegistry;

import java.util.List;
import java.util.function.Function;

public abstract class CombiningCondition implements IVeinCondition {
    protected final List<IVeinCondition> conditions;

    protected CombiningCondition(List<IVeinCondition> conditions){
        this.conditions = conditions;
    }

    public List<IVeinCondition> conditions(){
        return conditions;
    }

    public static <T extends CombiningCondition> Codec<T> codec(Function<List<IVeinCondition>, T> factory){
        return RecordCodecBuilder.create(instance -> instance.group(
                VeinConditionRegistry.predicateCodec().listOf().fieldOf("conditions").forGetter(CombiningCondition::conditions)
        ).apply(instance, factory));
    }
}