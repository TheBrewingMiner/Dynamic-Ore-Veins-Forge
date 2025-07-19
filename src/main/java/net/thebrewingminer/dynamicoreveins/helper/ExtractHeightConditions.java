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
    private ExtractHeightConditions(){}

    public static List<IVeinCondition> extractHeightConditions(IVeinCondition root, IVeinCondition.Context context) {
        List<IVeinCondition> flatList = new ArrayList<>();
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor());
        flattenInto(root, flatList, worldGenerationContext);
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
                .filter(c -> c instanceof HeightRangeCondition)
                .map(c -> (HeightRangeCondition)c)
                .sorted(Comparator.comparingInt(h -> h.minInclusive().resolveY(worldGenerationContext)))    // Sort by increasing order of minimum y's.
                .toList();

            // Compute the complement of any and all found height ranges.
            if (!innerNotHeightRanges.isEmpty()){
                // Reference the level's height bounds.
                int worldMinY = worldGenerationContext.getMinGenY();
                int worldMaxY = worldMinY + worldGenerationContext.getGenDepth();
                int currY = worldMinY;

                // Iterate through the height ranges and find gaps.
                for (HeightRangeCondition heightRangeCondition : innerNotHeightRanges){
                    int minY = heightRangeCondition.minInclusive().resolveY(worldGenerationContext);
                    int maxY = heightRangeCondition.maxInclusive().resolveY(worldGenerationContext);

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
        } else if (condition instanceof HeightRangeCondition heightRange){
            output.add(heightRange); // Base case: individual height range condition.
        }
    }
}