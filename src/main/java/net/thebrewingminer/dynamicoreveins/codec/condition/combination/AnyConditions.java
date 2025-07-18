package net.thebrewingminer.dynamicoreveins.codec.condition.combination;

import com.mojang.serialization.Codec;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

public class AnyConditions extends CombiningConditionBase {
    // Build the codec for this object.
    public static final Codec<AnyConditions> CODEC = CombiningConditionBase.codec(AnyConditions::new);

    public AnyConditions(List<IVeinCondition> conditions){
        // Let the superclass handle the conditions list.
        super(conditions);
    }

    @Override
    public boolean test(Context context){
        // Check that any condition matches with the given worldgen context.
        return conditions.stream().anyMatch(c -> c.test(context));
    }

    @Override
    public String type(){
        // String identifier.
        return "dynamic_veins:any_of";
    }
}