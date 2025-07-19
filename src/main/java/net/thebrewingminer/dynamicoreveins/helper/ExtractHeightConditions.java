package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.thebrewingminer.dynamicoreveins.codec.condition.HeightRangeCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.AllConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.AnyConditions;
import net.thebrewingminer.dynamicoreveins.codec.condition.combination.NotCondition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExtractHeightConditions {
    // Helps "flatten" the condition logic tree of a vein's condition list.
    // In the context of this mod, height ranges of ore veins are not explicitly handled. Instead, they are derived from height range conditions.
    // This helper method essentially processes the config.conditions() object and parses its conditions, ignoring all basic predicates besides
    // height range predicates. This list is then passed onto FindMatchingHeightRange() to determine the valid height range to use for vein
    // generation (edge roundoff).

    private ExtractHeightConditions(){}

    public static List<IVeinCondition> extractHeightConditions(IVeinCondition root, IVeinCondition.Context context) {
        List<IVeinCondition> flatList = new ArrayList<>();      // The list to return afterward.
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()); // WG context to resolve height range bounds.
        flattenInto(root, flatList, worldGenerationContext);    // Traverse the conditions tree and flatten it into one list.

        return flatList;
    }

    private static void flattenInto(IVeinCondition condition, List<IVeinCondition> output, WorldGenerationContext worldGenerationContext) {
        // Recursively split each combining condition list into a unified output list.
        if (condition instanceof AllConditions all) {
            for (IVeinCondition sub : all.conditions()) {
                flattenInto(sub, output, worldGenerationContext);
            }
        } else if (condition instanceof AnyConditions any) {
            for (IVeinCondition sub : any.conditions()) {
                flattenInto(sub, output, worldGenerationContext);
            }
        } else if (condition instanceof NotCondition not) {
            // Special case: NOT.
            // Process the inner condition(s) of this object with a temporary list before adding to the output list.
            List<IVeinCondition> innerConditions = new ArrayList<>();

            // Recursively collect and flatten all conditions in the NotCondition object before attempting to invert.
            // Store that into the temporary list. This ensures all nested combinations are expanded first.
            for (IVeinCondition sub : not.conditions()) {
                flattenInto(sub, innerConditions, worldGenerationContext);
            }

            // Extract all HeightRangeCondition objects from the temporary list.
            List<HeightRangeCondition> innerNotHeightRanges = innerConditions.stream()
                    .filter(c -> c instanceof HeightRangeCondition)                 // Filters for only HeightRangeCondition instances.
                    .map(c -> (HeightRangeCondition)c)
                    .toList();

            // Compute the complement of any and all found height ranges in this NOT object.
            computeComplementRanges(innerNotHeightRanges, output, worldGenerationContext);
        } else if (condition instanceof HeightRangeCondition heightRange){
            output.add(heightRange);    // Base case: individual height range condition.
                                        // Any other leaf condition is ignored.
        }
    }

    private static void computeComplementRanges(List<HeightRangeCondition> negatedRanges, List<IVeinCondition> output, WorldGenerationContext worldGenerationContext){
        if (negatedRanges.isEmpty()) return;

        List<HeightRangeCondition> sortedRanges = negatedRanges.stream()
                .sorted(Comparator.comparingInt(h -> h.minInclusive().resolveY(worldGenerationContext)))    // Sort ranges in increasing order by minimum y.
                .toList();

        // Level height context.
        int worldMinY = worldGenerationContext.getMinGenY();
        int worldMaxY = worldMinY + worldGenerationContext.getGenDepth();
        int currY = worldMinY;  // Start iterating over ranges up from the minimum y.

        // Iterate through the height ranges and find gaps.
        for (HeightRangeCondition heightRange : sortedRanges){
            // Info from the current height range.
            int minY = heightRange.minInclusive().resolveY(worldGenerationContext);
            int maxY = heightRange.maxInclusive().resolveY(worldGenerationContext);

            // If there is a gap between the current y and the next minimum y, add the gap as a non-negated height range condition.
            if (currY < minY){
                output.add(new HeightRangeCondition(VerticalAnchor.absolute(currY), VerticalAnchor.absolute(minY - 1)));
            }
            // Move the current y to the beginning of the next gap (after the next negated height range).
            currY = Math.max(currY, maxY + 1);
        }

        // After going through all height range conditions in the list, check if there is still space between the last height range
        // and the world height limit. If so, add that as a valid height range.
        if (currY <= worldMaxY){
            output.add(new HeightRangeCondition(VerticalAnchor.absolute(currY), VerticalAnchor.absolute(worldMaxY)));
        }
    }
}