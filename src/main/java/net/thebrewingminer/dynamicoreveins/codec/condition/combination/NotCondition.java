package net.thebrewingminer.dynamicoreveins.codec.condition.combination;

import com.mojang.serialization.Codec;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

public class NotCondition extends CombiningConditionBase {
    // Build the codec for this object.
    public static final Codec<NotCondition> CODEC = CombiningConditionBase.codec(NotCondition::new);

    public NotCondition(List<IVeinCondition> conditions){
        // Let the superclass handle the conditions list.
        super(conditions);
    }

    @Override
    public boolean test(Context context){
        // Check that none of the conditions pass with the given worldgen context.
        return conditions.stream().noneMatch(c -> c.test(context));
    }

    @Override
    public String type() {
        // String identifier.
        return "dynamic_veins:not";
    }
}