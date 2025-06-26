package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;

public record AlwaysTrueCondition() implements IVeinCondition {
    public static final Codec<AlwaysTrueCondition> CODEC = Codec.unit(new AlwaysTrueCondition());

    @Override
    public boolean test(Context context) {
        return true;
    }

    @Override
    public Codec<? extends IVeinCondition> codec() {
        return CODEC;
    }

    @Override
    public String type() {
        return "dynamic_veins:always_true";
    }
}
