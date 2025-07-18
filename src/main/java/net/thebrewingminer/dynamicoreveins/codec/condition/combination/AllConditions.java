package net.thebrewingminer.dynamicoreveins.codec.condition.combination;

import com.mojang.serialization.Codec;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

public class AllConditions extends CombiningConditionBase {
    // Build the codec for this object.
    public static final Codec<AllConditions> CODEC = CombiningConditionBase.codec(AllConditions::new);

    public AllConditions(List<IVeinCondition> conditions){
        // Let the superclass handle the conditions list.
        super(conditions);
    }

    @Override
    public boolean test(Context context){
        // Check that all conditions pass with the given worldgen context.
        return conditions.stream().allMatch(c -> c.test(context));
    }

    @Override
    public String type(){
        // String identifier.
        return "dynamic_veins:all_of";
    }
}