package net.thebrewingminer.dynamicoreveins.helper;

import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.thebrewingminer.dynamicoreveins.codec.condition.HeightRangeCondition;
import net.thebrewingminer.dynamicoreveins.codec.condition.IVeinCondition;

import java.util.List;

public class FindMatchingHeightRange {
    private FindMatchingHeightRange(){}

    public static HeightRangeWrapper findMatchingHeightRange(List<IVeinCondition> conditions, IVeinCondition.Context veinContext) {
        WorldGenerationContext worldGenContext = new WorldGenerationContext(veinContext.chunkGenerator(), veinContext.heightAccessor());
        HeightRangeWrapper firstMatchingRange = null;
        int matchingRangeCounter = 0;
        int DEFAULT_MIN_Y = worldGenContext.getMinGenY();
        int DEFAULT_MAX_Y = DEFAULT_MIN_Y + worldGenContext.getGenDepth();
        int minOverlapY = Integer.MIN_VALUE;
        int maxOverlapY = Integer.MAX_VALUE;
        int y = veinContext.pos().getY();

        for (IVeinCondition condition : conditions) {
            if (condition instanceof HeightRangeCondition heightCondition) {
                int minY = heightCondition.minInclusive().resolveY(worldGenContext);
                int maxY = heightCondition.maxInclusive().resolveY(worldGenContext);
                if (y >= minY && y <= maxY) {
                    matchingRangeCounter++;
                    if (firstMatchingRange == null){
                        firstMatchingRange = new HeightRangeWrapper(minY, maxY);    // For the first range the current pos is, find the min and max of that range.
                    }
                    if (minY > minOverlapY) minOverlapY = minY;                     // Set those as the found mins and maxes. If another height range is found for
                    if (maxY < maxOverlapY) maxOverlapY = maxY;                     // this position, expand to find the min and max of all found ranges.
                }
            }
        }
        if ((matchingRangeCounter > 1) && (minOverlapY <= maxOverlapY)){
            return (new HeightRangeWrapper(minOverlapY, maxOverlapY));      // Return a new range for the expanded range if more than one valid range was found for this pos.
        } else if (firstMatchingRange != null){                             // If only one valid range was found, return that one's range.
            return firstMatchingRange;
        } else {
            return new HeightRangeWrapper(DEFAULT_MIN_Y, DEFAULT_MAX_Y);    // Otherwise, default if no valid range was found.
        }
    }
}