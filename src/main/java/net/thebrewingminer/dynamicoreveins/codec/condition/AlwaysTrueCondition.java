package net.thebrewingminer.dynamicoreveins.codec.condition;

import com.mojang.serialization.Codec;

public record AlwaysTrueCondition() implements IVeinCondition {
    // Build the codec.
    public static final Codec<AlwaysTrueCondition> CODEC = Codec.unit(new AlwaysTrueCondition());

    @Override
    public boolean test(Context context) {
        // Always true.
        return true;
    }

    @Override
    public String type() {
        // String identifier.
        return "dynamic_veins:always_true";
    }
}
