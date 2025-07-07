package net.thebrewingminer.dynamicoreveins.helper;

import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.AllConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.AnyConditions;

import java.util.ArrayList;
import java.util.List;

public class FlattenConditions {
    private FlattenConditions(){}

    public static List<IVeinCondition> flattenConditions(IVeinCondition root) {
        List<IVeinCondition> flatList = new ArrayList<>();
        flattenInto(root, flatList);
        return flatList;
    }

    private static void flattenInto(IVeinCondition condition, List<IVeinCondition> output) {
        if (condition instanceof AllConditions all) {
            for (IVeinCondition sub : all.conditions()) {
                flattenInto(sub, output);
            }                                                   // Recursively split each condition list into a unified list.
        } else if (condition instanceof AnyConditions any) {
            for (IVeinCondition sub : any.conditions()) {
                flattenInto(sub, output);
            }
        } else {
            output.add(condition); // Base case: individual condition
        }
    }
}