package net.thebrewingminer.dynamicoreveins.codec.condition.combination;

import com.mojang.serialization.Codec;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

public class NotCondition extends CombiningConditionBase {
    public static final Codec<NotCondition> CODEC = CombiningConditionBase.codec(NotCondition::new);

    public NotCondition(List<IVeinCondition> conditions){
        super(conditions);
    }

    @Override
    public boolean test(Context context){
        return conditions.stream().noneMatch(c -> c.test(context));
    }

//    @Override
//    public Codec<? extends IVeinCondition> codec(){
//        return CODEC;
//    }

    @Override
    public String type() {
        return "dynamic_veins:not";
    }
}