package net.thebrewingminer.dynamicoreveins.codec.condition.predicate;

import com.mojang.serialization.Codec;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

public class AllConditions extends CombiningCondition{
    public static final Codec<AllConditions> CODEC = CombiningCondition.codec(AllConditions::new);

    public AllConditions(List<IVeinCondition> conditions){
        super(conditions);
    }

    @Override
    public boolean test(Context context){
        return conditions.stream().allMatch(c -> c.test(context));
    }

    @Override
    public Codec<? extends IVeinCondition> codec(){
        return CODEC;
    }

    @Override
    public String type(){
        return "dynamic_veins:all_of";
    }
}