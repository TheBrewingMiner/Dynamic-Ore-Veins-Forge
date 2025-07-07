package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

public class InThresholdHelper {
    private InThresholdHelper(){}

    // Practically a repeat of logic in DensityFunctionThreshold. This is used to compute a function that is known to
    // be wired to its noises already. It removes the need to create new DensityFunctionThreshold objects and without
    // double wiring (Not sure if double wiring causes issues, but to be safe, this will remain separate). Also cleans
    // up the mixin class.
    public static boolean inThreshold(DensityFunction function, double min, double max, IVeinCondition.Context veinContext) {
        int x = veinContext.pos().getX();
        int y = veinContext.pos().getY();
        int z = veinContext.pos().getZ();
        double value = function.compute(new DensityFunction.SinglePointContext(x, y, z));
        return (value >= min && value <= max);
    }
}