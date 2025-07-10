package net.thebrewingminer.dynamicoreveins.codec.condition.combination;

import com.mojang.serialization.Codec;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

public class AnyConditions extends CombiningConditionBase {
    public static final Codec<AnyConditions> CODEC = CombiningConditionBase.codec(AnyConditions::new);

    public AnyConditions(List<IVeinCondition> conditions){
        super(conditions);
    }

    @Override
    public boolean test(Context context){
        return conditions.stream().anyMatch(c -> c.test(context));
    }

//    @Override
//    public Codec<? extends IVeinCondition> codec(){
//        return CODEC;
//    }

    @Override
    public String type(){
        return "dynamic_veins:any_of";
    }
}